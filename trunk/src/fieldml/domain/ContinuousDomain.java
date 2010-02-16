package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.value.ContinuousDomainValue;

public class ContinuousDomain
    extends Domain
{
    public final int componentCount;

    @SerializationAsString
    public final ContinuousDomain componentDomain;


    public ContinuousDomain( String name )
    {
        super( name );

        componentCount = 1;
        componentDomain = this;
    }


    public ContinuousDomain( String name, ContinuousDomain itemDomain, int componentCount )
    {
        super( name );

        this.componentCount = componentCount;
        this.componentDomain = itemDomain;
    }


    public ContinuousDomain( String name, int componentCount )
    {
        super( name );

        this.componentCount = componentCount;
        this.componentDomain = this;
    }


    public ContinuousDomain( String name, ContinuousDomain itemDomain )
    {
        // TODO Eek! Magic number 0 means 'no limit'
        this( name, itemDomain, 0 );
    }


    public ContinuousDomainValue makeValue( double... values )
    {
        if( componentCount != 0 )
        {
            assert values.length == componentCount;
        }

        return new ContinuousDomainValue( this, values );
    }
}
