package fieldmlx.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.Domain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;

public  class ImportOperation<D extends Domain, V extends DomainValue<D>>
    implements CompositionOperation
{
    @SerializationAsString
    public final AbstractEvaluator<D, V> evaluator;
    
    @SerializationAsString
    public final D domain;


    public ImportOperation( AbstractEvaluator<D, V> evaluator, D domain )
    {
        this.evaluator = evaluator;
        this.domain = domain;
    }

    public ImportOperation( AbstractEvaluator<D, V> evaluator )
    {
        this.evaluator = evaluator;
        this.domain = evaluator.valueDomain;
    }


    @Override
    public void perform( DomainValues context )
    {
        V v = evaluator.evaluate( context, domain );
        if( v != null )
        {
            context.set( v );
        }
    }
}
