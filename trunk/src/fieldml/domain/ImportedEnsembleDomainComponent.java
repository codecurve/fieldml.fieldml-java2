package fieldml.domain;

public class ImportedEnsembleDomainComponent
    extends EnsembleDomainComponent
{
    //This is only here for serialization.
    public final String sourceDomain;
    
    //This is only here for serialization.
    public final String sourceComponent;
    
    private final Iterable<Integer> values;


    public ImportedEnsembleDomainComponent( String name, EnsembleDomain sourceDomain, String sourceComponent )
    {
        super( name );
        
        this.sourceDomain = sourceDomain.name;
        this.sourceComponent = sourceComponent; 
        
        values = sourceDomain.getComponent( sourceComponent ).getValues();
    }


    @Override
    public Iterable<Integer> getValues()
    {
        return values;
    }
}
