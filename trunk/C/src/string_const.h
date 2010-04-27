#ifndef H_STRING_CONST
#define H_STRING_CONST

const char* const FIELDML_TAG                       = "fieldml";

const char* const REGION_TAG                        = "Region";

const char* const ENSEMBLE_DOMAIN_TAG               = "EnsembleDomain";
const char* const CONTIGUOUS_ENSEMBLE_BOUNDS_TAG    = "ContiguousEnsembleBounds";
const char* const ARBITRARY_ENSEMBLE_BOUNDS_TAG     = "ArbitraryEnsembleBounds";

const char* const CONTINUOUS_DOMAIN_TAG             = "ContinuousDomain";

const char* const MESH_DOMAIN_TAG                   = "MeshDomain";
const char* const MESH_SHAPES_TAG                   = "shapes";
const char* const MESH_CONNECTIVITY_TAG             = "pointConnectivity";

const char* const IMPORTED_CONTINUOUS_TAG           = "ImportedContinuousEvaluator";
const char* const ENSEMBLE_ALIAS_TAG                = "ensembleAliases";
const char* const CONTINUOUS_ALIAS_TAG              = "continuousAliases";

const char* const CONTINUOUS_VARIABLE_TAG           = "ContinuousVariableEvaluator";

const char* const ENSEMBLE_VARIABLE_TAG             = "EnsembleVariableEvaluator";

const char* const ENSEMBLE_PARAMETERS_TAG           = "EnsembleParameters";

const char* const CONTINUOUS_PARAMETERS_TAG         = "ContinuousParameters";
const char* const SEMI_DENSE_DATA_TAG               = "SemidenseData";
const char* const SPARSE_INDEXES_TAG                = "sparseIndexes";
const char* const DENSE_INDEXES_TAG                 = "denseIndexes";

const char* const CONTINUOUS_PIECEWISE_TAG          = "ContinuousPiecewiseEvaluator";
const char* const ELEMENT_EVALUATORS_TAG            = "elementEvaluators";

const char* const CONTINUOUS_AGGREGATE_TAG          = "ContinuousAggregateEvaluator";
const char* const SOURCE_FIELDS_TAG                 = "sourceFields";

const char* const CONTINUOUS_DEREFERENCE_TAG        = "ContinuousDereferenceEvaluator";

const char *const MARKUP_TAG                        = "markup";

const char* const SIMPLE_MAP_ENTRY_TAG              = "SimpleMapEntry";

const char* const ENTRY_TAG                         = "entry";

const char* const INLINE_DATA_TAG                   = "inlineData";
const char* const FILE_DATA_TAG                     = "fileData";

#endif // H_STRING_CONST