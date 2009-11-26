package fieldml;

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
import fieldml.evaluator.BilinearQuadEvaluator;
import fieldml.evaluator.BilinearSimplexEvaluator;
import fieldml.evaluator.BiquadraticQuadEvaluator;
import fieldml.field.ContinuousAggregateField;
import fieldml.field.ContinuousParameters;
import fieldml.field.EnsembleParameters;
import fieldml.field.Field;
import fieldml.field.PiecewiseField;
import fieldml.field.composite.ContinuousCompositeField;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class HangingNodeTest
{
    private static void serialize( Region region )
    {
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

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
        Field<?, ?> meshX = region.getField( "test_mesh.coordinates.x" );
        Field<?, ?> meshXY = region.getField( "test_mesh.coordinates.xy" );
    }


    public static void main( String[] args )
    {
        Region library = Region.getLibrary();

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

        Region testRegion = new Region( "test" );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 3, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalPointsDomain = new EnsembleDomain( "test_mesh.points" );
        globalPointsDomain.addValues( 1, 2, 3, 4, 5, 6, 7 );
        testRegion.addDomain( globalPointsDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8 );
        testRegion.addDomain( globalNodesDomain );

        ContinuousDomain weighting = library.getContinuousDomain( "library.weighting.1d" );

        ContinuousParameters p2nArithmeticMeanMap = new ContinuousParameters( "test_mesh.p2nAMap", weighting, globalNodesDomain,
            globalPointsDomain );
        p2nArithmeticMeanMap.setDefaultValue( 0 );
        p2nArithmeticMeanMap.setValue( 1.0, 1, 1 );
        p2nArithmeticMeanMap.setValue( 1.0, 2, 2 );
        p2nArithmeticMeanMap.setValue( 1.0, 3, 3 );
        p2nArithmeticMeanMap.setValue( 0.5, 4, 2 );
        p2nArithmeticMeanMap.setValue( 0.5, 4, 6 );
        p2nArithmeticMeanMap.setValue( 1.0, 6, 5 );
        p2nArithmeticMeanMap.setValue( 1.0, 7, 6 );
        p2nArithmeticMeanMap.setValue( 1.0, 8, 7 );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );

        quadNodeList.setValue( 6, 1, 1 );
        quadNodeList.setValue( 7, 1, 2 );
        quadNodeList.setValue( 1, 1, 3 );
        quadNodeList.setValue( 2, 1, 4 );

        quadNodeList.setValue( 4, 2, 1 );
        quadNodeList.setValue( 5, 2, 2 );
        quadNodeList.setValue( 2, 2, 3 );
        quadNodeList.setValue( 3, 2, 4 );

        quadNodeList.setValue( 7, 3, 1 );
        quadNodeList.setValue( 8, 3, 2 );
        quadNodeList.setValue( 4, 3, 3 );
        quadNodeList.setValue( 5, 3, 4 );

        testRegion.addField( quadNodeList );

        ContinuousDomain mesh1DDomain = library.getContinuousDomain( "library.co-ordinates.rc.1d" );
        ContinuousDomain mesh2DDomain = library.getContinuousDomain( "library.co-ordinates.rc.2d" );

        ContinuousParameters meshPointsX = new ContinuousParameters( "test_mesh.point.x", mesh1DDomain, globalPointsDomain );
        meshPointsX.setValue( 00.0, 1 );
        meshPointsX.setValue( 20.0, 2 );
        meshPointsX.setValue( 30.0, 3 );
        meshPointsX.setValue( 30.0, 4 );
        meshPointsX.setValue( 00.0, 5 );
        meshPointsX.setValue( 20.0, 6 );
        meshPointsX.setValue( 30.0, 7 );

        testRegion.addField( meshPointsX );

        ContinuousParameters meshPointsY = new ContinuousParameters( "test_mesh.point.y", mesh1DDomain, globalPointsDomain );
        meshPointsY.setValue( 20.0, 1 );
        meshPointsY.setValue( 20.0, 2 );
        meshPointsY.setValue( 20.0, 3 );
        meshPointsY.setValue( 10.0, 4 );
        meshPointsY.setValue( 00.0, 5 );
        meshPointsY.setValue( 00.0, 6 );
        meshPointsY.setValue( 00.0, 7 );

        testRegion.addField( meshPointsY );
        
        ContinuousCompositeField meshX = new ContinuousCompositeField( "test_mesh.point.x", mesh1DDomain, globalNodesDomain );
        meshX.importMappedField( meshPointsX, p2nArithmeticMeanMap, globalPointsDomain );
        testRegion.addField( meshX );

        ContinuousCompositeField meshY = new ContinuousCompositeField( "test_mesh.point.y", mesh1DDomain, globalNodesDomain );
        meshY.importMappedField( meshPointsY, p2nArithmeticMeanMap, globalPointsDomain );
        testRegion.addField( meshY );

        /*
         * 
         * Because piecewise fields are strictly scalar, there is (probably) no reason to share evaluators. Aggregate fields
         * wishing to share components can do so simply by sharing entire piecewise fields.
         */

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", mesh1DDomain, meshDomain );
        meshCoordinatesX.addEvaluator( new BilinearQuadEvaluator( "bilinear_quad", meshX, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesX.setEvaluator( 1, "bilinear_quad" );
        meshCoordinatesX.setEvaluator( 2, "bilinear_quad" );
        meshCoordinatesX.setEvaluator( 3, "bilinear_quad" );

        testRegion.addField( meshCoordinatesX );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", mesh1DDomain, meshDomain );
        meshCoordinatesY.addEvaluator( new BilinearQuadEvaluator( "bilinear_quad", meshY, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.setEvaluator( 1, "bilinear_quad" );
        meshCoordinatesY.setEvaluator( 2, "bilinear_quad" );
        meshCoordinatesY.setEvaluator( 3, "bilinear_quad" );

        testRegion.addField( meshCoordinatesY );

        ContinuousAggregateField meshCoordinates = new ContinuousAggregateField( "test_mesh.coordinates.xy", mesh2DDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addField( meshCoordinates );

        test( testRegion );

        serialize( testRegion );
    }
}
