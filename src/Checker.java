import java.util.HashMap;
import java.util.ArrayList;

public class Checker {

    private ArrayList<HashMap<Integer, HashMap<Integer, Integer>>> edgesData; // for each time unit, for each edge (for
                                                                              // each origin, each dest), free capacity
    Boolean valid = true;

    public void check(data d, Solution s) {
        System.out.println("------------------------------ Beginning solution check ------------------------------");
        // Init data structure
        edgesData = new ArrayList<HashMap<Integer, HashMap<Integer, Integer>>>();
        for (int i = 0; i <= s.objectiveValue; i++) {
            edgesData.add(d.getEdgesCapas());
        }
        // Capacity contraint check
        for (int evacNode : s.evacNodesList.keySet()) { // Iterates on nodes to be evacuated
            if (valid) {
                System.out.println("[Evacuation of node " + evacNode + "]");
                int startDate = s.evacNodesList.get(evacNode).beginDate;
                int usedCapa = s.evacNodesList.get(evacNode).evacRate;
                int currStart = startDate;
                Path_data evacPath = null;
                for (Path_data ed : d.evac_paths) { // we find the right evacuation path
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
                    int currTime = currStart; // next packet departure !
                    System.out.println(" Still to evacuate : " + peopleNotEvacuated);
                    currNode = evacNode;
                    for (int follow : evacPath.following) { // we compute the impact of the current packet on every edge
                                                            // through time
                        // System.out.println("curr time = " + currTime + ", curr node = " + currNode +
                        // ", follow = " + follow);
                        if (valid) {
                            if (currTime > s.objectiveValue) {
                                valid = false;
                                System.out.println("Failed to evacuate everyone before goal time from node " + evacNode
                                        + "(Still " + peopleNotEvacuated + " at time " + currTime + ")");
                            } else {
                                edgesData.get(currTime).get(currNode).put(follow,
                                        edgesData.get(currTime).get(currNode).get(follow) - usedCapa);
                                if (edgesData.get(currTime).get(currNode).get(follow) < 0) {
                                    valid = false;
                                    System.out.println("capacity constraint violated on edge (" + currNode + ", "
                                            + follow + ") (flow of " + usedCapa + " coming from " + evacNode
                                            + " at time " + currTime + ")");
                                }
                                currTime += d.getEdgeLength(currNode, follow);
                                currNode = follow;
                                // System.out.println("[NEXT] curr time = " + currTime + ", curr node = " +
                                // currNode + ", follow = " + follow + ", k = " + k);
                            }
                        }
                    }
                    currStart++; // go to next people packet departur
                }
            }
        }
        if (valid) {
            System.out.println("Capacity contraint validity passed");

            // Due date constraint check => check that an edge is not used anymore after its
            // due date
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
                                    System.out.println("Due date constraint violated : edge (" + edgeOrig + ", "
                                            + edgeDest + ") still used ("
                                            + (edge_capa - edgesData.get(i).get(edgeOrig).get(edgeDest)) + "/"
                                            + edge_capa + " used at time " + i + " (due date was " + due_date + ")) ");
                                    valid = false;
                                }
                            }
                        }

                    }
                }
            }

            if (valid) {
                System.out.println("Due Date contraint validity passed");
                System.out.println("Solution is VALID");
            } else {
                System.out.println("Due Date contraint validity failed");
                System.out.println("Solution is NOT VALID");
            }

        } else {
            System.out.println("Capacity contraint validity failed");
            System.out.println("Solution is NOT VALID");
        }

        System.out.println("------------------------------ Checker ending ------------------------------");

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
