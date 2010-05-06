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

  INTERFACE FieldmlInput_ReadRawData
    MODULE PROCEDURE FieldmlInput_ReadRawData_Int
    MODULE PROCEDURE FieldmlInput_ReadRawData_Real
  END INTERFACE FieldmlInput_ReadRawData

  INTERFACE

  END INTERFACE

  PUBLIC :: FieldmlInput_GetMeshInfo, FieldmlInput_GetCoordinateSystemInfo, FieldmlInput_GetBasisInfo, &
    & Fieldml_GetFieldHandles, FieldmlInput_GetComponentBasis, FieldmlInput_GetBasisConnectivityInfo, &
    & FieldmlInput_ReadRawData, FieldmlInput_GetBasisHandles

CONTAINS

  !
  !================================================================================================================================
  !

  SUBROUTINE FieldmlInput_ReadRawData_Int( parseHandle, parametersHandle, array, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: parametersHandle
    INTEGER(C_INT), INTENT(INOUT) :: array(:,:)
    INTEGER(INTG), INTENT(OUT) :: err

    !Locals
    INTEGER(C_INT) :: length, offset, fileType, dataUnit, i
    INTEGER(C_INT) :: index1Handle, index2Handle, count1, count2, swizzleCount
    INTEGER(C_INT), ALLOCATABLE, TARGET :: swizzle(:), buffer(:)
    CHARACTER(LEN=BUFFER_SIZE) :: name
    
    dataUnit = 1

    length = Fieldml_CopyObjectName( parseHandle, parametersHandle, name, BUFFER_SIZE )
    
    IF( Fieldml_GetObjectType( parseHandle, parametersHandle ) /= FHT_ENSEMBLE_PARAMETERS ) THEN
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF
    
    IF( Fieldml_GetParameterDataDescription( parseHandle, parametersHandle ) /= DESCRIPTION_SEMIDENSE ) THEN
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF

    IF( ( Fieldml_GetSemidenseIndexCount( parseHandle, parametersHandle, 1 ) /= 0 ) .OR. &
      & ( Fieldml_GetSemidenseIndexCount( parseHandle, parametersHandle, 0 ) /= 2 ) ) THEN
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF
    
    index1Handle = Fieldml_GetSemidenseIndex( parseHandle, parametersHandle, 1, 0 )
    index2Handle = Fieldml_GetSemidenseIndex( parseHandle, parametersHandle, 2, 0 )
    
    count1 = Fieldml_GetEnsembleDomainElementCount( parseHandle, index1Handle )
    count2 = Fieldml_GetEnsembleDomainElementCount( parseHandle, index2Handle )
    
    IF( Fieldml_GetParameterDataLocation( parseHandle, parametersHandle ) /= LOCATION_FILE ) THEN
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF

    length = Fieldml_CopyParameterDataFilename( parseHandle, parametersHandle, name, BUFFER_SIZE )
    offset = Fieldml_GetParameterDataOffset( parseHandle, parametersHandle )
    fileType = Fieldml_GetParameterDataFileType( parseHandle, parametersHandle )
    
    IF( ( length == 0 ) .OR. ( fileType == TYPE_UNKNOWN ) ) THEN
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF
    
    swizzleCount = Fieldml_GetSwizzleCount( parseHandle, parametersHandle )

    IF( swizzleCount > 0 ) THEN
      IF( swizzleCount /= count1 ) THEN
        err = FML_ERR_INVALID_OBJECT
        RETURN
      ENDIF
      ALLOCATE( swizzle( count1 ) )
      swizzleCount = Fieldml_CopySwizzleData( parseHandle, parametersHandle, C_LOC(swizzle), count1 )
    ENDIF
    
    ALLOCATE( buffer( count1 ) )

    OPEN( UNIT = dataUnit, FILE = name( 1 : length ), STATUS = 'old' )

    IF( fileType == TYPE_LINES ) THEN
      DO i = 1, offset
        READ( dataUnit, * )
      END DO
    ENDIF

    DO i = 1, count2
        READ(1,*) buffer( 1:count1 )
        IF( swizzleCount == 0 ) THEN
          array( i, 1:count1 ) = buffer( 1:count1 )
        ELSE
          array( i, 1:count1 ) = buffer(swizzle)
        ENDIF
    ENDDO


999    CLOSE( dataUnit )

    IF( ALLOCATED( swizzle ) ) THEN
        DEALLOCATE( swizzle )
    ENDIF
    DEALLOCATE( buffer )
    
  END SUBROUTINE FieldmlInput_ReadRawData_Int

  !
  !================================================================================================================================
  !

  SUBROUTINE FieldmlInput_ReadRawData_Real( parseHandle, parametersHandle, array, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: parametersHandle
    REAL(C_DOUBLE), INTENT(INOUT) :: array(:,:)
    INTEGER(INTG), INTENT(OUT) :: err

    !Locals
    INTEGER(C_INT) :: length, offset, fileType, dataUnit, i
    INTEGER(C_INT) :: index1Handle, index2Handle, count1, count2, swizzleCount
    INTEGER(C_INT), ALLOCATABLE, TARGET :: swizzle(:)
    REAL(C_DOUBLE), ALLOCATABLE, TARGET :: buffer(:)
    CHARACTER(LEN=BUFFER_SIZE) :: name
    
    dataUnit = 1

    length = Fieldml_CopyObjectName( parseHandle, parametersHandle, name, BUFFER_SIZE )
    
    IF( Fieldml_GetObjectType( parseHandle, parametersHandle ) /= FHT_CONTINUOUS_PARAMETERS ) THEN
      WRITE(*,'("ERROR1")')
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF
    
    IF( Fieldml_GetParameterDataDescription( parseHandle, parametersHandle ) /= DESCRIPTION_SEMIDENSE ) THEN
      WRITE(*,'("ERROR2")')
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF

    IF( ( Fieldml_GetSemidenseIndexCount( parseHandle, parametersHandle, 1 ) /= 0 ) .OR. &
      & ( Fieldml_GetSemidenseIndexCount( parseHandle, parametersHandle, 0 ) /= 2 ) ) THEN
      WRITE(*,'("ERROR3")')
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF
    
    index1Handle = Fieldml_GetSemidenseIndex( parseHandle, parametersHandle, 1, 0 )
    index2Handle = Fieldml_GetSemidenseIndex( parseHandle, parametersHandle, 2, 0 )
    
    count1 = Fieldml_GetEnsembleDomainElementCount( parseHandle, index1Handle )
    count2 = Fieldml_GetEnsembleDomainElementCount( parseHandle, index2Handle )
    
    IF( Fieldml_GetParameterDataLocation( parseHandle, parametersHandle ) /= LOCATION_FILE ) THEN
      WRITE(*,'("ERROR4")')
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF

    length = Fieldml_CopyParameterDataFilename( parseHandle, parametersHandle, name, BUFFER_SIZE )
    offset = Fieldml_GetParameterDataOffset( parseHandle, parametersHandle )
    fileType = Fieldml_GetParameterDataFileType( parseHandle, parametersHandle )
    
    IF( ( length == 0 ) .OR. ( fileType /= TYPE_LINES ) ) THEN
      WRITE(*,'("ERROR5")')
      err = FML_ERR_INVALID_OBJECT
      RETURN
    ENDIF
    
    swizzleCount = Fieldml_GetSwizzleCount( parseHandle, parametersHandle )

    IF( swizzleCount > 0 ) THEN
      IF( swizzleCount /= count1 ) THEN
        WRITE(*,'("ERROR6")')
        err = FML_ERR_INVALID_OBJECT
        RETURN
      ENDIF
      ALLOCATE( swizzle( count1 ) )
      swizzleCount = Fieldml_CopySwizzleData( parseHandle, parametersHandle, C_LOC(swizzle), count1 )
    ENDIF
    
    ALLOCATE( buffer( count1 ) )

    OPEN( UNIT = dataUnit, FILE = name( 1 : length ), STATUS = 'old' )

    IF( fileType == TYPE_LINES ) THEN
      DO i = 1, offset
        READ( dataUnit, * )
      END DO
    ENDIF
    
    DO i = 1, count2
      READ( dataUnit, * ) buffer( 1:count1 )
      IF( swizzleCount == 0 ) THEN
        array( i, 1:count1 ) = buffer( 1:count1 )
      ELSE
        array( i, 1:count1 ) = buffer(swizzle)
      ENDIF
    ENDDO


999    CLOSE( dataUnit )

    IF( ALLOCATED( swizzle ) ) THEN
        DEALLOCATE( swizzle )
    ENDIF
    DEALLOCATE( buffer )
    
  END SUBROUTINE FieldmlInput_ReadRawData_Real

  !
  !================================================================================================================================
  !

  SUBROUTINE FieldmlInput_GetBasisConnectivityInfo( parseHandle, meshHandle, basisHandle, connectivityHandle, layoutHandle, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle !<The parse handle
    INTEGER(C_INT), INTENT(IN) :: meshHandle
    INTEGER(C_INT), INTENT(IN) :: basisHandle
    INTEGER(C_INT), INTENT(OUT) :: connectivityHandle
    INTEGER(C_INT), INTENT(OUT) :: layoutHandle
    INTEGER(INTG), INTENT(OUT) :: err !<The error code
    
    !Local variables
    INTEGER(C_INT) :: count, i, xiHandle, paramsHandle, handle1, handle2

    count = Fieldml_GetImportAliasCount( parseHandle, basisHandle )
    IF( count /= 2 ) THEN
      err = FML_ERR_INVALID_BASIS
      RETURN
    END IF
    
    xiHandle = Fieldml_GetMeshXiDomain( parseHandle, meshHandle )

    handle1 = Fieldml_GetImportAliasLocalHandle( parseHandle, basisHandle, 1 )
    handle2 = Fieldml_GetImportAliasLocalHandle( parseHandle, basisHandle, 2 )

    IF( handle1 == xiHandle ) THEN
      paramsHandle = handle2
    ELSE IF( handle2 == xiHandle ) THEN
      paramsHandle = handle1
    ELSE
      err = FML_ERR_INVALID_BASIS
      RETURN
    ENDIF

    IF( Fieldml_GetObjectType( parseHandle, paramsHandle ) /= FHT_CONTINUOUS_DEREFERENCE ) THEN
      err = FML_ERR_INVALID_BASIS
      RETURN
    ENDIF
    
    handle1 = Fieldml_GetDereferenceIndexes( parseHandle, paramsHandle )
    
    count = Fieldml_GetMeshConnectivityCount( parseHandle, meshHandle )
    DO i = 1, count
      IF( Fieldml_GetMeshConnectivitySource( parseHandle, meshHandle, i ) == handle1 ) THEN
        connectivityHandle = handle1
        layoutHandle = Fieldml_GetMeshConnectivityDomain( parseHandle, meshHandle, i )
        RETURN
      ENDIF
    ENDDO

    err = FML_ERR_INVALID_BASIS
999 RETURN
  END SUBROUTINE FieldmlInput_GetBasisConnectivityInfo

  !
  !================================================================================================================================
  !

  SUBROUTINE FieldmlInput_GetBasisInfo( parseHandle, meshHandle, objectHandle, basisType, basisInterpolations, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: meshHandle
    INTEGER(C_INT), INTENT(IN) :: objectHandle
    INTEGER(INTG), INTENT(OUT) :: basisType
    INTEGER(C_INT), ALLOCATABLE, INTENT(OUT) :: basisInterpolations(:)
    INTEGER(INTG), INTENT(OUT) :: err

    !Locals
    INTEGER(C_INT) :: length, connectivityHandle, layoutHandle
    CHARACTER(LEN=BUFFER_SIZE) :: name
    
    IF( .NOT. FieldmlInput_IsKnownBasis( parseHandle, meshHandle, objectHandle, err ) ) THEN
      RETURN
    ENDIF

    length = Fieldml_CopyImportRemoteName( parseHandle, objectHandle, name, BUFFER_SIZE )

    IF( Fieldml_GetObjectType( parseHandle, objectHandle ) /= FHT_CONTINUOUS_IMPORT ) THEN
      err = FML_ERR_INVALID_BASIS
      RETURN
    ENDIF

    IF( INDEX( name, 'library.fem.triquadratic_lagrange') == 1 ) THEN
      CALL REALLOCATE_INT( basisInterpolations, 3, "", err, errorString, *999 )
      basisInterpolations = BASIS_QUADRATIC_LAGRANGE_INTERPOLATION
      basisType = BASIS_LAGRANGE_HERMITE_TP_TYPE
    ELSE IF( INDEX( name, 'library.fem.trilinear_lagrange') == 1 ) THEN
      CALL REALLOCATE_INT( basisInterpolations, 3, "", err, errorString, *999 )
      basisInterpolations = BASIS_LINEAR_LAGRANGE_INTERPOLATION
      basisType = BASIS_LAGRANGE_HERMITE_TP_TYPE
    ELSE
      err = FML_ERR_UNKNOWN_BASIS
      RETURN
    ENDIF
    
    CALL FieldmlInput_GetBasisConnectivityInfo( parseHandle, meshHandle, objectHandle, connectivityHandle, layoutHandle, err )
    IF( connectivityHandle == FML_INVALID_HANDLE ) THEN
      err = FML_ERR_INVALID_BASIS
      RETURN
    ENDIF

999 RETURN
    !Deliberately not finalized, so the user can make OpenCMISS-specific tweaks.
  END SUBROUTINE FieldmlInput_GetBasisInfo

  !
  !================================================================================================================================
  !

  FUNCTION FieldmlInput_IsKnownBasis( parseHandle, meshHandle, objectHandle, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle
    INTEGER(C_INT), INTENT(IN) :: meshHandle
    INTEGER(C_INT), INTENT(IN) :: objectHandle
    INTEGER(INTG), INTENT(OUT) :: err
    
    !Function
    LOGICAL :: FieldmlInput_IsKnownBasis

    !Locals
    INTEGER(C_INT) :: length, connectivityHandle, layoutHandle
    CHARACTER(LEN=BUFFER_SIZE) :: name
    
    FieldmlInput_IsKnownBasis = .FALSE.

    length = Fieldml_CopyImportRemoteName( parseHandle, objectHandle, name, BUFFER_SIZE )

    IF( Fieldml_GetObjectType( parseHandle, objectHandle ) /= FHT_CONTINUOUS_IMPORT ) THEN
      err = FML_ERR_INVALID_BASIS
      RETURN
    ENDIF

    IF( ( INDEX( name, 'library.fem.triquadratic_lagrange') /= 1 ) .AND. &
      & ( INDEX( name, 'library.fem.trilinear_lagrange') /= 1 ) ) THEN
      err = FML_ERR_UNKNOWN_BASIS
      RETURN
    ENDIF
    
    CALL FieldmlInput_GetBasisConnectivityInfo( parseHandle, meshHandle, objectHandle, connectivityHandle, layoutHandle, err )
    IF( connectivityHandle == FML_INVALID_HANDLE ) THEN
      err = FML_ERR_INVALID_BASIS
      RETURN
    ENDIF
    
    FieldmlInput_IsKnownBasis = .TRUE.
    
  END FUNCTION FieldmlInput_IsKnownBasis
  
  !
  !================================================================================================================================
  !

  SUBROUTINE FieldmlInput_GetBasisHandles( parseHandle, meshHandle, bases, err )
    !Argument variables
    TYPE(C_PTR), INTENT(IN) :: parseHandle !<The parse handle
    INTEGER(C_INT), INTENT(IN) :: meshHandle
    INTEGER(C_INT), ALLOCATABLE, INTENT(INOUT) :: bases(:) !<An array to hold the identified bases
    INTEGER(INTG), INTENT(OUT) :: err !<The error code

    !Local variables
    INTEGER(INTG) :: basisCount
    INTEGER(C_INT) :: objectHandle, i, count

    count = Fieldml_GetObjectCount( parseHandle, FHT_CONTINUOUS_IMPORT )
    basisCount = 0

    DO i = 1, count
      objectHandle = Fieldml_GetObjectHandle( parseHandle, FHT_CONTINUOUS_IMPORT, i )

      IF( .NOT. FieldmlInput_IsKnownBasis( parseHandle, meshHandle, objectHandle, err ) ) THEN
        CYCLE
      ENDIF
      
      basisCount = basisCount + 1
      CALL GROW_ARRAY( bases, 1, "", err, errorString, *999 )
      bases( basisCount ) = objectHandle

999   CYCLE
    ENDDO

  END SUBROUTINE FieldmlInput_GetBasisHandles

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
    INTEGER(INTG) :: componentHandle, length, elementHandle, count, i, handle, nodeDomain, j, foundDomain
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
      
      foundDomain = .FALSE.
      DO j = 1, SIZE( nodeDomains )
        IF( nodeDomains( j ) == nodeDomain ) THEN
          foundDomain = .TRUE.
        END IF
      END DO
      
      IF( .NOT. foundDomain ) THEN
        CALL GROW_ARRAY( nodeDomains, 1, "", err, errorString, *999 )
        nodeDomains( SIZE( nodeDomains ) ) = nodeDomain
      END IF

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

END MODULE FIELDML_INPUT_ROUTINES
