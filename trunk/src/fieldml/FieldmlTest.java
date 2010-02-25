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
import fieldml.evaluator.ContinuousPiecewiseEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.evaluator.MapEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldml.region.WorldRegion;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class FieldmlTest
    extends TestCase
{
    public static String REGION_NAME = "Fieldml_Test";


    public void testSerialization()
    {
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

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
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

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
        meshXY.evaluate( context );
        output = context.get( meshXY.getValueDomain() );
        assert output.values[0] == 25;
        assert output.values[1] == 5;
        
        
        
/*
        //Contexty
        context.set( meshDomain, 4, 0.5, 0.5 );
        meshXY.evaluate( context );
        output = context.get( meshXY.getValueDomain() );
        
        // Derivates and maps are difficult/impossible.

        //Pipeliney
        EnsembleConstantEvaluator element = new EnsembleConstantEvaluator( meshDomain.elementDomain );
        element.setValue( 3 );
        
        ContinuousConstantEvaluator xi = new ContinuousConstantEvaluator( meshDomain.chartDomain );
        element.setValue( 0.5, 0.5 );
        
        meshXYPressure.setVariable( "test_mesh.element_value", element );
        meshXYPressure.setVariable( "test_mesh.xi_value", xi );
        
        foo = meshXY.evaluate();
        
        pressure.setVariable( "test_mesh.element_value", element );
        pressure.setVariable( "test_mesh.xi_value", xi );
        
        bob = pressure.evaluate();
        
        element.setValue( 4 );
        
        bar = meshXY.evaluate();
        
        // Repetitive set value calls. Multiple return types impossible (without structs).
*/
    }


    public static Region buildRegion( Region parent )
    {
        Region testRegion = new SubRegion( REGION_NAME, parent );
        Region library = testRegion.getLibrary();

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements", 1, 2, 3, 4 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad" );
        meshDomain.setShape( 2, "library.shape.triangle" );
        meshDomain.setShape( 3, "library.shape.triangle" );
        meshDomain.setShape( 4, "library.shape.quad" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 );
        testRegion.addDomain( globalNodesDomain );
        
        EnsembleDomain anonymous = library.getEnsembleDomain( "library.anonymous" );

        EnsembleDomain globalNodesListDomain = new EnsembleDomain( "test_mesh.nodes_list", anonymous, globalNodesDomain );
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

        ContinuousDomain rc1Domain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain rc2Domain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

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

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.mesh.dofs", rc1Domain );

        MapEvaluator elementBilinearLagrange = new MapEvaluator( "test_mesh.element.bilinear_lagrange", rc1Domain, quadNodeList,
            bilinearLagrange, dofs );
        testRegion.addEvaluator( elementBilinearLagrange );
        MapEvaluator elementBilinearSimplex = new MapEvaluator( "test_mesh.element.bilinear_simplex", rc1Domain, triangleNodeList,
            bilinearSimplex, dofs );
        testRegion.addEvaluator( elementBilinearSimplex );
        MapEvaluator elementBiquadraticLagrange = new MapEvaluator( "test_mesh.element.biquadratic_lagrange", rc1Domain, biquadNodeList,
            biquadraticLagrange, dofs );
        testRegion.addEvaluator( elementBiquadraticLagrange );

        ContinuousPiecewiseEvaluator meshCoordinatesT1 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.template1", rc1Domain, testMeshElementDomain );
        meshCoordinatesT1.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesT1.setEvaluator( 2, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 3, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 4, elementBilinearLagrange );
        testRegion.addEvaluator( meshCoordinatesT1 );

        ContinuousPiecewiseEvaluator meshCoordinatesT2 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.template2", rc1Domain, testMeshElementDomain );
        meshCoordinatesT2.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesT2.setEvaluator( 2, elementBilinearSimplex );
        meshCoordinatesT2.setEvaluator( 3, elementBilinearSimplex );
        meshCoordinatesT2.setEvaluator( 4, elementBiquadraticLagrange );
        testRegion.addEvaluator( meshCoordinatesT2 );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", rc1Domain, globalNodesDomain );
        meshX.setValue( 1, 00.0 );
        meshX.setValue( 2, 10.0 );
        meshX.setValue( 3, 20.0 );
        meshX.setValue( 4, 00.0 );
        meshX.setValue( 5, 10.0 );
        meshX.setValue( 6, 20.0 );
        meshX.setValue( 7, 30.0 );
        meshX.setValue( 13, 30.0 );

        testRegion.addEvaluator( meshX );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", rc1Domain, meshCoordinatesT1 );
        meshCoordinatesX.setVariable( "test_mesh.mesh.dofs", meshX );
        testRegion.addEvaluator( meshCoordinatesX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", rc1Domain, globalNodesDomain );
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

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", rc1Domain, meshCoordinatesT2 );
        meshCoordinatesY.setVariable( "test_mesh.mesh.dofs", meshY );
        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", rc2Domain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }
}
