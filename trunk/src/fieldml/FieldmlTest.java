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
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.Evaluator;
import fieldml.evaluator.NodeDofEvaluator;
import fieldml.field.ContinuousAggregateField;
import fieldml.field.ContinuousMappingField;
import fieldml.field.EnsembleMappingField;
import fieldml.field.FEMField;
import fieldml.field.Field;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.MeshDomainValue;

public class FieldmlTest
{
    private static void serialize()
    {
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        StringBuilder s = new StringBuilder();
        s.append( "\n" );
        s.append( "1___2___3\n" );
        s.append( "|   |2 /|\n" );
        s.append( "| 1 | / |\n" );
        s.append( "|   |/ 3|\n" );
        s.append( "4___5___6\n" );

        Comment comment1 = new Comment( s.toString() );
        root.addContent( comment1 );

        JdomReflectiveHandler handler = new JdomReflectiveHandler( doc.getRootElement() );
        for( ContinuousDomain domain : ContinuousDomain.domains.values() )
        {
            ReflectiveWalker.Walk( domain, handler );
        }
        for( EnsembleDomain domain : EnsembleDomain.domains.values() )
        {
            ReflectiveWalker.Walk( domain, handler );
        }
        for( MeshDomain domain : MeshDomain.domains.values() )
        {
            ReflectiveWalker.Walk( domain, handler );
        }

        for( Field<?, ?> field : Field.fields.values() )
        {
            ReflectiveWalker.Walk( field, handler );
        }

        for( Evaluator evaluator : ContinuousEvaluator.evaluators.values() )
        {
            ReflectiveWalker.Walk( evaluator, handler );
        }
        for( Evaluator evaluator : EnsembleEvaluator.evaluators.values() )
        {
            ReflectiveWalker.Walk( evaluator, handler );
        }

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


    private static void test()
    {
        MeshDomain meshDomain = MeshDomain.domains.get( "test_mesh.domain" );
        Field<?, ?> meshX = Field.fields.get( "test_mesh.coordinates.x" );
        Field<?, ?> meshXY = Field.fields.get( "test_mesh.coordinates.xy" );

        ContinuousDomainValue output;

        // Test element 1
        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 1, 0.0, 0.0 ) );
        assert output.chartValues[0] == 0;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 1, 0.0, 1.0 ) );
        assert output.chartValues[0] == 0;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 1, 0.5, 0.0 ) );
        assert output.chartValues[0] == 5;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 1, 1.0, 0.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 1, 1.0, 1.0 ) );
        assert output.chartValues[0] == 10;

        // Test element 2
        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 2, 0.0, 0.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 2, 1.0, 0.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 2, 0.0, 1.0 ) );
        assert output.chartValues[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 2, 0.5, 0.5 ) );
        assert output.chartValues[0] == 15;

        // Test element 3
        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 3, 0.0, 0.0 ) );
        assert output.chartValues[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 3, 1.0, 0.0 ) );
        assert output.chartValues[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 3, 0.0, 1.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( MeshDomainValue.makeValue( meshDomain, 3, 0.5, 0.5 ) );
        assert output.chartValues[0] == 15;

        output = (ContinuousDomainValue)meshXY.evaluate( MeshDomainValue.makeValue( meshDomain, 3, 0.5, 0.5 ) );
        assert output.chartValues[0] == 15;
        assert output.chartValues[1] == 5;
    }


    public static void main( String[] args )
    {
        EnsembleDomain triangleNodeDomain = new EnsembleDomain( "library.local_nodes.simplex.bilinear" );
        triangleNodeDomain.addValues( 1, 2, 3 );

        EnsembleDomain quadNodeDomain = new EnsembleDomain( "library.local_nodes.quad.bilinear" );
        quadNodeDomain.addValues( 1, 2, 3, 4 );

        EnsembleDomain biquadNodeDomain = new EnsembleDomain( "library.local_nodes.quad.biquadratic" );
        biquadNodeDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8, 9 );

        EnsembleDomain testMeshElementDomain = new EnsembleDomain( "test_mesh.elements" );
        testMeshElementDomain.addValues( 1, 2, 3 );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2, testMeshElementDomain );
        meshDomain.setShape( 1, "library.shape.quad.00_10_01_11" );
        meshDomain.setShape( 2, "library.shape.triangle.00_10_01" );
        meshDomain.setShape( 3, "library.shape.triangle.00_10_01" );

        EnsembleDomain globalNodesDomain = new EnsembleDomain( "test_mesh.nodes" );
        globalNodesDomain.addValues( 1, 2, 3, 4, 5, 6 );

        EnsembleMappingField triangleNodeList = new EnsembleMappingField( "test_mesh.triangle_nodes", globalNodesDomain,
            testMeshElementDomain, triangleNodeDomain );

        triangleNodeList.setValue( 2, 2, 1 );
        triangleNodeList.setValue( 5, 2, 2 );
        triangleNodeList.setValue( 3, 2, 3 );

        triangleNodeList.setValue( 6, 3, 1 );
        triangleNodeList.setValue( 3, 3, 2 );
        triangleNodeList.setValue( 5, 3, 3 );

        EnsembleMappingField quadNodeList = new EnsembleMappingField( "test_mesh.quad_nodes", globalNodesDomain,
            testMeshElementDomain, quadNodeDomain );

        quadNodeList.setValue( 4, 1, 1 );
        quadNodeList.setValue( 5, 1, 2 );
        quadNodeList.setValue( 1, 1, 3 );
        quadNodeList.setValue( 2, 1, 4 );

        ContinuousDomain meshXdomain = new ContinuousDomain( "test_mesh.co-ordinates.x", 1 );
        ContinuousDomain meshYdomain = new ContinuousDomain( "test_mesh.co-ordinates.y", 1 );

        ContinuousDomain meshXYdomain = new ContinuousDomain( "test_mesh.co-ordinates.xy", 2 );

        ContinuousMappingField meshX = new ContinuousMappingField( "test_mesh.node.x", meshXdomain, globalNodesDomain );
        meshX.setValue( 00.0, 1 );
        meshX.setValue( 10.0, 2 );
        meshX.setValue( 20.0, 3 );
        meshX.setValue( 00.0, 4 );
        meshX.setValue( 10.0, 5 );
        meshX.setValue( 20.0, 6 );

        ContinuousMappingField meshY = new ContinuousMappingField( "test_mesh.node.y", meshYdomain, globalNodesDomain );
        meshY.setValue( 10.0, 1 );
        meshY.setValue( 10.0, 2 );
        meshY.setValue( 10.0, 3 );
        meshY.setValue( 00.0, 4 );
        meshY.setValue( 00.0, 5 );
        meshY.setValue( 00.0, 6 );

        NodeDofEvaluator meshXQuadBilinear = new NodeDofEvaluator( "test_mesh.evaluator.x.quad_bilinear", meshX, quadNodeList,
            "library::quad_bilinear" );
        NodeDofEvaluator meshXSimplexBilinear = new NodeDofEvaluator( "test_mesh.evaluator.x.simplex_bilinear", meshX,
            triangleNodeList, "library::triangle_bilinear" );

        FEMField meshCoordinatesX = new FEMField( "test_mesh.coordinates.x", meshXdomain, meshDomain );
        meshCoordinatesX.setEvaluator( 1, meshXQuadBilinear );
        meshCoordinatesX.setEvaluator( 2, meshXSimplexBilinear );
        meshCoordinatesX.setEvaluator( 3, meshXSimplexBilinear );

        NodeDofEvaluator meshYQuadBilinear = new NodeDofEvaluator( "test_mesh.evaluator.y.quad_bilinear", meshY, quadNodeList,
            "library::quad_bilinear" );
        NodeDofEvaluator meshYSimplexBilinear = new NodeDofEvaluator( "test_mesh.evaluator.y.simplex_bilinear", meshY,
            triangleNodeList, "library::triangle_bilinear" );

        FEMField meshCoordinatesY = new FEMField( "test_mesh.coordinates.y", meshYdomain, meshDomain );
        meshCoordinatesY.setEvaluator( 1, meshYQuadBilinear );
        meshCoordinatesY.setEvaluator( 2, meshYSimplexBilinear );
        meshCoordinatesY.setEvaluator( 3, meshYSimplexBilinear );

        ContinuousAggregateField meshCoordinates = new ContinuousAggregateField( "test_mesh.coordinates.xy", meshXYdomain );
        meshCoordinates.setSourceField( 1, meshCoordinatesX );
        meshCoordinates.setSourceField( 2, meshCoordinatesY );

        test();

        serialize();
    }
}
