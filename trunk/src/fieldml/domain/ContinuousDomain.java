package fieldml.domain;

import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;

public class ContinuousDomain
    extends Domain
    implements ContinuousValueSource
{
    public ContinuousDomain( Region owner, String name, EnsembleDomain componentDomain )
    {
        super( name, componentDomain );

        owner.addDomain( this );
    }


    public ContinuousDomain( Region owner, String name )
    {
        super( name, null );

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


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        return context.get( this );
    }


    public final ContinuousDomainValue getValue( DomainValues context, ContinuousDomain domain )
    {
        return getValue( context );
    }


    @Override
    public ContinuousDomain getValueDomain()
    {
        return this;
    }
}
