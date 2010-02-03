package fieldml.region;

import java.util.HashMap;
import java.util.Map;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.ContinuousListDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.EnsembleListDomain;
import fieldml.domain.MeshDomain;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.ContinuousListEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.evaluator.EnsembleListEvaluator;
import fieldml.field.PiecewiseField;
import fieldml.field.PiecewiseTemplate;
import fieldml.io.ReflectiveHandler;
import fieldml.io.ReflectiveWalker;
import fieldml.util.SimpleMap;
import fieldml.util.SimpleMapEntry;

public class Region
{
    private static final String LIBRARY_NAME = "library";

    private static final Region library;

    private static final SimpleMap<String, Region> regions;

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
        
        region.addDomain( new EnsembleListDomain( "library.local_nodes.quad.1x1_list", quad1x1LocalNodeDomain ) );

        EnsembleDomain line1LocalNodeDomain = new EnsembleDomain( "library.local_nodes.line.1" );
        line1LocalNodeDomain.addValues( 1, 2 );
        region.addDomain( line1LocalNodeDomain );

        EnsembleDomain line2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.line.2" );
        line2LocalNodeDomain.addValues( 1, 2, 3 );
        region.addDomain( line2LocalNodeDomain );

        EnsembleDomain quad2x2LocalNodeDomain = new EnsembleDomain( "library.local_nodes.quad.2x2" );
        quad2x2LocalNodeDomain.addValues( 1, 2, 3, 4, 5, 6, 7, 8, 9 );
        region.addDomain( quad2x2LocalNodeDomain );

        EnsembleDomain quadEdgeDirectionDomain = new EnsembleDomain( "library.edge_direction.quad" );
        quadEdgeDirectionDomain.addValues( 1, 2 );
        region.addDomain( quadEdgeDirectionDomain );

        ContinuousDomain weighting = new ContinuousDomain( "library.weighting.1d", 1 );
        region.addDomain( weighting );

        region.addDomain( new ContinuousDomain( "library.weighting.2d", 2 ) );

        region.addDomain( new ContinuousDomain( "library.weighting.3d", 3 ) );

        region.addDomain( new ContinuousDomain( "library.co-ordinates.rc.1d", 1 ) );

        region.addDomain( new ContinuousDomain( "library.co-ordinates.rc.2d", 2 ) );

        region.addDomain( new ContinuousDomain( "library.co-ordinates.rc.3d", 3 ) );

        region.addDomain( new ContinuousDomain( "library.bicubic_hermite.scaling", 16 ) );

        region.addDomain( new ContinuousListDomain( "library.linear_lagrange.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.bilinear_lagrange.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.quadratic_lagrange.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.biquadratic_lagrange.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.cubic_lagrange.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.bilinear_simplex.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.bicubic_hermite.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.quadratic_bspline.parameters", weighting ) );

        region.addDomain( new ContinuousListDomain( "library.weighting.list", weighting ) );

        region.addDomain( new ContinuousDomain( "library.bicubic_hermite.nodal.parameters", 4 ) );
        
        return region;
    }


    public static Region getLibrary()
    {
        return library;
    }

    private final SimpleMap<String, MeshDomain> meshDomains;

    private final SimpleMap<String, ContinuousDomain> continuousDomains;

    private final SimpleMap<String, ContinuousListDomain> continuousListDomains;

    private final SimpleMap<String, EnsembleDomain> ensembleDomains;

    private final SimpleMap<String, EnsembleListDomain> ensembleListDomains;

    private final SimpleMap<String, ContinuousEvaluator> continuousEvaluators;

    private final SimpleMap<String, ContinuousListEvaluator> continuousListEvaluators;

    private final SimpleMap<String, EnsembleEvaluator> ensembleEvaluators;

    private final SimpleMap<String, EnsembleListEvaluator> ensembleListEvaluators;

    private final SimpleMap<String, PiecewiseTemplate> piecewiseTemplates;

    private final Map<String, Region> subregions;

    private final String name;


    public Region( String name )
    {
        this.name = name;

        meshDomains = new SimpleMap<String, MeshDomain>();
        continuousDomains = new SimpleMap<String, ContinuousDomain>();
        continuousListDomains = new SimpleMap<String, ContinuousListDomain>();
        ensembleDomains = new SimpleMap<String, EnsembleDomain>();
        ensembleListDomains = new SimpleMap<String, EnsembleListDomain>();
        continuousEvaluators = new SimpleMap<String, ContinuousEvaluator>();
        continuousListEvaluators = new SimpleMap<String, ContinuousListEvaluator>();
        ensembleEvaluators = new SimpleMap<String, EnsembleEvaluator>();
        ensembleListEvaluators = new SimpleMap<String, EnsembleListEvaluator>();
        piecewiseTemplates = new SimpleMap<String, PiecewiseTemplate>();
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


    public ContinuousListDomain getContinuousListDomain( String name )
    {
        ContinuousListDomain domain = continuousListDomains.get( name );

        assert domain != null : "Domain " + name + " does not exist in region " + this.name;

        return domain;
    }


    public EnsembleDomain getEnsembleDomain( String name )
    {
        EnsembleDomain domain = ensembleDomains.get( name );

        assert domain != null : "Domain " + name + " does not exist in region " + this.name;

        return domain;
    }


    public EnsembleListDomain getEnsembleListDomain( String name )
    {
        EnsembleListDomain domain = ensembleListDomains.get( name );

        assert domain != null : "Domain " + name + " does not exist in region " + this.name;

        return domain;
    }


    public ContinuousEvaluator getContinuousEvaluator( String name )
    {
        ContinuousEvaluator evaluator = continuousEvaluators.get( name );

        assert evaluator != null : "Evaluator " + name + " does not exist in region " + this.name;

        return evaluator;
    }


    public ContinuousListEvaluator getContinuousListEvaluator( String name )
    {
        ContinuousListEvaluator evaluator = continuousListEvaluators.get( name );

        assert evaluator != null : "Evaluator " + name + " does not exist in region " + this.name;

        return evaluator;
    }


    public EnsembleEvaluator getEnsembleEvaluator( String name )
    {
        EnsembleEvaluator evaluator = ensembleEvaluators.get( name );

        assert evaluator != null : "Evaluator " + name + " does not exist in region " + this.name;

        return evaluator;
    }


    public EnsembleListEvaluator getEnsembleListEvaluator( String name )
    {
        EnsembleListEvaluator evaluator = ensembleListEvaluators.get( name );

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


    public void addDomain( ContinuousListDomain domain )
    {
        continuousListDomains.put( domain.name, domain );
    }


    public void addDomain( EnsembleDomain domain )
    {
        ensembleDomains.put( domain.name, domain );
    }


    public void addDomain( EnsembleListDomain domain )
    {
        ensembleListDomains.put( domain.name, domain );
    }


    public void addEvaluator( ContinuousEvaluator evaluator )
    {
        continuousEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( ContinuousListEvaluator evaluator )
    {
        continuousListEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( EnsembleEvaluator evaluator )
    {
        ensembleEvaluators.put( evaluator.getName(), evaluator );
    }


    public void addEvaluator( EnsembleListEvaluator evaluator )
    {
        ensembleListEvaluators.put( evaluator.getName(), evaluator );
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
        for( SimpleMapEntry<String, EnsembleListDomain> k : ensembleListDomains )
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
        for( SimpleMapEntry<String, ContinuousListEvaluator> k : continuousListEvaluators )
        {
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, EnsembleEvaluator> k : ensembleEvaluators )
        {
            ReflectiveWalker.Walk( k.value, handler );
        }
        for( SimpleMapEntry<String, EnsembleListEvaluator> k : ensembleListEvaluators )
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
