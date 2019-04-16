import java.util.ArrayList;
import java.util.HashMap;

public class data {

private int num_evac;
private int safe_node;

private ArrayList<ArrayList<Integer>> evac_paths;
private HashMap<Integer, HashMap<Integer, Edge_data>> edges;

class Edge_data {
	int duedate;
	float length;
	int capacity;
	
	public Edge_data(int d, float l, int c) {
		duedate = d;
		length = l;
		capacity = c;
	}
}

public data() {
	// init the data structures
	evac_paths = new ArrayList<ArrayList<Integer>>();
	edges = new HashMap<Integer, HashMap<Integer, Edge_data>>();
}

public void set_num_evac(int id) {
	this.num_evac = id;
}

public void set_safe_node(int id) {
	this.safe_node = id;
}

public void add_evac_path(ArrayList<Integer> aList) { // note that aList must be created by caller
	this.evac_paths.add(aList);
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

}

