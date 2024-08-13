package graphs;

import java.util.*;

/**
 * Graph Design: A way for you to develop a graph on your terms to satisfy certain functionality.
 */
public class Graph {

    /**
     * Feel free to develop this class however you like, with either an adjacency list or using nodes. For reference, the staff solution
     * was developed using a combination of an adjacency list and the edge class to incorporate route names. The preferred solution
     * (and potentially the most intuitive) is using the adjacency list HashMap to store the vertices/stops as values and a list of
     * that vertex's edges (i.e. the 'A-C' & 'A-B' edges for vertex 'A') as the value. The vertex can be stored as a Node or just a String!
     *
     */

    /**
     * a map to represent an adjacency list for our graph.
     */
    private Map<String, List<Edge>> adjacencyList;


    /**
     * an Edge class example you might like to use. Hint: this was particularly helpful in the staff solution.
     */
    private class Edge {
        private String from;
        private String to;
        private String routeName;
        private double weight;

//        Edge(String from, Double weight){
//            this.from = from;
//            this.weight = weight;
//        }

        Edge(String from, String to, double weight, String routeName) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.routeName = routeName;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public double getWeight() {
            return weight;
        }

        public String getRouteName() {
            return routeName;
        }

        @Override
        public String toString() {
            return "Edge{" + "from='" + from + '\'' + ", to='" + to + '\'' + ", routeName='" + routeName + '\'' + ", weight=" + weight + '}';
        }
    }

    /**
     * Getting this show on the road!
     */
    public Graph() {
        adjacencyList = new HashMap<>();
    }

    /**
     * Task 1: Add Transit Paths - add Transit Paths to the graph design you have developed.
     *
     * @param stops
     * @param routeName
     * @param travelTimes
     */
    public void addTransitRoute(List<String> stops, String routeName, List<Double> travelTimes) {
        for (int i = 0; i < stops.size() - 1; i++) {
            String from = stops.get(i);
            String to = stops.get(i + 1);
            double weight = travelTimes.get(i);

            adjacencyList.putIfAbsent(from, new ArrayList<>());
            adjacencyList.putIfAbsent(to, new ArrayList<>());

            adjacencyList.get(from).add(new Edge(from, to, weight, routeName));
            adjacencyList.get(to).add(new Edge(to, from, weight, routeName)); // 双向边
        }
    }

    /**
     * Task 2: Get Transit Paths - get the three transit paths from a start to destination that use the least amount of transfers.
     * Break ties using the shorter path!
     *
     * @param start
     * @param destination
     * @return a List<List<String>> of vertices and routes for the three minimized transfer paths [[A, C, D, E, F, G, 372, 556], ...].
     * The inner list should be formatted where you add Strings in the sequential order "A" then "B" and all vertices, then "32" and all bus routes etc.
     * i.e. We want an inner list of [A, B, G, 32, 1Line] since the route from A -> B is on route 32 and from B -> G is on the 1Line.
     * Ties are broken using the shorter path!
     * Note: Do not add the same route multiple times for a path! I.e. Only add route "32" once per path.
     */
    public List<List<String>> getTransitPaths(String start, String destination) {
        List<List<String>> allPaths = allPaths(start, destination);
        List<List<Edge>> allPathsByEdges = new ArrayList<>();
        List<List<String>> result = new ArrayList<>();
        Set<String> uniquePaths = new HashSet<>();

//        System.out.println("All paths before sorting:");
        for (List<String> path : allPaths) {
//            System.out.println(path);
            allPathsByEdges.addAll(cartesianEdgesOnPath(path));
        }
        // sort all paths by number of transfers and then by total distance
        allPathsByEdges.sort((edges1, edges2) -> {
            int transfers1 = countTransfersByEdges(edges1);
            int transfers2 = countTransfersByEdges(edges2);
            if (transfers1 != transfers2) {
                return Integer.compare(transfers1, transfers2);
            } else {
                return Double.compare(calculateDistanceByEdges(edges1), calculateDistanceByEdges(edges2));
            }
        });

//        System.out.println("All paths after sorting:");
//        for (List<Edge> edges : allPathsByEdges) {
//            System.out.println(pathWithRoutes(edges) + " with distance: " + calculateDistanceByEdges(edges));
//        }
        for (List<Edge> edges : allPathsByEdges) {
            List<String> pathWithRoutes = pathWithRoutes(edges);
            String uniquePathKey = String.join(",", pathWithRoutes);
            // Add the path if it's unique
            if (uniquePaths.add(uniquePathKey)) {
                result.add(pathWithRoutes);
            }
            if (result.size() == 3) break; // Stop after finding 3 unique paths
        }
//        System.out.println("Final selected paths:");
//        for (List<String> path : result) {
//            System.out.println(path);
//        }
        return result;
    }

    private List<List<Edge>> cartesianEdgesOnPath(List<String> path) {
        // add the routes between nodes
        List<List<Edge>> edgesList = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            List<Edge> edges = new ArrayList<>();
            for (Edge edge : adjacencyList.getOrDefault(from, Collections.emptyList())) {
                if (edge.getTo().equals(to)) {
                    edges.add(edge);
                }
            }
            edgesList.add(edges);
        }
        List<List<Edge>> result = new ArrayList<>();
        // Cartesian product of edgesList
        for (List<Edge> edges : edgesList) {
            if (result.isEmpty()) {
                result.add(edges);
            } else {
                int resultSize = result.size();
                int edgesSize = edges.size();
                // make edgeSize-1 copies of result
                for (int i = 1; i < edgesSize; i++) {
                    for (int j = 0; j < resultSize; j++) {
                        result.add(new ArrayList<>(result.get(j)));
                    }
                }
                // add the edges to the result
                for (int i = 0; i < edgesSize; i++) {
                    Edge edge = edges.get(i);
                    for (int j = 0; j < resultSize; j++) {
                        result.get(i * resultSize + j).add(edge);
                    }
                }
            }
        }
        return result;
    }

    private int countTransfersByEdges(List<Edge> edges) {
        if (edges.isEmpty()) return 0;
        int count = 1;
        for (int i = 1; i < edges.size(); i++) {
            if (!edges.get(i).getRouteName().equals(edges.get(i - 1).getRouteName())) {
                count++;
            }
        }
        return count;
    }

    private double calculateDistanceByEdges(List<Edge> edges) {
        double distance = 0;
        for (Edge edge : edges) {
            distance += edge.getWeight();
        }
        return distance;
    }

    private List<String> pathWithRoutes(List<Edge> edges) {
        if (edges.isEmpty()) return Collections.emptyList();
        List<String> path = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        String previousRoute = edges.get(0).getRouteName();
        path.add(edges.get(0).getFrom());
        routes.add(edges.get(0).getRouteName());
        for (Edge edge : edges) {
            path.add(edge.getTo());
            if (!edge.getRouteName().equals(previousRoute)) {
                routes.add(edge.getRouteName());
                previousRoute = edge.getRouteName();
            }
        }
        path.addAll(routes);
        return path;
    }

    /**
     * You can use this as a helper to return all paths from a start vertex to an end vertex.
     * Call this in getTransitPaths!
     * This method is designed to help give partial credit in the event that you are unable to finish getTransitPaths!
     *
     * @param start
     * @param destination
     * @return a List<List<String>> of containing all vertices among all paths from start to dest [[A, C, D, E, F, G], ...].
     * Do not add transit routes to this method! You should take care of that in getTransitPaths!
     */
    public List<List<String>> allPaths(String start, String destination) {
        List<List<String>> result = new ArrayList<>();
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        dfs(start, destination, visited, path, result);

        return result;
    }

    private void dfs(String current, String destination, Set<String> visited, List<String> path, List<List<String>> result) {
        visited.add(current);
        path.add(current);

        if (current.equals(destination)) {
            result.add(new ArrayList<>(path));
        } else {
            for (Edge edge : adjacencyList.getOrDefault(current, new ArrayList<Edge>())) {
                if (!visited.contains(edge.getTo())) {
                    dfs(edge.getTo(), destination, visited, path, result);
                }
            }
        }

        path.remove(path.size() - 1);
        visited.remove(current);
    }

    /**
     * Task 3: Get Shortest Coffee Path - get the shortest path from start to destination with a coffee shop on the route.
     *
     * @param start
     * @param destination
     * @param coffeeStops
     * @return a List<String> representing the shortest path from a start to a destination with a coffee shop along the way
     * return in the form of a List where you add Strings in the sequential order "A" then "B" and all vertices, then "32" and all bus routes etc.
     * i.e. We want to return [A, B, G, 32, 1Line] since the route from A -> B is on route 32 and the route from B -> G is on the 1Line.
     * Note: Do not add the same route multiple times for a path! I.e. Only add route "32" once per path.
     */
    public List<String> getShortestCoffeePath(String start, String destination, Set<String> coffeeStops) {
        List<String> bestPath = null;
        double bestDistance = Double.MAX_VALUE;
        for (String coffeeStop : coffeeStops) {
            List<String> pathToCoffee = shortestPath(start, coffeeStop);
            List<String> pathFromCoffee = shortestPath(coffeeStop, destination);
//            System.out.println("Path to coffee stop " + coffeeStop + ": " + pathToCoffee);
//            System.out.println("Path from coffee stop " + coffeeStop + " to destination: " + pathFromCoffee);
            if (!pathToCoffee.isEmpty() && !pathFromCoffee.isEmpty()) {
                // Combine paths ensuring no duplication of the coffee stop
                List<String> fullPath = new ArrayList<>(pathToCoffee);
                fullPath.addAll(pathFromCoffee.subList(1, pathFromCoffee.size()));
                List<List<Edge>> edgesToCoffee = cartesianEdgesOnPath(fullPath);
                edgesToCoffee.sort(Comparator.comparingDouble(this::calculateDistanceByEdges).thenComparingInt(this::countTransfersByEdges));
                double totalDistance = calculateDistanceByEdges(edgesToCoffee.get(0));
//                System.out.println("Combined path through coffee stop " + coffeeStop + ": " + fullPath + " with distance: " + totalDistance);
                if (totalDistance < bestDistance) {
                    bestDistance = totalDistance;
                    bestPath = pathWithRoutes(edgesToCoffee.get(0));
                }
            }
        }
//        System.out.println("Best path with coffee stop: " + bestPath);
        return bestPath;
    }

    /**
     * A helper method used to actually find the shortest path between any start node and destination node.
     * Call this in getShortestCoffeePaths!
     * This method is designed to help give partial credit in the event that you are unable to finish getShortestCoffeePath!
     *
     * @param start
     * @param destination
     * @return a List<String> containing all vertices along the shortest path in the form [A, C, D, E, F, G].
     * Do not add transit routes to this method! You should take care of that in getShortestCoffeePaths!
     */
    public List<String> shortestPath(String start, String destination) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<NodeDistancePair> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        distances.put(start, 0.0);
        priorityQueue.add(new NodeDistancePair(start, 0.0));

        while (!priorityQueue.isEmpty()) {
            NodeDistancePair currentPair = priorityQueue.poll();
            String currentNode = currentPair.node;

            if (currentNode.equals(destination)) {
                break;
            }

            for (Edge edge : adjacencyList.getOrDefault(currentNode, new ArrayList<>())) {
                double newDist = distances.get(currentNode) + edge.getWeight();
                if (newDist < distances.getOrDefault(edge.getTo(), Double.MAX_VALUE)) {
                    distances.put(edge.getTo(), newDist);
                    previousNodes.put(edge.getTo(), currentNode);
                    priorityQueue.add(new NodeDistancePair(edge.getTo(), newDist));
                }
            }
        }

        List<String> path = new ArrayList<>();
        for (String at = destination; at != null; at = previousNodes.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private class NodeDistancePair {
        String node;
        double distance;

        NodeDistancePair(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}
