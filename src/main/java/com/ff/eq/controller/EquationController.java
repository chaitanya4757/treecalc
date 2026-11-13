package com.ff.eq.controller;

import com.ff.eq.dto.Dtos.*;
import com.ff.eq.service.EquationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equations")
@Tag(name = "Equations")
public class EquationController {

    private final EquationService service;

    public EquationController(EquationService service) {
        this.service = service;
    }

    @Operation(summary = "Store an equation (converted to a postfix expression tree)")
    @PostMapping("/store")
    public StoreResponse store(@Valid @RequestBody StoreRequest request) {
        long id = service.store(request.equation());
        return new StoreResponse("Equation stored successfully", String.valueOf(id));
    }

    @Operation(summary = "List all stored equations in infix form (inorder traversal of each tree)")
    @GetMapping
    public ListResponse list() {
        return new ListResponse(service.list());
    }

    @Operation(summary = "Evaluate a stored equation with variable values")
    @PostMapping("/{id}/evaluate")
    public EvaluateResponse evaluate(@PathVariable long id, @RequestBody EvaluateRequest request) {
        return service.evaluate(id, request.variables());
    }
}