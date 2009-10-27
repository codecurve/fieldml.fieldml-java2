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


    @Override
    public int getComponentCount()
    {
        return components.size();
    }
}
