package fieldml.io;

import org.jdom.Element;

public class JdomReflectiveHandler
    implements ReflectiveHandler
{
    private Element currentElement;


    public JdomReflectiveHandler( Element rootElement )
    {
        this.currentElement = rootElement;
    }


    @Override
    public void onEndInstance( Class<?> class1 )
    {
        currentElement = currentElement.getParentElement();
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
    public void onStartInstance( Class<?> class1 )
    {
        Element e = new Element( class1.getSimpleName() );
        currentElement.addContent( e );
        currentElement = e;
    }


    @Override
    public void onIntListElement( Object o2 )
    {
        Element e = new Element( "Integer" );
        e.setAttribute( "value", o2.toString() );
        currentElement.addContent( e );
    }


    @Override
    public void onStringListElement( Object o2 )
    {
        Element e = new Element( "String" );
        e.setAttribute( "value", o2.toString() );
        currentElement.addContent( e );
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
