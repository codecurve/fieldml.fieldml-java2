#ifndef H_STRING_TABLE
#define H_STRING_TABLE

/*
    String Table

    String Table is a very simple string-to-data map.

    NOTE: The discard function is called on all removed entries. Duplicate
    entries will therefore result in duplicate discard calls. As the discard
    function is typically a destructor/deallocator, this can result in the
    code attempting to free stale pointers. It is up to the user to make
    sure this doesn't happen.
*/

/*
    ADT declaration
*/
typedef struct _StringTable StringTable;

/*
    Function-pointer type declaration for cleanup functions.
    NOTE: Intentionally compatible with stdlib:free.
*/
#ifndef DISCARD_FN_DEF
typedef void(*TABLE_DATA_DISCARD)( void * );
#define DISCARD_FN_DEF
#endif

/*
    Create a new String Table.
*/
StringTable *createStringTable();

/*
    Sets an entry in the given table. If an entry with the same name already exists,
    it is passed to the discard function. An internal copy of the name is made, so
    the user is free to deallocate/reused the name parameter.

    NOTE: Both name and data can be NULL.
*/
void setStringTableEntry( StringTable *table, const char *name, void *data, TABLE_DATA_DISCARD discard );

/*
    Returns the entry with the given name, or NULL if there is no such entry.

    NOTE: NULL may also be returned if the data is actually NULL.
*/
void *getStringTableEntry( StringTable *table, const char *name );

/*
    Get the number of entries in the table.

    NOTE: NULL entries are counted.
*/
int getStringTableCount( StringTable *table );

/*
    Get the name of the entry with the given index.

    NOTE: NULL if index is invalid. May be NULL otherwise.
*/
char *getStringTableEntryName( StringTable *table, int index );

/*
    Get the data of the entry with the given index.

    NOTE: NULL if index is invalid. May also be NULL otherwise.
*/
void *getStringTableEntryData( StringTable *table, int index );

/*
    Deallocate the table's data. Each entry's data is passed to the discard function.
*/
void destroyStringTable( StringTable *table, TABLE_DATA_DISCARD discard );


void setStringTableDefault( StringTable *table, void *value, TABLE_DATA_DISCARD discard );


void *getStringTableDefault( StringTable *table );


#endif // H_STRING_TABLE
