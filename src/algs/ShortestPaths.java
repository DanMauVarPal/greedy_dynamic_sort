package algs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import storage.Graph;

/**
 * Result shared by both algorithms: for every vertex, the shortest distance from
 * {@code source} and the predecessor on that path ({@code -1} for the source and for
 * unreachable vertices). Printing code reads this and never needs to know which algorithm ran.
 */
public record ShortestPaths(Graph graph, int source, long[] dist, int[] prev) {
    public static final long UNREACHABLE = Long.MAX_VALUE;

    public boolean hasPathTo(String name) {
        return distanceTo(name) != UNREACHABLE;
    }

    /** Shortest distance to the named vertex, or {@link #UNREACHABLE}. */
    public long distanceTo(String name) {
        return dist[indexOf(name)];
    }

    /** Vertex names from the source to {@code name}, source first; empty if there is no path. */
    public List<String> pathTo(String name) {
        int v = indexOf(name);
        if (dist[v] == UNREACHABLE) return List.of();
        List<String> path = new ArrayList<>();
        for (; v != -1; v = prev[v]) path.add(graph.nameOf(v));
        Collections.reverse(path);
        return path;
    }

    private int indexOf(String name) {
        int v = graph.indexOf(name);
        if (v == -1) throw new IllegalArgumentException("unknown vertex: " + name);
        return v;
    }
}
