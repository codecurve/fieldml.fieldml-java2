package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class ContinuousDomain
    extends Domain
{
    @SerializationAsString
    public final ContinuousDomain baseDomain;


    public ContinuousDomain( Region owner, String name, EnsembleDomain componentDomain )
    {
        super( name, componentDomain );
        
        this.baseDomain = null;

        owner.addDomain( this );
    }


    public ContinuousDomain( Region owner, String name, ContinuousDomain baseDomain )
    {
        super( name, baseDomain.componentDomain );
        
        this.baseDomain = baseDomain;

        owner.addDomain( this );
    }


    public ContinuousDomain( Region owner, String name )
    {
        super( name, null );
        
        this.baseDomain = null;

        owner.addDomain( this );
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
