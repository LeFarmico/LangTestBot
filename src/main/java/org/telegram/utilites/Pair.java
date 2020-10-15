package org.telegram.utilites;

public final class Pair<T, U> {

    public Pair(T first, U second) {
        this.second = second;
        this.first = first;
    }

    public final T first;
    public final U second;

    public static <T, U> org.telegram.utilites.Pair<T, U> pair(T first, U second) {
        return new org.telegram.utilites.Pair<T, U>(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
