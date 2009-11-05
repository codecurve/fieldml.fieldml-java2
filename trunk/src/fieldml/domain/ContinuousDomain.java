package fieldml.domain;

import fieldml.value.ContinuousDomainValue;

public class ContinuousDomain
    extends Domain
{
    public final int dimensions;


    public ContinuousDomain( String name, int dimensions )
    {
        super( name );

        this.dimensions = dimensions;
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
