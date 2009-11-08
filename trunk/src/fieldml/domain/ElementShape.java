package fieldml.domain;

public class ElementShape
{
    public final String name;
    
    public ElementShape( String name )
    {
        this.name = name;
    }
    
    
    @Override
    public String toString()
    {
        return name;
    }
}
