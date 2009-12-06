package fieldml.domain;

import java.util.ArrayList;
import java.util.List;

import fieldml.value.EnsembleDomainValue;

public class EnsembleDomain
    extends Domain
{
    public final List<Integer> values;


    public EnsembleDomain( String name )
    {
        super( name );

        values = new ArrayList<Integer>();
    }


    public EnsembleDomainValue makeValue( int indexValue )
    {
        return new EnsembleDomainValue( this, indexValue );
    }


    public int getValueCount()
    {
        return values.size();
    }


    public void addValue( int value )
    {
        values.add( value );
    }


    public void addValues( int... values )
    {
        for( int value : values )
        {
            addValue( value );
        }
    }
}
