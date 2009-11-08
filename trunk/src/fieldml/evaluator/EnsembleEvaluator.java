package fieldml.evaluator;

import java.util.HashMap;
import java.util.Map;

import fieldml.value.EnsembleDomainValue;
import fieldml.value.MeshDomainValue;

public abstract class EnsembleEvaluator
    extends Evaluator
{
    public static final Map<String, EnsembleEvaluator> evaluators = new HashMap<String, EnsembleEvaluator>();


    public EnsembleEvaluator( String name )
    {
        super( name );

        evaluators.put( name, this );
    }


    public abstract EnsembleDomainValue evaluate( MeshDomainValue value );
}
