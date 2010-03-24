package fieldmlx.util;

import fieldml.annotations.SerializationAsString;

public class SimpleMapEntry<K, V>
{
    @SerializationAsString
    public final K key;
    
    @SerializationAsString
    public final V value;
    
    public SimpleMapEntry( K key, V value )
    {
        this.key = key;
        this.value = value;
    }
}
