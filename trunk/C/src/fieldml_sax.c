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


//========================================================================
//
// Structs
//
//========================================================================

typedef enum _SaxState
{
    FML_ROOT,
    FML_FIELDML,
    FML_REGION,

    FML_ENSEMBLE_DOMAIN,
    FML_ENSEMBLE_DOMAIN_BOUNDS,
    
    FML_CONTINUOUS_DOMAIN,

    FML_CONTINUOUS_IMPORT,
    FML_CONTINUOUS_ALIASES,
    FML_ENSEMBLE_ALIASES,

    FML_ENSEMBLE_PARAMETERS,
    FML_CONTINUOUS_PARAMETERS,

    FML_CONTINUOUS_VARIABLE,

    FML_ENSEMBLE_VARIABLE,

    FML_SEMI_DENSE,
    FML_DENSE_INDEXES,
    FML_SPARSE_INDEXES,
    FML_SEMIDENSE_INLINE_DATA,
    FML_SEMIDENSE_FILE_DATA,
    
    FML_CONTINUOUS_PIECEWISE,
    FML_ELEMENT_EVALUATORS,
    
    FML_CONTINUOUS_AGGREGATE,
    FML_SOURCE_FIELDS,
}
SaxState;


typedef struct _SaxContext
{
    IntStack *state;

    FieldmlContext *fieldmlContext;
}
SaxContext;


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
    return 0;
}


static int hasInternalSubset( void *context )
{
    return 0;
}


static int hasExternalSubset( void *context )
{
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
    return NULL;
}


static xmlEntityPtr getEntity( void *context, const xmlChar *name )
{
    return NULL;
}


static xmlEntityPtr getParameterEntity( void *context, const xmlChar *name )
{
    return NULL;
}


static void onEntityDecl( void *context, const xmlChar *name, int type, const xmlChar *publicId,
                       const xmlChar *systemId, xmlChar *content )
{
}


static void onAttributeDecl( void *context, const xmlChar * elem, const xmlChar * name, int type, int def,
                   const xmlChar * defaultValue, xmlEnumerationPtr tree )
{
    xmlFreeEnumeration( tree );
}


static void onElementDecl( void *context, const xmlChar *name, int type,
        xmlElementContentPtr content )
{
}


static void onNotationDecl( void *context, const xmlChar *name,
         const xmlChar *publicId, const xmlChar *systemId )
{
}


static void onUnparsedEntityDecl( void *context, const xmlChar *name, const xmlChar *publicId,
                               const xmlChar *systemId, const xmlChar *notationName )
{
}


static void setDocumentLocator( void *context, xmlSAXLocatorPtr loc )
{
    /*
    At the moment (libxml 2.7.2), this is worse than useless.
    The locator only wraps functions which require the library's internal
    parsing context, which is only passed into this function if you pass
    in 'NULL' as the user-data which initiating the SAX parse...
    which is exactly what we don't want to do.
    */
}


static void onStartDocument( void *context )
{
}


static void onEndDocument( void *context )
{
}


static void onCharacters( void *context, const xmlChar *ch, int len )
{
    SaxContext *saxContext = (SaxContext*)context;

    switch( peekInt( saxContext->state ) )
    {
    case FML_SEMIDENSE_INLINE_DATA:
        semidenseInlineData( saxContext->fieldmlContext, ch, len );
        break;
    default:
        break;
    }
}


static void onReference( void *context, const xmlChar *name )
{
}


static void onIgnorableWhitespace( void *context, const xmlChar *ch, int len )
{
}


static void onProcessingInstruction( void *context, const xmlChar *target, const xmlChar *data )
{
}


static void onCdataBlock( void *context, const xmlChar *value, int len )
{
}


static void comment( void *context, const xmlChar *value )
{
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


static void onStartElementNs( void *context, const xmlChar *name, const xmlChar *prefix, const xmlChar *URI,
            int nb_namespaces, const xmlChar **namespaces,
            int nb_attributes, int nb_defaulted, const xmlChar **attributes )
{
    int i, len;
    
    SaxAttributes *saxAttributes = createAttributes( nb_attributes, attributes );
    SaxContext *saxContext = (SaxContext*)context;

    switch( peekInt( saxContext->state ) )
    {
    case FML_ROOT:
        if( strcmp( name, FIELDML_TAG ) == 0 )
        {
            pushInt( saxContext->state, FML_FIELDML );
        }
        break;
    case FML_FIELDML:
        if( strcmp( name, REGION_TAG ) == 0 )
        {
            pushInt( saxContext->state, FML_REGION );
        }
        break;
    case FML_ENSEMBLE_DOMAIN:
        if( strcmp( name, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG ) == 0 )
        {
            startContiguousBounds( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_ENSEMBLE_DOMAIN_BOUNDS );
        }
        break;
    case FML_CONTINUOUS_IMPORT:
        if( strcmp( name, CONTINUOUS_ALIAS_TAG ) == 0 )
        {
            pushInt( saxContext->state, FML_CONTINUOUS_ALIASES );
        }
        else if( strcmp( name, ENSEMBLE_ALIAS_TAG ) == 0 )
        {
            pushInt( saxContext->state, FML_ENSEMBLE_ALIASES );
        }
        break;
    case FML_ENSEMBLE_PARAMETERS:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            startSemidenseData( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_SEMI_DENSE );
        }
        break;
    case FML_CONTINUOUS_PARAMETERS:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            startSemidenseData( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_SEMI_DENSE );
        }
        break;
    case FML_CONTINUOUS_PIECEWISE:
    	if( strcmp( name, ELEMENT_EVALUATORS_TAG ) == 0 )
    	{
    		pushInt( saxContext->state, FML_ELEMENT_EVALUATORS );
    	}
    	break;
    case FML_CONTINUOUS_AGGREGATE:
    	if( strcmp( name, SOURCE_FIELDS_TAG ) == 0 )
    	{
    		pushInt( saxContext->state, FML_SOURCE_FIELDS );
    	}
    	break;
    case FML_SOURCE_FIELDS:
    	if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
    	{
    		onContinuousAggregateEntry( saxContext->fieldmlContext, saxAttributes );
    	}
    	break;
    case FML_ELEMENT_EVALUATORS:
    	if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
    	{
    		onContinuousPiecewiseEntry( saxContext->fieldmlContext, saxAttributes );
    	}
    	break;
    case FML_SEMI_DENSE:
        if( strcmp( name, DENSE_INDEXES_TAG ) == 0 )
        {
            pushInt( saxContext->state, FML_DENSE_INDEXES );
        }
        if( strcmp( name, SPARSE_INDEXES_TAG ) == 0 )
        {
            pushInt( saxContext->state, FML_SPARSE_INDEXES );
        }
        if( strcmp( name, INLINE_DATA_TAG ) == 0 )
        {
            semidenseStartInlineData( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_SEMIDENSE_INLINE_DATA );
        }
        if( strcmp( name, FILE_DATA_TAG ) == 0 )
        {
            semidenseFileData( saxContext->fieldmlContext, saxAttributes );
        }
        break;
    case FML_DENSE_INDEXES:
        if( strcmp( name, ENTRY_TAG ) == 0 )
        {
            semidenseIndex( saxContext->fieldmlContext, saxAttributes, 0 );
        }
        break;
    case FML_SPARSE_INDEXES:
        if( strcmp( name, ENTRY_TAG ) == 0 )
        {
            semidenseIndex( saxContext->fieldmlContext, saxAttributes, 1 );
        }
        break;
    case FML_CONTINUOUS_ALIASES:
    case FML_ENSEMBLE_ALIASES:
        if( strcmp( name, SIMPLE_MAP_ENTRY_TAG ) == 0 )
        {
            continuousImportAlias( saxContext->fieldmlContext, saxAttributes );
        }
        break;
    case FML_REGION:
        if( strcmp( name, ENSEMBLE_DOMAIN_TAG ) == 0 )
        {
            startEnsembleDomain( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_ENSEMBLE_DOMAIN );
            break;
        }
        if( strcmp( name, CONTINUOUS_DOMAIN_TAG ) == 0 )
        {
            startContinuousDomain( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_CONTINUOUS_DOMAIN );
            break;
        }
        if( strcmp( name, IMPORTED_CONTINUOUS_TAG ) == 0 )
        {
            startContinuousImport( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_CONTINUOUS_IMPORT );
            break;
        }
        if( strcmp( name, ENSEMBLE_PARAMETERS_TAG ) == 0 )
        {
            startEnsembleParameters( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_ENSEMBLE_PARAMETERS );
            break;
        }
        if( strcmp( name, CONTINUOUS_PARAMETERS_TAG ) == 0 )
        {
            startContinuousParameters( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_CONTINUOUS_PARAMETERS );
            break;
        }
        if( strcmp( name, CONTINUOUS_PIECEWISE_TAG ) == 0 )
        {
        	startContinuousPiecewise( saxContext->fieldmlContext, saxAttributes );
        	pushInt( saxContext->state, FML_CONTINUOUS_PIECEWISE );
        	break;
        }
        if( strcmp( name, CONTINUOUS_VARIABLE_TAG ) == 0 )
        {
            startVariable( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_CONTINUOUS_VARIABLE );
            break;
        }
        if( strcmp( name, CONTINUOUS_AGGREGATE_TAG ) == 0 )
        {
        	startContinuousAggregate( saxContext->fieldmlContext, saxAttributes );
        	pushInt( saxContext->state, FML_CONTINUOUS_AGGREGATE );
        	break;
        }
        if( strcmp( name, ENSEMBLE_VARIABLE_TAG ) == 0 )
        {
            startVariable( saxContext->fieldmlContext, saxAttributes );
            pushInt( saxContext->state, FML_ENSEMBLE_VARIABLE );
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
        	char attributeBuffer[1024];

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


static void onEndElementNs( void *context, const xmlChar *name, const xmlChar *prefix, const xmlChar *URI )
{
    SaxContext *saxContext = (SaxContext*)context;

    switch( peekInt( saxContext->state ) )
    {
    case FML_FIELDML:
        if( strcmp( name, FIELDML_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_DOMAIN_BOUNDS:
        if( strcmp( name, CONTIGUOUS_ENSEMBLE_BOUNDS_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_DOMAIN:
        if( strcmp( name, ENSEMBLE_DOMAIN_TAG ) == 0 )
        {
            endEnsembleDomain( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_DOMAIN:
        if( strcmp( name, CONTINUOUS_DOMAIN_TAG ) == 0 )
        {
            endContinuousDomain( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_ALIASES:
        if( strcmp( name, CONTINUOUS_ALIAS_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_ALIASES:
        if( strcmp( name, ENSEMBLE_ALIAS_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_IMPORT:
        if( strcmp( name, IMPORTED_CONTINUOUS_TAG ) == 0 )
        {
            endContinuousImport( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_PARAMETERS:
        if( strcmp( name, ENSEMBLE_PARAMETERS_TAG ) == 0 )
        {
            endEnsembleParameters( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_PIECEWISE:
        if( strcmp( name, CONTINUOUS_PIECEWISE_TAG ) == 0 )
        {
            endContinuousPiecewise( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_SOURCE_FIELDS:
    	if( strcmp( name, SOURCE_FIELDS_TAG ) == 0 )
    	{
    		popInt( saxContext->state );
    	}
    	break;
    case FML_CONTINUOUS_AGGREGATE:
    	if( strcmp( name, CONTINUOUS_AGGREGATE_TAG ) == 0 )
    	{
    		endContinuousAggregate( saxContext->fieldmlContext );
    		popInt( saxContext->state );
    	}
    	break;
    case FML_CONTINUOUS_PARAMETERS:
        if( strcmp( name, CONTINUOUS_PARAMETERS_TAG ) == 0 )
        {
            endContinuousParameters( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_SEMI_DENSE:
        if( strcmp( name, SEMI_DENSE_DATA_TAG ) == 0 )
        {
            endSemidenseData( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_ELEMENT_EVALUATORS:
    	if( strcmp( name, ELEMENT_EVALUATORS_TAG ) == 0 )
    	{
    		popInt( saxContext->state );
    	}
    	break;
    case FML_SPARSE_INDEXES:
        if( strcmp( name, SPARSE_INDEXES_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_DENSE_INDEXES:
        if( strcmp( name, DENSE_INDEXES_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_SEMIDENSE_INLINE_DATA:
        if( strcmp( name, INLINE_DATA_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_SEMIDENSE_FILE_DATA:
        if( strcmp( name, FILE_DATA_TAG ) == 0 )
        {
            popInt( saxContext->state );
        }
        break;
    case FML_CONTINUOUS_VARIABLE:
        if( strcmp( name, CONTINUOUS_VARIABLE_TAG ) == 0 )
        {
            endVariable( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_ENSEMBLE_VARIABLE:
        if( strcmp( name, ENSEMBLE_VARIABLE_TAG ) == 0 )
        {
            endVariable( saxContext->fieldmlContext );
            popInt( saxContext->state );
        }
        break;
    case FML_REGION:
        if( strcmp( name, REGION_TAG ) == 0 )
        {
            popInt( saxContext->state );
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
    onEntityDecl,
    onNotationDecl,
    onAttributeDecl,
    onElementDecl,
    onUnparsedEntityDecl,
    setDocumentLocator,
    onStartDocument,
    onEndDocument,
    NULL,
    NULL,
    onReference,
    onCharacters,
    onIgnorableWhitespace,
    onProcessingInstruction,
    comment,
    warning,
    error,
    fatalError,
    getParameterEntity,
    onCdataBlock,
    externalSubset,
    XML_SAX2_MAGIC,
    NULL,
    onStartElementNs,
    onEndElementNs,
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
    
    FieldmlParse *parse = createFieldmlParse();

    context.state = createIntStack();
    context.fieldmlContext = createFieldmlContext( parse );

    pushInt( context.state, FML_ROOT );

    res = xmlSAXUserParseFile( SAX2Handler, &context, filename );
    if( res != 0 ) 
    {
        fprintf( stdout, "xmlSAXUserParseFile returned error %d\n", res );
    }
    if( peekInt( context.state ) != -1 )
    {
        int s;
        fprintf( stdout, "Parser state not empty:\n");
        while( peekInt( context.state ) != -1 )
        {
            s = popInt( context.state );
            fprintf( stdout, "    %d\n", s );
        }
    }

    dumpFieldmlParse( parse );

    destroyFieldmlParse( parse );
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

