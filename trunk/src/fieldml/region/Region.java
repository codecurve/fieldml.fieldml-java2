package fieldml.region;

import java.util.HashMap;
import java.util.Map;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.function.ContinuousFunction;
import fieldml.io.ReflectiveHandler;
import fieldml.io.ReflectiveWalker;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;

public class Region
{
    private static final Region library;

    private static final SimpleMap<String, Region> regions;

    static
    {
        regions = new SimpleMap<String, Region>();

        library = new Library();
    }


    public static Region getLibrary()
    {
        return library;
    }

    private final SimpleMap<String, MeshDomain> meshDomains;

    private final SimpleMap<String, ContinuousDomain> continuousDomains;

    private final SimpleMap<String, EnsembleDomain> ensembleDomains;

    private final SimpleMap<String, ContinuousEvaluator> continuousEvaluators;

    private final SimpleMap<String, EnsembleEvaluator> ensembleEvaluators;

    private final SimpleMap<String, PiecewiseTemplate> piecewiseTemplates;

    private final SimpleMap<String, ContinuousFunction> functions;
    
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
        piecewiseTemplates = new SimpleMap<String, PiecewiseTemplate>();
        functions = new SimpleMap<String, ContinuousFunction>();
        subregions = new HashMap<String, Region>();

        assert regions.get( name ) == null;

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


    public ContinuousFunction getContinuousFunction( String name )
    {
        ContinuousFunction function = functions.get( name );

        assert function != null : "Function " + name + " does not exist in region " + this.name;

        return function;
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


    public PiecewiseTemplate getPiecewiseTemplate( String name )
    {
        PiecewiseTemplate template = piecewiseTemplates.get( name );

        assert template != null : "Template " + name + " does not exist in region " + this.name;

        return template;
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


    public void addFunction( String name, ContinuousFunction function )
    {
        functions.put( name, function );
    }

    public void addEvaluator( ContinuousEvaluator evaluator )
    {
        continuousEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( EnsembleEvaluator evaluator )
    {
        ensembleEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addPiecewiseTemplate( PiecewiseTemplate template )
    {
        piecewiseTemplates.put( template.name, template );
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
        for( SimpleMapEntry<String, PiecewiseTemplate> k : piecewiseTemplates )
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
}
