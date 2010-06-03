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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "fieldml_api.h"
#include "fieldml_io.h"

typedef enum _FmlStreamType
{
    FILE_STREAM,
    STRING_STREAM,
}
FmlStreamType;

typedef struct _FileStream
{
    FILE *file;
}
FileStream;

typedef struct _StringStream
{
    const char *string;
    int stringPos;
    int stringMaxLen;
}
StringStream;

typedef struct _FieldmlInputStream
{
    FmlStreamType type;
    union
    {
        FileStream fileStream;
        StringStream stringStream;
    }
    stream;
    
    char *buffer;
    int bufferCount;
    int bufferPos;
    int isEof;
}
FieldmlStream;

typedef struct _FieldmlOutputStream
{
    FmlStreamType type;
    union
    {
        FileStream fileStream;
        StringStream stringStream;
    }
    stream;
}
FieldmlOutputStream;

static const int BUFFER_SIZE = 1024;

//Using a #define because the relevant buffer is allocated on stack.
#define NBUFFER_SIZE 64

static int loadBuffer( FmlInputStream stream )
{
    int len;
    
    stream->bufferPos = 0;

    switch( stream->type )
    {
    case FILE_STREAM:
        stream->bufferCount = fread( stream->buffer, 1, BUFFER_SIZE, stream->stream.fileStream.file );
        break;
    case STRING_STREAM:
        len = BUFFER_SIZE;
        if( len + stream->stream.stringStream.stringPos > stream->stream.stringStream.stringMaxLen )
        {
            len = stream->stream.stringStream.stringMaxLen - stream->stream.stringStream.stringPos;
        }
        memcpy( stream->buffer, stream->stream.stringStream.string, len );
        stream->stream.stringStream.stringPos += len;
        stream->bufferCount = len;
        break;
    }

    if( stream->bufferCount <= 0 )
    {
        stream->isEof = 1;
        return 0;
    }
    
    return 1;
}


int FmlInputStreamReadInt( FmlInputStream stream )
{
    int value = 0;
    int invert = 0;
    int gotDigit = 0;
    int d;
    
    while( 1 )
    {
        if( stream->bufferPos >= stream->bufferCount )
        {
            if( !loadBuffer( stream ) )
            {
                return 0;
            }
        }
        
        while( stream->bufferPos < stream->bufferCount )
        {
            d = stream->buffer[stream->bufferPos++];
            if( ( d >= '0' ) && ( d <= '9' ) )
            {
                gotDigit = 1;
                
                value *= 10;
                value += ( d - '0' );
            }
            else if( ( d == '-' ) && ( !gotDigit ) )
            {
                invert = 1 - invert;
            }
            else if( gotDigit )
            {
                stream->bufferPos--;
                if( invert )
                {
                    value = -value;
                }
                return value;
            }
        }
    }
}


double FmlInputStreamReadDouble( FmlInputStream stream )
{
    int d;
    int count = 0;
    int skipping = 1;
    char nBuffer[NBUFFER_SIZE];
    
    while( 1 )
    {
        if( stream->bufferPos >= stream->bufferCount )
        {
            if( !loadBuffer( stream ) )
            {
                return 0;
            }
        }
        
        while( stream->bufferPos < stream->bufferCount )
        {
            d = stream->buffer[stream->bufferPos++];
            if( ( ( d >= '0' ) && ( d <= '9' ) ) || ( d == 'e' ) || ( d == 'E' ) || ( d == '-' ) || ( d == '+') || ( d == '.' ) )
            {
                skipping = 0;
                
                if( count < NBUFFER_SIZE - 1 )
                {
                    //Yes, this will truncate ridiculously long numbers that can't fit into a double anyway.
                    nBuffer[count++] = d;
                }
            }
            else if( skipping )
            {
                continue;
            }
            else
            {
                stream->bufferPos--;
                nBuffer[count] = 0;
                return strtod( nBuffer, NULL );
            }
        }
    }
}


int FmlInputStreamSkipLine( FmlInputStream stream )
{
    while( 1 )
    {
        if( stream->bufferPos >= stream->bufferCount )
        {
            if( !loadBuffer( stream ) )
            {
                return FML_ERR_FILE_READ;
            }
        }
        
        while( stream->bufferPos < stream->bufferCount )
        {
            if( stream->buffer[stream->bufferPos++] == '\n' )
            {
                return FML_ERR_NO_ERROR;
            }
        }
    }
}


FmlInputStream FmlCreateFileInputStream( const char *filename )
{
    FmlInputStream stream;
    FILE *file;
    
    file = fopen( filename, "r" );
    
    if( file == NULL )
    {
        return NULL;
    }
    
    stream = calloc( 1, sizeof( FieldmlStream ) );

    stream->type = FILE_STREAM;
    stream->stream.fileStream.file = file;
    
    stream->buffer = calloc( 1, BUFFER_SIZE );
    stream->bufferCount = 0;
    stream->bufferPos = 0;
    stream->isEof = 0;
    
    return stream;
}


FmlInputStream FmlCreateStringInputStream( const char *string )
{
    FmlInputStream stream;
    
    stream = calloc( 1, sizeof( FieldmlStream ) );

    stream->type = STRING_STREAM;
    stream->stream.stringStream.string = string;
    stream->stream.stringStream.stringPos = 0;
    stream->stream.stringStream.stringMaxLen = strlen( string );
    
    stream->buffer = calloc( 1, BUFFER_SIZE );
    stream->bufferCount = 0;
    stream->bufferPos = 0;
    stream->isEof = 0;
    
    return stream;
}


int FmlInputStreamIsEof( FmlInputStream stream )
{
    return stream->isEof;
}


void FmlInputStreamDestroy( FmlInputStream stream )
{
    if( stream->type == FILE_STREAM )
    {
        if( stream->stream.fileStream.file != NULL )
        {
            fclose( stream->stream.fileStream.file );
        }
    }
    
    free( stream->buffer );
    free( stream );
}


FmlOutputStream FmlCreateFileOutputStream( const char *filename, int append )
{
    FmlOutputStream stream;
    FILE *file;
    
    if( append )
    {
        file = fopen( filename, "a" );
    }
    else
    {
        file = fopen( filename, "w" );
    }
    
    if( file == NULL )
    {
        return NULL;
    }
    
    stream = calloc( 1, sizeof( FieldmlStream ) );

    stream->type = FILE_STREAM;
    stream->stream.fileStream.file = file;
    
    return stream;
}


int FmlOutputStreamWriteDouble( FmlOutputStream stream, double value )
{
    if( stream->type == FILE_STREAM )
    {
        fprintf( stream->stream.fileStream.file, "%.8g ", value );
    }
    
    return FML_ERR_NO_ERROR;
}


int FmlOutputStreamWriteInt( FmlOutputStream stream, int value )
{
    if( stream->type == FILE_STREAM )
    {
        fprintf( stream->stream.fileStream.file, "%d ", value );
    }
    
    return FML_ERR_NO_ERROR;
}


int FmlOutputStreamWriteNewline( FmlOutputStream stream )
{
    if( stream->type == FILE_STREAM )
    {
        fprintf( stream->stream.fileStream.file, "\n" );
    }
    
    return FML_ERR_NO_ERROR;
}


void FmlOutputStreamDestroy( FmlOutputStream stream )
{
    if( stream->type == FILE_STREAM )
    {
        if( stream->stream.fileStream.file != NULL )
        {
            fclose( stream->stream.fileStream.file );
        }
    }
    
    free( stream );
}
