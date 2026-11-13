package com.ff.eq.tree;
import com.ff.eq.exception.Errors;
import java.util.Map;
/** Expression tree built from a postfix expression. */
public sealed interface Node {
  record Num(double v) implements Node {}
  record Var(String name) implements Node {}
  record Neg(Node child) implements Node {}
  record Bin(char op, Node l, Node r) implements Node {}

  static int prec(char op){ return switch(op){ case '+','-'->1; case '*','/'->2; case '^'->4; default->0; }; }

  default double eval(Map<String,Double> vars){
    return switch(this){
      case Num n -> n.v();
      case Var x -> { Double d = vars.get(x.name()); if(d==null) throw new Errors.MissingVariable("Missing value for variable '"+x.name()+"'"); yield d; }
      case Neg n -> -n.child().eval(vars);
      case Bin b -> { double l=b.l().eval(vars), r=b.r().eval(vars);
        yield switch(b.op()){ case '+'->l+r; case '-'->l-r; case '*'->l*r; case '^'->Math.pow(l,r);
          default -> { if(r==0) throw new Errors.InvalidEquation("Division by zero"); yield l/r; } }; }
    };
  }

  /** Inorder traversal with minimal parentheses. */
  default String infix(){
    return switch(this){
      case Num n -> fmt(n.v());
      case Var x -> x.name();
      case Neg n -> "-" + (n.child() instanceof Bin ? "("+n.child().infix()+")" : n.child().infix());
      case Bin b -> side(b.l(), b, false) + " " + b.op() + " " + side(b.r(), b, true);
    };
  }
  private static String side(Node c, Bin p, boolean right){
    String s = c.infix();
    if(c instanceof Bin cb){
      int cp=prec(cb.op()), pp=prec(p.op());
      boolean paren = cp<pp || (cp==pp && (p.op()=='^' ? !right : right));
      if(paren) return "("+s+")";
    }
    return s;
  }
  default String postfix(){
    return switch(this){
      case Num n -> fmt(n.v());
      case Var x -> x.name();
      case Neg n -> n.child().postfix()+" neg";
      case Bin b -> b.l().postfix()+" "+b.r().postfix()+" "+b.op();
    };
  }
  static String fmt(double d){ return d==Math.rint(d) && Math.abs(d)<1e15 ? String.valueOf((long)d) : String.valueOf(d); }
}
