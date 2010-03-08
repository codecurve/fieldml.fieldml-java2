package fieldml.value;

import java.util.HashMap;
import java.util.Map;

import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.MeshEvaluator;

public class DomainValues
{
    private final Map<String, ContinuousEvaluator> continuousVariables;

    private final Map<String, EnsembleEvaluator> ensembleVariables;

    private final Map<String, MeshEvaluator> meshVariables;


    public DomainValues()
    {
        continuousVariables = new HashMap<String, ContinuousEvaluator>();
        ensembleVariables = new HashMap<String, EnsembleEvaluator>();
        meshVariables = new HashMap<String, MeshEvaluator>();
    }


    public DomainValues( DomainValues input )
    {
        this();

        for( String name : input.continuousVariables.keySet() )
        {
            continuousVariables.put( name, input.continuousVariables.get( name ) );
        }
        for( String name : input.ensembleVariables.keySet() )
        {
            ensembleVariables.put( name, input.ensembleVariables.get( name ) );
        }
        for( String name : input.meshVariables.keySet() )
        {
            meshVariables.put( name, input.meshVariables.get( name ) );
        }
    }


    public void setVariable( String name, ContinuousEvaluator evaluator )
    {
        continuousVariables.put( name, evaluator );
    }


    public void setVariable( String name, EnsembleEvaluator evaluator )
    {
        ensembleVariables.put( name, evaluator );
    }


    public void setVariable( String name, MeshEvaluator evaluator )
    {
        meshVariables.put( name, evaluator );
    }


    public ContinuousEvaluator getContinuousVariable( String name )
    {
        return continuousVariables.get( name );
    }


    public EnsembleEvaluator getEnsembleVariable( String name )
    {
        return ensembleVariables.get( name );
    }


    public MeshEvaluator getMeshVariable( String name )
    {
        return meshVariables.get( name );
    }
}
