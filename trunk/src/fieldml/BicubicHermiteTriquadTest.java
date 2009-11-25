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
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.PiecewiseField;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.function.BicubicHermiteQuad;
import fieldml.function.BilinearQuad;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.EnsembleDomainValue;
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

        region.serializeToXml( root );

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
        AbstractEvaluator<?, ?> meshZ = region.getField( "test_mesh.coordinates" );
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

        testRegion.addField( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.co-ordinates.rc.3d" );

        ContinuousDomain meshddsDomain = new ContinuousDomain( "test_mesh.co-ordinates.d/ds", 1 );
        testRegion.addDomain( meshddsDomain );

        ContinuousDomain meshd2dsDomain = new ContinuousDomain( "test_mesh.co-ordinates.d2/ds1ds2", 1 );
        testRegion.addDomain( meshd2dsDomain );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodesDomain );
        meshX.setValue( -1.0, 1 );
        meshX.setValue( 0.0, 2 );
        meshX.setValue( 1.0, 3 );
        meshX.setValue( -0.5, 4 );
        meshX.setValue( 0.0, 5 );
        meshX.setValue( 0.5, 6 );
        meshX.setValue( 0.0, 7 );

        testRegion.addField( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", mesh1DDomain, globalNodesDomain );

        meshY.setValue( Math.sqrt( 1.0 / 3.0 ), 1 );
        meshY.setValue( Math.sqrt( 1.0 / 3.0 ), 2 );
        meshY.setValue( Math.sqrt( 1.0 / 3.0 ), 3 );
        meshY.setValue( Math.sqrt( 1.0 / 3.0 ) - Math.sqrt( 0.75 ), 4 );
        meshY.setValue( 0.0, 5 );
        meshY.setValue( Math.sqrt( 1.0 / 3.0 ) - Math.sqrt( 0.75 ), 6 );
        meshY.setValue( Math.sqrt( 1.0 / 3.0 ) - Math.sqrt( 3 ), 7 );

        testRegion.addField( meshY );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", mesh1DDomain, globalNodesDomain );
        meshZ.setValue( 0.0, 1 );
        meshZ.setValue( 0.0, 2 );
        meshZ.setValue( 0.0, 3 );
        meshZ.setValue( 0.0, 4 );
        meshZ.setValue( 0.0, 5 );
        meshZ.setValue( 0.0, 6 );
        meshZ.setValue( 0.0, 7 );

        testRegion.addField( meshZ );

        ContinuousParameters meshdX = new ContinuousParameters( "test_mesh.node.dx/ds", meshddsDomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdX.setDefaultValue( 1.0 );

        testRegion.addField( meshdX );

        ContinuousParameters meshdY = new ContinuousParameters( "test_mesh.node.dy/ds", meshddsDomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdY.setDefaultValue( 1.0 );

        testRegion.addField( meshdY );

        ContinuousParameters meshdZ = new ContinuousParameters( "test_mesh.node.dz/ds", meshddsDomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdZ.setDefaultValue( 0 );
        meshdZ.setValue( 1.0, 1, 1 );
        meshdZ.setValue( 1.0, 1, 2 );
        meshdZ.setValue( 1.0, 2, 1 );
        meshdZ.setValue( 1.0, 2, 2 );
        meshdZ.setValue( -1.0, 3, 1 );
        meshdZ.setValue( -1.0, 3, 2 );
        meshdZ.setValue( 1.0, 4, 1 );
        meshdZ.setValue( -1.0, 4, 2 );
        meshdZ.setValue( 0.0, 5, 1 );
        meshdZ.setValue( 0.0, 5, 2 );
        meshdZ.setValue( -1.0, 6, 1 );
        meshdZ.setValue( -1.0, 6, 2 );
        meshdZ.setValue( -1.0, 7, 1 );
        meshdZ.setValue( -1.0, 7, 2 );

        testRegion.addField( meshdZ );

        ContinuousParameters meshd2X = new ContinuousParameters( "test_mesh.node.d2x/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2X.setDefaultValue( 0.0 );
        testRegion.addField( meshd2X );

        ContinuousParameters meshd2Y = new ContinuousParameters( "test_mesh.node.d2y/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2Y.setDefaultValue( 0.0 );
        testRegion.addField( meshd2Y );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2dsDomain, globalNodesDomain );
        meshd2Z.setDefaultValue( 0.0 );
        testRegion.addField( meshd2Z );

        ContinuousDomain bicubicHermiteScalingDomain = library.getContinuousDomain( "library.bicubic_hermite.scaling" );

        final double sq3 = Math.sqrt( 1.0 / 3.0 );

        ContinuousParameters bicubicZHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.z",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicZHermiteQuadScaling.setDefaultValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 0, 0, 0, 0 ) );

        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq3, 1, 1 ), 1, 1 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq3, sq3, 1 ), 1, 2 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 1, 1 ), 1, 3 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, sq3, 1 ), 1, 4 );

        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, sq3, 1 ), 2, 1 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 1, 1 ), 2, 2 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq3, sq3, 1 ), 2, 3 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq3, 1, 1 ), 2, 4 );

        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq3, 1, 1 ), 3, 1 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq3, sq3, 1 ), 3, 2 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 1, 1 ), 3, 3 );
        bicubicZHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, sq3, 1 ), 3, 4 );

        testRegion.addField( bicubicZHermiteQuadScaling );

        final double sq34 = Math.sqrt( 3.0 / 4.0 );

        ContinuousParameters bicubicXHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.x",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );

        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq34, -0.5, 1 ), 1, 1 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq34, 0.0, 1 ), 1, 2 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, -0.5, 1 ), 1, 3 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 0.0, 1 ), 1, 4 );

        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.5, -sq34, 1 ), 2, 1 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.5, -1, 1 ), 2, 2 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.0, -sq34, 1 ), 2, 3 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.0, -1, 1 ), 2, 4 );

        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, -sq34, -0.5, 1 ), 3, 1 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, -sq34, -sq34, 1 ), 3, 2 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, -0.5, -0.5, 1 ), 3, 3 );
        bicubicXHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, -0.5, -sq34, 1 ), 3, 4 );

        testRegion.addField( bicubicXHermiteQuadScaling );

        ContinuousParameters bicubicYHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling.y",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );

        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.5, sq34, 1 ), 1, 1 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.5, 1, 1 ), 1, 2 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0, sq34, 1 ), 1, 3 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0, 1, 1 ), 1, 4 );

        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq34, 0.5, 1 ), 2, 1 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq34, 0, 1 ), 2, 2 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 0.5, 1 ), 2, 3 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 1, 0.0, 1 ), 2, 4 );

        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.5, -sq34, 1 ), 3, 1 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 0.5, -0.5, 1 ), 3, 2 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq34, -sq34, 1 ), 3, 3 );
        bicubicYHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, sq34, -0.5, 1 ), 3, 4 );

        testRegion.addField( bicubicYHermiteQuadScaling );

        ContinuousDomain weightingDomain = library.getContinuousDomain( "library.weighting.1d" );

        ContinuousParameters meshdZds1Map = new ContinuousParameters( "test_mesh.node.ds1.map", weightingDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain, edgeDirectionDomain );
        meshdZds1Map.setDefaultValue( 0.0 );
        meshdZds1Map.setValue( 1.0, 1, 1, 1 );
        meshdZds1Map.setValue( -0.5, 1, 2, 1 );
        meshdZds1Map.setValue( -0.5, 1, 2, 2 );
        meshdZds1Map.setValue( 1.0, 1, 3, 1 );
        meshdZds1Map.setValue( 1.0, 1, 4, 2 );

        meshdZds1Map.setValue( 1.0, 2, 1, 1 );
        meshdZds1Map.setValue( 1.0, 2, 2, 1 );
        meshdZds1Map.setValue( 1.0, 2, 3, 1 );
        meshdZds1Map.setValue( 1.0, 2, 4, 1 );

        meshdZds1Map.setValue( 1.0, 3, 1, 2 );
        meshdZds1Map.setValue( -1.0, 3, 2, 2 );
        meshdZds1Map.setValue( 1.0, 3, 3, 1 );
        meshdZds1Map.setValue( 1.0, 3, 4, 2 );

        testRegion.addField( meshdZds1Map );

        ContinuousParameters meshdZds2Map = new ContinuousParameters( "test_mesh.node.ds2.map", weightingDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain, edgeDirectionDomain );
        meshdZds2Map.setDefaultValue( 0.0 );
        meshdZds2Map.setValue( 1.0, 1, 1, 2 );
        meshdZds2Map.setValue( 1.0, 1, 2, 1 );
        meshdZds2Map.setValue( 1.0, 1, 3, 2 );
        meshdZds2Map.setValue( 1.0, 1, 4, 1 );

        meshdZds2Map.setValue( 1.0, 2, 1, 2 );
        meshdZds2Map.setValue( 1.0, 2, 2, 2 );
        meshdZds2Map.setValue( -1.0, 2, 3, 2 );
        meshdZds2Map.setValue( -1.0, 2, 4, 2 );

        meshdZds2Map.setValue( -1.0, 3, 1, 1 );
        meshdZds2Map.setValue( -0.5, 3, 2, 1 );
        meshdZds2Map.setValue( -0.5, 3, 2, 2 );
        meshdZds2Map.setValue( -1.0, 3, 3, 1 );
        meshdZds2Map.setValue( -1.0, 3, 4, 1 );

        testRegion.addField( meshdZds2Map );

        ContinuousCompositeEvaluator meshdXds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dx/ds1", meshddsDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );
        meshdXds1.importValue( EnsembleDomainValue.makeValue( edgeDirectionDomain, 1 ) );
        meshdXds1.importField( meshdX );

        testRegion.addField( meshdXds1 );

        ContinuousCompositeEvaluator meshdXds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dx/ds2", meshddsDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );
        meshdXds2.importValue( EnsembleDomainValue.makeValue( edgeDirectionDomain, 2 ) );
        meshdXds2.importField( meshdX );

        testRegion.addField( meshdXds2 );

        ContinuousCompositeEvaluator meshdYds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dy/ds1", meshddsDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );
        meshdYds1.importValue( EnsembleDomainValue.makeValue( edgeDirectionDomain, 1 ) );
        meshdYds1.importField( meshdY );

        testRegion.addField( meshdYds1 );

        ContinuousCompositeEvaluator meshdYds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dy/ds2", meshddsDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );
        meshdYds2.importValue( EnsembleDomainValue.makeValue( edgeDirectionDomain, 2 ) );
        meshdYds2.importField( meshdY );

        testRegion.addField( meshdYds2 );

        ContinuousCompositeEvaluator meshdZds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds1", meshddsDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );
        meshdZds1.importMappedField( meshdZ, meshdZds1Map, edgeDirectionDomain );

        testRegion.addField( meshdZds1 );

        ContinuousCompositeEvaluator meshdZds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds2", meshddsDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );
        meshdZds2.importMappedField( meshdZ, meshdZds2Map, edgeDirectionDomain );

        testRegion.addField( meshdZds2 );

        ContinuousDomain bicubicHermiteParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.parameters" );

        ContinuousAggregateEvaluator bicubicXHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_parameters.x",
            bicubicHermiteParametersDomain );
        bicubicXHermiteParameters.setSourceField( 1, meshX );
        bicubicXHermiteParameters.setSourceField( 2, meshdXds1 );
        bicubicXHermiteParameters.setSourceField( 3, meshdXds2 );
        bicubicXHermiteParameters.setSourceField( 4, meshd2X );

        testRegion.addField( bicubicXHermiteParameters );

        ContinuousAggregateEvaluator bicubicYHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_parameters.y",
            bicubicHermiteParametersDomain );
        bicubicYHermiteParameters.setSourceField( 1, meshY );
        bicubicYHermiteParameters.setSourceField( 2, meshdYds1 );
        bicubicYHermiteParameters.setSourceField( 3, meshdYds2 );
        bicubicYHermiteParameters.setSourceField( 4, meshd2Y );

        testRegion.addField( bicubicYHermiteParameters );

        ContinuousAggregateEvaluator bicubicZHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_parameters.z",
            bicubicHermiteParametersDomain );
        bicubicZHermiteParameters.setSourceField( 1, meshZ );
        bicubicZHermiteParameters.setSourceField( 2, meshdZds1 );
        bicubicZHermiteParameters.setSourceField( 3, meshdZds2 );
        bicubicZHermiteParameters.setSourceField( 4, meshd2Z );

        testRegion.addField( bicubicZHermiteParameters );
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

        testRegion.addField( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshDomain );
        meshCoordinatesY.addEvaluator( new BicubicHermiteQuad( "hermite_quad", bicubicYHermiteParameters,
            bicubicYHermiteQuadScaling, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.addEvaluator( new BilinearQuad( "bilinear_quad", meshY, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.setEvaluator( 1, "hermite_quad" );
        meshCoordinatesY.setEvaluator( 2, "hermite_quad" );
        meshCoordinatesY.setEvaluator( 3, "hermite_quad" );

        testRegion.addField( meshCoordinatesY );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshDomain );
        meshCoordinatesZ.addEvaluator( new BicubicHermiteQuad( "hermite_quad", bicubicZHermiteParameters,
            bicubicZHermiteQuadScaling, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesZ.setEvaluator( 1, "hermite_quad" );
        meshCoordinatesZ.setEvaluator( 2, "hermite_quad" );
        meshCoordinatesZ.setEvaluator( 3, "hermite_quad" );

        testRegion.addField( meshCoordinatesZ );

        ContinuousAggregateEvaluator meshCoordinates = new ContinuousAggregateEvaluator( "test_mesh.coordinates", mesh3DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );
        meshCoordinates.setSourceField( 3, meshCoordinatesZ );

        testRegion.addField( meshCoordinates );

        test( testRegion );

        serialize( testRegion );

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, "test_mesh.domain", 3, 64 );
        FileWriter f = new FileWriter( "trunk/data/collada three quads.xml" );
        f.write( collada );
        f.close();

    }
}
