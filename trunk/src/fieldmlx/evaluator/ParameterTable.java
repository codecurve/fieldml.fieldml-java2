package fieldmlx.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import fieldml.domain.EnsembleDomain;
import fieldml.util.SimpleMapEntry;
import fieldml.value.DomainValue;

public class ParameterTable<V extends DomainValue<?>>
    implements Iterable<SimpleMapEntry<int[], V>>
{
    private EnsembleDomain[] parameterDomains;

    private V defaultValue;

    private final ArrayList<int[]> keys;

    private final ArrayList<V> values;

    private class TableIterator
        implements Iterator<SimpleMapEntry<int[], V>>
    {
        private int currentIndex;
        private SimpleMapEntry<int[], V> nextEntry;


        private TableIterator()
        {
            currentIndex = 0;
            updateNextEntry();
        }


        private void updateNextEntry()
        {
            if( currentIndex >= keys.size() )
            {
                nextEntry = null;
            }
            else
            {
                nextEntry = new SimpleMapEntry<int[], V>( keys.get( currentIndex ), values.get( currentIndex ) );
            }
        }


        @Override
        public boolean hasNext()
        {
            return nextEntry != null;
        }


        @Override
        public SimpleMapEntry<int[], V> next()
        {
            SimpleMapEntry<int[], V> entry = nextEntry;
            currentIndex++;
            updateNextEntry();

            return entry;
        }


        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


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


    public V evaluate( int[] indexes )
    {
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


    @Override
    public Iterator<SimpleMapEntry<int[], V>> iterator()
    {
        return new TableIterator();
    }
}
