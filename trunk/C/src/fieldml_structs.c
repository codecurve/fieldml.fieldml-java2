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
 * The Original Code is FieldML
 *
 * The Initial Developer of the Original Code is Auckland Uniservices Ltd,
 * Auckland, New Zealand. Portions created by the Initial Developer are
 * Copyright (C) 2010 the Initial Developer. All Rights Reserved.
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

#include "string_const.h"
#include "int_table.h"
#include "string_table.h"
#include "simple_list.h"
#include "fieldml_structs.h"
#include "fieldml_sax.h"

//========================================================================
//
// Consts
//
//========================================================================

const int VIRTUAL_REGION_HANDLE = -1; //For derived objects, e.g. mesh domain xi and element domains.
const int LIBRARY_REGION_HANDLE = 0;
const int FILE_REGION_HANDLE = 1;


//========================================================================
//
// Util
//
//========================================================================


static void setRegionHandle( FieldmlRegion *region, FmlObjectHandle handle, int regionHandle )
{
    FieldmlObject *object = (FieldmlObject*)getSimpleListEntry( region->objects, handle );
    object->regionHandle = regionHandle;
}


//========================================================================
//
// Creators
//
//========================================================================


FieldmlObject *createFieldmlObject( const char *name, FieldmlHandleType type, int regionHandle )
{
    FieldmlObject *object = calloc( 1, sizeof( FieldmlObject ) );
    object->regionHandle = regionHandle;
    object->name = strdup( name );
    object->type = type;
    object->markup = createStringTable();
    
    return object;
}


FieldmlObject *createEnsembleDomain( const char * name, int region, FmlObjectHandle componentDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_ENSEMBLE_DOMAIN, region );
    EnsembleDomain *domain = calloc( 1, sizeof( EnsembleDomain ) );
    domain->boundsType = BOUNDS_UNKNOWN;
    domain->componentDomain = FML_INVALID_HANDLE;
    //TODO Support (or remove) multi-component ensemble domains.

    object->object.ensembleDomain = domain;

    return object;
}


FieldmlObject *createContinuousDomain( const char * name, int region, FmlObjectHandle componentDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_DOMAIN, region );
    ContinuousDomain *domain = calloc( 1, sizeof( ContinuousDomain ) );
    domain->componentDomain = componentDomain;

    object->object.continuousDomain = domain;
    
    return object;
}


FieldmlObject *createMeshDomain( const char *name, int region, FmlObjectHandle xiDomain, FmlObjectHandle elementDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_MESH_DOMAIN, region );
    MeshDomain *domain = calloc( 1, sizeof( MeshDomain ) );
    domain->xiDomain = xiDomain;
    domain->elementDomain = elementDomain;
    domain->shapes = createIntTable();
    domain->connectivity = createIntTable();
    
    object->object.meshDomain = domain;

    return object;
}


FieldmlObject *createContinuousReference( const char *name, int region, FmlObjectHandle evaluator, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_REFERENCE, region );
    ContinuousReference *reference = calloc( 1, sizeof( ContinuousReference ) );
    reference->remoteEvaluator = evaluator;
    reference->valueDomain = valueDomain;
    reference->aliases = createIntTable();

    object->object.continuousReference = reference;
    
    return object;
}


FieldmlObject *createEnsembleVariable( const char *name, int region, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_ENSEMBLE_VARIABLE, region );
    Variable *variable = calloc( 1, sizeof( Variable ) );
    variable->valueDomain = valueDomain;
    variable->parameters = createIntStack();

    object->object.variable = variable;

    return object;
}


FieldmlObject *createContinuousVariable( const char *name, int region, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_VARIABLE, region );
    Variable *variable = calloc( 1, sizeof( Variable ) );
    variable->valueDomain = valueDomain;
    variable->parameters = createIntStack();

    object->object.variable = variable;

    return object;
}


FieldmlObject *createEnsembleParameters( const char *name, int region, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_ENSEMBLE_PARAMETERS, region );
    Parameters *parameters = calloc( 1, sizeof( Parameters ) );
    parameters->valueDomain = valueDomain;
    parameters->descriptionType = DESCRIPTION_UNKNOWN;

    object->object.parameters = parameters;

    return object;
}


FieldmlObject *createContinuousParameters( const char *name, int region, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_PARAMETERS, region );
    Parameters *parameters = calloc( 1, sizeof( Parameters ) );
    parameters->valueDomain = valueDomain;
    parameters->descriptionType = DESCRIPTION_UNKNOWN;

    object->object.parameters = parameters;

    return object;
}


FieldmlObject *createContinuousPiecewise( const char *name, int region, FmlObjectHandle indexDomain, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_PIECEWISE, region );
    ContinuousPiecewise *piecewise = calloc( 1, sizeof( ContinuousPiecewise ) );
    piecewise->valueDomain = valueDomain;
    piecewise->indexDomain = indexDomain;
    
    piecewise->aliases = createIntTable();
    piecewise->evaluators = createIntTable();
    
    object->object.piecewise = piecewise;
    
    return object;
}


FieldmlObject *createContinuousAggregate( const char *name, int region, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_AGGREGATE, region );
    ContinuousAggregate *aggregate = calloc( 1, sizeof( ContinuousAggregate ) );
    aggregate->valueDomain = valueDomain;
    
    aggregate->aliases = createIntTable();
    aggregate->evaluators = createIntTable();
    
    object->object.aggregate = aggregate;
    
    return object;
}


SemidenseData *createSemidenseData()
{
    SemidenseData *data = calloc( 1, sizeof( SemidenseData ) );
    data->denseIndexes = createIntStack();
    data->sparseIndexes = createIntStack();
    data->locationType = LOCATION_UNKNOWN;

    return data;
}


static FmlObjectHandle addEnsembleDomain( FieldmlRegion *region, int regionHandle, const char *name, int count )
{
    int handle;
    
    handle = Fieldml_CreateEnsembleDomain( region, name, FML_INVALID_HANDLE );
    Fieldml_SetContiguousBoundsCount( region, handle, count );
    setRegionHandle( region, handle, regionHandle );
    
    return handle;
}


static FmlObjectHandle addContinuousDomain( FieldmlRegion *region, int regionHandle, const char *name, FmlObjectHandle componentHandle )
{
    int handle;

    handle = Fieldml_CreateContinuousDomain( region, name, componentHandle );
    setRegionHandle( region, handle, regionHandle );
    
    return handle;
}


static FmlObjectHandle addEvaluator( FieldmlRegion *region, int regionHandle, const char *name, FmlObjectHandle domainHandle )
{
    FieldmlObject *object;
    int handle;
    int type;
    
    type = Fieldml_GetObjectType( region, domainHandle );
    
    if( type == FHT_ENSEMBLE_DOMAIN )
    {
        object = createFieldmlObject( name, FHT_REMOTE_ENSEMBLE_EVALUATOR, regionHandle );
    }
    else if( type == FHT_CONTINUOUS_DOMAIN )
    {
        object = createFieldmlObject( name, FHT_REMOTE_CONTINUOUS_EVALUATOR, regionHandle );
    }

    handle = addFieldmlObject( region, object );
    setRegionHandle( region, handle, regionHandle );
    
    return handle;
}


static void addMarkup( FieldmlRegion *region, FmlObjectHandle handle, const char *attribute, const char *value );


static void addLibraryDomains( FieldmlRegion *region )
{
    FmlObjectHandle handle;

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.generic.1d", 1 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.real.1d", handle );

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.generic.2d", 2 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.real.2d", handle );

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.generic.3d", 3 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.real.3d", handle );
    
    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.xi.1d", 1 );
    handle = addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.xi.1d", handle );
    addMarkup( region, handle, "xi", "true" );

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.xi.2d", 2 );
    handle = addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.xi.2d", handle );
    addMarkup( region, handle, "xi", "true" );

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.xi.3d", 3 );
    handle = addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.xi.3d", handle );
    addMarkup( region, handle, "xi", "true" );

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.local_nodes.line.2", 2 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.parameters.linear_lagrange", handle ); 
    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.local_nodes.line.3", 3 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.parameters.quadratic_lagrange", handle ); 

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.local_nodes.square.2x2", 4 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.parameters.bilinear_lagrange", handle ); 
    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.local_nodes.square.3x3", 9 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.parameters.biquadratic_lagrange", handle ); 

    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.local_nodes.cube.2x2x2", 8 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.parameters.trilinear_lagrange", handle ); 
    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.local_nodes.cube.3x3x3", 27 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.parameters.triquadratic_lagrange", handle ); 
    
    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.rc.1d", 1 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.coordinates.rc.1d", handle );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.velocity.rc.1d", handle );
    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.rc.2d", 2 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.coordinates.rc.2d", handle );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.velocity.rc.2d", handle );
    handle = addEnsembleDomain( region, LIBRARY_REGION_HANDLE, "library.ensemble.rc.3d", 3 );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.coordinates.rc.3d", handle );
    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.velocity.rc.3d", handle );

    addContinuousDomain( region, LIBRARY_REGION_HANDLE, "library.pressure", FML_INVALID_HANDLE );
}


static void addLibraryEvaluators( FieldmlRegion *region )
{
    FmlObjectHandle domainHandle;
    
    domainHandle = Fieldml_GetNamedObject( region, "library.real.1d" );
    
    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.linear_lagrange", domainHandle );
    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.bilinear_lagrange", domainHandle );
    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.trilinear_lagrange", domainHandle );

    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.quadratic_lagrange", domainHandle );
    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.biquadratic_lagrange", domainHandle );
    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.triquadratic_lagrange", domainHandle );

    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.cubic_lagrange", domainHandle );
    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.bicubic_lagrange", domainHandle );
    addEvaluator( region, LIBRARY_REGION_HANDLE, "library.fem.tricubic_lagrange", domainHandle );
}


FieldmlRegion *createFieldmlRegion( const char *name )
{
    FieldmlRegion *region = calloc( 1, sizeof( FieldmlRegion ) );

    region->name = strdup( name );
    region->objects = createSimpleList();
    region->errors = createSimpleList();
    
    addLibraryDomains( region );
    
    addLibraryEvaluators( region );
    
    return region;
}


//========================================================================
//
// Destroyers
//
//========================================================================

void destroyEnsembleDomain( EnsembleDomain *domain )
{
    free( domain );
}


void destroyContinuousDomain( ContinuousDomain *domain )
{
    free( domain );
}


void destroyMeshDomain( MeshDomain *domain )
{
    destroyIntTable( domain->shapes, free );
    destroyIntTable( domain->connectivity, NULL );
    free( domain );
}


void destroyContinuousReference( ContinuousReference *reference )
{
    destroyIntTable( reference->aliases, NULL );
    free( reference );
}


void destroySemidenseData( SemidenseData *data )
{
    destroyIntStack( data->sparseIndexes );
    destroyIntStack( data->denseIndexes );

    switch( data->locationType )
    {
    case LOCATION_FILE:
        free( data->dataLocation.fileData.filename );
        break;
      case LOCATION_INLINE:
        free( data->dataLocation.stringData.string );
        break;
    default:
        break;
    }

    free( data );
}


void destroyParameters( Parameters *parameters )
{
    switch( parameters->descriptionType )
    {
    case DESCRIPTION_SEMIDENSE:
        destroySemidenseData( parameters->dataDescription.semidense );
        break;
    default:
        break;
    }
    
    free( parameters );
}


void destroyContinuousPiecewise( ContinuousPiecewise *piecewise )
{
    destroyIntTable( piecewise->aliases, NULL );
    destroyIntTable( piecewise->evaluators, NULL );
    free( piecewise );
}


void destroyContinuousAggregate( ContinuousAggregate *aggregate )
{
    destroyIntTable( aggregate->aliases, NULL );
    destroyIntTable( aggregate->evaluators, NULL );
    free( aggregate );
}


void destroyVariable( Variable *variable )
{
    destroyIntStack( variable->parameters );

    free( variable );
}


void destroyFieldmlObject( FieldmlObject *object )
{
    switch( object->type )
    {
    case FHT_ENSEMBLE_DOMAIN:
        destroyEnsembleDomain( object->object.ensembleDomain );
        break;
    case FHT_CONTINUOUS_DOMAIN:
        destroyContinuousDomain( object->object.continuousDomain );
        break;
    case FHT_MESH_DOMAIN:
        destroyMeshDomain( object->object.meshDomain );
        break;
    case FHT_CONTINUOUS_REFERENCE:
        destroyContinuousReference( object->object.continuousReference );
        break;
    case FHT_CONTINUOUS_PARAMETERS:
    case FHT_ENSEMBLE_PARAMETERS:
        destroyParameters( object->object.parameters );
        break;
    case FHT_CONTINUOUS_PIECEWISE:
        destroyContinuousPiecewise( object->object.piecewise );
        break;
    case FHT_CONTINUOUS_AGGREGATE:
        destroyContinuousAggregate( object->object.aggregate );
        break;
    case FHT_CONTINUOUS_VARIABLE:
    case FHT_ENSEMBLE_VARIABLE:
        destroyVariable( object->object.variable );
        break;
    default:
        break;
    }
    destroyStringTable( object->markup, free );
    free( object->name );
    free( object );
}


void destroyFieldmlRegion( FieldmlRegion *region )
{
    destroySimpleList( region->objects, destroyFieldmlObject );
    destroySimpleList( region->errors, free );
    free( region->name );

    free( region );
}


//========================================================================
//
// Utility
//
//========================================================================


static void addMarkup( FieldmlRegion *region, FmlObjectHandle handle, const char *attribute, const char *value )
{
    FieldmlObject *object = (FieldmlObject*)getSimpleListEntry( region->objects, handle );

    setStringTableEntry( object->markup, attribute, strdup( value ), free );
}


int setErrorDirect( const char *file, const int line, FieldmlRegion *region, int error )
{
    region->lastError = error;

    if( error != FML_ERR_NO_ERROR )
    {
        if( region->debug )
        {
            printf("FIELDML %s (%s): Error %d at %s:%d\n", FML_VERSION_STRING, __DATE__, error, file, line );
        }
    }
    
    return error;
}


int getError( FieldmlRegion *region )
{
    return region->lastError;
}


void logError( FieldmlRegion *region, const char *error, const char *name1, const char *name2 )
{
    char *string;
    int len;
    
    len = strlen( error );
    if( name1 != NULL )
    {
        len +=  strlen( name1 ) + 2;
    }
    if( name2 != NULL )
    {
        len +=  strlen( name2 ) + 2;
    }
    
    len++;
    
    string = malloc( len );
    
    strcpy( string, error );
    
    if( name1 != NULL )
    {
        strcat( string, ": " );
        strcat( string, name1 );
    }
    if( name2 != NULL )
    {
        strcat( string, ": " );
        strcat( string, name2 );
    }
    
    addSimpleListEntry( region->errors, string );
    
    fprintf( stderr, "%s\n", string );
}


FmlObjectHandle addFieldmlObject( FieldmlRegion *region, FieldmlObject *object )
{
    int doSwitch;
    FieldmlObject *oldObject;
    FmlObjectHandle handle = Fieldml_GetNamedObject( region, object->name );
    
    if( handle == FML_INVALID_HANDLE )
    {
        return addSimpleListEntry( region->objects, object );
    }

    doSwitch = 0;
    
    oldObject = (FieldmlObject*)getSimpleListEntry( region->objects, handle );
    
    if( ( oldObject->regionHandle != VIRTUAL_REGION_HANDLE ) ||
        ( object->regionHandle == VIRTUAL_REGION_HANDLE ) )
    {
        // Do nothing. Virtual objects should never replace non-virtual ones.
    }
    if( oldObject->type == FHT_UNKNOWN_ENSEMBLE_DOMAIN )
    {
        if( object->type == FHT_ENSEMBLE_DOMAIN )
        {
            doSwitch = 1;
        }
    }
    else if( oldObject->type == FHT_UNKNOWN_CONTINUOUS_DOMAIN )
    {
        if( object->type == FHT_CONTINUOUS_DOMAIN )
        {
            doSwitch = 1;
        }
    }
    else if( oldObject->type == FHT_UNKNOWN_ENSEMBLE_SOURCE )
    {
        if( ( object->type == FHT_ENSEMBLE_DOMAIN ) ||
            ( object->type == FHT_ENSEMBLE_PARAMETERS ) ||
            ( object->type == FHT_ENSEMBLE_VARIABLE ) )
        {
            doSwitch = 1;
        }
    }
    else if( oldObject->type == FHT_UNKNOWN_CONTINUOUS_SOURCE )
    {
        if( ( object->type == FHT_CONTINUOUS_DOMAIN ) ||
            ( object->type == FHT_CONTINUOUS_PIECEWISE ) ||
            ( object->type == FHT_CONTINUOUS_REFERENCE ) ||
            ( object->type == FHT_CONTINUOUS_AGGREGATE ) ||
            ( object->type == FHT_CONTINUOUS_PARAMETERS ) ||
            ( object->type == FHT_CONTINUOUS_VARIABLE ) )
        {
            doSwitch = 1;
        }
    }
    else if( oldObject->type == FHT_UNKNOWN_CONTINUOUS_EVALUATOR )
    {
        if( ( object->type == FHT_CONTINUOUS_PIECEWISE ) ||
            ( object->type == FHT_CONTINUOUS_REFERENCE ) ||
            ( object->type == FHT_CONTINUOUS_AGGREGATE ) ||
            ( object->type == FHT_CONTINUOUS_PARAMETERS ) ||
            ( object->type == FHT_CONTINUOUS_VARIABLE ) )
        {
            doSwitch = 1;
        }
    }
    else if( oldObject->type == FHT_UNKNOWN_ENSEMBLE_EVALUATOR )
    {
        if( ( object->type == FHT_ENSEMBLE_PARAMETERS ) ||
            ( object->type == FHT_ENSEMBLE_VARIABLE ) )
        {
            doSwitch = 1;
        }
    }
    
    if( doSwitch )
    {
        oldObject->regionHandle = object->regionHandle;
        oldObject->type = object->type;
        oldObject->object = object->object;
        object->type = FHT_UNKNOWN;
        destroyFieldmlObject( object );
        
        return handle;
    }
    
    logError( region, "Handle collision. Cannot replace", object->name, oldObject->name );
    fprintf( stderr, "Handle collision. Cannot replace %s:%d with %s:%d\n", object->name, object->type, oldObject->name, oldObject->type );
    destroyFieldmlObject( object );
    
    return FML_INVALID_HANDLE;
}
