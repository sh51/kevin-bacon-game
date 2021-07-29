import java.util.*;

/**
 * Library for graph analysis
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2016
 * @author Sihao Huang, implemented randomWalk, verticesByInDegree, verticesByOutDegree,
 * 			plus bfs related functions for Kevin Bacon game
 */
public class GraphLibExtended {
	public static double INF = Double.MAX_VALUE;
	/**
	 * Takes a random walk from a vertex, up to a given number of steps
	 * So a 0-step path only includes start, while a 1-step path includes start and one of its out-neighbors,
	 * and a 2-step path includes start, an out-neighbor, and one of the out-neighbor's out-neighbors
	 * Stops earlier if no step can be taken (i.e., reach a vertex with no out-edge)
	 * @param g		graph to walk on
	 * @param start	initial vertex (assumed to be in graph)
	 * @param steps	max number of steps
	 * @return		a list of vertices starting with start, each with an edge to the sequentially next in the list;
	 * 			    null if start isn't in graph
	 */
	public static <V,E> List<V> randomWalk(Graph<V,E> g, V start, int steps) throws Exception {
		if (!g.hasVertex(start)) throw new Exception("Start vertex not found.");

		V curr = start;
		List<V> walk = new ArrayList<>();
		int cnt = 0;

		while (curr != null && cnt <= steps) {
			walk.add(curr);
			List<V> outVertices = new ArrayList<>();
			g.outNeighbors(curr).forEach(outVertices::add);
			if (outVertices.size() == 0) curr = null;
			else curr = outVertices.get((int)(Math.random() * outVertices.size()));
			cnt++;
		}

		return walk;
	}
	
	/**
	 * Orders vertices in decreasing order by their in-degree
	 * @param g		graph
	 * @return		list of vertices sorted by in-degree, decreasing (i.e., largest at index 0)
	 */
	public static <V,E> List<V> verticesByInDegree(Graph<V,E> g) {
		List<V> sortedVertices = new ArrayList<>();

		g.vertices().forEach(sortedVertices::add);
		sortedVertices.sort(Comparator.comparingInt(v -> g.inDegree((V) v)).reversed());

		return sortedVertices;
	}

	/**
	 * Orders vertices in decreasing order by their out-degree
	 * @param g		graph
	 * @return		list of vertices sorted by out-degree, decreasing (i.e., largest at index 0)
	 */
	public static <V,E> List<V> verticesByOutDegree(Graph<V,E> g) {
		List<V> sortedVertices = new ArrayList<>();

		g.vertices().forEach(sortedVertices::add);
		sortedVertices.sort(Comparator.comparingInt(v -> g.outDegree((V) v)).reversed());

		return sortedVertices;
	}

	/**
	 * Return the spanning tree from a source vertex in a graph.
	 * @param g The original graph
	 * @param source The vertex where bfs starts
	 * @return A spanning tree as a Graph, the distances are stored in the edges.
	 */
	public static <V,E> Graph<V,Integer> bfs(Graph<V,E> g, V source) {
		Graph<V, Integer> spanningTree = new AdjacencyMapGraph<>();
		V v;
		Deque<V> q = new ArrayDeque<>();
		q.push(source);
		spanningTree.insertVertex(source);

		while (!q.isEmpty()) {
			v = q.poll();
			for (V neighbor: g.outNeighbors(v))
				if (!spanningTree.hasVertex(neighbor)) {
					spanningTree.insertVertex(neighbor);
					spanningTree.insertDirected(neighbor, v, spanningTree.outDegree(v) > 0 ? spanningTree.getLabel(v, spanningTree.outNeighbors(v).iterator().next()) + 1 : 1);
					q.add(neighbor);
				}
		}


		return spanningTree;
	}

	/**
	 * Return the path from the source to its origin
	 * @param tree A spanning tree
	 * @param source The first vertex of the path
	 * @return An ordered list of vertices that form the path
	 */
	public static <V,E> List<V> getPath(Graph<V,E> tree, V source) {
		List<V> path = new ArrayList<>();
		V v = source;

		// follow the path and add the vertices
		while(tree.outDegree(v) > 0) {
			path.add(v);
			v = tree.outNeighbors(v).iterator().next();
		}

		// append the center
		path.add(v);

		return path;
	}

	/**
	 * Return the set of vertices that are in graph but not subgraph
	 */
	public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,Integer> subgraph) {
		Set<V> vs = new HashSet<>();

		for(V v: graph.vertices())
			if(!subgraph.hasVertex(v)) vs.add(v);

		return vs;
	}

	/**
	 * Return the average separation of a given spanning tree
	 * Since the distances are stored in the edges, a root would no longer be needed
	 */
	public static <V> double averageSeparation(Graph<V,Integer> tree) {
		double avg = 0;
		int vCnt = tree.numVertices() - 1;	// number of vertices that are connected to the center

		// separation of inf means no vertices can reach the center
		if (vCnt == 0) return INF;

		for(V v: tree.vertices())
			// Only vertices other than the center have an out edge
			if (tree.outDegree(v) > 0) avg += getDistance(tree, v);

		return avg/vCnt;
	}
	/**
	 * A shorthand for getting the distance of a given vertex in a spanning tree
	 */
	public static <V> int getDistance(Graph<V,Integer> tree, V v) {
		return tree.getLabel(v, tree.outNeighbors(v).iterator().next());
	}
}
