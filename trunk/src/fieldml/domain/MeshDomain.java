package fieldml.domain;

import java.util.HashMap;
import java.util.Map;

import fieldml.annotations.SerializationAsString;

public class MeshDomain
    extends Domain
{
    public final int dimensions;

    @SerializationAsString
    public final EnsembleDomain elementDomain;
    
    public final Map<Integer, String> shapes;
    
    public String defaultShape; 


    public MeshDomain( String name, int dimensions, EnsembleDomain elementDomain )
    {
        super( name );

        this.dimensions = dimensions;
        this.elementDomain = elementDomain;
        
        shapes = new HashMap<Integer, String>();
    }
    
    
    public void setDefaultShape( String shape )
    {
        defaultShape = shape;
    }
    
    
    public void setShape( int element, String shapeName )
    {
        shapes.put( element, shapeName );
    }
}
