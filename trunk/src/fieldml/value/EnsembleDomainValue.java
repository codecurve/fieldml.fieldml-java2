package fieldml.value;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue<EnsembleDomain>
{
    public int indexValue;


    private EnsembleDomainValue( EnsembleDomain domain, int indexValue )
    {
        super( domain );

        this.indexValue = indexValue;
    }


    public static EnsembleDomainValue makeValue( EnsembleDomain domain, int indexValue )
    {
        return new EnsembleDomainValue( domain, indexValue );
    }


    @Override
    public String toString()
    {
        return "" + indexValue;
    }
}
