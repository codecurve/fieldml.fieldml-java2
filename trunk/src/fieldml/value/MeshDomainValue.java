package fieldml.value;

import fieldml.domain.MeshDomain;

public class MeshDomainValue
    extends DomainValue
{
    public int indexValue;
    
    public double[] chartValues;


    public MeshDomainValue( MeshDomain domain, int indexValue, double[] chartValues )
    {
        super( domain );

        this.indexValue = indexValue;
        this.chartValues = chartValues;
    }
}
