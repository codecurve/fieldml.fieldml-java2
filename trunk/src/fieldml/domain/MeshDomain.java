package fieldml.domain;

import fieldml.value.MeshDomainValue;

public class MeshDomain
    extends EnsembleDomain
{
    public final int dimensionality;


    public MeshDomain( String name, int dimensionality )
    {
        super( name );

        this.dimensionality = dimensionality;
    }


    public MeshDomainValue getValue( int indexValue, double... chartValues )
    {
        if( chartValues.length > dimensionality )
        {
            return null;
        }

        return new MeshDomainValue( this, indexValue, chartValues );
    }
}
