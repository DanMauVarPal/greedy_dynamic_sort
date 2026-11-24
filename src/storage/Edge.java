package storage;

/** One directed arc between two vertex indices; an undirected edge is stored as two arcs. */
public record Edge(int from, int to, int weight) {}
