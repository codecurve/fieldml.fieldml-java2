package fieldml.evaluator;

import fieldml.domain.ContinuousListDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;

public interface ContinuousListEvaluator
{
    public String getName();


    public ContinuousListDomainValue evaluate( DomainValues context );


    public ContinuousListDomainValue evaluate( EnsembleDomain domain, int index );


    public ContinuousListDomainValue evaluate( MeshDomain domain, int index, double... chartValues );
    
    
    public ContinuousListDomain getValueDomain();
}
