#include <stdlib.h>

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


void pushInt( IntStack *stack, int value )
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


int popInt( IntStack *stack )
{
    if( stack->size == 0 )
    {
        return -1;
    }

    return stack->data[--stack->size];
}


int peekInt( IntStack *stack )
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
