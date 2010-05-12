#ifndef H_FIELDML_PARSE
#define H_FIELDML_PARSE

#include "string_table.h"
#include "fieldml_structs.h"
#include "fieldml_api.h"

extern const int FILE_REGION_HANDLE;

extern const int LIBRARY_REGION_HANDLE;

extern const int VIRTUAL_REGION_HANDLE;


typedef struct _FieldmlParse FieldmlParse;

typedef struct _FieldmlObject FieldmlObject;

void addError( FieldmlParse *parse, const char *error, const char *name1, const char *name2 );

FmlObjectHandle addFieldmlObject( FieldmlParse *parse, FieldmlObject *object );

FmlObjectHandle getOrCreateObjectHandle( FieldmlParse *parse, const char *name, FieldmlHandleType type );

FieldmlParse *createFieldmlParse();

void destroyFieldmlParse( FieldmlParse *parse );


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

FieldmlObject *createContinuousDereference( const char *name, int region, FmlObjectHandle valueIndexes, FmlObjectHandle valueSource, FmlObjectHandle valueDomain );

SemidenseData *createSemidenseData();

void finalizeFieldmlParse( FieldmlParse *parse );

#endif // H_FIELDML_PARSE
