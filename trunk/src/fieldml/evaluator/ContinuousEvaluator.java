package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public interface ContinuousEvaluator
    extends Evaluator<ContinuousDomain>
{
    public ContinuousDomainValue evaluate( DomainValues context );
}
