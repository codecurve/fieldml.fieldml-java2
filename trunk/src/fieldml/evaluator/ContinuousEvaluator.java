package fieldml.evaluator;

import fieldml.domain.MeshDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public interface ContinuousEvaluator
{
    public String getName();


    public ContinuousDomainValue evaluate( DomainValues input );


    public ContinuousDomainValue evaluate( MeshDomain domain, int index, double... chartValues );
}
