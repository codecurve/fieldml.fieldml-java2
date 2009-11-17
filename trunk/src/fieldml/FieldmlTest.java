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
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class FieldmlTest
{
    private static void serialize( Region region )
    {
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1____2____3_11_7\n" );
        s.append( "|    |   /|    |\n" );
        s.append( "|    |*2/ | *4 |\n" );
        s.append( "| *1 | /  8  9 10\n" );
        s.append( "|    |/*3 |    |\n" );
        s.append( "4____5____6_12_13\n" );

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

        ContinuousDomainValue output;

        // Test element 1
        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 1, 0.0, 0.0 );
        assert output.values[0] == 0;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 1, 0.0, 1.0 );
        assert output.values[0] == 0;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 1, 0.5, 0.0 );
        assert output.values[0] == 5;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 1, 1.0, 0.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 1, 1.0, 1.0 );
        assert output.values[0] == 10;

        // Test element 2
        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 2, 0.0, 0.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 2, 1.0, 0.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 2, 0.0, 1.0 );
        assert output.values[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 2, 0.5, 0.5 );
        assert output.values[0] == 15;

        // Test element 3
        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 3, 0.0, 0.0 );
        assert output.values[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 3, 1.0, 0.0 );
        assert output.values[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 3, 0.0, 1.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain, 3, 0.5, 0.5 );
        assert output.values[0] == 15;

        output = (ContinuousDomainValue)meshXY.evaluate( meshDomain, 3, 0.5, 0.5 );
        assert output.values[0] == 15;
        assert output.values[1] == 5;

        output = (ContinuousDomainValue)meshXY.evaluate( meshDomain, 4, 0.5, 0.5 );
        assert output.values[0] == 25;
        assert output.values[1] == 5;
    }


    public static void main( String[] args )
    {
        Region library = Region.getLibrary();

        EnsembleDomain triangle1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.triangle.1x1" );

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

        EnsembleDomain quad2x2LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.2x2" );

        Region testRegion = new Region( "test" );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3, 4 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.triangle.00_10_01" );
        meshDomain.setShape( 3, "library.shape.triangle.00_10_01" );
        meshDomain.setShape( 4, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 );
        testRegion.addDomain( globalNodesDomain );

        EnsembleParameters triangleNodeList = new EnsembleParameters( "test_mesh.triangle_nodes", globalNodesDomain, testMeshElementDomain,
            triangle1x1LocalNodeDomain );

        triangleNodeList.setValue( 2, 2, 1 );
        triangleNodeList.setValue( 5, 2, 2 );
        triangleNodeList.setValue( 3, 2, 3 );

        triangleNodeList.setValue( 6, 3, 1 );
        triangleNodeList.setValue( 3, 3, 2 );
        triangleNodeList.setValue( 5, 3, 3 );

        testRegion.addField( triangleNodeList );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );

        quadNodeList.setValue( 4, 1, 1 );
        quadNodeList.setValue( 5, 1, 2 );
        quadNodeList.setValue( 1, 1, 3 );
        quadNodeList.setValue( 2, 1, 4 );

        quadNodeList.setValue( 6, 4, 1 );
        quadNodeList.setValue( 13, 4, 2 );
        quadNodeList.setValue( 3, 4, 3 );
        quadNodeList.setValue( 7, 4, 4 );

        testRegion.addField( quadNodeList );

        EnsembleParameters biquadNodeList = new EnsembleParameters( "test_mesh.biquad_nodes", globalNodesDomain, testMeshElementDomain,
            quad2x2LocalNodeDomain );

        biquadNodeList.setValue( 6, 4, 1 );
        biquadNodeList.setValue( 12, 4, 2 );
        biquadNodeList.setValue( 13, 4, 3 );
        biquadNodeList.setValue( 8, 4, 4 );
        biquadNodeList.setValue( 9, 4, 5 );
        biquadNodeList.setValue( 10, 4, 6 );
        biquadNodeList.setValue( 3, 4, 7 );
        biquadNodeList.setValue( 11, 4, 8 );
        biquadNodeList.setValue( 7, 4, 9 );

        testRegion.addField( biquadNodeList );

        ContinuousDomain meshXdomain = new ContinuousDomain( "test_mesh.co-ordinates.x", 1 );
        testRegion.addDomain( meshXdomain );

        ContinuousDomain meshYdomain = new ContinuousDomain( "test_mesh.co-ordinates.y", 1 );
        testRegion.addDomain( meshYdomain );

        ContinuousDomain meshXYdomain = new ContinuousDomain( "test_mesh.co-ordinates.xy", 2 );
        testRegion.addDomain( meshXYdomain );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", meshXdomain, globalNodesDomain );
        meshX.setValue( 00.0, 1 );
        meshX.setValue( 10.0, 2 );
        meshX.setValue( 20.0, 3 );
        meshX.setValue( 00.0, 4 );
        meshX.setValue( 10.0, 5 );
        meshX.setValue( 20.0, 6 );
        meshX.setValue( 30.0, 7 );
        meshX.setValue( 30.0, 13 );

        testRegion.addField( meshX );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", meshYdomain, globalNodesDomain );
        meshY.setValue( 10.0, 1 );
        meshY.setValue( 10.0, 2 );
        meshY.setValue( 10.0, 3 );
        meshY.setValue( 00.0, 4 );
        meshY.setValue( 00.0, 5 );
        meshY.setValue( 00.0, 6 );
        meshY.setValue( 10.0, 7 );
        meshY.setValue( 05.0, 8 );
        meshY.setValue( 05.0, 9 );
        meshY.setValue( 05.0, 10 );
        meshY.setValue( 10.0, 11 );
        meshY.setValue( 00.0, 12 );
        meshY.setValue( 00.0, 13 );

        testRegion.addField( meshY );
        
        /*
         * 
         * Because piecewise fields are strictly scalar, there is (probably) no reason to share evaluators. Aggregate fields
         * wishing to share components can do so simply by sharing entire piecewise fields.
         */

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", meshXdomain, meshDomain );

        meshCoordinatesX.addEvaluator( new BilinearQuadEvaluator( "bilinear_quad", meshX, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesX.addEvaluator( new BilinearSimplexEvaluator( "bilinear_simplex", meshX, triangleNodeList,
            triangle1x1LocalNodeDomain ) );

        testRegion.addField( meshCoordinatesX );

        meshCoordinatesX.setEvaluator( 1, "bilinear_quad" );
        meshCoordinatesX.setEvaluator( 2, "bilinear_simplex" );
        meshCoordinatesX.setEvaluator( 3, "bilinear_simplex" );
        meshCoordinatesX.setEvaluator( 4, "bilinear_quad" );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", meshYdomain, meshDomain );

        meshCoordinatesY.addEvaluator( new BilinearQuadEvaluator( "bilinear_quad", meshY, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.addEvaluator( new BilinearSimplexEvaluator( "bilinear_simplex", meshY, triangleNodeList,
            triangle1x1LocalNodeDomain ) );
        meshCoordinatesY.addEvaluator( new BiquadraticQuadEvaluator( "biquadratic_quad", meshY, biquadNodeList, quad2x2LocalNodeDomain ) );

        meshCoordinatesY.setEvaluator( 1, "bilinear_quad" );
        meshCoordinatesY.setEvaluator( 2, "bilinear_simplex" );
        meshCoordinatesY.setEvaluator( 3, "bilinear_simplex" );
        meshCoordinatesY.setEvaluator( 4, "biquadratic_quad" );

        testRegion.addField( meshCoordinatesY );

        ContinuousAggregateField meshCoordinates = new ContinuousAggregateField( "test_mesh.coordinates.xy", meshXYdomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        testRegion.addField( meshCoordinates );

        test( testRegion );

        serialize( testRegion );
    }
}
