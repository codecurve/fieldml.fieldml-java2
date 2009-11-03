package fieldml.field;

import java.util.HashMap;
import java.util.Map;

public class Field
{
    public static final Map<String, Field> fields = new HashMap<String, Field>();

    public final String name;


    public Field( String name )
    {
        this.name = name;

        fields.put( name, this );
    }
}
