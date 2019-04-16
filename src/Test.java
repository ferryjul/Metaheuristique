import java.util.ArrayList;
import java.util.HashMap;

public class Test {

    /*
    public String instanceName;
    public int evacNodesNB;
    public HashMap<Integer,EvacNodeData> evacNodesList;
    public Boolean nature; // true for valid, false for invalid
    public int objectiveValue;
    public int computeTime;
    public String method;
    public String other;

    public class EvacNodeData {
        public int evacRate;
        public int beginDate;
    }

    */
public static void main(String[] args) {

/* On crée la solution du TD */
Solution TD_sol = new Solution();
TD_sol.instanceName="example TD";
TD_sol.evacNodesNB=3;
HashMap<Integer,EvacNodeData> hm = new HashMap<Integer,EvacNodeData>();
hm.put(1,new EvacNodeData(8,3));
hm.put(2,new EvacNodeData(5,0));
hm.put(3,new EvacNodeData(3,0));
TD_sol.evacNodesList=hm;
TD_sol.computeTime = 1000;
TD_sol.objectiveValue = 37;
TD_sol.method = "resolu à la main";
TD_sol.other = "Olivier - on connait une meilleure solution";


/* On crée la data de l'exemple du TD */
data TD_data = new data();
TD_data.set_safe_node(6);
TD_data.set_num_evac(3);
ArrayList<Integer> path1 = new ArrayList<Integer>();
path1.add(4);
path1.add(5);
path1.add(6);
TD_data.add_evac_path(1,48,8,3,path1);
ArrayList<Integer> path2 = new ArrayList<Integer>();
path2.add(4);
path2.add(5);
path2.add(6);
TD_data.add_evac_path(2,30,5,3,path2);
ArrayList<Integer> path3 = new ArrayList<Integer>();
path3.add(5);
path3.add(6);
TD_data.add_evac_path(3,33,3,2,path1);
TD_data.add_edge(1, 4, 0, 7, 8);
TD_data.add_edge(2, 4, 0, 4, 5);
TD_data.add_edge(3, 5, 0, 6, 3);
TD_data.add_edge(4, 5, 0, 9, 10);
TD_data.add_edge(5, 6, 0, 12, 11);
TD_data.set_nb_edge(5);
TD_data.set_nb_node(6);

/* On vérifie le tout */
new Checker().check(TD_data, TD_sol);
}

}
