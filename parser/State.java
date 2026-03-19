package parser;

import java.util.*;

public class State implements Comparable<State>, Iterable<Item> {
    private Set<Item> itemSet;
    private List<Item> items;
    private int name;

    public State(int name) {
        this.itemSet = new HashSet<>();
        this.items = new ArrayList<>();
        this.name = name;
    }

    public boolean isEmpty() {
        return this.itemSet.isEmpty();
    }

    public State() {
        this(0);
    }

    public int getName() {
        return name;
    }

    public void merge(State state) {
        this.itemSet.addAll(state.itemSet);
        this.items = this.itemSet.stream().toList();
    }

    public Iterator<Item> iterator() {
        return this.items.iterator();
    }

    public List<Item> canTransitionOnX(String X) {
        List<Item> canTransition = new ArrayList<Item>();

        for (Item item: this.itemSet) {
            String nextSymbol = item.getNextSymbol();
            if (nextSymbol != null && nextSymbol.equals(X)) {
                canTransition.add(item);
            }
        }
        return canTransition;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int size() {
        return this.items.size();
    }

    public void addItem(Item item) {
        this.items.add(item);
        this.itemSet.add(item);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        ArrayList<Item> sortedList = new ArrayList<>(items);
        sortedList.sort(Comparator.comparingInt(Item::hashCode));
        for (Item item : sortedList) {
            hash = 37 * hash + Objects.hashCode(item);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final State other = (State) obj;
        if (items.size() != other.items.size()) {
            return false;
        }
        for (Item item : items) {
            if (!other.itemSet.contains(item)) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(Item item) {
        return this.itemSet.contains(item);
    }

    @Override
    public String toString() {
        return this.name + ": " + items.toString();
    }

    @Override
    public int compareTo(State o) {
        return new Integer(this.name).compareTo(new Integer(o.name));
    }

}
