package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.Domain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldmlx.annotations.SerializationAsString;

public class ContinuousVariableEvaluator
    extends ContinuousEvaluator
{
    // This is only an advisory that the users of the variable may not work unless these dependencies exist.
    @SerializationAsString
    public Domain[] parameterDomains;


    public ContinuousVariableEvaluator( String name, ContinuousDomain valueDomain, Domain... parameterDomains )
    {
        super( name, valueDomain );

        this.parameterDomains = parameterDomains;
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        ContinuousEvaluator variable = context.getContinuousVariable( name );

        return variable.getValue( context );
    }
}
