package fieldml.domain;

import fieldml.value.ContinuousDomainValue;

public class ContinuousDomain
    extends Domain
{
    public ContinuousDomain( String name, EnsembleDomain componentDomain )
    {
        super( name, componentDomain );
    }


    public ContinuousDomain( String name )
    {
        super( name, null );
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
