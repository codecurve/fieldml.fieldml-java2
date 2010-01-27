package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public interface ContinuousEvaluator
{
    public String getName();


    public ContinuousDomainValue evaluate( DomainValues context );


    public ContinuousDomainValue evaluate( EnsembleDomain domain, int index );


    public ContinuousDomainValue evaluate( MeshDomain domain, int index, double... chartValues );
    
    
    public ContinuousDomain getValueDomain();
}
