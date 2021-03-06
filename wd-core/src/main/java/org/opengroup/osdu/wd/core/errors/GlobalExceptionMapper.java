// Copyright 2020 Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.wd.core.errors;

import javassist.NotFoundException;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.logging.ILogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ValidationException;
import java.nio.file.AccessDeniedException;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionMapper extends ResponseEntityExceptionHandler {

	@Autowired
	private ILogger log;

	@ExceptionHandler(AppException.class)
	protected ResponseEntity<Object> handleAppException(AppException e) {
		return this.getErrorResponse(e);
	}

	@ExceptionHandler(ValidationException.class)
	protected ResponseEntity<Object> handleValidationException(ValidationException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Validation error.", e.getMessage(), e));
	}

	@ExceptionHandler(NotFoundException.class)
	protected ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.NOT_FOUND.value(), "Resource not found.", e.getMessage(), e));
	}

	@ExceptionHandler(AccessDeniedException.class)
	protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.FORBIDDEN.value(), "Access denied", e.getMessage(), e));
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull HttpRequestMethodNotSupportedException e,
																		 @NonNull HttpHeaders headers,
																		 @NonNull HttpStatus status,
																		 @NonNull WebRequest request) {
		return this.getErrorResponse(
				new AppException(org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED, "Method not found.",
						"Method not found.", e));
	}

	public ResponseEntity<Object> getErrorResponse(AppException e) {

		String exceptionMsg = e.getOriginalException() != null
				? e.getOriginalException().getMessage()
				: e.getError().getMessage();

		if (e.getError().getCode() > 499) {
			this.log.error(exceptionMsg, e);
		} else {
			this.log.warning(exceptionMsg, e);
		}

		AppError error = new AppError(e.getError().getMessage());
		return new ResponseEntity<>(error, HttpStatus.resolve(e.getError().getCode()));
	}
}
