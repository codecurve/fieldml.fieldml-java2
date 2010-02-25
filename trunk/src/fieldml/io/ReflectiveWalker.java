package fieldml.io;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import fieldml.annotations.SerializationAsString;
import fieldml.annotations.SerializationBlocked;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.EnsembleDomainValue;

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
        boolean doneHeader = false;
        
        for( Object o2 : (Iterable<? extends Object>)o )
        {
            if( o2 == null )
            {
                continue;
            }
            if( !doneHeader ) 
            {
                handler.onStartList( o, f.getName() );
                doneHeader = true;
            }
            if( o2.getClass() == String.class )
            {
                handler.onStringListElement( (String)o2 );
            }
            else if( o2.getClass() == Integer.class )
            {
                handler.onIntListElement( o2 );
            }
            else if( f.isAnnotationPresent( SerializationAsString.class ) )
            {
                handler.onListElementAsString( o2 );
            }
            else
            {
                Walk( o2, handler );
            }
        }
        if( doneHeader )
        {
            handler.onEndList( o );
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
                    handler.onStringListElement( (String)o2 );
                }
                else if( o2.getClass() == Integer.class )
                {
                    handler.onIntListElement( o2 );
                }
                else if( f.isAnnotationPresent( SerializationAsString.class ) )
                {
                    handler.onListElementAsString( o2 );
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
        if( !handler.onStartInstance( o ) )
        {
            return;
        }

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

            try
            {
                Class<?> fieldType = f.get( o ).getClass();

                if( Iterable.class.isAssignableFrom( fieldType ) )
                {
                    WalkList( f, f.get( o ), handler );
                    continue;
                }
                if( Map.class.isAssignableFrom( fieldType ) )
                {
                    handler.onStartList( o, f.getName() );
                    WalkMap( (Map<?, ?>)f.get( o ), handler );
                    handler.onEndList( o );
                    continue;
                }
                if( ContinuousDomainValue.class.isAssignableFrom( fieldType )
                    || EnsembleDomainValue.class.isAssignableFrom( fieldType )
                    )
                {
                    Object domainValue = f.get( o );
                    Field valuesField = domainValue.getClass().getField( "values" );
                    Object values = valuesField.get( domainValue );
                    handler.onStartList( values, valuesField.getName() );
                    WalkArray( valuesField, values, handler );
                    handler.onEndList( values );
                    continue;
                }
                if( fieldType.isArray() )
                {
                    handler.onStartList( o, f.getName() );
                    WalkArray( f, f.get( o ), handler );
                    handler.onEndList( o );
                    continue;
                }
                if( fieldType == String.class )
                {
                    handler.onStringField( f.getName(), (String)f.get( o ) );
                    continue;
                }
                else if( fieldType == Integer.class )
                {
                    handler.onIntField( f.getName(), (Integer)f.get( o ) );
                    continue;
                }
                else if( f.isAnnotationPresent( SerializationAsString.class ) )
                {
                    handler.onFieldAsString( f.getName(), f.get( o ) );
                    continue;
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
                    handler.onFieldAsString( f.getName(), f.get( o ) );
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
