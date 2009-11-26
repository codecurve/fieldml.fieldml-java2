package fieldml.region;

import org.jdom.Element;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;

public class Region
{
    private static final String LIBRARY_NAME = "library";

    private static final Region library;

    public static final SimpleMap<String, Region> regions;

    static
    {
        regions = new SimpleMap<String, Region>();

        library = buildLibrary();
    }


    private static Region buildLibrary()
    {
        Region region = new Region( LIBRARY_NAME );

        EnsembleDomain triangle1x1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.triangle.1x1" );
        triangle1x1LocalNodeDomain.addValues( 1, 2, 3 );
        region.addDomain( triangle1x1LocalNodeDomain );

        EnsembleDomain quad1x1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.1x1" );
        quad1x1LocalNodeDomain.addValues( 1, 2, 3, 4 );
        region.addDomain( quad1x1LocalNodeDomain );

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.2x2" );
        quad2x2LocalNodeDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8, 9 );
        region.addDomain( quad2x2LocalNodeDomain );

        EnsembleDomain quadEdgeDirectionDomain = new EnsembleDomain( "library.edge_direction.quad" );
        quadEdgeDirectionDomain.addValues( 1, 2 );
        region.addDomain( quadEdgeDirectionDomain );

        region.addDomain( new ContinuousDomain( "library.co-ordinates.rc.1d", 1 ) );

        region.addDomain( new ContinuousDomain( "library.co-ordinates.rc.2d", 2 ) );

        region.addDomain( new ContinuousDomain( "library.co-ordinates.rc.3d", 3 ) );

        region.addDomain( new ContinuousDomain( "library.bicubic_hermite.scaling", 4 ) );

        region.addDomain( new ContinuousDomain( "library.bicubic_hermite.parameters", 4 ) );

        region.addDomain( new ContinuousDomain( "library.weighting.1d", 1 ) );

        region.addDomain( new ContinuousDomain( "library.weighting.2d", 2 ) );

        return region;
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

    private final String name;

    public Region( String name )
    {
        this.name = name;
        
        meshDomains = new SimpleMap<String, MeshDomain>();
        continuousDomains = new SimpleMap<String, ContinuousDomain>();
        ensembleDomains = new SimpleMap<String, EnsembleDomain>();
        continuousEvaluators = new SimpleMap<String, ContinuousEvaluator>();
        ensembleEvaluators = new SimpleMap<String, EnsembleEvaluator>();

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


    public void addEvaluator( ContinuousEvaluator evaluator )
    {
        continuousEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( EnsembleEvaluator evaluator )
    {
        ensembleEvaluators.put( evaluator.getName(), evaluator );
    }


    public void serializeToXml( Element root )
    {
        JdomReflectiveHandler handler = new JdomReflectiveHandler( root );
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
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, EnsembleEvaluator> k : ensembleEvaluators )
        {
            ReflectiveWalker.Walk( k.value, handler );
        }
    }
}
