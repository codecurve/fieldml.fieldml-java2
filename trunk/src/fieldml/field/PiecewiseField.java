package fieldml.field;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
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


    public void addDofs( ContinuousEvaluator evaluator )
    {
        //Can only have one set of dofs on any given domain.
        assert dofEvaluators.get( evaluator.getValueDomain() ) == null; 
        
        dofEvaluators.put( evaluator.getValueDomain(), evaluator );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        return valueDomain.makeValue( template.evaluate( context, dofEvaluators ) );
    }
}
