package fieldml.evaluator;

import fieldml.value.MeshDomainValue;

public abstract class Evaluator
{
    public abstract double evaluate( MeshDomainValue value );
}
