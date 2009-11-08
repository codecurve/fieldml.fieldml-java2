package purgatory.fieldml.util;

import java.io.FileReader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import purgatory.fieldml.FieldML;
import purgatory.fieldml.implementation.FieldMLJava;


public class FieldmlSaxHandler
    extends DefaultHandler
{
    private enum ParsingState
    {
        NONE,
        DISCRETE_DOMAIN,
        DISCRETE_COMPONENT,
        CONTINUOUS_DOMAIN,
    }

    private final StringBuilder characters;

    private String currentName;

    private ParsingState state;

    private final FieldML fieldml;
    
    int err;

    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes )
    {
        if( state == ParsingState.NONE )
        {
            if( qName.compareTo( "discrete_domain" ) == 0 )
            {
                if( ( attributes.getValue( "name" ) == null ) || ( attributes.getValue( "value" ) == null ) )
                {
                    // ERROR
                }

                String domainValue = attributes.getValue( "value" );

                if( domainValue.compareTo( "index" ) == 0 )
                {
                    err = fieldml.FieldML_BeginDiscreteDomain( attributes.getValue( "name" ) );
                    state = ParsingState.DISCRETE_DOMAIN;
                }
                else
                {
                    // ERROR
                }
            }
            else if( qName.compareTo( "continuous_domain" ) == 0 )
            {
                if( ( attributes.getValue( "name" ) == null ) || ( attributes.getValue( "value" ) == null ) )
                {
                    // ERROR
                }

                String domainValue = attributes.getValue( "value" );

                if( domainValue.compareTo( "real" ) == 0 )
                {
                    err = fieldml.FieldML_BeginContinuousDomain( attributes.getValue( "name" ) );
                    state = ParsingState.CONTINUOUS_DOMAIN;
                }
                else
                {
                    // ERROR
                }
            }
        }
        else if( state == ParsingState.CONTINUOUS_DOMAIN )
        {
            if( qName.compareTo( "component" ) == 0 )
            {
                if( attributes.getValue( "id" ) == null )
                {
                    // ERROR
                }

                double min = Double.NEGATIVE_INFINITY;
                double max = Double.POSITIVE_INFINITY;

                if( attributes.getValue( "min" ) != null )
                {
                    min = Double.parseDouble( attributes.getValue( "min" ) );
                }
                if( attributes.getValue( "max" ) != null )
                {
                    max = Double.parseDouble( attributes.getValue( "max" ) );
                }

                fieldml.FieldML_AddContinuousDomainComponent( attributes.getValue( "id" ), min, max );
            }
        }
        else if( state == ParsingState.DISCRETE_DOMAIN )
        {
            if( qName.compareTo( "component" ) == 0 )
            {
                if( attributes.getValue( "id" ) == null )
                {
                    // ERROR
                }

                currentName = attributes.getValue( "id" );

                characters.setLength( 0 );

                state = ParsingState.DISCRETE_COMPONENT;
            }
        }
    }


    @Override
    public void endElement( String uri, String localName, String qName )
    {
        if( state == ParsingState.CONTINUOUS_DOMAIN )
        {
            if( qName.compareTo( "continuous_domain" ) == 0 )
            {
                err = fieldml.FieldML_EndDomain();
                state = ParsingState.NONE;
            }
        }
        else if( state == ParsingState.DISCRETE_DOMAIN )
        {
            if( qName.compareTo( "discrete_domain" ) == 0 )
            {
                err = fieldml.FieldML_EndDomain();
                state = ParsingState.NONE;
            }
        }
        else if( state == ParsingState.DISCRETE_COMPONENT )
        {
            if( qName.compareTo( "component" ) == 0 )
            {
                // Parse a space (and/or comma?) separated list of values. Currently, only integer values are supported.
                int[] values = getValues( characters.toString() );
                
                fieldml.FieldML_AddDiscreteDomainComponent( currentName, values, values.length ); 
            }
        }
    }


    private int[] getValues( String string )
    {
    	// TODO: getValues does not exist yet, so for interim, hard coding return value.
        return new int[] { 1, 2, 3 };
    }


    @Override
    public void characters( char[] ch, int start, int length )
    {
        characters.append( String.copyValueOf( ch, start, length ) );
    }


    private FieldmlSaxHandler()
    {
        characters = new StringBuilder();
        
        fieldml = new FieldMLJava();
    }


    public static void main( String[] args )
    {
        try
        {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            FieldmlSaxHandler handler = new FieldmlSaxHandler();
            xr.setContentHandler( handler );
            xr.setErrorHandler( handler );

            // Parse each file provided on the
            // command line.
            for( int i = 0; i < args.length; i++ )
            {
                FileReader r = new FileReader( args[i] );
                xr.parse( new InputSource( r ) );
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
