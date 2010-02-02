package fieldml.evaluator;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousVariableEvaluator
    implements ContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousDomain valueDomain;

    public final String name;


    public ContinuousVariableEvaluator( String name, ContinuousDomain valueDomain )
    {
        this.name = name;
        this.valueDomain = valueDomain;

    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        ContinuousEvaluator variable = context.getContinuousVariable( name );
        
        return variable.evaluate( context );
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public ContinuousDomain getValueDomain()
    {
        return valueDomain;
    }

}
