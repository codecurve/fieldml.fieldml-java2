package fieldml;

import java.io.FileWriter;
import java.io.IOException;

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
import fieldml.evaluator.ContinuousMap;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.function.BicubicHermiteQuad;
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
            outputter.output( doc, System.out );
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

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

        Region testRegion = new Region( REGION_NAME );

        EnsembleDomain edgeDirectionDomain = new EnsembleDomain( "test_mesh.edge_direction" );
        edgeDirectionDomain.addValues( 1, 2 );
        testRegion.addDomain( edgeDirectionDomain );

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

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );

        quadNodeList.setValue( 4, 1, 1 );
        quadNodeList.setValue( 5, 1, 2 );
        quadNodeList.setValue( 1, 1, 3 );
        quadNodeList.setValue( 2, 1, 4 );

        quadNodeList.setValue( 6, 2, 1 );
        quadNodeList.setValue( 3, 2, 2 );
        quadNodeList.setValue( 5, 2, 3 );
        quadNodeList.setValue( 2, 2, 4 );

        quadNodeList.setValue( 6, 3, 1 );
        quadNodeList.setValue( 5, 3, 2 );
        quadNodeList.setValue( 7, 3, 3 );
        quadNodeList.setValue( 4, 3, 4 );

        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.co-ordinates.rc.3d" );

        ContinuousDomain meshddsDomain = new ContinuousDomain( "test_mesh.co-ordinates.d/ds", 1 );
        testRegion.addDomain( meshddsDomain );

        ContinuousDomain meshd2dsDomain = new ContinuousDomain( "test_mesh.co-ordinates.d2/ds1ds2", 1 );
        testRegion.addDomain( meshd2dsDomain );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( meshY );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", mesh1DDomain, globalNodesDomain );
        testRegion.addEvaluator( meshZ );

        final double alpha1 = Math.sqrt( 1.0 / 3.0 );
        final double alpha2 = alpha1 - Math.sqrt( 0.75 );
        final double alpha3 = alpha1 - Math.sqrt( 3 );

        meshX.setValue( -1.0, 1 );
        meshY.setValue( alpha1, 1 );
        meshZ.setValue( 0.0, 1 );

        meshX.setValue( 0.0, 2 );
        meshY.setValue( alpha1, 2 );
        meshZ.setValue( 0.0, 2 );

        meshX.setValue( 1.0, 3 );
        meshY.setValue( alpha1, 3 );
        meshZ.setValue( 0.0, 3 );

        meshX.setValue( -0.5, 4 );
        meshY.setValue( alpha2, 4 );
        meshZ.setValue( 0.0, 4 );

        meshX.setValue( 0.0, 5 );
        meshY.setValue( 0.0, 5 );
        meshZ.setValue( 0.0, 5 );

        meshX.setValue( 0.5, 6 );
        meshY.setValue( alpha2, 6 );
        meshZ.setValue( 0.0, 6 );

        meshX.setValue( 0.0, 7 );
        meshY.setValue( alpha3, 7 );
        meshZ.setValue( 0.0, 7 );

        ContinuousParameters meshdX = new ContinuousParameters( "test_mesh.node.dx/ds", meshddsDomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdX.setDefaultValue( 0 );
        testRegion.addEvaluator( meshdX );

        ContinuousParameters meshdY = new ContinuousParameters( "test_mesh.node.dy/ds", meshddsDomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdY.setDefaultValue( 0 );
        testRegion.addEvaluator( meshdY );

        ContinuousParameters meshdZ = new ContinuousParameters( "test_mesh.node.dz/ds", meshddsDomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdZ.setDefaultValue( 0 );
        testRegion.addEvaluator( meshdZ );

        double vxNorthWestOuter = Math.cos( 2 * Math.PI / 3 );
        double vyNorthWestOuter = Math.sin( 2 * Math.PI / 3 );
        double vxNorthEastOuter = Math.cos( 2 * Math.PI / 6 );
        double vyNorthEastOuter = Math.sin( 2 * Math.PI / 6 );

        double vxNorthEastInner = Math.cos( 2 * Math.PI / 12 );
        double vyNorthEastInner = Math.sin( 2 * Math.PI / 12 );
        double vxNorthWestInner = Math.cos( 2 * Math.PI * 5 / 12 );
        double vyNorthWestInner = Math.sin( 2 * Math.PI * 5 / 12 );

        meshdX.setValue( 1.0, 1, 1 );
        meshdY.setValue( 0.0, 1, 1 );
        meshdZ.setValue( 0.0, 1, 1 );

        meshdX.setValue( 0.0, 1, 2 );
        meshdY.setValue( 1.0, 1, 2 );
        meshdZ.setValue( 0.0, 1, 2 );

        meshdX.setValue( 0.0, 2, 1 );
        meshdY.setValue( 1.0, 2, 1 );
        meshdZ.setValue( 1.0, 2, 1 );

        meshdX.setValue( 1.0, 2, 2 );
        meshdY.setValue( 0.0, 2, 2 );
        meshdZ.setValue( 0.0, 2, 2 );

        meshdX.setValue( 0.0, 3, 1 );
        meshdY.setValue( 1.0, 3, 1 );
        meshdZ.setValue( 0.0, 3, 1 );

        meshdX.setValue( -1.0, 3, 2 );
        meshdY.setValue( 0.0, 3, 2 );
        meshdZ.setValue( 0.0, 3, 2 );

        meshdX.setValue( vxNorthEastInner, 4, 1 );
        meshdY.setValue( vyNorthEastInner, 4, 1 );
        meshdZ.setValue( 0.0, 4, 1 );

        meshdX.setValue( 0.0, 4, 2 );
        meshdY.setValue( 1.0, 4, 2 );
        meshdZ.setValue( 1.0, 4, 2 );

        meshdX.setValue( 0.0, 5, 1 );
        meshdY.setValue( 1.0, 5, 1 );
        meshdZ.setValue( 1.0, 5, 1 );

        meshdX.setValue( -vxNorthWestInner, 5, 2 );
        meshdY.setValue( -vyNorthWestInner, 5, 2 );
        meshdZ.setValue( 0.0, 5, 2 );

        meshdX.setValue( 0.0, 6, 1 );
        meshdY.setValue( 1.0, 6, 1 );
        meshdZ.setValue( 1.0, 6, 1 );

        meshdX.setValue( vxNorthWestInner, 6, 2 );
        meshdY.setValue( vyNorthWestInner, 6, 2 );
        meshdZ.setValue( 0.0, 6, 2 );

        meshdX.setValue( vxNorthEastOuter, 7, 1 );
        meshdY.setValue( vyNorthEastOuter, 7, 1 );
        meshdZ.setValue( 0.0, 7, 1 );

        meshdX.setValue( vxNorthWestOuter, 7, 2 );
        meshdY.setValue( vyNorthWestOuter, 7, 2 );
        meshdZ.setValue( 0.0, 7, 2 );

        ContinuousParameters meshd2X = new ContinuousParameters( "test_mesh.node.d2x/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2X.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2X );

        ContinuousParameters meshd2Y = new ContinuousParameters( "test_mesh.node.d2y/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2Y.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2Y );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2Z.setDefaultValue( 0.0 );
        testRegion.addEvaluator( meshd2Z );

        ContinuousDomain bicubicHermiteScalingDomain = library.getContinuousDomain( "library.bicubic_hermite.scaling" );

        ContinuousParameters bicubicZHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.z",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicZHermiteQuadScaling.setDefaultValue( bicubicHermiteScalingDomain.makeValue( 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicZHermiteQuadScaling );

        ContinuousParameters bicubicXHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.x",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicXHermiteQuadScaling.setDefaultValue( bicubicHermiteScalingDomain.makeValue( 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicXHermiteQuadScaling );

        ContinuousParameters bicubicYHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.y",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicYHermiteQuadScaling.setDefaultValue( bicubicHermiteScalingDomain.makeValue( 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicYHermiteQuadScaling );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.1d" );

        ContinuousParameters meshd_ds1Weights = new ContinuousParameters( "test_mesh.node.ds1.weights", weightingDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain, edgeDirectionDomain );
        meshd_ds1Weights.setDefaultValue( 0.0 );

        meshd_ds1Weights.setValue( 1.0, 1, 1, 1 );
        meshd_ds1Weights.setValue( 1.0, 1, 2, 1 );
        meshd_ds1Weights.setValue( 1.0, 1, 2, 2 );
        meshd_ds1Weights.setValue( 1.0, 1, 3, 1 );
        meshd_ds1Weights.setValue( 1.0, 1, 4, 2 );

        meshd_ds1Weights.setValue( 1.0, 2, 1, 1 );
        meshd_ds1Weights.setValue( 1.0, 2, 2, 1 );
        meshd_ds1Weights.setValue( 1.0, 2, 3, 1 );
        meshd_ds1Weights.setValue( 1.0, 2, 4, 1 );

        meshd_ds1Weights.setValue( 1.0, 3, 1, 2 );
        meshd_ds1Weights.setValue( -1.0, 3, 2, 2 );
        meshd_ds1Weights.setValue( 1.0, 3, 3, 2 );
        meshd_ds1Weights.setValue( 1.0, 3, 4, 2 );

        testRegion.addEvaluator( meshd_ds1Weights );

        ContinuousMap meshd_ds1Map = new ContinuousMap( "test_mesh.node.ds1.map", meshd_ds1Weights, edgeDirectionDomain );
        testRegion.addMap( meshd_ds1Map );

        ContinuousParameters meshd_ds2Weights = new ContinuousParameters( "test_mesh.node.ds2.weights", weightingDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain, edgeDirectionDomain );
        meshd_ds2Weights.setDefaultValue( 0.0 );
        meshd_ds2Weights.setValue( 1.0, 1, 1, 2 );
        meshd_ds2Weights.setValue( 1.0, 1, 2, 1 );
        meshd_ds2Weights.setValue( 1.0, 1, 3, 2 );
        meshd_ds2Weights.setValue( 1.0, 1, 4, 1 );

        meshd_ds2Weights.setValue( 1.0, 2, 1, 2 );
        meshd_ds2Weights.setValue( 1.0, 2, 2, 2 );
        meshd_ds2Weights.setValue( -1.0, 2, 3, 2 );
        meshd_ds2Weights.setValue( -1.0, 2, 4, 2 );

        meshd_ds2Weights.setValue( -1.0, 3, 1, 1 );
        meshd_ds2Weights.setValue( -1.0, 3, 2, 1 );
        meshd_ds2Weights.setValue( -1.0, 3, 2, 2 );
        meshd_ds2Weights.setValue( -1.0, 3, 3, 1 );
        meshd_ds2Weights.setValue( -1.0, 3, 4, 1 );

        testRegion.addEvaluator( meshd_ds2Weights );

        ContinuousMap meshd_ds2Map = new ContinuousMap( "test_mesh.node.ds2.map", meshd_ds2Weights, edgeDirectionDomain );
        testRegion.addMap( meshd_ds2Map );

        ContinuousCompositeEvaluator meshdXds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dx/ds1", meshddsDomain );
        meshdXds1.importMap( meshddsDomain, meshdX, meshd_ds1Map );

        testRegion.addEvaluator( meshdXds1 );

        ContinuousCompositeEvaluator meshdXds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dx/ds2", meshddsDomain );
        meshdXds2.importMap( meshddsDomain, meshdX, meshd_ds2Map );

        testRegion.addEvaluator( meshdXds2 );

        ContinuousCompositeEvaluator meshdYds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dy/ds1", meshddsDomain );
        meshdYds1.importMap( meshddsDomain, meshdY, meshd_ds1Map );

        testRegion.addEvaluator( meshdYds1 );

        ContinuousCompositeEvaluator meshdYds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dy/ds2", meshddsDomain );
        meshdYds2.importMap( meshddsDomain, meshdY, meshd_ds2Map );

        testRegion.addEvaluator( meshdYds2 );

        ContinuousCompositeEvaluator meshdZds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds1", meshddsDomain );
        meshdZds1.importMap( meshddsDomain, meshdZ, meshd_ds1Map );

        testRegion.addEvaluator( meshdZds1 );

        ContinuousCompositeEvaluator meshdZds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds2", meshddsDomain );
        meshdZds2.importMap( meshddsDomain, meshdZ, meshd_ds2Map );

        testRegion.addEvaluator( meshdZds2 );

        ContinuousDomain bicubicHermiteParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.parameters" );

        ContinuousAggregateEvaluator bicubicXHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_parameters.x",
            bicubicHermiteParametersDomain );
        bicubicXHermiteParameters.setSourceField( 1, meshX );
        bicubicXHermiteParameters.setSourceField( 2, meshdXds1 );
        bicubicXHermiteParameters.setSourceField( 3, meshdXds2 );
        bicubicXHermiteParameters.setSourceField( 4, meshd2X );

        testRegion.addEvaluator( bicubicXHermiteParameters );

        ContinuousAggregateEvaluator bicubicYHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_parameters.y",
            bicubicHermiteParametersDomain );
        bicubicYHermiteParameters.setSourceField( 1, meshY );
        bicubicYHermiteParameters.setSourceField( 2, meshdYds1 );
        bicubicYHermiteParameters.setSourceField( 3, meshdYds2 );
        bicubicYHermiteParameters.setSourceField( 4, meshd2Y );

        testRegion.addEvaluator( bicubicYHermiteParameters );

        ContinuousAggregateEvaluator bicubicZHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_parameters.z",
            bicubicHermiteParametersDomain );
        bicubicZHermiteParameters.setSourceField( 1, meshZ );
        bicubicZHermiteParameters.setSourceField( 2, meshdZds1 );
        bicubicZHermiteParameters.setSourceField( 3, meshdZds2 );
        bicubicZHermiteParameters.setSourceField( 4, meshd2Z );

        testRegion.addEvaluator( bicubicZHermiteParameters );

        PiecewiseTemplate meshCoordinatesH3 = new PiecewiseTemplate( "test_mesh.coordinates.h3", meshDomain );
        meshCoordinatesH3.addFunction( new BicubicHermiteQuad( "hermite_quad", bicubicHermiteParametersDomain, bicubicXHermiteQuadScaling,
            quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesH3.setFunction( 1, "hermite_quad" );
        meshCoordinatesH3.setFunction( 2, "hermite_quad" );
        meshCoordinatesH3.setFunction( 3, "hermite_quad" );
        testRegion.addPiecewiseTemplate( meshCoordinatesH3 );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesX.addDofs( bicubicXHermiteParameters );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesY.addDofs( bicubicYHermiteParameters );

        testRegion.addEvaluator( meshCoordinatesY );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesZ.addDofs( bicubicZHermiteParameters );

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
