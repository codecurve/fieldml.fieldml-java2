package fieldmlx.evaluator;

import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class EnsembleCompositeEvaluator
    extends EnsembleEvaluator
{
    public final CompositionEvaluator<EnsembleDomain, EnsembleDomainValue> composer;


    public EnsembleCompositeEvaluator( String name, EnsembleDomain valueDomain )
    {
        super( name, valueDomain );

        composer = new CompositionEvaluator<EnsembleDomain, EnsembleDomainValue>();
    }


    public void importValue( Domain domain, DomainValue<?> value )
    {
        composer.importValue( domain, value );
    }


    @Override
    public EnsembleDomainValue getValue( DomainValues context )
    {
        return composer.evaluate( context ).get( valueDomain );
    }
}
