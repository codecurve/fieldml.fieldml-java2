package fieldml.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;

public class ContinuousPiecewiseEvaluator
    extends ContinuousEvaluator
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

    public final SimpleMap<ContinuousDomain, ContinuousValueSource> aliases;

    public final List<TemplateMap> elementMaps;


    public ContinuousPiecewiseEvaluator( String name, ContinuousDomain valueDomain, EnsembleDomain indexDomain )
    {
        super( name, valueDomain );

        this.indexDomain = indexDomain;

        elementMaps = new ArrayList<TemplateMap>();
        aliases = new SimpleMap<ContinuousDomain, ContinuousValueSource>();
        for( int i = 0; i <= indexDomain.getValueCount(); i++ )
        {
            elementMaps.add( null );
        }
    }


    public void setEvaluator( int index, ContinuousEvaluator evaluator )
    {
        elementMaps.set( index, new TemplateMap( index, evaluator ) );
    }


    public void alias( ContinuousValueSource source, ContinuousDomain destination )
    {
        aliases.put( destination, source );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<ContinuousDomain, ContinuousValueSource> e : aliases )
        {
            localContext.alias( e.value, e.key );
        }

        EnsembleDomainValue v = localContext.get( indexDomain );

        TemplateMap templateMap = elementMaps.get( v.values[0] );

        if( templateMap != null )
        {
            ContinuousDomainValue templateValue = templateMap.evaluator.getValue( localContext );

            assert templateValue != null : getName() + " got no value from " + templateMap.evaluator;

            return new ContinuousDomainValue( valueDomain, templateValue.values );
        }

        assert false;

        return null;
    }
}
