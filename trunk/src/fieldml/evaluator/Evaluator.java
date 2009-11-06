package fieldml.evaluator;



public abstract class Evaluator
{
    public final String name;


    public Evaluator( String name )
    {
        this.name = name;
    }


    @Override
    public String toString()
    {
        return name;
    }
}
