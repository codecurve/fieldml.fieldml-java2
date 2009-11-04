package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializeToString;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValue;
import fieldml.value.EnsembleDomainValue;

public class MappingField<D extends DomainValue>
    extends Field<D>
{
    @SerializeToString
    public EnsembleDomain[] parameterDomains;

    public class MapEntry
    {
        public D value;

        public int[] keys;


        private MapEntry( D value, int[] keys )
        {
            this.value = value;
            this.keys = keys;
        }


        private boolean match( int[] values )
        {
            for( int i = 0; i < values.length; i++ )
            {
                if( keys[i] != values[i] )
                {
                    return false;
                }
            }

            return true;
        }


        public boolean match( DomainValue[] values )
        {
            for( int i = 0; i < values.length; i++ )
            {
                if( parameterDomains[i] != values[i].domain )
                {
                    return false;
                }
                //TODO Icky
                if( keys[i] != ((EnsembleDomainValue)values[i]).indexValue )
                {
                    return false;
                }
            }

            return true;
        }
    }

    public final List<MapEntry> entries;


    public MappingField( String name, Domain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name, valueDomain );

        this.parameterDomains = parameterDomains;

        entries = new ArrayList<MapEntry>();
    }


    public void setValue( D value, int... keys )
    {
        entries.add( new MapEntry( value, keys ) );
    }


    public D evaluate( int... values )
    {
        for( MapEntry m : entries )
        {
            if( m.match( values ) )
            {
                return m.value;
            }
        }

        return null;
    }


    public D evaluate( DomainValue... values )
    {
        for( MapEntry m : entries )
        {
            if( m.match( values ) )
            {
                return m.value;
            }
        }

        return null;
    }
}
