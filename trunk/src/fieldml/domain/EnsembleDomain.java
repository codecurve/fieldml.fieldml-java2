package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.value.EnsembleDomainValue;

public class EnsembleDomain
    extends Domain
{
    public final EnsembleBounds bounds;

    @SerializationAsString
    public final EnsembleDomain baseDomain;


    public EnsembleDomain( String name, EnsembleDomain componentDomain, EnsembleDomain baseDomain )
    {
        super( name, componentDomain );

        assert baseDomain.componentCount == 1;

        this.bounds = baseDomain.bounds;
        this.baseDomain = baseDomain;
    }


    public EnsembleDomain( String name, EnsembleBounds bounds )
    {
        super( name, null );

        this.bounds = bounds;
        this.baseDomain = this;
    }


    public EnsembleDomain( String name, int... values )
    {
        this( name, new ArbitraryEnsembleBounds( values ) );
    }


    public EnsembleDomain( String name, int valueCount )
    {
        this( name, new ContiguousEnsembleBounds( valueCount ) );
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
