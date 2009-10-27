package fieldml.domain;

public abstract class Domain
{
    public final String name;
    
    
    Domain( String name )
    {
        this.name = name;
    }
    
    
    public abstract int getComponentCount();
}
