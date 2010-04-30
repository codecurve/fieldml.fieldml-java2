!> \file
!> $Id$
!> \author Caton Little
!> \brief This module handles reading in FieldML files. 
!>
!> \section LICENSE
!>
!> Version: MPL 1.1/GPL 2.0/LGPL 2.1
!>
!> The contents of this file are subject to the Mozilla Public License
!> Version 1.1 (the "License"); you may not use this file except in
!> compliance with the License. You may obtain a copy of the License at
!> http://www.mozilla.org/MPL/
!>
!> Software distributed under the License is distributed on an "AS IS"
!> basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
!> License for the specific language governing rights and limitations
!> under the License.
!>
!> The Original Code is OpenCMISS
!>
!> The Initial Developer of the Original Code is University of Auckland,
!> Auckland, New Zealand and University of Oxford, Oxford, United
!> Kingdom. Portions created by the University of Auckland and University
!> of Oxford are Copyright (C) 2007 by the University of Auckland and
!> the University of Oxford. All Rights Reserved.
!>
!> Contributor(s):
!>
!> Alternatively, the contents of this file may be used under the terms of
!> either the GNU General Public License Version 2 or later (the "GPL"), or
!> the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
!> in which case the provisions of the GPL or the LGPL are applicable instead
!> of those above. If you wish to allow use of your version of this file only
!> under the terms of either the GPL or the LGPL, and not to allow others to
!> use your version of this file under the terms of the MPL, indicate your
!> decision by deleting the provisions above and replace them with the notice
!> and other provisions required by the GPL or the LGPL. If you do not delete
!> the provisions above, a recipient may use your version of this file under
!> the terms of any one of the MPL, the GPL or the LGPL.
!>

!> Temporary IO routines for fluid mechanics

MODULE FIELDML_INPUT_ROUTINES

  USE BASE_ROUTINES
!  USE EQUATIONS_SET_CONSTANTS
!  USE FIELD_ROUTINES
  USE TYPES
  USE INPUT_OUTPUT 
  USE KINDS
  USE FIELDML_API
  USE BASIS_ROUTINES 
  USE OPENCMISS
  USE API_UTIL_ARRAY
  USE UTIL_ARRAY

  IMPLICIT NONE

  PRIVATE

  !Module parameters
  TYPE(VARYING_STRING) :: errorString

  !Module types

  !Interfaces

  INTERFACE

  END INTERFACE

  PUBLIC :: FIELDML_INPUT_TEST

CONTAINS

  !
  !================================================================================================================================
  !
  
  FUNCTION fieldmlCreateTpBasis( userNumber, interpolations, err )
    !Argument variables
    INTEGER(INTG), INTENT(IN) :: userNumber
    INTEGER(INTG), INTENT(IN) :: interpolations(:)
    INTEGER(INTG), INTENT(OUT) :: err
    !Function variable
    TYPE(CMISSBasisType) :: fieldmlCreateTpBasis

    CALL CMISSBasisTypeInitialise( fieldmlCreateTpBasis, err )
    CALL CMISSBasisCreateStart( userNumber, fieldmlCreateTpBasis, err )
    CALL CMISSBasisTypeSet( fieldmlCreateTpBasis, BASIS_LAGRANGE_HERMITE_TP_TYPE, err )
    CALL CMISSBasisNumberOfXiSet( fieldmlCreateTpBasis, size( interpolations ), err )
    CALL CMISSBasisInterpolationXiSet( fieldmlCreateTpBasis, interpolations, err )
    
    !Deliberately not finalized, so the user can make OpenCMISS-specific tweaks.
  END FUNCTION


  !
  !================================================================================================================================
  !

  SUBROUTINE FIELDML_READ_BASES( parseHandle, bases, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle !<The parse handle
    TYPE(CMISSBasisType), ALLOCATABLE, INTENT(INOUT) :: bases(:) !<An array to hold the user numbers of the bases.
    INTEGER(INTG), INTENT(OUT) :: err !<The error code
    
    !Local variables
    INTEGER(INTG) :: count, i, objectHandle, length, basisCount
    INTEGER(INTG), ALLOCATABLE :: tpTypes(:)
    LOGICAL :: knownBasis
    CHARACTER(LEN=1024) :: name
    
    count = Fieldml_GetObjectCount( parseHandle, FHT_CONTINUOUS_IMPORT )
    basisCount = 0
    
    DO i = 1, count
      objectHandle = Fieldml_GetObjectHandle( parseHandle, FHT_CONTINUOUS_IMPORT, i )

      length = Fieldml_CopyImportRemoteName( parseHandle, objectHandle, name, 1024 )
      
      knownBasis = .FALSE.
      
      IF( INDEX( name, 'library.fem.triquadratic_lagrange') == 1 ) THEN
        knownBasis = .TRUE.
        CALL REALLOCATE_INT( tpTypes, 3, "", err, errorString, *999 )
        tpTypes = BASIS_QUADRATIC_LAGRANGE_INTERPOLATION
      ELSE IF( INDEX( name, 'library.fem.trilinear_lagrange') == 1 ) THEN
        knownBasis = .TRUE.
        CALL REALLOCATE_INT( tpTypes, 3, "", err, errorString, *999 )
        tpTypes = BASIS_LINEAR_LAGRANGE_INTERPOLATION
      ENDIF
      
      IF( .NOT. knownBasis ) THEN
        CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  unknown basis: ", name(1:length), err, errorString, *999 )
        CYCLE
      ENDIF

      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  known basis: ", name(1:length), err, errorString, *999 )
      
      basisCount = basisCount + 1
      CALL GROW_ARRAY( bases, 1, err, *999 )
      bases( basisCount ) = fieldmlCreateTpBasis( objectHandle, tpTypes, err )
      
999   CYCLE
    END DO

  END SUBROUTINE FIELDML_READ_BASES

  !
  !================================================================================================================================
  !

  FUNCTION Fieldml_HasMarkup( parse, object, attribute, value, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(INTG), INTENT(IN) :: object
    CHARACTER(LEN=*), INTENT(IN) :: attribute
    CHARACTER(LEN=*), INTENT(IN) :: value
    INTEGER(INTG), INTENT(OUT) :: err

    LOGICAL :: Fieldml_HasMarkup

    !Locals
    INTEGER(INTG) :: length
    CHARACTER(LEN=1024) :: buffer

    length = Fieldml_CopyMarkupAttributeValue( parse, object, attribute//C_NULL_CHAR, buffer, 1024 )
    Fieldml_HasMarkup = ( index( buffer, value ) == 1 )
  END FUNCTION Fieldml_HasMarkup

  !
  !================================================================================================================================
  !

  FUNCTION Fieldml_IsElementEvaluatorCompatible( parse, object, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(INTG), INTENT(IN) :: object
    INTEGER(INTG), INTENT(OUT) :: err
    
    LOGICAL :: Fieldml_IsElementEvaluatorCompatible

    INTEGER(INTG) :: type, length
    CHARACTER(LEN=1024) :: name

    type = Fieldml_GetObjectType( parse, object )
    IF( type /= FHT_CONTINUOUS_IMPORT ) THEN
      Fieldml_IsElementEvaluatorCompatible = .FALSE.
      RETURN
    END IF
 
    length = Fieldml_CopyImportRemoteName( parse, object, name, 1024 )
    IF( index( name, 'library.fem.trilinear_lagrange' ) == 1 ) THEN
      Fieldml_IsElementEvaluatorCompatible = .TRUE.
    ELSE IF( index( name, 'library.fem.triquadratic_lagrange' ) == 1 ) THEN
      Fieldml_IsElementEvaluatorCompatible = .TRUE.
    ELSE
      Fieldml_IsElementEvaluatorCompatible = .FALSE.
    END IF
  
  END FUNCTION Fieldml_IsElementEvaluatorCompatible

  !
  !================================================================================================================================
  !

  FUNCTION Fieldml_IsTemplateCompatible( parse, object, elementDomain, err )
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(INTG), INTENT(IN) :: object
    INTEGER(INTG), INTENT(IN) :: elementDomain
    INTEGER(INTG), INTENT(OUT) :: err
    
    LOGICAL :: Fieldml_IsTemplateCompatible

    INTEGER(INTG) :: type, count, i, evaluator, domain

    type = Fieldml_GetObjectType( parse, object )
    IF( type /= FHT_CONTINUOUS_PIECEWISE ) THEN
      Fieldml_IsTemplateCompatible = .FALSE.
      RETURN
    END IF

    domain = Fieldml_GetIndexDomain( parse, object, 1 )
    IF( domain /= elementDomain ) THEN
      Fieldml_IsTemplateCompatible = .TRUE.
      RETURN
    END IF

    count = Fieldml_GetEvaluatorCount( parse, object )
    DO i = 1, count
      evaluator = Fieldml_GetEvaluatorHandle( parse, object, i )
      IF( .NOT. Fieldml_IsElementEvaluatorCompatible( parse, evaluator, err ) ) THEN
        Fieldml_IsTemplateCompatible = .FALSE.
        RETURN
      END IF
    END DO
    
    Fieldml_IsTemplateCompatible = .TRUE.
    
  END FUNCTION Fieldml_IsTemplateCompatible

  !
  !================================================================================================================================
  !

  FUNCTION Fieldml_IsFieldCompatible( parse, object, elementDomain, err )
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(INTG), INTENT(IN) :: object
    INTEGER(INTG), INTENT(IN) :: elementDomain
    INTEGER(INTG), INTENT(OUT) :: err

    LOGICAL :: Fieldml_IsFieldCompatible

    INTEGER(INTG) :: type, count, i, evaluator
    
    type = Fieldml_GetObjectType( parse, object )
    
    IF( type /= FHT_CONTINUOUS_AGGREGATE ) THEN
      Fieldml_IsFieldCompatible = .FALSE.
      RETURN
    END IF

    count = Fieldml_GetEvaluatorCount( parse, object )
    IF( count < 1 ) THEN
      Fieldml_IsFieldCompatible = .FALSE.
      RETURN
    END IF

    Fieldml_IsFieldCompatible = .TRUE.
    DO i = 1, count
      evaluator = Fieldml_GetEvaluatorHandle( parse, object, i )
      IF( .NOT. Fieldml_IsTemplateCompatible( parse, evaluator, elementDomain, err ) ) THEN
        Fieldml_IsFieldCompatible = .FALSE.
        RETURN
      END IF
    END DO

  END FUNCTION Fieldml_IsFieldCompatible

  !
  !================================================================================================================================
  !

  SUBROUTINE Fieldml_GetFieldHandles( parse, fieldHandles, meshHandle, err )
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(INTG), ALLOCATABLE :: fieldHandles(:)
    INTEGER(INTG), INTENT(IN) :: meshHandle
    INTEGER(INTG), INTENT(OUT) :: err

    INTEGER(INTG) :: count, i, object, fieldCount, elementDomain
   
    elementDomain = Fieldml_GetMeshElementDomain( parse, meshHandle )

    fieldCount = 0
    count = Fieldml_GetObjectCount( parse, FHT_CONTINUOUS_AGGREGATE )
    DO i = 1, count
      object = Fieldml_GetObjectHandle( parse, FHT_CONTINUOUS_AGGREGATE, i )
      IF( .NOT. Fieldml_HasMarkup( parse, object, 'field', 'true', err ) ) THEN
        CYCLE
      END IF

      IF( .NOT. Fieldml_IsFieldCompatible( parse, object, elementDomain, err ) ) THEN
        CYCLE
      END IF

      CALL GROW_ARRAY( fieldHandles, 1, "", err, errorString, *999 )
      fieldCount = fieldCount + 1
      fieldHandles( fieldCount ) = object
    END DO
    
999 RETURN
  END SUBROUTINE Fieldml_GetFieldHandles


  !
  !================================================================================================================================
  !


  SUBROUTINE FIELDML_GET_MESH_METRICS( parseHandle, meshHandle, dimensions, elementCount, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(INTG), INTENT(IN) :: meshHandle
    INTEGER(INTG), INTENT(OUT) :: dimensions
    INTEGER(INTG), INTENT(OUT) :: elementCount
    INTEGER(INTG), INTENT(OUT) :: err
  
    !Locals
    INTEGER(INTG) :: xiHandle, componentHandle, length, elementHandle
    CHARACTER(LEN=1024) :: name
     
    xiHandle = Fieldml_GetMeshXiDomain( parseHandle, meshHandle )
    componentHandle = Fieldml_GetDomainComponentEnsemble( parseHandle, xiHandle )
 
    !At the moment, library domains do not actually exist, so we can't directly
    !ask them what their cardinality is.
    length = Fieldml_CopyObjectName( parseHandle, componentHandle, name, 1024 )
    IF( index( name, 'library.ensemble.xi.3d' ) == 1 ) THEN
      dimensions = 3
    ELSE IF( index( name, 'library.ensemble.xi.2d' )  == 1 ) THEN
      dimensions = 2
    ELSE
      dimensions = 0
    END IF
    
    elementHandle = Fieldml_GetMeshElementDomain( parseHandle, meshHandle )
 
    elementCount = Fieldml_GetEnsembleDomainElementCount( parseHandle, elementHandle )

  END SUBROUTINE FIELDML_GET_MESH_METRICS

  !
  !================================================================================================================================
  !

  SUBROUTINE FIELDML_INPUT_TEST( err )
    !Argument variables
    INTEGER(INTG), INTENT(OUT) :: err !<The error code.
    
    !Local variables
    TYPE(VARYING_STRING) :: errorString
    INTEGER(C_INT) :: count, mesh, xi, element, length, xiComponents, i
    TYPE(C_PTR) :: handle
    TYPE(VARYING_STRING) :: filename
    CHARACTER(LEN=1024) :: name
    TYPE(CMISSBasisType), ALLOCATABLE :: bases(:)
    INTEGER(C_INT), ALLOCATABLE :: fields(:)
    
    filename = "HEX-M2-V2-P1_FE.xml"
    
    handle = Fieldml_ParseFile( char( filename )//C_NULL_CHAR )

    count = Fieldml_GetErrorCount( handle )

    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  error count: ", count, err, errorString, *999 )
    
    count = Fieldml_GetObjectCount( handle, 3 )

    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh count: ", count, err, errorString, *999 )
    
    mesh = Fieldml_GetObjectHandle( handle, 3, 1 )

    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh handle: ", mesh, err, errorString, *999 )
    
    length = Fieldml_CopyObjectName( handle, mesh, name, 1024 )
    
    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh name length: ", length, err, errorString, *999 )
    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh name: ", name(1:length), err, errorString, *999 )
    
    xi = Fieldml_GetMeshXiDomain( handle, mesh )
    element = Fieldml_GetMeshElementDomain( handle, mesh )
    xiComponents = Fieldml_GetDomainComponentEnsemble( handle, xi )
    
    length = Fieldml_CopyObjectName( handle, xiComponents, name, 1024 )
    
    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  xi name: ", name(1:length), err, errorString, *999 )
    
    CALL FIELDML_READ_BASES( handle, bases, err )
    
    CALL Fieldml_GetFieldHandles( handle, fields, mesh, err )

    count = size( fields )
    DO i = 1, count
      length = Fieldml_CopyObjectName( handle, fields(i), name, 1024 )
      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  field: ", fields(i), err, errorString, *999 )
      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "         ", name(1:length), err, errorString, *999 )
    END DO
    
    CALL EXITS( "FIELDML_INPUT_TEST" )
    RETURN
999 CALL ERRORS( "FIELDML_INPUT_TEST", err, errorString )
    CALL EXITS( "FIELDML_INPUT_TEST" )
    RETURN
  END SUBROUTINE FIELDML_INPUT_TEST
    
END MODULE FIELDML_INPUT_ROUTINES
