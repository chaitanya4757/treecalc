package com.ff.eq.parser;
import com.ff.eq.exception.Errors.InvalidEquation;
import com.ff.eq.tree.Node;
import java.util.*;

/** Tokenizer -> shunting-yard (postfix) -> expression tree. */
public final class EquationParser {
  private EquationParser(){}

  public static Node parse(String s){ return build(toPostfix(tokenize(s))); }

  static List<String> tokenize(String s){
    if(s==null||s.isBlank()) throw new InvalidEquation("Equation must not be empty");
    List<String> t = new ArrayList<>(); int i=0;
    while(i<s.length()){
      char c=s.charAt(i);
      if(Character.isWhitespace(c)){ i++; continue; }
      String tok;
      if(Character.isDigit(c)||c=='.'){ int j=i; while(j<s.length()&&(Character.isDigit(s.charAt(j))||s.charAt(j)=='.')) j++; tok=s.substring(i,j); i=j;
        try{ Double.parseDouble(tok);}catch(NumberFormatException e){ throw new InvalidEquation("Invalid number '"+tok+"'"); } }
      else if(Character.isLetter(c)){ int j=i; while(j<s.length()&&Character.isLetter(s.charAt(j))) j++; tok=s.substring(i,j); i=j; }
      else if("+-*/^()".indexOf(c)>=0){ tok=String.valueOf(c); i++; }
      else throw new InvalidEquation("Unexpected character '"+c+"' at position "+i);
      if(!t.isEmpty() && implicitBefore(t.get(t.size()-1), tok)) t.add("*");
      t.add(tok);
    }
    return t;
  }
  private static boolean isOperand(String x){ return Character.isLetterOrDigit(x.charAt(0))||x.charAt(0)=='.'; }
  private static boolean implicitBefore(String prev,String cur){
    boolean left = isOperand(prev)||prev.equals(")");
    boolean right = isOperand(cur)||cur.equals("(");
    return left&&right;
  }

  static List<String> toPostfix(List<String> toks){
    List<String> out=new ArrayList<>(); Deque<String> ops=new ArrayDeque<>();
    boolean expectOperand=true;
    for(String t: toks){
      char c=t.charAt(0);
      if(isOperand(t)){ if(!expectOperand) throw new InvalidEquation("Unexpected operand '"+t+"'"); out.add(t); expectOperand=false; }
      else if(c=='('){ if(!expectOperand) throw new InvalidEquation("Unexpected '('"); ops.push("("); }
      else if(c==')'){
        if(expectOperand) throw new InvalidEquation("Unexpected ')'");
        while(!ops.isEmpty() && !ops.peek().equals("(")) out.add(ops.pop());
        if(ops.isEmpty()) throw new InvalidEquation("Unbalanced parentheses");
        ops.pop();
      } else if(expectOperand){
        if(c=='-') ops.push("neg"); else if(c=='+') {} else throw new InvalidEquation("Unexpected operator '"+t+"'");
      } else {
        while(!ops.isEmpty() && !ops.peek().equals("(") && (p(ops.peek())>p(t) || (p(ops.peek())==p(t) && c!='^'))) out.add(ops.pop());
        ops.push(t); expectOperand=true;
      }
    }
    if(expectOperand) throw new InvalidEquation("Equation ends unexpectedly");
    while(!ops.isEmpty()){ String o=ops.pop(); if(o.equals("(")) throw new InvalidEquation("Unbalanced parentheses"); out.add(o); }
    return out;
  }
  // unary minus binds tighter than * / but looser than ^
  private static int p(String o){ return o.equals("neg") ? 3 : Node.prec(o.charAt(0)); }

  static Node build(List<String> pf){
    Deque<Node> st=new ArrayDeque<>();
    for(String t: pf){
      if(t.equals("neg")) st.push(new Node.Neg(st.pop()));
      else if(isOperand(t)) st.push(Character.isLetter(t.charAt(0)) ? new Node.Var(t) : new Node.Num(Double.parseDouble(t)));
      else { Node r=st.pop(), l=st.pop(); st.push(new Node.Bin(t.charAt(0), l, r)); }
    }
    if(st.size()!=1) throw new InvalidEquation("Malformed equation");
    return st.pop();
  }
}
