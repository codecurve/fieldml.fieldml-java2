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
  USE INPUT_OUTPUT 
  USE KINDS
  USE FIELDML_API
  USE BASIS_ROUTINES 
  USE OPENCMISS
  USE UTIL_ARRAY
  USE CMISS

  IMPLICIT NONE

  PRIVATE

  !Module parameters
  INTEGER(INTG), PARAMETER :: BUFFER_SIZE = 1024
  
  INTEGER(INTG), PARAMETER :: FML_ERR_NO_ERROR = 0
  INTEGER(INTG), PARAMETER :: FML_ERR_UNKNOWN_BASIS = 10001
  INTEGER(INTG), PARAMETER :: FML_ERR_INVALID_BASIS = 10002
  INTEGER(INTG), PARAMETER :: FML_ERR_UNKNOWN_MESH_XI = 10003
  INTEGER(INTG), PARAMETER :: FML_ERR_UNKNOWN_COORDINATE_TYPE = 10004
  INTEGER(INTG), PARAMETER :: FML_ERR_INVALID_OBJECT = 10006
  INTEGER(INTG), PARAMETER :: FML_ERR_INVALID_PARAMETER = 10007
  INTEGER(INTG), PARAMETER :: FML_ERR_INVALID_MESH = 10008
  INTEGER(INTG), PARAMETER :: FML_ERR_INVALID_CONNECTIVITY = 10009
  !INTEGER(INTG), PARAMETER :: 
  !INTEGER(INTG), PARAMETER :: 
  !INTEGER(INTG), PARAMETER :: 

  TYPE(VARYING_STRING) :: errorString

  !Interfaces

  INTERFACE

  END INTERFACE

  PUBLIC :: FIELDML_INPUT_TEST, FieldmlInput_GetMeshInfo, FieldmlInput_GetCoordinateSystemInfo, FieldmlInput_ReadBases, &
    & Fieldml_GetFieldHandles, FieldmlInput_GetComponentBasis

CONTAINS

  !
  !================================================================================================================================
  !
  
  SUBROUTINE FieldmlInput_CreateBasis( parseHandle, objectHandle, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: objectHandle
    INTEGER(INTG), INTENT(OUT) :: err

    !Locals
    INTEGER(C_INT) :: length
    INTEGER(INTG), ALLOCATABLE :: tpTypes(:)
    CHARACTER(LEN=BUFFER_SIZE) :: name
    
    length = Fieldml_CopyImportRemoteName( parseHandle, objectHandle, name, BUFFER_SIZE )

    IF( Fieldml_GetObjectType( parseHandle, objectHandle ) /= FHT_CONTINUOUS_IMPORT ) THEN
      err = FML_ERR_INVALID_BASIS
      RETURN
    ENDIF
      
    IF( INDEX( name, 'library.fem.triquadratic_lagrange') == 1 ) THEN
      CALL REALLOCATE_INT( tpTypes, 3, "", err, errorString, *999 )
      tpTypes = BASIS_QUADRATIC_LAGRANGE_INTERPOLATION
    ELSE IF( INDEX( name, 'library.fem.trilinear_lagrange') == 1 ) THEN
      CALL REALLOCATE_INT( tpTypes, 3, "", err, errorString, *999 )
      tpTypes = BASIS_LINEAR_LAGRANGE_INTERPOLATION
    ELSE
      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  unknown basis: ", name(1:length), err, errorString, *999 )
      err = FML_ERR_UNKNOWN_BASIS
      RETURN
    ENDIF

    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  known basis: ", name(1:length), err, errorString, *999 )

    CALL CMISSBasisCreateStart( objectHandle, err )
    CALL CMISSBasisTypeSet( objectHandle, BASIS_LAGRANGE_HERMITE_TP_TYPE, err )
    CALL CMISSBasisNumberOfXiSet( objectHandle, size( tpTypes ), err )
    CALL CMISSBasisInterpolationXiSet( objectHandle, tpTypes, err )
    CALL CMISSBasisQuadratureNumberOfGaussXiSet( objectHandle, (/3,3,3/), err ) !CPL MUST FIX

999 RETURN    
    !Deliberately not finalized, so the user can make OpenCMISS-specific tweaks.
  END SUBROUTINE FieldmlInput_CreateBasis


  !
  !================================================================================================================================
  !

  SUBROUTINE FieldmlInput_ReadBases( parseHandle, bases, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle !<The parse handle
    INTEGER(INTG), ALLOCATABLE, INTENT(INOUT) :: bases(:) !<An array to hold the identified bases
    INTEGER(INTG), INTENT(OUT) :: err !<The error code
    
    !Local variables
    INTEGER(INTG) :: basisCount
    INTEGER(C_INT) :: objectHandle, i, count
    
    count = Fieldml_GetObjectCount( parseHandle, FHT_CONTINUOUS_IMPORT )
    basisCount = 0
    
    DO i = 1, count
      objectHandle = Fieldml_GetObjectHandle( parseHandle, FHT_CONTINUOUS_IMPORT, i )
      
      CALL FieldmlInput_CreateBasis( parseHandle, objectHandle, err )
      
      IF( err /= FML_ERR_NO_ERROR ) THEN
        CYCLE
      ENDIF

      basisCount = basisCount + 1
      CALL GROW_ARRAY( bases, 1, "", err, errorString, *999 )
      bases( basisCount ) = objectHandle
      
999   CYCLE
    ENDDO

  END SUBROUTINE FieldmlInput_ReadBases

  !
  !================================================================================================================================
  !

  FUNCTION FieldmlInput_HasMarkup( parse, object, attribute, value, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(C_INT), INTENT(IN) :: object
    CHARACTER(LEN=*), INTENT(IN) :: attribute
    CHARACTER(LEN=*), INTENT(IN) :: value
    INTEGER(INTG), INTENT(OUT) :: err

    LOGICAL :: FieldmlInput_HasMarkup

    !Locals
    INTEGER(C_INT) :: length
    CHARACTER(LEN=BUFFER_SIZE) :: buffer

    length = Fieldml_CopyMarkupAttributeValue( parse, object, attribute//C_NULL_CHAR, buffer, BUFFER_SIZE )

    FieldmlInput_HasMarkup = ( INDEX( buffer, value ) == 1 )
    
    err = FML_ERR_NO_ERROR
    
  END FUNCTION FieldmlInput_HasMarkup

  !
  !================================================================================================================================
  !

  FUNCTION FieldmlInput_IsElementEvaluatorCompatible( parse, object, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(C_INT), INTENT(IN) :: object
    INTEGER(INTG), INTENT(OUT) :: err
    
    LOGICAL :: FieldmlInput_IsElementEvaluatorCompatible

    INTEGER(C_INT) :: type, length
    CHARACTER(LEN=BUFFER_SIZE) :: name

    type = Fieldml_GetObjectType( parse, object )
    IF( type /= FHT_CONTINUOUS_IMPORT ) THEN
      FieldmlInput_IsElementEvaluatorCompatible = .FALSE.
      RETURN
    ENDIF
 
    length = Fieldml_CopyImportRemoteName( parse, object, name, BUFFER_SIZE )
    IF( INDEX( name, 'library.fem.trilinear_lagrange' ) == 1 ) THEN
      FieldmlInput_IsElementEvaluatorCompatible = .TRUE.
    ELSE IF( INDEX( name, 'library.fem.triquadratic_lagrange' ) == 1 ) THEN
      FieldmlInput_IsElementEvaluatorCompatible = .TRUE.
    ELSE
      FieldmlInput_IsElementEvaluatorCompatible = .FALSE.
    ENDIF
    
    err = FML_ERR_NO_ERROR
  
  END FUNCTION FieldmlInput_IsElementEvaluatorCompatible

  !
  !================================================================================================================================
  !

  FUNCTION FieldmlInput_IsTemplateCompatible( parse, object, elementDomain, err )
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(C_INT), INTENT(IN) :: object
    INTEGER(C_INT), INTENT(IN) :: elementDomain
    INTEGER(INTG), INTENT(OUT) :: err
    
    LOGICAL :: FieldmlInput_IsTemplateCompatible

    INTEGER(C_INT) :: type, count, i, evaluator, domain, firstEvaluator

    type = Fieldml_GetObjectType( parse, object )
    IF( type /= FHT_CONTINUOUS_PIECEWISE ) THEN
      FieldmlInput_IsTemplateCompatible = .FALSE.
      RETURN
    ENDIF

    domain = Fieldml_GetIndexDomain( parse, object, 1 )
    IF( domain /= elementDomain ) THEN
      FieldmlInput_IsTemplateCompatible = .TRUE.
      RETURN
    ENDIF

    count = Fieldml_GetEvaluatorCount( parse, object )
    
    IF( count == 0 ) THEN
      FieldmlInput_IsTemplateCompatible = .FALSE.
      RETURN
    ENDIF
    
    firstEvaluator = Fieldml_GetEvaluatorHandle( parse, object, 1 )
    IF( .NOT. FieldmlInput_IsElementEvaluatorCompatible( parse, firstEvaluator, err ) ) THEN
      FieldmlInput_IsTemplateCompatible = .FALSE.
      RETURN
    ENDIF
    
    !At the moment, OpenCMISS does not support different evaluators per element.
    
    DO i = 2, count
      evaluator = Fieldml_GetEvaluatorHandle( parse, object, i )
      IF( evaluator /= firstEvaluator ) THEN
        FieldmlInput_IsTemplateCompatible = .FALSE.
        RETURN
      ENDIF
    ENDDO
    
    FieldmlInput_IsTemplateCompatible = .TRUE.
    
  END FUNCTION FieldmlInput_IsTemplateCompatible

  !
  !================================================================================================================================
  !

  FUNCTION FieldmlInput_IsFieldCompatible( parse, object, elementDomain, err )
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(C_INT), INTENT(IN) :: object
    INTEGER(C_INT), INTENT(IN) :: elementDomain
    INTEGER(INTG), INTENT(OUT) :: err

    LOGICAL :: FieldmlInput_IsFieldCompatible

    INTEGER(C_INT) :: type, count, i, evaluator
    
    type = Fieldml_GetObjectType( parse, object )
    
    IF( type /= FHT_CONTINUOUS_AGGREGATE ) THEN
      FieldmlInput_IsFieldCompatible = .FALSE.
      RETURN
    ENDIF

    count = Fieldml_GetEvaluatorCount( parse, object )
    IF( count < 1 ) THEN
      FieldmlInput_IsFieldCompatible = .FALSE.
      RETURN
    ENDIF

    FieldmlInput_IsFieldCompatible = .TRUE.
    DO i = 1, count
      evaluator = Fieldml_GetEvaluatorHandle( parse, object, i )
      IF( .NOT. FieldmlInput_IsTemplateCompatible( parse, evaluator, elementDomain, err ) ) THEN
        FieldmlInput_IsFieldCompatible = .FALSE.
        RETURN
      ENDIF
    ENDDO

  END FUNCTION FieldmlInput_IsFieldCompatible

  !
  !================================================================================================================================
  !

  SUBROUTINE Fieldml_GetFieldHandles( parse, fieldHandles, meshHandle, err )
    TYPE(C_PTR), INTENT(IN) :: parse
    INTEGER(C_INT), ALLOCATABLE :: fieldHandles(:)
    INTEGER(C_INT), INTENT(IN) :: meshHandle
    INTEGER(INTG), INTENT(OUT) :: err

    INTEGER(C_INT) :: count, i, object, fieldCount, elementDomain
   
    elementDomain = Fieldml_GetMeshElementDomain( parse, meshHandle )

    fieldCount = 0
    count = Fieldml_GetObjectCount( parse, FHT_CONTINUOUS_AGGREGATE )
    DO i = 1, count
      object = Fieldml_GetObjectHandle( parse, FHT_CONTINUOUS_AGGREGATE, i )
      IF( .NOT. FieldmlInput_HasMarkup( parse, object, 'field', 'true', err ) ) THEN
        CYCLE
      ENDIF

      IF( .NOT. FieldmlInput_IsFieldCompatible( parse, object, elementDomain, err ) ) THEN
        CYCLE
      ENDIF

      CALL GROW_ARRAY( fieldHandles, 1, "", err, errorString, *999 )
      fieldCount = fieldCount + 1
      fieldHandles( fieldCount ) = object
    ENDDO
    
999 RETURN
  END SUBROUTINE Fieldml_GetFieldHandles


  !
  !================================================================================================================================
  !
  
  SUBROUTINE FieldmlInput_GetCoordinateSystemInfo( parseHandle, evaluatorHandle, coordinateType, coordinateCount, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: evaluatorHandle
    INTEGER(INTG), INTENT(OUT) :: coordinateType
    INTEGER(INTG), INTENT(OUT) :: coordinateCount
    INTEGER(INTG), INTENT(OUT) :: err
        
    !Locals
    INTEGER(C_INT) :: domainHandle, length
    CHARACTER(LEN=BUFFER_SIZE) :: name
    
    domainHandle = Fieldml_GetValueDomain( parseHandle, evaluatorHandle )
    
    IF( domainHandle == FML_INVALID_HANDLE ) THEN
      coordinateType = 0 !Doesn't seem to be a CMISSCoordinateUnknownType
      RETURN
    ENDIF
    
    length = Fieldml_CopyObjectName( parseHandle, domainHandle, name, BUFFER_SIZE )

    IF( INDEX( name, 'library.coordinates.rc.3d' ) == 1 ) THEN
      coordinateType = CMISSCoordinateRectangularCartesianType
      coordinateCount = 3
    ELSE IF( INDEX( name, 'library.coordinates.rc.2d' ) == 1 ) THEN
      coordinateType = CMISSCoordinateRectangularCartesianType
      coordinateCount = 2
    ELSE
      coordinateType = 0 !Doesn't seem to be a CMISSCoordinateUnknownType
      err = FML_ERR_UNKNOWN_COORDINATE_TYPE
    ENDIF
  
  END SUBROUTINE FieldmlInput_GetCoordinateSystemInfo


  !
  !================================================================================================================================
  !


  SUBROUTINE FieldmlInput_GetMeshInfo( parseHandle, meshHandle, dimensions, elementCount, nodeDomains, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: meshHandle
    INTEGER(INTG), INTENT(OUT) :: dimensions
    INTEGER(INTG), INTENT(OUT) :: elementCount
    INTEGER(INTG), ALLOCATABLE, INTENT(INOUT) :: nodeDomains(:)
    INTEGER(INTG), INTENT(OUT) :: err
  
    !Locals
    INTEGER(INTG) :: componentHandle, length, elementHandle, count, i, handle, nodeDomain
    CHARACTER(LEN=BUFFER_SIZE) :: name
     
    elementHandle = Fieldml_GetMeshElementDomain( parseHandle, meshHandle )
    elementCount = Fieldml_GetEnsembleDomainElementCount( parseHandle, elementHandle )
    handle = Fieldml_GetMeshXiDomain( parseHandle, meshHandle )
    componentHandle = Fieldml_GetDomainComponentEnsemble( parseHandle, handle )

    count = Fieldml_GetMeshConnectivityCount( parseHandle, meshHandle )

    IF( count == 0 ) THEN
      err = FML_ERR_INVALID_MESH
      RETURN
    END IF
    
    DO i = 1, count
      handle = Fieldml_GetMeshConnectivitySource( parseHandle, meshHandle, i )
      IF( Fieldml_GetObjectType( parseHandle, handle ) /= FHT_ENSEMBLE_PARAMETERS ) THEN
        err = FML_ERR_INVALID_CONNECTIVITY
        RETURN
      END IF
      
      IF( Fieldml_GetIndexCount( parseHandle, handle ) /= 2 ) THEN
        err = FML_ERR_INVALID_CONNECTIVITY
        RETURN
      END IF
      
      IF( ( Fieldml_GetIndexDomain( parseHandle, handle, 1 ) /= elementHandle ) .AND. &
        & ( Fieldml_GetIndexDomain( parseHandle, handle, 2 ) /= elementHandle ) ) THEN
        err = FML_ERR_INVALID_CONNECTIVITY
        RETURN
      END IF
      
      nodeDomain = Fieldml_GetValueDomain( parseHandle, handle )

      IF( .NOT. FieldmlInput_HasMarkup( parseHandle, nodeDomain, "geometric", "point", err ) ) THEN
        err = FML_ERR_INVALID_CONNECTIVITY
        RETURN
      END IF
      
      CALL GROW_ARRAY( nodeDomains, 1, "", err, errorString, *999 )
      nodeDomains( SIZE( nodeDomains ) ) = nodeDomain

      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  node domain: ", nodeDomain, err, errorString, *999 )
      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "        count: ", Fieldml_GetEnsembleDomainElementCount( parseHandle, nodeDomains( i ) ), err, errorString, *999 )
      
999   CYCLE      
      
    END DO
    
    IF( SIZE( nodeDomains ) < 1 ) THEN
      err = FML_ERR_INVALID_CONNECTIVITY
      RETURN
    END IF
     
    !At the moment, library domains do not actually exist, so we can't directly
    !ask them what their cardinality is.
    length = Fieldml_CopyObjectName( parseHandle, componentHandle, name, BUFFER_SIZE )
    IF( INDEX( name, 'library.ensemble.xi.3d' ) == 1 ) THEN
      dimensions = 3
    ELSE IF( INDEX( name, 'library.ensemble.xi.2d' )  == 1 ) THEN
      dimensions = 2
    ELSE IF( INDEX( name, 'library.ensemble.xi.1d' )  == 1 ) THEN
      dimensions = 1
    ELSE
      dimensions = 0
      err = FML_ERR_UNKNOWN_MESH_XI
      RETURN
    ENDIF    

  END SUBROUTINE FieldmlInput_GetMeshInfo

  !
  !================================================================================================================================
  !

  FUNCTION FieldmlInput_GetComponentBasis( parseHandle, fieldHandle, componentNumber, err )
    !Arguments
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: fieldHandle
    INTEGER(INTG), INTENT(IN) :: componentNumber
    INTEGER(INTG), INTENT(OUT) :: err
    
    !Function
    INTEGER(C_INT) :: FieldmlInput_GetComponentBasis
  
    !Locals
    INTEGER(INTG) :: templateHandle
    
    FieldmlInput_GetComponentBasis = FML_INVALID_HANDLE
    
    IF( Fieldml_GetObjectType( parseHandle, fieldHandle ) /= FHT_CONTINUOUS_AGGREGATE ) THEN
      err = FML_ERR_INVALID_OBJECT
      RETURN
    END IF
    
    templateHandle = Fieldml_GetEvaluatorHandle( parseHandle, fieldHandle, componentNumber )
    IF( templateHandle == FML_INVALID_HANDLE ) THEN
      err = FML_ERR_INVALID_PARAMETER
      RETURN
    END IF
    
    IF( Fieldml_GetObjectType( parseHandle, templateHandle ) /= FHT_CONTINUOUS_PIECEWISE ) THEN
      err = FML_ERR_INVALID_PARAMETER
      RETURN
    END IF
    
    !At the moment, we don't need an element number, as the evaluator for all elements is the same
    FieldmlInput_GetComponentBasis = Fieldml_GetEvaluatorHandle( parseHandle, templateHandle, 1 )
    
  END FUNCTION FieldmlInput_GetComponentBasis
  
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
    CHARACTER(LEN=BUFFER_SIZE) :: name
    INTEGER(INTG), ALLOCATABLE :: bases(:)
    INTEGER(C_INT), ALLOCATABLE :: fields(:)
    
    handle = Fieldml_ParseFile( "test.xml"//C_NULL_CHAR )

    count = Fieldml_GetErrorCount( handle )

    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  error count: ", count, err, errorString, *999 )
    
    count = Fieldml_GetObjectCount( handle, 3 )

    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh count: ", count, err, errorString, *999 )
    
    mesh = Fieldml_GetObjectHandle( handle, 3, 1 )

    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh handle: ", mesh, err, errorString, *999 )
    
    length = Fieldml_CopyObjectName( handle, mesh, name, BUFFER_SIZE )
    
    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh name length: ", length, err, errorString, *999 )
    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  mesh name: ", name(1:length), err, errorString, *999 )
    
    xi = Fieldml_GetMeshXiDomain( handle, mesh )
    element = Fieldml_GetMeshElementDomain( handle, mesh )
    xiComponents = Fieldml_GetDomainComponentEnsemble( handle, xi )
    
    length = Fieldml_CopyObjectName( handle, xiComponents, name, BUFFER_SIZE )
    
    CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  xi name: ", name(1:length), err, errorString, *999 )
    
    CALL Fieldml_GetFieldHandles( handle, fields, mesh, err )

    count = size( fields )
    DO i = 1, count
      length = Fieldml_CopyObjectName( handle, fields(i), name, BUFFER_SIZE )
      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "  field: ", fields(i), err, errorString, *999 )
      CALL WRITE_STRING_VALUE( GENERAL_OUTPUT_TYPE, "         ", name(1:length), err, errorString, *999 )
    ENDDO
    
    CALL EXITS( "FIELDML_INPUT_TEST" )
    RETURN
999 CALL ERRORS( "FIELDML_INPUT_TEST", err, errorString )
    CALL EXITS( "FIELDML_INPUT_TEST" )
    RETURN
  END SUBROUTINE FIELDML_INPUT_TEST
    
END MODULE FIELDML_INPUT_ROUTINES
