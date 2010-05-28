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

#ifndef H_INT_TABLE
#define H_INT_TABLE

/*
    Int Table

    Int Table is a very simple int-to-data map.

    NOTE: The discard function is called on all removed entries. Duplicate
    entries will therefore result in duplicate discard calls. As the discard
    function is typically a destructor/deallocator, this can result in the
    code attempting to free stale pointers. It is up to the user to make
    sure this doesn't happen.
*/

/*
    ADT declaration
*/
typedef struct _IntTable IntTable;

/*
    Function-pointer type declaration for cleanup functions.
    NOTE: Intentionally compatible with stdlib:free.
*/
#ifndef DISCARD_FN_DEF
typedef void(*TABLE_DATA_DISCARD)( void * );
#define DISCARD_FN_DEF
#endif

/*
    Create a new Int Table.
*/
IntTable *createIntTable();

/*
    Sets an entry in the given table. If an entry with the same name already exists,
    it is passed to the discard function. An internal copy of the name is made, so
    the user is free to deallocate/reused the name parameter.

    NOTE: Both name and data can be NULL.
*/
void setIntTableEntry( IntTable *table, int name, void *data, TABLE_DATA_DISCARD discard );
void setIntTableIntEntry( IntTable *table, int name, int data );

/*
    Returns the entry with the given name, or NULL if there is no such entry.

    NOTE: NULL may also be returned if the data is actually NULL.
*/
void *getIntTableEntry( IntTable *table, int name );
int getIntTableIntEntry( IntTable *table, int name );

/*
    Get the number of entries in the table.

    NOTE: NULL entries are counted.
*/
int getIntTableCount( IntTable *table );

/*
    Get the name of the entry with the given index.

    NOTE: NULL if index is invalid. May be NULL otherwise.
*/
int getIntTableEntryName( IntTable *table, int index );

/*
    Get the data of the entry with the given index.

    NOTE: NULL if index is invalid. May also be NULL otherwise.
*/
void *getIntTableEntryData( IntTable *table, int index );
int getIntTableEntryIntData( IntTable *table, int index );

/*
    Deallocate the table's data. Each entry's data is passed to the discard function.
*/
void destroyIntTable( IntTable *table, TABLE_DATA_DISCARD discard );


void setIntTableDefault( IntTable *table, void *value, TABLE_DATA_DISCARD discard );
void setIntTableDefaultInt( IntTable *table, int value );


void *getIntTableDefault( IntTable *table );
int getIntTableDefaultInt( IntTable *table );

#endif // H_INT_TABLE
