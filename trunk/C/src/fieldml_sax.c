#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

#include <libxml/sax.h>
#include <libxml/globals.h>
#include <libxml/xmlerror.h>
#include <libxml/parser.h>
#include <libxml/xmlmemory.h>

#include "string_table.h"
#include "fieldml_parse.h"
#include "fieldml_sax.h"
#include "string_const.h"

static int noent = 0;

char attributeBuffer[1024];

//========================================================================
//
// Utils
//
//========================================================================

// strndup not available on all platforms.
static char *strdupN( const xmlChar *str, unsigned int n )
{
    char *dup;
    
    if( strlen( str ) < n )
    {
        return _strdup( str );
    }
    
    dup = malloc( n + 1 );
    memcpy( dup, str, n );
    dup[n] = 0;
    
    return dup;
}

static SaxAttributes *createAttributes( const int attributeCount, const xmlChar ** attributes )
{
    int i, attributeNumber;
    
    SaxAttributes *saxAttributes = calloc( 1, sizeof( SaxAttributes ) );
    
    saxAttributes->count = attributeCount;
    saxAttributes->attributes = calloc( attributeCount, sizeof( SaxAttribute ) );
    
    attributeNumber = 0;
    for( i = 0; i < attributeCount * 5; i += 5 )
    {
        saxAttributes->attributes[attributeNumber].attribute = _strdup( attributes[i + 0] );
        saxAttributes->attributes[attributeNumber].prefix = _strdup( attributes[i + 1] );
        saxAttributes->attributes[attributeNumber].URI = _strdup( attributes[i + 2] );
        saxAttributes->attributes[attributeNumber].value = strdupN( attributes[i + 3], attributes[i + 4] - attributes[i + 3] );
        attributeNumber++;
    }
    
    return saxAttributes;
}


static void destroyAttributes( SaxAttributes *saxAttributes )
{
    int i;
    
    for( i = 0; i < saxAttributes->count; i++ )
    {
        free( saxAttributes->attributes[i].attribute );
        free( saxAttributes->attributes[i].prefix );
        free( saxAttributes->attributes[i].URI );
        free( saxAttributes->attributes[i].value );
    }
    free( saxAttributes->attributes );
    
    free( saxAttributes );
}


char * getAttribute( SaxAttributes *saxAttributes, char *attribute )
{
    int i;
    for( i = 0; i < saxAttributes->count; i++ )
    {
        if( strcmp( attribute, saxAttributes->attributes[i].attribute ) == 0 )
        {
            return saxAttributes->attributes[i].value;
        }
    }
    
    return NULL;
}

//========================================================================
//
// SAX handlers
//
//========================================================================

static int isStandalone( void *context )
{
    fprintf( stdout, "SAX.isStandalone( )\n" );
    return 0;
}


static int hasInternalSubset( void *context )
{
    fprintf( stdout, "SAX.hasInternalSubset( )\n" );
    return 0;
}


static int hasExternalSubset( void *context )
{
    fprintf( stdout, "SAX.hasExternalSubset( )\n" );
    return 0;
}

static void onInternalSubset( void *context, const xmlChar *name, const xmlChar *externalID, const xmlChar *systemID )
{
}

static void externalSubset( void *context, const xmlChar *name, const xmlChar *externalID, const xmlChar *systemID )
{
}


static xmlParserInputPtr resolveEntity( void *context, const xmlChar *publicId, const xmlChar *systemId )
{
    /* xmlParserCtxtPtr ctxt = ( xmlParserCtxtPtr ) ctx; */


    fprintf( stdout, "SAX.resolveEntity( " );
    if ( publicId != NULL )
    fprintf( stdout, "%s", ( char * )publicId );
    else
    fprintf( stdout, " " );
    if ( systemId != NULL )
    fprintf( stdout, ", %s )\n", ( char * )systemId );
    else
    fprintf( stdout, ", )\n" );
    return NULL;
}


static xmlEntityPtr getEntity( void *context, const xmlChar *name )
{
    fprintf( stdout, "SAX.getEntity( %s )\n", name );
    return NULL;
}


static xmlEntityPtr getParameterEntity( void *context, const xmlChar *name )
{
    fprintf( stdout, "SAX.getParameterEntity( %s )\n", name );
    return NULL;
}


static void entityDecl( void *context, const xmlChar *name, int type, const xmlChar *publicId,
                       const xmlChar *systemId, xmlChar *content )
{
    const xmlChar *nullstr = (xmlChar*)"null";
    if ( publicId == NULL )
        publicId = nullstr;
    if ( systemId == NULL )
        systemId = nullstr;
    if ( content == NULL )
        content = ( xmlChar * )nullstr;
    fprintf( stdout, "SAX.entityDecl( %s, %d, %s, %s, %s )\n",
            name, type, publicId, systemId, content );
}


static void attributeDecl( void *context, const xmlChar * elem, const xmlChar * name, int type, int def,
                   const xmlChar * defaultValue, xmlEnumerationPtr tree )
{
    if ( defaultValue == NULL )
        fprintf( stdout, "SAX.attributeDecl( %s, %s, %d, %d, NULL, ... )\n",
                elem, name, type, def );
    else
        fprintf( stdout, "SAX.attributeDecl( %s, %s, %d, %d, %s, ... )\n",
                elem, name, type, def, defaultValue );
    xmlFreeEnumeration( tree );
}


static void elementDecl( void *context, const xmlChar *name, int type,
        xmlElementContentPtr content )
{
    fprintf( stdout, "SAX.elementDecl( %s, %d, ... )\n",
            name, type );
}


static void notationDecl( void *context, const xmlChar *name,
         const xmlChar *publicId, const xmlChar *systemId )
{
    fprintf( stdout, "SAX.notationDecl( %s, %s, %s )\n",
            ( char * ) name, ( char * ) publicId, ( char * ) systemId );
}


static void unparsedEntityDecl( void *context, const xmlChar *name, const xmlChar *publicId,
                               const xmlChar *systemId, const xmlChar *notationName )
{
    const xmlChar *nullstr = (xmlChar*)"null";

    if ( publicId == NULL )
        publicId = nullstr;
    if ( systemId == NULL )
        systemId = nullstr;
    if ( notationName == NULL )
        notationName = nullstr;
    fprintf( stdout, "SAX.unparsedEntityDecl( %s, %s, %s, %s )\n",
            ( char * ) name, ( char * ) publicId, ( char * ) systemId,
        ( char * ) notationName );
}


static void setDocumentLocator( void *context, xmlSAXLocatorPtr loc )
{
    SaxContext *saxContext = (SaxContext*)context;

    saxContext->locator = loc;
    fprintf( stdout, "SAX.setDocumentLocator( )\n" );
}


static void startDocument( void *context )
{
    fprintf( stdout, "SAX.startDocument( )\n" );
}


static void endDocument( void *context )
{
    fprintf( stdout, "SAX.endDocument( )\n" );
}


static void characters( void *context, const xmlChar *ch, int len )
{
    char output[40];
    int i, allWhite;
    
    allWhite = 1;
    for ( i = 0;( i<len ) && ( i < 30 );i++ )
    {
        output[i] = ch[i];
        if( ch[i] > ' ' )
        {
            allWhite = 0;
        }
    }
    output[i] = 0;
    
    if( allWhite )
    {
        return;
    }

    fprintf( stdout, "SAX.characters( %s, %d )\n", output, len );
}


static void reference( void *context, const xmlChar *name )
{
    fprintf( stdout, "SAX.reference( %s )\n", name );
}


static void ignorableWhitespace( void *context, const xmlChar *ch, int len )
{
    char output[40];
    int i;

    for ( i = 0;( i<len ) && ( i < 30 );i++ )
    output[i] = ch[i];
    output[i] = 0;
    fprintf( stdout, "SAX.ignorableWhitespace( %s, %d )\n", output, len );
}


static void processingInstruction( void *context, const xmlChar *target, const xmlChar *data )
{
    if ( data != NULL )
    fprintf( stdout, "SAX.processingInstruction( %s, %s )\n",
        ( char * ) target, ( char * ) data );
    else
    fprintf( stdout, "SAX.processingInstruction( %s, NULL )\n",
        ( char * ) target );
}


static void cdataBlock( void *context, const xmlChar *value, int len )
{
    fprintf( stdout, "SAX.pcdata( %.20s, %d )\n",
        ( char * ) value, len );
}


static void comment( void *context, const xmlChar *value )
{
    //We don't care about comments.
}


static void XMLCDECL warning( void *context, const char *msg, ... )
{
    va_list args;

    va_start( args, msg );
    fprintf( stdout, "SAX.warning: " );
    vfprintf( stdout, msg, args );
    va_end( args );
}


static void XMLCDECL error( void *context, const char *msg, ... )
{
    va_list args;

    va_start( args, msg );
    fprintf( stdout, "SAX.error: " );
    vfprintf( stdout, msg, args );
    va_end( args );
}


static void XMLCDECL fatalError( void *context, const char *msg, ... )
{
    va_list args;

    va_start( args, msg );
    fprintf( stdout, "SAX.fatalError: " );
    vfprintf( stdout, msg, args );
    va_end( args );
}


static void startElementNs( void *context, const xmlChar *name, const xmlChar *prefix, const xmlChar *URI,
            int nb_namespaces, const xmlChar **namespaces,
            int nb_attributes, int nb_defaulted, const xmlChar **attributes )
{
    int i, len;
    
    SaxAttributes *saxAttributes = createAttributes( nb_attributes, attributes );
    SaxContext *saxContext = (SaxContext*)context;

    switch( saxContext->state )
    {
    case FML_ROOT:
        if( strcmp( name, FIELDML_TAG ) == 0 )
        {
            saxContext->state = FML_FIELDML;
        }
        break;
    case FML_FIELDML:
        if( strcmp( name, REGION_TAG ) == 0 )
        {
            saxContext->state = FML_REGION;
        }
        break;
    case FML_ENSEMBLE_DOMAIN:
        if( strcmp( name, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG ) == 0 )
        {
            startContiguousBounds( saxContext, saxAttributes );
            saxContext->state = FML_ENSEMBLE_DOMAIN_BOUNDS;
        }
        break;
    case FML_CONTINUOUS_IMPORT:
        if( strcmp( name, CONTINUOUS_ALIAS_TAG ) == 0 )
        {
            saxContext->state = FML_CONTINUOUS_IMPORT_C_ALIASES;
        }
        else if( strcmp( name, ENSEMBLE_ALIAS_TAG ) == 0 )
        {
            saxContext->state = FML_CONTINUOUS_IMPORT_E_ALIASES;
        }
        break;
    case FML_ENSEMBLE_PARAMETERS:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            saxContext->state = FML_SEMI_DENSE_ENSEMBLE;
        }
        break;
    case FML_CONTINUOUS_PARAMETERS:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            saxContext->state = FML_SEMI_DENSE_CONTINUOUS;
        }
        break;
    case FML_CONTINUOUS_IMPORT_C_ALIASES:
    case FML_CONTINUOUS_IMPORT_E_ALIASES:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            continuousImportAlias( saxContext, saxAttributes );
        }
        break;
    case FML_REGION:
        if( strcmp( name, ENSEMBLE_DOMAIN_TAG ) == 0 )
        {
            startEnsembleDomain( saxContext, saxAttributes );
            saxContext->state = FML_ENSEMBLE_DOMAIN;
            break;
        }
        if( strcmp( name, CONTINUOUS_DOMAIN_TAG ) == 0 )
        {
            startContinuousDomain( saxContext, saxAttributes );
            saxContext->state = FML_CONTINUOUS_DOMAIN;
            break;
        }
        if( strcmp( name, IMPORTED_CONTINUOUS_TAG ) == 0 )
        {
            startContinuousImport( saxContext, saxAttributes );
            saxContext->state = FML_CONTINUOUS_IMPORT;
            break;
        }
        if( strcmp( name, ENSEMBLE_PARAMETERS_TAG ) == 0 )
        {
            startEnsembleParameters( saxContext, saxAttributes );
            saxContext->state = FML_ENSEMBLE_PARAMETERS;
            break;
        }
        if( strcmp( name, CONTINUOUS_PARAMETERS_TAG ) == 0 )
        {
            startContinuousParameters( saxContext, saxAttributes );
            saxContext->state = FML_CONTINUOUS_PARAMETERS;
            break;
        }
        //FALLTHROUGH
    default:
        fprintf( stdout, "SAX.startElementNs( %s", ( char * ) name );
        if ( prefix == NULL )
        fprintf( stdout, ", NULL" );
        else
        fprintf( stdout, ", %s", ( char * ) prefix );
        if ( URI == NULL )
        fprintf( stdout, ", NULL" );
        else
        fprintf( stdout, ", '%s'", ( char * ) URI );
        fprintf( stdout, ", %d", nb_namespaces );

        fprintf( stdout, ", %d, %d", nb_attributes, nb_defaulted );
        if ( attributes != NULL )
        {
            for ( i = 0;i < nb_attributes * 5;i += 5 )
            {
                if ( attributes[i + 1] != NULL )
                fprintf( stdout, ", %s:%s='", attributes[i + 1], attributes[i] );
                else
                fprintf( stdout, ", %s='", attributes[i] );
                
                len = ( int )( attributes[i + 4] - attributes[i + 3] );
                if( len > 1023 )
                {
                    len = 1023;
                }
                strncpy( attributeBuffer, attributes[i + 3], len );
                attributeBuffer[ len ] = 0;
                
                fprintf( stdout, "%s'", attributeBuffer );
            }
            fprintf( stdout, " )\n" );
        }
        break;
    }
    
    destroyAttributes( saxAttributes );
}


static void endElementNs( void *context, const xmlChar *name, const xmlChar *prefix, const xmlChar *URI )
{
    SaxContext *saxContext = (SaxContext*)context;

    switch( saxContext->state )
    {
    case FML_FIELDML:
        if( strcmp( name, FIELDML_TAG ) == 0 )
        {
            saxContext->state = FML_ROOT;
        }
        break;
    case FML_ENSEMBLE_DOMAIN_BOUNDS:
        if( strcmp( name, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG ) == 0 )
        {
            saxContext->state = FML_ENSEMBLE_DOMAIN;
        }
        break;
    case FML_ENSEMBLE_DOMAIN:
        if( strcmp( name, ENSEMBLE_DOMAIN_TAG ) == 0 )
        {
            endEnsembleDomain( saxContext );
            saxContext->state = FML_REGION;
        }
        break;
    case FML_CONTINUOUS_DOMAIN:
        if( strcmp( name, CONTINUOUS_DOMAIN_TAG ) == 0 )
        {
            endContinuousDomain( saxContext );
            saxContext->state = FML_REGION;
        }
        break;
    case FML_CONTINUOUS_IMPORT_C_ALIASES:
        if( strcmp( name, CONTINUOUS_ALIAS_TAG ) == 0 )
        {
            saxContext->state = FML_CONTINUOUS_IMPORT;
        }
        break;
    case FML_CONTINUOUS_IMPORT_E_ALIASES:
        if( strcmp( name, ENSEMBLE_ALIAS_TAG ) == 0 )
        {
            saxContext->state = FML_CONTINUOUS_IMPORT;
        }
        break;
    case FML_CONTINUOUS_IMPORT:
        if( strcmp( name, IMPORTED_CONTINUOUS_TAG ) == 0 )
        {
            endContinuousImport( saxContext );
            saxContext->state = FML_REGION;
        }
        break;
    case FML_ENSEMBLE_PARAMETERS:
        if( strcmp( name, ENSEMBLE_PARAMETERS_TAG ) == 0 )
        {
            endEnsembleParameters( saxContext );
            saxContext->state = FML_REGION;
        }
        break;
    case FML_SEMI_DENSE_ENSEMBLE:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            saxContext->state = FML_ENSEMBLE_PARAMETERS;
        }
        break;
    case FML_CONTINUOUS_PARAMETERS:
        if( strcmp( name, CONTINUOUS_PARAMETERS_TAG ) == 0 )
        {
            endContinuousParameters( saxContext );
            saxContext->state = FML_REGION;
        }
        break;
    case FML_SEMI_DENSE_CONTINUOUS:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            saxContext->state = FML_CONTINUOUS_PARAMETERS;
        }
        break;
    case FML_REGION:
        if( strcmp( name, REGION_TAG ) == 0 )
        {
            saxContext->state = FML_FIELDML;
        }
        //FALLTHROUGH
    default:
        fprintf( stdout, "SAX.endElementNs( %s", ( char * ) name );
        if ( prefix == NULL )
        fprintf( stdout, ", NULL" );
        else
        fprintf( stdout, ", %s", ( char * ) prefix );
        if ( URI == NULL )
        fprintf( stdout, ", NULL )\n" );
        else
        fprintf( stdout, ", '%s' )\n", ( char * ) URI );
        break;
    }
}


static xmlSAXHandler SAX2HandlerStruct =
{
    onInternalSubset,
    isStandalone,
    hasInternalSubset,
    hasExternalSubset,
    resolveEntity,
    getEntity,
    entityDecl,
    notationDecl,
    attributeDecl,
    elementDecl,
    unparsedEntityDecl,
    setDocumentLocator,
    startDocument,
    endDocument,
    NULL,
    NULL,
    reference,
    characters,
    ignorableWhitespace,
    processingInstruction,
    comment,
    warning,
    error,
    fatalError,
    getParameterEntity,
    cdataBlock,
    externalSubset,
    XML_SAX2_MAGIC,
    NULL,
    startElementNs,
    endElementNs,
    NULL
};

static xmlSAXHandlerPtr SAX2Handler = &SAX2HandlerStruct;

//========================================================================
//
// Main
//
//========================================================================

static void parseAndPrintFile( char *filename )
{
    int res;
    SaxContext context;

    context.state = FML_ROOT;
    context.parse = createFieldmlParse();

    res = xmlSAXUserParseFile( SAX2Handler, &context, filename );
    if ( res != 0 )
    {
        fprintf( stdout, "xmlSAXUserParseFile returned error %d\n", res );
    }

    dumpFieldmlParse( context.parse );

    destroyFieldmlParse( context.parse );
}


int main( int argc, char **argv )
{
    int i;
    int files = 0;

    LIBXML_TEST_VERSION

    xmlSubstituteEntitiesDefault( 1 );
    for ( i = 1; i < argc ; i++ )
    {
        parseAndPrintFile( argv[i] );
    }
    xmlCleanupParser( );
    xmlMemoryDump( );

    return 0;
}

