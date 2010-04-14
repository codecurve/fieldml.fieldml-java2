#ifndef H_FIELDMLSAX
#define H_FIELDMLSAX

#include "int_stack.h"

typedef struct _SaxAttribute
{
    char *attribute;
    char *prefix;
    char *URI;
    char *value;
}
SaxAttribute;


typedef struct _SaxAttributes
{
    SaxAttribute *attributes;
    int count;
}
SaxAttributes;

char * getAttribute( SaxAttributes *saxAttributes, char *attribute );

typedef enum _SaxState
{
    FML_ROOT,
    FML_FIELDML,
    FML_REGION,

    FML_ENSEMBLE_DOMAIN,
    FML_ENSEMBLE_DOMAIN_BOUNDS,
    
    FML_CONTINUOUS_DOMAIN,

    FML_CONTINUOUS_IMPORT,
    FML_CONTINUOUS_ALIASES,
    FML_ENSEMBLE_ALIASES,

    FML_ENSEMBLE_PARAMETERS,
    FML_CONTINUOUS_PARAMETERS,

    FML_CONTINUOUS_VARIABLE,

    FML_ENSEMBLE_VARIABLE,

    FML_SEMI_DENSE_CONTINUOUS,
    FML_SEMI_DENSE_ENSEMBLE,
    FML_DENSE_INDEXES,
    FML_SPARSE_INDEXES,
    FML_SEMIDENSE_INLINE_DATA,
    FML_SEMIDENSE_FILE_DATA,
    
    FML_CONTINUOUS_PIECEWISE,
    FML_ELEMENT_EVALUATORS,
    
    FML_CONTINUOUS_AGGREGATE,
    FML_SOURCE_FIELDS,
}
SaxState;


typedef struct _SaxContext
{
    IntStack *state;
    xmlSAXLocator *locator;

    //We should really have a proper object stack, but so far it only gets two-deep.
    void *currentObject;
    void *currentObject2;

    FieldmlParse *parse;
}
SaxContext;


#endif // H_FIELDMLSAX
