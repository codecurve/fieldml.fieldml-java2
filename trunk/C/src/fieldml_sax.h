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

const char * getAttribute( SaxAttributes *saxAttributes, const char *attribute );

FieldmlParse *parseFieldmlFile( const char *filename );

#endif // H_FIELDMLSAX
