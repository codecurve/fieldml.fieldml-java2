package fieldml.function;

public abstract class ContinuousFunction
{
    public abstract double[] evaluate( double ... args );
    
    
    public String toString()
    {
        return getClass().getSimpleName();
    }
}
