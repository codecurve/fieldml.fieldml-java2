package fieldml.value;

import fieldml.domain.Domain;

public class EnsembleDomainValue
    extends DomainValue
{
    public int indexValue;


    public EnsembleDomainValue( Domain domain, int indexValue )
    {
        super( domain );
        
        this.indexValue = indexValue;
    }
}
