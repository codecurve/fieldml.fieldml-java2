package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public interface EnsembleEvaluator
{
    public String getName();


    public EnsembleDomainValue evaluate( DomainValues input );


    public EnsembleDomain getValueDomain();
}
