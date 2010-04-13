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

/*
    Returns the entry with the given name, or NULL if there is no such entry.

    NOTE: NULL may also be returned if the data is actually NULL.
*/
void *getIntTableEntry( IntTable *table, int name );

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

/*
    Deallocate the table's data. Each entry's data is passed to the discard function.
*/
void destroyIntTable( IntTable *table, TABLE_DATA_DISCARD discard );

#endif // H_INT_TABLE
