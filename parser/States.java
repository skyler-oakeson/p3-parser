package parser;

import java.util.*;

public class States {
    private Set<State> stateSet;
    private List<State> states;
    private int id = 0;

    public States() {
        this.stateSet = new HashSet<>();
        this.states = new ArrayList<>();
    }

    public void addState(State state) {
        state.setName(this.id);
        if (!stateSet.contains(state)) {
            this.id++;
            this.states.add(state);
            this.stateSet.add(state);
        }
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
