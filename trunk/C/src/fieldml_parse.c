#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <libxml/sax.h>

#include "string_const.h"
#include "fieldml_parse.h"
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


static void setRegionHandle( FieldmlParse *parse, FmlObjectHandle handle, int regionHandle )
{
    FieldmlObject *object = (FieldmlObject*)getSimpleListEntry( parse->objects, handle );
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
    object->name = _strdup( name );
    object->type = type;
    object->markup = createStringTable();
    
    return object;
}


FieldmlObject *createEnsembleDomain( const char * name, int region, FmlObjectHandle componentDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_ENSEMBLE_DOMAIN, region );
    EnsembleDomain *domain = calloc( 1, sizeof( EnsembleDomain ) );
    domain->boundsType = BOUNDS_UNKNOWN;

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


FieldmlObject *createContinuousImport( const char *name, int region, FmlObjectHandle evaluator, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_IMPORT, region );
    ContinuousImport *import = calloc( 1, sizeof( ContinuousImport ) );
    import->remoteEvaluator = evaluator;
    import->valueDomain = valueDomain;
    import->aliases = createIntTable();

    object->object.continuousImport = import;
    
    return object;
}


FieldmlObject *createEnsembleVariable( const char *name, int region, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_ENSEMBLE_VARIABLE, region );
    Variable *variable = calloc( 1, sizeof( Variable ) );
    variable->valueDomain = valueDomain;
    variable->parameters = createSimpleList();

    object->object.variable = variable;

    return object;
}


FieldmlObject *createContinuousVariable( const char *name, int region, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_VARIABLE, region );
    Variable *variable = calloc( 1, sizeof( Variable ) );
    variable->valueDomain = valueDomain;
    variable->parameters = createSimpleList();

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


FieldmlObject *createContinuousDereference( const char *name, int region, FmlObjectHandle valueIndexes, FmlObjectHandle valueSource, FmlObjectHandle valueDomain )
{
    FieldmlObject *object = createFieldmlObject( name, FHT_CONTINUOUS_DEREFERENCE, region );
    ContinuousDereference *dereference = calloc( 1, sizeof( ContinuousDereference ) );
    
    dereference->valueDomain = valueDomain;
    dereference->valueIndexes = valueIndexes;
    dereference->valueSource = valueSource;

    object->object.dereference = dereference;
    
    return object;
}


SemidenseData *createSemidenseData()
{
    SemidenseData *data = calloc( 1, sizeof( SemidenseData ) );
    data->denseIndexes = createSimpleList();
    data->sparseIndexes = createSimpleList();
    data->locationType = LOCATION_UNKNOWN;

    return data;
}


static FmlObjectHandle addEnsembleDomain( FieldmlParse *parse, int regionHandle, const char *name, int count )
{
    int handle;
    
    handle = Fieldml_CreateEnsembleDomain( parse, name, FML_INVALID_HANDLE );
    Fieldml_SetContiguousBoundsCount( parse, handle, count );
    setRegionHandle( parse, handle, regionHandle );
    
    return handle;
}


static FmlObjectHandle addContinuousDomain( FieldmlParse *parse, int regionHandle, const char *name, FmlObjectHandle componentHandle )
{
    int handle;

    handle = Fieldml_CreateContinuousDomain( parse, name, componentHandle );
    setRegionHandle( parse, handle, regionHandle );
    
    return handle;
}


static void addMarkup( FieldmlParse *parse, FmlObjectHandle handle, const char *attribute, const char *value );


void addLibraryDomains( FieldmlParse *parse )
{
    FmlObjectHandle handle;

    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.real.1d", FML_INVALID_HANDLE );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.real.2d", FML_INVALID_HANDLE );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.real.3d", FML_INVALID_HANDLE );
    
    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.ensemble.xi.1d", 1 );
    handle = addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.xi.1d", handle );
    addMarkup( parse, handle, "xi", "true" );

    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.ensemble.xi.2d", 2 );
    handle = addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.xi.2d", handle );
    addMarkup( parse, handle, "xi", "true" );

    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.ensemble.xi.3d", 3 );
    handle = addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.xi.3d", handle );
    addMarkup( parse, handle, "xi", "true" );

    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.local_nodes.line.2", 2 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.parameters.linear_lagrange", handle ); 
    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.local_nodes.line.3", 3 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.parameters.quadratic_lagrange", handle ); 

    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.local_nodes.quad.2x2", 4 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.parameters.bilinear_lagrange", handle ); 
    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.local_nodes.quad.3x3", 9 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.parameters.biquadratic_lagrange", handle ); 

    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.local_nodes.cube.2x2x2", 8 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.parameters.trilinear_lagrange", handle ); 
    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.local_nodes.cube.3x3x3", 27 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.parameters.triquadratic_lagrange", handle ); 
    
    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.ensemble.rc.1d", 1 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.coordinates.rc.1d", handle );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.velocity.rc.1d", handle );
    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.ensemble.rc.2d", 2 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.coordinates.rc.2d", handle );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.velocity.rc.2d", handle );
    handle = addEnsembleDomain( parse, LIBRARY_REGION_HANDLE, "library.ensemble.rc.3d", 3 );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.coordinates.rc.3d", handle );
    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.velocity.rc.3d", handle );

    addContinuousDomain( parse, LIBRARY_REGION_HANDLE, "library.pressure", FML_INVALID_HANDLE );
}


FieldmlParse *createFieldmlParse()
{
    FieldmlParse *parse = calloc( 1, sizeof( FieldmlParse ) );

    parse->objects = createSimpleList();
    parse->errors = createSimpleList();
    
    addLibraryDomains( parse );
    
    return parse;
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


void destroyContinuousImport( ContinuousImport *import )
{
    destroyIntTable( import->aliases, NULL );
    free( import );
}


void destroySemidenseData( SemidenseData *data )
{
    destroySimpleList( data->sparseIndexes, NULL );
    destroySimpleList( data->denseIndexes, NULL );

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


void destroyContinuousDereference( ContinuousDereference *dereference )
{
    free( dereference );
}


void destroyVariable( Variable *variable )
{
    destroySimpleList( variable->parameters, free );

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
    case FHT_CONTINUOUS_IMPORT:
        destroyContinuousImport( object->object.continuousImport );
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
    case FHT_CONTINUOUS_DEREFERENCE:
        destroyContinuousDereference( object->object.dereference );
        break;
    default:
        break;
    }
    destroyStringTable( object->markup, free );
    free( object->name );
    free( object );
}


void destroyFieldmlParse( FieldmlParse *parse )
{
    destroySimpleList( parse->objects, destroyFieldmlObject );
    destroySimpleList( parse->errors, free );

    free( parse );
}


//========================================================================
//
// Utility
//
//========================================================================


static void addMarkup( FieldmlParse *parse, FmlObjectHandle handle, const char *attribute, const char *value )
{
    FieldmlObject *object = (FieldmlObject*)getSimpleListEntry( parse->objects, handle );

    setStringTableEntry( object->markup, attribute, _strdup( value ), free );
}


void addError( FieldmlParse *parse, const char *error, const char *name1, const char *name2 )
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
    
    addSimpleListEntry( parse->errors, string );
    
    fprintf( stderr, "%s\n", string );
}


static FmlObjectHandle getObjectHandle( FieldmlParse *parse, const char *name )
{
    int i, count;
    FieldmlObject *object;
    
    count = getSimpleListCount( parse->objects );
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( parse->objects, i );
        if( strcmp( name, object->name ) == 0 )
        {
            return i;
        }
    }
    
    return FML_INVALID_HANDLE;
}


FmlObjectHandle addFieldmlObject( FieldmlParse *parse, FieldmlObject *object )
{
    int doSwitch;
    FieldmlObject *oldObject;
    FmlObjectHandle handle = getObjectHandle( parse, object->name );
    
    if( handle == FML_INVALID_HANDLE )
    {
        return addSimpleListEntry( parse->objects, object );
    }

    doSwitch = 0;
    
    oldObject = (FieldmlObject*)getSimpleListEntry( parse->objects, handle );
    
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
            ( object->type == FHT_CONTINUOUS_IMPORT ) ||
            ( object->type == FHT_CONTINUOUS_AGGREGATE ) ||
            ( object->type == FHT_CONTINUOUS_DEREFERENCE ) ||
            ( object->type == FHT_CONTINUOUS_PARAMETERS ) ||
            ( object->type == FHT_CONTINUOUS_VARIABLE ) )
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
    
    addError( parse, "Handle collision. Cannot replace", object->name, oldObject->name );
    fprintf( stderr, "Handle collision. Cannot replace %s:%d with %s:%d\n", object->name, object->type, oldObject->name, oldObject->type );
    destroyFieldmlObject( object );
    
    return FML_INVALID_HANDLE;
}


FmlObjectHandle getOrCreateObjectHandle( FieldmlParse *parse, const char *name, FieldmlHandleType type )
{
    FmlObjectHandle handle = getObjectHandle( parse, name );

    if( handle == FML_INVALID_HANDLE )
    {
        handle = addFieldmlObject( parse, createFieldmlObject( name, type, VIRTUAL_REGION_HANDLE ) );
    }
    
    return handle;
}


void finalizeFieldmlParse( FieldmlParse *parse )
{
    FieldmlObject *object;
    int i, count;
    
    count = getSimpleListCount( parse->objects );
    
    for( i = 0; i < count; i++ )
    {
        object = (FieldmlObject*)getSimpleListEntry( parse->objects, i );
        
        if( ( object->type == FHT_UNKNOWN_CONTINUOUS_DOMAIN ) || ( object->type == FHT_UNKNOWN_CONTINUOUS_SOURCE ) )
        {
            object->type = FHT_REMOTE_CONTINUOUS_DOMAIN;
        }
        else if( ( object->type == FHT_UNKNOWN_ENSEMBLE_DOMAIN ) || ( object->type == FHT_UNKNOWN_ENSEMBLE_SOURCE ) )
        {
            object->type = FHT_REMOTE_ENSEMBLE_DOMAIN;
        }
        else if( object->type == FHT_UNKNOWN_CONTINUOUS_EVALUATOR )
        {
            object->type = FHT_REMOTE_CONTINUOUS_EVALUATOR;
        }
        else if( object->type == FHT_UNKNOWN_ENSEMBLE_EVALUATOR )
        {
            object->type = FHT_REMOTE_ENSEMBLE_EVALUATOR;
        }
    }
}
