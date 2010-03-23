package fieldml.evaluator;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializationAsString;
import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;
import fieldml.value.ContinuousDomainValue;
import fieldml.value.ContinuousValueSource;
import fieldml.value.DomainValues;
import fieldml.value.EnsembleDomainValue;
import fieldml.value.EnsembleValueSource;
import fieldml.value.MeshValueSource;

public class ContinuousPiecewiseEvaluator
    extends ContinuousEvaluator
    implements DelegatingEvaluator
{
    public static class TemplateMap
    {
        public final int index;

        @SerializationAsString
        public final ContinuousValueSource evaluator;


        public TemplateMap( int index, ContinuousValueSource evaluator )
        {
            this.index = index;
            this.evaluator = evaluator;
        }
    }

    @SerializationAsString
    public final EnsembleDomain indexDomain;

    public final SimpleMap<ContinuousDomain, ContinuousValueSource> continuousAliases;

    public final SimpleMap<EnsembleDomain, EnsembleValueSource> ensembleAliases;

    public final SimpleMap<MeshDomain, MeshValueSource> meshAliases;

    public final List<TemplateMap> elementMaps;


    public ContinuousPiecewiseEvaluator( String name, ContinuousDomain valueDomain, EnsembleDomain indexDomain )
    {
        super( name, valueDomain );

        this.indexDomain = indexDomain;

        continuousAliases = new SimpleMap<ContinuousDomain, ContinuousValueSource>();
        ensembleAliases = new SimpleMap<EnsembleDomain, EnsembleValueSource>();
        meshAliases = new SimpleMap<MeshDomain, MeshValueSource>();
        elementMaps = new ArrayList<TemplateMap>();
        for( int i = 0; i <= indexDomain.getValueCount(); i++ )
        {
            elementMaps.add( null );
        }
    }


    public void setEvaluator( int index, ContinuousValueSource evaluator )
    {
        elementMaps.set( index, new TemplateMap( index, evaluator ) );
    }


    public void alias( ContinuousValueSource local, ContinuousDomain remote )
    {
        continuousAliases.put( remote, local );
    }


    public void alias( EnsembleValueSource local, EnsembleDomain remote )
    {
        ensembleAliases.put( remote, local );
    }


    public void alias( MeshValueSource local, MeshDomain remote )
    {
        meshAliases.put( remote, local );
    }


    @Override
    public ContinuousDomainValue getValue( DomainValues context )
    {
        DomainValues localContext = new DomainValues( context );
        for( SimpleMapEntry<ContinuousDomain, ContinuousValueSource> e : continuousAliases )
        {
            localContext.alias( e.value, e.key );
        }
        for( SimpleMapEntry<EnsembleDomain, EnsembleValueSource> e : ensembleAliases )
        {
            localContext.alias( e.value, e.key );
        }
        for( SimpleMapEntry<MeshDomain, MeshValueSource> e : meshAliases )
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
