package fieldml.value;

import fieldml.domain.ContinuousDomain;

public class ContinuousDomainValue
    extends DomainValue
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
}
