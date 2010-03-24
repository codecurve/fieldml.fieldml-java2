package fieldml;

import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format.TextMode;

import fieldml.io.JdomReflectiveHandler;
import fieldml.region.Region;

public abstract class FieldmlTestCase
    extends TestCase
{
    public final void serialize( Region region, String comment )
    {
        Document doc = new Document();
        Element root = new Element( "fieldml" );
        doc.setRootElement( root );

        Comment comment1 = new Comment( comment );
        root.addContent( comment1 );
        
        Element regionRoot = new Element( "Region" );
        regionRoot.setAttribute( "name", region.getName() );
        root.addContent( regionRoot );

        JdomReflectiveHandler handler = new JdomReflectiveHandler( regionRoot );
        region.walkObjects( handler );

        Format format = Format.getPrettyFormat();
        format.setTextMode( TextMode.PRESERVE );
        XMLOutputter outputter = new XMLOutputter( format );
        try
        {
            PrintStream output = new PrintStream( "trunk\\data\\" + getClass().getSimpleName() + ".xml" );
            outputter.output( doc, output );
        }
        catch( IOException e )
        {
            System.err.println( e );
        }
    }
}
