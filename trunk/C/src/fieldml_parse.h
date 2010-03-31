#ifndef H_FIELDML_PARSE
#define H_FIELDML_PARSE

typedef struct _SaxContext SaxContext;

typedef struct _SaxAttributes SaxAttributes;

typedef struct _FieldmlParse FieldmlParse;

FieldmlParse *createFieldmlParse();

void destroyFieldmlParse( FieldmlParse *parse );

void dumpFieldmlParse( FieldmlParse *parse );


void startEnsembleDomain( SaxContext *context, SaxAttributes *saxAttributes );

void endEnsembleDomain( SaxContext *context );


void startContinuousDomain( SaxContext *context, SaxAttributes *saxAttributes );

void endContinuousDomain( SaxContext *context );


void startContiguousBounds( SaxContext *context, SaxAttributes *saxAttributes );


void startContinuousImport( SaxContext *context, SaxAttributes *saxAttributes );

void continuousImportAlias( SaxContext *context, SaxAttributes *saxAttributes );

void endContinuousImport( SaxContext *context );


void startEnsembleParameters( SaxContext *context, SaxAttributes *saxAttributes );

void endEnsembleParameters( SaxContext *context );


void startContinuousParameters( SaxContext *context, SaxAttributes *saxAttributes );

void endContinuousParameters( SaxContext *context );


#endif // H_FIELDML_PARSE