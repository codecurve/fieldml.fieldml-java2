#ifndef H_FIELDML_STRUCTS
#define H_FIELDML_STRUCTS

#include "simple_list.h"
#include "string_table.h"
#include "int_table.h"
#include "fieldml_api.h"


typedef enum _FieldmlHandleType
{
	FHT_UNKNOWN,
	
	FHT_ENSEMBLE_DOMAIN,
	FHT_CONTINUOUS_DOMAIN,
	FHT_MESH_DOMAIN,
	FHT_CONTINUOUS_IMPORT,
	FHT_ENSEMBLE_PARAMETERS,
	FHT_CONTINUOUS_PARAMETERS,
	FHT_CONTINUOUS_PIECEWISE,
	FHT_CONTINUOUS_AGGREGATE,
	FHT_CONTINUOUS_DEREFERENCE,
	FHT_CONTINUOUS_VARIABLE,
	FHT_ENSEMBLE_VARIABLE,
	FHT_IMPORTED_ENSEMBLE,
	FHT_IMPORTED_CONTINUOUS,

	//These four are stand-in types used to allow forward-declaration during parsing.
	FHT_UNKNOWN_ENSEMBLE_DOMAIN,
	FHT_UNKNOWN_CONTINUOUS_DOMAIN,
	FHT_UNKNOWN_ENSEMBLE_SOURCE,
	FHT_UNKNOWN_CONTINUOUS_SOURCE,
	
}
FieldmlHandleType;

typedef struct _ContiguousBounds
{
    int count;
}
ContiguousBounds;


typedef struct _EnsembleDomain
{
	int handle;
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
	int handle;
	int componentDomain;
}
ContinuousDomain;


typedef struct _MeshDomain
{
	int handle;
	int xiEnsemble;
	
	IntTable *shapes;
	StringTable *connectivity;
}
MeshDomain;


typedef struct _ContinuousImport
{
	int handle;
    char *remoteName;
    int valueDomain;

    IntTable *aliases;
}
ContinuousImport;


typedef struct _ContinuousPiecewise
{
	int valueDomain;
	int indexDomain;
    
    IntTable *evaluators;
}
ContinuousPiecewise;


typedef struct _ContinuousAggregate
{
	int valueDomain;
	
	StringTable *markup;
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
    int isText;
}
FileDataSource;


typedef struct _SemidenseData
{
    SimpleList *sparseIndexes;
    SimpleList *denseIndexes;
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

    DataLocationType locationType;
    union
    {
        StringDataSource stringData;
        FileDataSource fileData;
    }
    dataLocation;
}
Parameters;


typedef struct _FieldmlObject
{
	FieldmlHandleType type;
	char *name;
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
    SimpleList *objects;
}
FieldmlParse;


#endif //H_FIELDML_STRUCTS
