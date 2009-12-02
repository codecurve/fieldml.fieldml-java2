package fieldml;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

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
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.PiecewiseField;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.function.BicubicHermiteQuad;
import fieldml.function.BilinearQuad;
import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldmlx.util.MinimalColladaExporter;

public class BicubicHermiteTriquadTest
{
    private static void serialize( Region region )
    {
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


    private static void test( Region region )
    {
        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        ContinuousEvaluator meshZ = region.getContinuousEvaluator( "test_mesh.coordinates" );
    }


    public static void main( String[] args )
        throws FileNotFoundException, IOException
    {
        Region library = Region.getLibrary();

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

        Region testRegion = new Region( "test" );

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

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );

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
        meshdZ.setValue( 0.0, 2, 1 );

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
        bicubicZHermiteQuadScaling.setDefaultValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicZHermiteQuadScaling );

        ContinuousParameters bicubicXHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.x",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicXHermiteQuadScaling.setDefaultValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicXHermiteQuadScaling );

        ContinuousParameters bicubicYHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.y",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicYHermiteQuadScaling.setDefaultValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 1, 1 ) );
        testRegion.addEvaluator( bicubicYHermiteQuadScaling );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.1d" );

        ContinuousParameters meshd_ds1Map = new ContinuousParameters( "test_mesh.node.ds1.map", weightingDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain, edgeDirectionDomain );
        meshd_ds1Map.setDefaultValue( 0.0 );

        meshd_ds1Map.setValue( 1.0, 1, 1, 1 );
        meshd_ds1Map.setValue( 1.0, 1, 2, 1 );
        meshd_ds1Map.setValue( 1.0, 1, 2, 2 );
        meshd_ds1Map.setValue( 1.0, 1, 3, 1 );
        meshd_ds1Map.setValue( 1.0, 1, 4, 2 );

        meshd_ds1Map.setValue( 1.0, 2, 1, 1 );
        meshd_ds1Map.setValue( 1.0, 2, 2, 1 );
        meshd_ds1Map.setValue( 1.0, 2, 3, 1 );
        meshd_ds1Map.setValue( 1.0, 2, 4, 1 );

        meshd_ds1Map.setValue( 1.0, 3, 1, 2 );
        meshd_ds1Map.setValue( -1.0, 3, 2, 2 );
        meshd_ds1Map.setValue( 1.0, 3, 3, 2 );
        meshd_ds1Map.setValue( 1.0, 3, 4, 2 );

        testRegion.addEvaluator( meshd_ds1Map );

        ContinuousParameters meshd_ds2Map = new ContinuousParameters( "test_mesh.node.ds2.map", weightingDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain, edgeDirectionDomain );
        meshd_ds2Map.setDefaultValue( 0.0 );
        meshd_ds2Map.setValue( 1.0, 1, 1, 2 );
        meshd_ds2Map.setValue( 1.0, 1, 2, 1 );
        meshd_ds2Map.setValue( 1.0, 1, 3, 2 );
        meshd_ds2Map.setValue( 1.0, 1, 4, 1 );

        meshd_ds2Map.setValue( 1.0, 2, 1, 2 );
        meshd_ds2Map.setValue( 1.0, 2, 2, 2 );
        meshd_ds2Map.setValue( -1.0, 2, 3, 2 );
        meshd_ds2Map.setValue( -1.0, 2, 4, 2 );

        meshd_ds2Map.setValue( -1.0, 3, 1, 1 );
        meshd_ds2Map.setValue( -1.0, 3, 2, 1 );
        meshd_ds2Map.setValue( -1.0, 3, 2, 2 );
        meshd_ds2Map.setValue( -1.0, 3, 3, 1 );
        meshd_ds2Map.setValue( -1.0, 3, 4, 1 );

        testRegion.addEvaluator( meshd_ds2Map );

        ContinuousCompositeEvaluator meshdXds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dx/ds1", meshddsDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdXds1.importMappedField( meshdX, meshd_ds1Map, edgeDirectionDomain );

        testRegion.addEvaluator( meshdXds1 );

        ContinuousCompositeEvaluator meshdXds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dx/ds2", meshddsDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdXds2.importMappedField( meshdX, meshd_ds2Map, edgeDirectionDomain );

        testRegion.addEvaluator( meshdXds2 );

        ContinuousCompositeEvaluator meshdYds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dy/ds1", meshddsDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdYds1.importMappedField( meshdY, meshd_ds1Map, edgeDirectionDomain );

        testRegion.addEvaluator( meshdYds1 );

        ContinuousCompositeEvaluator meshdYds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dy/ds2", meshddsDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdYds2.importMappedField( meshdY, meshd_ds2Map, edgeDirectionDomain );

        testRegion.addEvaluator( meshdYds2 );

        ContinuousCompositeEvaluator meshdZds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds1", meshddsDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdZds1.importMappedField( meshdZ, meshd_ds1Map, edgeDirectionDomain );

        testRegion.addEvaluator( meshdZds1 );

        ContinuousCompositeEvaluator meshdZds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds2", meshddsDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdZds2.importMappedField( meshdZ, meshd_ds2Map, edgeDirectionDomain );

        testRegion.addEvaluator( meshdZds2 );

        ContinuousDomain bicubicHermiteParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.parameters" );

        ContinuousAggregateEvaluator bicubicXHermiteParameters = new ContinuousAggregateEvaluator(
            "test_mesh.bicubic_parameters.x", bicubicHermiteParametersDomain );
        bicubicXHermiteParameters.setSourceField( 1, meshX );
        bicubicXHermiteParameters.setSourceField( 2, meshdXds1 );
        bicubicXHermiteParameters.setSourceField( 3, meshdXds2 );
        bicubicXHermiteParameters.setSourceField( 4, meshd2X );

        testRegion.addEvaluator( bicubicXHermiteParameters );

        ContinuousAggregateEvaluator bicubicYHermiteParameters = new ContinuousAggregateEvaluator(
            "test_mesh.bicubic_parameters.y", bicubicHermiteParametersDomain );
        bicubicYHermiteParameters.setSourceField( 1, meshY );
        bicubicYHermiteParameters.setSourceField( 2, meshdYds1 );
        bicubicYHermiteParameters.setSourceField( 3, meshdYds2 );
        bicubicYHermiteParameters.setSourceField( 4, meshd2Y );

        testRegion.addEvaluator( bicubicYHermiteParameters );

        ContinuousAggregateEvaluator bicubicZHermiteParameters = new ContinuousAggregateEvaluator(
            "test_mesh.bicubic_parameters.z", bicubicHermiteParametersDomain );
        bicubicZHermiteParameters.setSourceField( 1, meshZ );
        bicubicZHermiteParameters.setSourceField( 2, meshdZds1 );
        bicubicZHermiteParameters.setSourceField( 3, meshdZds2 );
        bicubicZHermiteParameters.setSourceField( 4, meshd2Z );

        testRegion.addEvaluator( bicubicZHermiteParameters );
        /*
         * 
         * Because piecewise fields are strictly scalar, there is (probably) no reason to share evaluators. Aggregate fields
         * wishing to share components can do so simply by sharing entire piecewise fields.
         */

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshDomain );
        meshCoordinatesX.addEvaluator( new BicubicHermiteQuad( "hermite_quad", bicubicXHermiteParameters,
            bicubicXHermiteQuadScaling, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesX.addEvaluator( new BilinearQuad( "bilinear_quad", meshX, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesX.setEvaluator( 1, "hermite_quad" );
        meshCoordinatesX.setEvaluator( 2, "hermite_quad" );
        meshCoordinatesX.setEvaluator( 3, "hermite_quad" );

        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshDomain );
        meshCoordinatesY.addEvaluator( new BicubicHermiteQuad( "hermite_quad", bicubicYHermiteParameters,
            bicubicYHermiteQuadScaling, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.addEvaluator( new BilinearQuad( "bilinear_quad", meshY, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.setEvaluator( 1, "hermite_quad" );
        meshCoordinatesY.setEvaluator( 2, "hermite_quad" );
        meshCoordinatesY.setEvaluator( 3, "hermite_quad" );

        testRegion.addEvaluator( meshCoordinatesY );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshDomain );
        meshCoordinatesZ.addEvaluator( new BicubicHermiteQuad( "hermite_quad", bicubicZHermiteParameters,
            bicubicZHermiteQuadScaling, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesZ.setEvaluator( 1, "hermite_quad" );
        meshCoordinatesZ.setEvaluator( 2, "hermite_quad" );
        meshCoordinatesZ.setEvaluator( 3, "hermite_quad" );

        testRegion.addEvaluator( meshCoordinatesZ );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates", mesh3DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );
        meshCoordinates.setSourceField( 3, meshCoordinatesZ );

        testRegion.addEvaluator( meshCoordinates );

        test( testRegion );

        serialize( testRegion );

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, "test_mesh.domain", 3, 64 );
        FileWriter f = new FileWriter( "trunk/data/collada three quads.xml" );
        f.write( collada );
        f.close();

    }
}
