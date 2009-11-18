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
import fieldml.evaluator.HermiteQuadEvaluator;
import fieldml.field.ContinuousAggregateField;
import fieldml.field.ContinuousParameters;
import fieldml.field.EnsembleParameters;
import fieldml.field.Field;
import fieldml.field.PiecewiseField;
import fieldml.field.composite.ContinuousCompositeField;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class BicubicHermiteTest
{
    private static void serialize( Region region )
    {
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


    private static void visualize( Region region )
    {
        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        Field<?, ContinuousDomainValue> mesh = (Field<?, ContinuousDomainValue>)region.getField( "test_mesh.coordinates" );

        System.out.println( "0 BFC CERTIFY CW" );
        ContinuousDomainValue v;
        for( int i = 0; i < 16; i++ )
        {
            for( int j = 0; j < 16; j++ )
            {
                System.out.print( "4 4" );

                v = mesh.evaluate( meshDomain, 1, ( i + 0 ) / 16.0, ( j + 0 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );
                v = mesh.evaluate( meshDomain, 1, ( i + 1 ) / 16.0, ( j + 0 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );
                v = mesh.evaluate( meshDomain, 1, ( i + 1 ) / 16.0, ( j + 1 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );
                v = mesh.evaluate( meshDomain, 1, ( i + 0 ) / 16.0, ( j + 1 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );

                System.out.println( "" );

                System.out.print( "4 14" );

                v = mesh.evaluate( meshDomain, 2, ( i + 0 ) / 16.0, ( j + 0 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );
                v = mesh.evaluate( meshDomain, 2, ( i + 1 ) / 16.0, ( j + 0 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );
                v = mesh.evaluate( meshDomain, 2, ( i + 1 ) / 16.0, ( j + 1 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );
                v = mesh.evaluate( meshDomain, 2, ( i + 0 ) / 16.0, ( j + 1 ) / 16.0 );
                System.out.print( " " + v.values[0] + " " + v.values[1] + " " + v.values[2] );

                System.out.println( "" );
            }
        }
    }


    private static void test( Region region )
    {
        MeshDomain meshDomain = region.getMeshDomain( "test_mesh.domain" );
        Field<?, ?> meshZ = region.getField( "test_mesh.coordinates" );

        ContinuousDomainValue output;

        // Test element 1
        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 1, 0.0, 0.0 );
        assert output.values[0] == 0;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 1, 0.0, 1.0 );
        assert output.values[0] == 0;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 1, 0.5, 0.0 );
        assert output.values[0] == 5;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 1, 1.0, 0.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 1, 1.0, 1.0 );
        assert output.values[0] == 10;

        // Test element 2
        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 2, 0.0, 0.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 2, 1.0, 0.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 2, 0.0, 1.0 );
        assert output.values[0] == 20;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 2, 0.5, 0.5 );
        assert output.values[0] == 15;

        // Test element 3
        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 3, 0.0, 0.0 );
        assert output.values[0] == 20;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 3, 1.0, 0.0 );
        assert output.values[0] == 20;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 3, 0.0, 1.0 );
        assert output.values[0] == 10;

        output = (ContinuousDomainValue)meshZ.evaluate( meshDomain, 3, 0.5, 0.5 );
        assert output.values[0] == 15;
    }


    public static void main( String[] args )
    {
        Region library = Region.getLibrary();

        EnsembleDomain quad1x1LocalNodeDomain = library.getEnsembleDomain( "library.local_nodes.quad.1x1" );

        EnsembleDomain edgeDirectionDomain = new EnsembleDomain( "test_mesh.edge_direction" );
        edgeDirectionDomain.addValues( 1, 2 );

        Region testRegion = new Region( "test" );

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

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );

        quadNodeList.setValue( 1, 1, 1 );
        quadNodeList.setValue( 2, 1, 2 );
        quadNodeList.setValue( 3, 1, 3 );
        quadNodeList.setValue( 4, 1, 4 );
        quadNodeList.setValue( 2, 2, 1 );
        quadNodeList.setValue( 5, 2, 2 );
        quadNodeList.setValue( 4, 2, 3 );
        quadNodeList.setValue( 6, 2, 4 );

        testRegion.addField( quadNodeList );

        ContinuousDomain meshXdomain = library.getContinuousDomain( "library.co-ordinates.rc.x" );
        ContinuousDomain meshYdomain = library.getContinuousDomain( "library.co-ordinates.rc.y" );
        ContinuousDomain meshZdomain = library.getContinuousDomain( "library.co-ordinates.rc.z" );
        ContinuousDomain meshXYZDomain = library.getContinuousDomain( "library.co-ordinates.rc.xyz" );

        ContinuousDomain meshdZdomain = new ContinuousDomain( "test_mesh.co-ordinates.dz/ds", 1 );
        testRegion.addDomain( meshdZdomain );

        ContinuousDomain meshd2Zdomain = new ContinuousDomain( "test_mesh.co-ordinates.d2z/ds1ds2", 1 );
        testRegion.addDomain( meshd2Zdomain );

        ContinuousParameters meshX = new ContinuousParameters( "test_mesh.node.x", meshXdomain, globalNodesDomain );
        meshX.setValue( 0.0, 1 );
        meshX.setValue( 1.0, 2 );
        meshX.setValue( 0.0, 3 );
        meshX.setValue( 1.0, 4 );
        meshX.setValue( 3.0, 5 );
        meshX.setValue( 3.0, 6 );

        ContinuousParameters meshY = new ContinuousParameters( "test_mesh.node.y", meshYdomain, globalNodesDomain );
        meshY.setValue( 0.0, 1 );
        meshY.setValue( 0.0, 2 );
        meshY.setValue( 1.0, 3 );
        meshY.setValue( 1.0, 4 );
        meshY.setValue( 0.0, 5 );
        meshY.setValue( 1.0, 6 );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", meshZdomain, globalNodesDomain );
        meshZ.setValue( 0.0, 1 );
        meshZ.setValue( 0.0, 2 );
        meshZ.setValue( 0.0, 3 );
        meshZ.setValue( 0.0, 4 );
        meshZ.setValue( 0.0, 5 );
        meshZ.setValue( 0.0, 6 );

        testRegion.addField( meshZ );

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

        testRegion.addField( meshdZ );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2Zdomain, globalNodesDomain );
        meshd2Z.setValue( 0.0, 1 );
        meshd2Z.setValue( 0.0, 2 );
        meshd2Z.setValue( 0.0, 3 );
        meshd2Z.setValue( 0.0, 4 );
        meshd2Z.setValue( 0.0, 5 );
        meshd2Z.setValue( 0.0, 6 );

        ContinuousDomain bicubicHermiteScalingDomain = library.getContinuousDomain( "library.bicubic_hermite.scaling" );

        ContinuousParameters bicubicHermiteQuadScaling = new ContinuousParameters( "test_mesh.cubic_hermite_scaling",
            bicubicHermiteScalingDomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        bicubicHermiteQuadScaling.setDefaultValue( 1, 1, 1, 1 );
        bicubicHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 2, 1, 2 ), 2, 1 );
        bicubicHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 2, 1, 2 ), 2, 2 );
        bicubicHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 2, 1, 2 ), 2, 3 );
        bicubicHermiteQuadScaling.setValue( ContinuousDomainValue.makeValue( bicubicHermiteScalingDomain, 1, 2, 1, 2 ), 2, 4 );

        testRegion.addField( meshd2Z );

        EnsembleParameters meshds1Direction = new EnsembleParameters( "test_mesh.node.direction.ds1", edgeDirectionDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshds1Direction.setDefaultValue( 1 );

        testRegion.addField( meshds1Direction );

        EnsembleParameters meshds2Direction = new EnsembleParameters( "test_mesh.node.direction.ds2", edgeDirectionDomain,
            testMeshElementDomain, quad1x1LocalNodeDomain );
        meshds2Direction.setDefaultValue( 2 );

        testRegion.addField( meshds2Direction );

        ContinuousCompositeField meshdZds1 = new ContinuousCompositeField( "test_mesh.node.dz/ds1", meshdZdomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdZds1.importField( meshds1Direction );
        meshdZds1.importField( meshdZ );

        testRegion.addField( meshdZds1 );

        ContinuousCompositeField meshdZds2 = new ContinuousCompositeField( "test_mesh.node.dz/ds2", meshdZdomain, testMeshElementDomain, quad1x1LocalNodeDomain );
        meshdZds2.importField( meshds2Direction );
        meshdZds2.importField( meshdZ );

        testRegion.addField( meshdZds2 );

        ContinuousDomain bicubicHermiteParametersDomain = library.getContinuousDomain( "library.bicubic_hermite.parameters" );

        ContinuousAggregateField bicubicZHermiteParameters = new ContinuousAggregateField( "test_mesh.bicubic_parameters.z",
            bicubicHermiteParametersDomain );
        bicubicZHermiteParameters.setSourceField( 1, meshZ );
        bicubicZHermiteParameters.setSourceField( 2, meshdZds1 );
        bicubicZHermiteParameters.setSourceField( 3, meshdZds2 );
        bicubicZHermiteParameters.setSourceField( 4, meshd2Z );

        /*
         * 
         * Because piecewise fields are strictly scalar, there is (probably) no reason to share evaluators. Aggregate fields
         * wishing to share components can do so simply by sharing entire piecewise fields.
         */

        PiecewiseField meshCoordinatesX = new PiecewiseField( "test_mesh.coordinates.x", meshXdomain, meshDomain );
        meshCoordinatesX.addEvaluator( new BilinearQuadEvaluator( "linear_quad", meshX, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesX.setEvaluator( 1, "linear_quad" );
        meshCoordinatesX.setEvaluator( 2, "linear_quad" );

        PiecewiseField meshCoordinatesY = new PiecewiseField( "test_mesh.coordinates.y", meshYdomain, meshDomain );
        meshCoordinatesY.addEvaluator( new BilinearQuadEvaluator( "linear_quad", meshY, quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesY.setEvaluator( 1, "linear_quad" );
        meshCoordinatesY.setEvaluator( 2, "linear_quad" );

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", meshZdomain, meshDomain );
        meshCoordinatesZ.addEvaluator( new HermiteQuadEvaluator( "hermite_quad", bicubicZHermiteParameters, bicubicHermiteQuadScaling,
            quadNodeList, quad1x1LocalNodeDomain ) );
        meshCoordinatesZ.setEvaluator( 1, "hermite_quad" );
        meshCoordinatesZ.setEvaluator( 2, "hermite_quad" );

        ContinuousAggregateField meshCoordinates = new ContinuousAggregateField( "test_mesh.coordinates", meshXYZDomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );
        meshCoordinates.setSourceField( 3, meshCoordinatesZ );

        testRegion.addField( meshCoordinates );

        visualize( testRegion );

//        test( testRegion );

//        serialize( testRegion );
    }
}
