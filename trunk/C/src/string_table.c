#include <stdlib.h>

#include <string.h>

#include "string_table.h"

static const int CAPACITY_INCREMENT = 32;

struct _StringTable
{
    int entries;
    int capacity;

    char **names;
    char **data;
};


StringTable *createStringTable()
{
    StringTable *table = calloc( 1, sizeof( StringTable ) );
    table->entries = 0;
    table->capacity = CAPACITY_INCREMENT;
    table->names = calloc( CAPACITY_INCREMENT, sizeof( char * ) );
    table->data = calloc( CAPACITY_INCREMENT, sizeof( void * ) );

    return table;
}


static int getStringTableEntryIndex( StringTable *table, const char *name )
{
    int i;
    for( i = 0; i < table->entries; i++ )
    {
        if( strcmp( table->names[i], name ) == 0 )
        {
            return i;
        }
    }

    return -1;
}


void setStringTableEntry( StringTable *table, const char *name, void *data, TABLE_DATA_DISCARD discard )
{
    int index;
    void *oldData;

    index = getStringTableEntryIndex( table, name );
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
        char **newNames = calloc( table->capacity + CAPACITY_INCREMENT, sizeof( char * ) );
        char **newData = calloc( table->capacity + CAPACITY_INCREMENT, sizeof( void * ) );

        char **oldNames = table->names;
        char **oldData = table->data;

        memcpy( newNames, oldNames, table->capacity * sizeof( char * ) );
        memcpy( newData, oldData, table->capacity * sizeof( void * ) );

        table->capacity += CAPACITY_INCREMENT;
        table->names = newNames;
        table->data = newData;

        free( oldNames );
        free( oldData );
    }

    table->names[table->entries] = strdup( name );
    table->data[table->entries] = data;
    table->entries++;
}


void *getStringTableEntry( StringTable *table, const char *name )
{
    int index = getStringTableEntryIndex( table, name );

    if( index < 0 )
    {
        return NULL;
    }

    return table->data[index];
}


void destroyStringTable( StringTable *table, TABLE_DATA_DISCARD discard )
{
    int i;
    
    for( i = 0; i < table->entries; i++ )
    {
        if( table->names[i] != NULL )
        {
            free( table->names[i] );
        }
        if( discard != NULL )
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


int getStringTableCount( StringTable *table )
{
    return table->entries;
}


char *getStringTableEntryName( StringTable *table, int index )
{
    if( ( index < 0 ) || ( index >= table->entries ) )
    {
        return NULL;
    }
    return table->names[index];
}


void *getStringTableEntryData( StringTable *table, int index )
{
    if( ( index < 0 ) || ( index >= table->entries ) )
    {
        return NULL;
    }
    return table->data[index];
}
