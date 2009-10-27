package fieldml.io;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Uses reflection to walk an object's instantiation graph.
 * 
 * Only public, non-static fields are traversed, which provides us with sufficient control
 * over traversal for now. At some point in the future, traversal should be explicitly managed
 * via appropriate annotations.
 * 
 * @see ReflectiveHandler
 */
public class ReflectiveWalker
{
    public static void Walk( Object o, ReflectiveHandler handler )
    {
        handler.onStartInstance( o.getClass() );

        Field[] fields = o.getClass().getFields();

        for( Field f : fields )
        {
            int modifiers = f.getModifiers();

            if( Modifier.isStatic( modifiers ) )
            {
                continue;
            }

            Class<?> type = f.getType();

            try
            {
                if( type == String.class )
                {
                    handler.onStringField( f.getName(), f.get( o ).toString() );
                    continue;
                }
                if( Iterable.class.isAssignableFrom( type ) )
                {
                    handler.onStartList( o, f.getName() );
                    for( Object o2 : (Iterable<? extends Object>)f.get( o ) )
                    {
                        if( o2.getClass() == String.class )
                        {
                            handler.onStringListElement( o2 );
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
                    handler.onEndList( o );
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
                Walk( f.get( o ), handler );
            }
            catch( Exception e )
            {
            }
        }

        handler.onEndInstance( o.getClass() );
    }
}
