#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <libxml/sax.h>

#include "fieldml_parse.h"
#include "string_table.h"
#include "fieldml_sax.h"

struct _FieldmlParse
{
    StringTable *ensembleDomains;
    StringTable *continuousDomains;
    StringTable *contiguousBounds;
    StringTable *continuousImports;
    StringTable *ensembleParameters;
    StringTable *continuousParameters;
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
    char *baseName;
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

    return parse;
}


EnsembleDomain *createEnsembleDomain( char *name, char *baseDomain, char *componentEnsemble )
{
    EnsembleDomain *domain;

    domain = calloc( 1, sizeof( EnsembleDomain ) );
    domain->name = _strdup( name );
    domain->baseName = _strdup( baseDomain );
    domain->componentEnsemble = _strdup( componentEnsemble );
    domain->boundsType = BOUNDS_UNKNOWN;

    return domain;
}


//========================================================================
//
// Destroyers
//
//========================================================================

void destroyEnsembleDomain( EnsembleDomain *domain )
{
    free( domain->name );
    free( domain->baseName );
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


void destroyFieldmlParse( FieldmlParse *parse )
{
    destroyStringTable( parse->ensembleDomains, destroyEnsembleDomain );
    destroyStringTable( parse->contiguousBounds, free );
    destroyStringTable( parse->continuousDomains, destroyContinuousDomain );

    free( parse );
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

//========================================================================
//
// Event handlers
//
//========================================================================

void startEnsembleDomain( SaxContext *context, SaxAttributes *saxAttributes )
{
    char *name;
    char *baseDomain;
    char *componentEnsemble;
    EnsembleDomain *domain;
        
    name = getAttribute( saxAttributes, "name" );
    if( name == NULL )
    {
        fprintf( stderr, "EnsembleDomain has no name\n" );
        return;
    }
    
    baseDomain = getAttribute( saxAttributes, "baseDomain" );
    if( baseDomain == NULL )
    {
        fprintf( stderr, "EnsembleDomain %s has no base domain\n", name );
        return;
    }

    componentEnsemble = getAttribute( saxAttributes, "componentDomain" );

    domain = createEnsembleDomain( name, baseDomain, componentEnsemble );

    context->currentObject = domain;
}


void endEnsembleDomain( SaxContext *context )
{
    EnsembleDomain *domain = (EnsembleDomain*)context->currentObject;

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

    domain = calloc( 1, sizeof( ContinuousDomain ) );
    domain->name = _strdup( name );
    domain->baseName = _strdup( baseDomain );
    domain->componentEnsemble = _strdup( componentEnsemble );

    context->currentObject = domain;
}


void endContinuousDomain( SaxContext *context )
{
    ContinuousDomain *domain = (ContinuousDomain*)context->currentObject;

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

    bounds = calloc( 1, sizeof( ContiguousBounds ) );
    bounds->count = atoi( count );

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

    import = calloc( 1, sizeof( ContinuousImport ) );
    import->name = _strdup( name );
    import->remoteName = _strdup( remoteName );
    import->valueDomain = _strdup( valueDomain );
    import->aliases = createStringTable();

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

    parameters = calloc( 1, sizeof( EnsembleParameters ) );

    parameters->name = _strdup( name );
    parameters->valueDomain = _strdup( valueDomain );
    parameters->storageType = STORAGE_UNKNOWN;

    context->currentObject = parameters;
}


void endEnsembleParameters( SaxContext *context )
{

    EnsembleParameters *parameters = (EnsembleParameters*)context->currentObject;

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
}


void endContinuousParameters( SaxContext *context )
{
}
