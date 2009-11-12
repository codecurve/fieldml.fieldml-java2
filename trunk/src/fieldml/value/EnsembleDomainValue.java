package fieldml.value;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue
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
}
