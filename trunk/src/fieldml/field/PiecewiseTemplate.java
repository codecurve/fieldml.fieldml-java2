package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.function.ContinuousFunction;
import fieldml.util.SimpleMap;
import fieldml.value.DomainValues;
import fieldml.value.MeshDomainValue;

public class PiecewiseTemplate
{
    public final String name;

    @SerializationAsString
    public final MeshDomain meshDomain;

    // NOTE This should be a map, but a list makes serialization much nicer.
    public final List<ContinuousFunction> functions;

    public final SimpleMap<Integer, String> elementFunctions;


    public PiecewiseTemplate( String name, MeshDomain meshDomain )
    {
        this.name = name;
        this.meshDomain = meshDomain;

        functions = new ArrayList<ContinuousFunction>();
        elementFunctions = new SimpleMap<Integer, String>();
    }


    public void addFunction( ContinuousFunction function )
    {
        functions.add( function );
    }


    public void setFunction( int element, String functionName )
    {
        elementFunctions.put( element, functionName );
    }


    private ContinuousFunction getFunction( String name )
    {
        for( ContinuousFunction f : functions )
        {
            if( f.name.equals( name ) )
            {
                return f;
            }
        }

        return null;
    }


    public double evaluate( DomainValues context, SimpleMap<ContinuousDomain, ContinuousEvaluator> dofEvaluators )
    {
        MeshDomainValue v = context.get( meshDomain );

        final String evaluatorName = elementFunctions.get( v.indexValue );
        ContinuousFunction function = getFunction( evaluatorName );

        if( function != null )
        {
            return function.evaluate( context, v, dofEvaluators );
        }

        assert false;

        return 0;
    }


    @Override
    public String toString()
    {
        return name;
    }
}
