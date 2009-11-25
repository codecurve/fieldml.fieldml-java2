package purgatory.fieldml.field;

import purgatory.fieldml.domain.DiscreteIndexDomain;
import purgatory.fieldml.domain.Domain;
import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObjectManager;

public abstract class MappedField
    extends Field
{
    protected int keyComponentIndex;


    public MappedField( FieldmlObjectManager<Field> manager, String name, Domain valueDomain )
        throws FieldmlException
    {
        super( manager, name, valueDomain );

        keyComponentIndex = -1;
    }


    public void setMappingParameterDomain( DiscreteIndexDomain domain, int componentIndex )
        throws FieldmlException
    {
        if( getValueCount() != 0 )
        {
            // We could allow the user to just change the parameter domain.
            throw new BadFieldmlParameterException();
        }
        if( ( componentIndex < 0 ) || ( componentIndex >= domain.getComponentCount() ) )
        {
            throw new BadFieldmlParameterException();
        }

        addEvaluator( new ParameterEvaluator( "mapping parameter", domain, 0 ) );

        keyComponentIndex = componentIndex;
    }


    public int getMappingParameterComponentIndex()
        throws FieldmlException
    {
        if( keyComponentIndex < 0 )
        {
            throw new BadFieldmlParameterException();
        }
        return keyComponentIndex;
    }


    public Domain getMappingParameterDomain()
        throws FieldmlException
    {
        if( getParameterCount() == 0 )
        {
            throw new BadFieldmlParameterException();
        }

        return getValueDomain( 0 );
    }
}
