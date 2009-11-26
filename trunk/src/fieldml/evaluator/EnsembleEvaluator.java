package fieldml.evaluator;

import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public interface EnsembleEvaluator
{
    public String getName();


    public EnsembleDomainValue evaluate( DomainValues input );
}
