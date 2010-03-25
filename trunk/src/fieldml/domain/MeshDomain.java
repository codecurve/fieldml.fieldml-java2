package fieldml.domain;

import java.util.HashMap;
import java.util.Map;

import fieldml.evaluator.EnsembleEvaluator;
import fieldml.region.Region;
import fieldml.value.MeshDomainValue;

public class MeshDomain
    extends Domain
{
    private final ContinuousDomain xiDomain;

    private final EnsembleDomain elementDomain;

    private EnsembleDomain pointDomain;

    public final Map<Integer, String> shapes;

    public String defaultShape;

    public final Map<EnsembleDomain, EnsembleEvaluator> pointConnectivity;


    public MeshDomain( Region owner, String name, ContinuousDomain xiBase, EnsembleDomain elementBase, int elementCount )
    {
        this( owner, name, xiBase, elementBase, new ContiguousEnsembleBounds( elementCount ) );
    }


    public MeshDomain( Region owner, String name, ContinuousDomain xiBase, EnsembleDomain elementBase, EnsembleBounds elementBounds )
    {
        super( name, null );

        this.xiDomain = new ContinuousDomain( owner, name + ".xi", xiBase );
        this.elementDomain = new EnsembleDomain( owner, name + ".elements", elementBase, elementBounds );

        shapes = new HashMap<Integer, String>();
        pointConnectivity = new HashMap<EnsembleDomain, EnsembleEvaluator>();
        
        owner.addDomain( this );
    }


    public MeshDomainValue makeValue( int element, double... chartValues )
    {
        if( chartValues.length < xiDomain.componentCount )
        {
            return null;
        }

        return new MeshDomainValue( element, chartValues );
    }


    public void setDefaultShape( String shape )
    {
        defaultShape = shape;
    }


    public void setShape( int element, String shapeName )
    {
        shapes.put( element, shapeName );
    }


    public void setPointConnectivity( EnsembleDomain arrangement, EnsembleEvaluator evaluator )
    {
        if( pointDomain == null )
        {
            pointDomain = evaluator.valueDomain.baseDomain;
        }
        else
        {
            assert pointDomain == evaluator.valueDomain.baseDomain;
        }

        pointConnectivity.put( arrangement, evaluator );
    }


    public ContinuousDomain getXiDomain()
    {
        return xiDomain;
    }


    public EnsembleDomain getElementDomain()
    {
        return elementDomain;
    }
}
