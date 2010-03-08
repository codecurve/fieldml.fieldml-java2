package fieldml.region;

import java.util.HashMap;
import java.util.Map;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.MeshEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.io.ReflectiveHandler;
import fieldml.io.ReflectiveWalker;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;

public abstract class Region
{
    private final SimpleMap<String, MeshDomain> meshDomains;

    private final SimpleMap<String, ContinuousDomain> continuousDomains;

    private final SimpleMap<String, EnsembleDomain> ensembleDomains;

    private final SimpleMap<String, ContinuousEvaluator> continuousEvaluators;

    private final SimpleMap<String, EnsembleEvaluator> ensembleEvaluators;

    private final SimpleMap<String, MeshEvaluator> meshEvaluators;

    private final Map<String, Region> subregions;

    private final String name;


    public Region( String name )
    {
        this.name = name;

        meshDomains = new SimpleMap<String, MeshDomain>();
        continuousDomains = new SimpleMap<String, ContinuousDomain>();
        ensembleDomains = new SimpleMap<String, EnsembleDomain>();
        continuousEvaluators = new SimpleMap<String, ContinuousEvaluator>();
        ensembleEvaluators = new SimpleMap<String, EnsembleEvaluator>();
        meshEvaluators = new SimpleMap<String, MeshEvaluator>();
        subregions = new HashMap<String, Region>();
    }


    public MeshDomain getMeshDomain( String name )
    {
        MeshDomain domain = meshDomains.get( name );

        assert domain != null : "Domain " + name + " does not exist in region " + this.name;

        return domain;
    }


    public ContinuousDomain getContinuousDomain( String name )
    {
        ContinuousDomain domain = continuousDomains.get( name );

        assert domain != null : "Domain " + name + " does not exist in region " + this.name;

        return domain;
    }


    public EnsembleDomain getEnsembleDomain( String name )
    {
        EnsembleDomain domain = ensembleDomains.get( name );

        assert domain != null : "Domain " + name + " does not exist in region " + this.name;

        return domain;
    }


    public MeshEvaluator getMeshEvaluator( String name )
    {
        MeshEvaluator evaluator = meshEvaluators.get( name );

        assert evaluator != null : "Evaluator " + name + " does not exist in region " + this.name;

        return evaluator;
    }


    public ContinuousEvaluator getContinuousEvaluator( String name )
    {
        ContinuousEvaluator evaluator = continuousEvaluators.get( name );

        assert evaluator != null : "Evaluator " + name + " does not exist in region " + this.name;

        return evaluator;
    }


    public EnsembleEvaluator getEnsembleEvaluator( String name )
    {
        EnsembleEvaluator evaluator = ensembleEvaluators.get( name );

        assert evaluator != null : "Evaluator " + name + " does not exist in region " + this.name;

        return evaluator;
    }


    public void addDomain( MeshDomain domain )
    {
        meshDomains.put( domain.name, domain );
    }


    public void addDomain( ContinuousDomain domain )
    {
        continuousDomains.put( domain.name, domain );
    }


    public void addDomain( EnsembleDomain domain )
    {
        ensembleDomains.put( domain.name, domain );
    }


    public void addEvaluator( String name, MeshEvaluator meshEvaluator )
    {
        meshEvaluators.put( name, meshEvaluator );
    }


    public void addEvaluator( ContinuousEvaluator evaluator )
    {
        continuousEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( EnsembleEvaluator evaluator )
    {
        ensembleEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( MeshEvaluator evaluator )
    {
        meshEvaluators.put( evaluator.getName(), evaluator );
    }


    public void walkObjects( ReflectiveHandler handler )
    {
        for( SimpleMapEntry<String, ContinuousDomain> k : continuousDomains )
        {
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, EnsembleDomain> k : ensembleDomains )
        {
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, MeshDomain> k : meshDomains )
        {
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, ContinuousEvaluator> k : continuousEvaluators )
        {
            if( k.value instanceof PiecewiseField )
            {
                // Hack for better ordering.
                continue;
            }
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, EnsembleEvaluator> k : ensembleEvaluators )
        {
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, ContinuousEvaluator> k : continuousEvaluators )
        {
            if( k.value instanceof PiecewiseField )
            {
                // Hack for better ordering... evil twin.
                ReflectiveWalker.Walk( k.value, handler );
            }
        }
    }


    public void addSubregion( Region region )
    {
        subregions.put( region.name, region );
    }


    public Region getSubregion( String name )
    {
        return subregions.get( name );
    }


    public String getName()
    {
        return name;
    }
    
    
    public abstract Region getLibrary();
}
