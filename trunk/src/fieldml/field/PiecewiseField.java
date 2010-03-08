package fieldml.field;

import java.util.ArrayList;
import java.util.Collection;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.evaluator.AbstractContinuousEvaluator;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.Evaluator;
import fieldml.evaluator.MeshEvaluator;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;

public class PiecewiseField
    extends AbstractContinuousEvaluator
{
    @SerializationAsString
    public final ContinuousEvaluator template;

    public final SimpleMap<String, ContinuousEvaluator> continuousVariables;

    public final SimpleMap<String, EnsembleEvaluator> ensembleVariables;

    public final SimpleMap<String, MeshEvaluator> meshVariables;


    public PiecewiseField( String name, ContinuousDomain valueDomain, ContinuousEvaluator template )
    {
        super( name, valueDomain );

        assert valueDomain == template.getValueDomain() : "Mismatch between " + valueDomain + " and " + template.getValueDomain();

        this.template = template;

        continuousVariables = new SimpleMap<String, ContinuousEvaluator>();
        ensembleVariables = new SimpleMap<String, EnsembleEvaluator>();
        meshVariables = new SimpleMap<String, MeshEvaluator>();
    }


    public void setVariable( String name, ContinuousEvaluator evaluator )
    {
        assert !findVariable( name ) : "Variable " + name + " is already set";

        Collection<? extends Evaluator<?>> variables = getVariables();

        boolean found = false;
        for( Evaluator<?> e : variables )
        {
            found |= e.getName().equals( name );
        }

        assert found : "Variable " + name + " does not exist";
        
        continuousVariables.put( name, evaluator );
    }


    public void setVariable( String name, EnsembleEvaluator evaluator )
    {
        assert !findVariable( name ) : "Variable " + name + " is already set";

        Collection<? extends Evaluator<?>> variables = getVariables();

        boolean found = false;
        for( Evaluator<?> e : variables )
        {
            found |= e.getName().equals( name );
        }

        assert found : "Variable " + name + " does not exist";
        
        ensembleVariables.put( name, evaluator );
    }


    public void setVariable( String name, MeshEvaluator evaluator )
    {
        assert !findVariable( name ) : "Variable " + name + " is already set";

        Collection<? extends Evaluator<?>> variables = getVariables();

        boolean found = false;
        for( Evaluator<?> e : variables )
        {
            found |= e.getName().equals( name );
        }

        assert found : "Variable " + name + " does not exist";
        
        meshVariables.put( name, evaluator );
    }


    private boolean findVariable( String name )
    {
        for( SimpleMapEntry<String, ContinuousEvaluator> e : continuousVariables )
        {
            if( e.key.equals( name ) )
            {
                return true;
            }
        }
        for( SimpleMapEntry<String, EnsembleEvaluator> e : ensembleVariables )
        {
            if( e.key.equals( name ) )
            {
                return true;
            }
        }
        for( SimpleMapEntry<String, MeshEvaluator> e : meshVariables )
        {
            if( e.key.equals( name ) )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );

        for( SimpleMapEntry<String, ContinuousEvaluator> e : continuousVariables )
        {
            localContext.setVariable( e.key, e.value );
        }
        for( SimpleMapEntry<String, EnsembleEvaluator> e : ensembleVariables )
        {
            localContext.setVariable( e.key, e.value );
        }
        for( SimpleMapEntry<String, MeshEvaluator> e : meshVariables )
        {
            localContext.setVariable( e.key, e.value );
        }
        return template.evaluate( localContext );
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        ArrayList<Evaluator<?>> variables = new ArrayList<Evaluator<?>>();

        for( SimpleMapEntry<String, ContinuousEvaluator> e : continuousVariables )
        {
            variables.addAll( e.value.getVariables() );
        }
        for( SimpleMapEntry<String, EnsembleEvaluator> e : ensembleVariables )
        {
            variables.addAll( e.value.getVariables() );
        }
        for( SimpleMapEntry<String, MeshEvaluator> e : meshVariables )
        {
            variables.addAll( e.value.getVariables() );
        }
        
        variables.addAll( template.getVariables() );

        return variables;
    }
}
