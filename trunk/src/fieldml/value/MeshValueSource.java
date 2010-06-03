package fieldml.value;

import fieldml.domain.MeshDomain;

public interface MeshValueSource
{
    public MeshDomain getValueDomain();
    
    
    public MeshDomainValue getValue( DomainValues context );
    
    
    public MeshDomainValue getValue( DomainValues context, MeshDomain domain );
}
