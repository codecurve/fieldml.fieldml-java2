package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public abstract class TableEvaluator<D extends Domain, V extends DomainValue<D>>
    extends AbstractEvaluator<D, V>
{
    @SerializationAsString
    public final EnsembleDomain[] parameterDomains;
    
    @SerializationAsString
    public V defaultValue;

    public class MapEntry
    {
        public int[] keys;

        public V value;


        private MapEntry( V value, int[] keys )
        {
            this.value = value;
            this.keys = keys;
        }


        private boolean match( DomainValues values )
        {
            for( int i = 0; i < parameterDomains.length; i++ )
            {
                if( values.get( parameterDomains[i] ).values[0] != keys[i] )
                {
                    return false;
                }
            }

            return true;
        }
        
        
        @Override
        public String toString()
        {
            return "" + Arrays.toString( keys ) + " -> " + value;
        }
    }

    public final List<MapEntry> entries;


    public TableEvaluator( String name, D valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain );

        this.parameterDomains = parameterDomains;

        entries = new ArrayList<MapEntry>();
    }


    public void setValue( V value, int... keys )
    {
        entries.add( new MapEntry( value, keys ) );
    }
    
    
    public void setDefaultValue( V value )
    {
        defaultValue = value;
    }


    @Override
    public V evaluate( DomainValues values )
    {
        for( MapEntry m : entries )
        {
            if( m.match( values ) )
            {
                return m.value;
            }
        }

        return defaultValue;
    }
}
