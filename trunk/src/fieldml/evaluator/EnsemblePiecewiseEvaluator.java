package fieldml.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class EnsemblePiecewiseEvaluator
    extends AbstractEnsembleEvaluator
{
    public static class TemplateMap
    {
        public final int index;

        @SerializationAsString
        public final EnsembleEvaluator evaluator;


        public TemplateMap( int index, EnsembleEvaluator evaluator )
        {
            this.index = index;
            this.evaluator = evaluator;
        }
    }

    @SerializationAsString
    public final EnsembleEvaluator indexSource;

    public final SimpleMap<String, ContinuousEvaluator> variables;

    public final List<TemplateMap> elementMaps;


    public EnsemblePiecewiseEvaluator( String name, EnsembleDomain valueDomain, EnsembleEvaluator indexSource )
    {
        super( name, valueDomain );

        this.indexSource = indexSource;

        elementMaps = new ArrayList<TemplateMap>();
        variables = new SimpleMap<String, ContinuousEvaluator>();
        for( int i = 0; i <= indexSource.getValueDomain().getValueCount(); i++ )
        {
            elementMaps.add( null );
        }
    }


    public void setEvaluator( int index, EnsembleEvaluator evaluator )
    {
        elementMaps.set( index, new TemplateMap( index, evaluator ) );
    }


    public void setVariable( String name, ContinuousEvaluator evaluator )
    {
        variables.put( name, evaluator );
    }


    @Override
    public EnsembleDomainValue evaluate( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<String, ContinuousEvaluator> e : variables )
        {
            localContext.setVariable( e.key, e.value );
        }

        EnsembleDomainValue v = indexSource.evaluate( context );

        TemplateMap templateMap = elementMaps.get( v.values[0] );

        if( templateMap != null )
        {
            return new EnsembleDomainValue( valueDomain, templateMap.evaluator.evaluate( context ).values );
        }

        assert false;

        return null;
    }


    @Override
    public String toString()
    {
        return name;
    }


    @Override
    public Collection<? extends Evaluator<?>> getVariables()
    {
        ArrayList<Evaluator<?>> variables = new ArrayList<Evaluator<?>>();

        for( TemplateMap t : elementMaps )
        {
            variables.addAll( t.evaluator.getVariables() );
        }

        return variables;
    }
}
