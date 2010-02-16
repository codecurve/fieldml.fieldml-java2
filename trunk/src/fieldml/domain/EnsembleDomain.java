package fieldml.domain;

import java.util.Arrays;

import fieldml.annotations.SerializationAsString;
import fieldml.value.EnsembleDomainValue;

public class EnsembleDomain
    extends Domain
{
    public final int[] values;

    public final int componentCount;
    
    @SerializationAsString
    public final EnsembleDomain componentDomain; 


    public EnsembleDomain( String name, int... indexValues )
    {
        super( name );

        values = indexValues;
        componentCount = 1;
        componentDomain = this;
    }


    public EnsembleDomain( String name, EnsembleDomain itemDomain, int componentCount )
    {
        super( name );

        values = Arrays.copyOf( itemDomain.values, itemDomain.values.length );
        this.componentCount = componentCount;
        this.componentDomain = itemDomain;
    }


    public EnsembleDomain( String name, EnsembleDomain itemDomain )
    {
        //TODO Eek! Magic number 0 means 'no limit'
        this( name, itemDomain, 0 );
    }


    public EnsembleDomainValue makeValue( int... indexValues )
    {
        if( componentCount != 0 )
        {
            assert indexValues.length == componentCount;
        }

        return new EnsembleDomainValue( this, indexValues );
    }


    public int getValueCount()
    {
        return values.length;
    }
}
