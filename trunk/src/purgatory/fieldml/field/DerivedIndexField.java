package purgatory.fieldml.field;

import purgatory.fieldml.domain.ContinuousDomain;
import purgatory.fieldml.domain.DiscreteIndexDomain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.exception.WrongFieldmlObjectTypeException;
import purgatory.fieldml.field.component.IndexComponent;
import purgatory.fieldml.field.component.IndexValueComponent;
import purgatory.fieldml.field.component.IndexValueNamedComponent;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.value.Value;

/**
 * IndexField defines a non-composite index-valued field. Index-valued fields
 * are separate from integer/real valued fields, as few (if any) mathematical
 * operations on them make sense. It would also be incorrect to interpret such
 * fields as representing even dimensionless values such as radians or
 * temperature. Typically, an IndexField's domain is a single-component instance
 * of DiscreteDomain, and serves as an index into another field.
 * 
 * In fact, until a good use-case can be found for a multiple-index
 * discrete-domain field, the API will infer "parameter 1, component 1" when
 * assigning and evaluating.
 */
public class DerivedIndexField
    extends DerivedField
    implements IndexField
{
    private final IndexComponent[] components;


    public DerivedIndexField( FieldmlObjectManager<Field> manager, DiscreteIndexDomain valueDomain, String name )
        throws FieldmlException
    {
        super( manager, name, valueDomain );

        components = new IndexComponent[valueDomain.getComponentCount()];
    }


    // Specifying an arbitrarily nested composition of binary operators on
    // domain, constant and/or
    // imported arguments seems non-trivial. Perhaps passing an array of
    // argument specifiers, and
    // an array of operator specifiers, and applying an RPN-style evaluation
    // algorithm might work.
    public void setComponentEvaluation( String componentName )
        throws FieldmlException
    {
        throw new BadFieldmlParameterException();
    }


    @Override
    void evaluateComponents( FieldValues values, Value value )
        throws FieldmlException
    {
        if( ( value.indexValues == null ) || ( value.indexValues.length < getComponentCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        for( int i = 0; i < getComponentCount(); i++ )
        {
            value.indexValues[i] = components[i].evaluate( values );
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

        if( !( getValueDomain( valueIndex ) instanceof DiscreteIndexDomain ) )
        {
            throw new WrongFieldmlObjectTypeException();
        }

        components[componentIndex] = new IndexValueComponent( valueIndex, valueComponentIndex );
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
        
        components[componentIndex] = new IndexValueNamedComponent( valueIndex, nameValueIndex, nameValueComponentIndex );
    }
}
