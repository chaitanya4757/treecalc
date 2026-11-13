package com.ff.eq.service;

import com.ff.eq.dto.Dtos.EquationItem;
import com.ff.eq.dto.Dtos.EvaluateResponse;
import com.ff.eq.exception.Errors.NotFound;
import com.ff.eq.parser.EquationParser;
import com.ff.eq.tree.Node;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class EquationService {

    // id -> expression tree (kept in memory)
    private final Map<Long, Node> equations = new TreeMap<>();
    private long nextId = 1;

    public synchronized long store(String equation) {
        Node tree = EquationParser.parse(equation);
        long id = nextId++;
        equations.put(id, tree);
        return id;
    }

    public synchronized List<EquationItem> list() {
        List<EquationItem> result = new ArrayList<>();
        for (Map.Entry<Long, Node> entry : equations.entrySet()) {
            Node tree = entry.getValue();
            result.add(new EquationItem(String.valueOf(entry.getKey()), tree.infix(), tree.postfix()));
        }
        return result;
    }

    public synchronized EvaluateResponse evaluate(long id, Map<String, Double> variables) {
        Node tree = equations.get(id);
        if (tree == null) {
            throw new NotFound("Equation " + id + " not found");
        }
        if (variables == null) {
            variables = Map.of();
        }
        double result = tree.eval(variables);
        return new EvaluateResponse(String.valueOf(id), tree.infix(), variables, result);
    }
}