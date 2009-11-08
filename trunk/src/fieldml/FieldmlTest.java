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
import fieldml.field.FEMField;
import fieldml.field.Field;
import fieldml.field.MappingField;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.EnsembleDomainValue;

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

        for( Field<?> field : Field.fields.values() )
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
        Field<?> meshX = Field.fields.get( "test_mesh.coordinates.x" );

        ContinuousDomainValue output;

        // Test element 1
        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 1, 0.0, 0.0 ) );
        assert output.chartValues[0] == 0;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 1, 0.0, 1.0 ) );
        assert output.chartValues[0] == 0;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 1, 0.5, 0.0 ) );
        assert output.chartValues[0] == 5;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 1, 1.0, 0.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 1, 1.0, 1.0 ) );
        assert output.chartValues[0] == 10;

        // Test element 2
        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 2, 0.0, 0.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 2, 1.0, 0.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 2, 0.0, 1.0 ) );
        assert output.chartValues[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 2, 0.5, 0.5 ) );
        assert output.chartValues[0] == 15;

        // Test element 3
        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 3, 0.0, 0.0 ) );
        assert output.chartValues[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 3, 1.0, 0.0 ) );
        assert output.chartValues[0] == 20;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 3, 0.0, 1.0 ) );
        assert output.chartValues[0] == 10;

        output = (ContinuousDomainValue)meshX.evaluate( meshDomain.makeValue( 3, 0.5, 0.5 ) );
        assert output.chartValues[0] == 15;
    }


    public static void main( String[] args )
    {
        ContinuousDomain libraryLine = new ContinuousDomain( "library.line", 1 );

        ContinuousDomain libraryPlane = new ContinuousDomain( "library.plane", 2 );

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

        MappingField<EnsembleDomainValue> triangleNodeList = new MappingField<EnsembleDomainValue>( "test_mesh.triangle_nodes",
            globalNodesDomain, testMeshElementDomain, triangleNodeDomain );

        triangleNodeList.setValue( globalNodesDomain.makeValue( 2 ), 2, 1 );
        triangleNodeList.setValue( globalNodesDomain.makeValue( 5 ), 2, 2 );
        triangleNodeList.setValue( globalNodesDomain.makeValue( 3 ), 2, 3 );

        triangleNodeList.setValue( globalNodesDomain.makeValue( 6 ), 3, 1 );
        triangleNodeList.setValue( globalNodesDomain.makeValue( 3 ), 3, 2 );
        triangleNodeList.setValue( globalNodesDomain.makeValue( 5 ), 3, 3 );

        MappingField<EnsembleDomainValue> quadNodeList = new MappingField<EnsembleDomainValue>( "test_mesh.quad_nodes",
            globalNodesDomain, testMeshElementDomain, quadNodeDomain );

        quadNodeList.setValue( globalNodesDomain.makeValue( 4 ), 1, 1 );
        quadNodeList.setValue( globalNodesDomain.makeValue( 5 ), 1, 2 );
        quadNodeList.setValue( globalNodesDomain.makeValue( 1 ), 1, 3 );
        quadNodeList.setValue( globalNodesDomain.makeValue( 2 ), 1, 4 );

        MappingField<ContinuousDomainValue> meshX = new MappingField<ContinuousDomainValue>( "test_mesh.node.x", libraryLine,
            globalNodesDomain );
        meshX.setValue( libraryLine.makeValue( 00.0 ), 1 );
        meshX.setValue( libraryLine.makeValue( 10.0 ), 2 );
        meshX.setValue( libraryLine.makeValue( 20.0 ), 3 );
        meshX.setValue( libraryLine.makeValue( 00.0 ), 4 );
        meshX.setValue( libraryLine.makeValue( 10.0 ), 5 );
        meshX.setValue( libraryLine.makeValue( 20.0 ), 6 );

        MappingField<ContinuousDomainValue> meshY = new MappingField<ContinuousDomainValue>( "test_mesh.node.y", libraryLine,
            globalNodesDomain );
        meshY.setValue( libraryLine.makeValue( 10.0 ), 1 );
        meshY.setValue( libraryLine.makeValue( 10.0 ), 2 );
        meshY.setValue( libraryLine.makeValue( 10.0 ), 3 );
        meshY.setValue( libraryLine.makeValue( 00.0 ), 4 );
        meshY.setValue( libraryLine.makeValue( 00.0 ), 5 );
        meshY.setValue( libraryLine.makeValue( 00.0 ), 6 );

        NodeDofEvaluator meshXQuadBilinear = new NodeDofEvaluator( "test_mesh.evaluator.x.quad_bilinear", meshX, quadNodeList,
            "library::quad_bilinear" );
        NodeDofEvaluator meshXSimplexBilinear = new NodeDofEvaluator( "test_mesh.evaluator.x.simplex_bilinear", meshX,
            triangleNodeList, "library::triangle_bilinear" );

        FEMField<ContinuousDomainValue> meshCoordinatesX = new FEMField<ContinuousDomainValue>( "test_mesh.coordinates.x",
            libraryLine, meshDomain );
        meshCoordinatesX.setEvaluator( 1, meshXQuadBilinear );
        meshCoordinatesX.setEvaluator( 2, meshXSimplexBilinear );
        meshCoordinatesX.setEvaluator( 3, meshXSimplexBilinear );

        NodeDofEvaluator meshYQuadBilinear = new NodeDofEvaluator( "test_mesh.evaluator.y.quad_bilinear", meshY, quadNodeList,
            "library::quad_bilinear" );
        NodeDofEvaluator meshYSimplexBilinear = new NodeDofEvaluator( "test_mesh.evaluator.y.simplex_bilinear", meshY,
            triangleNodeList, "library::triangle_bilinear" );

        FEMField<ContinuousDomainValue> meshCoordinatesY = new FEMField<ContinuousDomainValue>( "test_mesh.coordinates.y",
            libraryLine, meshDomain );
        meshCoordinatesY.setEvaluator( 1, meshYQuadBilinear );
        meshCoordinatesY.setEvaluator( 2, meshYSimplexBilinear );
        meshCoordinatesY.setEvaluator( 3, meshYSimplexBilinear );

        test();

        serialize();
    }
}
