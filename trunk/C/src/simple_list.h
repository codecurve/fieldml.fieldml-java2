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

#ifndef H_SIMPLE_LIST
#define H_SIMPLE_LIST

/*
    Simple List

    Simple List is a very simple add-only list of objects.

    NOTE: The discard function is called on all removed entries. Duplicate
    entries will therefore result in duplicate discard calls. As the discard
    function is typically a destructor/deallocator, this can result in the
    code attempting to free stale pointers. It is up to the user to make
    sure this doesn't happen.
*/

/*
    ADT declaration
*/
typedef struct _SimpleList SimpleList;

/*
    Function-pointer type declaration for cleanup functions.
    NOTE: Intentionally compatible with stdlib:free.
*/
typedef void(*LIST_DATA_DISCARD)( void * );


/*
    Create a new Simple List
*/
SimpleList *createSimpleList();

/*
    Add an entry to the given list.

    NOTE: data can be NULL.
*/
int addSimpleListEntry( SimpleList *list, void *data );

/*
    Get the data with the given index.

    NOTE: NULL if index is invalid. May also be NULL otherwise.
*/
void *getSimpleListEntry( SimpleList *list, int index );

/*
    Get the number of entries in the list.

    NOTE: NULL entries are counted.
*/
int getSimpleListCount( SimpleList *list );

/*
    Deallocate the list's data. Each data entry is passed to the discard function.
*/
void destroySimpleList( SimpleList *list, LIST_DATA_DISCARD discard );


#endif // H_SIMPLE_LIST
