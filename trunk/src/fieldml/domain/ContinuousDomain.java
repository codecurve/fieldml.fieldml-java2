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


    public ContinuousDomainValue makeValue( double... values )
    {
        if( values.length < dimensions )
        {
            return null;
        }

        return new ContinuousDomainValue( this, values );
    }
}
