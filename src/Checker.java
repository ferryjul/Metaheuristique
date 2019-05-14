import java.util.HashMap;
import java.util.ArrayList;

public class Checker {

    private ArrayList<HashMap<Integer, HashMap<Integer, Integer>>> edgesData; // for each time unit, for each edge (for
                                                                              // each origin, each dest), free capacity
    Boolean valid = true;
    int maxTime = 0;
    int nodePB = 0;
    int valuePB = 0;
    int origPB = 0;
    int destPB = 0;
    int debugState = 1; // 0 = no display ; 1 = minimal display ; 2 = full display ; 3 = detailed display
    Boolean checkDueDate = false;

    public CheckerReturn check(data d, Solution s) {
        if(debugState >= 1) { 
            System.out.println("------------------------------ Beginning solution check ------------------------------");
        }
        // Init data structure
        edgesData = new ArrayList<HashMap<Integer, HashMap<Integer, Integer>>>();
        for (int i = 0; i <= s.objectiveValue; i++) {
            edgesData.add(d.getEdgesCapas());
        }
        // Capacity contraint check
        for (int evacNode : s.evacNodesList.keySet()) { // Iterates on nodes to be evacuated
            if (valid) {
                if(debugState >= 2) { 
                    System.out.println("[Evacuation of node " + evacNode + " (at time " + s.evacNodesList.get(evacNode).beginDate + ")]");
                }
                int startDate = s.evacNodesList.get(evacNode).beginDate;
                //System.out.println("startDate = " + startDate);
                int usedCapa = s.evacNodesList.get(evacNode).evacRate;
                int currTime = -1;
                if(startDate >= 0) {
                int currStart = startDate;
                Path_data evacPath = null;
                for (Path_data ed : d.evac_paths.values()) { // we find the right evacuation path
                    if (ed.origin == evacNode) {
                        evacPath = ed;
                    }
                }
                int currNode;
                int peopleNotEvacuated = evacPath.population;             
                while (peopleNotEvacuated != 0 && valid) { // we compute all people packets travel on this path here
                    if (usedCapa > peopleNotEvacuated) { // last people packet
                        usedCapa = peopleNotEvacuated;
                        peopleNotEvacuated = 0;
                    } else {
                        peopleNotEvacuated -= usedCapa;
                    }
                    currTime = currStart; // next packet departure !
                    if(debugState >= 2) { 
                        System.out.println(" Still to evacuate : " + peopleNotEvacuated);
                    }
                    currNode = evacNode;
                    for (int follow : evacPath.following) { // we compute the impact of the current packet on every edge
                                                            // through time
                        // System.out.println("curr time = " + currTime + ", curr node = " + currNode +
                        // ", follow = " + follow);
                        if (valid) {
                            if (currTime > s.objectiveValue) {
                                valid = false;
                                if(debugState >= 1) { 
                                    System.out.println("Failed to evacuate everyone before goal time from node " + evacNode
                                            + "(Still " + peopleNotEvacuated + " at time " + currTime + ")");
                                }
                            } else {
                                //System.out.println("currTime = " + currTime);
                                edgesData.get(currTime).get(currNode).put(follow,
                                        edgesData.get(currTime).get(currNode).get(follow) - usedCapa);
                                if (edgesData.get(currTime).get(currNode).get(follow) < 0) {
                                    valid = false;
                                    if(debugState >= 1) { 
                                        System.out.println("capacity constraint violated on edge (" + currNode + ", "
                                                + follow + ") (flow of " + usedCapa + " coming from " + evacNode
                                                + " at time " + currTime + ")");
                                    }
                                            valuePB = (-1) * edgesData.get(currTime).get(currNode).get(follow);
                                            nodePB = evacNode;
                                            origPB = currNode;
                                            destPB = follow;
                                }
                                currTime += d.getEdgeLength(currNode, follow);
                                currNode = follow;
                                // System.out.println("[NEXT] curr time = " + currTime + ", curr node = " +
                                // currNode + ", follow = " + follow + ", k = " + k);
                            }
                        }
                    }
                    currStart++; // go to next people packet departure
                }
            }
            else {
                valid = false;
                if(debugState >= 1) { 
                    System.out.println("Invalid start date (<0)");
                }
            }
		if(valid) {
            if(debugState >= 2) { 
                    System.out.println(" Evac ending at time " + currTime);
            }
                if(currTime > maxTime) {
                    maxTime = currTime;
                }
		}
            }
        }
        if (valid) {
            if(debugState >= 1) { 
                System.out.println("Capacity contraint validity passed");
            }

            // Due date constraint check => check that an edge is not used anymore after its
            // due date
            if(checkDueDate) {
                for (int edgeOrig : edgesData.get(0).keySet()) { // we loop on edges // Integer.MAX_VALUE means no due date
                    if (valid) {
                        for (int edgeDest : edgesData.get(0).get(edgeOrig).keySet()) {
                            int due_date = d.getEdgeDueDate(edgeOrig, edgeDest);
                            if (!((due_date == Integer.MAX_VALUE) || (due_date > s.objectiveValue)) && valid) { // if the
                                                                                                                // due date
                                // happens during
                                // evacuation
                                int edge_capa = d.getEdgeCapa(edgeOrig, edgeDest);
                                for (int i = due_date; i <= s.objectiveValue && valid; i++) { // we check that the edge is
                                                                                            // not used
                                    // anymore after its due date
                                    if (edgesData.get(i).get(edgeOrig).get(edgeDest) != edge_capa) {
                                        if(debugState >= 1) { 
                                            System.out.println("Due date constraint violated : edge (" + edgeOrig + ", "
                                                    + edgeDest + ") still used ("
                                                    + (edge_capa - edgesData.get(i).get(edgeOrig).get(edgeDest)) + "/"
                                                    + edge_capa + " used at time " + i + " (due date was " + due_date + ")) ");
                                        }
                                        valid = false;
                                    }
                                }
                            }

                        }
                    }
                }
            }

            if (valid) {
                if(debugState >= 1) { 
                    System.out.println("Due Date contraint validity passed");
                    System.out.println("Solution is VALID with value = " + s.objectiveValue + " unit times.");
                }
            } else {
                if(debugState >= 1) { 
                    System.out.println("Due Date contraint validity failed");
                    System.out.println("Solution is NOT VALID");
                }
            }

        } else {
            if(debugState >= 1) { 
                System.out.println("Capacity contraint validity failed");
                System.out.println("Solution is NOT VALID");
            }
            }
        if(valid) {
            maxTime++; // Pour prendre en compte l'instant t = 0;
            if(debugState >= 1) { 
                System.out.println("Total evac time = " + maxTime);
                System.out.println("------------------------------ Checker ending ------------------------------");
            }
            CheckerReturn r = new CheckerReturn();
            r.endingEvacTime = maxTime;
            return r; //-1 is not valid
        } else {
            if(debugState >= 1) { 
                System.out.println("------------------------------ Checker ending ------------------------------");
            }
            CheckerReturn r = new CheckerReturn(); // we fill in useful info for solution improvement
            r.endingEvacTime = -1;
            r.problematicNode = nodePB;
            r.exceedentFlow = valuePB;
            r.problematicNodes = new ArrayList<Integer>();
            for (int evacNode : s.evacNodesList.keySet()) {
                int a = evacNode;
                int b = -1;
                //System.out.println(evacNode + " yeye");
                for(int n : d.evac_paths.get(evacNode).following) { // because evac_paths is not an HashMap (should change that !)
                    b = n;
                    if((a == origPB) && (b == destPB)) {
                        r.problematicNodes.add(evacNode);
                    }
                    a = b;
                }      
            }   
            if(debugState == 3) {    
                printState();
            }
            return r;
        }
            
    }

    public void printState() {
        System.out.println("Evolution of the system : (" + edgesData.size() + " time units)");
        for (int i = 0; i < edgesData.size(); i++) {
            HashMap<Integer, HashMap<Integer, Integer>> aPath = edgesData.get(i);
            int time = i;
            System.out.println(" ---------- Unit Time " + time + " ----------");
            for (int edgeOrig : aPath.keySet()) {
                for (int edgeDest : aPath.get(edgeOrig).keySet()) {
                    System.out.println("Edge (" + edgeOrig + ", " + edgeDest + ") -> free capa = "
                            + aPath.get(edgeOrig).get(edgeDest));
                }
            }
        }
        System.out.println("End of display...");
        if (!valid) {
            System.out.println("Incorrect Solution !");
        }
    }
}
