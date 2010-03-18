package fieldml;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousDereferenceEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousPiecewiseEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.ImportedContinuous;
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

        /*
         * //Contexty context.set( meshDomain, 4, 0.5, 0.5 ); meshXY.evaluate( context ); output = context.get(
         * meshXY.getValueDomain() );
         * 
         * // Derivates and maps are difficult/impossible.
         * 
         * //Pipeliney EnsembleConstantEvaluator element = new EnsembleConstantEvaluator( meshDomain.elementDomain );
         * element.setValue( 3 );
         * 
         * ContinuousConstantEvaluator xi = new ContinuousConstantEvaluator( meshDomain.chartDomain ); element.setValue( 0.5,
         * 0.5 );
         * 
         * context.setVariable( "test_mesh.element_value", element ); meshXYPressure.setVariable( "test_mesh.xi_value", xi );
         * 
         * foo = meshXY.evaluate(context);
         * 
         * 
         * bob = pressure.evaluate();
         * 
         * element.setValue( 4 );
         * 
         * bar = meshXY.evaluate();
         * 
         * // Repetitive set value calls. Multiple return types impossible (without structs).
         */
    }


    public static Region buildRegion( Region parent )
    {
        Region testRegion = new SubRegion( REGION_NAME, parent );
        Region library = testRegion.getLibrary();

        ContinuousDomain rc1Domain = library.getContinuousDomain( "library.coordinates.rc.1d" );
        ContinuousDomain rc2Domain = library.getContinuousDomain( "library.coordinates.rc.2d" );
        EnsembleDomain pointDomain = library.getEnsembleDomain( "library.topology.0d" );
        EnsembleDomain tri1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.triangle.1x1" );
        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );
        EnsembleDomain quad2x2LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.2x2" );

        EnsembleDomain baseElementDomain = library.getEnsembleDomain( "library.topology.2d" );

        MeshDomain meshDomain = new MeshDomain( testRegion, "test_mesh.domain", rc2Domain, baseElementDomain, 4 );
        meshDomain.setShape( 1, "library.shape.quad" );
        meshDomain.setShape( 2, "library.shape.triangle" );
        meshDomain.setShape( 3, "library.shape.triangle" );
        meshDomain.setShape( 4, "library.shape.quad" );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", pointDomain, 13 );

        EnsembleParameters triangleNodeList = new EnsembleParameters( "test_mesh.triangle1x1_nodes", globalNodesDomain, meshDomain
            .getElementDomain(), tri1x1LocalNodeDomain );
        triangleNodeList.setValue( 2, 2, 5, 3 );
        triangleNodeList.setValue( 3, 6, 3, 5 );
        testRegion.addEvaluator( triangleNodeList );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad1x1_nodes", globalNodesDomain, meshDomain
            .getElementDomain(), quad1x1LocalNodeDomain );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 4, 6, 13, 3, 7 );
        testRegion.addEvaluator( quadNodeList );

        EnsembleParameters biquadNodeList = new EnsembleParameters( "test_mesh.quad2x2_nodes", globalNodesDomain, meshDomain
            .getElementDomain(), quad2x2LocalNodeDomain );
        biquadNodeList.setValue( 4, 6, 12, 13, 8, 9, 10, 3, 11, 7 );
        testRegion.addEvaluator( biquadNodeList );

        meshDomain.setPointConnectivity( tri1x1LocalNodeDomain, triangleNodeList );
        meshDomain.setPointConnectivity( quad1x1LocalNodeDomain, quadNodeList );
        meshDomain.setPointConnectivity( quad2x2LocalNodeDomain, biquadNodeList );

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.mesh.dofs", rc1Domain );
        testRegion.addEvaluator( dofs );

        // Bilinear Lagrange
        ContinuousDomain libraryBilinearLagrangeParams = library.getContinuousDomain( "library.parameters.bilinear_lagrange" );

        ContinuousDereferenceEvaluator meshBilinearLagrangeParams = new ContinuousDereferenceEvaluator( "test_mesh.element.bilinear_lagrange",
            libraryBilinearLagrangeParams, quadNodeList, dofs );
        testRegion.addEvaluator( meshBilinearLagrangeParams );

        ImportedContinuous bilinearLagrange = new ImportedContinuous( "test_mesh.bilinear_lagrange", library, "library.fem.bilinear_lagrange" );
        bilinearLagrange.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.2d" ) );
        bilinearLagrange.alias( meshBilinearLagrangeParams, libraryBilinearLagrangeParams );
        testRegion.addEvaluator( bilinearLagrange );

        // Biquadratic Lagrange
        ContinuousDomain libraryBiquadraticLagrangeParams = library.getContinuousDomain( "library.parameters.biquadratic_lagrange" );

        ContinuousDereferenceEvaluator meshBiquadraticLagrangeParams = new ContinuousDereferenceEvaluator( "test_mesh.element.biquadratic_lagrange",
            libraryBiquadraticLagrangeParams, biquadNodeList, dofs );
        testRegion.addEvaluator( meshBiquadraticLagrangeParams );

        ImportedContinuous biquadraticLagrange = new ImportedContinuous( "test_mesh.biquadratic_lagrange", library, "library.fem.biquadratic_lagrange" );
        biquadraticLagrange.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.2d" ) );
        biquadraticLagrange.alias( meshBiquadraticLagrangeParams, libraryBiquadraticLagrangeParams );
        testRegion.addEvaluator( biquadraticLagrange );

        // Bilinear Simplex
        ContinuousDomain libraryBilinearSimplexParams = library.getContinuousDomain( "library.parameters.bilinear_simplex" );

        ContinuousDereferenceEvaluator meshBilinearSimplexParams = new ContinuousDereferenceEvaluator( "test_mesh.element.bilinear_simplex",
            libraryBilinearSimplexParams, triangleNodeList, dofs );
        testRegion.addEvaluator( meshBilinearSimplexParams );

        ImportedContinuous bilinearSimplex = new ImportedContinuous( "test_mesh.bilinear_simplex", library, "library.fem.bilinear_simplex" );
        bilinearSimplex.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.2d" ) );
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
