import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Kevin Bacon Game
 *
 * @author Sihao Huang, Spring 2021
 */

public class KevinBaconGame {
    Graph<String, Set<String>> network;
    // holds the paths from current center
    Graph<String, Integer> spanningTree;
    // the center vertex
    String center;
    // the map of average separation
    Map<String, Double> avgSeparation;
    List<String> actorsSortedByAvgSeparation;
    List<String> actorsSortedByDegree;
    List<String> actorsSortedBySeparation;


    /**
     * Initialize the graph and preprocess some information
     * @param moviesPath path to the movies file
     * @param actorsPath path to the actors file
     * @param movieActorsPath path to the movie-actor file
     */
    public KevinBaconGame(String moviesPath, String actorsPath, String movieActorsPath) throws Exception {
        Map<String, String> actors = new HashMap<>();
        Map<String, String> movies = new HashMap<>();
        Map<String, Set<String>> actorsOfMovies = new HashMap<>();
        network = new AdjacencyMapGraph<>();

        String line;

        // read in the movies
        BufferedReader input = new BufferedReader(new FileReader(moviesPath));
        while ((line = input.readLine()) != null) {

            String[] tks = line.split("\\|");
            // set the default center to the first actor
            movies.put(tks[0], tks[1]);
            actorsOfMovies.put(tks[1], new HashSet<>());
        }
        input.close();

        // read in the actors
        input = new BufferedReader(new FileReader(actorsPath));
        while ((line = input.readLine()) != null) {
            String[] tks = line.split("\\|");
            if (center == null) center = tks[1];
            actors.put(tks[0], tks[1]);
            network.insertVertex(tks[1]);
        }
        input.close();

        // process the movie-actor records
        input = new BufferedReader(new FileReader(movieActorsPath));
        while ((line = input.readLine()) != null) {
            String[] tks = line.split("\\|");
            // filter out unknown records
            if (movies.get(tks[0]) != null && actors.get(tks[1]) != null)actorsOfMovies.get(movies.get(tks[0])).add(actors.get(tks[1]));
        }
        input.close();

        // construct graph from actorsOfMovies
        actorsOfMovies.forEach((movie, as) -> {
            for(String actorA: as)
                for(String actorB: as)
                    if (!actorA.equals(actorB)) {
                        if (!network.hasEdge(actorA, actorB)) network.insertUndirected(actorA, actorB, new HashSet<>());
                        network.getLabel(actorA, actorB).add(movie);
                    }
        });

        // save all the average separations preprocess the lists while calculating spanning trees
        avgSeparation = new HashMap<>();
        actorsSortedByAvgSeparation = new ArrayList<>();
        int cnt = 0;
        for(String actor: network.vertices()) {
            if (network.numVertices() > 10) {
                int progress = ++cnt/(network.numVertices()/10);
                if (cnt%(network.numVertices()/10) == 0) System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\bLoading..." + 10*progress + "%");
            }
            avgSeparation.put(actor, GraphLibExtended.averageSeparation(GraphLibExtended.bfs(network, actor)));
            actorsSortedByAvgSeparation.add(actor);
        }
        if (network.numVertices() > 10) System.out.println();
        actorsSortedByAvgSeparation.sort(Comparator.comparingDouble(actor -> avgSeparation.get(actor)));

        // construct actorsSortedByDegree and default actorsSortedBySeparation
        actorsSortedByDegree = GraphLibExtended.verticesByOutDegree(network);

        // set the center to Kevin Bacon if he is one of the actors
        if (network.hasVertex("Kevin Bacon")) center = "Kevin Bacon";
        changeCenter(center, true);

    }

    // Below are functions for the commands

    /**
     * list centers of the universe, sorted by average separation
     * @param count Number of centers to display. The centers
     *             will be grabbed from the top if count > 0,
     *              or the bottom if count < 0
     */
    void listActorsByAverageSeparation(int count) {
        int actualCount = Math.min(Math.abs(count), network.numVertices());

        System.out.println("The top " + actualCount + " actor(s) with " + (count >= 0 ? "lowest" : "highest") + " average separation");
        if (count >= 0) {
            for (int i = 0; i < actualCount; i++) {
                String actor = actorsSortedByAvgSeparation.get(i);
                System.out.println(actor + " - " + (avgSeparation.get(actor) == Double.MAX_VALUE ? "isolated" : avgSeparation.get(actor)));
            }
        } else {
            for (int i = network.numVertices() - 1; i > network.numVertices() - 1 - actualCount; i--) {
                String actor = actorsSortedByAvgSeparation.get(i);
                System.out.println(actor + " - " + (avgSeparation.get(actor) == Double.MAX_VALUE ? "isolated" : avgSeparation.get(actor)));
            }
        }
        System.out.println();
    }
    /**
     * list centers of the universe, sorted by degree
     * Only the ones with degree in the provided range will be displayed
     */
    void listActorsByDegree(int low, int high) {
        System.out.println("Actor(s) with degree between " + low + " to " + high + ":");
        if (low >= high) return;

        for(String actor: actorsSortedByDegree)
            if (network.outDegree(actor) >= low && network.outDegree(actor) <= high)
                System.out.println(actor + " - " + network.outDegree(actor));
    }
    /**
     * a default version of listActorsByDegree
     */
    void listActorsByDegree() {
        System.out.println("Actor(s) sorted by degree:");

        for(String actor: actorsSortedByDegree)
            System.out.println(actor + " - " + network.outDegree(actor));

    }

    /**
     * list actors with infinite separation from the current center
     */
    void listUnreachableActors() {
        System.out.println("Actor(s) unreachable from " + center);
        for (String actor: GraphLibExtended.missingVertices(network, spanningTree))
            System.out.println(actor);
        System.out.println();
    }

    /**
     * show the path from the queried actor to current center of the universe
     */
    void showPath(String actor) {
        if (!network.hasVertex(actor)) {
            System.out.println("Actor not found");
            return;
        }
        if (!spanningTree.hasVertex(actor)) {
            System.out.println(center + " is unreachable from " + actor);
            return;
        }

        List<String> path = GraphLibExtended.getPath(spanningTree, actor);

        if (path.size() - 1 == 0) {
            System.out.println(actor + " is the current center.");
            return;
        }
        System.out.println(actor + "'s number is " + (path.size() - 1));
        for(int i = 0; i < path.size() - 1; i++)
            System.out.println(path.get(i) + " appeared in " + network.getLabel(path.get(i), path.get(i + 1)) + " with " + path.get(i + 1));

        System.out.println();
    }

    /**
     *  list actors sorted by non-infinite separation from the current center, with separation within bounds
     */
    void listActorsBySeparation(int low, int high) {
        System.out.println("Actor(s) with separation from " + center + " between " + low + " to " + high + ":");
        if (low >= high) return;

        for(String actor: actorsSortedBySeparation) {
            int separation = GraphLibExtended.getDistance(spanningTree, actor);
            if (separation >= low && separation <= high) System.out.println(actor + " - " + separation);
        }
    }
    /**
     * a default version of listActorsBySeparation
     */
    void listActorsBySeparation() {
        System.out.println("Actor(s) sorted by separation from " + center + ":");

        for(String actor: actorsSortedBySeparation)
            System.out.println(actor + " - " + GraphLibExtended.getDistance(spanningTree, actor));

        System.out.println();
    }

    /**
     * Change the current center and recalculate actors sorted by separation
     */
    void changeCenter(String actor) {
        changeCenter(actor, false);
    }
    void changeCenter(String actor, Boolean hideOutput) {
        if (actor == null) return;
        center = actor;
        actorsSortedBySeparation = new ArrayList<>();
        spanningTree = GraphLibExtended.bfs(network, center);

        for(String v: spanningTree.vertices())
            if (!v.equals(center)) actorsSortedBySeparation.add(v);


        actorsSortedBySeparation.sort(Comparator.comparingInt(v -> GraphLibExtended.getDistance(spanningTree, v)));

        if (!hideOutput) System.out.println(center + " is now the center of the acting universe, connected to " + spanningTree.numVertices() + "/" + network.numVertices() + " actors with average separation " + avgSeparation.get(center) + "\n");
    }

    /**
     * Start the game
     */
    static void start() {
//        String MoviesPath = "inputs/moviesTest.txt";
//        String ActorsPath = "inputs/actorsTest.txt";
//        String MovieActorsPath = "inputs/movie-actorsTest.txt";
        String MoviesPath = "inputs/movies.txt";
        String ActorsPath = "inputs/actors.txt";
        String MovieActorsPath = "inputs/movie-actors.txt";
        String instructions = """
                Commands:
                c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation
                d <low> <high>: list actors sorted by degree, with degree between low and high
                h: show the instructions again
                i: list actors with infinite separation from the current center
                p <name>: find path from <name> to current center of the universe
                s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high
                u <name>: make <name> the center of the universe
                q: quit game
                """;
        String header = "Kevin Bacon game > ";
        String ERR_INVALID_COMMAND = "Invalid command";
        String ERR_UNKNOWN_ACTOR = "Actor not found";
        String ERR_UNREACHABLE_ACTOR = "This actor is not connected to ";
        String ERR_INVALID_PARAMETER = "Invalid parameter";
        String ERR_UNRECOGNIZED_COMMAND = "Unrecognized command";

        KevinBaconGame game = null;

        try {
            game = new KevinBaconGame(MoviesPath, ActorsPath, MovieActorsPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (game == null) {
            System.out.println("Unable to start game.");
            return;
        }

        // print out the command list
        System.out.println(instructions);

        // show the current center
        game.changeCenter(game.center);

        // print out line header
        System.out.print(header);

        Scanner in = new Scanner(System.in);
        String line;

        // handling commands
        while ((line = in.nextLine()) != null) {
            String[] tks = line.split(" +");

            switch (tks[0]) {
                case "c":   // list sorted by avg separation
                    if (tks.length > 1) {
                        try {
                            game.listActorsByAverageSeparation(Integer.parseInt(line.split(" +", 2)[1]));
                        } catch (NumberFormatException e) {
                            System.out.println(ERR_INVALID_PARAMETER);
                        }
                    } else System.out.println(ERR_INVALID_COMMAND);
                    break;
                case "d":   // list sorted by degree
                    if (tks.length > 2) {
                        tks = line.split(" +", 3);
                        try {
                            game.listActorsByDegree(Integer.parseInt(tks[1]), Integer.parseInt(tks[2]));
                        } catch (NumberFormatException e) {
                            System.out.println(ERR_INVALID_PARAMETER);
                        }
                    }
                    else if (tks.length == 1) game.listActorsByDegree();
                    else System.out.println(ERR_INVALID_COMMAND);
                    break;
                case "h":   // help - show the command list
                    if (tks.length == 1) System.out.println(instructions);
                    else System.out.println(ERR_INVALID_COMMAND);
                    break;
                case "i":   // show the unreachable actors from the current center
                    if (tks.length == 1) game.listUnreachableActors();
                    else System.out.println(ERR_INVALID_COMMAND);
                    break;
                case "p":   // display a path to the center
                    if (tks.length > 1) {
                        String actor = line.split(" +", 2)[1];
                        if (game.network.hasVertex(actor)) {
                            if (game.spanningTree.hasVertex(actor)) game.showPath(actor);
                            else System.out.println(ERR_UNREACHABLE_ACTOR + game.center);
                        }
                        else System.out.println(ERR_UNKNOWN_ACTOR);
                    } else System.out.println(ERR_INVALID_COMMAND);
                    break;
                case "s":   // list sorted by separation
                    if (tks.length > 2) {
                        tks = line.split(" +", 3);
                        try {
                            game.listActorsBySeparation(Integer.parseInt(tks[1]), Integer.parseInt(tks[2]));
                        } catch (NumberFormatException e) {
                            System.out.println(ERR_INVALID_PARAMETER);
                        }
                    }
                    else if (tks.length == 1) game.listActorsBySeparation();
                    else System.out.println(ERR_INVALID_COMMAND);
                    break;
                case "u":   // change the center of the universe
                    if (tks.length > 1) {
                        String actor = line.split(" +", 2)[1];
                        if (game.network.hasVertex(actor)) game.changeCenter(actor);
                        else System.out.println(ERR_UNKNOWN_ACTOR);
                    } else System.out.println(ERR_INVALID_COMMAND);
                    break;
                case "q":   // quit game
                    if (tks.length == 1) return;
                    else System.out.println(ERR_INVALID_COMMAND);
                default:
                    System.out.println(ERR_UNRECOGNIZED_COMMAND);
            }
            System.out.print(header);
        }

    }

    /**
     * Test function for a simple graph
     */
    static void testSimpleGraph() {
        Graph<String, String> g = new AdjacencyMapGraph<>();
        g.insertVertex("0");
        g.insertVertex("1");
        g.insertVertex("2");
        g.insertVertex("3");
        g.insertVertex("4");
        // 0 <-> 1 <-> 2 <-> 3 <-> 4
        // 0 <-> 3
        g.insertUndirected("0", "1", "aaa");
        g.insertUndirected("0", "3", "eee");
        g.insertUndirected("2", "1", "bbb");
        g.insertUndirected("3", "2", "ccc");
        g.insertUndirected("4", "3", "ddd");

        Graph<String, Integer> tree = GraphLibExtended.bfs(g, "0");

        System.out.println("Simple graph: ");
        System.out.println(g);
        System.out.println("------");
        System.out.println("Spanning tree from 0: ");
        System.out.println(tree);
        System.out.println("------");
        System.out.println("The path from 4 to 0: " + GraphLibExtended.getPath(tree, "4"));
        System.out.println("------");
        System.out.println("Average separation of 0: " + GraphLibExtended.averageSeparation(tree));
    }

    /**
     * Test function for boundary cases: empty input files, record file that has duplicates, record file that has unknown references
     */
    static void testBoundaryConditions() {
        String MoviesPath = "inputs/emptyMoviesTest.txt";
        String ActorsPath = "inputs/emptyActorsTest.txt";
        String MovieActorsPath = "inputs/emptyMovie-actorsTest.txt";

        try {
            System.out.println("Testing with empty input files:");
            KevinBaconGame game = new KevinBaconGame(MoviesPath, ActorsPath, MovieActorsPath);
            System.out.println(game.network);

            System.out.println("\n\n\n");
            MoviesPath = "inputs/moviesTest.txt";
            ActorsPath = "inputs/actorsTest.txt";
            MovieActorsPath = "inputs/movie-actorsTestWithUnknowns.txt";
            System.out.println("Testing: move-actors with unknown actors/movies");
            System.out.println("------");
            game = new KevinBaconGame(MoviesPath, ActorsPath, MovieActorsPath);
            System.out.println(game.network);


            System.out.println("\n\n\n");
            MovieActorsPath = "inputs/movie-actorsTest WithDuplicates.txt";
            System.out.println("Testing: move-actors with duplicate records");
            System.out.println("------");
            game = new KevinBaconGame(MoviesPath, ActorsPath, MovieActorsPath);
            System.out.println(game.network);

            game.listActorsBySeparation(1, 2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
//        testBoundaryConditions();
//        testSimpleGraph();
        KevinBaconGame.start();


    }
}
