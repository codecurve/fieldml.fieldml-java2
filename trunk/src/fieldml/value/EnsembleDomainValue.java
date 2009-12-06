package fieldml.value;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue<EnsembleDomain>
{
    public final int indexValue;


    public EnsembleDomainValue( EnsembleDomain domain, int indexValue )
    {
        super( domain );

        this.indexValue = indexValue;
    }


    @Override
    public String toString()
    {
        return "" + indexValue;
    }
}
