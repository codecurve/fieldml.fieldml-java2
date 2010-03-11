package fieldml.value;

import java.util.Arrays;

import fieldml.domain.ContinuousDomain;

public class ContinuousDomainValue
    extends DomainValue<ContinuousDomain>
{
    public double[] values;


    public ContinuousDomainValue( ContinuousDomain domain, double... values )
    {
        this.values = values;
    }


    @Override
    public String toString()
    {
        return "" + Arrays.toString( values );
    }
}
