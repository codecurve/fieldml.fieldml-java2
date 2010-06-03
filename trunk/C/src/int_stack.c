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

#include "int_stack.h"

static const int CAPACITY_INCREMENT = 32;

struct _IntStack
{
    int size;
    int capacity;
    int *data;
};

IntStack *createIntStack()
{
    IntStack *stack = calloc( 1, sizeof( IntStack ) );
    stack->size = 0;
    stack->capacity = CAPACITY_INCREMENT;
    stack->data = calloc( stack->capacity, sizeof( int ) );

    return stack;
}


void intStackPush( IntStack *stack, int value )
{
    if( stack->size == stack->capacity )
    {
        int *newData = calloc( stack->capacity + CAPACITY_INCREMENT, sizeof( int ) );
        memcpy( newData, stack->data, stack->capacity * sizeof( int ) );
        free( stack->data );
        stack->data = newData;
        stack->capacity += CAPACITY_INCREMENT;
    }

    stack->data[stack->size++] = value;
}


int intStackPop( IntStack *stack )
{
    if( stack->size == 0 )
    {
        return -1;
    }

    return stack->data[--stack->size];
}


int intStackPeek( IntStack *stack )
{
    if( stack->size == 0 )
    {
        return -1;
    }

    return stack->data[stack->size - 1];
}


void destroyIntStack( IntStack *stack )
{
    free( stack->data );
    free( stack );
}


int intStackGetCount( IntStack *stack )
{
    return stack->size;
}


int intStackGet( IntStack *stack, int index )
{
    if( ( index < 0 ) || ( index >= stack->size ) )
    {
        return -1;
    }
    
    return stack->data[index];
}
