package fieldml.evaluator;

import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldmlx.evaluator.CompositionEvaluator;

public class EnsembleCompositeEvaluator
    extends EnsembleEvaluator
{
    public final CompositionEvaluator<EnsembleDomain, EnsembleDomainValue> composer;


    public EnsembleCompositeEvaluator( String name, EnsembleDomain valueDomain )
    {
        super( name, valueDomain );

        composer = new CompositionEvaluator<EnsembleDomain, EnsembleDomainValue>();
    }


    public void importValue( EnsembleDomainValue value )
    {
        composer.importValue( value );
    }


    @Override
    public EnsembleDomainValue evaluate( DomainValues context )
    {
        return composer.evaluate( context ).get( valueDomain );
    }
}
