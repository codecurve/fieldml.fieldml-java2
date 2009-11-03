package fieldml.value;

import fieldml.domain.ContinuousDomain;

public class ContinuousDomainValue
    extends DomainValue
{
    public double[] chartValues;


    public ContinuousDomainValue( ContinuousDomain domain, double[] chartValues )
    {
        super( domain );

        this.chartValues = chartValues;
    }
}
