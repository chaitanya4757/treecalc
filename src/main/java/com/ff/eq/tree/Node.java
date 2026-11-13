package com.ff.eq.tree;

import com.ff.eq.exception.Errors;
import java.util.Map;

public abstract class Node {

    public abstract double eval(Map<String, Double> vars); // calculate the value
    public abstract String infix();                        // inorder traversal
    public abstract String postfix();                      // postorder traversal

    public static int prec(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        if (op == '^') return 4;
        return 0;
    }

    static String format(double d) {
        if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    public static class Num extends Node {
        private final double value;

        public Num(double value) { this.value = value; }

        public double eval(Map<String, Double> vars) { return value; }
        public String infix() { return format(value); }
        public String postfix() { return format(value); }
    }

    public static class Var extends Node {
        private final String name;

        public Var(String name) { this.name = name; }

        public double eval(Map<String, Double> vars) {
            Double value = vars.get(name);
            if (value == null) {
                throw new Errors.MissingVariable("Missing value for variable '" + name + "'");
            }
            return value;
        }
        public String infix() { return name; }
        public String postfix() { return name; }
    }

    public static class Neg extends Node {
        private final Node child;

        public Neg(Node child) { this.child = child; }

        public double eval(Map<String, Double> vars) { return -child.eval(vars); }

        public String infix() {
            if (child instanceof Bin) {
                return "-(" + child.infix() + ")";
            }
            return "-" + child.infix();
        }
        public String postfix() { return child.postfix() + " neg"; }
    }

    public static class Bin extends Node {
        private final char op;
        private final Node left;
        private final Node right;

        public Bin(char op, Node left, Node right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        public double eval(Map<String, Double> vars) {
            double l = left.eval(vars);
            double r = right.eval(vars);
            if (op == '+') return l + r;
            if (op == '-') return l - r;
            if (op == '*') return l * r;
            if (op == '^') return Math.pow(l, r);
            // only '/' is left
            if (r == 0) {
                throw new Errors.InvalidEquation("Division by zero");
            }
            return l / r;
        }

        public String infix() {
            return wrap(left, false) + " " + op + " " + wrap(right, true);
        }

        public String postfix() {
            return left.postfix() + " " + right.postfix() + " " + op;
        }

        private String wrap(Node child, boolean isRightChild) {
            String text = child.infix();
            if (!(child instanceof Bin)) {
                return text;
            }
            int childPrec = prec(((Bin) child).op);
            int myPrec = prec(op);
            boolean needBrackets = false;
            if (childPrec < myPrec) {
                needBrackets = true;
            } else if (childPrec == myPrec) {
                needBrackets = (op == '^') ? !isRightChild : isRightChild;
            }
            return needBrackets ? "(" + text + ")" : text;
        }
    }
}