package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.region.Region;
import fieldml.value.EnsembleDomainValue;

public class EnsembleDomain
    extends Domain
{
    public final EnsembleBounds bounds;

    @SerializationAsString
    public final EnsembleDomain baseDomain;


    public EnsembleDomain( Region owner, String name, EnsembleDomain componentDomain, EnsembleDomain baseDomain )
    {
        super( name, componentDomain );

        assert baseDomain.componentCount == 1;

        this.bounds = baseDomain.bounds;
        this.baseDomain = baseDomain;
        
        owner.addDomain( this );
    }


    public EnsembleDomain( Region owner, String name, EnsembleBounds bounds )
    {
        super( name, null );

        this.bounds = bounds;
        this.baseDomain = this;
        
        owner.addDomain( this );
    }


    public EnsembleDomain( Region owner, String name, int... values )
    {
        this( owner, name, new ArbitraryEnsembleBounds( values ) );
    }


    public EnsembleDomain( Region owner, String name, int valueCount )
    {
        this( owner, name, new ContiguousEnsembleBounds( valueCount ) );
    }


    public int getValueCount()
    {
        // MUSTDO Audit
        return bounds.getValueCount();
    }


    public EnsembleDomainValue makeValue( int... indexValues )
    {
        return new EnsembleDomainValue( this, indexValues );
    }
}
