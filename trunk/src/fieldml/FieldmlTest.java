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
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.MapEvaluator;
import fieldml.evaluator.hardcoded.BilinearLagrange;
import fieldml.evaluator.hardcoded.BilinearSimplex;
import fieldml.evaluator.hardcoded.BiquadraticLagrange;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

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
            PrintStream output = new PrintStream( "trunk\\data\\" + getClass().getSimpleName() + ".xml" );
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

        DomainValues context = new DomainValues();

        ContinuousDomainValue output;

        // Test element 1
        context.set( meshDomain, 1, 0.0, 0.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 0;

        context.set( meshDomain, 1, 0.0, 1.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 0;

        context.set( meshDomain, 1, 0.5, 0.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 5;

        context.set( meshDomain, 1, 1.0, 0.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 10;

        context.set( meshDomain, 1, 1.0, 1.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 10;

        // Test element 2
        context.set( meshDomain, 2, 0.0, 0.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 10;

        context.set( meshDomain, 2, 1.0, 0.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 10;

        context.set( meshDomain, 2, 0.0, 1.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 20;

        context.set( meshDomain, 2, 0.5, 0.5 );
        output = meshX.evaluate( context );
        assert output.values[0] == 15;

        // Test element 3
        context.set( meshDomain, 3, 0.0, 0.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 20;

        context.set( meshDomain, 3, 1.0, 0.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 20;

        context.set( meshDomain, 3, 0.0, 1.0 );
        output = meshX.evaluate( context );
        assert output.values[0] == 10;

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshX.evaluate( context );
        assert output.values[0] == 15;

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 15;
        assert output.values[1] == 5;

        context.set( meshDomain, 4, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 5;
    }


    public static Region buildRegion()
    {
        Region library = Region.getLibrary();
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

        EnsembleListParameters triangleNodeList = new EnsembleListParameters( "test_mesh.triangle_nodes", globalNodesListDomain,
            testMeshElementDomain );
        triangleNodeList.setValue( 2, 2, 5, 3 );
        triangleNodeList.setValue( 3, 6, 3, 5 );
        testRegion.addEvaluator( triangleNodeList );

        EnsembleListParameters quadNodeList = new EnsembleListParameters( "test_mesh.quad_nodes", globalNodesListDomain,
            testMeshElementDomain );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 4, 6, 13, 3, 7 );

        testRegion.addEvaluator( quadNodeList );

        EnsembleListParameters biquadNodeList = new EnsembleListParameters( "test_mesh.biquad_nodes", globalNodesListDomain,
            testMeshElementDomain );
        biquadNodeList.setValue( 4, 6, 12, 13, 8, 9, 10, 3, 11, 7 );

        testRegion.addEvaluator( biquadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodesDomain );
        meshX.setValue( 1, 00.0 );
        meshX.setValue( 2, 10.0 );
        meshX.setValue( 3, 20.0 );
        meshX.setValue( 4, 00.0 );
        meshX.setValue( 5, 10.0 );
        meshX.setValue( 6, 20.0 );
        meshX.setValue( 7, 30.0 );
        meshX.setValue( 13, 30.0 );

        testRegion.addEvaluator( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", mesh1DDomain, globalNodesDomain );
        meshY.setValue( 1, 10.0 );
        meshY.setValue( 2, 10.0 );
        meshY.setValue( 3, 10.0 );
        meshY.setValue( 4, 00.0 );
        meshY.setValue( 5, 00.0 );
        meshY.setValue( 6, 00.0 );
        meshY.setValue( 7, 10.0 );
        meshY.setValue( 8, 05.0 );
        meshY.setValue( 9, 05.0 );
        meshY.setValue( 10, 05.0 );
        meshY.setValue( 11, 10.0 );
        meshY.setValue( 12, 00.0 );
        meshY.setValue( 13, 00.0 );

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

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.mesh.dofs", mesh1DDomain );

        MapEvaluator elementBilinearLagrange = new MapEvaluator( "test_mesh.element.bilinear_lagrange", mesh1DDomain, quadNodeList,
            bilinearLagrange, dofs );
        testRegion.addEvaluator( elementBilinearLagrange );
        MapEvaluator elementBilinearSimplex = new MapEvaluator( "test_mesh.element.bilinear_simplex", mesh1DDomain, triangleNodeList,
            bilinearSimplex, dofs );
        testRegion.addEvaluator( elementBilinearSimplex );
        MapEvaluator elementBiquadraticLagrange = new MapEvaluator( "test_mesh.element.biquadratic_lagrange", mesh1DDomain, biquadNodeList,
            biquadraticLagrange, dofs );
        testRegion.addEvaluator( elementBiquadraticLagrange );

        PiecewiseTemplate meshCoordinatesT1 = new PiecewiseTemplate( "test_mesh.coordinates.template1", meshDomain );
        meshCoordinatesT1.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesT1.setEvaluator( 2, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 3, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 4, elementBilinearLagrange );
        testRegion.addPiecewiseTemplate( meshCoordinatesT1 );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesT1 );
        meshCoordinatesX.setVariable( "test_mesh.mesh.dofs", meshX );
        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseTemplate meshCoordinatesT2 = new PiecewiseTemplate( "test_mesh.coordinates.template2", meshDomain );
        meshCoordinatesT2.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesT2.setEvaluator( 2, elementBilinearSimplex );
        meshCoordinatesT2.setEvaluator( 3, elementBilinearSimplex );
        meshCoordinatesT2.setEvaluator( 4, elementBiquadraticLagrange );
        testRegion.addPiecewiseTemplate( meshCoordinatesT2 );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesT2 );
        meshCoordinatesY.setVariable( "test_mesh.mesh.dofs", meshY );
        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }
}
