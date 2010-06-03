package fieldmlx.evaluator;

import fieldmlx.util.SimpleMap;

public class Markup
    extends SimpleMap<String, String>
{
    public Markup()
    {
    }


    public boolean has( String attribute, String value )
    {
        String v = get( attribute );
        if( v == null )
        {
            return false;
        }

        return v.equals( value );
    }
}
