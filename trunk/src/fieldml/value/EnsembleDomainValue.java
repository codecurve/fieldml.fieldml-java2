package fieldml.value;

import java.util.Arrays;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue<EnsembleDomain>
{
    public final int values[];


    public EnsembleDomainValue( EnsembleDomain domain, int ...indexValues )
    {
        super( domain );

        this.values = Arrays.copyOf( indexValues, indexValues.length );
    }


    @Override
    public String toString()
    {
        return Arrays.toString( values );
    }
}
