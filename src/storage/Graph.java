package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Weighted graph ADT, directed or undirected, as an adjacency list.
 * Vertices are named; each name maps to an index {@code 0..V-1} in first-seen order.
 */
public final class Graph {
    private final boolean directed;
    private final Map<String, Integer> indexByName = new HashMap<>();
    private final List<String> names = new ArrayList<>();
    private final List<List<Edge>> adjacency = new ArrayList<>();
    private final List<Edge> arcs = new ArrayList<>(); // flat copy of adjacency, for edge sweeps
    private int edgeCount;

    public Graph(boolean directed) {
        this.directed = directed;
    }

    /**
     * Loads a graph from a text file: an optional run of comment ({@code #}) and blank
     * lines, a {@code directed} or {@code undirected} header, then one
     * {@code from, to, weight} line per edge.
     *
     * @throws IOException on an unreadable file
     * @throws IllegalArgumentException with message {@code "<file> line N: <reason>"} on bad content
     */
    public static Graph load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        Graph graph = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String where = file + " line " + (i + 1) + ": ";
            if (graph == null) {
                graph = switch (line) {
                    case "directed" -> new Graph(true);
                    case "undirected" -> new Graph(false);
                    default -> throw new IllegalArgumentException(
                            where + "expected 'directed' or 'undirected', got '" + line + "'");
                };
                continue;
            }
            String[] fields = line.split(",", -1);
            if (fields.length != 3) {
                throw new IllegalArgumentException(where + "expected 'from, to, weight', got '" + line + "'");
            }
            String from = fields[0].strip();
            String to = fields[1].strip();
            if (from.isEmpty() || to.isEmpty()) {
                throw new IllegalArgumentException(where + "empty vertex name in '" + line + "'");
            }
            int weight;
            try {
                weight = Integer.parseInt(fields[2].strip());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(where + "weight is not an integer: '" + fields[2].strip() + "'");
            }
            graph.addEdge(from, to, weight);
        }
        if (graph == null) {
            throw new IllegalArgumentException(file + ": missing 'directed' or 'undirected' header");
        }
        return graph;
    }

    /** Adds an edge, creating unseen vertices; an undirected graph stores two arcs. */
    public void addEdge(String from, String to, int weight) {
        int u = vertex(from);
        int v = vertex(to);
        addArc(new Edge(u, v, weight));
        if (!directed) addArc(new Edge(v, u, weight));
        edgeCount++;
    }

    public boolean isDirected() {
        return directed;
    }

    public int vertexCount() {
        return names.size();
    }

    /** Edges as written in the file (an undirected edge counts once), not arcs. */
    public int edgeCount() {
        return edgeCount;
    }

    /** Index of the named vertex, or -1 if absent. */
    public int indexOf(String name) {
        return indexByName.getOrDefault(name, -1);
    }

    public String nameOf(int index) {
        return names.get(index);
    }

    /** Vertex names in first-seen order. */
    public List<String> names() {
        return Collections.unmodifiableList(names);
    }

    /** Outgoing arcs of one vertex. */
    public List<Edge> neighbors(int index) {
        return Collections.unmodifiableList(adjacency.get(index));
    }

    /** Every arc in the graph, for algorithms that sweep all edges. */
    public List<Edge> edges() {
        return Collections.unmodifiableList(arcs);
    }

    private int vertex(String name) {
        return indexByName.computeIfAbsent(name, n -> {
            names.add(n);
            adjacency.add(new ArrayList<>());
            return names.size() - 1;
        });
    }

    private void addArc(Edge arc) {
        adjacency.get(arc.from()).add(arc);
        arcs.add(arc);
    }
}
