package fieldml;

import java.io.FileWriter;
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
import fieldml.domain.ContinuousListDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousListParameters;
import fieldml.evaluator.ContinuousListVariableEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.ContinuousVariableEvaluator;
import fieldml.evaluator.DotProductEvaluator;
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.FunctionEvaluator;
import fieldml.evaluator.MapEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldmlx.util.MinimalColladaExporter;

public class BicubicHermiteTriquadTest
    extends TestCase
{
    public static String REGION_NAME = "BicubicHermiteTriquad_Test";


    public void testSerialization()
    {
        Region region = buildRegion();

        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

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
        // TODO STUB BicubicHermiteTriquadTest.testEvaluation
    }


    public static Region buildRegion()
    {
        Region library = Region.getLibrary();

        Region testRegion = new Region( REGION_NAME );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 3, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        testRegion.addDomain( globalNodesDomain );

        EnsembleListDomain globalNodesListDomain = new EnsembleListDomain( "test_mesh.node_list", globalNodesDomain );
        testRegion.addDomain( globalNodesListDomain );

        EnsembleListParameters quadNodeList = new EnsembleListParameters( "test_mesh.quad_nodes", globalNodesListDomain,
            testMeshElementDomain );
        quadNodeList.setValue( 1, 4, 5, 1, 2 );
        quadNodeList.setValue( 2, 6, 3, 5, 2 );
        quadNodeList.setValue( 3, 6, 5, 7, 4 );
        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.co-ordinates.rc.3d" );

        ContinuousDomain meshddsDomain = new ContinuousDomain( "test_mesh.co-ordinates.d/ds", 1 );
        testRegion.addDomain( meshddsDomain );

        ContinuousListDomain meshddsListDomain = new ContinuousListDomain( "test_mesh.co-ordinates.d/ds.list", meshddsDomain );
        testRegion.addDomain( meshddsListDomain );

        ContinuousDomain meshd2dsDomain = new ContinuousDomain( "test_mesh.co-ordinates.d2/ds1ds2", 1 );
        testRegion.addDomain( meshd2dsDomain );

        ContinuousVariableEvaluator nodalUDofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.u", mesh1DDomain );

        ContinuousListVariableEvaluator nodaldUdSDofs = new ContinuousListVariableEvaluator( "test_mesh.node.dofs.du/ds", meshddsListDomain );

        ContinuousVariableEvaluator nodald2UdS2Dofs = new ContinuousVariableEvaluator( "test_mesh.node.dofs.d2u/ds2", mesh1DDomain );

        ContinuousDomain bicubicHermiteScalingDomain = library.getContinuousDomain( "library.bicubic_hermite.scaling" );

        ContinuousListDomain weightingDomain = library.getContinuousListDomain( "library.weighting.list" );

        ContinuousListParameters meshd_ds1Weights = new ContinuousListParameters( "test_mesh.node.ds1.weights", weightingDomain,
            testMeshElementDomain, globalNodesDomain );
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

        ContinuousListParameters meshd_ds2Weights = new ContinuousListParameters( "test_mesh.node.ds2.weights", weightingDomain,
            testMeshElementDomain, globalNodesDomain );
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

        DotProductEvaluator meshdUds1 = new DotProductEvaluator( "test_mesh.node.du/ds1", meshddsDomain, nodaldUdSDofs, meshd_ds1Weights );
        testRegion.addEvaluator( meshdUds1 );

        DotProductEvaluator meshdUds2 = new DotProductEvaluator( "test_mesh.node.du/ds2", meshddsDomain, nodaldUdSDofs, meshd_ds2Weights );
        testRegion.addEvaluator( meshdUds2 );

        ContinuousDomain bicubicHermiteNodalParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.nodal.parameters" );

        ContinuousAggregateEvaluator bicubicHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_hermite_parameters",
            bicubicHermiteNodalParametersDomain );
        bicubicHermiteParameters.setSourceField( 1, nodalUDofs );
        bicubicHermiteParameters.setSourceField( 2, meshdUds1 );
        bicubicHermiteParameters.setSourceField( 3, meshdUds2 );
        bicubicHermiteParameters.setSourceField( 4, nodald2UdS2Dofs );

        testRegion.addEvaluator( bicubicHermiteParameters );

        FunctionEvaluator meshBicubicHermite = new FunctionEvaluator( "test_mesh.bicubic_hermite", weightingDomain, meshDomain, library
            .getContinuousFunction( "library.function.bicubic_hermite" ) );
        testRegion.addEvaluator( meshBicubicHermite );

        MapEvaluator elementBicubicHermite = new MapEvaluator( "test_mesh.element.bicubic_hermite", mesh1DDomain, quadNodeList,
            meshBicubicHermite, bicubicHermiteParameters );
        testRegion.addEvaluator( elementBicubicHermite );

        PiecewiseTemplate meshCoordinatesH3 = new PiecewiseTemplate( "test_mesh.coordinates.h3", meshDomain );
        meshCoordinatesH3.setEvaluator( 1, elementBicubicHermite );
        meshCoordinatesH3.setEvaluator( 2, elementBicubicHermite );
        meshCoordinatesH3.setEvaluator( 3, elementBicubicHermite );
        testRegion.addPiecewiseTemplate( meshCoordinatesH3 );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( meshY );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", mesh1DDomain, globalNodesDomain );
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

        ContinuousListParameters meshdX = new ContinuousListParameters( "test_mesh.node.dx/ds", meshddsListDomain, globalNodesDomain );
        meshdX.setDefaultValue( 0, 0 );
        testRegion.addEvaluator( meshdX );

        ContinuousListParameters meshdY = new ContinuousListParameters( "test_mesh.node.dy/ds", meshddsListDomain, globalNodesDomain );
        meshdY.setDefaultValue( 0, 0 );
        testRegion.addEvaluator( meshdY );

        ContinuousListParameters meshdZ = new ContinuousListParameters( "test_mesh.node.dz/ds", meshddsListDomain, globalNodesDomain );
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

        ContinuousParameters meshd2X = new ContinuousParameters( "test_mesh.node.d2x/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2X.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2X );

        ContinuousParameters meshd2Y = new ContinuousParameters( "test_mesh.node.d2y/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2Y.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2Y );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2Z.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2Z );

        ContinuousParameters bicubicZHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.z",
            bicubicHermiteScalingDomain, testMeshElementDomain );
        bicubicZHermiteQuadScaling.setDefaultValue( bicubicHermiteScalingDomain.makeValue( 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicZHermiteQuadScaling );

        ContinuousParameters bicubicXHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.x",
            bicubicHermiteScalingDomain, testMeshElementDomain );
        bicubicXHermiteQuadScaling.setDefaultValue( bicubicHermiteScalingDomain.makeValue( 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicXHermiteQuadScaling );

        ContinuousParameters bicubicYHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.y",
            bicubicHermiteScalingDomain, testMeshElementDomain );
        bicubicYHermiteQuadScaling.setDefaultValue( bicubicHermiteScalingDomain.makeValue( 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicYHermiteQuadScaling );

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
        Region testRegion = buildRegion();

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, 64, "test_mesh.domain", "test_mesh.coordinates" );
        FileWriter f = new FileWriter( "trunk/data/collada three quads.xml" );
        f.write( collada );
        f.close();

    }
}
