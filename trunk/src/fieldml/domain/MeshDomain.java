package fieldml.domain;

import fieldml.value.DomainValue;
import fieldml.value.MeshDomainValue;

public class MeshDomain
    extends EnsembleDomain
{
    private final int dimensionality;


    public MeshDomain( String name, int dimensionality )
    {
        super( name );

        this.dimensionality = dimensionality;
    }


    public DomainValue getValue( int ensembleValue, double[] chartValues )
    {
        if( chartValues.length > dimensionality )
        {
            return null;
        }

        return new MeshDomainValue( this, ensembleValue, chartValues );
    }
}
