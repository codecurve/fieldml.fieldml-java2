#ifndef H_FIELDML_PARSE
#define H_FIELDML_PARSE

#include "string_table.h"

typedef struct _SaxAttributes SaxAttributes;

typedef struct _FieldmlContext FieldmlContext;

typedef struct _FieldmlParse FieldmlParse;

FieldmlContext *createFieldmlContext( FieldmlParse *parse );

void destroyFieldmlContext( FieldmlContext *context );


void destroyFieldmlParse( FieldmlParse *parse );

FieldmlParse *createFieldmlParse();

void destroyFieldmlParse( FieldmlParse *parse );


void startEnsembleDomain( FieldmlContext *context, SaxAttributes *attributes );

void startContiguousBounds( FieldmlContext *context, SaxAttributes *attributes );

void endEnsembleDomain( FieldmlContext *context );


void startContinuousDomain( FieldmlContext *context, SaxAttributes *attributes );

void endContinuousDomain( FieldmlContext *context );


void startMeshDomain( FieldmlContext *context, SaxAttributes *attributes );

void onMeshContiguousBounds( FieldmlContext *context, SaxAttributes *attributes );

void onMeshShape( FieldmlContext *context, SaxAttributes *attributes );

void onMeshConnectivity( FieldmlContext *context, SaxAttributes *attributes );

void endMeshDomain( FieldmlContext *context );


void startContinuousImport( FieldmlContext *context, SaxAttributes *attributes );

void onContinuousImportAlias( FieldmlContext *context, SaxAttributes *attributes );

void onEnsembleImportAlias( FieldmlContext *context, SaxAttributes *attributes );

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


void startContinuousDereference( FieldmlContext *context, SaxAttributes *attributes );

void endContinuousDereference( FieldmlContext *context );


void startContinuousVariable( FieldmlContext *context, SaxAttributes *attributes );

void startEnsembleVariable( FieldmlContext *context, SaxAttributes *attributes );

void endVariable( FieldmlContext *context );


void onMarkupEntry( FieldmlContext *context, SaxAttributes *attributes );


void startSemidenseData( FieldmlContext *context, SaxAttributes *attributes );

void onSemidenseDenseIndex( FieldmlContext *context, SaxAttributes *attributes );

void onSemidenseSparseIndex( FieldmlContext *context, SaxAttributes *attributes );

void endSemidenseData( FieldmlContext *context );

void startInlineData( FieldmlContext *context, SaxAttributes *attributes );

void onInlineData( FieldmlContext *context, const char *const characters, const int length );

void onFileData( FieldmlContext *context, SaxAttributes *attributes );


void finalizeFieldmlParse( FieldmlParse *parse );

#endif // H_FIELDML_PARSE
