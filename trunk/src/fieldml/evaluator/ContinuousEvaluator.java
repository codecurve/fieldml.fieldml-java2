package fieldml.evaluator;

import fieldml.value.MeshDomainValue;

public abstract class ContinuousEvaluator
{
    public final String name;


    public ContinuousEvaluator( String name )
    {
        this.name = name;
    }


    public abstract double evaluate( MeshDomainValue value );
}
