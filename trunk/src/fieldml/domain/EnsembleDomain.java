package fieldml.domain;

import java.util.ArrayList;
import java.util.List;

public class EnsembleDomain
    extends Domain
{
    public final List<Integer> values;


    public EnsembleDomain( String name )
    {
        super( name );
        
        values = new ArrayList<Integer>();
    }


    public void addValue( int value )
    {
        values.add( value );
    }
}
