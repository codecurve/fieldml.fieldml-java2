package fieldml.domain;

import fieldml.value.DomainValue;
import fieldml.value.EnsembleDomainValue;

public class SimpleEnsembleDomain
    extends EnsembleDomain
{
    public SimpleEnsembleDomain( String name )
    {
        super( name );
    }


    public DomainValue getValue( int value )
    {
        return new EnsembleDomainValue( this, value );
    }
}
