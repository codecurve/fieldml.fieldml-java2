package fieldml.domain;

import java.util.ArrayList;

public class DirectEnsembleDomainComponent
    extends EnsembleDomainComponent
{
    public final ArrayList<Integer> values;


    public DirectEnsembleDomainComponent( String name )
    {
        super( name );

        values = new ArrayList<Integer>();
    }


    public void addValue( int value )
    {
        values.add( value );
    }


    @Override
    public Iterable<Integer> getValues()
    {
        return values;
    }
}
