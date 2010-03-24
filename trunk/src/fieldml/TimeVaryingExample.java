package fieldml;

import java.io.FileWriter;
import java.io.IOException;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousDereferenceEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousPiecewiseEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.ImportedContinuousEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.function.QuadraticBSpline;
import fieldml.function.QuadraticLagrange;
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldml.region.WorldRegion;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.util.MinimalColladaExporter;

public class TimeVaryingExample
    extends FieldmlTestCase
{
    public void testSerialize()
    {
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1____2____3____4____5\n" );
        
        serialize( region, s.toString() ); 
    }

    private static double[] rawDofs =
    { 0.954915, 1.0450850, -0.427051, -1.190983, -0.427051, 1.0450850, 0.954915 };


    public void testEvaluation()
    {
        WorldRegion world = new WorldRegion();
        Region testRegion = buildRegion( world );
        Region bsplineRegion = testRegion.getSubregion( QuadraticBSplineExample.REGION_NAME );

        MeshDomain timeMeshDomain = testRegion.getMeshDomain( "tv_test.time.mesh" );
        MeshDomain splineMeshDomain = bsplineRegion.getMeshDomain( "test_mesh.domain" );
        // ContinuousEvaluator meshParams = region.getContinuousEvaluator( "test_mesh.element.parameters" );
        ContinuousEvaluator meshZ = testRegion.getContinuousEvaluator( "tv_test.coordinates.z" );

        DomainValues context = new DomainValues();

        ContinuousDomainValue output;

        double[] interpolatorValues = new double[3];
        double params[] = new double[3];
        double xi[] = new double[1];
        double timeXi[] = new double[1];
        double quadraticParams[] = new double[3];
        double value;

        xi[0] = 0.25;
        timeXi[0] = 0.33;

        params[0] = rawDofs[0];
        params[1] = rawDofs[1];
        params[2] = rawDofs[2];
        interpolatorValues = QuadraticBSpline.evaluateDirect( xi[0] );
        quadraticParams[0] = ( interpolatorValues[0] * params[0] ) + ( interpolatorValues[1] * params[1] )
            + ( interpolatorValues[2] * params[2] );

        params[0] = 0;
        params[1] = 0;
        params[2] = 0;
        interpolatorValues = QuadraticBSpline.evaluateDirect( xi[0] );
        quadraticParams[1] = ( interpolatorValues[0] * params[0] ) + ( interpolatorValues[1] * params[1] )
            + ( interpolatorValues[2] * params[2] );

        params[0] = -rawDofs[0];
        params[1] = -rawDofs[1];
        params[2] = -rawDofs[2];
        interpolatorValues = QuadraticBSpline.evaluateDirect( xi[0] );
        quadraticParams[2] = ( interpolatorValues[0] * params[0] ) + ( interpolatorValues[1] * params[1] )
            + ( interpolatorValues[2] * params[2] );

        context.set( splineMeshDomain, 1, xi );
        context.set( timeMeshDomain, 1, timeXi );
        output = meshZ.getValue( context );

        interpolatorValues = QuadraticLagrange.evaluateDirect( timeXi[0] );
        value = ( interpolatorValues[0] * quadraticParams[0] ) + ( interpolatorValues[1] * quadraticParams[1] )
            + ( interpolatorValues[2] * quadraticParams[2] );

        assertEquals( value, output.values[0] );
    }

    public static String REGION_NAME = "TimeVaryingExample_Test";


    public static Region buildRegion( Region parent )
    {
        Region library = parent.getLibrary();
        Region tvRegion = new SubRegion( REGION_NAME, parent );

        Region bsplineRegion = QuadraticBSplineExample.buildRegion( tvRegion );

        tvRegion.addSubregion( bsplineRegion );

        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.coordinates.rc.1d" );

        EnsembleDomain pointDomain = library.getEnsembleDomain( "library.topology.0d" );
        EnsembleDomain baseElementDomain = library.getEnsembleDomain( "library.topology.1d" );

        MeshDomain timeMeshDomain = new MeshDomain( tvRegion, "tv_test.time.mesh", rc1CoordinatesDomain, baseElementDomain, 3 );
        timeMeshDomain.setDefaultShape( "line_0_1" );

        EnsembleDomain timeDofsDomain = new EnsembleDomain( tvRegion, "tv_test.time.dofs.domain", pointDomain, 7 );
        
        ContinuousParameters timeDofs = new ContinuousParameters( "tv_test.time.dofs.values", rc1CoordinatesDomain, timeDofsDomain );
        timeDofs.setValue( 1, 0 );
        timeDofs.setValue( 2, 1 );
        timeDofs.setValue( 3, 2.5 );
        timeDofs.setValue( 4, 4 );
        timeDofs.setValue( 5, 5.0 );
        timeDofs.setValue( 6, 6.625 );
        timeDofs.setValue( 7, 10.0 );// Deliberately non-linear. C1 continuous for my own amusement.
        tvRegion.addEvaluator( timeDofs );

        EnsembleDomain quadraticParamsDomain = library.getEnsembleDomain( "library.local_nodes.line.2" );
        
        EnsembleParameters elementDofIndexes = new EnsembleParameters( "tv_test.time.element_dof_indexes", timeDofsDomain,
            timeMeshDomain.getElementDomain(), quadraticParamsDomain );
        elementDofIndexes.setValue( 1, 1, 2, 3 );
        elementDofIndexes.setValue( 2, 3, 4, 5 );
        elementDofIndexes.setValue( 3, 5, 6, 7 );

        tvRegion.addEvaluator( elementDofIndexes );

        ContinuousVariableEvaluator tvMeshDofs = new ContinuousVariableEvaluator( "tv_test.mesh.dofs", rc1CoordinatesDomain, timeDofsDomain );
        tvRegion.addEvaluator( tvMeshDofs );

        ContinuousDomain quadraticLagrangeParams = library.getContinuousDomain( "library.parameters.quadratic_lagrange" );

        ContinuousDereferenceEvaluator meshQuadraticLagrangeParams = new ContinuousDereferenceEvaluator( "tv_test.mesh.element.quadratic_lagrange.params",
            quadraticLagrangeParams, elementDofIndexes, tvMeshDofs );
        tvRegion.addEvaluator( meshQuadraticLagrangeParams );

        ImportedContinuousEvaluator elementQLagrange = library.importContinuousEvaluator( "tv_test.mesh.quadratic_lagrange", "library.fem.quadratic_lagrange" );
        elementQLagrange.alias( timeMeshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.1d" ) );
        elementQLagrange.alias( meshQuadraticLagrangeParams, quadraticLagrangeParams );
        tvRegion.addEvaluator( elementQLagrange );
        
        ContinuousPiecewiseEvaluator meshTimeTemplate = new ContinuousPiecewiseEvaluator( "tv_test.time.template", rc1CoordinatesDomain, timeMeshDomain.getElementDomain() );
        meshTimeTemplate.setEvaluator( 1, elementQLagrange );
        meshTimeTemplate.setEvaluator( 2, elementQLagrange );
        meshTimeTemplate.setEvaluator( 3, elementQLagrange );
        tvRegion.addEvaluator( meshTimeTemplate );

        PiecewiseField meshTime = new PiecewiseField( "tv_test.time", rc1CoordinatesDomain, meshTimeTemplate );
        meshTime.setVariable( "tv_test.mesh.dofs", timeDofs );
        tvRegion.addEvaluator( meshTime );

        EnsembleDomain bsplineDofsDomain = bsplineRegion.getEnsembleDomain( "test_mesh.dofs" );

        ContinuousParameters dofs = new ContinuousParameters( "tv_test.dofs.z", rc1CoordinatesDomain, bsplineDofsDomain, timeDofsDomain );
        dofs.setValue( new int[]{ 1, 1 }, 0.954915 );
        dofs.setValue( new int[]{ 2, 1 }, 1.0450850 );
        dofs.setValue( new int[]{ 3, 1 }, -0.427051 );
        dofs.setValue( new int[]{ 4, 1 }, -1.190983 );
        dofs.setValue( new int[]{ 5, 1 }, -0.427051 );
        dofs.setValue( new int[]{ 6, 1 }, 1.0450850 );
        dofs.setValue( new int[]{ 7, 1 }, 0.954915 );

        dofs.setValue( new int[]{ 1, 2 }, 0.0 );
        dofs.setValue( new int[]{ 2, 2 }, 0.0 );
        dofs.setValue( new int[]{ 3, 2 }, 0.0 );
        dofs.setValue( new int[]{ 4, 2 }, 0.0 );
        dofs.setValue( new int[]{ 5, 2 }, 0.0 );
        dofs.setValue( new int[]{ 6, 2 }, 0.0 );
        dofs.setValue( new int[]{ 7, 2 }, 0.0 );

        dofs.setValue( new int[]{ 1, 3 }, -0.954915 );
        dofs.setValue( new int[]{ 2, 3 }, -1.0450850 );
        dofs.setValue( new int[]{ 3, 3 }, 0.427051 );
        dofs.setValue( new int[]{ 4, 3 }, 1.190983 );
        dofs.setValue( new int[]{ 5, 3 }, 0.427051 );
        dofs.setValue( new int[]{ 6, 3 }, -1.0450850 );
        dofs.setValue( new int[]{ 7, 3 }, -0.954915 );

        dofs.setValue( new int[]{ 1, 4 }, 0.0 );
        dofs.setValue( new int[]{ 2, 4 }, 0.0 );
        dofs.setValue( new int[]{ 3, 4 }, 0.0 );
        dofs.setValue( new int[]{ 4, 4 }, 0.0 );
        dofs.setValue( new int[]{ 5, 4 }, 0.0 );
        dofs.setValue( new int[]{ 6, 4 }, 0.0 );
        dofs.setValue( new int[]{ 7, 4 }, 0.0 );

        dofs.setValue( new int[]{ 1, 5 }, 0.954915 );
        dofs.setValue( new int[]{ 2, 5 }, 1.0450850 );
        dofs.setValue( new int[]{ 3, 5 }, -0.427051 );
        dofs.setValue( new int[]{ 4, 5 }, -1.190983 );
        dofs.setValue( new int[]{ 5, 5 }, -0.427051 );
        dofs.setValue( new int[]{ 6, 5 }, 1.0450850 );
        dofs.setValue( new int[]{ 7, 5 }, 0.954915 );

        dofs.setValue( new int[]{ 1, 6 }, 0.0 );
        dofs.setValue( new int[]{ 2, 6 }, 0.0 );
        dofs.setValue( new int[]{ 3, 6 }, 0.0 );
        dofs.setValue( new int[]{ 4, 6 }, 0.0 );
        dofs.setValue( new int[]{ 5, 6 }, 0.0 );
        dofs.setValue( new int[]{ 6, 6 }, 0.0 );
        dofs.setValue( new int[]{ 7, 6 }, 0.0 );

        dofs.setValue( new int[]{ 1, 7 }, -0.954915 );
        dofs.setValue( new int[]{ 2, 7 }, -1.0450850 );
        dofs.setValue( new int[]{ 3, 7 }, 0.427051 );
        dofs.setValue( new int[]{ 4, 7 }, 1.190983 );
        dofs.setValue( new int[]{ 5, 7 }, 0.427051 );
        dofs.setValue( new int[]{ 6, 7 }, -1.0450850 );
        dofs.setValue( new int[]{ 7, 7 }, -0.954915 );
        tvRegion.addEvaluator( dofs );

        ContinuousEvaluator bsplineTemplate = bsplineRegion.getContinuousEvaluator( "test_mesh.coordinates" );

        PiecewiseField slicedZ = new PiecewiseField( "tv_test.coordinates.sliced_z", rc1CoordinatesDomain, bsplineTemplate );
        slicedZ.setVariable( "test_mesh.dofs", dofs );
        tvRegion.addEvaluator( slicedZ );

        PiecewiseField zValue = new PiecewiseField( "tv_test.coordinates.z", rc1CoordinatesDomain, meshTimeTemplate );
        zValue.setVariable( "tv_test.mesh.dofs", slicedZ );
        tvRegion.addEvaluator( zValue );

        return tvRegion;
    }


    public void test()
        throws IOException
    {
        WorldRegion world = new WorldRegion();
        Region library = world.getLibrary();
        Region testRegion = buildRegion( world );
        Region bsplineRegion = testRegion.getSubregion( QuadraticBSplineExample.REGION_NAME );

        ContinuousDomain rc1CoordinatesDomain = library.getContinuousDomain( "library.coordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.coordinates.rc.3d" );
        // These are only for visualization. Do not serialize.

        EnsembleDomain globalNodesDomain = bsplineRegion.getEnsembleDomain( "test_mesh.nodes" );
        EnsembleEvaluator lineNodeList = bsplineRegion.getEnsembleEvaluator( "test_mesh.line_nodes" );
        MeshDomain bsplineDomain = bsplineRegion.getMeshDomain( "test_mesh.domain" );

        ContinuousParameters nodalX = new ContinuousParameters( "test_mesh.node.x", rc1CoordinatesDomain, globalNodesDomain );
        nodalX.setValue( 1, 0.0 );
        nodalX.setValue( 2, 1.0 );
        nodalX.setValue( 3, 2.0 );
        nodalX.setValue( 4, 3.0 );
        nodalX.setValue( 5, 4.0 );
        nodalX.setValue( 6, 5.0 );

        ContinuousVariableEvaluator linearDofs = new ContinuousVariableEvaluator( "test_mesh.linear.dofs", rc1CoordinatesDomain, globalNodesDomain );
        testRegion.addEvaluator( linearDofs );

        ContinuousDomain linearLagrangeParams = library.getContinuousDomain( "library.parameters.linear_lagrange" );

        ContinuousDereferenceEvaluator meshLinearLagrangeParams = new ContinuousDereferenceEvaluator( "test_mesh.element.linear_lagrange.params",
            linearLagrangeParams, lineNodeList, linearDofs );
        testRegion.addEvaluator( meshLinearLagrangeParams );

        ImportedContinuousEvaluator elementLLagrange = library.importContinuousEvaluator( "test_mesh.linear_lagrange", "library.fem.linear_lagrange" );
        elementLLagrange.alias( bsplineDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.1d" ) );
        elementLLagrange.alias( meshLinearLagrangeParams, linearLagrangeParams );
        testRegion.addEvaluator( elementLLagrange );
        
        ContinuousPiecewiseEvaluator linearMeshCoordinates = new ContinuousPiecewiseEvaluator( "test_mesh.linear_coordinates", rc1CoordinatesDomain, bsplineDomain.getElementDomain() );
        linearMeshCoordinates.setEvaluator( 1, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 2, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 3, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 4, elementLLagrange );
        linearMeshCoordinates.setEvaluator( 5, elementLLagrange );
        testRegion.addEvaluator( linearMeshCoordinates );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", rc1CoordinatesDomain, linearMeshCoordinates );
        meshCoordinatesX.setVariable( "test_mesh.linear.dofs", nodalX );

        ContinuousEvaluator meshTime = testRegion.getContinuousEvaluator( "tv_test.time" );
        ContinuousEvaluator zValue = testRegion.getContinuousEvaluator( "tv_test.coordinates.z" );

        ContinuousAggregateEvaluator testCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates", mesh3DDomain );
        testCoordinates.setSourceField( 1, meshCoordinatesX );
        testCoordinates.setSourceField( 2, meshTime );
        testCoordinates.setSourceField( 3, zValue );

        testRegion.addEvaluator( testCoordinates );

        testRegion.addDomain( bsplineDomain );

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, 16, "test_mesh.coordinates", "test_mesh.domain",
            "tv_test.time.mesh" );
        FileWriter f = new FileWriter( "trunk/data/collada tv b-spline.xml" );
        f.write( collada );
        f.close();
    }
}
