package purgatory.fieldml.field;

import purgatory.fieldml.domain.ContinuousDomain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.exception.WrongFieldmlObjectTypeException;
import purgatory.fieldml.field.component.RealComponent;
import purgatory.fieldml.field.component.RealValueComponent;
import purgatory.fieldml.field.component.RealValueNamedComponent;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;

public class DerivedRealField
    extends DerivedField
    implements RealField
{
    private final RealComponent[] components;


    public DerivedRealField( FieldmlObjectManager<Field> manager, ContinuousDomain valueDomain, String name )
        throws FieldmlException
    {
        super( manager, name, valueDomain );

        components = new RealComponent[valueDomain.getComponentCount()];
    }


    @Override
    void evaluateComponents( FieldValues values, Value value )
        throws FieldmlException
    {
        if( ( value.realValues == null ) || ( value.realValues.length < getComponentCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        for( int i = 0; i < getComponentCount(); i++ )
        {
            value.realValues[i] = components[i].evaluate( values );
        }
    }


    @Override
    public void defineComponent( int componentIndex, int valueIndex, int valueComponentIndex )
        throws FieldmlException
    {
        if( ( componentIndex < 0 ) || ( componentIndex >= components.length ) )
        {
            throw new BadFieldmlParameterException();
        }
        if( components[componentIndex] != null )
        {
            throw new BadFieldmlParameterException();
        }
        
        if( ( valueIndex < 0 ) || ( valueIndex >= getValueCount() ) )
        {
            throw new BadFieldmlParameterException();
        }
        if( ( valueComponentIndex < 0 ) || ( valueComponentIndex >= getValueDomain( valueIndex ).getComponentCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        if( !( getValueDomain( valueIndex ) instanceof ContinuousDomain ) )
        {
            throw new WrongFieldmlObjectTypeException();
        }
        
        components[componentIndex] = new RealValueComponent( valueIndex, valueComponentIndex );
    }


    @Override
    public void defineNamedComponent( int componentIndex, int valueIndex, int nameValueIndex, int nameValueComponentIndex )
        throws FieldmlException
    {
        if( ( componentIndex < 0 ) || ( componentIndex >= components.length ) )
        {
            throw new BadFieldmlParameterException();
        }
        if( components[componentIndex] != null )
        {
            throw new BadFieldmlParameterException();
        }
        
        if( ( valueIndex < 0 ) || ( valueIndex >= getValueCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        if( ( nameValueIndex < 0 ) || ( nameValueIndex >= getValueCount() ) )
        {
            throw new BadFieldmlParameterException();
        }
        if( ( nameValueComponentIndex < 0 ) || ( nameValueComponentIndex >= getValueDomain( nameValueIndex ).getComponentCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        if( !( getValueDomain( valueIndex ) instanceof ContinuousDomain ) )
        {
            throw new WrongFieldmlObjectTypeException();
        }
        
        //TODO Check the domain of the name value.
        
        components[componentIndex] = new RealValueNamedComponent( valueIndex, nameValueIndex, nameValueComponentIndex );
    }
}
