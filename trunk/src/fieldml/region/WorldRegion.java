package fieldml.region;

import fieldmlx.region.Library;

public class WorldRegion
    extends Region
{
    private static final String WORLD_NAME = "world";
    
    private final Region library;


    public WorldRegion()
    {
        super( WORLD_NAME );

        library = Library.getLibrarySingleton(this);
    }


    @Override
    public Region getLibrary()
    {
        return library;
    }
}
