import java.util.HashMap;
import java.util.ArrayList;

public class Checker {

private ArrayList<HashMap<Integer,HashMap<Integer, Integer>>> edgesData; // for each time unit, for each edge (for each origin, each dest), free capacity

public void check(data d, Solution s) {
    Boolean valid = true;
    System.out.println("Beginning check");
    // Init data structure
    edgesData = new ArrayList<HashMap<Integer,HashMap<Integer, Integer>>>();
    for(int i = 0 ; i <= s.objectiveValue ; i++) {
        edgesData.add(d.getEdgesCapas());
    }
    // Perfom verif
    for(int i = 0 ; i <= s.objectiveValue && valid; i ++) {
        for(int evacNode : s.evacNodesList.keySet()) {
            if(s.evacNodesList.get(evacNode).beginDate == i) { // if evac begins now
                    int usedCapa = s.evacNodesList.get(evacNode).evacRate;
                    int currTime = i;
                    Path_data evacPath = null;
                    for(Path_data ed : d.evac_paths) { // we find the right evacuation path
                        if(ed.origin == evacNode) {
                            evacPath = ed;
                        }
                    }
                    int currNode = evacNode;
                    for(int follow : evacPath.following) { // we compute the impact for each edge for each time unit
                        int k = edgesData.get(currTime).get(currNode).get(follow);
                        k-=usedCapa;
                        edgesData.get(currTime).get(currNode).put(follow,k);
                        if(edgesData.get(currTime).get(currNode).get(follow)<0) {
                            valid = false;
                        }
                        currNode = follow;
                    }
            }
        }
    }
    System.out.println("Generated output");
}

}