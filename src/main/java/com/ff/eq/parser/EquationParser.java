package com.ff.eq.parser;

import com.ff.eq.exception.Errors.InvalidEquation;
import com.ff.eq.tree.Node;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// Steps: text -> tokens -> postfix (shunting-yard) -> expression tree
public class EquationParser {

    public static Node parse(String text) {
        List<String> tokens = tokenize(text);
        List<String> postfix = toPostfix(tokens);
        return buildTree(postfix);
    }

    //split the text into numbers, variables, operators and brackets
    static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidEquation("Equation must not be empty");
        }
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            String token;

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            } else if (Character.isDigit(c) || c == '.') {
                int j = i;
                while (j < text.length() && (Character.isDigit(text.charAt(j)) || text.charAt(j) == '.')) {
                    j++;
                }
                token = text.substring(i, j);
                i = j;
                try {
                    Double.parseDouble(token);
                } catch (NumberFormatException e) {
                    throw new InvalidEquation("Invalid number '" + token + "'");
                }
            } else if (Character.isLetter(c)) {
                int j = i;
                while (j < text.length() && Character.isLetter(text.charAt(j))) {
                    j++;
                }
                token = text.substring(i, j);
                i = j;
            } else if ("+-*/^()".indexOf(c) >= 0) {
                token = String.valueOf(c);
                i++;
            } else {
                throw new InvalidEquation("Unexpected character '" + c + "' at position " + i);
            }

            if (!tokens.isEmpty() && needsMultiply(tokens.get(tokens.size() - 1), token)) {
                tokens.add("*");
            }
            tokens.add(token);
        }
        return tokens;
    }

    private static boolean isOperand(String token) {
        char c = token.charAt(0);
        return Character.isLetterOrDigit(c) || c == '.';
    }

    private static boolean needsMultiply(String previous, String current) {
        boolean leftSide = isOperand(previous) || previous.equals(")");
        boolean rightSide = isOperand(current) || current.equals("(");
        return leftSide && rightSide;
    }

    // Unary minus binds tighter than * and / but weaker than ^
    private static int precedence(String op) {
        if (op.equals("neg")) return 3;
        return Node.prec(op.charAt(0));
    }

    // shunting-yard algorithm
    static List<String> toPostfix(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        boolean expectOperand = true; 

        for (String token : tokens) {
            char c = token.charAt(0);

            if (isOperand(token)) {
                if (!expectOperand) {
                    throw new InvalidEquation("Unexpected operand '" + token + "'");
                }
                output.add(token);
                expectOperand = false;

            } else if (c == '(') {
                if (!expectOperand) {
                    throw new InvalidEquation("Unexpected '('");
                }
                stack.push("(");

            } else if (c == ')') {
                if (expectOperand) {
                    throw new InvalidEquation("Unexpected ')'");
                }
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new InvalidEquation("Unbalanced parentheses");
                }
                stack.pop(); 

            } else if (expectOperand) {
                if (c == '-') {
                    stack.push("neg");
                } else if (c != '+') {
                    throw new InvalidEquation("Unexpected operator '" + token + "'");
                }

            } else {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    int top = precedence(stack.peek());
                    int current = precedence(token);
                    boolean popIt = top > current || (top == current && c != '^');
                    if (!popIt) break;
                    output.add(stack.pop());
                }
                stack.push(token);
                expectOperand = true;
            }
        }

        if (expectOperand) {
            throw new InvalidEquation("Equation ends unexpectedly");
        }
        while (!stack.isEmpty()) {
            String op = stack.pop();
            if (op.equals("(")) {
                throw new InvalidEquation("Unbalanced parentheses");
            }
            output.add(op);
        }
        return output;
    }

    // build the tree from the postfix tokens using a stack
    static Node buildTree(List<String> postfix) {
        Deque<Node> stack = new ArrayDeque<>();
        for (String token : postfix) {
            if (token.equals("neg")) {
                stack.push(new Node.Neg(stack.pop()));
            } else if (isOperand(token)) {
                if (Character.isLetter(token.charAt(0))) {
                    stack.push(new Node.Var(token));
                } else {
                    stack.push(new Node.Num(Double.parseDouble(token)));
                }
            } else {
                Node right = stack.pop();
                Node left = stack.pop();
                stack.push(new Node.Bin(token.charAt(0), left, right));
            }
        }
        if (stack.size() != 1) {
            throw new InvalidEquation("Malformed equation");
        }
        return stack.pop();
    }
}