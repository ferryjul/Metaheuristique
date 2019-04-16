import java.util.ArrayList;
import java.util.HashMap;

public class data {

private int num_evac;
private int safe_node;
private int nb_edge;
private int nb_node;
private ArrayList<Path_data> evac_paths;
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

class Path_data {
	int origin;
	ArrayList<Integer> following;
	int nb_following;
	int max_rate;

	public Path_data(int o, int nb_f, int rate, ArrayList<Integer> fol) {
		origin = o;
		nb_following = nb_f;
		max_rate = rate;
		following = fol;
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

}

