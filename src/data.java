import java.util.ArrayList;
import java.util.HashMap;

public class data {

private int num_evac;
private int safe_node;
private int nb_edge;
private int nb_node;
public ArrayList<Path_data> evac_paths;
private HashMap<Integer, HashMap<Integer, Edge_data>> edges;  // origin, hashmap<destination, edge_data>

public class Edge_data {
	int duedate;
	float length;
	Integer capacity;
	
	public Edge_data(int d, float l, int c) {
		duedate = d;
		length = l;
		capacity = c;
	}
}

public data() {
	// init the data structures
	evac_paths = new ArrayList<Path_data>();
	edges = new HashMap<Integer, HashMap<Integer, Edge_data>>();
}

public void set_nb_edge(int n) {
	this.nb_edge = n;
}

public void set_nb_node(int n) {
	this.nb_node = n;
}

public void set_num_evac(int id) {
	this.num_evac = id;
}

public void set_safe_node(int id) {
	this.safe_node = id;
}

public void add_evac_path(int origin, int pop, int rate, int k, ArrayList<Integer> follow) { // note that aList must be created by caller
	this.evac_paths.add(new Path_data(origin, k, rate, follow));
}

public void add_edge(int origin, int destination, int duedate, float length, int capacity) {
	if(edges.containsKey(origin)) {
		edges.get(origin).put(destination, new Edge_data(duedate, length, capacity));
	}
	else {
		HashMap<Integer,Edge_data> hm = new HashMap<Integer,Edge_data>();
		hm.put(destination, new Edge_data(duedate, length, capacity));
		edges.put(origin, hm);
	}
}

public HashMap<Integer,HashMap<Integer,Integer>> getEdgesCapas() {
	HashMap<Integer,HashMap<Integer,Integer>> result = new HashMap<Integer,HashMap<Integer,Integer>>();
	for(int orig : edges.keySet()) {
		HashMap<Integer,Integer> currHM = new HashMap<Integer,Integer>();
		for(int dest : edges.get(orig).keySet()) {
			currHM.put((Integer) dest, edges.get(orig).get(dest).capacity);
		}
		result.put(orig, currHM);
	}
	return result;
}
}