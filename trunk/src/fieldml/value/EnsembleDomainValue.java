package fieldml.value;

import java.util.Arrays;

import fieldml.domain.EnsembleDomain;

public class EnsembleDomainValue
    extends DomainValue<EnsembleDomain>
{
    public final int values[];


    public EnsembleDomainValue( int ...indexValues )
    {
        this.values = Arrays.copyOf( indexValues, indexValues.length );
    }


    @Override
    public String toString()
    {
        return Arrays.toString( values );
    }
}
