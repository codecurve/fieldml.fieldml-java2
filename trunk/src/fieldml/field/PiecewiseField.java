package fieldml.field;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class PiecewiseField
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final PiecewiseTemplate template;

    @SerializationAsString
    public final ContinuousEvaluator[] dofs;


    public PiecewiseField( String name, ContinuousDomain valueDomain, PiecewiseTemplate template )
    {
        super( name, valueDomain );

        this.template = template;

        dofs = new ContinuousEvaluator[template.totalDofSets];
    }


    public void setDofs( int index, ContinuousEvaluator evaluator )
    {
        dofs[index - 1] = evaluator;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        return valueDomain.makeValue( template.evaluate( context, dofs ) );
    }
}
