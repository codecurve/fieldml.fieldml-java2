package fieldml.value;

import fieldml.domain.ContinuousDomain;

public class ContinuousDomainValue
    extends DomainValue
{
    public double[] chartValues;


    private ContinuousDomainValue( ContinuousDomain domain, double... chartValues )
    {
        super( domain );

        this.chartValues = chartValues;
    }


    public static ContinuousDomainValue makeValue( ContinuousDomain domain, double... chartValues )
    {
        if( chartValues.length < domain.dimensions )
        {
            return null;
        }

        return new ContinuousDomainValue( domain, chartValues );
    }
}
