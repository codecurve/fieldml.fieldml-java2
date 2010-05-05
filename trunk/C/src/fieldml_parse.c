#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <libxml/sax.h>

#include "fieldml_parse.h"
#include "int_table.h"
#include "string_table.h"
#include "simple_list.h"
#include "fieldml_structs.h"
#include "fieldml_sax.h"

//========================================================================
//
// Structs
//
//========================================================================

struct _FieldmlContext
{
    FieldmlObject *currentObject;
    
    int bufferLength;
    char *buffer;
    
    FieldmlParse *parse;
};


//========================================================================
//
// Creators
//
//========================================================================


FieldmlContext *createFieldmlContext( FieldmlParse *parse )
{
    FieldmlContext *context = calloc( 1, sizeof( FieldmlContext ) );
    
    context->parse = parse;
    
    return context;
}


EnsembleDomain *createEnsembleDomain( int componentDomain )
{
    EnsembleDomain *domain = calloc( 1, sizeof( EnsembleDomain ) );
    domain->componentDomain = componentDomain;
    domain->boundsType = BOUNDS_UNKNOWN;
    
    return domain;
}


ContinuousDomain *createContinuousDomain( int componentDomain )
{
    ContinuousDomain *domain = calloc( 1, sizeof( ContinuousDomain ) );
    domain->componentDomain = componentDomain;
    
    return domain;
}


MeshDomain *createMeshDomain( int xiDomain, int elementDomain )
{
    MeshDomain *domain;
    
    domain = calloc( 1, sizeof( MeshDomain ) );
    domain->xiDomain = xiDomain;
    domain->elementDomain = elementDomain;
    domain->shapes = createIntTable();
    domain->connectivity = createIntTable();
    
    return domain;
}

ContinuousImport *createContinuousImport( char *remoteName, int valueDomain )
{
    ContinuousImport *import;

    import = calloc( 1, sizeof( ContinuousImport ) );
    import->remoteName = _strdup( remoteName );
    import->valueDomain = valueDomain;
    import->aliases = createIntTable();

    return import;
}


Parameters *createParameters( int valueDomain )
{
    Parameters *parameters;

    parameters = calloc( 1, sizeof( Parameters ) );
    parameters->valueDomain = valueDomain;
    parameters->descriptionType = DESCRIPTION_UNKNOWN;

    return parameters;
}


ContinuousPiecewise *createContinuousPiecewise( int valueDomain, int indexDomain )
{
    ContinuousPiecewise *piecewise;
    
    piecewise = calloc( 1, sizeof( ContinuousPiecewise ) );
    piecewise->valueDomain = valueDomain;
    piecewise->indexDomain = indexDomain;
    
    piecewise->evaluators = createIntTable();
    
    return piecewise;
}


ContinuousAggregate *createContinuousAggregate( int valueDomain )
{
    ContinuousAggregate *aggregate;
    
    aggregate = calloc( 1, sizeof( ContinuousAggregate ) );
    aggregate->valueDomain = valueDomain;
    
    aggregate->evaluators = createIntTable();
    
    return aggregate;
}


ContinuousDereference *createContinuousDereference( int valueDomain, int valueIndexes, int valueSource )
{
    ContinuousDereference *dereference;
    
    dereference = calloc( 1, sizeof( ContinuousDereference ) );
    dereference->valueDomain = valueDomain;
    dereference->valueIndexes = valueIndexes;
    dereference->valueSource = valueSource;
    
    return dereference;
}


SemidenseData *createSemidenseData()
{
    SemidenseData *data;
    data = calloc( 1, sizeof( SemidenseData ) );
    data->denseIndexes = createSimpleList();
    data->sparseIndexes = createSimpleList();
    data->locationType = LOCATION_UNKNOWN;

    return data;
}


Variable *createVariable( int valueDomain )
{
    Variable *variable;

    variable = calloc( 1, sizeof( Variable ) );
    variable->valueDomain = valueDomain;
    variable->parameters = createSimpleList();

    return variable;
}


FieldmlObject *createFieldmlObject( const char *name, FieldmlHandleType type )
{
    FieldmlObject *object;
    
    object = calloc( 1, sizeof( FieldmlObject ) );
    object->name = _strdup( name );
    object->type = type;
    object->markup = createStringTable();
    
    return object;
}


static int addObject( FieldmlParse *parse, FieldmlObject *object );


int addEnsembleDomain( FieldmlParse *parse, const char *name, int count )
{
    FieldmlObject *object;
    EnsembleDomain *ensemble;

    ensemble = createEnsembleDomain( FML_INVALID_HANDLE );
    ensemble->boundsType = BOUNDS_DISCRETE_CONTIGUOUS;
    ensemble->bounds.contiguous.count = count;
    object = createFieldmlObject( name, FHT_ENSEMBLE_DOMAIN );
    object->object.ensembleDomain = ensemble;
    return addObject( parse, object );
}


int addContinuousDomain( FieldmlParse *parse, const char *name, int componentHandle )
{
    FieldmlObject *object;
    ContinuousDomain *continuous;

    continuous = createContinuousDomain( componentHandle );
    object = createFieldmlObject( name, FHT_CONTINUOUS_DOMAIN );
    object->object.continuousDomain = continuous;

    return addObject( parse, object );
}


static void addMarkup( FieldmlParse *parse, int handle, const char *attribute, const char *value );


void addLibraryDomains( FieldmlParse *parse )
{
    int handle;

    addContinuousDomain( parse, "library.real.1d", FML_INVALID_HANDLE );
    addContinuousDomain( parse, "library.real.2d", FML_INVALID_HANDLE );
    addContinuousDomain( parse, "library.real.3d", FML_INVALID_HANDLE );
    
    handle = addEnsembleDomain( parse, "library.ensemble.xi.1d", 1 );
    handle = addContinuousDomain( parse, "library.xi.1d", handle );
    addMarkup( parse, handle, "xi", "true" );

    handle = addEnsembleDomain( parse, "library.ensemble.xi.2d", 2 );
    handle = addContinuousDomain( parse, "library.xi.2d", handle );
    addMarkup( parse, handle, "xi", "true" );

    handle = addEnsembleDomain( parse, "library.ensemble.xi.3d", 3 );
    handle = addContinuousDomain( parse, "library.xi.3d", handle );
    addMarkup( parse, handle, "xi", "true" );

    handle = addEnsembleDomain( parse, "library.local_nodes.line.2", 2 );
    addContinuousDomain( parse, "library.parameters.linear_lagrange", handle ); 
    handle = addEnsembleDomain( parse, "library.local_nodes.line.3", 3 );
    addContinuousDomain( parse, "library.parameters.quadratic_lagrange", handle ); 

    handle = addEnsembleDomain( parse, "library.local_nodes.quad.2x2", 4 );
    addContinuousDomain( parse, "library.parameters.bilinear_lagrange", handle ); 
    handle = addEnsembleDomain( parse, "library.local_nodes.quad.3x3", 9 );
    addContinuousDomain( parse, "library.parameters.biquadratic_lagrange", handle ); 

    handle = addEnsembleDomain( parse, "library.local_nodes.cube.2x2x2", 8 );
    addContinuousDomain( parse, "library.parameters.trilinear_lagrange", handle ); 
    handle = addEnsembleDomain( parse, "library.local_nodes.cube.3x3x3", 27 );
    addContinuousDomain( parse, "library.parameters.triquadratic_lagrange", handle ); 
    
    handle = addEnsembleDomain( parse, "library.ensemble.rc.1d", 1 );
    addContinuousDomain( parse, "library.coordinates.rc.1d", handle );
    addContinuousDomain( parse, "library.velocity.rc.1d", handle );
    handle = addEnsembleDomain( parse, "library.ensemble.rc.2d", 2 );
    addContinuousDomain( parse, "library.coordinates.rc.2d", handle );
    addContinuousDomain( parse, "library.velocity.rc.2d", handle );
    handle = addEnsembleDomain( parse, "library.ensemble.rc.3d", 3 );
    addContinuousDomain( parse, "library.coordinates.rc.3d", handle );
    addContinuousDomain( parse, "library.velocity.rc.3d", handle );

    addContinuousDomain( parse, "library.pressure", FML_INVALID_HANDLE );
}


FieldmlParse *createFieldmlParse()
{
    FieldmlParse *parse;

    parse = calloc( 1, sizeof( FieldmlParse ) );

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

void destroyFieldmlContext( FieldmlContext *context )
{
    free( context );
}


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
    free( import->remoteName );
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
    destroyIntTable( piecewise->evaluators, NULL );
    free( piecewise );
}


void destroyContinuousAggregate( ContinuousAggregate *aggregate )
{
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


static void addMarkup( FieldmlParse *parse, int handle, const char *attribute, const char *value )
{
    FieldmlObject *object = (FieldmlObject*)getSimpleListEntry( parse->objects, handle );

    setStringTableEntry( object->markup, attribute, _strdup( value ), free );
}


int intParserCount( const char *buffer )
{
    const char *p = buffer;
    int digits = 0;
    int count = 0;
    char c;
    
    while( *p != 0 )
    {
        c = *p++;
        if( isdigit( c ) )
        {
            digits++;
            continue;
        }
        
        if( digits > 0 )
        {
            count++;
            digits = 0;
        }
    }
    
    return count;
}


const int *intParserInts( const char *buffer )
{
    int count = intParserCount( buffer );
    int *ints = calloc( count, sizeof( int ) );
    const char *p = buffer;
    int number = 0;
    int digits;
    char c;
    
    digits = 0;
    count = 0;
    while( *p != 0 )
    {
        c = *p++;
        if( isdigit( c ) )
        {
            number *= 10;
            number += ( c - '0' );
            digits++;
            continue;
        }
        
        if( digits > 0 )
        {
            ints[count++] = number;
            number = 0;
            digits = 0;
        }
    }
    
    return ints;
}


void addError( FieldmlParse *parse, char *error, char *name1, char *name2 )
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


static int getObjectHandle( FieldmlParse *parse, char *name )
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


static int addObject( FieldmlParse *parse, FieldmlObject *object )
{
    FieldmlObject *oldObject;
    int handle = getObjectHandle( parse, object->name );
    
    if( handle == FML_INVALID_HANDLE )
    {
        return addSimpleListEntry( parse->objects, object );
    }

    oldObject = (FieldmlObject*)getSimpleListEntry( parse->objects, handle );
    if( oldObject->type == FHT_UNKNOWN_ENSEMBLE_DOMAIN )
    {
        if( object->type == FHT_ENSEMBLE_DOMAIN )
        {
            oldObject->type = object->type;
            oldObject->object = object->object;
            object->type = FHT_UNKNOWN_ENSEMBLE_DOMAIN;
            destroyFieldmlObject( object );
            
            return handle;
        }
    }
    else if( oldObject->type == FHT_UNKNOWN_CONTINUOUS_DOMAIN )
    {
        if( object->type == FHT_CONTINUOUS_DOMAIN )
        {
            oldObject->type = object->type;
            oldObject->object = object->object;
            object->type = FHT_UNKNOWN_CONTINUOUS_DOMAIN;
            destroyFieldmlObject( object );
            
            return handle;
        }
    }
    else if( oldObject->type == FHT_UNKNOWN_ENSEMBLE_SOURCE )
    {
        if( ( object->type == FHT_ENSEMBLE_DOMAIN ) ||
            ( object->type == FHT_ENSEMBLE_PARAMETERS ) ||
            ( object->type == FHT_ENSEMBLE_VARIABLE ) )
        {
            oldObject->type = object->type;
            oldObject->object = object->object;
            object->type = FHT_UNKNOWN_ENSEMBLE_SOURCE;
            destroyFieldmlObject( object );
            
            return handle;
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
            oldObject->type = object->type;
            oldObject->object = object->object;
            object->type = FHT_UNKNOWN_CONTINUOUS_SOURCE;
            destroyFieldmlObject( object );
            
            return handle;
        }
    }
    
    addError( parse, "Handle collision. Cannot replace", object->name, oldObject->name );
    destroyFieldmlObject( object );
    
    return FML_INVALID_HANDLE;
}


int getOrCreateObjectHandle( FieldmlParse *parse, char *name, FieldmlHandleType type )
{
    int handle = getObjectHandle( parse, name );

    if( handle == FML_INVALID_HANDLE )
    {
        handle = addObject( parse, createFieldmlObject( name, type ) );
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
    }
}


//========================================================================
//
// Event handlers
//
//========================================================================

void startEnsembleDomain( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *componentEnsemble;
    int handle;
    FieldmlObject *object;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        addError( context->parse, "EnsembleDomain has no name", NULL, NULL );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, "componentDomain" );
    if( componentEnsemble != NULL )
    {
        handle = getOrCreateObjectHandle( context->parse, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
        handle = FML_INVALID_HANDLE;
    }

    object = createFieldmlObject( name, FHT_ENSEMBLE_DOMAIN );
    object->object.ensembleDomain = createEnsembleDomain( handle );

    context->currentObject = object;
}


void startContiguousBounds( FieldmlContext *context, SaxAttributes *attributes )
{
    char *count;
    FieldmlObject *object = context->currentObject;
    EnsembleDomain *domain;
    
    if( object->type == FHT_ENSEMBLE_DOMAIN )
    {
        domain = object->object.ensembleDomain;
    }
    else if( object->type == FHT_MESH_DOMAIN )
    {
        FieldmlObject *subObject = getSimpleListEntry( context->parse->objects, object->object.meshDomain->elementDomain );
        
        if( ( subObject == NULL ) || ( subObject->type != FHT_ENSEMBLE_DOMAIN ) )
        {
            addError( context->parse, "MeshDomain is missing its element domain", object->name, NULL );
            return;
        }
        
        domain = subObject->object.ensembleDomain;
    }
    else
    {
        return;
    }
    
    if( domain->boundsType != BOUNDS_UNKNOWN )
    {
        addError( context->parse, "EnsembleDomain already has a bounds", object->name, NULL );
        return;
    }

    count = getAttribute( attributes, "valueCount" );
    if( count == NULL )
    {
        addError( context->parse, "ContiguousEnsembleBounds for has no value count", object->name, NULL );
        return;
    }

    domain->boundsType = BOUNDS_DISCRETE_CONTIGUOUS;
    domain->bounds.contiguous.count = atoi( count );
}


void endEnsembleDomain( FieldmlContext *context )
{
    EnsembleDomain *domain = context->currentObject->object.ensembleDomain;

    if( domain->boundsType == BOUNDS_UNKNOWN )
    {
        addError( context->parse, "EnsembleDomain has no bounds", context->currentObject->name, NULL );
        destroyFieldmlObject( context->currentObject );
    }
    else
    {
        addObject( context->parse, context->currentObject );
    }
    
    context->currentObject = NULL;
}


void startContinuousDomain( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *componentEnsemble;
    int handle;
    FieldmlObject *object;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        addError( context->parse, "ContinuousDomain has no name", NULL, NULL );
        return;
    }
    
    componentEnsemble = getAttribute( attributes, "componentDomain" );
    if( componentEnsemble != NULL )
    {
        handle = getOrCreateObjectHandle( context->parse, componentEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    }
    else
    {
        handle = FML_INVALID_HANDLE;
    }

    object = createFieldmlObject( name, FHT_CONTINUOUS_DOMAIN );
    object->object.continuousDomain = createContinuousDomain( handle );

    context->currentObject = object;
}


void endContinuousDomain( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startMeshDomain( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *xiEnsemble;
    FieldmlObject *object, *xiObject, *elementObject;
    int handle, xiHandle, elementHandle;
    char *subName;
    
    name = getAttribute( attributes, "name" );
    xiEnsemble = getAttribute( attributes, "xiComponentDomain" );
    
    if( name == NULL )
    {
        addError( context->parse, "MeshDomain has no name", NULL, NULL );
        return;
    }
    
    if( xiEnsemble == NULL )
    {
        addError( context->parse, "MeshDomain has no xi components", name, NULL );
        return;
    }

    subName = calloc( 1, strlen( name ) + 12 );
    
    handle = getOrCreateObjectHandle( context->parse, xiEnsemble, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    strcpy( subName, name );
    strcat( subName, ".xi" );
    xiObject = createFieldmlObject( subName, FHT_CONTINUOUS_DOMAIN );
    xiObject->object.continuousDomain = createContinuousDomain( handle );
    xiHandle = addObject( context->parse, xiObject );
    if( xiHandle == FML_INVALID_HANDLE )
    {
        destroyFieldmlObject( xiObject );
    }
    
    strcpy( subName, name );
    strcat( subName, ".elements" );
    elementObject = createFieldmlObject( subName, FHT_ENSEMBLE_DOMAIN );
    elementObject->object.ensembleDomain = createEnsembleDomain( FML_INVALID_HANDLE );
    elementHandle = addObject( context->parse, elementObject );
    if( elementHandle == FML_INVALID_HANDLE )
    {
        destroyFieldmlObject( elementObject );
    }
    
    object = createFieldmlObject( name, FHT_MESH_DOMAIN );
    object->object.meshDomain = createMeshDomain( xiHandle, elementHandle );

    context->currentObject = object;
}


void onMeshShape( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    MeshDomain *domain = object->object.meshDomain;
    char *element = getAttribute( attributes, "key" );
    char *shape = getAttribute( attributes, "value" );

    if( ( element == NULL ) || ( shape == NULL ) )
    {
        addError( context->parse, "MeshDomain has malformed shape entry", object->name, NULL );
        return;
    }

    setIntTableEntry( domain->shapes, atoi( element ), _strdup( shape ), free );
}


void onMeshConnectivity( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    MeshDomain *domain = object->object.meshDomain;
    char *type = getAttribute( attributes, "key" );
    char *field = getAttribute( attributes, "value" );
    int fieldHandle, domainHandle;

    if( ( type == NULL ) || ( field == NULL ) )
    {
        addError( context->parse, "MeshDomain has malformed connectivity entry", object->name, NULL );
        return;
    }
    
    domainHandle = getOrCreateObjectHandle( context->parse, type, FHT_UNKNOWN_ENSEMBLE_SOURCE );
    fieldHandle = getOrCreateObjectHandle( context->parse, field, FHT_UNKNOWN_ENSEMBLE_SOURCE );

    setIntTableEntry( domain->connectivity, domainHandle, (void*)(fieldHandle + 1), NULL );
}


void endMeshDomain( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousImport( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name;
    char *remoteName;
    char *valueDomain;
    int handle;
    FieldmlObject *object;
        
    name = getAttribute( attributes, "name" );
    if( name == NULL )
    {
        addError( context->parse, "ImportedContinuousEvaluator has no name", NULL, NULL );
        return;
    }
    
    remoteName = getAttribute( attributes, "evaluator" );
    if( remoteName == NULL )
    {
        addError( context->parse, "ImportedContinuousEvaluator has no remote name", name, NULL );
        return;
    }
    
    valueDomain = getAttribute( attributes, "valueDomain" );
    if( valueDomain == NULL )
    {
        addError( context->parse, "ImportedContinuousEvaluator has no value domain", name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    object = createFieldmlObject( name, FHT_CONTINUOUS_IMPORT );
    object->object.continuousImport = createContinuousImport( remoteName, handle );

    context->currentObject = object;
}


void onContinuousImportAlias( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    ContinuousImport *import = object->object.continuousImport;
    char *remote = getAttribute( attributes, "key" );
    char *local = getAttribute( attributes, "value" );
    int localHandle, remoteHandle;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        addError( context->parse, "ImportedContinuousEvaluator has malformed alias", object->name, NULL );
        return;
    }

    localHandle = getOrCreateObjectHandle( context->parse, local, FHT_UNKNOWN_CONTINUOUS_SOURCE );

    remoteHandle = getOrCreateObjectHandle( context->parse, remote, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    setIntTableEntry( import->aliases, localHandle, (void*)(remoteHandle + 1), NULL );
}


void onEnsembleImportAlias( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    ContinuousImport *import = object->object.continuousImport;
    char *remote = getAttribute( attributes, "key" );
    char *local = getAttribute( attributes, "value" );
    int localHandle, remoteHandle;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        addError( context->parse, "ImportedContinuousEvaluator has malformed alias", object->name, NULL );
        return;
    }

    localHandle = getOrCreateObjectHandle( context->parse, local, FHT_UNKNOWN_ENSEMBLE_SOURCE );

    remoteHandle = getOrCreateObjectHandle( context->parse, remote, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    setIntTableEntry( import->aliases, localHandle, (void*)(remoteHandle + 1), NULL );
}


void endContinuousImport( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousDereference( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    char *valueIndexes = getAttribute( attributes, "valueIndexes" );
    char *valueSource = getAttribute( attributes, "valueSource" );
    FieldmlObject *object;
    int valueHandle, indexHandle, sourceHandle;

    if( name == NULL )
    {
        addError( context->parse, "ContinuousDereference has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousDereference has no value domain", name, NULL );
        return;
    }
    if( valueIndexes == NULL )
    {
        addError( context->parse, "ContinuousDereference has no value indexes", name, NULL );
        return;
    }
    if( valueSource == NULL )
    {
        addError( context->parse, "ContinuousDereference has no value source", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    indexHandle = getOrCreateObjectHandle( context->parse, valueIndexes, FHT_UNKNOWN_ENSEMBLE_SOURCE );
    sourceHandle = getOrCreateObjectHandle( context->parse, valueSource, FHT_UNKNOWN_CONTINUOUS_SOURCE );

    object = createFieldmlObject( name, FHT_CONTINUOUS_DEREFERENCE );
    object->object.dereference = createContinuousDereference( valueHandle, indexHandle, sourceHandle );

    context->currentObject = object;
}


void endContinuousDereference( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startEnsembleParameters( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    FieldmlObject *object;
    int handle;

    if( name == NULL )
    {
        addError( context->parse, "EnsembleParameters has no name", NULL, NULL );
        return;
    }

    if( valueDomain == NULL )
    {
        addError( context->parse, "EnsembleParameters has no value domain", name, NULL );
        return;
    }

    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );
    
    object = createFieldmlObject( name, FHT_ENSEMBLE_PARAMETERS );
    object->object.parameters = createParameters( handle );

    context->currentObject = object;
}


void endEnsembleParameters( FieldmlContext *context )
{
    Parameters *parameters = context->currentObject->object.parameters;

    if( parameters->descriptionType == DESCRIPTION_UNKNOWN )
    {
        addError( context->parse, "EnsembleParameters has no data", context->currentObject->name, NULL );
        destroyFieldmlObject( context->currentObject );
    }
    else if( ( parameters->descriptionType == DESCRIPTION_SEMIDENSE ) &&
          ( parameters->dataDescription.semidense->locationType == LOCATION_UNKNOWN ) )
    {
        addError( context->parse, "EnsembleParameters has no data", context->currentObject->name, NULL );
        destroyFieldmlObject( context->currentObject );
    }
    else
    {
        addObject( context->parse, context->currentObject );
    }
    
    context->currentObject = NULL;
}


void startContinuousParameters( FieldmlContext *context, SaxAttributes *attributes )
{
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    FieldmlObject *object;
    int handle;

    if( name == NULL )
    {
        addError( context->parse, "ContinuousParameters has no name", NULL, NULL );
        return;
    }

    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousParameters has no value domain", name, NULL );
        return;
    }

    handle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    
    object = createFieldmlObject( name, FHT_CONTINUOUS_PARAMETERS );
    object->object.parameters = createParameters( handle );

    context->currentObject = object;
}


void endContinuousParameters( FieldmlContext *context )
{
    Parameters *parameters = context->currentObject->object.parameters;

    if( parameters->descriptionType == DESCRIPTION_UNKNOWN )
    {
        addError( context->parse, "ContinuousParameters has no data", context->currentObject->name, NULL );
        destroyFieldmlObject( context->currentObject );
    }
    else if( ( parameters->descriptionType == DESCRIPTION_SEMIDENSE ) &&
          ( parameters->dataDescription.semidense->locationType == LOCATION_UNKNOWN ) )
    {
        addError( context->parse, "ContinuousParameters has no data", context->currentObject->name, NULL );
        destroyFieldmlObject( context->currentObject );
    }
    else
    {
        addObject( context->parse, context->currentObject );
    }
    
    context->currentObject = NULL;
}


void startInlineData( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->locationType != LOCATION_UNKNOWN )
        {
            addError( context->parse, "Parameters already has data", object->name, NULL );
            return;
        }
        parameters->dataDescription.semidense->locationType = LOCATION_INLINE;
    }
}


void onInlineData( FieldmlContext *context, const char *const characters, const int length )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    StringDataSource *source;
    char *newString;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->locationType != LOCATION_INLINE )
        {
            return;
        }
        source = &(parameters->dataDescription.semidense->dataLocation.stringData);
    }
    

    newString = malloc( source->length + length + 1 );
    memcpy( newString, source->string, source->length );
    memcpy( newString + source->length, characters, length );
    source->length += length;
    newString[ source->length ] = 0;
    free( source->string );
    source->string = newString;
}


void onFileData( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    char *file = getAttribute( attributes, "file" );
    char *type = getAttribute( attributes, "type" );
    char *offset = getAttribute( attributes, "offset" );
    FileDataSource *source;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->locationType != LOCATION_UNKNOWN )
        {
            addError( context->parse, "Parameters already has data", object->name, NULL );
            return;
        }
    }
    
    if( file == NULL )
    {
        addError( context->parse, "Parameters file data for must have a file name", object->name, NULL );
        return;
    }
    if( type == NULL )
    {
        addError( context->parse, "Parameters file data for must have a file type", object->name, NULL );
        return;
    }
    
    parameters->dataDescription.semidense->locationType = LOCATION_FILE;

    source = &(parameters->dataDescription.semidense->dataLocation.fileData);
    source->filename = _strdup( file );
    source->isText = ( strcmp( type, "text" ) != 0 );
    if( offset == NULL )
    {
        source->offset = 0;
    }
    else
    {
        source->offset = atoi( offset );
    }
}


void startSwizzleData( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        if( parameters->dataDescription.semidense->swizzleCount > 0 )
        {
            addError( context->parse, "Parameters already has a swizzle", object->name, NULL );
            return;
        }
    }
}


void onSwizzleData( FieldmlContext *context, const char *const characters, const int length )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    char *newString;
    
    newString = malloc( context->bufferLength + length + 1 );
    if( context->buffer != NULL )
    {
        memcpy( newString, context->buffer, context->bufferLength );
    }
    memcpy( newString + context->bufferLength, characters, length );
    context->bufferLength += length;
    newString[ context->bufferLength ] = 0;
    free( context->buffer );
    context->buffer = newString;
}


void endSwizzleData( FieldmlContext *context )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    
    
    if( parameters->descriptionType == DESCRIPTION_SEMIDENSE )
    {
        parameters->dataDescription.semidense->swizzleCount = intParserCount( context->buffer );
        parameters->dataDescription.semidense->swizzle = intParserInts( context->buffer );
    }
    
    free( context->buffer );
    context->buffer = NULL;
    context->bufferLength = 0;
}


void startSemidenseData( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    
    if( parameters->descriptionType != DESCRIPTION_UNKNOWN )
    {
        addError( context->parse, "Parameters already has data", object->name, NULL );
        return;
    }
    
    parameters->descriptionType = DESCRIPTION_SEMIDENSE;
    parameters->dataDescription.semidense = createSemidenseData();
}


void onSemidenseSparseIndex( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    char *index;
    int handle;
    
    if( parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return;
    }

    index = getAttribute( attributes, "value" );
    if( index == NULL )
    {
        addError( context->parse, "Missing index in semi dense data", object->name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    addSimpleListEntry( parameters->dataDescription.semidense->sparseIndexes, (void*)(handle + 1) );//HACK!!
}


void onSemidenseDenseIndex( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    Parameters *parameters = object->object.parameters;
    char *index;
    int handle;

    if( parameters->descriptionType != DESCRIPTION_SEMIDENSE )
    {
        return;
    }

    index = getAttribute( attributes, "value" );
    if( index == NULL )
    {
        addError( context->parse, "Missing index in semi dense data", object->name, NULL );
        return;
    }

    handle = getOrCreateObjectHandle( context->parse, index, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    addSimpleListEntry( parameters->dataDescription.semidense->denseIndexes, (void*)(handle + 1) );//HACK!!
}


void endSemidenseData( FieldmlContext *context )
{
}


void startContinuousPiecewise( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object;
    char *name;
    char *valueDomain;
    char *indexDomain;
    int valueHandle, indexHandle;
    
    name = getAttribute( attributes, "name" );
    valueDomain = getAttribute( attributes, "valueDomain" );
    indexDomain = getAttribute( attributes, "indexDomain" );
    
    if( name == NULL )
    {
        addError( context->parse, "ContinuousPiecewise has no name", NULL, NULL );
        return;
    }
    
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousPiecewise has no value domain", name, NULL );
        return;
    }
    
    if( indexDomain == NULL )
    {
        addError( context->parse, "ContinuousPiecewise has no index domain", name, NULL );
        return;
    }
    
    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );
    indexHandle = getOrCreateObjectHandle( context->parse, indexDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    object = createFieldmlObject( name, FHT_CONTINUOUS_PIECEWISE );
    object->object.piecewise = createContinuousPiecewise( valueHandle, indexHandle );

    context->currentObject = object;
}


void onContinuousPiecewiseEntry( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    ContinuousPiecewise *piecewise = (ContinuousPiecewise*)object->object.piecewise;
    char *key;
    char *value;
    int handle;
    
    key = getAttribute( attributes, "key" );
    value = getAttribute( attributes, "value" );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        addError( context->parse, "Malformed element evaluator for ContinuousPiecewise", object->name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
    
    setIntTableEntry( piecewise->evaluators, atoi( key ), (void*)(handle + 1), NULL ); //HACK!!
}


void endContinuousPiecewise( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousAggregate( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object;
    char *name;
    char *valueDomain;
    int valueHandle;
    
    name = getAttribute( attributes, "name" );
    valueDomain = getAttribute( attributes, "valueDomain" );
    
    if( name == NULL )
    {
        addError( context->parse, "ContinuousAggregate has no name", NULL, NULL );
        return;
    }
    
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousAggregate has no value domain", name, NULL );
        return;
    }
    
    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    object = createFieldmlObject( name, FHT_CONTINUOUS_AGGREGATE );
    object->object.aggregate = createContinuousAggregate( valueHandle );

    context->currentObject = object;
}


void onContinuousAggregateEntry( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    ContinuousAggregate *aggregate = (ContinuousAggregate*)object->object.aggregate;
    char *key;
    char *value;
    int handle;
    
    key = getAttribute( attributes, "key" );
    value = getAttribute( attributes, "value" );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        addError( context->parse, "Malformed element evaluator for ContinuousAggregate", object->name, NULL );
        return;
    }
    
    handle = getOrCreateObjectHandle( context->parse, value, FHT_UNKNOWN_CONTINUOUS_SOURCE );
    
    setIntTableEntry( aggregate->evaluators, atoi( key ), (void*)(handle + 1), NULL ); //HACK!!
}


void endContinuousAggregate( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void startContinuousVariable( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object;
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    int valueHandle;

    if( name == NULL )
    {
        addError( context->parse, "ContinuousVariable has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        addError( context->parse, "ContinuousVariable has no value domain", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_CONTINUOUS_DOMAIN );

    object = createFieldmlObject( name, FHT_CONTINUOUS_VARIABLE );
    object->object.variable = createVariable( valueHandle );

    context->currentObject = object;
}


void startEnsembleVariable( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object;
    char *name = getAttribute( attributes, "name" );
    char *valueDomain = getAttribute( attributes, "valueDomain" );
    int valueHandle;
    
    if( name == NULL )
    {
        addError( context->parse, "EnsembleVariable has no name", NULL, NULL );
        return;
    }
    if( valueDomain == NULL )
    {
        addError( context->parse, "EnsembleVariable has no value domain", name, NULL );
        return;
    }

    valueHandle = getOrCreateObjectHandle( context->parse, valueDomain, FHT_UNKNOWN_ENSEMBLE_DOMAIN );

    object = createFieldmlObject( name, FHT_ENSEMBLE_VARIABLE );
    object->object.variable = createVariable( valueHandle );

    context->currentObject = object;
}


void endVariable( FieldmlContext *context )
{
    addObject( context->parse, context->currentObject );
    context->currentObject = NULL;
}


void onMarkupEntry( FieldmlContext *context, SaxAttributes *attributes )
{
    FieldmlObject *object = (FieldmlObject*)context->currentObject;
    char *key;
    char *value;
    
    if( object == NULL )
    {
        addError( context->parse, "Unexpected markup", NULL, NULL );
        return;
    }
    
    key = getAttribute( attributes, "key" );
    value = getAttribute( attributes, "value" );
    
    if( ( key == NULL ) || ( value == NULL ) )
    {
        addError( context->parse, "Malformed markup", object->name, NULL );
        return;
    }

    setStringTableEntry( object->markup, key, _strdup( value ), free );
}
