package parser;

import java.util.*;

public class States implements Iterable<State> {
    private Set<State> stateSet;
    private List<State> states;
    private int id = 0;

    public States() {
        this.stateSet = new HashSet<>();
        this.states = new ArrayList<>();
    }

    public int addState(State state) {
        int name = this.id;
        state.setName(name);
        this.states.add(state);
        this.stateSet.add(state);
        this.id++;
        return name;
    }

    public State getState(State state) {
        for (State s : this.states) {
            if (s.equals(state)) return s;
        }
        return null;
    }

    public Iterator<State> iterator() {
        return this.states.iterator();
    }

    public State getState(int name) {
        if (name >= states.size()) {
            return null;
        }
        return states.get(name);
    }

    public int size() {
        return this.states.size();
    }

    public boolean contains(State state) {
        return this.stateSet.contains(state);
    }

    @Override
    public String toString() {
        return states.toString();
    }

}
