package Structs;

import java.util.Comparator;

public final class Interval<T extends Comparable<? super T>> {
    public final T from, to;
    public Interval(T from, T to) {
        if(from == null || to == null || from.compareTo(to) > 0) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.to = to;
    }
    public boolean intersect(Interval<T> b) {
        return !((from.compareTo(b.from) < 0 && to.compareTo(b.from) < 0 ) ||
                 (b.from.compareTo(from) < 0 && b.to.compareTo(from) < 0 ));
    }
    @Override
    public final String toString() {
        return "[ " + from + " ; " + to + " ]";
    }

    @Override
    public Interval<T> clone() {
        return new Interval<T>(from,to);
    }
}
