package fieldml.field;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.annotations.SerializationAsString;
import fieldmlx.util.SimpleMap;
import fieldmlx.util.SimpleMapEntry;

public class PiecewiseField
    extends ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousEvaluator template;

    public final SimpleMap<String, ContinuousEvaluator> variables;


    public PiecewiseField( String name, ContinuousDomain valueDomain, ContinuousEvaluator template )
    {
        super( name, valueDomain );

        assert valueDomain == template.valueDomain;
        
        this.template = template;

        variables = new SimpleMap<String, ContinuousEvaluator>();
    }


    public void setVariable( String name, ContinuousEvaluator evaluator )
    {
        variables.put( name, evaluator );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<String, ContinuousEvaluator> e : variables )
        {
            localContext.setVariable( e.key, e.value );
        }
        return template.getValue( localContext );
    }
}
