#include <stdlib.h>
#include <string.h>

#include "int_table.h"

static const int CAPACITY_INCREMENT = 32;

struct _IntTable
{
    void *defaultValue;
    
    int entries;
    int capacity;

    int *names;
    void **data;
};


IntTable *createIntTable()
{
    IntTable *table = calloc( 1, sizeof( IntTable ) );
    table->entries = 0;
    table->capacity = CAPACITY_INCREMENT;
    table->names = calloc( CAPACITY_INCREMENT, sizeof( int ) );
    table->data = calloc( CAPACITY_INCREMENT, sizeof( void * ) );

    return table;
}


static int getIntTableEntryIndex( IntTable *table, int name )
{
    int i;
    for( i = 0; i < table->entries; i++ )
    {
        if( table->names[i] == name )
        {
            return i;
        }
    }

    return -1;
}


void setIntTableIntEntry( IntTable *table, int name, int data )
{
    int *entry;
    
    entry = malloc( sizeof( int ) );
    *entry = data;
    
    setIntTableEntry( table, name, entry, free );
}


void setIntTableEntry( IntTable *table, int name, void *data, TABLE_DATA_DISCARD discard )
{
    int index;
    void *oldData;

    index = getIntTableEntryIndex( table, name );
    if( index >= 0 )
    {
        if( data == table->data[index] )
        {
            return;
        }

        oldData = table->data[index];
        table->data[index] = data;

        if( ( discard != NULL ) && ( oldData != NULL ) )
        {
            discard( oldData );
        }

        return;
    }

    if( table->entries == table->capacity )
    {
        int *newNames = calloc( table->capacity + CAPACITY_INCREMENT, sizeof( int ) );
        void **newData = calloc( table->capacity + CAPACITY_INCREMENT, sizeof( void * ) );

        int *oldNames = table->names;
        void **oldData = table->data;

        memcpy( newNames, oldNames, table->capacity * sizeof( int ) );
        memcpy( newData, oldData, table->capacity * sizeof( void * ) );

        table->capacity += CAPACITY_INCREMENT;
        table->names = newNames;
        table->data = newData;

        free( oldNames );
        free( oldData );
    }

    table->names[table->entries] = name;
    table->data[table->entries] = data;
    table->entries++;
}


void *getIntTableEntry( IntTable *table, int name )
{
    int index = getIntTableEntryIndex( table, name );

    if( index < 0 )
    {
        return table->defaultValue;
    }

    return table->data[index];
}


int getIntTableIntEntry( IntTable *table, int name )
{
    int *entry;
    
    entry = getIntTableEntry( table, name );
    if( entry == NULL )
    {
        entry = table->defaultValue;
    }
    if( entry != NULL )
    {
        return *entry;
    }
    else
    {
        return -1;
    }
}


void destroyIntTable( IntTable *table, TABLE_DATA_DISCARD discard )
{
    int i;
    
    if( discard != NULL )
    {
        for( i = 0; i < table->entries; i++ )
        {
            if( table->data[i] != NULL )
            {
                discard( table->data[i] );
            }
        }
    }
    free( table->names );
    free( table->data );
    free( table );
}


int getIntTableCount( IntTable *table )
{
    return table->entries;
}


int getIntTableEntryName( IntTable *table, int index )
{
    if( ( index < 0 ) || ( index >= table->entries ) )
    {
        return -1;
    }
    return table->names[index];
}


void *getIntTableEntryData( IntTable *table, int index )
{
    if( ( index < 0 ) || ( index >= table->entries ) )
    {
        return NULL;
    }
    return table->data[index];
}


int getIntTableEntryIntData( IntTable *table, int index )
{
    int *data;
    
    data = getIntTableEntryData( table, index );
    if( data == NULL )
    {
        return -1;
    }
    else
    {
        return *data;
    }
}


void setIntTableDefault( IntTable *table, void *value, TABLE_DATA_DISCARD discard )
{
    if( table->defaultValue != NULL )
    {
        discard( table->defaultValue );
    }
    
    table->defaultValue = value;
}


void setIntTableDefaultInt( IntTable *table, int value )
{
    int *newValue;
    
    newValue = malloc( sizeof( int ) );
    *newValue = value;
    
    setIntTableDefault( table, newValue, free );
}


void *getIntTableDefault( IntTable *table )
{
    return table->defaultValue;
}


int getIntTableDefaultInt( IntTable *table )
{
    if( table->defaultValue == NULL )
    {
        return -1;
    }
    
    return *((int*)table->defaultValue);
}
