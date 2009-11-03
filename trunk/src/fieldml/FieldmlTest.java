package fieldml;

import java.io.IOException;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.domain.SimpleEnsembleDomain;
import fieldml.evaluator.NodeDofEvaluator;
import fieldml.field.FEMField;
import fieldml.field.Field;
import fieldml.field.MappingField;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;

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
        for( Domain domain : Domain.domains.values() )
        {
            ReflectiveWalker.Walk( domain, handler );
        }

        for( Field field : Field.fields.values() )
        {
            ReflectiveWalker.Walk( field, handler );
        }

        XMLOutputter outputter = new XMLOutputter( Format.getPrettyFormat() );
        try
        {
            outputter.output( doc, System.out );
        }
        catch( IOException e )
        {
            System.err.println( e );
        }
    }


    public static void main( String[] args )
    {
        ContinuousDomain globalLine = new ContinuousDomain( "global.line", 1 );

        ContinuousDomain temperature = new ContinuousDomain( "global.temperature", 1 );

        ContinuousDomain globalPlane = new ContinuousDomain( "global.plane", 2 );

        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2 );
        meshDomain.addValue( 1 );
        meshDomain.addValue( 2 );
        meshDomain.addValue( 3 );

        SimpleEnsembleDomain meshNodes = new SimpleEnsembleDomain( "test_mesh.nodes" );
        meshNodes.addValue( 1 );
        meshNodes.addValue( 2 );
        meshNodes.addValue( 3 );
        meshNodes.addValue( 4 );
        meshNodes.addValue( 5 );
        meshNodes.addValue( 6 );

        EnsembleDomain triangleNodeDomain = new SimpleEnsembleDomain( "global.triangle.local_node" );
        triangleNodeDomain.addValue( 1 );
        triangleNodeDomain.addValue( 2 );
        triangleNodeDomain.addValue( 3 );

        EnsembleDomain quadNodeDomain = new SimpleEnsembleDomain( "global.quad.local_node" );
        quadNodeDomain.addValue( 1 );
        quadNodeDomain.addValue( 2 );
        quadNodeDomain.addValue( 3 );
        quadNodeDomain.addValue( 4 );

        MappingField triangleNodeList = new MappingField( "test_mesh.triangle_nodes", meshNodes, meshDomain, triangleNodeDomain );

        triangleNodeList.setValue( meshNodes.getValue( 2 ), 2, 1 );
        triangleNodeList.setValue( meshNodes.getValue( 5 ), 2, 2 );
        triangleNodeList.setValue( meshNodes.getValue( 3 ), 2, 3 );

        triangleNodeList.setValue( meshNodes.getValue( 6 ), 3, 1 );
        triangleNodeList.setValue( meshNodes.getValue( 3 ), 3, 2 );
        triangleNodeList.setValue( meshNodes.getValue( 5 ), 3, 3 );

        MappingField quadNodeList = new MappingField( "test_mesh.quad_nodes", meshNodes, meshDomain, quadNodeDomain );

        quadNodeList.setValue( meshNodes.getValue( 1 ), 1, 1 );
        quadNodeList.setValue( meshNodes.getValue( 4 ), 1, 2 );
        quadNodeList.setValue( meshNodes.getValue( 5 ), 1, 3 );
        quadNodeList.setValue( meshNodes.getValue( 2 ), 1, 4 );
        
        MappingField meshX = new MappingField( "test_mesh.node.x", globalLine, meshNodes );
        meshX.setValue( globalLine.getValue( new double[]{1} ), 1 );
        meshX.setValue( globalLine.getValue( new double[]{1} ), 2 );
        meshX.setValue( globalLine.getValue( new double[]{1} ), 3 );
        meshX.setValue( globalLine.getValue( new double[]{0} ), 4 );
        meshX.setValue( globalLine.getValue( new double[]{0} ), 5 );
        meshX.setValue( globalLine.getValue( new double[]{0} ), 6 );

        MappingField meshY = new MappingField( "test_mesh.node.y", globalLine, meshNodes );
        meshY.setValue( globalLine.getValue( new double[]{0} ), 1 );
        meshY.setValue( globalLine.getValue( new double[]{1} ), 2 );
        meshY.setValue( globalLine.getValue( new double[]{2} ), 3 );
        meshY.setValue( globalLine.getValue( new double[]{0} ), 4 );
        meshY.setValue( globalLine.getValue( new double[]{1} ), 5 );
        meshY.setValue( globalLine.getValue( new double[]{2} ), 6 );
        
        FEMField meshCoordinates = new FEMField( "test_mesh.coordinates", globalPlane, meshDomain );
        

        serialize();
    }
}
