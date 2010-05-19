

 # ***** BEGIN LICENSE BLOCK *****
 # Version: MPL 1.1/GPL 2.0/LGPL 2.1
 #
 # The contents of this file are subject to the Mozilla Public License Version
 # 1.1 (the "License"); you may not use this file except in compliance with
 # the License. You may obtain a copy of the License at
 # http://www.mozilla.org/MPL/
 #
 # Software distributed under the License is distributed on an "AS IS" basis,
 # WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 # for the specific language governing rights and limitations under the
 # License.
 #
 # The Original Code is cmgui
 #
 # The Initial Developer of the Original Code is
 # Auckland Uniservices Ltd, Auckland, New Zealand.
 # Portions created by the Initial Developer are Copyright (C) 2005
 # the Initial Developer. All Rights Reserved.
 #
 # Contributor(s): 
 #
 # Alternatively, the contents of this file may be used under the terms of
 # either the GNU General Public License Version 2 or later (the "GPL"), or
 # the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 # in which case the provisions of the GPL or the LGPL are applicable instead
 # of those above. If you wish to allow use of your version of this file only
 # under the terms of either the GPL or the LGPL, and not to allow others to
 # use your version of this file under the terms of the MPL, indicate your
 # decision by deleting the provisions above and replace them with the notice
 # and other provisions required by the GPL or the LGPL. If you do not delete
 # the provisions above, a recipient may use your version of this file under
 # the terms of any one of the MPL, the GPL or the LGPL.
 #
 # ***** END LICENSE BLOCK ***** */

# Set the minimum version of cmake required to 2.6
CMAKE_MINIMUM_REQUIRED( VERSION 2.6 )

IF( "${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION}" LESS 2.5 )
        MESSAGE( FATAL_ERROR "CMake >= 2.6.0 required" )
ENDIF( "${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION}" LESS 2.5 )

PROJECT( FieldML )

# Define some names
SET( LIBRARY_TARGET_NAME fieldml )
STRING( TOUPPER ${LIBRARY_TARGET_NAME} UPPERCASE_LIBRARY_TARGET_NAME )
SET( TEST_EXE_TARGET_NAME fieldml_test )

# Define library version 
SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_MAJOR 0 )
SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_MINOR 0 )
SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_PATCH 0 )
SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_VERSION
	 ${${UPPERCASE_LIBRARY_TARGET_NAME}_MAJOR}.${${UPPERCASE_LIBRARY_TARGET_NAME}_MINOR}.${${UPPERCASE_LIBRARY_TARGET_NAME}_PATCH} )

# Forcing a Debug or Release configuration
STRING( REGEX MATCH "Debug|Release" HAVE_MATCHED_BUILD_TYPE "${${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TYPE}" )
IF( NOT HAVE_MATCHED_BUILD_TYPE )
        SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TYPE "Release" CACHE STRING "Specify the build type, the default is: release, options are: default, release" )
ENDIF( NOT HAVE_MATCHED_BUILD_TYPE )
SET( CMAKE_BUILD_TYPE ${${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TYPE} CACHE INTERNAL "Manipulate this with ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TYPE" FORCE)

# Define base install location
IF( NOT DEFINED ${UPPERCASE_LIBRARY_TARGET_NAME}_INSTALL_PREFIX )
        IF( WIN32 )
                SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_INSTALL_PREFIX
                        "c:/${LIBRARY_TARGET_NAME}-${${UPPERCASE_LIBRARY_TARGET_NAME}_VERSION}" )
        ELSE( WIN32 )
                SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_INSTALL_PREFIX
                        "/usr/local" )
        ENDIF( WIN32 )
ENDIF( NOT DEFINED ${UPPERCASE_LIBRARY_TARGET_NAME}_INSTALL_PREFIX )

SET( ${UPPERCASE_LIBRARY_TARGET_NAME}_INSTALL_PREFIX ${${UPPERCASE_LIBRARY_TARGET_NAME}_INSTALL_PREFIX} CACHE
        PATH "Install prefix prepended to install location" )
SET( CMAKE_INSTALL_PREFIX "${${UPPERCASE_LIBRARY_TARGET_NAME}_INSTALL_PREFIX}" CACHE INTERNAL "Prefix
        prepended to install directories if target destination is not absolute,
        immutable" FORCE )

OPTION( ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_STATIC_LIB "Build static ${LIBRARY_TARGET_NAME}" ${${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_STATIC_LIB} )
OPTION( ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TEST "Build ${LIBRARY_TARGET_NAME} test application" ${${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TEST} ) 

IF( NOT ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TEST )
        SET( EXCLUDE_TEST EXCLUDE_FROM_ALL )
ENDIF( NOT ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TEST )

# Find dependent libraries
FIND_PACKAGE( LibXml2 REQUIRED )
MESSAGE( STATUS "XML I D:${LIBXML2_INCLUDE_DIR}" )
MESSAGE( STATUS "XML L:${LIBXML2_LIBRARIES}" )

# Define the sources for the library
SET( LIBRARY_SRCS
	fieldml_api.c
	fieldml_io.c
        fieldml_parse.c
        fieldml_sax.c
        fieldml_validate.c
        fieldml_write.c
	int_stack.c 
        int_table.c
	simple_list.c
        string_const.c
        string_table.c )
SET( LIBRARY_PUBLIC_HDRS
        fieldml_api.h )
SET( LIBRARY_PRIVATE_HDRS
	fieldml_io.h
	fieldml_parse.h
	fieldml_sax.h
	fieldml_structs.h
	fieldml_validate.h
	fieldml_write.h
	int_stack.h
	int_table.h
	simple_list.h
	string_const.h
	string_table.h )
SET( TEST_EXE_SRCS
	fieldml_test.c )
# Define flags for build
SET( CMAKE_DEBUG_POSTFIX "d" )
IF( "${CMAKE_BUILD_TYPE}" STREQUAL "Debug" )
	ADD_DEFINITIONS( -DDEBUG )
ENDIF( "${CMAKE_BUILD_TYPE}" STREQUAL "Debug" )
IF( WIN32 )
	ADD_DEFINITIONS( -D_CRT_SECURE_NO_WARNINGS )
ENDIF( WIN32 )
INCLUDE_DIRECTORIES( ${LIBXML2_INCLUDE_DIR} )

# Create library
ADD_LIBRARY( ${LIBRARY_TARGET_NAME} ${LIBRARY_BUILD_TYPE} ${LIBRARY_SRCS} ${LIBRARY_PUBLIC_HDRS} ${LIBRARY_PRIVATE_HDRS} ${LIBRARY_WIN32_XTRAS} )
TARGET_LINK_LIBRARIES( ${LIBRARY_TARGET_NAME} ${LIBXML2_LIBRARIES} )

IF( ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TEST )
	ADD_EXECUTABLE( ${TEST_EXE_TARGET_NAME} ${EXCLUDE_TEST} ${TEST_EXE_SRCS} )
	TARGET_LINK_LIBRARIES( ${TEST_EXE_TARGET_NAME} ${LIBRARY_TARGET_NAME} ${LIBXML2_LIBRARIES} )
ENDIF( ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_TEST )

IF( WIN32 AND NOT ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_STATIC_LIB )
        SET_TARGET_PROPERTIES(${LIBRARY_TARGET_NAME} PROPERTIES IMPORT_SUFFIX _dll.lib)
        INSTALL( TARGETS ${LIBRARY_TARGET_NAME} ARCHIVE
                DESTINATION lib )
ENDIF( WIN32 AND NOT ${UPPERCASE_LIBRARY_TARGET_NAME}_BUILD_STATIC_LIB )

# Define install rules
INSTALL( TARGETS ${LIBRARY_TARGET_NAME} EXPORT ${LIBRARY_TARGET_NAME}-targets ${LIBRARY_INSTALL_TYPE}
        DESTINATION lib )
INSTALL( FILES ${LIBRARY_PUBLIC_HDRS}
        DESTINATION include )
INSTALL( EXPORT ${LIBRARY_TARGET_NAME}-targets DESTINATION lib/cmake )

SET( CONFIG_FILE_CONTENTS 
        "\nGET_FILENAME_COMPONENT( SELF_DIR \"\${CMAKE_CURRENT_LIST_FILE}\" PATH )"
        "\nINCLUDE( \${SELF_DIR}/${LIBRARY_TARGET_NAME}-targets.cmake )"
        "\nGET_FILENAME_COMPONENT( ${UPPERCASE_LIBRARY_TARGET_NAME}_INCLUDE_DIRS \"\${SELF_DIR}/../../include\" ABSOLUTE )"
        "\nSET( ${UPPERCASE_LIBRARY_TARGET_NAME}_LIBRARIES ${LIBRARY_TARGET_NAME} ${LIBXML2_LIBRARIES} )"
        "\nSET( ${UPPERCASE_LIBRARY_TARGET_NAME}_FOUND TRUE )" 
        "\n\n" )
FILE( WRITE ${LIBRARY_CONFIG_FILE} ${CONFIG_FILE_CONTENTS} )
INSTALL( FILES ${LIBRARY_CONFIG_FILE} DESTINATION lib/cmake )
