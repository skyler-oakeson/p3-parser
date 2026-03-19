/*
 * Look for TODO comments in this file for suggestions on how to implement
 * your parser.
 */
package parser;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import lexer.ExprLexer;
import lexer.ParenLexer;
import lexer.SimpleLexer;
import lexer.TinyLexer;
import org.antlr.v4.runtime.*;

import static parser.Util.EOF;
import static parser.Util.EPSILON;

/**
 *
 */
public class Parser {

  final Grammar grammar;

  /**
   * All states in the parser.
   */
  private final States states;

  /**
   * Action table for bottom-up parsing. Accessed as
   * actionTable.get(state).get(terminal). You may replace
   * the Integer with a State class if you choose.
   */
  private final HashMap<Integer, HashMap<String, Action>> actionTable;
  /**
   * Goto table for bottom-up parsing. Accessed as gotoTable.get(state).get(nonterminal).
   * You may replace the Integers with State classes if you choose.
   */
  private final HashMap<Integer, HashMap<String, Integer>> gotoTable;

  public Parser(String grammarFilename) throws IOException {
    gotoTable = new HashMap<>();
    actionTable = new HashMap<>();
    states = new States();

    grammar = new Grammar(grammarFilename);
    Item kernal = new Item(grammar.startRule, 0, EOF);
    Item end = new Item(grammar.startRule, 1, EOF);
    State start = computeClosure(kernal, grammar);
    Stack<State> stack = new Stack();
    stack.push(start);
    states.addState(start);

    // calculate states and fill tables
    // this code is really bad and I'd love to do this differently
    // this is what I have though and this is what I will continue with
    // one day I will rewrite this in a non-shitty language
    // where I can control the architecture I want to get rid of the
    // stateful programming so badly but it is way too much work
    while (!stack.isEmpty()) {
      State state = stack.pop();

      HashMap<String, Action> actions = new HashMap<>();
      HashMap<String, Integer> gotos = new HashMap<>();

      for (Item item: state) {
        if (item.getNextSymbol() == null) {
          Rule rule = item.getRule();
          String a = item.getA();
          actions.put(a, Action.createReduce(rule));
        }
      }

      for (String sym : grammar.symbols) {
        State transition = GOTO(state, sym, grammar);
        if (transition.isEmpty()) continue;

        int transitionId;
        if (!states.contains(transition)) {
          // New state: add it and push for processing
          transitionId = states.addState(transition);
          stack.push(transition);
        } else {
          // Already exists: just look up its id
          transitionId = states.getState(transition).getName();
        }

        // record the shift or goto
        if (grammar.terminals.contains(sym)) {
          actions.put(sym, Action.createShift(transitionId));
        } else {
          gotos.put(sym, transitionId);
        }
      }

      if (state.contains(end)) {
        actions.put(EOF, Action.createAccept());
      }

      this.actionTable.put(state.getName(), actions);
      this.gotoTable.put(state.getName(), gotos);
    }
  }

  public States getStates() {
    return states;
  }

  static public State computeClosure(Item I, Grammar grammar) {
    // --- initial item ---
    // [x -> alpha ● Y Beta, a]

    // --- B-productions ---
    // [Y -> y, b] where b = First(Beta, a)

    // Derive B-productions from the initial item
    // by recursively adding rules until nothing
    // can be added

    State closure = new State();
    Stack<Item> stack = new Stack<Item>();
    stack.push(I);
    while (!stack.isEmpty()) {
      Item item = stack.pop();
      Rule r = item.getRule();
      String a = item.getLookahead();
      String Y = item.getNextSymbol();
      String beta = item.getNextNextSymbol();
      closure.addItem(item);

      // if the next symbol is a non-terminal
      // add all rules beginning with that symbol
      // to the stack
      if (grammar.nonterminals.contains(Y)) {
        HashSet<String> firstSet = grammar.first.get(beta);

        if (firstSet == null) {
          firstSet = new HashSet<String>();
          firstSet.add(a);
        }

        if (firstSet.contains(EPSILON)) {
          firstSet.add(a);
        }

        for (Rule rule: grammar.nt2rules.get(Y)) {
          for (String b : firstSet) {
            Item newItem = new Item(rule, 0, b);
            if (!closure.contains(newItem) && !stack.contains(newItem)) {
              stack.push(newItem);
            }
          }
        }
      }
    }

    return closure;
  }

  //   This returns a new state that represents the transition from
  //   the given state on the symbol X.
  static public State GOTO(State state, String X, Grammar grammar) {
    State ret = new State();
    List<Item> items = state.canTransitionOnX(X);

    for (Item item: items) {
      ret.merge(computeClosure(new Item(item.getRule(), item.getDot()+1, item.getA()), grammar));
    }

    return ret;
  }

  // TODO: Implement this method
  // You will want to use StringBuilder. Another useful method will be String.format: for
  // printing a value in the table, use
  //   String.format("%8s", value)
  // How much whitespace you have shouldn't matter with regard to the tests, but it will
  // help you debug if you can format it nicely.
  public String actionTableToString() {
      StringBuilder builder = new StringBuilder();

      // Collect all terminals across all states for consistent column headers
      Set<String> allTerminals = new LinkedHashSet<>();
      for (HashMap<String, Action> row : actionTable.values()) {
        allTerminals.addAll(row.keySet());
      }

      // Header row: print each terminal as a column
      builder.append(String.format("%8s", "state"));
      for (String terminal : allTerminals) {
        builder.append(String.format("%8s", terminal));
      }
      builder.append("\n");

      // One row per state
      for (Map.Entry<Integer, HashMap<String, Action>> entry : actionTable.entrySet()) {
        int state = entry.getKey();
        HashMap<String, Action> row = entry.getValue();

        builder.append(String.format("%8d", state));
        for (String terminal : allTerminals) {
          Action action = row.get(terminal);
          builder.append(String.format("%8s", action != null ? action.toString() : ""));
        }
        builder.append("\n");
      }

      return builder.toString();
  }

  // TODO: Implement this method
  // You will want to use StringBuilder. Another useful method will be String.format: for
  // printing a value in the table, use
  //   String.format("%8s", value)
  // How much whitespace you have shouldn't matter with regard to the tests, but it will
  // help you debug if you can format it nicely.
  public String gotoTableToString() {
    StringBuilder builder = new StringBuilder();
    return builder.toString();
  }

  // TODO: Implement this method
  // You should return a list of the actions taken.
  public List<Action> parse(Lexer scanner) throws ParserException {
    // tokens is the output from the scanner. It is the list of tokens
    // scanned from the input file.
    // To get the token type: v.getSymbolicName(t.getType())
    // To get the token lexeme: t.getText()
    List<Action> actions = new ArrayList<>();
    ArrayList<? extends Token> tokens = new ArrayList<>(scanner.getAllTokens());
    Vocabulary v = scanner.getVocabulary();

    ArrayList<String> input = new ArrayList<>();
    Collections.reverse(tokens);
    input.add(Util.EOF);
    for (Token t : tokens) {
      input.add(0, v.getSymbolicName(t.getType()));
    }
    Collections.reverse(tokens);

    Stack<Integer> stack = new Stack<>();
    stack.push(0);
    int ip = 0;
    while (true) {
      if (input.get(ip) == null) {
        throw ParserException.create(tokens, ip);
      }

      if (stack.peek() == null) {
        throw ParserException.create(tokens, ip);
      }

      Integer s = stack.peek();
      String a = input.get(ip);

      if (actionTable.get(s).get(a) == null) {
        throw ParserException.create(tokens, ip);
      }

      Action act = actionTable.get(s).get(a);
      System.out.format("ACTION[%s, %s] = %s \n", s, a, act.toString());

      if (act.isShift()) {
        // push t onto the stack
        stack.push(act.getState());

        // advance input pointer
        ip++;
        actions.add(act);
      } else if (act.isReduce()) {
        // pop number of symbols in the rule that is matched
        for (int i = 0; i < act.getRule().getRhs().size(); i++) {
          stack.pop();
        }


        // let state t now be on top of the stack
        int t = stack.peek();

        // push GOTO[t, A] onto the stack
        String A = act.getRule().getLhs();
        stack.push(gotoTable.get(t).get(A));

        actions.add(act);
      } else if (act.isAccept()) {
        break;
      } else {
        throw ParserException.create(tokens, ip);
      }
    }



//    System.out.println(input);
    return actions;
  }

  //-------------------------------------------------------------------
  // Convenience functions
  //-------------------------------------------------------------------

  public List<Action> parseFromFile(String filename) throws IOException, ParserException {
//    System.out.println("\nReading input file " + filename + "\n");
    final CharStream charStream = CharStreams.fromFileName(filename);
    Lexer scanner = scanFile(charStream);
    return parse(scanner);
  }

  public List<Action> parseFromString(String program) throws ParserException {
    Lexer scanner = scanFile(CharStreams.fromString(program));
    return parse(scanner);
  }

  private Lexer scanFile(CharStream charStream) {
    // We use ANTLR's scanner (lexer) to produce the tokens.
    Lexer scanner = null;
    switch (grammar.grammarName) {
      case "Simple":
        scanner = new SimpleLexer(charStream);
        break;
      case "Paren":
        scanner = new ParenLexer(charStream);
        break;
      case "Expr":
        scanner = new ExprLexer(charStream);
        break;
      case "Tiny":
        scanner = new TinyLexer(charStream);
        break;
      default:
        System.out.println("Unknown scanner");
        break;
    }

    return scanner;
  }

}
