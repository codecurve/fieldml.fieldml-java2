package purgatory.fieldml.util.general;

public interface ImmutableList<T extends Object>
{
    public int size();
    
    public T get( int index );
}
