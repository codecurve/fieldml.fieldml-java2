package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldmlx.evaluator.CompositionEvaluator;

public class ContinuousCompositeEvaluator
    extends ContinuousEvaluator
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

    
    public void importField( AbstractEvaluator<?, ?> field )
    {
        composer.importField( field );
    }

    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        DomainValues compositionContext = composer.evaluate( context );
        return compositionContext.get( valueDomain );
    }
}
