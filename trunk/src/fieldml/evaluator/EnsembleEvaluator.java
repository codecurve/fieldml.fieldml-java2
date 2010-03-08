package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public interface EnsembleEvaluator
    extends Evaluator<EnsembleDomain>
{
    public EnsembleDomainValue evaluate( DomainValues context );


    public EnsembleDomainValue evaluate( DomainValues context, EnsembleDomain domain );
}
