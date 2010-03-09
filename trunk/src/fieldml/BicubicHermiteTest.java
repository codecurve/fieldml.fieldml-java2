package fieldml;

import java.io.FileWriter;
import java.io.IOException;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousCompositeEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousPiecewiseEvaluator;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.DotProductEvaluator;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.evaluator.MapEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldml.region.WorldRegion;
import fieldmlx.util.MinimalColladaExporter;

public class BicubicHermiteTest
    extends FieldmlTestCase
{
    public static String REGION_NAME = "BicubicHermite_Test";


    public void testSerialization()
    {
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1____2____5\n" );
        s.append( "|    |    |\n" );
        s.append( "|    |    |\n" );
        s.append( "|  1 |  2 |\n" );
        s.append( "|    |    |\n" );
        s.append( "3____4____6\n" );
        
        serialize( region, s.toString() ); 
    }


    public void testEvaluation()
    {
        // TODO STUB BicubicHermiteTest.testEvaluation
    }


    public static Region buildRegion( Region parent )
    {
        Region library = parent.getLibrary();
        Region testRegion = new SubRegion( REGION_NAME, parent );

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.list" );

        EnsembleDomain edgeDirectionDomain = new EnsembleDomain( testRegion, "test_mesh.edge_direction", 1, 2 );
        testRegion.addDomain( edgeDirectionDomain );

        EnsembleDomain xiComponentDomain = library.getEnsembleDomain( "library.coordinates.rc.2d" );

        MeshDomain meshDomain = new MeshDomain( testRegion, "test_mesh.domain", xiComponentDomain, 2 );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", 16 );

        EnsembleDomain globalNodesListDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", quad1x1LocalNodeDomain, globalNodesDomain );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesListDomain, meshDomain.getElementDomain() );
        quadNodeList.setValue( 1, 1, 2, 3, 4 );
        quadNodeList.setValue( 2, 2, 5, 4, 6 );
        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.coordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.coordinates.rc.3d" );

        ContinuousDomain meshdZdomain = new ContinuousDomain( testRegion, "test_mesh.coordinates.dz/ds" );

        ContinuousDomain meshd2Zdomain = new ContinuousDomain( testRegion, "test_mesh.coordinates.d2z/ds1ds2" );

        ContinuousVariableEvaluator nodalUDofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.u", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( nodalUDofs );

        ContinuousVariableEvaluator nodaldUdSDofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.du/ds", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( nodaldUdSDofs );

        ContinuousVariableEvaluator nodald2UdS2Dofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.d2u/ds2", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( nodald2UdS2Dofs );

        EnsembleParameters meshds1Direction = new EnsembleParameters( "test_mesh.node.direction.ds1", edgeDirectionDomain,
            meshDomain.getElementDomain(), quad1x1LocalNodeDomain );
        meshds1Direction.setDefaultValue( 1 );
        testRegion.addEvaluator( meshds1Direction );

        EnsembleParameters meshds2Direction = new EnsembleParameters( "test_mesh.node.direction.ds2", edgeDirectionDomain,
            meshDomain.getElementDomain(), quad1x1LocalNodeDomain );
        meshds2Direction.setDefaultValue( 2 );
        testRegion.addEvaluator( meshds2Direction );

        ContinuousCompositeEvaluator meshdUds1 = new ContinuousCompositeEvaluator( "test_mesh.node.du/ds1", meshdZdomain );
        meshdUds1.importField( meshds1Direction );
        meshdUds1.importField( nodaldUdSDofs );
        testRegion.addEvaluator( meshdUds1 );

        ContinuousCompositeEvaluator meshdUds2 = new ContinuousCompositeEvaluator( "test_mesh.node.du/ds2", meshdZdomain );
        meshdUds2.importField( meshds2Direction );
        meshdUds2.importField( nodaldUdSDofs );
        testRegion.addEvaluator( meshdUds2 );

        EnsembleDomain hermiteDerivativesDomain = library.getEnsembleDomain( "library.interpolation.hermite.derivatives" );

        ContinuousPiecewiseEvaluator bicubicHermiteParameters = new ContinuousPiecewiseEvaluator( "test_mesh.node.bicubic_parameters",
            mesh1DDomain, hermiteDerivativesDomain );
        bicubicHermiteParameters.setEvaluator( 1, nodalUDofs );
        bicubicHermiteParameters.setEvaluator( 2, meshdUds1 );
        bicubicHermiteParameters.setEvaluator( 3, meshdUds2 );
        bicubicHermiteParameters.setEvaluator( 4, nodald2UdS2Dofs );
        testRegion.addEvaluator( bicubicHermiteParameters );

        FunctionEvaluator meshBilinearLagrange = new FunctionEvaluator( "test_mesh.bilinear_lagrange", weightingDomain, meshDomain.getXiDomain(), library
            .getContinuousFunction( "library.function.bilinear_lagrange" ) );
        testRegion.addEvaluator( meshBilinearLagrange );

        MapEvaluator elementBilinearMap = new MapEvaluator( "test_mesh.element.bilinear_lagrange", mesh1DDomain, quadNodeList,
            meshBilinearLagrange, nodalUDofs );
        testRegion.addEvaluator( elementBilinearMap );

        ContinuousPiecewiseEvaluator meshCoordinatesL2 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.L2", mesh1DDomain,
            meshDomain.getElementDomain() );
        meshCoordinatesL2.setEvaluator( 1, elementBilinearMap );
        meshCoordinatesL2.setEvaluator( 2, elementBilinearMap );
        testRegion.addEvaluator( meshCoordinatesL2 );

        FunctionEvaluator meshBicubicHermite = new FunctionEvaluator( "test_mesh.bicubic_hermite", weightingDomain, meshDomain.getXiDomain(), library
            .getContinuousFunction( "library.function.bicubic_hermite" ) );
        testRegion.addEvaluator( meshBicubicHermite );

        EnsembleDomain hermiteParameterDomain = library.getEnsembleDomain( "library.interpolation.hermite.bicubic" );

        EnsembleParameters hermiteLocalNodeNumber = new EnsembleParameters( "test_mesh.bicubic_hermite.local_node", quad1x1LocalNodeDomain,
            hermiteParameterDomain );
        hermiteLocalNodeNumber.setValues( 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4 );

        EnsembleParameters hermiteNodeParameterNumber = new EnsembleParameters( "test_mesh.bicubic_hermite.nodal_parameter",
            hermiteDerivativesDomain, hermiteParameterDomain );
        hermiteNodeParameterNumber.setValues( 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4 );

        ContinuousDomain bicubicHermiteParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.parameters" );

        ContinuousParameters hermiteScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling", bicubicHermiteParametersDomain,
            meshDomain.getElementDomain() );
        hermiteScaling.setValue( 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 );
        hermiteScaling.setValue( 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2 );
        testRegion.addEvaluator( hermiteScaling );

        ContinuousCompositeEvaluator elementHermiteParameter = new ContinuousCompositeEvaluator(
            "test_mesh.bicubic_hermite.element_parameter", mesh1DDomain );
        elementHermiteParameter.importField( hermiteLocalNodeNumber );
        elementHermiteParameter.importField( quadNodeList, globalNodesDomain );
        elementHermiteParameter.importField( hermiteNodeParameterNumber );
        elementHermiteParameter.importField( bicubicHermiteParameters );

        ContinuousCompositeEvaluator elementHermiteParameters = new ContinuousCompositeEvaluator(
            "test_mesh.bicubic_hermite.element_parameters", bicubicHermiteParametersDomain );
        elementHermiteParameters.importField( elementHermiteParameter, bicubicHermiteParametersDomain );

        ContinuousEvaluator elementBicubicHermite = new DotProductEvaluator( "test_mesh.element.bicubic_hermite", mesh1DDomain,
            elementHermiteParameters, meshBicubicHermite, hermiteScaling );
        testRegion.addEvaluator( elementBicubicHermite );

        ContinuousPiecewiseEvaluator meshCoordinatesH3 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.H3", mesh1DDomain,
            meshDomain.getElementDomain() );
        meshCoordinatesH3.setEvaluator( 1, elementBicubicHermite );
        meshCoordinatesH3.setEvaluator( 2, elementBicubicHermite );
        testRegion.addEvaluator( meshCoordinatesH3 );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodesDomain );
        meshX.setValue( 1, 0.0 );
        meshX.setValue( 2, 1.0 );
        meshX.setValue( 3, 0.0 );
        meshX.setValue( 4, 1.0 );
        meshX.setValue( 5, 3.0 );
        meshX.setValue( 6, 3.0 );
        testRegion.addEvaluator( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", mesh1DDomain, globalNodesDomain );
        meshY.setValue( 1, 0.0 );
        meshY.setValue( 2, 0.0 );
        meshY.setValue( 3, 1.0 );
        meshY.setValue( 4, 1.0 );
        meshY.setValue( 5, 0.0 );
        meshY.setValue( 6, 1.0 );
        testRegion.addEvaluator( meshY );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", mesh1DDomain, globalNodesDomain );
        meshZ.setValue( 1, 0.0 );
        meshZ.setValue( 2, 0.0 );
        meshZ.setValue( 3, 0.0 );
        meshZ.setValue( 4, 0.0 );
        meshZ.setValue( 5, 0.0 );
        meshZ.setValue( 6, 0.0 );
        testRegion.addEvaluator( meshZ );

        ContinuousParameters meshdZ = new ContinuousParameters( "test_mesh.node.dz/ds", meshdZdomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdZ.setValue( new int[]{ 1, 1 }, -1.0 );
        meshdZ.setValue( new int[]{ 1, 2 }, 1.0 );
        meshdZ.setValue( new int[]{ 2, 1 }, 1.0 );
        meshdZ.setValue( new int[]{ 2, 2 }, 1.0 );
        meshdZ.setValue( new int[]{ 3, 1 }, -1.0 );
        meshdZ.setValue( new int[]{ 3, 2 }, -1.0 );
        meshdZ.setValue( new int[]{ 4, 1 }, 1.0 );
        meshdZ.setValue( new int[]{ 4, 2 }, -1.0 );
        meshdZ.setValue( new int[]{ 5, 1 }, -1.0 );
        meshdZ.setValue( new int[]{ 5, 2 }, 1.0 );
        meshdZ.setValue( new int[]{ 6, 1 }, -1.0 );
        meshdZ.setValue( new int[]{ 6, 2 }, -1.0 );
        testRegion.addEvaluator( meshdZ );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2Zdomain, globalNodesDomain );
        meshd2Z.setValue( 1, 0.0 );
        meshd2Z.setValue( 2, 0.0 );
        meshd2Z.setValue( 3, 0.0 );
        meshd2Z.setValue( 4, 0.0 );
        meshd2Z.setValue( 5, 0.0 );
        meshd2Z.setValue( 6, 0.0 );
        testRegion.addEvaluator( meshd2Z );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesL2 );
        meshCoordinatesX.setVariable( "test_mesh.node.dofs.u", meshX );
        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesL2 );
        meshCoordinatesY.setVariable( "test_mesh.node.dofs.u", meshY );
        testRegion.addEvaluator( meshCoordinatesY );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.u", meshZ );
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.du/ds", meshdZ );
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.d2u/ds2", meshd2Z );
        meshCoordinatesZ.setVariable( "test_mesh.element.bicubic_hermite_scaling", hermiteScaling );
        testRegion.addEvaluator( meshCoordinatesZ );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates", mesh3DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );
        meshCoordinates.setSourceField( 3, meshCoordinatesZ );
        testRegion.addEvaluator( meshCoordinates );

        return testRegion;
    }


    public void test()
        throws IOException
    {
        WorldRegion world = new WorldRegion();
        Region testRegion = buildRegion( world );

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, 16, "test_mesh.domain", "test_mesh.coordinates" );
        FileWriter f = new FileWriter( "trunk/data/collada two quads.xml" );
        f.write( collada );
        f.close();
    }
}
