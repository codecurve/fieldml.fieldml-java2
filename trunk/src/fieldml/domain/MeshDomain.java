package fieldml.domain;

import fieldml.annotations.SerializationAsString;
import fieldml.value.MeshDomainValue;

public class MeshDomain
    extends ContinuousDomain
{
    @SerializationAsString
    public final EnsembleDomain discretization;


    public MeshDomain( String name, int dimensions, EnsembleDomain discretization )
    {
        super( name, dimensions );

        this.discretization = discretization;
    }


    public MeshDomainValue getValue( int indexValue, double... chartValues )
    {
        if( chartValues.length > dimensions )
        {
            return null;
        }

        return new MeshDomainValue( this, indexValue, chartValues );
    }
}
