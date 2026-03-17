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
    actionTable = new HashMap<>();
    gotoTable = new HashMap<>();

    grammar = new Grammar(grammarFilename);

    states = new States();

    // TODO: Call methods to compute the states and parsing tables here.
    State start = computeClosure(new Item(grammar.startRule, 0, EOF), grammar);
    System.out.println(start);
    Stack<State> stack = new Stack();
    stack.push(start);

    while (!stack.isEmpty()) {
      State state = stack.pop();
      states.addState(state);
      for (String sym: grammar.symbols) {
        State transition = GOTO(state, sym, this.grammar);
        if (!states.contains(transition) && !transition.isEmpty()) {
          stack.push(transition);
          states.addState(transition);
        }
      }
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

  // TODO: Implement this method.
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
    ArrayList<? extends Token> tokens = new ArrayList<>(scanner.getAllTokens());
    Vocabulary v = scanner.getVocabulary();

    Stack<String> input = new Stack<>();
    Collections.reverse(tokens);
    input.add(Util.EOF);
    for (Token t : tokens) {
      input.push(v.getSymbolicName(t.getType()));
    }
    Collections.reverse(tokens);
//    System.out.println(input);

    // TODO: Parse the tokens. On an error, throw a ParseException, like so:
    //    throw ParserException.create(tokens, i)
    List<Action> actions = new ArrayList<>();
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
