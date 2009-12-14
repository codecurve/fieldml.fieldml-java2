package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.field.PiecewiseTemplate;
import fieldml.util.SimpleMap;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class PiecewiseField
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final PiecewiseTemplate template;

    public final SimpleMap<ContinuousDomain, ContinuousEvaluator> dofEvaluators;


    public PiecewiseField( String name, ContinuousDomain valueDomain, PiecewiseTemplate template )
    {
        super( name, valueDomain );

        this.template = template;

        dofEvaluators = new SimpleMap<ContinuousDomain, ContinuousEvaluator>();
    }


    public void setDofs( ContinuousDomain domain, ContinuousEvaluator evaluator )
    {
        dofEvaluators.put( domain, evaluator );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        return valueDomain.makeValue( template.evaluate( context, dofEvaluators ) );
    }
}
