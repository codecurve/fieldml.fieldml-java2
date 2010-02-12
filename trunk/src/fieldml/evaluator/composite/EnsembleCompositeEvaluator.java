package fieldml.evaluator.composite;

import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class EnsembleCompositeEvaluator
    extends CompositeEvaluator<EnsembleDomain, EnsembleDomainValue>
    implements EnsembleEvaluator
{
    public EnsembleCompositeEvaluator( String name, EnsembleDomain valueDomain )
    {
        super( name, valueDomain );
    }


    @Override
    protected EnsembleDomainValue onComposition( DomainValues localValues )
    {
        return localValues.get( valueDomain );
    }
}
