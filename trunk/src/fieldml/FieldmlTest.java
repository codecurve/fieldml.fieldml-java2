package fieldml;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousPiecewiseEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.ImportedContinuousEvaluator;
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
        ImportedContinuousEvaluator meshX = region.importContinuousEvaluator( "x", "test_mesh.coordinates.x" );
        ImportedContinuousEvaluator meshXY = region.importContinuousEvaluator( "xy", "test_mesh.coordinates.xy" );

        DomainValues context = new DomainValues();

        ContinuousDomainValue output;

        // Test element 1
        context.set( meshDomain, 1, 0.0, 0.0 );
        output = meshX.getValue( context );
        assertEquals( 0.0, output.values[0] );

        context.set( meshDomain, 1, 0.0, 1.0 );
        output = meshX.getValue( context );
        assertEquals( 0.0, output.values[0] );

        context.set( meshDomain, 1, 0.5, 0.0 );
        output = meshX.getValue( context );
        assertEquals( 5.0, output.values[0] );

        context.set( meshDomain, 1, 1.0, 0.0 );
        output = meshX.getValue( context );
        assertEquals( 10.0, output.values[0] );

        context.set( meshDomain, 1, 1.0, 1.0 );
        output = meshX.getValue( context );
        assertEquals( 10.0, output.values[0] );

        // Test element 2
        context.set( meshDomain, 2, 0.0, 0.0 );
        output = meshX.getValue( context );
        assertEquals( 10.0, output.values[0] );

        context.set( meshDomain, 2, 1.0, 0.0 );
        output = meshX.getValue( context );
        assertEquals( 10.0, output.values[0] );

        context.set( meshDomain, 2, 0.0, 1.0 );
        output = meshX.getValue( context );
        assertEquals( 20.0, output.values[0] );

        context.set( meshDomain, 2, 0.5, 0.5 );
        output = meshX.getValue( context );
        assertEquals( 15.0, output.values[0] );

        // Test element 3
        context.set( meshDomain, 3, 0.0, 0.0 );
        output = meshX.getValue( context );
        assertEquals( 20.0, output.values[0] );

        context.set( meshDomain, 3, 1.0, 0.0 );
        output = meshX.getValue( context );
        assertEquals( 20.0, output.values[0] );

        context.set( meshDomain, 3, 0.0, 1.0 );
        output = meshX.getValue( context );
        assertEquals( 10.0, output.values[0] );

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshX.getValue( context );
        assertEquals( 15.0, output.values[0] );

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshXY.getValue( context );
        assertEquals( 15.0, output.values[0] );
        assertEquals( 5.0, output.values[1] );

        context.set( meshDomain, 4, 0.5, 0.5 );
        meshXY.getValue( context );
        output = meshXY.getValue( context );
        assertEquals( 25.0, output.values[0] );
        assertEquals( 5.0, output.values[1] );
    }


    public static Region buildRegion( Region parent )
    {
        Region testRegion = new SubRegion( REGION_NAME, parent );
        Region library = testRegion.getLibrary();

        EnsembleDomain rc2Ensemble = library.getEnsembleDomain( "library.ensemble.xi.2d" );
        ContinuousDomain rc1Domain = library.getContinuousDomain( "library.coordinates.rc.1d" );
        ContinuousDomain rc2Domain = library.getContinuousDomain( "library.coordinates.rc.2d" );
        EnsembleDomain tri2x2LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.triangle.2x2" );
        EnsembleDomain quad2x2LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.2x2" );
        EnsembleDomain quad3x3LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.3x3" );

        MeshDomain meshDomain = new MeshDomain( testRegion, "test_mesh.domain", rc2Ensemble, 4 );
        meshDomain.setShape( 1, "library.shape.quad" );
        meshDomain.setShape( 2, "library.shape.triangle" );
        meshDomain.setShape( 3, "library.shape.triangle" );
        meshDomain.setShape( 4, "library.shape.quad" );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", 13 );

        EnsembleParameters triangleNodeList = new EnsembleParameters( "test_mesh.triangle2x2_nodes", globalNodesDomain, meshDomain
            .getElementDomain(), tri2x2LocalNodeDomain );
        triangleNodeList.setValue( 2, 2, 5, 3 );
        triangleNodeList.setValue( 3, 6, 3, 5 );
        testRegion.addEvaluator( triangleNodeList );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad2x2_nodes", globalNodesDomain, meshDomain
            .getElementDomain(), quad2x2LocalNodeDomain );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 4, 6, 13, 3, 7 );
        testRegion.addEvaluator( quadNodeList );

        EnsembleParameters biquadNodeList = new EnsembleParameters( "test_mesh.quad3x3_nodes", globalNodesDomain, meshDomain
            .getElementDomain(), quad3x3LocalNodeDomain );
        biquadNodeList.setValue( 4, 6, 12, 13, 8, 9, 10, 3, 11, 7 );
        testRegion.addEvaluator( biquadNodeList );

        meshDomain.setPointConnectivity( tri2x2LocalNodeDomain, triangleNodeList );
        meshDomain.setPointConnectivity( quad2x2LocalNodeDomain, quadNodeList );
        meshDomain.setPointConnectivity( quad3x3LocalNodeDomain, biquadNodeList );

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.mesh.dofs", rc1Domain );
        testRegion.addEvaluator( dofs );

        // Bilinear Lagrange
        ContinuousDomain libraryBilinearLagrangeParams = library.getContinuousDomain( "library.parameters.bilinear_lagrange" );

        ImportedContinuousEvaluator meshBilinearLagrangeParams = testRegion.importContinuousEvaluator( "test_mesh.element.bilinear_lagrange.params", "test_mesh.mesh.dofs" );
        meshBilinearLagrangeParams.alias( quadNodeList, globalNodesDomain );
        testRegion.addEvaluator( meshBilinearLagrangeParams );

        ImportedContinuousEvaluator bilinearLagrange = library.importContinuousEvaluator( "test_mesh.bilinear_lagrange", "library.fem.bilinear_lagrange" );
        bilinearLagrange.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.2d" ) );
        bilinearLagrange.alias( meshBilinearLagrangeParams, libraryBilinearLagrangeParams );
        testRegion.addEvaluator( bilinearLagrange );

        // Biquadratic Lagrange
        ContinuousDomain libraryBiquadraticLagrangeParams = library.getContinuousDomain( "library.parameters.biquadratic_lagrange" );

        ImportedContinuousEvaluator meshBiquadraticLagrangeParams = testRegion.importContinuousEvaluator( "test_mesh.element.biquadratic_lagrange", "test_mesh.mesh.dofs" );
        meshBiquadraticLagrangeParams.alias( biquadNodeList, globalNodesDomain );
        testRegion.addEvaluator( meshBilinearLagrangeParams );

        ImportedContinuousEvaluator biquadraticLagrange = library.importContinuousEvaluator( "test_mesh.biquadratic_lagrange", "library.fem.biquadratic_lagrange" );
        biquadraticLagrange.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.2d" ) );
        biquadraticLagrange.alias( meshBiquadraticLagrangeParams, libraryBiquadraticLagrangeParams );
        testRegion.addEvaluator( biquadraticLagrange );

        // Bilinear Simplex
        ContinuousDomain libraryBilinearSimplexParams = library.getContinuousDomain( "library.parameters.bilinear_simplex" );

        ImportedContinuousEvaluator meshBilinearSimplexParams = testRegion.importContinuousEvaluator( "test_mesh.element.bilinear_simplex", "test_mesh.mesh.dofs" );
        meshBilinearSimplexParams.alias( triangleNodeList, globalNodesDomain );
        testRegion.addEvaluator( meshBilinearSimplexParams );

        ImportedContinuousEvaluator bilinearSimplex = library.importContinuousEvaluator( "test_mesh.bilinear_simplex", "library.fem.bilinear_simplex" );
        bilinearSimplex.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.2d" ) );
        bilinearSimplex.alias( meshBilinearSimplexParams, libraryBilinearSimplexParams );
        testRegion.addEvaluator( bilinearSimplex );

        // Template 1
        ContinuousPiecewiseEvaluator meshCoordinatesT1 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.template1", rc1Domain,
            meshDomain.getElementDomain() );
        meshCoordinatesT1.setEvaluator( 1, bilinearLagrange );
        meshCoordinatesT1.setEvaluator( 2, bilinearSimplex );
        meshCoordinatesT1.setEvaluator( 3, bilinearSimplex );
        meshCoordinatesT1.setEvaluator( 4, bilinearLagrange );
        testRegion.addEvaluator( meshCoordinatesT1 );

        // Template 2
        ContinuousPiecewiseEvaluator meshCoordinatesT2 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.template2", rc1Domain,
            meshDomain.getElementDomain() );
        meshCoordinatesT2.setEvaluator( 1, bilinearLagrange );
        meshCoordinatesT2.setEvaluator( 2, bilinearSimplex );
        meshCoordinatesT2.setEvaluator( 3, bilinearSimplex );
        meshCoordinatesT2.setEvaluator( 4, biquadraticLagrange );
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
        meshCoordinatesX.set( "field", "true" );
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
        meshCoordinatesY.set( "field", "true" );
        meshCoordinatesY.setVariable( "test_mesh.mesh.dofs", meshY );
        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", rc2Domain );
        meshCoordinates.set( "field", "true" );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }
}
