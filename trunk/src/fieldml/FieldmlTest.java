package fieldml;

import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format.TextMode;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.hardcoded.BilinearLagrange;
import fieldml.evaluator.hardcoded.BilinearSimplex;
import fieldml.evaluator.hardcoded.BiquadraticLagrange;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.JdomReflectiveHandler;
import fieldml.map.IndirectMap;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class FieldmlTest
    extends TestCase
{
    public static String REGION_NAME = "Fieldml_Test";
    
    public void testSerialization()
    {
        Region region = buildRegion();

        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1____2____3_11_7\n" );
        s.append( "|    |   /|    |\n" );
        s.append( "|    |*2/ | *4 |\n" );
        s.append( "| *1 | /  8  9 10\n" );
        s.append( "|    |/*3 |    |\n" );
        s.append( "4____5____6_12_13\n" );

        Comment comment1 = new Comment( s.toString() );
        root.addContent( comment1 );

        JdomReflectiveHandler handler = new JdomReflectiveHandler( root );
        region.walkObjects( handler );

        Format format = Format.getPrettyFormat();
        format.setTextMode( TextMode.PRESERVE );
        XMLOutputter outputter = new XMLOutputter( format );
        try
        {
            PrintStream output = new PrintStream( "trunk\\data\\" + getClass().getSimpleName()  + ".xml");
            outputter.output( doc, output );
        }
        catch( IOException e )
        {
            System.err.println( e );
        }
    }


    public void testEvaluation()
    {
        Region region = buildRegion();

        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        ContinuousEvaluator meshX = region.getContinuousEvaluator( "test_mesh.coordinates.x" );
        ContinuousEvaluator meshXY = region.getContinuousEvaluator( "test_mesh.coordinates.xy" );

        ContinuousDomainValue output;

        // Test element 1
        output = meshX.evaluate( meshDomain, 1, 0.0, 0.0 );
        assert output.values[0] == 0;

        output = meshX.evaluate( meshDomain, 1, 0.0, 1.0 );
        assert output.values[0] == 0;

        output = meshX.evaluate( meshDomain, 1, 0.5, 0.0 );
        assert output.values[0] == 5;

        output = meshX.evaluate( meshDomain, 1, 1.0, 0.0 );
        assert output.values[0] == 10;

        output = meshX.evaluate( meshDomain, 1, 1.0, 1.0 );
        assert output.values[0] == 10;

        // Test element 2
        output = meshX.evaluate( meshDomain, 2, 0.0, 0.0 );
        assert output.values[0] == 10;

        output = meshX.evaluate( meshDomain, 2, 1.0, 0.0 );
        assert output.values[0] == 10;

        output = meshX.evaluate( meshDomain, 2, 0.0, 1.0 );
        assert output.values[0] == 20;

        output = meshX.evaluate( meshDomain, 2, 0.5, 0.5 );
        assert output.values[0] == 15;

        // Test element 3
        output = meshX.evaluate( meshDomain, 3, 0.0, 0.0 );
        assert output.values[0] == 20;

        output = meshX.evaluate( meshDomain, 3, 1.0, 0.0 );
        assert output.values[0] == 20;

        output = meshX.evaluate( meshDomain, 3, 0.0, 1.0 );
        assert output.values[0] == 10;

        output = meshX.evaluate( meshDomain, 3, 0.5, 0.5 );
        assert output.values[0] == 15;

        output = meshXY.evaluate( meshDomain, 3, 0.5, 0.5 );
        assert output.values[0] == 15;
        assert output.values[1] == 5;

        output = meshXY.evaluate( meshDomain, 4, 0.5, 0.5 );
        assert output.values[0] == 25;
        assert output.values[1] == 5;
    }


    public static Region buildRegion()
    {
        Region testRegion = new Region( REGION_NAME );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3, 4 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.triangle.00_10_01" );
        meshDomain.setShape( 3, "library.shape.triangle.00_10_01" );
        meshDomain.setShape( 4, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 );
        testRegion.addDomain( globalNodesDomain );
        
        EnsembleListDomain globalNodesListDomain = new EnsembleListDomain( "test_mesh.nodes_list", globalNodesDomain );
        testRegion.addDomain( globalNodesListDomain );

        EnsembleListParameters triangleNodeList = new EnsembleListParameters( "test_mesh.triangle_nodes", globalNodesListDomain, testMeshElementDomain );
        triangleNodeList.setValue( 2, 2, 5, 3 );
        triangleNodeList.setValue( 3, 6, 3, 5 );
        testRegion.addEvaluator( triangleNodeList );

        EnsembleListParameters quadNodeList = new EnsembleListParameters( "test_mesh.quad_nodes", globalNodesListDomain, testMeshElementDomain );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 4, 6, 13, 3, 7 );

        testRegion.addEvaluator( quadNodeList );

        EnsembleListParameters biquadNodeList = new EnsembleListParameters( "test_mesh.biquad_nodes", globalNodesListDomain, testMeshElementDomain );
        biquadNodeList.setValue( 4, 6, 12, 13, 8, 9, 10, 3, 11, 7 );

        testRegion.addEvaluator( biquadNodeList );

        ContinuousDomain meshXdomain = new ContinuousDomain( "test_mesh.co-ordinates.x", 1 );
        testRegion.addDomain( meshXdomain );

        ContinuousDomain meshYdomain = new ContinuousDomain( "test_mesh.co-ordinates.y", 1 );
        testRegion.addDomain( meshYdomain );

        ContinuousDomain meshXYdomain = new ContinuousDomain( "test_mesh.co-ordinates.xy", 2 );
        testRegion.addDomain( meshXYdomain );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", meshXdomain, globalNodesDomain );
        meshX.setValue( 00.0, 1 );
        meshX.setValue( 10.0, 2 );
        meshX.setValue( 20.0, 3 );
        meshX.setValue( 00.0, 4 );
        meshX.setValue( 10.0, 5 );
        meshX.setValue( 20.0, 6 );
        meshX.setValue( 30.0, 7 );
        meshX.setValue( 30.0, 13 );

        testRegion.addEvaluator( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", meshYdomain, globalNodesDomain );
        meshY.setValue( 10.0, 1 );
        meshY.setValue( 10.0, 2 );
        meshY.setValue( 10.0, 3 );
        meshY.setValue( 00.0, 4 );
        meshY.setValue( 00.0, 5 );
        meshY.setValue( 00.0, 6 );
        meshY.setValue( 10.0, 7 );
        meshY.setValue( 05.0, 8 );
        meshY.setValue( 05.0, 9 );
        meshY.setValue( 05.0, 10 );
        meshY.setValue( 10.0, 11 );
        meshY.setValue( 00.0, 12 );
        meshY.setValue( 00.0, 13 );

        testRegion.addEvaluator( meshY );
        
        /*
         * 
         * Because piecewise fields are strictly scalar, there is (probably) no reason to share evaluators. Aggregate fields
         * wishing to share components can do so simply by sharing entire piecewise fields.
         */
        ContinuousListEvaluator bilinearLagrange = new BilinearLagrange( "test_mesh.mesh.bilinear_lagrange", meshDomain );
        testRegion.addEvaluator( bilinearLagrange );

        ContinuousListEvaluator biquadraticLagrange = new BiquadraticLagrange( "test_mesh.mesh.biquadratic_lagrange", meshDomain );
        testRegion.addEvaluator( biquadraticLagrange );

        ContinuousListEvaluator bilinearSimplex = new BilinearSimplex( "test_mesh.mesh.bilinear_simplex", meshDomain );
        testRegion.addEvaluator( bilinearSimplex );
        
        IndirectMap elementBilinearLagrange = new IndirectMap( "test_mesh.element.bilinear_lagrange", quadNodeList, bilinearLagrange );
        testRegion.addMap( elementBilinearLagrange );
        IndirectMap elementBilinearSimplex = new IndirectMap( "test_mesh.element.bilinear_simplex", triangleNodeList, bilinearSimplex ); 
        testRegion.addMap( elementBilinearSimplex );
        IndirectMap elementBiquadraticLagrange = new IndirectMap( "test_mesh.element.biquadratic_lagrange", biquadNodeList, biquadraticLagrange ); 
        testRegion.addMap( elementBiquadraticLagrange );

        PiecewiseTemplate meshCoordinatesT1 = new PiecewiseTemplate( "test_mesh.coordinates.template1", meshDomain, 1 );
        meshCoordinatesT1.setMap( 1, elementBilinearLagrange, 1 );
        meshCoordinatesT1.setMap( 2, elementBilinearSimplex, 1 );
        meshCoordinatesT1.setMap( 3, elementBilinearSimplex, 1 );
        meshCoordinatesT1.setMap( 4, elementBilinearLagrange, 1 );
        testRegion.addPiecewiseTemplate( meshCoordinatesT1 );
        
        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", meshXdomain, meshCoordinatesT1 );
        meshCoordinatesX.setDofs( 1, meshX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseTemplate meshCoordinatesT2 = new PiecewiseTemplate( "test_mesh.coordinates.template2", meshDomain, 1 );
        meshCoordinatesT2.setMap( 1, elementBilinearLagrange, 1 );
        meshCoordinatesT2.setMap( 2, elementBilinearSimplex, 1 );
        meshCoordinatesT2.setMap( 3, elementBilinearSimplex, 1 );
        meshCoordinatesT2.setMap( 4, elementBiquadraticLagrange, 1 );
        testRegion.addPiecewiseTemplate( meshCoordinatesT2 );
        
        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", meshYdomain, meshCoordinatesT2 );
        meshCoordinatesY.setDofs( 1, meshY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", meshXYdomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );
        
        return testRegion;
    }
}
