package fieldml.evaluator;

import fieldml.domain.MeshDomain;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public interface MeshEvaluator
{
    public String getName();


    public MeshDomainValue evaluate( DomainValues context );
    
    
    public MeshDomain getValueDomain();
}
