package fieldml.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fieldml.annotations.SerializationAsString;
import fieldml.evaluator.ContinuousEvaluator;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.value.MeshDomainValue;

public class MeshDomain
    extends Domain
{
    private final ContinuousDomain xiDomain;

    private final EnsembleDomain elementDomain;

    private EnsembleDomain pointDomain;

    public final Map<Integer, String> shapes;

    public String defaultShape;

    public final Map<String, EnsembleEvaluator> pointConnectivity;

    @SerializationAsString
    public final List<ContinuousEvaluator> fields;


    public MeshDomain( String name, EnsembleDomain xiEnsemble, int elementCount )
    {
        this( name, xiEnsemble, new ContiguousEnsembleBounds( elementCount ) );
    }


    public MeshDomain( String name, EnsembleDomain xiEnsemble, EnsembleBounds elementBounds )
    {
        super( name, null );

        this.xiDomain = new ContinuousDomain( name + ".xi", xiEnsemble );
        this.elementDomain = new EnsembleDomain( name + ".elements", elementBounds );

        shapes = new HashMap<Integer, String>();
        pointConnectivity = new HashMap<String, EnsembleEvaluator>();
        fields = new ArrayList<ContinuousEvaluator>();
    }


    public MeshDomainValue makeValue( int element, double... chartValues )
    {
        if( chartValues.length < xiDomain.componentCount )
        {
            return null;
        }

        return new MeshDomainValue( this, element, chartValues );
    }


    public void setDefaultShape( String shape )
    {
        defaultShape = shape;
    }


    public void setShape( int element, String shapeName )
    {
        shapes.put( element, shapeName );
    }


    public void setPointConnectivity( String arrangement, EnsembleEvaluator evaluator )
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


    public void addField( ContinuousEvaluator field )
    {
        fields.add( field );
    }
}
