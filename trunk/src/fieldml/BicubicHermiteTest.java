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
import fieldml.domain.EnsembleListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousAggregateEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.evaluator.ContinuousParameters;
import fieldml.evaluator.EnsembleListParameters;
import fieldml.evaluator.EnsembleParameters;
import fieldml.evaluator.composite.ContinuousCompositeEvaluator;
import fieldml.evaluator.hardcoded.BicubicHermite;
import fieldml.evaluator.hardcoded.BilinearLagrange;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.JdomReflectiveHandler;
import fieldml.map.IndirectMap;
import fieldml.region.Region;
import fieldmlx.util.MinimalColladaExporter;

public class BicubicHermiteTest
    extends TestCase
{
    public static String REGION_NAME = "BicubicHermite_Test";


    public void testSerialization()
    {
        Region region = buildRegion();

        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1____2____5\n" );
        s.append( "|    |    |\n" );
        s.append( "|    |    |\n" );
        s.append( "|  1 |  2 |\n" );
        s.append( "|    |    |\n" );
        s.append( "3____4____6\n" );

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
        // TODO STUB BicubicHermiteTest.testEvaluation
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
        testMeshElementDomain.addValues( 1, 2 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6 );
        testRegion.addDomain( globalNodesDomain );

        EnsembleListDomain globalNodesListDomain = new EnsembleListDomain( "test_mesh.node_list", globalNodesDomain );
        testRegion.addDomain( globalNodesListDomain );

        EnsembleListParameters quadNodeList = new EnsembleListParameters( "test_mesh.quad_nodes", globalNodesListDomain,
            testMeshElementDomain );
        quadNodeList.setValue( 1, 1, 2, 3, 4 );
        quadNodeList.setValue( 2, 2, 5, 4, 6 );
        testRegion.addEvaluator( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh3DDomain = library.getContinuousDomain( "library.co-ordinates.rc.3d" );

        ContinuousDomain meshdZdomain = new ContinuousDomain( "test_mesh.co-ordinates.dz/ds", 1 );
        testRegion.addDomain( meshdZdomain );

        ContinuousDomain meshd2Zdomain = new ContinuousDomain( "test_mesh.co-ordinates.d2z/ds1ds2", 1 );
        testRegion.addDomain( meshd2Zdomain );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", mesh1DDomain, globalNodesDomain );
        meshX.setValue( 0.0, 1 );
        meshX.setValue( 1.0, 2 );
        meshX.setValue( 0.0, 3 );
        meshX.setValue( 1.0, 4 );
        meshX.setValue( 3.0, 5 );
        meshX.setValue( 3.0, 6 );
        testRegion.addEvaluator( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", mesh1DDomain, globalNodesDomain );
        meshY.setValue( 0.0, 1 );
        meshY.setValue( 0.0, 2 );
        meshY.setValue( 1.0, 3 );
        meshY.setValue( 1.0, 4 );
        meshY.setValue( 0.0, 5 );
        meshY.setValue( 1.0, 6 );
        testRegion.addEvaluator( meshY );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", mesh1DDomain, globalNodesDomain );
        meshZ.setValue( 0.0, 1 );
        meshZ.setValue( 0.0, 2 );
        meshZ.setValue( 0.0, 3 );
        meshZ.setValue( 0.0, 4 );
        meshZ.setValue( 0.0, 5 );
        meshZ.setValue( 0.0, 6 );
        testRegion.addEvaluator( meshZ );

        ContinuousParameters meshdZ = new ContinuousParameters( "test_mesh.node.dz/ds", meshdZdomain, globalNodesDomain,
            edgeDirectionDomain );
        meshdZ.setValue( -1.0, 1, 1 );
        meshdZ.setValue( 1.0, 1, 2 );
        meshdZ.setValue( 1.0, 2, 1 );
        meshdZ.setValue( 1.0, 2, 2 );
        meshdZ.setValue( -1.0, 3, 1 );
        meshdZ.setValue( -1.0, 3, 2 );
        meshdZ.setValue( 1.0, 4, 1 );
        meshdZ.setValue( -1.0, 4, 2 );
        meshdZ.setValue( -1.0, 5, 1 );
        meshdZ.setValue( 1.0, 5, 2 );
        meshdZ.setValue( -1.0, 6, 1 );
        meshdZ.setValue( -1.0, 6, 2 );
        testRegion.addEvaluator( meshdZ );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2Zdomain, globalNodesDomain );
        meshd2Z.setValue( 0.0, 1 );
        meshd2Z.setValue( 0.0, 2 );
        meshd2Z.setValue( 0.0, 3 );
        meshd2Z.setValue( 0.0, 4 );
        meshd2Z.setValue( 0.0, 5 );
        meshd2Z.setValue( 0.0, 6 );
        testRegion.addEvaluator( meshd2Z );

        ContinuousDomain bicubicHermiteScalingDomain = library.getContinuousDomain( "library.bicubic_hermite.scaling" );

        ContinuousParameters bicubicHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicHermiteQuadScaling.setDefaultValue( 1, 1, 1, 1 );
        bicubicHermiteQuadScaling.setValue( bicubicHermiteScalingDomain.makeValue( 1, 2, 1, 2 ), 2, 1 );
        bicubicHermiteQuadScaling.setValue( bicubicHermiteScalingDomain.makeValue( 1, 2, 1, 2 ), 2, 2 );
        bicubicHermiteQuadScaling.setValue( bicubicHermiteScalingDomain.makeValue( 1, 2, 1, 2 ), 2, 3 );
        bicubicHermiteQuadScaling.setValue( bicubicHermiteScalingDomain.makeValue( 1, 2, 1, 2 ), 2, 4 );
        testRegion.addEvaluator( bicubicHermiteQuadScaling );

        EnsembleParameters meshds1Direction = new EnsembleParameters( "test_mesh.node.direction.ds1", edgeDirectionDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshds1Direction.setDefaultValue( 1 );
        testRegion.addEvaluator( meshds1Direction );

        EnsembleParameters meshds2Direction = new EnsembleParameters( "test_mesh.node.direction.ds2", edgeDirectionDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshds2Direction.setDefaultValue( 2 );
        testRegion.addEvaluator( meshds2Direction );

        ContinuousCompositeEvaluator meshdZds1 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds1", meshdZdomain );
        meshdZds1.importField( meshds1Direction );
        meshdZds1.importField( meshdZ );
        testRegion.addEvaluator( meshdZds1 );

        ContinuousCompositeEvaluator meshdZds2 = new ContinuousCompositeEvaluator( "test_mesh.node.dz/ds2", meshdZdomain );
        meshdZds2.importField( meshds2Direction );
        meshdZds2.importField( meshdZ );
        testRegion.addEvaluator( meshdZds2 );

        ContinuousDomain bicubicHermiteNodalParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.nodal.parameters" );

        ContinuousAggregateEvaluator bicubicZHermiteParameters = new ContinuousAggregateEvaluator( "test_mesh.bicubic_parameters.z",
            bicubicHermiteNodalParametersDomain );
        bicubicZHermiteParameters.setSourceField( 1, meshZ );
        bicubicZHermiteParameters.setSourceField( 2, meshdZds1 );
        bicubicZHermiteParameters.setSourceField( 3, meshdZds2 );
        bicubicZHermiteParameters.setSourceField( 4, meshd2Z );
        testRegion.addEvaluator( bicubicZHermiteParameters );

        ContinuousListEvaluator meshBilinearLagrange = new BilinearLagrange( "test_mesh.mesh.bilinear_lagrange", meshDomain );
        testRegion.addEvaluator( meshBilinearLagrange );

        IndirectMap elementBilinearMap = new IndirectMap( "test_mesh.element.bilinear_lagrange", quadNodeList, meshBilinearLagrange );
        testRegion.addMap( elementBilinearMap );

        PiecewiseTemplate meshCoordinatesL2 = new PiecewiseTemplate( "test_mesh.coordinates.L2", meshDomain, 1 );
        meshCoordinatesL2.setMap( 1, elementBilinearMap, 1 );
        meshCoordinatesL2.setMap( 2, elementBilinearMap, 1 );
        testRegion.addPiecewiseTemplate( meshCoordinatesL2 );

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshCoordinatesL2 );
        meshCoordinatesX.setDofs( 1, meshX );
        testRegion.addEvaluator( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshCoordinatesL2 );
        meshCoordinatesY.setDofs( 1, meshY );
        testRegion.addEvaluator( meshCoordinatesY );

        ContinuousListEvaluator meshBicubicHermite = new BicubicHermite( "test_mesh.mesh.bicubic_hermite", meshDomain );
        testRegion.addEvaluator( meshBicubicHermite );

        IndirectMap elementBicubicHermiteMap = new IndirectMap( "test_mesh.element.bicubic_hermite", quadNodeList, meshBicubicHermite );
        testRegion.addMap( elementBicubicHermiteMap );

        PiecewiseTemplate meshCoordinatesH3 = new PiecewiseTemplate( "test_mesh.coordinates.H3", meshDomain, 1 );
        meshCoordinatesH3.setMap( 1, elementBicubicHermiteMap, 1 );
        meshCoordinatesH3.setMap( 2, elementBicubicHermiteMap, 1 );
        testRegion.addPiecewiseTemplate( meshCoordinatesH3 );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", mesh1DDomain, meshCoordinatesH3 );
        meshCoordinatesZ.setDofs( 1, bicubicZHermiteParameters );
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

        String collada = MinimalColladaExporter.exportFromFieldML( testRegion, 16, "test_mesh.domain", "test_mesh.coordinates" );
        FileWriter f = new FileWriter( "trunk/data/collada two quads.xml" );
        f.write( collada );
        f.close();
    }
}
