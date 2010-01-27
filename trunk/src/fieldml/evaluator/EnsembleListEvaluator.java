package fieldml.evaluator;

import fieldml.domain.EnsembleListDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleListDomainValue;

public interface EnsembleListEvaluator
{
    public String getName();


    public EnsembleListDomainValue evaluate( DomainValues input );


    public EnsembleListDomain getValueDomain();
}
