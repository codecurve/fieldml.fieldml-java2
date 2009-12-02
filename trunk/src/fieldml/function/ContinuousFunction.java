package fieldml.function;

import fieldml.value.MeshDomainValue;

public abstract class ContinuousFunction
{
    public final String name;


    public ContinuousFunction( String name )
    {
        this.name = name;
    }


    public abstract double evaluate( MeshDomainValue value );
}
