package fieldml.field;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.AbstractEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class PiecewiseField
    extends AbstractEvaluator<ContinuousDomain, ContinuousDomainValue>
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final PiecewiseTemplate template;

    public final SimpleMap<String, ContinuousEvaluator> variables;


    public PiecewiseField( String name, ContinuousDomain valueDomain, PiecewiseTemplate template )
    {
        super( name, valueDomain );

        this.template = template;
        
        variables = new SimpleMap<String, ContinuousEvaluator>();
    }


    public void setVariable( String name, ContinuousEvaluator evaluator )
    {
        variables.put( name, evaluator );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<String, ContinuousEvaluator> e : variables )
        {
            localContext.setVariable( e.key, e.value );
        }
        return valueDomain.makeValue( template.evaluate( localContext ) );
    }
}
