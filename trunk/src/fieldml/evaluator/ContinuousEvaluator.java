package fieldml.evaluator;

import java.util.HashMap;
import java.util.Map;

import fieldml.value.MeshDomainValue;

public abstract class ContinuousEvaluator
    extends Evaluator
{
    public static final Map<String, ContinuousEvaluator> evaluators = new HashMap<String, ContinuousEvaluator>();


    public ContinuousEvaluator( String name )
    {
        super( name );

        evaluators.put( name, this );
    }


    public abstract double evaluate( MeshDomainValue value );
}
