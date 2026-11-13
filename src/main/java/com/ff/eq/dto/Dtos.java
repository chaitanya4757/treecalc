package com.ff.eq.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
public class Dtos {
  public record StoreRequest(@NotBlank @Schema(example = "3x + 2y - z") String equation) {}
  public record StoreResponse(String message, long equationId) {}
  public record EquationItem(long equationId, @Schema(example = "3 * x + 2 * y - z") String equation, @Schema(example = "3 x * 2 y * + z -") String postfix) {}
  public record ListResponse(String message, java.util.List<EquationItem> equations) {}
  public record EvaluateRequest(@Schema(example = "{\"x\":2,\"y\":3,\"z\":1}") Map<String, Double> variables) {}
  public record EvaluateResponse(String message, long equationId, double result) {}
  public record ErrorResponse(int status, String error) {}
}
