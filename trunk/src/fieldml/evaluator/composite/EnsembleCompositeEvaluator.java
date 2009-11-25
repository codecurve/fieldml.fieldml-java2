package fieldml.evaluator.composite;

import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class EnsembleCompositeEvaluator
    extends CompositeEvaluator<EnsembleDomain, EnsembleDomainValue>
{
    public EnsembleCompositeEvaluator( String name, EnsembleDomain valueDomain, Domain[] parameterDomains )
    {
        super( name, valueDomain, parameterDomains );
    }


    @Override
    public EnsembleDomainValue evaluate( DomainValues input )
    {
        DomainValues localValues = new DomainValues( input );

        apply( localValues );

        return localValues.get( valueDomain );
    }
}
