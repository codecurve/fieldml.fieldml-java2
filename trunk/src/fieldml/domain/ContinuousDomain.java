package fieldml.domain;

import java.util.ArrayList;
import java.util.List;

public class ContinuousDomain
    extends Domain
{
    public final List<ContinuousDomainComponent> components;


    public ContinuousDomain( String name )
    {
        super( name );
        
        components = new ArrayList<ContinuousDomainComponent>();
    }


    public void addComponent( ContinuousDomainComponent component )
    {
        components.add( component );
    }


    @Override
    public int getComponentCount()
    {
        return components.size();
    }
}
