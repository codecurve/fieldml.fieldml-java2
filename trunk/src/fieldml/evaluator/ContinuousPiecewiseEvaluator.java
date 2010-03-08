package fieldml.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
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
    public final EnsembleDomain indexDomain;

    public final SimpleMap<String, ContinuousEvaluator> variables;

    public final List<TemplateMap> elementMaps;


    public ContinuousPiecewiseEvaluator( String name, ContinuousDomain valueDomain, EnsembleDomain indexDomain )
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


    public void setEvaluator( int index, ContinuousEvaluator evaluator )
    {
        elementMaps.set( index, new TemplateMap( index, evaluator ) );
    }


    public void setVariable( String name, ContinuousEvaluator evaluator )
    {
        variables.put( name, evaluator );
    }


    @Override
    public ContinuousDomainValue evaluate( DomainValues context )
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
            ContinuousDomainValue templateValue = templateMap.evaluator.evaluate( context );
            return new ContinuousDomainValue( valueDomain, templateValue.values );
        }

        assert false;

        return null;
    }
}
