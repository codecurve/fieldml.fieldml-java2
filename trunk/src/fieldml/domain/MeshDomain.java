package fieldml.domain;

import java.util.HashMap;
import java.util.Map;

import fieldml.annotations.SerializationAsString;
import fieldml.evaluator.EnsembleEvaluator;
import fieldml.value.MeshDomainValue;

public class MeshDomain
    extends Domain
{
    public final int dimensions;

    @SerializationAsString
    public final EnsembleDomain elementDomain;

    private EnsembleDomain pointDomain;
    
    public final Map<Integer, String> shapes;

    public String defaultShape;
    
    public final Map<String, EnsembleEvaluator> pointConnectivity;


    public MeshDomain( String name, int dimensions, EnsembleDomain elementDomain )
    {
        super( name, null );

        this.dimensions = dimensions;
        this.elementDomain = elementDomain;

        shapes = new HashMap<Integer, String>();
        pointConnectivity = new HashMap<String, EnsembleEvaluator>();
    }


    public MeshDomainValue makeValue( int indexValue, double... chartValues )
    {
        if( chartValues.length < dimensions )
        {
            return null;
        }

        return new MeshDomainValue( this, indexValue, chartValues );
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
}
