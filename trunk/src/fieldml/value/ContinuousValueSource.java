package fieldml.value;

import fieldml.domain.ContinuousDomain;

public interface ContinuousValueSource
{
    public ContinuousDomain getValueDomain();
    
    
    public ContinuousDomainValue getValue( DomainValues context );
}
