package fieldml.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldmlx.util.SimpleMap;
import fieldmlx.util.SimpleMapEntry;

public class EnsemblePiecewiseEvaluator
    extends EnsembleEvaluator
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
    public final EnsembleDomain indexDomain;

    public final SimpleMap<String, ContinuousEvaluator> variables;

    public final List<TemplateMap> elementMaps;


    public EnsemblePiecewiseEvaluator( String name, EnsembleDomain valueDomain, EnsembleDomain indexDomain )
    {
        super( name, valueDomain );

        this.indexDomain = indexDomain;

        elementMaps = new ArrayList<TemplateMap>();
        variables = new SimpleMap<String, ContinuousEvaluator>();
        for( int i = 0; i <= indexDomain.getValueCount(); i++ )
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
    public EnsembleDomainValue getValue( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<String, ContinuousEvaluator> e : variables )
        {
            localContext.setVariable( e.key, e.value );
        }

        EnsembleDomainValue v = context.get( indexDomain );

        TemplateMap templateMap = elementMaps.get( v.values[0] );

        if( templateMap != null )
        {
            return new EnsembleDomainValue( templateMap.evaluator.getValue( context ).values );
        }

        assert false;

        return null;
    }


    @Override
    public String toString()
    {
        return name;
    }
}
