package fieldml.field;

import java.util.ArrayList;
import java.util.List;

import fieldml.annotations.SerializeToString;
import fieldml.domain.Domain;
import fieldml.domain.EnsembleDomain;
import fieldml.value.DomainValue;

public class MappingField
    extends Field
{
    @SerializeToString()
    public EnsembleDomain[] parameterDomains;

    @SerializeToString()
    public Domain valueDomain;
    
    public class MapEntry
    {
        public DomainValue value;
        
        public int[] keys;
        
        private MapEntry( DomainValue value, int[] keys )
        {
            this.value = value;
            this.keys = keys;
        }
    }
    
    public final List<MapEntry> entries;


    public MappingField( String name, Domain valueDomain, EnsembleDomain... parameterDomains )
    {
        super( name );

        this.parameterDomains = parameterDomains;

        this.valueDomain = valueDomain;
        
        entries = new ArrayList<MapEntry>();
    }


    public void setValue( DomainValue value, int... keys )
    {
        entries.add( new MapEntry( value, keys ) );
    }
}
