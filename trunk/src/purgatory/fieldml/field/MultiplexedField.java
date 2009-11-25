package purgatory.fieldml.field;

import purgatory.fieldml.domain.ContinuousDomain;
import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;

//NOTE: Experimental class. Not currently in use.
public class MultiplexedField
    extends DerivedField
{
    private MappedField sourceField;

    private Value sourceValue;

    private int sourceParameterIndex;

    private int sourceComponentIndex;

    private final int sourceFieldParameterIndexes[];

    private FieldValues sourceParameters;

    // HACK!
    private boolean isReal;


    public MultiplexedField( FieldmlObjectManager<Field> manager, String name, Domain valueDomain )
        throws FieldmlException
    {
        super( manager, name, valueDomain );

        sourceFieldParameterIndexes = new int[1];
        sourceFieldParameterIndexes[0] = 0;
    }


    @Override
    void evaluateComponents( FieldValues parameters, Value value )
        throws FieldmlException
    {
        // TODO Parameter checking
        Value sourceParameterValue = sourceParameters.values.get( 0 );
        Value inputParameterValues = parameters.values.get( sourceParameterIndex );

        for( int i = 0; i < getComponentCount(); i++ )
        {
            sourceParameterValue.indexValues[0] = inputParameterValues.indexValues[i];

            sourceField.evaluate( sourceParameters, sourceFieldParameterIndexes, sourceValue );

            if( isReal )
            {
                value.realValues[i] = sourceValue.realValues[sourceComponentIndex];
            }
            else
            {
                value.indexValues[i] = sourceValue.indexValues[sourceComponentIndex];
            }
        }
    }


    public void setMultiplexComponent( MappedField sourceField, int sourceComponentIndex, int parameterIndex )
        throws FieldmlException
    {
        // TODO Parameter checking
        if( ( sourceField.valueDomain instanceof ContinuousDomain ) != ( valueDomain instanceof ContinuousDomain ) )
        {
            throw new BadFieldmlParameterException();
        }

        if( sourceComponentIndex >= sourceField.getComponentCount() )
        {
            throw new BadFieldmlParameterException();
        }

        if( sourceField.getValueDomain( 0 ).getComponentCount() != valueDomain.getComponentCount() )
        {
            throw new BadFieldmlParameterException();
        }

        isReal = sourceField instanceof RealField;

        this.sourceField = sourceField;
        this.sourceComponentIndex = sourceComponentIndex;
        this.sourceParameterIndex = parameterIndex;

        sourceValue = new Value( sourceField.valueDomain );

        sourceParameters = new FieldValues();
        sourceParameters.addDomain( sourceField.getValueDomain( 0 ) );
    }


    @Override
    public void defineComponent( int componentIndex, int valueIndex, int valueComponentIndex )
        throws FieldmlException
    {
        // TODO Auto-generated method stub
    }


    @Override
    public void defineNamedComponent( int componentIndex, int valueIndex, int nameValueIndex, int valueComponentIndex )
        throws FieldmlException
    {
        // TODO Auto-generated method stub
    }
}
