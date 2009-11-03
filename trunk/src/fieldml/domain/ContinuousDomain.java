package fieldml.domain;

import fieldml.value.ContinuousDomainValue;
import fieldml.value.DomainValue;

public class ContinuousDomain
    extends EnsembleDomain
{
    public final int dimensionality;


    public ContinuousDomain( String name, int dimensionality )
    {
        super( name );

        this.dimensionality = dimensionality;
    }


    public DomainValue getValue( double[] chartValues )
    {
        if( chartValues.length > dimensionality )
        {
            return null;
        }

        return new ContinuousDomainValue( this, chartValues );
    }
}
