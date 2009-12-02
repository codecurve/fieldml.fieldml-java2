package fieldml.io;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import fieldml.annotations.SerializationAsString;
import fieldml.annotations.SerializationBlocked;

/**
 * Uses reflection to walk an object's instantiation graph.
 * 
 * Only public, non-static fields are traversed, which provides us with sufficient control over traversal for now. At some point
 * in the future, traversal should be explicitly managed via appropriate annotations.
 * 
 * @see ReflectiveHandler
 */
public class ReflectiveWalker
{
    @SuppressWarnings( "unchecked" )
    private static void WalkList( Field f, Object o, ReflectiveHandler handler )
    {
        for( Object o2 : (Iterable<? extends Object>)o )
        {
            if( o2.getClass() == String.class )
            {
                handler.onStringListElement( o2 );
            }
            else if( f.isAnnotationPresent( SerializationAsString.class ) )
            {
                handler.onListElementAsString( o2 );
            }
            else if( o2.getClass() == Integer.class )
            {
                handler.onIntListElement( o2 );
            }
            else
            {
                Walk( o2, handler );
            }
        }
    }


    private static void WalkArray( Field f, Object o, ReflectiveHandler handler )
    {
        Class<?> type = f.getType();

        if( type == int[].class )
        {
            for( int i : (int[])o )
            {
                handler.onIntListElement( i );
            }
        }
        else if( type == double[].class )
        {
            for( double d : (double[])o )
            {
                handler.onDoubleListElement( d );
            }
        }
        else
        {
            for( Object o2 : (Object[])o )
            {
                if( o2.getClass() == String.class )
                {
                    handler.onStringListElement( o2 );
                }
                else if( f.isAnnotationPresent( SerializationAsString.class ) )
                {
                    handler.onListElementAsString( o2 );
                }
                else if( o2.getClass() == Integer.class )
                {
                    handler.onIntListElement( o2 );
                }
                else
                {
                    Walk( o2, handler );
                }
            }
        }
    }


    private static <K, V> void WalkMap( Map<K, V> o, ReflectiveHandler handler )
    {
        for( Entry<K, V> e : o.entrySet() )
        {
            handler.onMapEntry( e.getKey().toString(), e.getValue().toString() );
        }
    }


    public static void Walk( Object o, ReflectiveHandler handler )
    {
        handler.onStartInstance( o );

        Field[] fields = o.getClass().getFields();

        for( Field f : fields )
        {
            int modifiers = f.getModifiers();

            if( Modifier.isStatic( modifiers ) )
            {
                continue;
            }
            if( f.isAnnotationPresent( SerializationBlocked.class ) )
            {
                continue;
            }

            Class<?> type = f.getType();

            try
            {
                if( Iterable.class.isAssignableFrom( type ) )
                {
                    handler.onStartList( o, f.getName() );
                    WalkList( f, f.get( o ), handler );
                    handler.onEndList( o );
                    continue;
                }
                if( Map.class.isAssignableFrom( type ) )
                {
                    handler.onStartList( o, f.getName() );
                    WalkMap( (Map<?, ?>)f.get( o ), handler );
                    handler.onEndList( o );
                    continue;
                }
                if( type.isArray() )
                {
                    handler.onStartList( o, f.getName() );
                    WalkArray( f, f.get( o ), handler );
                    handler.onEndList( o );
                    continue;
                }
                if( type == String.class )
                {
                    handler.onStringField( f.getName(), f.get( o ).toString() );
                    continue;
                }
                else if( f.isAnnotationPresent( SerializationAsString.class ) )
                {
                    handler.onFieldAsString( f.getName(), f.get( o ) );
                }
            }
            catch( Exception e )
            {
            }

            try
            {
                handler.onIntField( f.getName(), f.getInt( o ) );
                continue;
            }
            catch( Exception e )
            {
            }

            try
            {
                if( f.isAnnotationPresent( SerializationAsString.class ) )
                {
                    handler.onStringField( f.getName(), f.get( o ).toString() );
                    continue;
                }
                Walk( f.get( o ), handler );
            }
            catch( Exception e )
            {
            }
        }

        handler.onEndInstance( o );
    }
}
