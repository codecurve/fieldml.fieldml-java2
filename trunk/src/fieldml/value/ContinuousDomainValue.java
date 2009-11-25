package fieldml.value;

import java.util.Arrays;

import fieldml.domain.ContinuousDomain;

public class ContinuousDomainValue
    extends DomainValue<ContinuousDomain>
{
    public double[] values;


    private ContinuousDomainValue( ContinuousDomain domain, double... values )
    {
        super( domain );

        this.values = values;
    }


    public static ContinuousDomainValue makeValue( ContinuousDomain domain, double... values )
    {
        if( values.length < domain.dimensions )
        {
            return null;
        }

        return new ContinuousDomainValue( domain, values );
    }


    @Override
    public String toString()
    {
        return "" + Arrays.toString( values );
    }
}
