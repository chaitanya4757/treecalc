package com.ff.eq.service;
import com.ff.eq.dto.Dtos.*;
import com.ff.eq.exception.Errors.NotFound;
import com.ff.eq.parser.EquationParser;
import com.ff.eq.tree.Node;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EquationService {
  private final Map<Long, Node> store = new ConcurrentSkipListMap<>();
  private final AtomicLong ids = new AtomicLong();

  public long store(String eq){ Node n = EquationParser.parse(eq); long id = ids.incrementAndGet(); store.put(id, n); return id; }
  public List<EquationItem> list(){ return store.entrySet().stream().map(e -> new EquationItem(e.getKey(), e.getValue().infix(), e.getValue().postfix())).toList(); }
  public double evaluate(long id, Map<String,Double> vars){
    Node n = store.get(id); if(n==null) throw new NotFound("Equation "+id+" not found");
    return n.eval(vars==null? Map.of(): vars);
  }
}
