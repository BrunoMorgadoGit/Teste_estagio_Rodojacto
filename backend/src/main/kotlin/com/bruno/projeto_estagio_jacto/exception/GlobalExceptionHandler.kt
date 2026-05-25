package com.bruno.projeto_estagio_jacto.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(
        exception: ResourceNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.NOT_FOUND, exception.message ?: "Resource not found", request.requestURI)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(
        exception: InvalidCredentialsException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.UNAUTHORIZED, exception.message ?: "Invalid credentials", request.requestURI)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(
        exception: BusinessException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.BAD_REQUEST, exception.message ?: "Business error", request.requestURI)

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicate(
        exception: DuplicateResourceException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.CONFLICT, exception.message ?: "Resource already exists", request.requestURI)

    @ExceptionHandler(ForbiddenOperationException::class)
    fun handleForbidden(
        exception: ForbiddenOperationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.FORBIDDEN, exception.message ?: "Access denied", request.requestURI)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        val details = exception.bindingResult.allErrors
            .filterIsInstance<FieldError>()
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }

        return ResponseEntity.badRequest().body(
            ApiErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Error",
                message = "Invalid request data",
                path = request.requestURI,
                fields = details,
            ),
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleIntegrityViolation(
        exception: DataIntegrityViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(
            HttpStatus.CONFLICT,
            "Operation cannot be completed because the record is referenced by other data",
            request.requestURI,
        )

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.requestURI)

    private fun buildResponse(
        status: HttpStatus,
        message: String,
        path: String,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(status).body(
            ApiErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = path,
            ),
        )
}
