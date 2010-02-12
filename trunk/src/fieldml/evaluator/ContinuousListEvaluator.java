package fieldml.evaluator;

import fieldml.domain.ContinuousListDomain;
import fieldml.value.ContinuousListDomainValue;
import fieldml.value.DomainValues;

public interface ContinuousListEvaluator
{
    public String getName();


    public ContinuousListDomainValue evaluate( DomainValues context );
    
    
    public ContinuousListDomain getValueDomain();
}
