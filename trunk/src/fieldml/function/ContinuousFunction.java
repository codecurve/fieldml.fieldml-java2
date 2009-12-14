package fieldml.function;

import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.util.SimpleMap;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public abstract class ContinuousFunction
{
    public final String name;


    public ContinuousFunction( String name )
    {
        this.name = name;
    }


    public abstract double evaluate( DomainValues context, MeshDomainValue meshLocation,
        SimpleMap<ContinuousDomain, ContinuousEvaluator> dofEvaluators );
}
