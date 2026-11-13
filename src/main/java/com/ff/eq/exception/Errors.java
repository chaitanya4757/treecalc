package com.ff.eq.exception;
public class Errors {
  public static class InvalidEquation extends RuntimeException { public InvalidEquation(String m){super(m);} }
  public static class MissingVariable extends RuntimeException { public MissingVariable(String m){super(m);} }
  public static class NotFound extends RuntimeException { public NotFound(String m){super(m);} }
}
