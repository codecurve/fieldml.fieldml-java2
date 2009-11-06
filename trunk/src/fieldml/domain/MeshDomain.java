package fieldml.domain;

import java.util.HashMap;
import java.util.Map;

import fieldml.annotations.SerializationAsString;
import fieldml.value.MeshDomainValue;

public class MeshDomain
    extends Domain
{
    public static final Map<String, MeshDomain> domains = new HashMap<String, MeshDomain>();

    public final int dimensions;

    @SerializationAsString
    public final EnsembleDomain discretization;


    public MeshDomain( String name, int dimensions, EnsembleDomain discretization )
    {
        super( name );

        this.dimensions = dimensions;
        this.discretization = discretization;

        domains.put( name, this );
    }


    public MeshDomainValue makeValue( int indexValue, double... chartValues )
    {
        if( chartValues.length < dimensions )
        {
            return null;
        }

        return new MeshDomainValue( this, indexValue, chartValues );
    }
}
