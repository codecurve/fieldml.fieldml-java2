package fieldml.domain;

import fieldml.value.EnsembleDomainValue;

public class SimpleEnsembleDomain
    extends EnsembleDomain
{
    public SimpleEnsembleDomain( String name )
    {
        super( name );
    }


    public EnsembleDomainValue getValue( int indexValue )
    {
        return new EnsembleDomainValue( this, indexValue );
    }


    @Override
    public EnsembleDomainValue getValue( int indexValue, double... chartValues )
    {
        return getValue( indexValue );
    }
}
