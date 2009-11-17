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
import fieldml.evaluator.HermiteQuadEvaluator;
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
        s.append( "1____2\n" );
        s.append( "|    |\n" );
        s.append( "|    |\n" );
        s.append( "|  1 |\n" );
        s.append( "|    |\n" );
        s.append( "3____4\n" );

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
        Field<?, ?> meshZ = region.getField( "test_mesh.coordinates.z" );

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

        EnsembleDomain quadEdgeDirectionDomain = library.getEnsembleDomain( "library.edge_direction.quad" );

        Region testRegion = new Region( "test" );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1 );
        testRegion.addDomain( testMeshElementDomain );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        testRegion.addDomain( meshDomain );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4 );
        testRegion.addDomain( globalNodesDomain );

        EnsembleParameters quadNodeList = new EnsembleParameters( "test_mesh.quad_nodes", globalNodesDomain, testMeshElementDomain,
            quad1x1LocalNodeDomain );

        quadNodeList.setValue( 1, 1, 1 );
        quadNodeList.setValue( 2, 1, 2 );
        quadNodeList.setValue( 3, 1, 3 );
        quadNodeList.setValue( 4, 1, 4 );

        testRegion.addField( quadNodeList );

        ContinuousDomain meshZdomain = new ContinuousDomain( "test_mesh.co-ordinates.z", 1 );
        testRegion.addDomain( meshZdomain );

        ContinuousDomain meshdZdomain = new ContinuousDomain( "test_mesh.co-ordinates.dz/ds", 1 );
        testRegion.addDomain( meshdZdomain );

        ContinuousDomain meshd2Zdomain = new ContinuousDomain( "test_mesh.co-ordinates.d2z/ds1ds2", 1 );
        testRegion.addDomain( meshd2Zdomain );

        ContinuousDomain meshYdomain = new ContinuousDomain( "test_mesh.co-ordinates.z", 1 );
        testRegion.addDomain( meshYdomain );

        ContinuousParameters meshZ = new ContinuousParameters( "test_mesh.node.z", meshZdomain, globalNodesDomain );
        meshZ.setValue( 0.0, 1 );
        meshZ.setValue( 0.0, 2 );
        meshZ.setValue( 0.0, 3 );
        meshZ.setValue( 0.0, 4 );

        testRegion.addField( meshZ );

        ContinuousParameters meshdZ = new ContinuousParameters( "test_mesh.node.dz/ds", meshdZdomain, globalNodesDomain,
            quadEdgeDirectionDomain );
        meshdZ.setValue( -1, 1, 1 );
        meshdZ.setValue( 1, 1, 2 );
        meshdZ.setValue( 1, 2, 1 );
        meshdZ.setValue( 1, 2, 2 );
        meshdZ.setValue( -1, 3, 1 );
        meshdZ.setValue( -1, 3, 2 );
        meshdZ.setValue( 1, 4, 1 );
        meshdZ.setValue( -1, 4, 2 );
        
        testRegion.addField( meshdZ );

        ContinuousParameters meshd2Z = new ContinuousParameters( "test_mesh.node.d2z/ds1ds2", meshd2Zdomain, globalNodesDomain );
        meshd2Z.setValue( 0.0, 1 );
        meshd2Z.setValue( 0.0, 2 );
        meshd2Z.setValue( 0.0, 3 );
        meshd2Z.setValue( 0.0, 4 );

        testRegion.addField( meshd2Z );

        EnsembleParameters meshds1Direction = new EnsembleParameters( "test_mesh.node.direction.ds1", quadEdgeDirectionDomain,
            globalNodesDomain );
        meshds1Direction.setValue( 1, 1 );
        meshds1Direction.setValue( 1, 2 );
        meshds1Direction.setValue( 1, 3 );
        meshds1Direction.setValue( 1, 4 );

        testRegion.addField( meshds1Direction );

        EnsembleParameters meshds2Direction = new EnsembleParameters( "test_mesh.node.direction.ds2", quadEdgeDirectionDomain,
            globalNodesDomain );
        meshds2Direction.setValue( 2, 1 );
        meshds2Direction.setValue( 2, 2 );
        meshds2Direction.setValue( 2, 3 );
        meshds2Direction.setValue( 2, 4 );

        testRegion.addField( meshds2Direction );

        ContinuousCompositeField meshdZds1 = new ContinuousCompositeField( "test_mesh.node.dz/ds1", meshdZdomain, globalNodesDomain );
        meshdZds1.importField( meshds1Direction );
        meshdZds1.importField( meshdZ );

        testRegion.addField( meshdZds1 );

        ContinuousCompositeField meshdZds2 = new ContinuousCompositeField( "test_mesh.node.dz/ds2", meshdZdomain, globalNodesDomain );
        meshdZds2.importField( meshds2Direction );
        meshdZds2.importField( meshdZ );

        testRegion.addField( meshdZds2 );

        /*
         * 
         * Because piecewise fields are strictly scalar, there is (probably) no reason to share evaluators. Aggregate fields
         * wishing to share components can do so simply by sharing entire piecewise fields.
         */

        PiecewiseField meshCoordinatesZ = new PiecewiseField( "test_mesh.coordinates.z", meshZdomain, meshDomain );
        meshCoordinatesZ.addEvaluator( new HermiteQuadEvaluator( "hermite_quad", meshZ, meshdZds1, meshdZds2, meshd2Z, quadNodeList,
            quad1x1LocalNodeDomain ) );
        meshCoordinatesZ.setEvaluator( 1, "hermite_quad" );

        testRegion.addField( meshCoordinatesZ );

//        test( testRegion );

        serialize( testRegion );
    }
}
