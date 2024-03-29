package fieldmlx.io;

public interface ReflectiveHandler
{
    boolean onStartInstance( Object o );


    void onStringField( String name, String value );


    void onIntField( String name, Integer value );


    void onEndInstance( Object o );


    void onStringListElement( String o );


    void onListElementAsString( Object o );


    void onIntListElement( Object o );


    void onDoubleListElement( Object value );


    void onStartList( Object o, String name );


    void onEndList( Object o );


    void onMapEntry( String key, String value );


    void onFieldAsString( String name, Object o );
}
