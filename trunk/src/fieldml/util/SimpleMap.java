package fieldml.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A crude map, designed for 'nice' serialization. Specifically, the key/value pairs are 
 * always serialized as strings.
 */
public class SimpleMap<K, V>
    implements Iterable<SimpleMapEntry<K, V>>
{
    private List<SimpleMapEntry<K, V>> entries;


    public SimpleMap()
    {
        entries = new ArrayList<SimpleMapEntry<K, V>>();
    }


    public void put( K key, V value )
    {
        SimpleMapEntry<K, V> e;
        for( Iterator<SimpleMapEntry<K, V>> i = entries.iterator(); i.hasNext(); )
        {
            e = i.next();

            if( e.key.equals( key ) )
            {
                i.remove();
            }
        }

        entries.add( new SimpleMapEntry<K, V>( key, value ) );
    }


    public V get( K key )
    {
        SimpleMapEntry<K, V> e;
        for( Iterator<SimpleMapEntry<K, V>> i = entries.iterator(); i.hasNext(); )
        {
            e = i.next();

            if( e.key.equals( key ) )
            {
                return e.value;
            }
        }

        return null;
    }


    @Override
    public Iterator<SimpleMapEntry<K, V>> iterator()
    {
        return entries.iterator();
    }
}
