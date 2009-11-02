package purgatory.fieldml.field;

import java.util.ArrayList;

import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObject;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.util.general.ImmutableList;
import purgatory.fieldml.util.general.MutableArrayList;
import purgatory.fieldml.value.Value;


public abstract class Field
    implements FieldmlObject
{
    Domain valueDomain;

    /**
     * A globally unique integer identifying the field, useful for internal
     * (inter-process) and external (client-server) communication. In order to
     * remain globally unique, this id number cannot be user-supplied. Fields
     * can be imported from external sources, and can therefore have id numbers
     * which are not known in advance by the user of the API when creating their
     * own fields.
     */
    private final int id;

    /**
     * A locally unique string.
     */
    private final String name;

    private final ArrayList<Evaluator> evaluators;


    public Field( FieldmlObjectManager<Field> manager, String name, Domain valueDomain )
        throws FieldmlException
    {
        this.name = name;
        this.valueDomain = valueDomain;

        evaluators = new ArrayList<Evaluator>();

        id = manager.add( this );
    }


    @Override
    public String toString()
    {
        return "Field " + name + " (" + id + ")";
    }


    public int getId()
    {
        return id;
    }


    public String getName()
    {
        return name;
    }


    public int getComponentCount()
    {
        return valueDomain.getComponentCount();
    }


    int getComponentIndex( String componentName )
    {
        return valueDomain.getComponentIndex( componentName );
    }


    public Domain getValueDomain()
    {
        return valueDomain;
    }


    /**
     * Evaluate this field using the given parameters. The parameters are
     * provided by a FieldValues object, and a list of indexes into that
     * object. This allows the caller to re-use the same FieldValues object
     * to build up a set of values without having to construct a correctly
     * ordered list of parameters for each field evaluation they wish to do.
     * 
     * The field evaluates into the given Value object, which may in turn be an
     * entry in the given FieldValues object.
     * 
     * This makes the FieldValues analogous to a heap, and the indexes into
     * it analogous to a list of references.
     */
    public abstract void evaluate( FieldValues values, int[] valueIndexes, Value value )
        throws FieldmlException;


    protected void addEvaluator( Evaluator parameter )
        throws FieldmlException
    {
        for( Evaluator p : evaluators )
        {
            if( p.getName().equals( parameter.getName() ) )
            {
                throw new BadFieldmlParameterException();
            }
        }

        evaluators.add( parameter );
    }


    public int getParameterCount()
    {
        int count = 0;

        for( Evaluator e : evaluators )
        {
            if( e instanceof ParameterEvaluator )
            {
                count++;
            }
        }

        return count;
    }


    public void getParameterDomains( int[] domainIds )
        throws FieldmlException
    {
        int index = 0;

        for( Evaluator e : evaluators )
        {
            if( !( e instanceof ParameterEvaluator ) )
            {
                continue;
            }

            if( domainIds.length <= index )
            {
                throw new BadFieldmlParameterException();
            }

            domainIds[index++] = e.getDomain().getId();
        }
    }


    public Domain getValueDomain( int valueIndex )
        throws FieldmlException
    {
        Evaluator evaluator = getEvaluator( valueIndex );

        return evaluator.getDomain();
    }


    public String getValueName( int valueIndex )
        throws FieldmlException
    {
        Evaluator evaluator = getEvaluator( valueIndex );

        return evaluator.getName();
    }


    public int getValueCount()
    {
        return evaluators.size();
    }


    public ImmutableList<Domain> getSignature()
    {
        MutableArrayList<Domain> signature = new MutableArrayList<Domain>();

        signature.add( valueDomain );

        for( Evaluator e : evaluators )
        {
            if( e instanceof ParameterEvaluator )
            {
                signature.add( e.getDomain() );
            }
        }

        return signature;
    }


    public int getValueType( int valueIndex )
        throws FieldmlException
    {
        Evaluator evaluator = getEvaluator( valueIndex );

        return evaluator.getType();
    }


    public Evaluator getEvaluator( int valueIndex )
        throws FieldmlException
    {
        if( ( valueIndex < 0 ) || ( valueIndex >= evaluators.size() ) )
        {
            throw new BadFieldmlParameterException();
        }

        return evaluators.get( valueIndex );
    }
}
