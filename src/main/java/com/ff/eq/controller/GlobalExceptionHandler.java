package com.ff.eq.controller;
import com.ff.eq.dto.Dtos.ErrorResponse;
import com.ff.eq.exception.Errors.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler({InvalidEquation.class, MissingVariable.class})
  ResponseEntity<ErrorResponse> bad(RuntimeException e){ return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage())); }
  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  ResponseEntity<ErrorResponse> invalid(Exception e){ return ResponseEntity.badRequest().body(new ErrorResponse(400, "Invalid request body")); }
  @ExceptionHandler(NotFound.class)
  ResponseEntity<ErrorResponse> nf(NotFound e){ return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage())); }
}
