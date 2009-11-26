package purgatory.fieldml.field;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import purgatory.fieldml.domain.ContinuousDomain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;


public class MappedRealField
    extends MappedField
    implements RealField
{
    private final Map<Integer, double[]> valueMap;


    public MappedRealField( FieldmlObjectManager<Field> manager, String name, ContinuousDomain valueDomain )
        throws FieldmlException
    {
        super( manager, name, valueDomain );

        valueMap = new HashMap<Integer, double[]>();
    }


    public void setComponentValues( int parameterValue, double[] componentValues )
        throws FieldmlException
    {
        int componentCount = getComponentCount();

        if( componentValues.length < componentCount )
        {
            throw new BadFieldmlParameterException();
        }

        valueMap.put( parameterValue, Arrays.copyOf( componentValues, componentCount ) );
    }


    @Override
    public void evaluate( FieldValues parameters, int[] parameterIndexes, Value value )
        throws FieldmlException
    {
        if( parameterIndexes.length < 1 )
        {
            throw new BadFieldmlParameterException();
        }
        Value parameter = parameters.values.get( parameterIndexes[0] );

        if( ( parameter.indexValues == null ) || ( parameter.indexValues.length <= keyComponentIndex ) )
        {
            throw new BadFieldmlParameterException();
        }
        if( ( value.realValues == null ) || ( value.realValues.length < getComponentCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        int keyValue = parameter.indexValues[keyComponentIndex];
        double[] values = valueMap.get( keyValue );
        int count = getComponentCount();

        for( int i = 0; i < count; i++ )
        {
            value.realValues[i] = values[i];
        }
    }


    public void getComponentValues( int parameterValue, double[] componentValues )
        throws FieldmlException
    {
        double[] values = valueMap.get( parameterValue );

        if( ( values == null ) || ( componentValues.length < values.length ) )
        {
            throw new BadFieldmlParameterException();
        }

        System.arraycopy( values, 0, componentValues, 0, getComponentCount() );
    }
}
