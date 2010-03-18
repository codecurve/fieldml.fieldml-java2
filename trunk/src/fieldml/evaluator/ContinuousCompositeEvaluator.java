package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleValueSource;
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


    public void alias( Domain domain, DomainValue<?> value )
    {
        composer.importValue( domain, value );
    }


    public void alias( ContinuousValueSource source, ContinuousDomain destination )
    {
        composer.aliasValue( source, destination );
    }


    public void alias( EnsembleValueSource source, EnsembleDomain destination )
    {
        composer.aliasValue( source, destination );
    }


    public <D extends Domain> void importField( AbstractEvaluator<D, ? extends DomainValue<D>> evaluator, D domain,
        EnsembleDomain indexDomain )
    {
//        assert domain.componentDomain == indexDomain.componentDomain;
        
        composer.importField( evaluator, domain, indexDomain );
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
    public ContinuousDomainValue getValue( DomainValues context )
    {
        DomainValues compositionContext = composer.evaluate( context );
        return compositionContext.get( valueDomain );
    }
}
