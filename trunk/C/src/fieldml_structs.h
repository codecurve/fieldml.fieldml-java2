#ifndef H_FIELDML_STRUCTS
#define H_FIELDML_STRUCTS

#include "simple_list.h"
#include "string_table.h"
#include "int_stack.h"
#include "int_table.h"
#include "fieldml_api.h"

extern const int FILE_REGION_HANDLE;

extern const int LIBRARY_REGION_HANDLE;

extern const int VIRTUAL_REGION_HANDLE;


typedef struct _ContiguousBounds
{
    int count;
}
ContiguousBounds;


typedef struct _EnsembleDomain
{
    int componentDomain;

    DomainBoundsType boundsType;
    union
    {
        ContiguousBounds contiguous;
    }
    bounds;
}
EnsembleDomain;


typedef struct _ContinuousDomain
{
    int componentDomain;
}
ContinuousDomain;


typedef struct _MeshDomain
{
    int xiDomain;
    int elementDomain;
    
    IntTable *shapes;
    IntTable *connectivity;
}
MeshDomain;


typedef struct _ContinuousImport
{
    int remoteEvaluator;
    int valueDomain;

    IntTable *aliases;
}
ContinuousImport;


typedef struct _ContinuousPiecewise
{
    int valueDomain;
    int indexDomain;
    
    IntTable *aliases;
    IntTable *evaluators;
}
ContinuousPiecewise;


typedef struct _ContinuousAggregate
{
    int valueDomain;

    IntTable *aliases;
    IntTable *evaluators;
}
ContinuousAggregate;


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
    DataFileType fileType;
}
FileDataSource;


typedef struct _SemidenseData
{
    IntStack *sparseIndexes;
    IntStack *denseIndexes;
    
    const int *swizzle;
    int swizzleCount;

    DataLocationType locationType;
    union
    {
        StringDataSource stringData;
        FileDataSource fileData;
    }
    dataLocation;
}
SemidenseData;


typedef struct _Variable
{
    int valueDomain;

    IntStack *parameters;
}
Variable;



typedef struct _Parameters
{
    int valueDomain;

    DataDescriptionType descriptionType;
    union
    {
        SemidenseData *semidense;
    }
    dataDescription;
}
Parameters;


typedef struct _FieldmlObject
{
    FieldmlHandleType type;
    StringTable *markup;
    char *name;
    int regionHandle; // One day this will be meaningful. For now, 0 = library, 1 = not library.
    union
    {
        EnsembleDomain *ensembleDomain;
        ContinuousDomain *continuousDomain;
        MeshDomain *meshDomain;
        ContinuousImport *continuousImport;
        Parameters *parameters;
        Variable* variable;
        ContinuousPiecewise *piecewise;
        ContinuousAggregate *aggregate;
    }
    object;
}
FieldmlObject;


typedef struct _FieldmlRegion
{
    int lastError;
    
    const char *name;
    
    SimpleList *errors;
    
    SimpleList *objects;
}
FieldmlRegion;


void logError( FieldmlRegion *region, const char *error, const char *name1, const char *name2 );

int setErrorX( const char *file, const int line, FieldmlRegion *region, int error );

int getError( FieldmlRegion *region );

FmlObjectHandle addFieldmlObject( FieldmlRegion *region, FieldmlObject *object );

FieldmlRegion *createFieldmlRegion();

void destroyFieldmlRegion( FieldmlRegion *region );

FieldmlObject *createFieldmlObject( const char *name, FieldmlHandleType type, int regionHandle );

FieldmlObject *createEnsembleDomain( const char * name, int region, FmlObjectHandle componentDomain );

FieldmlObject *createContinuousDomain( const char * name, int region, FmlObjectHandle componentDomain );

FieldmlObject *createMeshDomain( const char *name, int region, FmlObjectHandle xiDomain, FmlObjectHandle elementDomain );

FieldmlObject *createContinuousImport( const char *name, int region, FmlObjectHandle evaluator, FmlObjectHandle valueDomain );

FieldmlObject *createEnsembleVariable( const char *name, int region, FmlObjectHandle valueDomain );

FieldmlObject *createContinuousVariable( const char *name, int region, FmlObjectHandle valueDomain );

FieldmlObject *createEnsembleParameters( const char *name, int region, FmlObjectHandle valueDomain );

FieldmlObject *createContinuousParameters( const char *name, int region, FmlObjectHandle valueDomain );

FieldmlObject *createContinuousPiecewise( const char *name, int region, FmlObjectHandle indexDomain, FmlObjectHandle valueDomain );

FieldmlObject *createContinuousAggregate( const char *name, int region, FmlObjectHandle valueDomain );

SemidenseData *createSemidenseData();

#define setError( x, y ) setErrorX( __FILE__, __LINE__, x, y )

#endif //H_FIELDML_STRUCTS
