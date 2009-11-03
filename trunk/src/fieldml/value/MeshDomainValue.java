package fieldml.value;

import fieldml.domain.MeshDomain;

public class MeshDomainValue
    extends EnsembleDomainValue
{
    public double[] chartValues;


    public MeshDomainValue( MeshDomain domain, int ensembleValue, double[] chartValues )
    {
        super( domain, ensembleValue );

        this.chartValues = chartValues;
    }
}
