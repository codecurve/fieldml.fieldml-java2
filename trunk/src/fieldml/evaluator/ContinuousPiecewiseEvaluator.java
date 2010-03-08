package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class ContinuousPiecewiseEvaluator
    extends AbstractContinuousEvaluator
{
    public static class TemplateMap
    {
        public final int index;

        @SerializationAsString
        public final ContinuousEvaluator evaluator;


        public TemplateMap( int index, ContinuousEvaluator evaluator )
        {
            this.index = index;
            this.evaluator = evaluator;
        }
    }

    @SerializationAsString
    public final EnsembleEvaluator indexSource;

    public final SimpleMap<String, ContinuousEvaluator> continuousVariables;

    public final SimpleMap<String, EnsembleEvaluator> ensembleVariables;

    public final SimpleMap<String, MeshEvaluator> meshVariables;

    public final List<TemplateMap> elementMaps;


    public ContinuousPiecewiseEvaluator( String name, ContinuousDomain valueDomain, EnsembleEvaluator indexSource )
    {
        super( name, valueDomain );

        this.indexSource = indexSource;

        elementMaps = new ArrayList<TemplateMap>();
        continuousVariables = new SimpleMap<String, ContinuousEvaluator>();
        ensembleVariables = new SimpleMap<String, EnsembleEvaluator>();
        meshVariables = new SimpleMap<String, MeshEvaluator>();
        for( int i = 0; i <= indexSource.getValueDomain().getValueCount(); i++ )
        {
            elementMaps.add( null );
        }
    }


    public void setEvaluator( int index, ContinuousEvaluator evaluator )
    {
        elementMaps.set( index, new TemplateMap( index, evaluator ) );
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

        EnsembleDomainValue v = indexSource.evaluate( context );

        TemplateMap templateMap = elementMaps.get( v.values[0] );

        if( templateMap != null )
        {
            ContinuousDomainValue templateValue = templateMap.evaluator.evaluate( localContext );
            return new ContinuousDomainValue( valueDomain, templateValue.values );
        }

        assert false;

        return null;
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        ArrayList<Evaluator<?>> variables = new ArrayList<Evaluator<?>>();

        for( TemplateMap t : elementMaps )
        {
            if( t != null )
            {
                variables.addAll( t.evaluator.getVariables() );
            }
        }

        return variables;
    }
}
