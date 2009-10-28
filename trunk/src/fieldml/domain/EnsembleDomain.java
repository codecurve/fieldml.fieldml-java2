package fieldml.domain;

import java.util.ArrayList;
import java.util.List;

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
}
