import java.util.HashMap;

public class Solution {
    public String instanceName;
    public int evacNodesNB;
    public HashMap<Integer,EvacNodeData> evacNodesList;
    public Boolean nature; // true for valid, false for invalid
    public int objectiveValue;
    public int computeTime;
    public String method;
    public String other;

    public Solution(Solution sol) { // copy constructor
        this.instanceName = sol.instanceName;
        this.evacNodesNB = sol.evacNodesNB;
        this.evacNodesList = (HashMap<Integer,EvacNodeData>) sol.evacNodesList.clone();
        this.nature = sol.nature;
        this.objectiveValue = sol.objectiveValue;
        this.computeTime = sol.computeTime;
        this.method = sol.method;
        this.other = sol.other;
    }

    public Solution() {
        
    }

}