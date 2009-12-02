package fieldml.domain;

public abstract class Domain
{
    public final String name;

    public Object units;


    Domain( String name )
    {
        this.name = name;
    }


    @Override
    public String toString()
    {
        return name;
    }
}
