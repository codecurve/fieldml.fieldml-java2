package purgatory.fieldml.util;

import java.util.HashMap;
import java.util.Map;

import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.exception.NoSuchFieldmlObjectException;
import purgatory.fieldml.exception.WrongFieldmlObjectTypeException;


/**
 * Tracks all top level fieldml objects
 */
public class FieldmlObjectManager<T extends FieldmlObject>
{
    private static final int ID_FUDGE = 10000;

    private final Map<Integer, T> objects = new HashMap<Integer, T>();

    private final Map<String, Integer> objectIds = new HashMap<String, Integer>();


    public FieldmlObjectManager()
    {
    }


    public T get( int id )
        throws FieldmlException
    {
        T object = objects.get( id );

        if( object == null )
        {
            throw new NoSuchFieldmlObjectException( id );
        }

        return object;
    }


    public T get( String name )
        throws FieldmlException
    {
        return get( getId( name ) );
    }


    public int getId( String name )
        throws FieldmlException
    {
        Integer id = objectIds.get( name );

        if( id == null )
        {
            throw new NoSuchFieldmlObjectException( id );
        }

        return id;
    }


    private int generateNewUniqueId()
    {
        return objects.size() + ID_FUDGE;
    }


    public int add( T object )
        throws FieldmlException
    {
        if( objectIds.get( object.getName() ) != null )
        {
            //TODO perhaps add a "ObjectAlreadyExists" exception
            throw new BadFieldmlParameterException();
        }
        int id = generateNewUniqueId();
        objects.put( id, object );
        objectIds.put( object.getName(), id );

        return id;
    }


    public void remove( int id )
    {
        FieldmlObject object = objects.get( id );

        if( object != null )
        {
            objects.remove( id );
            objectIds.remove( object.getName() );
        }
    }


    public <S> S getByClass( int id, Class<S> objectClass )
        throws FieldmlException
    {
        T object = get( id );

        if( object == null )
        {
            throw new NoSuchFieldmlObjectException( id );
        }

        try
        {
            return objectClass.cast( object );
        }
        catch( ClassCastException e )
        {
            throw new WrongFieldmlObjectTypeException();
        }
    }
}
