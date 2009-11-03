package fieldml.io;

public interface ReflectiveHandler
{
    void onStartInstance( Class<?> class1 );


    void onStringField( String name, String value );


    void onIntField( String name, Integer value );


    void onEndInstance( Class<?> class1 );


    void onStringListElement( Object o2 );


    void onIntListElement( Object o2 );


    void onDoubleListElement( Object value );


    void onStartList( Object o, String name );


    void onEndList( Object o );
}
