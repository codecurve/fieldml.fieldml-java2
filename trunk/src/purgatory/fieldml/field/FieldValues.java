package purgatory.fieldml.field;

import java.util.ArrayList;

import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObject;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;


/**
 * This class essentially maintains a list of dynamic casts, allowing clients to access values by index without having to use
 * instanceof or casting.
 */
public class FieldValues
    implements FieldmlObject
{
    public final ArrayList<Value> values;
    
    private final int id;


    public FieldValues( FieldmlObjectManager<FieldValues> manager, ArrayList<Domain> domains )
        throws FieldmlException
    {
        values = new ArrayList<Value>();

        for( Domain parameterDomain : domains )
        {
            values.add( new Value( parameterDomain ) );
        }
        
        this.id = manager.add( this );
    }
    
    
    FieldValues()
    {
        values = new ArrayList<Value>();

        id = 0;
    }


    void addDomain( Domain domain )
    {
        values.add( new Value( domain ) );
    }


    @Override
    public int getId()
    {
        return id;
    }


    @Override
    public String getName()
    {
        // Cache names are never visible to the API user.
        return toString();
    }
}
