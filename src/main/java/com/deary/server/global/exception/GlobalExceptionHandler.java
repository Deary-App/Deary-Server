package com.deary.server.global.exception;

import com.deary.server.global.exception.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorDto> handleCustomException(CustomException exception, HttpServletRequest request) {
		ErrorCode errorCode = exception.getErrorCode();
		log.warn("비즈니스 예외 발생: code={}, path={}", errorCode.getCode(), request.getRequestURI());
		return toResponse(errorCode, errorCode.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse(CommonErrorCode.INVALID_INPUT.getMessage());
		return toResponse(CommonErrorCode.INVALID_INPUT, message, request);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorDto> handleHandlerMethodValidation(
			HandlerMethodValidationException exception,
			HttpServletRequest request
	) {
		String message = exception.getAllErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse(CommonErrorCode.INVALID_INPUT.getMessage());
		return toResponse(CommonErrorCode.INVALID_INPUT, message, request);
	}

	@ExceptionHandler({
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class
	})
	public ResponseEntity<ErrorDto> handleInvalidParameter(Exception exception, HttpServletRequest request) {
		return toResponse(CommonErrorCode.INVALID_INPUT, CommonErrorCode.INVALID_INPUT.getMessage(), request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDto> handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return toResponse(
				CommonErrorCode.INVALID_REQUEST_BODY,
				CommonErrorCode.INVALID_REQUEST_BODY.getMessage(),
				request
		);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorDto> handleMethodNotSupported(
			HttpRequestMethodNotSupportedException exception,
			HttpServletRequest request
	) {
		return toResponse(
				CommonErrorCode.METHOD_NOT_ALLOWED,
				CommonErrorCode.METHOD_NOT_ALLOWED.getMessage(),
				request
		);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorDto> handleNoResourceFound(NoResourceFoundException exception, HttpServletRequest request) {
		return toResponse(
				CommonErrorCode.RESOURCE_NOT_FOUND,
				CommonErrorCode.RESOURCE_NOT_FOUND.getMessage(),
				request
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> handleException(Exception exception, HttpServletRequest request) {
		log.error("예상하지 못한 예외 발생: path={}", request.getRequestURI(), exception);
		return toResponse(
				CommonErrorCode.INTERNAL_SERVER_ERROR,
				CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
				request
		);
	}

	private ResponseEntity<ErrorDto> toResponse(ErrorCode errorCode, String message, HttpServletRequest request) {
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ErrorDto.of(errorCode, message, request.getRequestURI()));
	}

}
