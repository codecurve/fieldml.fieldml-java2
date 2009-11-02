package purgatory.fieldml.field;

import java.util.HashMap;
import java.util.Map;

import purgatory.fieldml.domain.DiscreteIndexDomain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;


// TODO: Needs Javadoc
public class MappedFieldIdField
    extends MappedField
    implements IndexField
{
    private final Map<Integer, int[]> valueMap;

    private final FieldmlObjectManager<Field> manager;


    public MappedFieldIdField( FieldmlObjectManager<Field> manager, String name, DiscreteIndexDomain valueDomain )
        throws FieldmlException
    {
        super( manager, name, valueDomain );

        valueMap = new HashMap<Integer, int[]>();
        this.manager = manager;
    }


    public void setComponentValues( int parameterValue, String[] fieldNames )
        throws FieldmlException
    {
        int componentCount = getComponentCount();

        if( fieldNames.length < componentCount )
        {
            throw new BadFieldmlParameterException();
        }

        int[] fieldIds = new int[fieldNames.length];

        for( int i = 0; i < fieldNames.length; i++ )
        {
            fieldIds[i] = manager.getId( fieldNames[i] );
        }

        valueMap.put( parameterValue, fieldIds );
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
        if( ( value.indexValues == null ) || ( value.indexValues.length < getComponentCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        int keyValue = parameter.indexValues[keyComponentIndex];
        int[] values = valueMap.get( keyValue );
        int count = getComponentCount();

        for( int i = 0; i < count; i++ )
        {
            value.fieldIdValues[i] = values[i];
        }
    }


    public void getComponentFieldIds( int parameterValue, int[] componentFieldIds )
        throws FieldmlException
    {
        int[] ids = valueMap.get( parameterValue );

        if( ( ids == null ) || ( componentFieldIds.length < ids.length ) )
        {
            throw new BadFieldmlParameterException();
        }

        System.arraycopy( ids, 0, componentFieldIds, 0, getComponentCount() );
    }
}
