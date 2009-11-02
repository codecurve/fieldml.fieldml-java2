package fieldml;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;

public class FieldmlTest
{
    public static void main( String[] args )
    {
        MeshDomain meshDomain = new MeshDomain( "test_mesh.domain", 2 );
        meshDomain.addValue( 1 );
        meshDomain.addValue( 2 );
        meshDomain.addValue( 3 );
        meshDomain.addValue( 4 );
        
        EnsembleDomain triangleNodeDomain = new EnsembleDomain( "global.triangle.local_node" );
        triangleNodeDomain.addValue( 1 );
        triangleNodeDomain.addValue( 2 );
        triangleNodeDomain.addValue( 3 );

        EnsembleDomain quadNodeDomain = new EnsembleDomain( "global.quad.local_node" );
        quadNodeDomain.addValue( 1 );
        quadNodeDomain.addValue( 2 );
        quadNodeDomain.addValue( 3 );
        quadNodeDomain.addValue( 4 );

        
        
        
        
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        JdomReflectiveHandler handler = new JdomReflectiveHandler( doc.getRootElement() );
        ReflectiveWalker.Walk( meshDomain, handler );
        ReflectiveWalker.Walk( triangleNodeDomain, handler );
        ReflectiveWalker.Walk( quadNodeDomain, handler );

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
}
