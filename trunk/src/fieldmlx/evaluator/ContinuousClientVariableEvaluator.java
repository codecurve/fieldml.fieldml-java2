package fieldmlx.evaluator;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousClientVariableEvaluator
    extends ContinuousEvaluator
{
    public ContinuousClientVariableEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }

    private ContinuousDomainValue value;
    
    public void setValue( double ... values )
    {
        value = valueDomain.makeValue( values );
    }
    
    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        return value;
    }
}
