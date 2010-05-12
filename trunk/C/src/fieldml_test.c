#include <stdlib.h>
#include <stdio.h>

#include <libxml/sax.h>
#include <libxml/globals.h>
#include <libxml/xmlerror.h>
#include <libxml/parser.h>
#include <libxml/xmlmemory.h>
#include <libxml/xmlschemas.h>

#include "fieldml_api.h"


//========================================================================
//
// Main
//
//========================================================================

static int validate( char *filename )
{
    int res;
    xmlParserInputBufferPtr buffer;
    char *schema;
    xmlSchemaPtr schemas = NULL;
    xmlSchemaParserCtxtPtr sctxt;
    xmlSchemaValidCtxtPtr vctxt;
    
    LIBXML_TEST_VERSION

    xmlSubstituteEntitiesDefault( 1 );

    schema = "Fieldml.xsd";

    buffer = xmlParserInputBufferCreateFilename( filename, XML_CHAR_ENCODING_NONE );
    if( buffer == NULL )
    {
        return 0;
    }

    sctxt = xmlSchemaNewParserCtxt( schema );
    xmlSchemaSetParserErrors( sctxt, (xmlSchemaValidityErrorFunc)fprintf, (xmlSchemaValidityWarningFunc)fprintf, stderr );
    schemas = xmlSchemaParse( sctxt );
    if( schemas == NULL )
    {
        xmlGenericError( xmlGenericErrorContext, "WXS schema %s failed to compile\n", schema );
        schema = NULL;
    }
    xmlSchemaFreeParserCtxt( sctxt );

    vctxt = xmlSchemaNewValidCtxt( schemas );
    xmlSchemaSetValidErrors( vctxt, (xmlSchemaValidityErrorFunc)fprintf, (xmlSchemaValidityWarningFunc)fprintf, stderr );

    res = xmlSchemaValidateStream( vctxt, buffer, 0, NULL, NULL );
    if( res == 0 )
    {
        fprintf( stderr, "%s validates\n", filename );
    }
    else if( res > 0 )
    {
        fprintf( stderr, "%s fails to validate\n", filename );
    }
    else
    {
        fprintf( stderr, "%s validation generated an internal error\n", filename );
    }

    xmlSchemaFreeValidCtxt( vctxt );

    xmlSchemaFree( schemas );
    
    xmlCleanupParser( );
    xmlMemoryDump( );
    
    return res == 0;
}


void testRead( const char * filename )
{
    int i, j, count, count2;
    FmlObjectHandle oHandle;
    FmlParseHandle handle;
    DomainBoundsType boundsType;
    const int *swizzle;

    handle = Fieldml_ParseFile( filename );

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_DOMAIN );
    fprintf( stdout, "ContinuousDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_CONTINUOUS_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetDomainComponentEnsemble( handle, oHandle ) ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_ENSEMBLE_DOMAIN );
    fprintf( stdout, "EnsembleDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_ENSEMBLE_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetDomainComponentEnsemble( handle, oHandle ) ) );
        
        boundsType = Fieldml_GetDomainBoundsType( handle, oHandle );
        if( boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            fprintf( stdout, "    1...%d\n", Fieldml_GetContiguousBoundsCount( handle, oHandle ) );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_MESH_DOMAIN );
    fprintf( stdout, "MeshDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_MESH_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s, %s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetMeshElementDomain( handle, oHandle ) ),
            Fieldml_GetObjectName( handle, Fieldml_GetMeshXiDomain( handle, oHandle ) ) );
        boundsType = Fieldml_GetDomainBoundsType( handle, Fieldml_GetMeshElementDomain( handle, oHandle ) );
        if( boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            int bounds = Fieldml_GetContiguousBoundsCount( handle, Fieldml_GetMeshElementDomain( handle, oHandle ) );
            fprintf( stdout, "    1...%d\n", bounds );
            for( j = 1; j <= bounds; j++ )
            {
                fprintf( stdout, "    %d: %s\n", j, Fieldml_GetMeshElementShape( handle, oHandle, j ) );
            }
        }
        
        count2 = Fieldml_GetMeshConnectivityCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s: %s\n",
                Fieldml_GetObjectName( handle, Fieldml_GetMeshConnectivityDomain( handle, oHandle, j ) ),
                Fieldml_GetObjectName( handle, Fieldml_GetMeshConnectivitySource( handle, oHandle, j ) ) );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_PARAMETERS );
    fprintf( stdout, "ContinuousParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_CONTINUOUS_PARAMETERS, i );
        
        fprintf( stdout, "  %d: %d %s (%s)\n", i, Fieldml_GetParameterDataDescription( handle, oHandle ),
            Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
        count2 = Fieldml_GetSemidenseIndexCount( handle, oHandle, 1 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   sparse: %s\n", Fieldml_GetObjectName( handle, Fieldml_GetSemidenseIndex( handle, oHandle, j, 1 ) ) );
        }
        count2 = Fieldml_GetSemidenseIndexCount( handle, oHandle, 0 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    dense: %s\n", Fieldml_GetObjectName( handle, Fieldml_GetSemidenseIndex( handle, oHandle, j, 0 ) ) );
        }
        
        count2 = Fieldml_GetSwizzleCount( handle, oHandle );
        if( count2 > 0 )
        {
            swizzle = Fieldml_GetSwizzleData( handle, oHandle );
            fprintf( stdout, "    swizzle: " );
            for( j = 0; j < count2; j++ )
            {
                fprintf( stdout, "%d ", swizzle[j] );
            }
            fprintf( stdout, "\n" );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_ENSEMBLE_PARAMETERS );
    fprintf( stdout, "EnsembleParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_ENSEMBLE_PARAMETERS, i );
        
        fprintf( stdout, "  %d: %d %s (%s)\n", i, Fieldml_GetParameterDataDescription( handle, oHandle ),
            Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
        count2 = Fieldml_GetSemidenseIndexCount( handle, oHandle, 1 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   sparse: %s\n", Fieldml_GetObjectName( handle, Fieldml_GetSemidenseIndex( handle, oHandle, j, 1 ) ) );
        }
        count2 = Fieldml_GetSemidenseIndexCount( handle, oHandle, 0 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    dense: %s\n", Fieldml_GetObjectName( handle, Fieldml_GetSemidenseIndex( handle, oHandle, j, 0 ) ) );
        }

        count2 = Fieldml_GetSwizzleCount( handle, oHandle );
        if( count2 > 0 )
        {
            swizzle = Fieldml_GetSwizzleData( handle, oHandle );
            fprintf( stdout, "    swizzle: " );
            for( j = 0; j < count2; j++ )
            {
                fprintf( stdout, "%d ", swizzle[j] );
            }
            fprintf( stdout, "\n" );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_IMPORT );
    fprintf( stdout, "ContinuousImports: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_CONTINUOUS_IMPORT, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
        fprintf( stdout, "    Remote name: %s\n", Fieldml_GetObjectName( handle, Fieldml_GetImportRemoteEvaluator( handle, oHandle ) ) );
        
        count2 = Fieldml_GetAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                Fieldml_GetObjectName( handle, Fieldml_GetAliasLocalHandle( handle, oHandle, j ) ),
                Fieldml_GetObjectName( handle, Fieldml_GetAliasRemoteHandle( handle, oHandle, j ) ) ); 
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_PIECEWISE );
    fprintf( stdout, "ContinuousPiecewise: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_CONTINUOUS_PIECEWISE, i );
        
        fprintf( stdout, "  %d: %s over %s (%s)\n", i,
            Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetIndexDomain( handle, oHandle, 1 ) ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );

        count2 = Fieldml_GetAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                Fieldml_GetObjectName( handle, Fieldml_GetAliasLocalHandle( handle, oHandle, j ) ),
                Fieldml_GetObjectName( handle, Fieldml_GetAliasRemoteHandle( handle, oHandle, j ) ) ); 
        }

        count2 = Fieldml_GetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, Fieldml_GetEvaluatorElement( handle, oHandle, j ),
                Fieldml_GetObjectName( handle, Fieldml_GetEvaluatorHandle( handle, oHandle, j ) ) );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_AGGREGATE );
    fprintf( stdout, "ContinuousAggregate: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_CONTINUOUS_AGGREGATE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );

        count2 = Fieldml_GetAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                Fieldml_GetObjectName( handle, Fieldml_GetAliasLocalHandle( handle, oHandle, j ) ),
                Fieldml_GetObjectName( handle, Fieldml_GetAliasRemoteHandle( handle, oHandle, j ) ) ); 
        }

        count2 = Fieldml_GetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, Fieldml_GetEvaluatorElement( handle, oHandle, j ),
                Fieldml_GetObjectName( handle, Fieldml_GetEvaluatorHandle( handle, oHandle, j ) ) );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_VARIABLE );
    fprintf( stdout, "ContinuousVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_CONTINUOUS_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_ENSEMBLE_VARIABLE );
    fprintf( stdout, "EnsembleVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_ENSEMBLE_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_DEREFERENCE );
    fprintf( stdout, "ContinuousDereference: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_CONTINUOUS_DEREFERENCE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_REMOTE_ENSEMBLE_DOMAIN );
    fprintf( stdout, "External ensemble domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_REMOTE_ENSEMBLE_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s\n", i, Fieldml_GetObjectName( handle, oHandle ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_REMOTE_CONTINUOUS_DOMAIN );
    fprintf( stdout, "External continuous domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObjectHandle( handle, FHT_REMOTE_CONTINUOUS_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s\n", i, Fieldml_GetObjectName( handle, oHandle ) );
    }
    
    
    fprintf( stdout, "*******************************\n" );
    
    count = Fieldml_GetErrorCount( handle );
    if( count <= 0 )
    {
        fprintf( stdout, "No Errors\n" );
    }
    else
    {
        for( i = 1; i <= count; i++ )
        {
            fprintf( stdout, "   %s\n", Fieldml_GetError( handle, i ) );
        }
    }

    fprintf( stdout, "*******************************\n" );
    
    Fieldml_DestroyParse( handle );
}


int testWrite( const char *filename )
{
    FmlParseHandle handle;
    const char *outputFilename;
    int result;

    handle = Fieldml_ParseFile( filename );
    
    outputFilename = calloc( 1, strlen( filename ) + 10 );
    strcpy( outputFilename, filename );
    strcat( outputFilename, "_out.xml" );
    
    result = Fieldml_WriteFile( handle, outputFilename );

    Fieldml_DestroyParse( handle );
}


int main( int argc, char **argv )
{
    if( !validate( argv[1] ) )
    {
        return 1;
    }
    
    testRead( argv[1] );
    
    testWrite( argv[1] );
    
    return 0;
}
