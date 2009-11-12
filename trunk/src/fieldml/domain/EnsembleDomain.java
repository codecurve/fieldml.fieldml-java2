package fieldml.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnsembleDomain
    extends Domain
{
    public static final Map<String, EnsembleDomain> domains = new HashMap<String, EnsembleDomain>();

    public final List<Integer> values;


    public EnsembleDomain( String name )
    {
        super( name );

        values = new ArrayList<Integer>();

        domains.put( name, this );
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
