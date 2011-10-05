package com.gigaspaces.tutorials.common.dao;

public class Pair<K, V> {
    private final K k;
    private final V v;

    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getK() {

        return k;
    }

    public V getV() {
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (k != null ? !k.equals(pair.k) : pair.k != null) return false;
        if (v != null ? !v.equals(pair.v) : pair.v != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = k != null ? k.hashCode() : 0;
        result = 31 * result + (v != null ? v.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Pair");
        sb.append("{k=").append(k);
        sb.append(", v=").append(v);
        sb.append('}');
        return sb.toString();
    }
}
