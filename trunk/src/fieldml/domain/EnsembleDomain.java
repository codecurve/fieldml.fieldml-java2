package fieldml.domain;

import java.util.ArrayList;
import java.util.List;

import fieldml.value.DomainValue;
import fieldml.value.EnsembleDomainValue;

public class EnsembleDomain
    extends Domain
{
    public final List<EnsembleDomainComponent> components;


    public EnsembleDomain( String name )
    {
        super( name );

        components = new ArrayList<EnsembleDomainComponent>();
    }


    public void addComponent( EnsembleDomainComponent component )
    {
        components.add( component );
    }


    public EnsembleDomainComponent getComponent( String name )
    {
        for( EnsembleDomainComponent c : components )
        {
            if( c.name.equals( name ) )
            {
                return c;
            }
        }

        return null;
    }


    @Override
    public int getComponentCount()
    {
        return components.size();
    }


    public DomainValue getValue( double[] componentValues )
    {
        // TODO One day, this will be a lot more rigorous.
        if( componentValues.length < components.size() )
        {
            return null;
        }

        return new EnsembleDomainValue( this, componentValues );
    }
}
