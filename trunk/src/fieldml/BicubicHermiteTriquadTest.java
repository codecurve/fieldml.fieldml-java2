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
import fieldml.field.PiecewiseField;
import fieldml.region.Region;
import fieldml.region.SubRegion;
import fieldml.region.WorldRegion;
import fieldmlx.util.MinimalColladaExporter;

public class BicubicHermiteTriquadTest
    extends FieldmlTestCase
{
    public static String REGION_NAME = "BicubicHermiteTriquad_Test";


    public void testSerialization()
    {
        WorldRegion world = new WorldRegion();
        Region region = buildRegion( world );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1_______2_______3\n" );
        s.append( "\\       |       /\n" );
        s.append( " \\      |      / \n" );
        s.append( "  \\     5     /  \n" );
        s.append( "   \\   / \\   /   \n" );
        s.append( "    \\ /   \\ /    \n" );
        s.append( "     4     6     \n" );
        s.append( "      \\   /      \n" );
        s.append( "       \\ /       \n" );
        s.append( "        7        \n" );

        serialize( region, s.toString() ); 
    }


    public void testEvaluation()
    {
        // TODO STUB BicubicHermiteTriquadTest.testEvaluation
    }


    public static Region buildRegion( Region parent )
    {
        Region library = parent.getLibrary();
        Region testRegion = new SubRegion( REGION_NAME, parent );

        ContinuousDomain rc2Domain = library.getContinuousDomain( "library.coordinates.rc.2d" );
        EnsembleDomain pointDomain = library.getEnsembleDomain( "library.topology.0d" );
        EnsembleDomain baseElementDomain = library.getEnsembleDomain( "library.topology.2d" );

        MeshDomain meshDomain = new MeshDomain( testRegion, "test_mesh.domain", rc2Domain, baseElementDomain, 3 );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 3, "library.shape.quad.00_10_01_11" );

        EnsembleDomain globalNodeDomain = new EnsembleDomain( testRegion, "test_mesh.nodes", pointDomain, 7 );

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );
        
        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodeDomain,
            meshDomain.getElementDomain(), quad1x1LocalNodeDomain );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 2, 6, 3, 5, 2 );
        quadNodeList.setValue( 3, 6, 5, 7, 4 );
        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.coordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.coordinates.rc.3d" );

        ContinuousDomain meshddsDomain = new ContinuousDomain( testRegion, "test_mesh.coordinates.d/ds" );

        EnsembleDomain anonymous = library.getEnsembleDomain( "library.anonymous" );
        
        ContinuousDomain meshddsListDomain = new ContinuousDomain( testRegion, "test_mesh.coordinates.d/ds", anonymous );

        ContinuousDomain meshd2dsDomain = new ContinuousDomain( testRegion, "test_mesh.coordinates.d2/ds1ds2" );

        ContinuousVariableEvaluator nodalUDofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.u", mesh1DDomain, globalNodeDomain );
        testRegion.addEvaluator( nodalUDofs );

        ContinuousVariableEvaluator nodaldUdSDofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.du/ds", meshddsListDomain, globalNodeDomain );
        testRegion.addEvaluator( nodaldUdSDofs );

        ContinuousVariableEvaluator nodald2UdS2Dofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.d2u/ds2", mesh1DDomain, globalNodeDomain );
        testRegion.addEvaluator( nodald2UdS2Dofs );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.list" );

        ContinuousParameters meshd_ds1Weights = new ContinuousParameters( "test_mesh.node.ds1.weights", weightingDomain,
            meshDomain.getElementDomain(), globalNodeDomain );
        meshd_ds1Weights.setValue( new int[]{ 1, 4 }, 1.0, 0.0 );
        meshd_ds1Weights.setValue( new int[]{ 1, 5 }, 1.0, 1.0 );
        meshd_ds1Weights.setValue( new int[]{ 1, 1 }, 1.0, 0.0 );
        meshd_ds1Weights.setValue( new int[]{ 1, 2 }, 0.0, 1.0 );

        meshd_ds1Weights.setValue( new int[]{ 2, 6 }, 1.0, 0.0 );
        meshd_ds1Weights.setValue( new int[]{ 2, 3 }, 1.0, 0.0 );
        meshd_ds1Weights.setValue( new int[]{ 2, 5 }, 1.0, 0.0 );
        meshd_ds1Weights.setValue( new int[]{ 2, 2 }, 1.0, 0.0 );

        meshd_ds1Weights.setValue( new int[]{ 3, 6 }, 0.0, 1.0 );
        meshd_ds1Weights.setValue( new int[]{ 3, 5 }, 0.0, -1.0 );
        meshd_ds1Weights.setValue( new int[]{ 3, 7 }, 0.0, 1.0 );
        meshd_ds1Weights.setValue( new int[]{ 3, 4 }, 0.0, 1.0 );

        testRegion.addEvaluator( meshd_ds1Weights );

        ContinuousParameters meshd_ds2Weights = new ContinuousParameters( "test_mesh.node.ds2.weights", weightingDomain,
            meshDomain.getElementDomain(), globalNodeDomain );
        meshd_ds2Weights.setValue( new int[]{ 1, 4 }, 0.0, 1.0 );
        meshd_ds2Weights.setValue( new int[]{ 1, 5 }, 1.0, 0.0 );
        meshd_ds2Weights.setValue( new int[]{ 1, 1 }, 0.0, 1.0 );
        meshd_ds2Weights.setValue( new int[]{ 1, 2 }, 1.0, 0.0 );

        meshd_ds2Weights.setValue( new int[]{ 2, 6 }, 0.0, 1.0 );
        meshd_ds2Weights.setValue( new int[]{ 2, 3 }, 0.0, 1.0 );
        meshd_ds2Weights.setValue( new int[]{ 2, 5 }, 0.0, -1.0 );
        meshd_ds2Weights.setValue( new int[]{ 2, 2 }, 0.0, -1.0 );

        meshd_ds2Weights.setValue( new int[]{ 3, 6 }, -1.0, 0.0 );
        meshd_ds2Weights.setValue( new int[]{ 3, 5 }, -1.0, -1.0 );
        meshd_ds2Weights.setValue( new int[]{ 3, 7 }, -1.0, 0.0 );
        meshd_ds2Weights.setValue( new int[]{ 3, 4 }, -1.0, 0.0 );

        testRegion.addEvaluator( meshd_ds2Weights );
        
        DotProductEvaluator meshdUdsValue = new DotProductEvaluator( "test_mesh.node.du/ds", meshddsDomain, meshddsListDomain, weightingDomain );
        testRegion.addEvaluator( meshdUdsValue );

        ContinuousCompositeEvaluator meshdUds1 = new ContinuousCompositeEvaluator( "test_mesh.node.du/ds1", meshddsDomain );
        meshdUds1.importField( nodaldUdSDofs );
        meshdUds1.importField( meshd_ds1Weights );
        meshdUds1.importField( meshdUdsValue );
        testRegion.addEvaluator( meshdUds1 );

        ContinuousCompositeEvaluator meshdUds2 = new ContinuousCompositeEvaluator( "test_mesh.node.du/ds2", meshddsDomain );
        meshdUds2.importField( nodaldUdSDofs );
        meshdUds2.importField( meshd_ds2Weights );
        meshdUds2.importField( meshdUdsValue );
        testRegion.addEvaluator( meshdUds2 );

        EnsembleDomain hermiteDerivativesDomain = library.getEnsembleDomain( "library.interpolation.hermite.derivatives" );
        
        ContinuousPiecewiseEvaluator bicubicHermiteParameters = new ContinuousPiecewiseEvaluator( "test_mesh.bicubic_hermite_parameters",
            mesh1DDomain, hermiteDerivativesDomain );
        bicubicHermiteParameters.setEvaluator( 1, nodalUDofs );
        bicubicHermiteParameters.setEvaluator( 2, meshdUds1 );
        bicubicHermiteParameters.setEvaluator( 3, meshdUds2 );
        bicubicHermiteParameters.setEvaluator( 4, nodald2UdS2Dofs );
        testRegion.addEvaluator( bicubicHermiteParameters );

        EnsembleDomain hermiteParameterDomain = library.getEnsembleDomain( "library.interpolation.hermite.bicubic" );

        EnsembleParameters hermiteLocalNodeNumber = new EnsembleParameters( "test_mesh.bicubic_hermite.local_node", quad1x1LocalNodeDomain,
            hermiteParameterDomain );
        hermiteLocalNodeNumber.setValues( 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4 );

        EnsembleParameters hermiteNodeDerivativeNumber = new EnsembleParameters( "test_mesh.bicubic_hermite.nodal_parameter",
            hermiteDerivativesDomain, hermiteParameterDomain );
        hermiteNodeDerivativeNumber.setValues( 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4 );

        ContinuousDomain bicubicHermiteParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.parameters" );

        ContinuousCompositeEvaluator elementHermiteParameter = new ContinuousCompositeEvaluator(
            "test_mesh.bicubic_hermite.element_parameter", mesh1DDomain );
        elementHermiteParameter.importField( hermiteLocalNodeNumber );
        elementHermiteParameter.importField( hermiteNodeDerivativeNumber );
        elementHermiteParameter.importField( quadNodeList );
        elementHermiteParameter.importField( bicubicHermiteParameters );

        ContinuousEvaluator bicubicHermite = library.getContinuousEvaluator( "library.function.bicubic_hermite" );

        ContinuousEvaluator elementBicubicHermiteEvaluator = new DotProductEvaluator( "test_mesh.element.bicubic_hermite_evaluator", mesh1DDomain,
            bicubicHermiteParametersDomain, weightingDomain );
        testRegion.addEvaluator( elementBicubicHermiteEvaluator );

        ContinuousCompositeEvaluator elementBicubicHermite = new ContinuousCompositeEvaluator( "test_mesh.element.bicubic_hermite", mesh1DDomain );
        elementBicubicHermite.aliasValue( meshDomain.getXiDomain(), library.getContinuousDomain( "library.xi.rc.2d" ) );
        elementBicubicHermite.importField( elementHermiteParameter, bicubicHermiteParametersDomain );
        elementBicubicHermite.importField( bicubicHermite, weightingDomain );
        elementBicubicHermite.importField( elementBicubicHermiteEvaluator );
        testRegion.addEvaluator( elementBicubicHermite );

        ContinuousPiecewiseEvaluator meshCoordinatesH3 = new ContinuousPiecewiseEvaluator( "test_mesh.coordinates.h3", mesh1DDomain, meshDomain.getElementDomain() );
        meshCoordinatesH3.setEvaluator( 1, elementBicubicHermite );
        meshCoordinatesH3.setEvaluator( 2, elementBicubicHermite );
        meshCoordinatesH3.setEvaluator( 3, elementBicubicHermite );
        testRegion.addEvaluator( meshCoordinatesH3 );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodeDomain );
        testRegion.addEvaluator( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", mesh1DDomain, globalNodeDomain );
        testRegion.addEvaluator( meshY );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", mesh1DDomain, globalNodeDomain );
        testRegion.addEvaluator( meshZ );

        final double alpha1 = Math.sqrt( 1.0 / 3.0 );
        final double alpha2 = alpha1 - Math.sqrt( 0.75 );
        final double alpha3 = alpha1 - Math.sqrt( 3 );

        meshX.setValue( 1, -1.0 );
        meshY.setValue( 1, alpha1 );
        meshZ.setValue( 1, 0.0 );

        meshX.setValue( 2, 0.0 );
        meshY.setValue( 2, alpha1 );
        meshZ.setValue( 2, 0.0 );

        meshX.setValue( 3, 1.0 );
        meshY.setValue( 3, alpha1 );
        meshZ.setValue( 3, 0.0 );

        meshX.setValue( 4, -0.5 );
        meshY.setValue( 4, alpha2 );
        meshZ.setValue( 4, 0.0 );

        meshX.setValue( 5, 0.0 );
        meshY.setValue( 5, 0.0 );
        meshZ.setValue( 5, 0.0 );

        meshX.setValue( 6, 0.5 );
        meshY.setValue( 6, alpha2 );
        meshZ.setValue( 6, 0.0 );

        meshX.setValue( 7, 0.0 );
        meshY.setValue( 7, alpha3 );
        meshZ.setValue( 7, 0.0 );

        ContinuousParameters meshdX = new ContinuousParameters( "test_mesh.node.dx/ds", meshddsListDomain, globalNodeDomain );
        meshdX.setDefaultValue( 0, 0 );
        testRegion.addEvaluator( meshdX );

        ContinuousParameters meshdY = new ContinuousParameters( "test_mesh.node.dy/ds", meshddsListDomain, globalNodeDomain );
        meshdY.setDefaultValue( 0, 0 );
        testRegion.addEvaluator( meshdY );

        ContinuousParameters meshdZ = new ContinuousParameters( "test_mesh.node.dz/ds", meshddsListDomain, globalNodeDomain );
        meshdZ.setDefaultValue( 0, 0 );
        testRegion.addEvaluator( meshdZ );

        double vxNorthWestOuter = Math.cos( 2 * Math.PI / 3 );
        double vyNorthWestOuter = Math.sin( 2 * Math.PI / 3 );
        double vxNorthEastOuter = Math.cos( 2 * Math.PI / 6 );
        double vyNorthEastOuter = Math.sin( 2 * Math.PI / 6 );

        double vxNorthEastInner = Math.cos( 2 * Math.PI / 12 );
        double vyNorthEastInner = Math.sin( 2 * Math.PI / 12 );
        double vxNorthWestInner = Math.cos( 2 * Math.PI * 5 / 12 );
        double vyNorthWestInner = Math.sin( 2 * Math.PI * 5 / 12 );

        meshdX.setValue( 1, 1.0, 0.0 );
        meshdY.setValue( 1, 0.0, 1.0 );
        meshdZ.setValue( 1, 0.0, 0.0 );

        meshdX.setValue( 2, 0.0, 1.0 );
        meshdY.setValue( 2, 1.0, 0.0 );
        meshdZ.setValue( 2, 1.0, 0.0 );

        meshdX.setValue( 3, 0.0, -1.0 );
        meshdY.setValue( 3, 1.0, 0.0 );
        meshdZ.setValue( 3, 0.0, 0.0 );

        meshdX.setValue( 4, vxNorthEastInner, 0.0 );
        meshdY.setValue( 4, vyNorthEastInner, 1.0 );
        meshdZ.setValue( 4, 0.0, 1.0 );

        meshdX.setValue( 5, 0.0, -vxNorthWestInner );
        meshdY.setValue( 5, 1.0, -vyNorthWestInner );
        meshdZ.setValue( 5, 1.0, 0.0 );

        meshdX.setValue( 6, 0.0, vxNorthWestInner );
        meshdY.setValue( 6, 1.0, vyNorthWestInner );
        meshdZ.setValue( 6, 1.0, 0.0 );

        meshdX.setValue( 7, vxNorthEastOuter, vxNorthWestOuter );
        meshdY.setValue( 7, vyNorthEastOuter, vyNorthWestOuter );
        meshdZ.setValue( 7, 0.0, 0.0 );

        ContinuousParameters meshd2X = new ContinuousParameters( "test_mesh.node.d2x/ds1ds2", meshd2dsDomain, globalNodeDomain );
        meshd2X.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2X );

        ContinuousParameters meshd2Y = new ContinuousParameters( "test_mesh.node.d2y/ds1ds2", meshd2dsDomain, globalNodeDomain );
        meshd2Y.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2Y );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2dsDomain, globalNodeDomain );
        meshd2Z.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2Z );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesX.setVariable( "test_mesh.node.dofs.u", meshX );
        meshCoordinatesX.setVariable( "test_mesh.node.dofs.du/ds", meshdX );
        meshCoordinatesX.setVariable( "test_mesh.node.dofs.d2u/ds2", meshd2X );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesY.setVariable( "test_mesh.node.dofs.u", meshY );
        meshCoordinatesY.setVariable( "test_mesh.node.dofs.du/ds", meshdY );
        meshCoordinatesY.setVariable( "test_mesh.node.dofs.d2u/ds2", meshd2Y );

        testRegion.addEvaluator( meshCoordinatesY );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.u", meshZ );
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.du/ds", meshdZ );
        meshCoordinatesZ.setVariable( "test_mesh.node.dofs.d2u/ds2", meshd2Z );

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

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, 64, "test_mesh.domain", "test_mesh.coordinates" );
        FileWriter f = new FileWriter( "trunk/data/collada three quads.xml" );
        f.write( collada );
        f.close();

    }
}
