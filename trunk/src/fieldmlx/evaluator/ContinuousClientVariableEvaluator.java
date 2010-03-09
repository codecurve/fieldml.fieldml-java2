package fieldmlx.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.AbstractContinuousEvaluator;
import fieldml.evaluator.Evaluator;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class ContinuousClientVariableEvaluator
    extends AbstractContinuousEvaluator
{
    public ContinuousClientVariableEvaluator( String name, ContinuousDomain valueDomain )
    {
        super( name, valueDomain );
    }

    private ContinuousDomainValue value;


    public void setValue( double... values )
    {
        value = valueDomain.makeValue( values );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        return value;
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        return new ArrayList<Evaluator<?>>();
    }
}