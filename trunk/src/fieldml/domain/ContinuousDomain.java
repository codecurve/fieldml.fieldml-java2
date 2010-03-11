package fieldml.domain;

import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;

public class ContinuousDomain
    extends Domain
{
    public ContinuousDomain( Region owner, String name, EnsembleDomain componentDomain )
    {
        super( name, componentDomain );
        
        owner.addDomain( this );
    }


    public ContinuousDomain( Region owner, String name )
    {
        this( owner, name, null );
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
