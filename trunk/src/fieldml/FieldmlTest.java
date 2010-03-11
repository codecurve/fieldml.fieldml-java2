package fieldml;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousCompositeEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousPiecewiseEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.field.PiecewiseField;
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldml.region.WorldRegion;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class FieldmlTest
    extends FieldmlTestCase
{
    public static String REGION_NAME = "Fieldml_Test";


    public void testSerialization()
    {
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1____2____3_11_7\n" );
        s.append( "|    |   /|    |\n" );
        s.append( "|    |*2/ | *4 |\n" );
        s.append( "| *1 | /  8  9 10\n" );
        s.append( "|    |/*3 |    |\n" );
        s.append( "4____5____6_12_13\n" );

        serialize( region, s.toString() ); 
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
        output = meshXY.evaluate( context );
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
        
        context.setVariable( "test_mesh.element_value", element );
        meshXYPressure.setVariable( "test_mesh.xi_value", xi );
        
        foo = meshXY.evaluate(context);
        
        
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

        EnsembleDomain xiComponentDomain = library.getEnsembleDomain( "library.coordinates.rc.2d" );

        MeshDomain meshDomain = new MeshDomain( testRegion, "test_mesh.domain", xiComponentDomain, 4 );
        meshDomain.setShape( 1, "library.shape.quad" );
        meshDomain.setShape( 2, "library.shape.triangle" );
        meshDomain.setShape( 3, "library.shape.triangle" );
        meshDomain.setShape( 4, "library.shape.quad" );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 );
        
        EnsembleDomain anonymous = library.getEnsembleDomain( "library.anonymous" );

        EnsembleDomain globalNodesListDomain = new EnsembleDomain( testRegion, "test_mesh.nodes_list", anonymous, globalNodesDomain );

        EnsembleParameters triangleNodeList = new EnsembleParameters( "test_mesh.triangle_nodes", globalNodesListDomain,
            meshDomain.getElementDomain() );
        triangleNodeList.setValue( 2, 2, 5, 3 );
        triangleNodeList.setValue( 3, 6, 3, 5 );
        testRegion.addEvaluator( triangleNodeList );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesListDomain,
            meshDomain.getElementDomain() );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 4, 6, 13, 3, 7 );

        testRegion.addEvaluator( quadNodeList );

        EnsembleParameters biquadNodeList = new EnsembleParameters( "test_mesh.biquad_nodes", globalNodesListDomain,
            meshDomain.getElementDomain() );
        biquadNodeList.setValue( 4, 6, 12, 13, 8, 9, 10, 3, 11, 7 );

        testRegion.addEvaluator( biquadNodeList );
        
        meshDomain.setPointConnectivity( "simplex_1x1", triangleNodeList );
        meshDomain.setPointConnectivity( "quad_1x1", quadNodeList );
        meshDomain.setPointConnectivity( "quad_2x2", biquadNodeList );

        ContinuousDomain rc1Domain = library.getContinuousDomain( "library.coordinates.rc.1d" );
        ContinuousDomain rc2Domain = library.getContinuousDomain( "library.coordinates.rc.2d" );

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.mesh.dofs", rc1Domain, globalNodesDomain );
        testRegion.addEvaluator( dofs );

        ContinuousDomain parameterList = library.getContinuousDomain( "library.parameter.list" );

        ContinuousEvaluator bilinearLagrange = library.getContinuousEvaluator( "library.fem.bilinear_lagrange" );
        
        ContinuousCompositeEvaluator elementBilinearLagrange = new ContinuousCompositeEvaluator( "test_mesh.element.bilinear_lagrange", rc1Domain );
        elementBilinearLagrange.aliasValue( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.2d" ) );
        elementBilinearLagrange.importField( quadNodeList );
        elementBilinearLagrange.importField( dofs, parameterList, globalNodesListDomain );
        elementBilinearLagrange.importField( bilinearLagrange );
        elementBilinearLagrange.aliasValue( library.getContinuousDomain( "library.real.1d" ), rc1Domain );
        testRegion.addEvaluator( elementBilinearLagrange );
        
        ContinuousEvaluator biquadraticLagrange = library.getContinuousEvaluator( "library.fem.biquadratic_lagrange" );

        ContinuousCompositeEvaluator elementBiquadraticLagrange = new ContinuousCompositeEvaluator( "test_mesh.element.biquadratic_lagrange", rc1Domain );
        elementBiquadraticLagrange.aliasValue( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.2d" ) );
        elementBiquadraticLagrange.importField( biquadNodeList );
        elementBiquadraticLagrange.importField( dofs, parameterList, globalNodesListDomain );
        elementBiquadraticLagrange.importField( biquadraticLagrange );
        elementBiquadraticLagrange.aliasValue( library.getContinuousDomain( "library.real.1d" ), rc1Domain );
        testRegion.addEvaluator( elementBiquadraticLagrange );
        
        ContinuousEvaluator bilinearSimplex = library.getContinuousEvaluator( "library.fem.bilinear_simplex" );

        ContinuousCompositeEvaluator elementBilinearSimplex = new ContinuousCompositeEvaluator( "test_mesh.element.bilinear_simplex", rc1Domain );
        elementBilinearSimplex.aliasValue( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.2d" ) );
        elementBilinearSimplex.importField( triangleNodeList );
        elementBilinearSimplex.importField( dofs, parameterList, globalNodesListDomain );
        elementBilinearSimplex.importField( bilinearSimplex );
        elementBilinearSimplex.aliasValue( library.getContinuousDomain( "library.real.1d" ), rc1Domain );
        testRegion.addEvaluator( elementBilinearSimplex );
        

        ContinuousPiecewiseEvaluator meshCoordinatesT1 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.template1", rc1Domain, meshDomain.getElementDomain() );
        meshCoordinatesT1.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesT1.setEvaluator( 2, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 3, elementBilinearSimplex );
        meshCoordinatesT1.setEvaluator( 4, elementBilinearLagrange );
        testRegion.addEvaluator( meshCoordinatesT1 );

        ContinuousPiecewiseEvaluator meshCoordinatesT2 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.template2", rc1Domain, meshDomain.getElementDomain() );
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
        
        meshDomain.addField( meshCoordinatesT1 );
        meshDomain.addField( meshCoordinatesT2 );
        meshDomain.addField( meshCoordinates );

        return testRegion;
    }
}
