#ifndef H_FIELDML_IO
#define H_FIELDML_IO

typedef struct _FieldmlInputStream *FmlInputStream;

typedef struct _FieldmlOutputStream *FmlOutputStream;

int FmlInputStreamReadInt( FmlInputStream stream );

double FmlInputStreamReadDouble( FmlInputStream stream );

int FmlInputStreamSkipLine( FmlInputStream stream );

FmlInputStream FmlCreateFileInputStream( const char *filename );

FmlInputStream FmlCreateStringInputStream( const char *string );

int FmlInputStreamIsEof( FmlInputStream stream );

void FmlInputStreamDestroy( FmlInputStream stream );


FmlOutputStream FmlCreateFileOutputStream( const char *filename, int append );

int FmlOutputStreamWriteDouble( FmlOutputStream stream, double value );

int FmlOutputStreamWriteInt( FmlOutputStream stream, int value );

int FmlOutputStreamWriteNewline( FmlOutputStream stream );

void FmlOutputStreamDestroy( FmlOutputStream stream );

#endif
