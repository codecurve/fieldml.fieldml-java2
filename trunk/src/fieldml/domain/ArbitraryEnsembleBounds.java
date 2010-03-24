package fieldml.domain;

public class ArbitraryEnsembleBounds
    extends EnsembleBounds
{
    public int[] values;


    public ArbitraryEnsembleBounds( int... values )
    {
        this.values = values;
    }


    @Override
    public int getValueCount()
    {
        return values.length;
    }
}
