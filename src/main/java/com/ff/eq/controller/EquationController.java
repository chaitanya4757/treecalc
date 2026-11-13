package com.ff.eq.controller;
import com.ff.eq.dto.Dtos.*;
import com.ff.eq.service.EquationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/equations") @Tag(name = "Equations")
public class EquationController {
  private final EquationService svc;
  public EquationController(EquationService s){ this.svc=s; }

  @Operation(summary = "Store an equation (converted to a postfix expression tree)")
  @PostMapping("/store")
  public StoreResponse store(@Valid @RequestBody StoreRequest r){ return new StoreResponse("Equation stored successfully", svc.store(r.equation())); }

  @Operation(summary = "List all stored equations in infix form (inorder traversal of each tree)")
  @GetMapping
  public ListResponse list(){ return new ListResponse("Equations retrieved successfully", svc.list()); }

  @Operation(summary = "Evaluate a stored equation with variable values")
  @PostMapping("/{id}/evaluate")
  public EvaluateResponse evaluate(@PathVariable long id, @RequestBody EvaluateRequest r){
    return new EvaluateResponse("Equation evaluated successfully", id, svc.evaluate(id, r.variables()));
  }
}
