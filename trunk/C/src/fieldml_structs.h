#ifndef H_FIELDML_STRUCTS
#define H_FIELDML_STRUCTS

#include "simple_list.h"
#include "string_table.h"
#include "int_table.h"
#include "fieldml_api.h"


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


typedef struct _ContinuousDereference
{
    int valueDomain;
    int valueIndexes;
    int valueSource;
}
ContinuousDereference;


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
    SimpleList *sparseIndexes;
    SimpleList *denseIndexes;
    
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

    SimpleList *parameters;
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
        ContinuousDereference *dereference;
    }
    object;
}
FieldmlObject;


typedef struct _FieldmlParse
{
    SimpleList *errors;
    
    SimpleList *objects;
}
FieldmlParse;


#endif //H_FIELDML_STRUCTS
