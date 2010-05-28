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
 * The Original Code is OpenCMISS
 *
 * The Initial Developer of the Original Code is University of Auckland,
 * Auckland, New Zealand and University of Oxford, Oxford, United
 * Kingdom. Portions created by the University of Auckland and University
 * of Oxford are Copyright (C) 2007 by the University of Auckland and
 * the University of Oxford. All Rights Reserved.
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

#include <string.h>
#include <stdlib.h>

#include "simple_list.h"

static const int CAPACITY_INCREMENT = 32;

struct _SimpleList
{
    int capacity;
    int size;

    void **data;
};


SimpleList *createSimpleList()
{
    SimpleList *list = calloc( 1, sizeof( SimpleList ) );
    list->size = 0;
    list->capacity = CAPACITY_INCREMENT;
    list->data = calloc( CAPACITY_INCREMENT, sizeof( void * ) );

    return list;
}


int addSimpleListEntry( SimpleList *list, void *entry )
{
    if( list->size >= list->capacity )
    {
        void ** newData = calloc( list->capacity + CAPACITY_INCREMENT, sizeof( void * ) );
        memcpy( newData, list->data, list->capacity * sizeof( void * ) );
        free( list->data );
        list->data = newData;
        list->capacity += CAPACITY_INCREMENT;
    }

    list->data[list->size++] = entry;
    
    return list->size - 1;
}


void *getSimpleListEntry( SimpleList *list, int index )
{
    if( ( index < 0 ) || ( index >= list->size ) )
    {
        return NULL;
    }

    return list->data[index];
}


int getSimpleListCount( SimpleList *list )
{
    return list->size;
}


void destroySimpleList( SimpleList *list, LIST_DATA_DISCARD discard )
{
    int i;

    if( discard != NULL )
    {
        for( i = 0; i < list->size; i++ )
        {
            if( list->data[i] != NULL )
            {
                discard( list->data[i] );
            }
        }
    }

    free( list->data );
    free( list );
}
