package fieldml.evaluator;

import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public interface EnsembleEvaluator
{
    public EnsembleDomainValue evaluate( DomainValues input );
}
