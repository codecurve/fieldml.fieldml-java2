package fieldml.value;

import fieldml.domain.EnsembleDomain;

public interface EnsembleValueSource
{
    public EnsembleDomain getValueDomain();
    
    
    public EnsembleDomainValue getValue( DomainValues context );
    
    
    public EnsembleDomainValue getValue( DomainValues context, EnsembleDomain domain );
}
