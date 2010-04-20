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
