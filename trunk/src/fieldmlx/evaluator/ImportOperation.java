package fieldmlx.evaluator;

import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.value.DomainValue;
import fieldml.value.DomainValues;
import fieldmlx.annotations.SerializationAsString;

public  class ImportOperation<D extends Domain, V extends DomainValue<D>>
    implements CompositionOperation
{
    @SerializationAsString
    public final AbstractEvaluator<D, V> evaluator;
    
    @SerializationAsString
    public final D domain;
    
    
    @SerializationAsString
    public final EnsembleDomain indexDomain;


    public ImportOperation( AbstractEvaluator<D, V> evaluator, D domain, EnsembleDomain indexDomain )
    {
        this.evaluator = evaluator;
        this.domain = domain;
        this.indexDomain = indexDomain;
    }

    public ImportOperation( AbstractEvaluator<D, V> evaluator, D domain )
    {
        this.evaluator = evaluator;
        this.domain = domain;
        this.indexDomain = null;
    }

    public ImportOperation( AbstractEvaluator<D, V> evaluator )
    {
        this.evaluator = evaluator;
        this.domain = evaluator.valueDomain;
        this.indexDomain = null;
    }


    @Override
    public void perform( DomainValues context )
    {
        V v = evaluator.getValue( context, domain, indexDomain );
        if( v != null )
        {
            context.set( domain, v );
        }
    }
}
