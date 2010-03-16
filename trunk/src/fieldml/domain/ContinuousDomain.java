package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.region.Region;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;

public class ContinuousDomain
    extends Domain
    implements ContinuousValueSource
{
    @SerializationAsString
    public final ContinuousDomain baseDomain;

    private ContinuousDomainValue clientValue;


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


    public void setValue( double... values )
    {
        clientValue = makeValue( values );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        return clientValue;
    }


    @Override
    public ContinuousDomain getValueDomain()
    {
        return this;
    }
}
