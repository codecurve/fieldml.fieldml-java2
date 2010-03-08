package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.value.EnsembleDomainValue;

public abstract class AbstractEnsembleEvaluator
    extends AbstractEvaluator<EnsembleDomain, EnsembleDomainValue>
    implements EnsembleEvaluator
{
    @SerializationAsString
    public AbstractEnsembleEvaluator fallback;


    public AbstractEnsembleEvaluator( String name, EnsembleDomain valueDomain )
    {
        super( name, valueDomain );
    }


    public void setFallback( AbstractEnsembleEvaluator fallback )
    {
        this.fallback = fallback;
    }
}
