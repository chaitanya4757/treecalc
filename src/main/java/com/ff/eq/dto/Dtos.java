package com.ff.eq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

// Request and response objects of the API
public class Dtos {

    public record StoreRequest(
            @NotBlank @Schema(example = "3x + 2y - z") String equation) {}

    public record StoreResponse(String message, String equationId) {}

    public record EquationItem(
            String equationId,
            @Schema(example = "3 * x + 2 * y - z") String equation,
            @Schema(example = "3 x * 2 y * + z -") String postfix) {}

    public record ListResponse(List<EquationItem> equations) {}

    public record EvaluateRequest(
            @Schema(example = "{\"x\":2,\"y\":3,\"z\":1}") Map<String, Double> variables) {}

    public record EvaluateResponse(
            String equationId, String equation, Map<String, Double> variables, double result) {}

    public record ErrorResponse(int status, String error) {}
}