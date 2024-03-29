package fieldml;

import java.io.FileWriter;
import java.io.IOException;

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

        EnsembleDomain quad2x2LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.2x2" );
        EnsembleDomain rc2Domain = library.getEnsembleDomain( "library.ensemble.xi.2d" );

        EnsembleDomain edgeDirectionDomain = new EnsembleDomain( testRegion, "test_mesh.edge_direction", 1, 2 );
        testRegion.addDomain( edgeDirectionDomain );

        MeshDomain meshDomain = new MeshDomain( testRegion, "test_mesh.domain", rc2Domain, 2 );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", 16 );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesDomain, meshDomain.getElementDomain(),
            quad2x2LocalNodeDomain );
        quadNodeList.setValue( 1, 1, 2, 3, 4 );
        quadNodeList.setValue( 2, 2, 5, 4, 6 );
        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.coordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.coordinates.rc.3d" );

        ContinuousDomain meshdZdomain = new ContinuousDomain( testRegion, "test_mesh.coordinates.dz_ds" );

        ContinuousDomain meshd2Zdomain = new ContinuousDomain( testRegion, "test_mesh.coordinates.d2z_ds1ds2" );

        ContinuousVariableEvaluator nodalUDofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.u", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( nodalUDofs );

        ContinuousVariableEvaluator nodaldUdSDofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.du_ds", meshdZdomain,
            globalNodesDomain );
        testRegion.addEvaluator( nodaldUdSDofs );

        ContinuousVariableEvaluator nodald2UdS2Dofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.d2u_ds2", mesh1DDomain,
            globalNodesDomain );
        testRegion.addEvaluator( nodald2UdS2Dofs );

        EnsembleParameters meshds1Direction = new EnsembleParameters( "test_mesh.node.direction.ds1", edgeDirectionDomain, meshDomain
            .getElementDomain(), quad2x2LocalNodeDomain );
        meshds1Direction.setDefaultValue( 1 );
        testRegion.addEvaluator( meshds1Direction );

        EnsembleParameters meshds2Direction = new EnsembleParameters( "test_mesh.node.direction.ds2", edgeDirectionDomain, meshDomain
            .getElementDomain(), quad2x2LocalNodeDomain );
        meshds2Direction.setDefaultValue( 2 );
        testRegion.addEvaluator( meshds2Direction );

        ImportedContinuousEvaluator meshdUds1 = testRegion.importContinuousEvaluator( "test_mesh.node.du_ds1", "test_mesh.node.dofs.du_ds" );
        meshdUds1.alias( meshds1Direction, edgeDirectionDomain );
        testRegion.addEvaluator( meshdUds1 );

        ImportedContinuousEvaluator meshdUds2 = testRegion.importContinuousEvaluator( "test_mesh.node.du_ds2", "test_mesh.node.dofs.du_ds" );
        meshdUds2.alias( meshds2Direction, edgeDirectionDomain );
        testRegion.addEvaluator( meshdUds2 );

        EnsembleDomain hermiteDerivativesDomain = library.getEnsembleDomain( "library.interpolation.hermite.derivatives" );

        ContinuousPiecewiseEvaluator bicubicHermiteParameters = new ContinuousPiecewiseEvaluator( "test_mesh.node.bicubic_parameters",
            mesh1DDomain, hermiteDerivativesDomain );
        bicubicHermiteParameters.setEvaluator( 1, nodalUDofs );
        bicubicHermiteParameters.setEvaluator( 2, meshdUds1 );
        bicubicHermiteParameters.setEvaluator( 3, meshdUds2 );
        bicubicHermiteParameters.setEvaluator( 4, nodald2UdS2Dofs );
        testRegion.addEvaluator( bicubicHermiteParameters );

        ContinuousDomain libraryBilinearLagrangeParams = library.getContinuousDomain( "library.parameters.bilinear_lagrange" );

        ImportedContinuousEvaluator meshBilinearLagrangeParams = testRegion.importContinuousEvaluator( "test_mesh.element.bilinear_lagrange.params",
            "test_mesh.node.dofs.u" );
        meshBilinearLagrangeParams.alias( quadNodeList, globalNodesDomain );
        testRegion.addEvaluator( meshBilinearLagrangeParams );

        ImportedContinuousEvaluator elementBilinear = library.importContinuousEvaluator( "test_mesh.element.bilinear_lagrange",
            "library.fem.bilinear_lagrange" );
        elementBilinear.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.2d" ) );
        elementBilinear.alias( meshBilinearLagrangeParams, libraryBilinearLagrangeParams );
        testRegion.addEvaluator( elementBilinear );

        ContinuousPiecewiseEvaluator meshCoordinatesL2 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.L2", mesh1DDomain,
            meshDomain.getElementDomain() );
        meshCoordinatesL2.setEvaluator( 1, elementBilinear );
        meshCoordinatesL2.setEvaluator( 2, elementBilinear );
        testRegion.addEvaluator( meshCoordinatesL2 );

        EnsembleDomain hermiteParameterDomain = library.getEnsembleDomain( "library.interpolation.hermite.bicubic" );

        EnsembleParameters hermiteLocalNodeNumber = new EnsembleParameters( "test_mesh.bicubic_hermite.local_node", quad2x2LocalNodeDomain,
            hermiteParameterDomain );
        hermiteLocalNodeNumber.setValues( 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4 );

        EnsembleParameters hermiteParameterDerivativeNumber = new EnsembleParameters( "test_mesh.bicubic_hermite.nodal_parameter",
            hermiteDerivativesDomain, hermiteParameterDomain );
        hermiteParameterDerivativeNumber.setValues( 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4 );

        ContinuousDomain bicubicHermiteParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.parameters" );
        ContinuousDomain bicubicHermiteScalingDomain = library.getContinuousDomain( "library.bicubic_hermite.scaling" );

        ContinuousParameters hermiteScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling", bicubicHermiteScalingDomain,
            meshDomain.getElementDomain() );
        hermiteScaling.setValue( 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 );
        hermiteScaling.setValue( 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2 );
        testRegion.addEvaluator( hermiteScaling );

        ImportedContinuousEvaluator elementHermiteParameter = testRegion.importContinuousEvaluator(
            "test_mesh.bicubic_hermite.element_parameter", "test_mesh.node.bicubic_parameters" );
        elementHermiteParameter.alias( hermiteLocalNodeNumber, quad2x2LocalNodeDomain );
        elementHermiteParameter.alias( quadNodeList, globalNodesDomain );
        elementHermiteParameter.alias( hermiteParameterDerivativeNumber, hermiteDerivativesDomain );
        testRegion.addEvaluator( elementHermiteParameter );

        ContinuousDomain parameterScalingDomain = library.getContinuousDomain( "library.scale.list" );

        ImportedContinuousEvaluator elementBicubic = library.importContinuousEvaluator( "test_mesh.element.bicubic_hermite",
            "library.fem.scaled_bicubic_hermite" );
        elementBicubic.alias( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.2d" ) );
        elementBicubic.alias( hermiteScaling, parameterScalingDomain );
        elementBicubic.alias( elementHermiteParameter, bicubicHermiteParametersDomain );
        testRegion.addEvaluator( elementBicubic );

        ContinuousPiecewiseEvaluator meshCoordinatesH3 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.H3", mesh1DDomain,
            meshDomain.getElementDomain() );
        meshCoordinatesH3.setEvaluator( 1, elementBicubic );
        meshCoordinatesH3.setEvaluator( 2, elementBicubic );
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

        ContinuousParameters meshdZ = new ContinuousParameters( "test_mesh.node.dz_ds", meshdZdomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdZ.setValue( new int[]
        { 1, 1 }, -1.0 );
        meshdZ.setValue( new int[]
        { 1, 2 }, 1.0 );
        meshdZ.setValue( new int[]
        { 2, 1 }, 1.0 );
        meshdZ.setValue( new int[]
        { 2, 2 }, 1.0 );
        meshdZ.setValue( new int[]
        { 3, 1 }, -1.0 );
        meshdZ.setValue( new int[]
        { 3, 2 }, -1.0 );
        meshdZ.setValue( new int[]
        { 4, 1 }, 1.0 );
        meshdZ.setValue( new int[]
        { 4, 2 }, -1.0 );
        meshdZ.setValue( new int[]
        { 5, 1 }, -1.0 );
        meshdZ.setValue( new int[]
        { 5, 2 }, 1.0 );
        meshdZ.setValue( new int[]
        { 6, 1 }, -1.0 );
        meshdZ.setValue( new int[]
        { 6, 2 }, -1.0 );
        testRegion.addEvaluator( meshdZ );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z_ds1ds2", meshd2Zdomain, globalNodesDomain );
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
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.du_ds", meshdZ );
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.d2u_ds2", meshd2Z );
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
