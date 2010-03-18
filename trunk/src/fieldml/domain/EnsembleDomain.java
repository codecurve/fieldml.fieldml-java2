package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.region.Region;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.EnsembleValueSource;

public class EnsembleDomain
    extends Domain
    implements EnsembleValueSource
{
    public final EnsembleBounds bounds;

    @SerializationAsString
    public final EnsembleDomain superDomain;

    @SerializationAsString
    public final EnsembleDomain baseDomain;


    public EnsembleDomain( Region owner, String name, EnsembleDomain componentDomain, EnsembleDomain baseDomain )
    {
        super( name, componentDomain );

        assert baseDomain.componentCount == 1;

        this.bounds = baseDomain.bounds;
        this.superDomain = baseDomain.superDomain;
        this.baseDomain = baseDomain;

        owner.addDomain( this );
    }


    public EnsembleDomain( Region owner, String name, EnsembleDomain superDomain, EnsembleBounds bounds )
    {
        super( name, null );

        this.bounds = bounds;
        this.superDomain = superDomain;
        this.baseDomain = this;

        owner.addDomain( this );
    }


    public EnsembleDomain( Region owner, String name, EnsembleDomain superDomain, int... values )
    {
        this( owner, name, superDomain, new ArbitraryEnsembleBounds( values ) );
    }


    public EnsembleDomain( Region owner, String name, EnsembleDomain superDomain, int valueCount )
    {
        this( owner, name, superDomain, new ContiguousEnsembleBounds( valueCount ) );
    }


    public int getValueCount()
    {
        // MUSTDO Audit
        return bounds.getValueCount();
    }


    public EnsembleDomainValue makeValue( int... indexValues )
    {
        return new EnsembleDomainValue( indexValues );
    }


    @Override
    public EnsembleDomainValue getValue( DomainValues context )
    {
        return context.get( this );
    }


    public final EnsembleDomainValue getValue( DomainValues context, EnsembleDomain domain )
    {
        if( domain != this )
        {
            return null;
        }

        return getValue( context );
    }


    @Override
    public EnsembleDomain getValueDomain()
    {
        return this;
    }
}
