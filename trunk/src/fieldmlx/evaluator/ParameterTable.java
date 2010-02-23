package fieldmlx.evaluator;

import java.util.ArrayList;
import java.util.Arrays;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class ParameterTable<V extends DomainValue<?>>
{
    private EnsembleDomain[] parameterDomains;

    private V defaultValue;

    private final ArrayList<int[]> keys;

    private final ArrayList<V> values;


    public ParameterTable( EnsembleDomain... parameterDomains )
    {
        this.parameterDomains = parameterDomains;
        for( EnsembleDomain e : parameterDomains )
        {
            assert e.componentDomain == null : "Parameter table index domain " + e + " must be scalar";
        }

        keys = new ArrayList<int[]>();
        values = new ArrayList<V>();
    }


    public void setDefaultValue( V value )
    {
        defaultValue = value;
    }


    public void setValue( int index, V value )
    {
        int[] indexes = new int[1];
        indexes[0] = index;

        setValue( indexes, value );
    }


    public void setValue( int[] indexes, V value )
    {
        keys.add( Arrays.copyOf( indexes, indexes.length ) );
        values.add( value );
    }


    public V evaluate( DomainValues context )
    {
        int[] indexes = new int[parameterDomains.length];

        EnsembleDomainValue index;
        for( int i = 0; i < parameterDomains.length; i++ )
        {
            index = context.get( parameterDomains[i] );
            assert index != null : "Index " + parameterDomains[i] + " is missing";
            indexes[i] = index.values[0];
        }

        V value = null;
        for( int i = 0; i < keys.size(); i++ )
        {
            int[] test = keys.get( i );
            if( Arrays.equals( test, indexes ) )
            {
                value = values.get( i );
                break;
            }
        }

        if( value == null )
        {
            value = defaultValue;
        }

        return value;
    }


    public int parameterCount()
    {
        return parameterDomains.length;
    }
}
