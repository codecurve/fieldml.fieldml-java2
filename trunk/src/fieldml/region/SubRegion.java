package fieldml.region;

public class SubRegion
    extends Region
{
    private final Region parent;
    
    public SubRegion( String name, Region parent )
    {
        super( name );
        
        this.parent = parent;

        assert parent.getSubregion( name ) == null : "Region " + name + " already exists in parent " + parent.getName();

        parent.addSubregion( this );
    }

    @Override
    public Region getLibrary()
    {
        return parent.getLibrary();
    }

}
