package br.com.harmony.domain.validation;

public record ValidationError(String field, ValidationErrorCode code, String message, String rejectedValue) {}
