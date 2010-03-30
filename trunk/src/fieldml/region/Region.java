package fieldml.region;

import java.util.HashMap;
import java.util.Map;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.ImportedContinuousEvaluator;
import fieldml.evaluator.ImportedEnsembleEvaluator;
import fieldml.field.PiecewiseField;
import fieldmlx.io.ReflectiveHandler;
import fieldmlx.io.ReflectiveWalker;
import fieldmlx.util.SimpleMap;
import fieldmlx.util.SimpleMapEntry;

public abstract class Region
{
    private static final Map<String, Region> regions;

    static
    {
        regions = new HashMap<String, Region>();
    }

    private final SimpleMap<String, MeshDomain> meshDomains;

    private final SimpleMap<String, ContinuousDomain> continuousDomains;

    private final SimpleMap<String, EnsembleDomain> ensembleDomains;

    private final SimpleMap<String, ContinuousEvaluator> continuousEvaluators;

    private final SimpleMap<String, EnsembleEvaluator> ensembleEvaluators;

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
        subregions = new HashMap<String, Region>();

        regions.put( name, this );
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


    public ImportedContinuousEvaluator importContinuousEvaluator( String localName, String remoteName )
    {
        ContinuousEvaluator evaluator = getContinuousEvaluator( remoteName );

        assert evaluator != null : "Evaluator " + remoteName + " does not exist in region " + name;

        return new ImportedContinuousEvaluator( localName, evaluator );
    }


    public ImportedEnsembleEvaluator importEnsembleEvaluator( String localName, String remoteName )
    {
        EnsembleEvaluator evaluator = getEnsembleEvaluator( remoteName );

        assert evaluator != null : "Evaluator " + remoteName + " does not exist in region " + name;

        return new ImportedEnsembleEvaluator( localName, evaluator );
    }


    //NOTE: This is only public so that Library doesn't gripe.
    public ContinuousEvaluator getContinuousEvaluator( String name )
    {
        ContinuousEvaluator evaluator = continuousEvaluators.get( name );

        assert evaluator != null : "Evaluator " + name + " does not exist in region " + this.name;

        return evaluator;
    }


    //NOTE: This is only public so that Library doesn't gripe.
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


    public void addEvaluator( ContinuousEvaluator evaluator )
    {
        continuousEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( EnsembleEvaluator evaluator )
    {
        ensembleEvaluators.put( evaluator.getName(), evaluator );
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
