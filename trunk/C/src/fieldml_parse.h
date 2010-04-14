#ifndef H_FIELDML_PARSE
#define H_FIELDML_PARSE

typedef struct _SaxContext SaxContext;

typedef struct _SaxAttributes SaxAttributes;

typedef struct _FieldmlParse FieldmlParse;

FieldmlParse *createFieldmlParse();

void destroyFieldmlParse( FieldmlParse *parse );

void dumpFieldmlParse( FieldmlParse *parse );


void startEnsembleDomain( SaxContext *context, SaxAttributes *attributes );

void endEnsembleDomain( SaxContext *context );


void startContinuousDomain( SaxContext *context, SaxAttributes *attributes );

void endContinuousDomain( SaxContext *context );


void startContiguousBounds( SaxContext *context, SaxAttributes *attributes );


void startContinuousImport( SaxContext *context, SaxAttributes *attributes );

void continuousImportAlias( SaxContext *context, SaxAttributes *attributes );

void endContinuousImport( SaxContext *context );


void startEnsembleParameters( SaxContext *context, SaxAttributes *attributes );

void endEnsembleParameters( SaxContext *context );


void startContinuousParameters( SaxContext *context, SaxAttributes *attributes );

void endContinuousParameters( SaxContext *context );


void startContinuousPiecewise( SaxContext *context, SaxAttributes *attributes );

void onContinuousPiecewiseEntry( SaxContext *context, SaxAttributes *attributes );

void endContinuousPiecewise( SaxContext *context );


void startContinuousAggregate( SaxContext *context, SaxAttributes *attributes );

void onContinuousAggregateEntry( SaxContext *context, SaxAttributes *attributes );

void endContinuousAggregate( SaxContext *context );


void startVariable( SaxContext *context, SaxAttributes *attributes );

void endVariable( SaxContext *context );


void startSemidenseData( SaxContext *context, SaxAttributes *attributes, int isEnsemble );

void semidenseIndex( SaxContext *context, SaxAttributes *attributes, int isSparse );

void semidenseStartInlineData( SaxContext *context, SaxAttributes *attributes );

void semidenseInlineData( SaxContext *context, const char *const characters, const int length );

void semidenseFileData( SaxContext *context, SaxAttributes *attributes );

void endSemidenseData( SaxContext *context, int isEnsemble );


#endif // H_FIELDML_PARSE
