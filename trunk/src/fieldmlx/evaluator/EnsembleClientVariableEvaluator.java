package fieldmlx.evaluator;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.domain.EnsembleDomain;
import fieldml.evaluator.AbstractEnsembleEvaluator;
import fieldml.evaluator.Evaluator;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class EnsembleClientVariableEvaluator
    extends AbstractEnsembleEvaluator
{
    public EnsembleClientVariableEvaluator( String name, EnsembleDomain valueDomain )
    {
        super( name, valueDomain );
    }

    private EnsembleDomainValue value;


    public void setValue( int... values )
    {
        value = valueDomain.makeValue( values );
    }


    @Override
    public EnsembleDomainValue evaluate( DomainValues context )
    {
        return value;
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        return new ArrayList<Evaluator<?>>();
    }
}
