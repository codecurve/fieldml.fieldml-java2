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
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.evaluator.MapEvaluator;
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

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements", 1, 2, 3, 4 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.triangle.00_10_01" );
        meshDomain.setShape( 3, "library.shape.triangle.00_10_01" );
        meshDomain.setShape( 4, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 );
        testRegion.addDomain( globalNodesDomain );

        EnsembleDomain globalNodesListDomain = new EnsembleDomain( "test_mesh.nodes_list", globalNodesDomain );
        testRegion.addDomain( globalNodesListDomain );

        EnsembleParameters triangleNodeList = new EnsembleParameters( "test_mesh.triangle_nodes", globalNodesListDomain,
            testMeshElementDomain );
        triangleNodeList.setValue( 2, 2, 5, 3 );
        triangleNodeList.setValue( 3, 6, 3, 5 );
        testRegion.addEvaluator( triangleNodeList );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesListDomain,
            testMeshElementDomain );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 4, 6, 13, 3, 7 );

        testRegion.addEvaluator( quadNodeList );

        EnsembleParameters biquadNodeList = new EnsembleParameters( "test_mesh.biquad_nodes", globalNodesListDomain,
            testMeshElementDomain );
        biquadNodeList.setValue( 4, 6, 12, 13, 8, 9, 10, 3, 11, 7 );

        testRegion.addEvaluator( biquadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.list" );

        FunctionEvaluator bilinearLagrange = new FunctionEvaluator( "test_mesh.bilinear_lagrange", weightingDomain, meshDomain, library
            .getContinuousFunction( "library.function.bilinear_lagrange" ) );
        testRegion.addEvaluator( bilinearLagrange );

        FunctionEvaluator biquadraticLagrange = new FunctionEvaluator( "test_mesh.biquadratic_lagrange", weightingDomain, meshDomain, library
            .getContinuousFunction( "library.function.biquadratic_lagrange" ) );
        testRegion.addEvaluator( biquadraticLagrange );

        FunctionEvaluator bilinearSimplex = new FunctionEvaluator( "test_mesh.bilinear_simplex", weightingDomain, meshDomain, library
            .getContinuousFunction( "library.function.bilinear_simplex" ) );
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

        /*
        PiecewiseTemplate meshInterpolationT3 = new PiecewiseTemplate( "test_mesh.coordinates.template3", meshDomain );
        meshInterpolationT3.setEvaluator( 1, bilinearLagrange );
        meshInterpolationT3.setEvaluator( 2, bilinearSimplex );
        meshInterpolationT3.setEvaluator( 3, bilinearSimplex );
        meshInterpolationT3.setEvaluator( 4, bilinearLagrange );
        testRegion.addPiecewiseTemplate( meshInterpolationT3 );
        PiecewiseTemplate meshIndexesT3 = new PiecewiseTemplate( "test_mesh.coordinates.template3", meshDomain, globalNodesListDomain );
        meshIndexesT3.setEvaluator( 1, quadNodeList );
        meshIndexesT3.setEvaluator( 2, triangleNodeList );
        meshIndexesT3.setEvaluator( 3, triangleNodeList );
        meshIndexesT3.setEvaluator( 4, quadNodeList );
        testRegion.addPiecewiseTemplate( meshIndexesT3 );
        MapEvaluator template3Evaluator = new MapEvaluator( "test_mesh.template3", mesh1DDomain, meshIndexesT3,
            meshInterpolationT3, dofs );
            */
        
        PiecewiseTemplate meshCoordinatesT1 = new PiecewiseTemplate( "test_mesh.coordinates.template1", meshDomain );
        meshCoordinatesT1.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesT1.setEvaluator( 2, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 3, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 4, elementBilinearLagrange );
        testRegion.addPiecewiseTemplate( meshCoordinatesT1 );

        PiecewiseTemplate meshCoordinatesT2 = new PiecewiseTemplate( "test_mesh.coordinates.template2", meshDomain );
        meshCoordinatesT2.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesT2.setEvaluator( 2, elementBilinearSimplex );
        meshCoordinatesT2.setEvaluator( 3, elementBilinearSimplex );
        meshCoordinatesT2.setEvaluator( 4, elementBiquadraticLagrange );
        testRegion.addPiecewiseTemplate( meshCoordinatesT2 );

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

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesT1 );
        meshCoordinatesX.setVariable( "test_mesh.mesh.dofs", meshX );
        testRegion.addEvaluator( meshCoordinatesX );

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
