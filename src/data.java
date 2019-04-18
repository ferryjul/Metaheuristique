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
	int length;
	Integer capacity;
	
	public Edge_data(int d, int l, int c) {
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
	this.evac_paths.add(new Path_data(origin, pop, k, rate, follow));
}

public void add_edge(int origin, int destination, int duedate, int length, int capacity) {
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

public int getEdgeLength(int origNode, int DestNode) {
	return edges.get(origNode).get(DestNode).length;
}

public int getEdgeDueDate(int origNode, int DestNode) {
	return edges.get(origNode).get(DestNode).duedate;
}

public int getEdgeCapa(int origNode, int DestNode) {
	return edges.get(origNode).get(DestNode).capacity;
}


public void read_data()
{
    System.out.println("---------- READ DATA ----------");
    System.out.println("num_evac => " + num_evac);
    System.out.println("safe node => " + safe_node);
    System.out.println("---------- EVACUATION PATHS ----------");

    for(Path_data p : evac_paths)
    {
		System.out.println("origin => "+p.origin);
		System.out.println("population => "+p.population);
		System.out.println("max_rate => "+p.max_rate);
        System.out.println("nb_following => "+p.nb_following);
		System.out.print("following => ");
		for(Integer x : p.following)
		{
			System.out.print(" "+x+" ");
		}
		System.out.println("");
		System.out.println("-------------------------------------------------");

    }

	System.out.println("---------- NODE AND EDGES ----------");
	System.out.println("nb node => " + nb_node);
    System.out.println("nb edge => " + nb_edge);
    System.out.println("--------------ARCS----------------");

    edges.forEach((k,v) -> {
        System.out.println("origin =>"+k);
        v.forEach((x,y) -> {
            System.out.println("destination => "+x);
            System.out.println("duedate => " +y.duedate);
            System.out.println("length => " +y.length);
			System.out.println("capacity => " +y.capacity);
			System.out.println("------------------------------");
        });
    });
    
}

}