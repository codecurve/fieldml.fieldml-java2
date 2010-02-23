package fieldml.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousVariableEvaluator
    extends ContinuousEvaluator
{
    public ContinuousVariableEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain ); 
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        ContinuousEvaluator variable = context.getContinuousVariable( name );

        return variable.evaluate( context );
    }
}
