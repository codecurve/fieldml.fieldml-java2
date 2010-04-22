#include <stdlib.h>
#include <stdio.h>

#include <libxml/sax.h>
#include <libxml/globals.h>
#include <libxml/xmlerror.h>
#include <libxml/parser.h>
#include <libxml/xmlmemory.h>

#include "fieldml_api.h"


//========================================================================
//
// Main
//
//========================================================================

int main( int argc, char **argv )
{
    int i, j, count, count2;
    FmlObjectHandle oHandle;
    FmlParseHandle handle = fmlParseFile( argv[1] );
    DomainBoundsType boundsType;

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_DOMAIN );
    fprintf( stdout, "ContinuousDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetDomainComponentEnsemble( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_ENSEMBLE_DOMAIN );
    fprintf( stdout, "EnsembleDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_ENSEMBLE_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetDomainComponentEnsemble( handle, oHandle ) ) );
        
        boundsType = fmlGetDomainBoundsType( handle, oHandle );
        if( boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            fprintf( stdout, "    1...%d\n", fmlGetContiguousBoundsCount( handle, oHandle ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_MESH_DOMAIN );
    fprintf( stdout, "MeshDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_MESH_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s, %s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetMeshElementDomain( handle, oHandle ) ),
            fmlGetObjectName( handle, fmlGetMeshXiDomain( handle, oHandle ) ) );
        boundsType = fmlGetDomainBoundsType( handle, fmlGetMeshElementDomain( handle, oHandle ) );
        if( boundsType == BOUNDS_DISCRETE_CONTIGUOUS )
        {
            int bounds = fmlGetContiguousBoundsCount( handle, fmlGetMeshElementDomain( handle, oHandle ) );
            fprintf( stdout, "    1...%d\n", bounds );
            for( j = 1; j <= bounds; j++ )
            {
                fprintf( stdout, "    %d: %s\n", j, fmlGetMeshElementShape( handle, oHandle, j ) );
            }
        }
        
        count2 = fmlGetMeshConnectivityCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s: %s\n",
                fmlGetObjectName( handle, fmlGetMeshConnectivityDomain( handle, oHandle, j ) ),
                fmlGetObjectName( handle, fmlGetMeshConnectivitySource( handle, oHandle, j ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_PARAMETERS );
    fprintf( stdout, "ContinuousParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_PARAMETERS, i );
        
        fprintf( stdout, "  %d: %d %s (%s)\n", i, fmlGetParameterDataDescription( handle, oHandle ),
            fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 1 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   sparse: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 1 ) ) );
        }
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 0 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    dense: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 0 ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_ENSEMBLE_PARAMETERS );
    fprintf( stdout, "EnsembleParameters: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_ENSEMBLE_PARAMETERS, i );
        
        fprintf( stdout, "  %d: %d %s (%s)\n", i, fmlGetParameterDataDescription( handle, oHandle ),
            fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 1 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   sparse: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 1 ) ) );
        }
        count2 = fmlGetSemidenseIndexCount( handle, oHandle, 0 );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    dense: %s\n", fmlGetObjectName( handle, fmlGetSemidenseIndex( handle, oHandle, j, 0 ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_IMPORT );
    fprintf( stdout, "ContinuousImports: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_IMPORT, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        fprintf( stdout, "    Remote name: %s\n", fmlGetImportRemoteName( handle, oHandle ) );
        
        count2 = fmlGetImportAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                fmlGetObjectName( handle, fmlGetImportAliasLocalHandle( handle, oHandle, j ) ),
                fmlGetObjectName( handle, fmlGetImportAliasRemoteHandle( handle, oHandle, j ) ) ); 
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_PIECEWISE );
    fprintf( stdout, "ContinuousPiecewise: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_PIECEWISE, i );
        
        fprintf( stdout, "  %d: %s over %s (%s)\n", i,
            fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetIndexDomain( handle, oHandle, 1 ) ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, fmlGetEvaluatorElement( handle, oHandle, j ),
                fmlGetObjectName( handle, fmlGetEvaluatorHandle( handle, oHandle, j ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_AGGREGATE );
    fprintf( stdout, "ContinuousAggregate: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_AGGREGATE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
        count2 = fmlGetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, fmlGetEvaluatorElement( handle, oHandle, j ),
                fmlGetObjectName( handle, fmlGetEvaluatorHandle( handle, oHandle, j ) ) );
        }
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_VARIABLE );
    fprintf( stdout, "ContinuousVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_ENSEMBLE_VARIABLE );
    fprintf( stdout, "EnsembleVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_ENSEMBLE_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_CONTINUOUS_DEREFERENCE );
    fprintf( stdout, "ContinuousDereference: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_CONTINUOUS_DEREFERENCE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, fmlGetObjectName( handle, oHandle ),
            fmlGetObjectName( handle, fmlGetValueDomain( handle, oHandle ) ) );
    }

    count = fmlGetObjectCount( handle, FHT_REMOTE_ENSEMBLE_DOMAIN );
    fprintf( stdout, "External ensemble domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_REMOTE_ENSEMBLE_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s\n", i, fmlGetObjectName( handle, oHandle ) );
    }

    count = fmlGetObjectCount( handle, FHT_REMOTE_CONTINUOUS_DOMAIN );
    fprintf( stdout, "External continuous domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = fmlGetObjectHandle( handle, FHT_REMOTE_CONTINUOUS_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s\n", i, fmlGetObjectName( handle, oHandle ) );
    }
    
    
    fprintf( stdout, "*******************************\n" );
    
    count = fmlGetErrorCount( handle );
    if( count <= 0 )
    {
        fprintf( stdout, "No Errors\n" );
    }
    else
    {
        for( i = 1; i <= count; i++ )
        {
            fprintf( stdout, "   %s\n", fmlGetError( handle, i ) );
        }
    }

    fprintf( stdout, "*******************************\n" );
    
    fmlDestroyParse( handle );
    
    return 0;
}
