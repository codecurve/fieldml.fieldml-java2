package fieldml.evaluator;

import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public interface ContinuousEvaluator
{
    public ContinuousDomainValue evaluate( DomainValues input );
}
