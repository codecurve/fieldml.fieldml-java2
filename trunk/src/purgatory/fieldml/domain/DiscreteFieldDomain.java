package purgatory.fieldml.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObjectManager;
import purgatory.fieldml.util.general.ImmutableList;


public class DiscreteFieldDomain
    extends Domain
{
    //TODO We need to convert the field names into field ids for internal use.
    private final List<int[]> fieldIds = new ArrayList<int[]>();

    private final List<String[]> fieldNames = new ArrayList<String[]>();


    public DiscreteFieldDomain( FieldmlObjectManager<Domain> manager, String name )
        throws FieldmlException
    {
        super( manager, name );
    }


    public int addComponent( String componentName, String[] names, int valueCount )
        throws FieldmlException
    {
        if( names.length < valueCount )
        {
            throw new BadFieldmlParameterException();
        }
        if( valueCount < 1 )
        {
            throw new BadFieldmlParameterException();
        }

        super.addComponent( componentName );

        // TODO Check that each value is unique.
        fieldNames.add( Arrays.copyOfRange( names, 0, valueCount ) );
        fieldIds.add( new int[ valueCount ] );

        return getComponentCount() - 1;
    }


    public int getComponentValueCount( int componentIndex )
        throws FieldmlException
    {
        if( ( componentIndex < 0 ) || ( componentIndex >= fieldNames.size() ) )
        {
            throw new BadFieldmlParameterException();
        }

        return fieldNames.get( componentIndex ).length;
    }


    public int getComponentValues( int componentIndex, String[] values )
        throws FieldmlException
    {
        if( ( componentIndex < 0 ) || ( componentIndex >= fieldNames.size() ) )
        {
            throw new BadFieldmlParameterException();
        }

        String[] sourceValues = fieldNames.get( componentIndex );

        if( values.length < sourceValues.length )
        {
            throw new BadFieldmlParameterException();
        }

        System.arraycopy( sourceValues, 0, values, 0, sourceValues.length );

        return sourceValues.length;
    }


    public ImmutableList<Domain> getSignature()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
