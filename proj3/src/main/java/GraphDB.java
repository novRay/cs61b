import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    public final Map<Long, Node> nodes = new HashMap<>();
    private final Map<Long, Way> ways = new HashMap<>();
    public final Trie locationTrie = new Trie();
    public Map<String, List<Long>> locations = new HashMap<>();
    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcMome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        // use Iterator.remove method to avoid ConcurrentModificationException
        Iterator<Long> iter = nodes.keySet().iterator();
        while (iter.hasNext()) {
            Long v = iter.next();
            if (nodes.get(v).adj.isEmpty()) {
                iter.remove();
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        return nodes.get(v).adj;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double minDistance = Double.MAX_VALUE;
        long res = 0;
        for (Long n : nodes.keySet()) {
            Node v = nodes.get(n);
            double dist = distance(lon, lat, v.lon, v.lat);
            if (minDistance > dist) {
                minDistance = dist;
                res = n;
            }
        }
        return res;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodes.get(v).lon;
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodes.get(v).lat;
    }

    /**
     * Adds a node to the graph
     * @param n The node to be added
     */
    void addNode(Node n) {
        nodes.put(n.id, n);
    }

    void addAdj(Long v1, Long v2) {
        nodes.get(v1).adj.add(v2);
    }

    void removeNode(Long v) {
        nodes.remove(v);
    }

    /**
     * Adds a way to the graph
     * @param w The way to be added
     */
    void addWay(Way w) {
        ways.put(w.id, w);
    }

    Map<Long, Node> getNodes() {
        return nodes;
    }

    static class Node {
        long id;
        double lon;
        double lat;
        String name;
        Set<Long> wayOn = new HashSet<>();
        Set<Long> adj = new HashSet<>();
        double priority = 0.0;  // for v -> w, distance from source to v + distance from w to goal

        public Node(long id, double lon, double lat) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
        }
        public Node(long id, double lon, double lat, String name) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            this.name = name;
        }
    }

    class NodeComparator implements Comparator<Long> {

        @Override
        public int compare(Long o1, Long o2) {
            return Double.compare(nodes.get(o1).priority, nodes.get(o2).priority);
        }
    }

    public Comparator<Long> getNodeComparator() {
        return new NodeComparator();
    }

    void changePriority(long v, double p) {
        nodes.get(v).priority = p;
    }

    List<Map<String, Object>> searchLocations(String name) {
        List<Map<String, Object>> res = new LinkedList<>();
        if (locations.containsKey(name)) {
            for (long v : locations.get(name)) {
                Node n = nodes.get(v);
                Map<String, Object> info = new HashMap<>();
                info.put("lat", n.lat);
                info.put("lon", n.lon);
                info.put("name", n.name);
                info.put("id", n.id);
                res.add(info);
            }
        }
        return res;
    }

    void addLocation(String name, long n) {
        if (locations.containsKey(name)) {
            locations.get(name).add(n);
        } else {
            List<Long> ids = new LinkedList<>();
            ids.add(n);
            locations.put(name, ids);
        }
    }

    Set<Long> getWayOn(long v) {
        return nodes.get(v).wayOn;
    }


    String getWayName(long wayId) {
        if (ways.get(wayId).name != null) {
            return ways.get(wayId).name;
        } else {
            return "unknown road";
        }
    }

    static class Way {
        long id;
        String maxSpeed;
        String name;

        public Way(long id) {
            this.id = id;
        }
    }

}
