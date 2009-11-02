package fieldml.value;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue
{
    public int value;


    public EnsembleDomainValue( EnsembleDomain domain, int value )
    {
        super( domain );
        
        this.value = value;
    }
}
