package fieldml;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import fieldml.domain.DirectEnsembleDomainComponent;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.ImportedEnsembleDomainComponent;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;

public class FieldmlTest
{
    public static void main( String[] args )
    {
        DirectEnsembleDomainComponent component1 = new DirectEnsembleDomainComponent( "e" );
        component1.addValue( 1 );
        component1.addValue( 2 );
        component1.addValue( 3 );
        component1.addValue( 4 );

        EnsembleDomain elementDomain = new EnsembleDomain( "mesh.element_domain" );
        elementDomain.addComponent( component1 );

        EnsembleDomain triangleNodesDomain = new EnsembleDomain( "mesh.triangle_nodes" );
        triangleNodesDomain.addComponent( new ImportedEnsembleDomainComponent( "node1", elementDomain, "e" ) );
        triangleNodesDomain.addComponent( new ImportedEnsembleDomainComponent( "node2", elementDomain, "e" ) );
        triangleNodesDomain.addComponent( new ImportedEnsembleDomainComponent( "node3", elementDomain, "e" ) );

        EnsembleDomain quadNodesDomain = new EnsembleDomain( "mesh.quad_nodes" );
        quadNodesDomain.addComponent( new ImportedEnsembleDomainComponent( "node1", elementDomain, "e" ) );
        quadNodesDomain.addComponent( new ImportedEnsembleDomainComponent( "node2", elementDomain, "e" ) );
        quadNodesDomain.addComponent( new ImportedEnsembleDomainComponent( "node3", elementDomain, "e" ) );
        quadNodesDomain.addComponent( new ImportedEnsembleDomainComponent( "node4", elementDomain, "e" ) );

        
        
        
        
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        JdomReflectiveHandler handler = new JdomReflectiveHandler( doc.getRootElement() );
        ReflectiveWalker.Walk( elementDomain, handler );
        ReflectiveWalker.Walk( triangleNodesDomain, handler );
        ReflectiveWalker.Walk( quadNodesDomain, handler );

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
