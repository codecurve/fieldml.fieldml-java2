package fieldml.domain;

public class ContinuousDomainComponent
    extends DomainComponent
{
    public double min;

    public double max;


    public ContinuousDomainComponent( String name )
    {
        super( name );
    }


    public void setMin( double min )
    {
        this.min = min;
    }


    public double getMin()
    {
        return min;
    }


    public void setMax( double max )
    {
        this.max = max;
    }


    public double getMax()
    {
        return max;
    }
}
