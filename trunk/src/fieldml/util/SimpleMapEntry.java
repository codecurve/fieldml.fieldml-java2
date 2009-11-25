package fieldml.util;

import fieldml.annotations.SerializationAsString;

public class SimpleMapEntry<K, V>
{
    @SerializationAsString
    public final K key;
    
    @SerializationAsString
    public final V value;
    
    SimpleMapEntry( K key, V value )
    {
        this.key = key;
        this.value = value;
    }
}
