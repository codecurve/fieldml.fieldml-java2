#ifndef H_FIELDML_PARSE
#define H_FIELDML_PARSE

typedef struct _SaxAttributes SaxAttributes;

typedef struct _FieldmlContext FieldmlContext;

typedef struct _FieldmlParse FieldmlParse;

FieldmlContext *createFieldmlContext( FieldmlParse *parse );

void destroyFieldmlContext( FieldmlContext *context );


void destroyFieldmlParse( FieldmlParse *parse );

FieldmlParse *createFieldmlParse();

void destroyFieldmlParse( FieldmlParse *parse );

void dumpFieldmlParse( FieldmlParse *parse );


void startEnsembleDomain( FieldmlContext *context, SaxAttributes *attributes );

void startContiguousBounds( FieldmlContext *context, SaxAttributes *attributes );

void endEnsembleDomain( FieldmlContext *context );


void startContinuousDomain( FieldmlContext *context, SaxAttributes *attributes );

void endContinuousDomain( FieldmlContext *context );


void startMeshDomain( FieldmlContext *context, SaxAttributes *attributes );

void onMeshShape( FieldmlContext *context, SaxAttributes *attributes );

void onMeshConnectivity( FieldmlContext *context, SaxAttributes *attributes );

void endMeshDomain( FieldmlContext *context );


void startContinuousImport( FieldmlContext *context, SaxAttributes *attributes );

void continuousImportAlias( FieldmlContext *context, SaxAttributes *attributes );

void endContinuousImport( FieldmlContext *context );


void startEnsembleParameters( FieldmlContext *context, SaxAttributes *attributes );

void endEnsembleParameters( FieldmlContext *context );


void startContinuousParameters( FieldmlContext *context, SaxAttributes *attributes );

void endContinuousParameters( FieldmlContext *context );


void startContinuousPiecewise( FieldmlContext *context, SaxAttributes *attributes );

void onContinuousPiecewiseEntry( FieldmlContext *context, SaxAttributes *attributes );

void endContinuousPiecewise( FieldmlContext *context );


void startContinuousAggregate( FieldmlContext *context, SaxAttributes *attributes );

void onContinuousAggregateEntry( FieldmlContext *context, SaxAttributes *attributes );

void endContinuousAggregate( FieldmlContext *context );


void startVariable( FieldmlContext *context, SaxAttributes *attributes );

void endVariable( FieldmlContext *context );


void onMarkupEntry( FieldmlContext *context, SaxAttributes *attributes );


void startSemidenseData( FieldmlContext *context, SaxAttributes *attributes );

void semidenseIndex( FieldmlContext *context, SaxAttributes *attributes, int isSparse );

void semidenseStartInlineData( FieldmlContext *context, SaxAttributes *attributes );

void semidenseInlineData( FieldmlContext *context, const char *const characters, const int length );

void semidenseFileData( FieldmlContext *context, SaxAttributes *attributes );

void endSemidenseData( FieldmlContext *context );


#endif // H_FIELDML_PARSE
