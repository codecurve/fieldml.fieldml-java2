package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public abstract class AbstractContinuousEvaluator
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public ContinuousEvaluator fallback;


    public AbstractContinuousEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }


    public void setFallback( ContinuousEvaluator fallback )
    {
        this.fallback = fallback;
    }


    @Override
    public abstract ContinuousDomainValue evaluate( DomainValues context );
}
