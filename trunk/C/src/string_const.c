/* \file
 * $Id$
 * \author Caton Little
 * \brief 
 *
 * \section LICENSE
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is FieldML
 *
 * The Initial Developer of the Original Code is Auckland Uniservices Ltd,
 * Auckland, New Zealand. Portions created by the Initial Developer are
 * Copyright (C) 2010 the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 */
#include <stdlib.h>
#include <string.h>

#define WIN_PATH_SEP '\\'
#define PATH_SEP '/'

#ifdef WIN32
#define DEFAULT_SEP WIN_PATH_SEP
#else
#define DEFAULT_SEP PATH_SEP
#endif

const char * const FML_VERSION_STRING               = "0.2_alpha";

const char * const FIELDML_TAG                      = "fieldml";

const char * const REGION_TAG                       = "Region";

const char * const ENSEMBLE_DOMAIN_TAG              = "EnsembleDomain";
const char * const BOUNDS_TAG                       = "bounds";
const char * const CONTIGUOUS_ENSEMBLE_BOUNDS_TAG   = "ContiguousEnsembleBounds";
const char * const ARBITRARY_ENSEMBLE_BOUNDS_TAG    = "ArbitraryEnsembleBounds";

const char * const CONTINUOUS_DOMAIN_TAG            = "ContinuousDomain";

const char * const MESH_DOMAIN_TAG                  = "MeshDomain";
const char * const MESH_SHAPES_TAG                  = "shapes";
const char * const MESH_CONNECTIVITY_TAG            = "pointConnectivity";

const char * const CONTINUOUS_REFERENCE_TAG         = "ContinuousReferenceEvaluator";
const char * const ALIASES_TAG                      = "aliases";

const char * const CONTINUOUS_VARIABLE_TAG          = "ContinuousVariableEvaluator";

const char * const ENSEMBLE_VARIABLE_TAG            = "EnsembleVariableEvaluator";

const char * const ENSEMBLE_PARAMETERS_TAG          = "EnsembleParameters";

const char * const CONTINUOUS_PARAMETERS_TAG        = "ContinuousParameters";
const char * const SEMI_DENSE_DATA_TAG              = "SemidenseData";
const char * const SPARSE_INDEXES_TAG               = "sparseIndexes";
const char * const DENSE_INDEXES_TAG                = "denseIndexes";

const char * const CONTINUOUS_PIECEWISE_TAG         = "ContinuousPiecewiseEvaluator";
const char * const ELEMENT_EVALUATORS_TAG           = "elementEvaluators";

const char * const CONTINUOUS_AGGREGATE_TAG         = "ContinuousAggregateEvaluator";
const char * const SOURCE_FIELDS_TAG                = "sourceFields";

const char * const MARKUP_TAG                       = "markup";

const char * const MAP_ENTRY_TAG                    = "SimpleMapEntry";
const char * const MAP_DEFAULT                      = "SimpleMapDefault";

const char * const ENTRY_TAG                        = "entry";

const char * const DATA_LOCATION_TAG                = "dataLocation";

const char * const INLINE_DATA_TAG                  = "inlineData";
const char * const FILE_DATA_TAG                    = "fileData";
const char * const SWIZZLE_TAG                      = "swizzle";




const char * const VERSION_ATTRIB                   = "version";

const char * const NAME_ATTRIB                      = "name";

const char * const COMPONENT_DOMAIN_ATTRIB          = "componentDomain";

const char * const IS_COMPONENT_DOMAIN_ATTRIB       = "isComponentDomain";

const char * const VALUE_DOMAIN_ATTRIB              = "valueDomain";

const char * const KEY_ATTRIB                       = "key";
const char * const VALUE_ATTRIB                     = "value";
const char * const DEFAULT_ATTRIB                   = "default";

const char *const VALUE_COUNT_ATTRIB                = "valueCount";

const char *const XI_COMPONENT_DOMAIN_ATTRIB        = "xiComponentDomain";

const char *const VALUE_SOURCE_ATTRIB               = "valueSource";
const char *const VALUE_INDEXES_ATTRIB              = "valueIndexes";

const char *const EVALUATOR_ATTRIB                  = "evaluator";

const char *const INDEX_DOMAIN_ATTRIB               = "indexDomain";

const char *const FILE_ATTRIB                       = "file";
const char *const TYPE_ATTRIB                       = "type";
const char *const OFFSET_ATTRIB                     = "offset";

const char * const STRING_TYPE_TEXT                 = "text";
const char * const STRING_TYPE_LINES                = "lines";

const char * const STRING_TRUE                      = "true";

// strndup not available on all platforms.
char *strdupN( const char *str, unsigned int n )
{
    char *dup;
    
    if( str == NULL )
    {
        return NULL;
    }

    if( strlen( str ) < n )
    {
        return strdup( str );
    }

    dup = malloc( n + 1 );
    memcpy( dup, str, n );
    dup[n] = 0;

    return dup;
}


char *strdupS( const char *str )
{
    if( str == NULL )
    {
        return NULL;
    }
    
    return strdup( str );
}


char *strdupDir( const char *filename )
{
    const char *lastSep = NULL;
    const char *c;
    
    for( c = filename; *c != 0; c++ )
    {
        if( *c == PATH_SEP )
        {
            lastSep = c;
        }
#ifdef WIN32
        else if( *c == WIN_PATH_SEP )
        {
            lastSep = c;
        }
#endif
    }
    
    if( lastSep != NULL )
    {
        return strdupN( filename, lastSep - filename );
    }
    else
    {
        return strdup( "" );
    }
}


char *makeFilename( const char *dir, const char *file )
{
    int len = 0;
    char *filename;

    if( file == NULL )
    {
        return NULL;
    }
    
    if( dir == NULL )
    {
        //HACK
        dir = "";
    }
    
    //Directory
    len += strlen( dir );
    //Path separator
    len++;
    //File
    len += strlen( file );
    //NUL terminator
    len++;
    
    filename = malloc( len );
    len = 0;

    if( strlen( dir ) > 0 )
    {
        memcpy( filename, dir, strlen( dir ) );
        len += strlen( dir );
        
        filename[len++] = DEFAULT_SEP;
    }

    memcpy( filename + len, file, strlen( file ) );
    len += strlen( file );
    
    filename[len] = 0;
    
    return filename;
}
