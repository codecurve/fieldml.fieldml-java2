package fieldml.region;

public class WorldRegion
    extends Region
{
    private static final String WORLD_NAME = "world";
    
    private final Library library;


    public WorldRegion()
    {
        super( WORLD_NAME );

        library = new Library( this );
    }


    @Override
    public Library getLibrary()
    {
        return library;
    }
}
