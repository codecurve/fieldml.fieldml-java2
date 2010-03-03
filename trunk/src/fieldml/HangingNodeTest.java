package fieldml;

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
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldml.region.WorldRegion;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class HangingNodeTest
    extends FieldmlTestCase
{
    public static String REGION_NAME = "HangingNode_Test";


    public void testSerialization()
    {
        WorldRegion world = new WorldRegion();
        // Region region = buildDirectMapRegion();
        Region region = buildVirtualNodeRegion( world );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1______2____3\n" );
        s.append( "|      |    |\n" );
        s.append( "|      | *2 |\n" );
        s.append( "|      |    |\n" );
        s.append( "|  *1  4____5\n" );
        s.append( "|      |    |\n" );
        s.append( "|      | *3 |\n" );
        s.append( "6______7____8\n" );

        serialize( region, s.toString() ); 
    }


    public void testEvaluation()
    {
        WorldRegion world = new WorldRegion();
        Region region = buildVirtualNodeRegion( world );

        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        ContinuousEvaluator meshXY = region.getContinuousEvaluator( "test_mesh.coordinates.xy" );
        DomainValues context = new DomainValues();

        ContinuousDomainValue output;

        context.set( meshDomain, 1, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 10;
        assert output.values[1] == 10;

        context.set( meshDomain, 2, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 15;

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 5;

        region = buildDirectMapRegion();

        meshDomain = region.getMeshDomain( "test_mesh.domain" );
        meshXY = region.getContinuousEvaluator( "test_mesh.coordinates.xy" );

        context = new DomainValues();
        
        context.set( meshDomain, 1, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 10;
        assert output.values[1] == 10;

        context.set( meshDomain, 2, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 15;

        context.set( meshDomain, 3, 0.5, 0.5 );
        output = meshXY.evaluate( context );
        assert output.values[0] == 25;
        assert output.values[1] == 5;
    }


    /**
     * This example creates a local node set from the available dofs, adding 'virtual' nodes as needed, then using a standard
     * element x localnode -> globalnode lookup for generating the dof vectors needed for interpolation.
     */
    public static Region buildVirtualNodeRegion( Region parent )
    {
        Region library = parent.getLibrary();
        Region testRegion = new SubRegion( REGION_NAME, parent );

        EnsembleDomain xiComponentDomain = library.getEnsembleDomain( "library.co-ordinates.rc.2d" );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", xiComponentDomain, 3 );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 3, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( "test_mesh.global_dofs_index", 7 );
        testRegion.addDomain( globalDofsDomain );

        EnsembleDomain anonymous = library.getEnsembleDomain( "library.anonymous" );

        EnsembleDomain globalDofListDomain = new EnsembleDomain( "test_mesh.global_dof_index_list", anonymous, globalDofsDomain );
        testRegion.addDomain( globalDofListDomain );

        EnsembleDomain localDofsDomain = new EnsembleDomain( "test_mesh.local_dof_index", 8 );
        testRegion.addDomain( localDofsDomain );

        EnsembleDomain localDofListDomain = new EnsembleDomain( "test_mesh.local_dof_index_list", anonymous, localDofsDomain );
        testRegion.addDomain( localDofListDomain );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.list" );

        ContinuousParameters globalToLocalWeights = new ContinuousParameters( "test_mesh.global_to_local.weights", weightingDomain,
            localDofsDomain );
        globalToLocalWeights.setValue( 1, 1.0 );
        globalToLocalWeights.setValue( 2, 1.0 );
        globalToLocalWeights.setValue( 3, 1.0 );
        globalToLocalWeights.setValue( 4, 0.5, 0.5 );
        globalToLocalWeights.setValue( 5, 1.0 );
        globalToLocalWeights.setValue( 6, 1.0 );
        globalToLocalWeights.setValue( 7, 1.0 );
        globalToLocalWeights.setValue( 8, 1.0 );
        testRegion.addEvaluator( globalToLocalWeights );

        EnsembleParameters globalToLocalIndexes = new EnsembleParameters( "test_mesh.global_to_local.indexes", globalDofListDomain,
            localDofsDomain );
        globalToLocalIndexes.setValue( 1, 1 );
        globalToLocalIndexes.setValue( 2, 2 );
        globalToLocalIndexes.setValue( 3, 3 );
        globalToLocalIndexes.setValue( 4, 2, 6 );
        globalToLocalIndexes.setValue( 5, 4 );
        globalToLocalIndexes.setValue( 6, 5 );
        globalToLocalIndexes.setValue( 7, 6 );
        globalToLocalIndexes.setValue( 8, 7 );
        testRegion.addEvaluator( globalToLocalIndexes );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", localDofListDomain, meshDomain.getElementDomain() );
        quadNodeList.setValue( 1, 6, 7, 1, 2 );
        quadNodeList.setValue( 2, 4, 5, 2, 3 );
        quadNodeList.setValue( 3, 7, 8, 4, 5 );

        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshPointsX = new ContinuousParameters( "test_mesh.point.x", mesh1DDomain, globalDofsDomain );
        meshPointsX.setValue( 1, 00.0 );
        meshPointsX.setValue( 2, 20.0 );
        meshPointsX.setValue( 3, 30.0 );
        meshPointsX.setValue( 4, 30.0 );
        meshPointsX.setValue( 5, 00.0 );
        meshPointsX.setValue( 6, 20.0 );
        meshPointsX.setValue( 7, 30.0 );

        testRegion.addEvaluator( meshPointsX );

        ContinuousParameters meshPointsY = new ContinuousParameters( "test_mesh.point.y", mesh1DDomain, globalDofsDomain );
        meshPointsY.setValue( 1, 20.0 );
        meshPointsY.setValue( 2, 20.0 );
        meshPointsY.setValue( 3, 20.0 );
        meshPointsY.setValue( 4, 10.0 );
        meshPointsY.setValue( 5, 00.0 );
        meshPointsY.setValue( 6, 00.0 );
        meshPointsY.setValue( 7, 00.0 );

        testRegion.addEvaluator( meshPointsY );

        FunctionEvaluator bilinearLagrange = new FunctionEvaluator( "test_mesh.bilinear_lagrange", weightingDomain, meshDomain.getXiDomain(), library
            .getContinuousFunction( "library.function.bilinear_lagrange" ) );
        testRegion.addEvaluator( bilinearLagrange );

        ContinuousVariableEvaluator globalDofs = new ContinuousVariableEvaluator( "test_mesh.dofs", mesh1DDomain );

        MapEvaluator localDofs = new MapEvaluator( "test_mesh.local_dofs", mesh1DDomain, globalToLocalIndexes, globalToLocalWeights,
            globalDofs );
        testRegion.addEvaluator( localDofs );

        MapEvaluator elementBilinearLagrange = new MapEvaluator( "test_mesh.element.bilinear_lagrange", mesh1DDomain, quadNodeList,
            bilinearLagrange, localDofs );
        testRegion.addEvaluator( elementBilinearLagrange );

        ContinuousPiecewiseEvaluator meshCoordinatesTemplate = new ContinuousPiecewiseEvaluator( "test_mesh.template.bilinear_lagrange",
            mesh1DDomain, meshDomain.getElementDomain() );
        meshCoordinatesTemplate.setEvaluator( 1, elementBilinearLagrange );
        meshCoordinatesTemplate.setEvaluator( 2, elementBilinearLagrange );
        meshCoordinatesTemplate.setEvaluator( 3, elementBilinearLagrange );
        testRegion.addEvaluator( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.setVariable( "test_mesh.dofs", meshPointsX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.setVariable( "test_mesh.dofs", meshPointsY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }


    /**
     * This example maps global dofs directly to per-element parameters.
     */
    public static Region buildDirectMapRegion()
    {
        WorldRegion world = new WorldRegion();
        Region library = world.getLibrary();
        Region testRegion = new SubRegion( REGION_NAME, world );

        EnsembleDomain xiComponentDomain = library.getEnsembleDomain( "library.co-ordinates.rc.2d" );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", xiComponentDomain, 3 );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 3, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalDofsDomain = new EnsembleDomain( "test_mesh.global_dofs_index", 7 );
        testRegion.addDomain( globalDofsDomain );

        EnsembleDomain anonymous = library.getEnsembleDomain( "library.anonymous" );

        EnsembleDomain globalDofIndexesDomain = new EnsembleDomain( "test_mesh.global_dof_index_list", anonymous, globalDofsDomain );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.list" );

        EnsembleDomain quad1x1NodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );
        EnsembleDomain quad1x1NodeListDomain = new EnsembleDomain( "test_mesh.nodes.1x1", anonymous, quad1x1NodeDomain );

        ContinuousParameters elementWeights = new ContinuousParameters( "test_mesh.element.dof_weights", weightingDomain,
            meshDomain.getElementDomain(), quad1x1NodeDomain );
        EnsembleParameters elementIndexes = new EnsembleParameters( "test_mesh.element.dof_indexes", globalDofIndexesDomain,
            meshDomain.getElementDomain(), quad1x1NodeDomain );

        elementWeights.setValue( new int[]{ 1, 1 }, 1.0 );
        elementWeights.setValue( new int[]{ 1, 2 }, 1.0 );
        elementWeights.setValue( new int[]{ 1, 3 }, 1.0 );
        elementWeights.setValue( new int[]{ 1, 4 }, 1.0 );

        elementIndexes.setValue( new int[]{ 1, 1 }, 5 );
        elementIndexes.setValue( new int[]{ 1, 2 }, 6 );
        elementIndexes.setValue( new int[]{ 1, 3 }, 1 );
        elementIndexes.setValue( new int[]{ 1, 4 }, 2 );

        elementWeights.setValue( new int[]{ 2, 1 }, 0.5, 0.5 );
        elementWeights.setValue( new int[]{ 2, 2 }, 1.0 );
        elementWeights.setValue( new int[]{ 2, 3 }, 1.0 );
        elementWeights.setValue( new int[]{ 2, 4 }, 1.0 );

        elementIndexes.setValue( new int[]{ 2, 1 }, 2, 6 );
        elementIndexes.setValue( new int[]{ 2, 2 }, 4 );
        elementIndexes.setValue( new int[]{ 2, 3 }, 2 );
        elementIndexes.setValue( new int[]{ 2, 4 }, 3 );

        elementWeights.setValue( new int[]{ 3, 1 }, 1.0 );
        elementWeights.setValue( new int[]{ 3, 2 }, 1.0 );
        elementWeights.setValue( new int[]{ 3, 3 }, 0.5, 0.5 );
        elementWeights.setValue( new int[]{ 3, 4 }, 1.0 );

        elementIndexes.setValue( new int[]{ 3, 1 }, 5 );
        elementIndexes.setValue( new int[]{ 3, 2 }, 7 );
        elementIndexes.setValue( new int[]{ 3, 3 }, 2, 6 );
        elementIndexes.setValue( new int[]{ 3, 4 }, 4 );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshPointsX = new ContinuousParameters( "test_mesh.point.x", mesh1DDomain, globalDofsDomain );
        meshPointsX.setValue( 1, 00.0 );
        meshPointsX.setValue( 2, 20.0 );
        meshPointsX.setValue( 3, 30.0 );
        meshPointsX.setValue( 4, 30.0 );
        meshPointsX.setValue( 5, 00.0 );
        meshPointsX.setValue( 6, 20.0 );
        meshPointsX.setValue( 7, 30.0 );

        testRegion.addEvaluator( meshPointsX );

        ContinuousParameters meshPointsY = new ContinuousParameters( "test_mesh.point.y", mesh1DDomain, globalDofsDomain );
        meshPointsY.setValue( 1, 20.0 );
        meshPointsY.setValue( 2, 20.0 );
        meshPointsY.setValue( 3, 20.0 );
        meshPointsY.setValue( 4, 10.0 );
        meshPointsY.setValue( 5, 00.0 );
        meshPointsY.setValue( 6, 00.0 );
        meshPointsY.setValue( 7, 00.0 );

        testRegion.addEvaluator( meshPointsY );

        EnsembleParameters elementLocalDofIndexes = new EnsembleParameters( "test_mesh.element.local_dof_indexes", quad1x1NodeListDomain );
        elementLocalDofIndexes.setDefaultValue( 1, 2, 3, 4 );

        FunctionEvaluator bilinearLagrange = new FunctionEvaluator( "test_mesh.bilinear_lagrange", weightingDomain, meshDomain.getXiDomain(), library
            .getContinuousFunction( "library.function.bilinear_lagrange" ) );
        testRegion.addEvaluator( bilinearLagrange );

        ContinuousVariableEvaluator dofs = new ContinuousVariableEvaluator( "test_mesh.dofs", mesh1DDomain );

        MapEvaluator bilinearElementDofs = new MapEvaluator( "test_mesh.element.bilinear_dofs", mesh1DDomain, elementIndexes,
            elementWeights, dofs );

        MapEvaluator elementBilinear = new MapEvaluator( "test_mesh.element.bilinear_lagrange", mesh1DDomain, elementLocalDofIndexes,
            bilinearLagrange, bilinearElementDofs );

        ContinuousPiecewiseEvaluator meshCoordinatesTemplate = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.template", mesh1DDomain, meshDomain.getElementDomain() );
        meshCoordinatesTemplate.setEvaluator( 1, elementBilinear );
        meshCoordinatesTemplate.setEvaluator( 2, elementBilinear );
        meshCoordinatesTemplate.setEvaluator( 3, elementBilinear );
        testRegion.addEvaluator( meshCoordinatesTemplate );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesX.setVariable( "test_mesh.dofs", meshPointsX );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesTemplate );
        meshCoordinatesY.setVariable( "test_mesh.dofs", meshPointsY );

        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }
}
