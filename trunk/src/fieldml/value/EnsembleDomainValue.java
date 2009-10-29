package fieldml.value;

import java.util.Arrays;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue
{
    public double[] values;


    public EnsembleDomainValue( EnsembleDomain domain, double[] values )
    {
        super( domain );
        
        this.values = Arrays.copyOf( values, values.length );
    }
}
