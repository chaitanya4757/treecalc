package com.ff.eq;
import com.ff.eq.exception.Errors.*;
import com.ff.eq.parser.EquationParser;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EquationTest {
  Map<String,Double> v = Map.of("x",2.0,"y",3.0,"z",1.0);
  @Test void postfixAndInfix(){
    var n = EquationParser.parse("3x + 2y - z");
    assertEquals("3 x * 2 y * + z -", n.postfix());
    assertEquals("3 * x + 2 * y - z", n.infix());
    assertEquals(11.0, n.eval(v));
  }
  @Test void precedence(){
    assertEquals(14.0, EquationParser.parse("2 + 3 * 4").eval(Map.of()));
    assertEquals(20.0, EquationParser.parse("(2 + 3) * 4").eval(Map.of()));
    assertEquals(512.0, EquationParser.parse("2^3^2").eval(Map.of()));
    assertEquals(-4.0, EquationParser.parse("-2^2").eval(Map.of()));
    assertEquals(3.0, EquationParser.parse("x - -1").eval(Map.of("x",2.0)));
    assertEquals(8.0, EquationParser.parse("2(x)").eval(Map.of("x",4.0)));
  }
  @Test void infixKeepsNeededParens(){
    assertEquals("(2 + 3) * 4", EquationParser.parse("(2+3)*4").infix());
    assertEquals("a - (b - c)", EquationParser.parse("a-(b-c)").infix());
  }
  @Test void errors(){
    for(String s: new String[]{"","(2+3","2+3)","3 ++* x","2 +","2 $ 3"}) assertThrows(InvalidEquation.class, () -> EquationParser.parse(s), s);
    assertThrows(MissingVariable.class, () -> EquationParser.parse("x+q").eval(Map.of("x",1.0)));
    assertThrows(InvalidEquation.class, () -> EquationParser.parse("1/0").eval(Map.of()));
  }
}
