package fieldml.value;

import fieldml.domain.MeshDomain;

public class MeshDomainValue
    extends DomainValue<MeshDomain>
{
    public int indexValue;
    
    public double[] chartValues;


    private MeshDomainValue( MeshDomain domain, int indexValue, double[] chartValues )
    {
        super( domain );

        this.indexValue = indexValue;
        this.chartValues = chartValues;
    }



    public static MeshDomainValue makeValue( MeshDomain domain, int indexValue, double... chartValues )
    {
        if( chartValues.length < domain.dimensions )
        {
            return null;
        }

        return new MeshDomainValue( domain, indexValue, chartValues );
    }
    
    
    @Override
    public String toString()
    {
        return "" + indexValue + "+(" + chartValues +")";
    }
}
