package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldmlx.evaluator.CompositionEvaluator;

public class ContinuousCompositeEvaluator
    extends AbstractContinuousEvaluator
{
    public final CompositionEvaluator<ContinuousDomain, ContinuousDomainValue> composer;


    public ContinuousCompositeEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );

        composer = new CompositionEvaluator<ContinuousDomain, ContinuousDomainValue>();
    }


    public void importValue( EnsembleDomainValue value )
    {
        composer.importValue( value );
    }

    
    public <D extends Domain> void importField( AbstractEvaluator<D, ? extends DomainValue<D>> evaluator, D domain )
    {
        composer.importField( evaluator, domain );
    }

    public <D extends Domain> void importField( AbstractEvaluator<D, ? extends DomainValue<D>> evaluator )
    {
        importField( evaluator, evaluator.valueDomain );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        DomainValues compositionContext = composer.evaluate( context );
        return compositionContext.get( valueDomain );
    }
}
