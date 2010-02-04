package fieldml.evaluator.composite;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class ContinuousCompositeEvaluator
    extends CompositeEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    public ContinuousCompositeEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }


    public void importValue( EnsembleDomainValue value )
    {
        operations.add( new ValueOperation( value ) );
    }


    @Override
    protected ContinuousDomainValue onComposition( DomainValues localValues )
    {
        return localValues.get( valueDomain );
    }
}
