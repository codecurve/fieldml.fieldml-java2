package fieldml.evaluator;

import java.util.HashMap;
import java.util.Map;

import fieldml.value.MeshDomainValue;

public abstract class Evaluator
{
    public static final Map<String, Evaluator> evaluators = new HashMap<String, Evaluator>();

    public final String name;


    public Evaluator( String name )
    {
        this.name = name;

        evaluators.put( name, this );
    }


    public String toString()
    {
        return name;
    }


    public abstract double evaluate( MeshDomainValue value );
}
