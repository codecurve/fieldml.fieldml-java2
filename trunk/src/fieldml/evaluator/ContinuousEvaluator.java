package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public interface ContinuousEvaluator
{
    public String getName();


    public ContinuousDomainValue evaluate( DomainValues context );
    
    
    public ContinuousDomain getValueDomain();
}
