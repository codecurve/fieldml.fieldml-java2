package fieldmlx.function;

public abstract class ContinuousFunction
{
    public abstract double[] evaluate( double ... args );
    
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }
}
