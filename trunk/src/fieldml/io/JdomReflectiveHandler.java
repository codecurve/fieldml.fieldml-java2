package fieldml.io;

import org.jdom.Element;
import org.jdom.Text;

public class JdomReflectiveHandler
    implements ReflectiveHandler
{
    private Element rootElement;

    private Element currentElement;


    public JdomReflectiveHandler( Element rootElement )
    {
        this.rootElement = rootElement;
        this.currentElement = rootElement;
    }


    @Override
    public void onEndInstance( Object o )
    {
        currentElement = currentElement.getParentElement();
        if( currentElement == rootElement )
        {
            currentElement.addContent( new Text( "\n" ) );
        }
    }


    @Override
    public void onIntField( String name, Integer value )
    {
        currentElement.setAttribute( name, value.toString() );
    }


    @Override
    public void onStringField( String name, String value )
    {
        currentElement.setAttribute( name, value );
    }


    @Override
    public void onFieldAsString( String name, Object value )
    {
        currentElement.setAttribute( name, value.toString() );
    }


    @Override
    public void onStartInstance( Object o )
    {
        Element e = new Element( o.getClass().getSimpleName() );
        currentElement.addContent( e );
        currentElement = e;
    }


    @Override
    public void onIntListElement( Object o )
    {
        Text t = new Text( o.toString() + "  " );
        currentElement.addContent( t );
    }


    @Override
    public void onMapEntry( String key, String value )
    {
        Element e = new Element( "entry" );
        e.setAttribute( "key", key );
        e.setAttribute( "value", value );
        currentElement.addContent( e );
    }


    @Override
    public void onDoubleListElement( Object o )
    {
        Text t = new Text( o.toString() + "  " );
        currentElement.addContent( t );
    }


    @Override
    public void onStringListElement( Object o )
    {
        Text t = new Text( "\"" + o.toString() + "\"  " );
        currentElement.addContent( t );
    }


    @Override
    public void onListElementAsString( Object o )
    {
        Text t = new Text( "\"" + o.toString() + "\"  " );
        currentElement.addContent( t );
    }


    @Override
    public void onStartList( Object o, String name )
    {
        Element e = new Element( name );
        currentElement.addContent( e );
        currentElement = e;
    }


    @Override
    public void onEndList( Object o )
    {
        currentElement = currentElement.getParentElement();
    }
}
