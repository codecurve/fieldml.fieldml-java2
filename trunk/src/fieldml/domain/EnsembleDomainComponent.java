package fieldml.domain;

import java.util.ArrayList;
import java.util.List;

public class EnsembleDomainComponent
    extends DomainComponent
{
    public final List<Integer> values;


    public EnsembleDomainComponent( String name )
    {
        super( name );

        values = new ArrayList<Integer>();
    }


    public void addValue( int value )
    {
        values.add( value );
    }
}
