package fieldml.domain;

import fieldml.value.ContinuousDomainValue;

public class ContinuousDomain
    extends EnsembleDomain
{
    public final int dimensions;


    public ContinuousDomain( String name, int dimensionality )
    {
        super( name );

        this.dimensions = dimensionality;
    }


    public ContinuousDomainValue getValue( double...chartValues )
    {
        if( chartValues.length > dimensions )
        {
            return null;
        }

        return new ContinuousDomainValue( this, chartValues );
    }


    @Override
    public ContinuousDomainValue getValue( int indexValue, double... chartValues )
    {
        return getValue( chartValues );
    }
}
