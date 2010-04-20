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
