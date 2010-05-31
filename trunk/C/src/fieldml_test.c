/* \file
 * $Id$
 * \author Caton Little
 * \brief 
 *
 * \section LICENSE
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is OpenCMISS
 *
 * The Initial Developer of the Original Code is University of Auckland,
 * Auckland, New Zealand and University of Oxford, Oxford, United
 * Kingdom. Portions created by the University of Auckland and University
 * of Oxford are Copyright (C) 2007 by the University of Auckland and
 * the University of Oxford. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <libxml/SAX.h>
#include <libxml/globals.h>
#include <libxml/xmlerror.h>
#include <libxml/parser.h>
#include <libxml/xmlmemory.h>
#include <libxml/xmlschemas.h>

#include "fieldml_io.h"
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
    FmlHandle handle;
    DomainBoundsType boundsType;
    const int *swizzle;
    const char *shape;

    handle = Fieldml_CreateFromFile( filename );
    
    Fieldml_SetDebug( handle, 1 );

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_DOMAIN );
    fprintf( stdout, "ContinuousDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_CONTINUOUS_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetDomainComponentEnsemble( handle, oHandle ) ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_ENSEMBLE_DOMAIN );
    fprintf( stdout, "EnsembleDomains: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_ENSEMBLE_DOMAIN, i );
        
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
        oHandle = Fieldml_GetObject( handle, FHT_MESH_DOMAIN, i );
        
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
                shape = Fieldml_GetMeshElementShape( handle, oHandle, j, 0 );
                if( shape != NULL )
                {
                    fprintf( stdout, "    %d: %s\n", j, shape );
                }
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
        oHandle = Fieldml_GetObject( handle, FHT_CONTINUOUS_PARAMETERS, i );
        
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
        oHandle = Fieldml_GetObject( handle, FHT_ENSEMBLE_PARAMETERS, i );
        
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
        oHandle = Fieldml_GetObject( handle, FHT_CONTINUOUS_IMPORT, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
        fprintf( stdout, "    Remote name: %s\n", Fieldml_GetObjectName( handle, Fieldml_GetImportRemoteEvaluator( handle, oHandle ) ) );
        
        count2 = Fieldml_GetAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                Fieldml_GetObjectName( handle, Fieldml_GetAliasLocal( handle, oHandle, j ) ),
                Fieldml_GetObjectName( handle, Fieldml_GetAliasRemote( handle, oHandle, j ) ) ); 
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_PIECEWISE );
    fprintf( stdout, "ContinuousPiecewise: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_CONTINUOUS_PIECEWISE, i );
        
        fprintf( stdout, "  %d: %s over %s (%s)\n", i,
            Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetIndexDomain( handle, oHandle, 1 ) ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );

        count2 = Fieldml_GetAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                Fieldml_GetObjectName( handle, Fieldml_GetAliasLocal( handle, oHandle, j ) ),
                Fieldml_GetObjectName( handle, Fieldml_GetAliasRemote( handle, oHandle, j ) ) ); 
        }

        count2 = Fieldml_GetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, Fieldml_GetEvaluatorElement( handle, oHandle, j ),
                Fieldml_GetObjectName( handle, Fieldml_GetEvaluator( handle, oHandle, j ) ) );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_AGGREGATE );
    fprintf( stdout, "ContinuousAggregate: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_CONTINUOUS_AGGREGATE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );

        count2 = Fieldml_GetAliasCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "    %s  -->  %s\n",
                Fieldml_GetObjectName( handle, Fieldml_GetAliasLocal( handle, oHandle, j ) ),
                Fieldml_GetObjectName( handle, Fieldml_GetAliasRemote( handle, oHandle, j ) ) ); 
        }

        count2 = Fieldml_GetEvaluatorCount( handle, oHandle );
        for( j = 1; j <= count2; j++ )
        {
            fprintf( stdout, "   entry %d: %d -> %s\n", j, Fieldml_GetEvaluatorElement( handle, oHandle, j ),
                Fieldml_GetObjectName( handle, Fieldml_GetEvaluator( handle, oHandle, j ) ) );
        }
    }

    count = Fieldml_GetObjectCount( handle, FHT_CONTINUOUS_VARIABLE );
    fprintf( stdout, "ContinuousVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_CONTINUOUS_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_ENSEMBLE_VARIABLE );
    fprintf( stdout, "EnsembleVariable: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_ENSEMBLE_VARIABLE, i );
        
        fprintf( stdout, "  %d: %s (%s)\n", i, Fieldml_GetObjectName( handle, oHandle ),
            Fieldml_GetObjectName( handle, Fieldml_GetValueDomain( handle, oHandle ) ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_REMOTE_ENSEMBLE_DOMAIN );
    fprintf( stdout, "External ensemble domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_REMOTE_ENSEMBLE_DOMAIN, i );
        
        fprintf( stdout, "  %d: %s\n", i, Fieldml_GetObjectName( handle, oHandle ) );
    }

    count = Fieldml_GetObjectCount( handle, FHT_REMOTE_CONTINUOUS_DOMAIN );
    fprintf( stdout, "External continuous domain: %d\n", count ); 
    for( i = 1; i <= count; i++ )
    {
        oHandle = Fieldml_GetObject( handle, FHT_REMOTE_CONTINUOUS_DOMAIN, i );
        
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
    
    Fieldml_Destroy( handle );
}


int testWrite( const char *filename )
{
    FmlHandle handle;
    char *outputFilename;
    int result;

    handle = Fieldml_CreateFromFile( filename );
    
    Fieldml_SetDebug( handle, 1 );

    outputFilename = calloc( 1, strlen( filename ) + 10 );
    strcpy( outputFilename, filename );
    strcat( outputFilename, "_out.xml" );
    
    result = Fieldml_WriteFile( handle, outputFilename );

    Fieldml_Destroy( handle );
}


void testStream()
{
    FmlInputStream stream = FmlCreateStringInputStream( "129 24 ,, 333 .. 456 -512  \n 6324 \r\n asc123asc" );
    int iExpected[7] = { 129, 24, 333, 456, -512, 6324, 123 };
    int iActual;
    double dExpected[9] = { 129, 24.1, -78.239, -21.34, 65.12, 3.0, 3.2, 0.092, -0.873 };
    double dActual; 
    int i;
    
    for( i = 0; i < 7; i++ )
    {
        iActual = FmlInputStreamReadInt( stream );
        
        if( iActual != iExpected[i] )
        {
            fprintf( stderr, "Mismatch at %d: %d != %d\n", i, iExpected[i], iActual );
        }
    }
    
    FmlInputStreamDestroy( stream );
    
    stream = FmlCreateStringInputStream( "129 ,, 24.1 -78.239-21.34 --65.12,,\r\n\t asf3asf3.2asf.092xxx-.873" );

    for( i = 0; i < 7; i++ )
    {
        dActual = FmlInputStreamReadDouble( stream );
        
        if( dActual != dExpected[i] )
        {
            fprintf( stderr, "Mismatch at %d: %f != %f\n", i, dExpected[i], dActual );
        }
    }

    FmlInputStreamDestroy( stream );
}


int main( int argc, char **argv )
{
    if( !validate( argv[1] ) )
    {
        return 1;
    }
    
    testRead( argv[1] );
    
    testWrite( argv[1] );

    testStream();
    
    return 0;
}
