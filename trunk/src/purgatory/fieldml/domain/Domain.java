package purgatory.fieldml.domain;

import java.util.ArrayList;
import java.util.List;

import purgatory.fieldml.exception.BadFieldmlParameterException;
import purgatory.fieldml.exception.FieldmlException;
import purgatory.fieldml.util.FieldmlObject;
import purgatory.fieldml.util.FieldmlObjectManager;


public abstract class Domain
    implements FieldmlObject
{
    /**
     * A globally unique integer identifying the domain, useful for internal
     * (inter-process) and external (client-server) communication. In order to
     * remain globally unique, this id number cannot be user-supplied. Domains
     * can be imported from external sources, and can therefore have id numbers
     * which are not known in advance by the user of the API when creating their
     * own domains.
     */
    private final int id;

    /**
     * A locally unique string.
     */
    private final String name;

    private final List<String> componentNames = new ArrayList<String>();


    public Domain( FieldmlObjectManager<Domain> manager, String name )
        throws FieldmlException
    {
        this.name = name;

        id = manager.add( this );
    }


    @Override
    public String toString()
    {
        return "Domain " + getName() + " (" + id + ")";
    }


    public int getId()
    {
        return id;
    }


    public String getName()
    {
        return name;
    }


    public int getComponentCount()
    {
        return componentNames.size();
    }


    public String getComponentName( int componentIndex )
        throws FieldmlException
    {
        if( ( componentIndex < 0 ) || ( componentIndex >= componentNames.size() ) )
        {
            throw new BadFieldmlParameterException();
        }

        return componentNames.get( componentIndex );
    }


    // This should not be directly invoked except by descendant classes.
    void addComponent( String componentName )
        throws FieldmlException
    {
        if( getComponentIndex( componentName ) != -1 )
        {
            throw new BadFieldmlParameterException();
        }

        componentNames.add( componentName );
    }


    public int getComponentIndex( String componentName )
    {
        return componentNames.indexOf( componentName );
    }
}
