package fieldml.value;

import java.util.Arrays;

import fieldml.domain.MeshDomain;

public class MeshDomainValue
    extends DomainValue<MeshDomain>
{
    public final int indexValue;

    public double[] chartValues;


    public MeshDomainValue( MeshDomain domain, int indexValue, double[] chartValues )
    {
        super( domain );

        this.indexValue = indexValue;
        this.chartValues = chartValues;
    }


    @Override
    public String toString()
    {
        return "" + indexValue + "+(" + Arrays.toString( chartValues ) + ")";
    }
}
