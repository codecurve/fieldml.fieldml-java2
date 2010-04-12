#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <libxml/sax.h>

#include "fieldml_parse.h"
#include "string_table.h"
#include "simple_list.h"
#include "fieldml_sax.h"

struct _FieldmlParse
{
    StringTable *ensembleDomains;
    StringTable *continuousDomains;
    StringTable *contiguousBounds;
    StringTable *continuousImports;
    StringTable *ensembleParameters;
    StringTable *continuousParameters;
    StringTable *semidenseData;
    StringTable *variables;
};

typedef enum _EnsembleBoundsType
{
    BOUNDS_UNKNOWN,     // EnsembleDomain bounds not yet known.
    BOUNDS_CONTIGUOUS,  // Contiguous bounds (i.e. 1 ... N)
    BOUNDS_ARBITRARY,   // Arbitrary bounds (not yet supported)
}
EnsembleBoundsType;


typedef enum _ParameterStorageType
{
    STORAGE_UNKNOWN,
    STORAGE_SEMIDENSE,
}
ParameterStorageType;


typedef struct _EnsembleDomain
{
    char *name;
    char *componentEnsemble;

    EnsembleBoundsType boundsType;
}
EnsembleDomain;


typedef struct _ContinuousDomain
{
    char *name;
    char *baseName;
    char *componentEnsemble;
}
ContinuousDomain;


typedef struct _ContiguousBounds
{
    int count;
}
ContiguousBounds;


typedef struct _ContinuousImport
{
    char *name;
    char *remoteName;
    char *valueDomain;

    StringTable *aliases;
}
ContinuousImport;


typedef struct _EnsembleParameters
{
    char *name;
    char *valueDomain;

    ParameterStorageType storageType;
}
EnsembleParameters;


typedef struct _ContinuousParameters
{
    char *name;
    char *valueDomain;

    ParameterStorageType storageType;
}
ContinuousParameters;


typedef enum _DataLocation
{
    LOC_UNKNOWN,
    LOC_INLINE,
    LOC_FILE,
}
DataLocation;


typedef struct _StringDataSource
{
    char *string;
    int length;
}
StringDataSource;


typedef struct _FileDataSource
{
    char *filename;
    int offset;
    int isText;
}
FileDataSource;


typedef struct _DataSource
{
    DataLocation location;
    union TaggedDataSource
    {
        StringDataSource stringData;
        FileDataSource fileData;
    }
    data;
}
DataSource;


typedef struct _SemidenseData
{
    SimpleList *sparseIndexes;
    SimpleList *denseIndexes;

    DataSource dataSource;
}
SemidenseData;


typedef struct _Variable
{
    char *name;
    char *valueDomain;
    SimpleList *parameters;
}
Variable;

//========================================================================
//
// Creators
//
//========================================================================


FieldmlParse *createFieldmlParse()
{
    FieldmlParse *parse;

    parse = calloc( 1, sizeof( FieldmlParse ) );
    parse->ensembleDomains = createStringTable();
    parse->continuousDomains = createStringTable();
    parse->contiguousBounds = createStringTable();
    parse->continuousImports = createStringTable();
    parse->ensembleParameters = createStringTable();
    parse->continuousParameters = createStringTable();
    parse->semidenseData = createStringTable();
    parse->variables = createStringTable();

    return parse;
}


EnsembleDomain *createEnsembleDomain( char *name, char *componentEnsemble )
{
    EnsembleDomain *domain;

    domain = calloc( 1, sizeof( EnsembleDomain ) );
    domain->name = _strdup( name );
    domain->componentEnsemble = _strdup( componentEnsemble );
    domain->boundsType = BOUNDS_UNKNOWN;

    return domain;
}


ContiguousBounds *createContiguousBounds( char *countString )
{
    ContiguousBounds *bounds;

    bounds = calloc( 1, sizeof( ContiguousBounds ) );
    bounds->count = atoi( countString );

    return bounds;
}


ContinuousDomain *createContinuousDomain( char *name, char *baseDomain, char *componentEnsemble )
{
    ContinuousDomain *domain;

    domain = calloc( 1, sizeof( ContinuousDomain ) );
    domain->name = _strdup( name );
    domain->baseName = _strdup( baseDomain );
    domain->componentEnsemble = _strdup( componentEnsemble );

    return domain;
}


ContinuousImport *createContinuousImport( char *name, char *remoteName, char *valueDomain )
{
    ContinuousImport *import;

    import = calloc( 1, sizeof( ContinuousImport ) );
    import->name = _strdup( name );
    import->remoteName = _strdup( remoteName );
    import->valueDomain = _strdup( valueDomain );
    import->aliases = createStringTable();

    return import;
}


EnsembleParameters *createEnsembleParameters( char *name, char *valueDomain )
{
    EnsembleParameters *parameters;

    parameters = calloc( 1, sizeof( EnsembleParameters ) );
    parameters->name = _strdup( name );
    parameters->valueDomain = _strdup( valueDomain );
    parameters->storageType = STORAGE_UNKNOWN;

    return parameters;
}


ContinuousParameters *createContinuousParameters( char *name, char *valueDomain )
{
    ContinuousParameters *parameters;

    parameters = calloc( 1, sizeof( ContinuousParameters ) );
    parameters->name = _strdup( name );
    parameters->valueDomain = _strdup( valueDomain );
    parameters->storageType = STORAGE_UNKNOWN;

    return parameters;
}


SemidenseData *createSemidenseData()
{
    SemidenseData *data;
    data = calloc( 1, sizeof( SemidenseData ) );
    data->denseIndexes = createSimpleList();
    data->sparseIndexes = createSimpleList();
    data->dataSource.location = LOC_UNKNOWN;

    return data;
}


Variable *createVariable( char *name, char *valueDomain )
{
    Variable *variable;

    variable = calloc( 1, sizeof( Variable ) );
    variable->name = _strdup( name );
    variable->valueDomain = _strdup( valueDomain );
    variable->parameters = createSimpleList();

    return variable;
}

//========================================================================
//
// Destroyers
//
//========================================================================

void destroyEnsembleDomain( EnsembleDomain *domain )
{
    free( domain->name );
    free( domain->componentEnsemble );
    free( domain );
}


void destroyContinuousDomain( ContinuousDomain *domain )
{
    free( domain->name );
    free( domain->baseName );
    free( domain->componentEnsemble );
    free( domain );
}


void destroyContinuousImport( ContinuousImport *import )
{
    free( import->name );
    free( import->remoteName );
    free( import->valueDomain );
    destroyStringTable( import->aliases, free );
    free( import );
}


void destroyEnsembleParameters( EnsembleParameters *parameters )
{
    free( parameters->name );
    free( parameters->valueDomain );

    switch( parameters->storageType )
    {
    case STORAGE_SEMIDENSE:
        break;
    default:
        break;
    }

    free( parameters );
}


void destroyContinuousParameters( ContinuousParameters *parameters )
{
    free( parameters->name );
    free( parameters->valueDomain );

    switch( parameters->storageType )
    {
    case STORAGE_SEMIDENSE:
        break;
    default:
        break;
    }

    free( parameters );
}


void destroySemidenseData( SemidenseData *data )
{
    destroySimpleList( data->sparseIndexes, free );
    destroySimpleList( data->denseIndexes, free );
    
    if( data->dataSource.location == LOC_INLINE )
    {
    	free( data->dataSource.data.stringData.string );
    }
    else if( data->dataSource.location == LOC_FILE )
    {
    	free( data->dataSource.data.fileData.filename );
    }
    
    free( data );
}


void destroyVariable( Variable *variable )
{
    free( variable->name );
    free( variable->valueDomain );
    destroySimpleList( variable->parameters, free );

    free( variable );
}


void destroyFieldmlParse( FieldmlParse *parse )
{
    destroyStringTable( parse->ensembleDomains, destroyEnsembleDomain );
    destroyStringTable( parse->contiguousBounds, free );
    destroyStringTable( parse->continuousDomains, destroyContinuousDomain );
    destroyStringTable( parse->continuousImports, destroyContinuousImport );
    destroyStringTable( parse->continuousParameters, destroyContinuousParameters );
    destroyStringTable( parse->ensembleParameters, destroyEnsembleParameters );
    destroyStringTable( parse->semidenseData, destroySemidenseData );
    destroyStringTable( parse->variables, destroyVariable );

    free( parse );
}


//========================================================================
//
// Event handlers
//
//========================================================================

void startEnsembleDomain( SaxContext *context, SaxAttributes *saxAttributes )
{
    char *name;
    char *componentEnsemble;
    EnsembleDomain *domain;
        
    name = getAttribute( saxAttributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "EnsembleDomain has no name\n" );
        return;
    }
    
    componentEnsemble = getAttribute( saxAttributes, "componentDomain" );

    domain = createEnsembleDomain( name, componentEnsemble );

    context->currentObject = domain;
}


void endEnsembleDomain( SaxContext *context )
{
    EnsembleDomain *domain = (EnsembleDomain*)context->currentObject;
    context->currentObject = NULL;

    if( domain->boundsType == BOUNDS_UNKNOWN )
    {
        fprintf( stderr, "EnsembleDomain %s has no bounds\n", domain->name );
        destroyEnsembleDomain( domain );
        return;
    }

    setEntry( context->parse->ensembleDomains, domain->name, domain, destroyEnsembleDomain );
}


void startContinuousDomain( SaxContext *context, SaxAttributes *saxAttributes )
{
    char *name;
    char *baseDomain;
    char *componentEnsemble;
    ContinuousDomain *domain;
        
    name = getAttribute( saxAttributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "ContinuousDomain has no name\n" );
        return;
    }
    
    baseDomain = getAttribute( saxAttributes, "baseDomain" );
    if( baseDomain == NULL )
    {
        fprintf( stderr, "ContinuousDomain %s has no base domain\n", name );
        return;
    }

    componentEnsemble = getAttribute( saxAttributes, "componentDomain" );

    domain = createContinuousDomain( name, baseDomain, componentEnsemble );

    context->currentObject = domain;
}


void endContinuousDomain( SaxContext *context )
{
    ContinuousDomain *domain = (ContinuousDomain*)context->currentObject;
    context->currentObject = NULL;

    setEntry( context->parse->continuousDomains, domain->name, domain, destroyContinuousDomain );
}


void startContiguousBounds( SaxContext *context, SaxAttributes *saxAttributes )
{
    char * count;
    EnsembleDomain *domain;
    ContiguousBounds *bounds;
        
    domain = (EnsembleDomain*)context->currentObject;

    count = getAttribute( saxAttributes, "valueCount" );
    if( count == NULL )
    {
        fprintf( stderr, "ContiguousEnsembleBounds for %s has no value count\n", domain->name );
        return;
    }

    domain->boundsType = BOUNDS_CONTIGUOUS;

    bounds = createContiguousBounds( count );

    setEntry( context->parse->contiguousBounds, domain->name, bounds, free );
}


void dumpFieldmlParse( FieldmlParse *parse )
{
    int i, count;

    count = getCount( parse->continuousDomains );
    fprintf( stdout, "ContinuousDomains:\n" );
    for( i = 0; i < count; i++ )
    {
        fprintf( stdout, "    %s\n", ((ContinuousDomain*)getData( parse->continuousDomains, i ))->name );
    }

    count = getCount( parse->ensembleDomains );
    fprintf( stdout, "EnsembleDomains:\n" );
    for( i = 0; i < count; i++ )
    {
        fprintf( stdout, "    %s\n", ((EnsembleDomain*)getData( parse->ensembleDomains, i ))->name );
    }

    count = getCount( parse->continuousImports );
    fprintf( stdout, "Continuous Imports:\n" );
    for( i = 0; i < count; i++ )
    {
        int j, count2;
        ContinuousImport *import;

        import = (ContinuousImport*)getData( parse->continuousImports, i );

        fprintf( stdout, "    %s\n", import->name );
        
        count2 = getCount( import->aliases );
        for( j = 0; j < count2; j++ )
        {
            fprintf( stdout, "        %s -> %s\n", getName( import->aliases, j ), getData( import->aliases, j ) );
        }
    }

    count = getCount( parse->continuousParameters );
    fprintf( stdout, "Continuous Parameters:\n" );
    for( i = 0; i < count; i++ )
    {
        ContinuousParameters *params;
        SemidenseData *data;

        params = (ContinuousParameters*)getData( parse->continuousParameters, i );
        data = (SemidenseData*)getEntry( parse->semidenseData, params->name );

        fprintf( stdout, "%    s\n", params->name );
        
        if( data->dataSource.location == LOC_INLINE )
        {
			fprintf( stdout, "    *******************************\n");
			fprintf( stdout, "%s\n", data->dataSource.data.stringData.string );
			fprintf( stdout, "    *******************************\n");
        }
        else if( data->dataSource.location == LOC_FILE )
        {
        	fprintf( stdout, "    file = %s, offset = %d\n", data->dataSource.data.fileData.filename, data->dataSource.data.fileData.offset );
        }
    }

    count = getCount( parse->ensembleParameters );
    fprintf( stdout, "Ensemble Parameters:\n" );
    for( i = 0; i < count; i++ )
    {
        EnsembleParameters *params;
        SemidenseData *data;

        params = (EnsembleParameters*)getData( parse->ensembleParameters, i );
        data = (SemidenseData*)getEntry( parse->semidenseData, params->name );

        fprintf( stdout, "%    s\n", params->name );

        if( data->dataSource.location == LOC_INLINE )
        {
			fprintf( stdout, "    *******************************\n");
			fprintf( stdout, "%s\n", data->dataSource.data.stringData.string );
			fprintf( stdout, "    *******************************\n");
        }
        else if( data->dataSource.location == LOC_FILE )
        {
        	fprintf( stdout, "    file = %s, offset = %d\n", data->dataSource.data.fileData.filename, data->dataSource.data.fileData.offset );
        }
    }
}


void startContinuousImport( SaxContext *context, SaxAttributes *saxAttributes )
{
    char *name;
    char *remoteName;
    char *valueDomain;
    ContinuousImport *import;
        
    name = getAttribute( saxAttributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "ImportedContinuousEvaluator has no name\n" );
        return;
    }
    
    remoteName = getAttribute( saxAttributes, "evaluator" );
    if( remoteName == NULL )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has no remote name\n", name );
        return;
    }
    
    valueDomain = getAttribute( saxAttributes, "valueDomain" );
    if( valueDomain == NULL )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has no value domain\n", name );
        return;
    }

    import = createContinuousImport( name, remoteName, valueDomain );

    context->currentObject = import;
}

void continuousImportAlias( SaxContext *context, SaxAttributes *saxAttributes )
{
    ContinuousImport *import;
    char *remote = getAttribute( saxAttributes, "key" );
    char *local = getAttribute( saxAttributes, "value" );

    import = (ContinuousImport*)context->currentObject;

    if( ( remote == NULL ) || ( local == NULL ) )
    {
        fprintf( stderr, "ImportedContinuousEvaluator %s has malformed alias\n", import->name );
        return;
    }

    setEntry( import->aliases, remote, _strdup( local ), free );
}


void endContinuousImport( SaxContext *context )
{
    ContinuousImport *import = (ContinuousImport*)context->currentObject;
    context->currentObject = NULL;

    setEntry( context->parse->continuousImports, import->name, import, destroyContinuousImport );
}


void startEnsembleParameters( SaxContext *context, SaxAttributes *saxAttributes )
{
    EnsembleParameters *parameters;

    char *name = getAttribute( saxAttributes, "name" );
    char *valueDomain = getAttribute( saxAttributes, "valueDomain" );

    if( name == NULL )
    {
        fprintf( stderr, "EnsembleParameters has no name\n" );
        return;
    }

    if( valueDomain == NULL )
    {
        fprintf( stderr, "EnsembleParameters %s has no value domain\n", name );
        return;
    }

    parameters = createEnsembleParameters( name, valueDomain );

    context->currentObject = parameters;
}


void endEnsembleParameters( SaxContext *context )
{
    EnsembleParameters *parameters = (EnsembleParameters*)context->currentObject;
    context->currentObject = NULL;

    if( parameters->storageType == STORAGE_UNKNOWN )
    {
        fprintf( stderr, "EnsembleParameters %s has no data\n", parameters->name );
        destroyEnsembleParameters( parameters );
        return;
    }

    setEntry( context->parse->ensembleParameters, parameters->name, parameters, destroyEnsembleParameters );
}


void startContinuousParameters( SaxContext *context, SaxAttributes *saxAttributes )
{
    ContinuousParameters *parameters;

    char *name = getAttribute( saxAttributes, "name" );
    char *valueDomain = getAttribute( saxAttributes, "valueDomain" );

    if( name == NULL )
    {
        fprintf( stderr, "ContinuousParameters has no name\n" );
        return;
    }

    if( valueDomain == NULL )
    {
        fprintf( stderr, "ContinuousParameters %s has no value domain\n", name );
        return;
    }

    parameters = createContinuousParameters( name, valueDomain );

    context->currentObject = parameters;
}


void endContinuousParameters( SaxContext *context )
{
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    context->currentObject = NULL;

    if( parameters->storageType == STORAGE_UNKNOWN )
    {
        fprintf( stderr, "ContinuousParameters %s has no data\n", parameters->name );
        destroyContinuousParameters( parameters );
        return;
    }

    setEntry( context->parse->continuousParameters, parameters->name, parameters, destroyContinuousParameters );
}


void startSemidenseData( SaxContext *context, SaxAttributes *saxAttributes, int isEnsemble )
{
    SemidenseData *data = createSemidenseData();

    context->currentObject2 = data;
}


void semidenseIndex( SaxContext *context, SaxAttributes *saxAttributes, int isSparse )
{
    SemidenseData *data = (SemidenseData*)context->currentObject2;

    char *index = getAttribute( saxAttributes, "value" );
    if( index == NULL )
    {
        fprintf( stderr, "Invalid index in semi dense data\n" );
        return;
    }

    if( isSparse )
    {
        addListEntry( data->sparseIndexes, _strdup( index ) );
    }
    else
    {
        addListEntry( data->denseIndexes, _strdup( index ) );
    }
}


void semidenseStartInlineData( SaxContext *context, SaxAttributes *saxAttributes )
{
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    SemidenseData *data = (SemidenseData*)context->currentObject2;
    
    if( data->dataSource.location != LOC_UNKNOWN )
    {
        fprintf( stderr, "Semidense data for %s already has data\n", parameters->name );
        return;
    }
    
    data->dataSource.location = LOC_INLINE;
}


void semidenseFileData( SaxContext *context, SaxAttributes *saxAttributes )
{
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    SemidenseData *data = (SemidenseData*)context->currentObject2;
    char *file = getAttribute( saxAttributes, "file" );
    char *type = getAttribute( saxAttributes, "type" );
    char *offset = getAttribute( saxAttributes, "offset" );
    FileDataSource *source;
    
    if( data->dataSource.location != LOC_UNKNOWN )
    {
        fprintf( stderr, "Semidense data for %s already has data\n", parameters->name );
        return;
    }
    
    if( file == NULL )
    {
        fprintf( stderr, "Semidense file data for %s must have a file name\n", parameters->name );
        return;
    }
    if( type == NULL )
    {
        fprintf( stderr, "Semidense file data for %s must have a file type\n", parameters->name );
        return;
    }
    
    data->dataSource.location = LOC_FILE;

    source = &(data->dataSource.data.fileData);
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


void semidenseInlineData( SaxContext *context, const char *const characters, const int length )
{
    StringDataSource *source;
    char *newString;
    ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
    SemidenseData *data = (SemidenseData*)context->currentObject2;

    if( data->dataSource.location != LOC_INLINE )
    {
        fprintf( stderr, "Semidense data for %s already has non-inline data\n", parameters->name );
        return;
    }

    source = &(data->dataSource.data.stringData);

    newString = malloc( source->length + length + 1 );
    memcpy( newString, source->string, source->length );
    memcpy( newString + source->length, characters, length );
    source->length += length;
    newString[ source->length ] = 0;
    free( source->string );
    source->string = newString;
}


void endSemidenseData( SaxContext *context, int isEnsemble )
{
    SemidenseData *data = (SemidenseData*)context->currentObject2;
    context->currentObject2 = NULL;

    if( isEnsemble )
    {
        EnsembleParameters *parameters = (EnsembleParameters*)context->currentObject;
        
        parameters->storageType = STORAGE_SEMIDENSE;
        setEntry( context->parse->semidenseData, parameters->name, data, destroySemidenseData );
    }
    else
    {
        ContinuousParameters *parameters = (ContinuousParameters*)context->currentObject;
        
        parameters->storageType = STORAGE_SEMIDENSE;
        setEntry( context->parse->semidenseData, parameters->name, data, destroySemidenseData );
    }
}


void startVariable( SaxContext *context, SaxAttributes *saxAttributes )
{
    Variable *variable;

    char *name = getAttribute( saxAttributes, "name" );
    char *valueDomain = getAttribute( saxAttributes, "valueDomain" );

    if( name == NULL )
    {
        fprintf( stderr, "Variable has no name\n" );
        return;
    }
    if( valueDomain == NULL )
    {
        fprintf( stderr, "Variable %s has no value domain\n", name );
        return;
    }

    variable = createVariable( name, valueDomain );

    context->currentObject = variable;
}


void endVariable( SaxContext *context )
{
    Variable *variable = (Variable*)context->currentObject;
    context->currentObject = NULL;

    setEntry( context->parse->variables, variable->name, variable, destroyVariable );
}
