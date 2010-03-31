#ifndef H_FIELDMLSAX
#define H_FIELDMLSAX

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
    FML_REGION,
    FML_FIELDML,

    FML_ENSEMBLE_DOMAIN,
    FML_ENSEMBLE_DOMAIN_BOUNDS,
    
    FML_CONTINUOUS_DOMAIN,

    FML_CONTINUOUS_IMPORT,
    FML_CONTINUOUS_IMPORT_C_ALIASES,
    FML_CONTINUOUS_IMPORT_E_ALIASES,

    FML_ENSEMBLE_PARAMETERS,
    FML_SEMI_DENSE_ENSEMBLE,

    FML_CONTINUOUS_PARAMETERS,
    FML_SEMI_DENSE_CONTINUOUS,
}
SaxState;


typedef struct _SaxContext
{
    SaxState state;
    xmlSAXLocator *locator;

    void *currentObject;

    FieldmlParse *parse;
}
SaxContext;


#endif // H_FIELDMLSAX
