package fieldml.region;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;

import fieldml.domain.ContinuousDomain;
import fieldml.domain.EnsembleDomain;
import fieldml.domain.MeshDomain;
import fieldml.field.Field;
import fieldml.io.JdomReflectiveHandler;
import fieldml.io.ReflectiveWalker;

public class Region
{
    private static final String LIBRARY_NAME = "library";

    private static final Region library;

    public static final Map<String, Region> regions;

    static
    {
        regions = new HashMap<String, Region>();

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

        return region;
    }


    public static Region getLibrary()
    {
        return library;
    }

    private final Map<String, MeshDomain> meshDomains;

    private final Map<String, ContinuousDomain> continuousDomains;

    private final Map<String, EnsembleDomain> ensembleDomains;

    private final Map<String, Field<?, ?>> fields;


    public Region( String name )
    {
        meshDomains = new HashMap<String, MeshDomain>();
        continuousDomains = new HashMap<String, ContinuousDomain>();
        ensembleDomains = new HashMap<String, EnsembleDomain>();
        fields = new HashMap<String, Field<?, ?>>();

        assert regions.get( name ) == null;

        regions.put( name, this );
    }


    public MeshDomain getMeshDomain( String name )
    {
        return meshDomains.get( name );
    }


    public ContinuousDomain getContinuousDomain( String name )
    {
        return continuousDomains.get( name );
    }


    public EnsembleDomain getEnsembleDomain( String name )
    {
        return ensembleDomains.get( name );
    }


    public Field<?, ?> getField( String name )
    {
        return fields.get( name );
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


    public void addField( Field<?, ?> field )
    {
        assert fields.get( field.name ) == null;

        fields.put( field.name, field );
    }


    public void serializeToXml( Element root )
    {
        JdomReflectiveHandler handler = new JdomReflectiveHandler( root );
        for( ContinuousDomain domain : continuousDomains.values() )
        {
            ReflectiveWalker.Walk( domain, handler );
        }
        for( EnsembleDomain domain : ensembleDomains.values() )
        {
            ReflectiveWalker.Walk( domain, handler );
        }
        for( MeshDomain domain : meshDomains.values() )
        {
            ReflectiveWalker.Walk( domain, handler );
        }

        for( Field<?, ?> field : fields.values() )
        {
            ReflectiveWalker.Walk( field, handler );
        }
    }
}
