package fieldml.value;

import fieldml.domain.MeshDomain;

public class MeshDomainValue
    extends ContinuousDomainValue
{
    public int indexValue;


    public MeshDomainValue( MeshDomain domain, int indexValue, double[] chartValues )
    {
        super( domain, chartValues );

        this.indexValue = indexValue;
    }
}
