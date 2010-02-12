package fieldml.value;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue<EnsembleDomain>
{
    public final int values[];


    public EnsembleDomainValue( EnsembleDomain domain, int indexValue )
    {
        super( domain );

        this.values = new int[]{indexValue};
    }


    @Override
    public String toString()
    {
        return "" + values[0];
    }
}
