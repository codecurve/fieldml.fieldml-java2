package fieldml.domain;


public abstract class EnsembleDomainComponent
    extends DomainComponent
{
    public EnsembleDomainComponent( String name )
    {
        super( name );
    }


    public abstract Iterable<Integer> getValues();
}
