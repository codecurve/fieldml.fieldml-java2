package fieldml.domain;

public class ContiguousEnsembleBounds
    extends EnsembleBounds
{
    public int valueCount;


    public ContiguousEnsembleBounds( int valueCount )
    {
        this.valueCount = valueCount;
    }


    @Override
    public int getValueCount()
    {
        return valueCount;
    }
}
