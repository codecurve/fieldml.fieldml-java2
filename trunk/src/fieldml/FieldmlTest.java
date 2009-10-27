package fieldml;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleDomainComponent;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;

public class FieldmlTest
{
    public static void main( String[] args )
    {
        EnsembleDomainComponent component1 = new EnsembleDomainComponent( "e" );
        component1.addValue( 1 );
        component1.addValue( 2 );

        EnsembleDomain elementDomain = new EnsembleDomain( "mesh.element_domain" );
        elementDomain.addComponent( component1 );

        
        
        
        
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        JdomReflectiveHandler handler = new JdomReflectiveHandler( doc.getRootElement() );
        ReflectiveWalker.Walk( elementDomain, handler );

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
