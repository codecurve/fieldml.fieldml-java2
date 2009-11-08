package purgatory.fieldml.field;

import purgatory.fieldml.domain.DiscreteFieldDomain;
import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.util.general.ImmutableList;
import purgatory.fieldml.value.Value;

public abstract class DerivedField
    extends Field
{
    private final FieldValues localFieldValues;

    private boolean hasNonInputParameters;

    private final FieldmlObjectManager<Field> manager;


    public DerivedField( FieldmlObjectManager<Field> manager, String name, Domain valueDomain )
        throws FieldmlException
    {
        super( manager, name, valueDomain );

        localFieldValues = new FieldValues();

        hasNonInputParameters = false;

        this.manager = manager;
    }


    public int addParameter( String parameterName, Domain domain )
        throws FieldmlException
    {
        // To keep parameter indexes consistent between Field.Foo calls and
        // ComputedField.Foo calls, we need to ensure that input parameters
        // always occur before derived parameters.
        if( hasNonInputParameters )
        {
            throw new BadFieldmlParameterException();
        }

        int parameterIndex = getParameterCount();

        addEvaluator( new ParameterEvaluator( parameterName, domain, parameterIndex ) );

        localFieldValues.addDomain( domain );

        return parameterIndex;
    }


    private boolean checkArgumentIndexes( ImmutableList<Domain> signature, int[] argumentIndexes )
        throws FieldmlException
    {
        int parameterCount = signature.size() - 1;

        if( parameterCount > argumentIndexes.length )
        {
            throw new BadFieldmlParameterException();
        }

        for( int i = 0; i < parameterCount; i++ )
        {
            if( argumentIndexes[i] >= localFieldValues.values.size() )
            {
                // ERROR derived parameter references an unknown parameter.
                // NOTE although a derived parameter could forward-reference a
                // parameter, the easiest way to prevent circular dependancies
                // is to insist that derived parameters only refer to already
                // defined ones.
                throw new BadFieldmlParameterException();
            }

            Domain parameterDomain = localFieldValues.values.get( argumentIndexes[i] ).domain;

            if( parameterDomain.getId() != signature.get( i + 1 ).getId() )
            {
                // Domain mismatch
                return false;
            }
        }

        return true;
    }


    public int addFieldValue( String valueName, Field field, int[] fieldParameterIndexes )
        throws FieldmlException
    {
        ImmutableList<Domain> signature = field.getSignature();

        if( !checkArgumentIndexes( signature, fieldParameterIndexes ) )
        {
            throw new BadFieldmlParameterException();
        }

        int parameterIndex = getParameterCount();

        addEvaluator( new FieldEvaluator( valueName, field, fieldParameterIndexes, parameterIndex ) );
        localFieldValues.addDomain( field.valueDomain );

        hasNonInputParameters = true;

        return parameterIndex;
    }


    public int addIndirectFieldValue( String valueName, int fieldValueIndex, int fieldValueComponentIndex,
        int[] fieldParameterIndexes )
        throws FieldmlException
    {
        if( fieldValueIndex > getParameterCount() )
        {
            throw new BadFieldmlParameterException();
        }

        Domain parameterDomain = localFieldValues.values.get( fieldValueIndex ).domain;

        if( !( parameterDomain instanceof DiscreteFieldDomain ) )
        {
            throw new BadFieldmlParameterException();
        }

        DiscreteFieldDomain fieldParameterDomain = (DiscreteFieldDomain)parameterDomain;

        ImmutableList<Domain> signature = fieldParameterDomain.getSignature();

        checkArgumentIndexes( signature, fieldParameterIndexes );

        addEvaluator( new IndirectFieldEvaluator( manager, valueName, signature.get( 0 ), fieldValueIndex,
            fieldValueComponentIndex, fieldParameterIndexes, getParameterCount() ) );
        localFieldValues.addDomain( signature.get( 0 ) );

        hasNonInputParameters = true;

        return getParameterCount();
    }


    abstract void evaluateComponents( FieldValues values, Value value )
        throws FieldmlException;


    @Override
    public void evaluate( FieldValues parameters, int[] parameterIndexes, Value value )
        throws FieldmlException
    {
        if( getParameterCount() > parameterIndexes.length )
        {
            throw new BadFieldmlParameterException();
        }

        for( int i = 0; i < getParameterCount(); i++ )
        {
            Evaluator evaluator = getEvaluator( i );

            evaluator.evaluate( parameters, parameterIndexes, localFieldValues );
        }

        evaluateComponents( localFieldValues, value );
    }


    public int getFieldValueField( int valueIndex )
        throws FieldmlException
    {
        if( ( valueIndex < 0 ) || ( valueIndex >= getParameterCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        if( valueIndex < getParameterCount() )
        {
            throw new BadFieldmlParameterException();
        }

        Evaluator evaluator = getEvaluator( valueIndex );

        if( !( evaluator instanceof FieldEvaluator ) )
        {
            throw new BadFieldmlParameterException();
        }

        FieldEvaluator directParameter = (FieldEvaluator)evaluator;

        return directParameter.getField().getId();
    }


    public int getFieldValueParameters( int valueIndex, int[] parameterIndexes )
        throws FieldmlException
    {
        if( ( valueIndex < 0 ) || ( valueIndex >= getParameterCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        if( valueIndex < getParameterCount() )
        {
            throw new BadFieldmlParameterException();
        }

        Evaluator evaluator = getEvaluator( valueIndex );

        int[] indexes = null;
        if( evaluator instanceof FieldEvaluator )
        {
            indexes = ( (FieldEvaluator)evaluator ).getIndexes();
        }
        else if( evaluator instanceof IndirectFieldEvaluator )
        {
            indexes = ( (IndirectFieldEvaluator)evaluator ).getIndexes();
        }
        else
        {
            throw new BadFieldmlParameterException();
        }

        System.arraycopy( indexes, 0, parameterIndexes, 0, indexes.length );

        return indexes.length;
    }


    public abstract void defineComponent( int componentIndex, int valueIndex, int valueComponentIndex )
        throws FieldmlException;


    public abstract void defineNamedComponent( int componentIndex, int valueIndex, int nameValueIndex, int valueComponentIndex )
        throws FieldmlException;
}
