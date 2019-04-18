import java.util.ArrayList;
import java.util.HashMap;

public class Test {

public static void main(String[] args) {

    /* On crée l'exemple du TD */
    dataReader reader = new dataReader();
    data TD_data = new data();
    TD_data = reader.Convert_File_In_Data("donneetest.full");
    TD_data.read_data();
    /* On crée la solution du TD */
    Solution TD_sol = new Solution();
    TD_sol.instanceName="example TD";
    TD_sol.evacNodesNB=3;
    HashMap<Integer,EvacNodeData> hm = new HashMap<Integer,EvacNodeData>();
    hm.put(1,new EvacNodeData(8,3)); //(9,3)
    hm.put(2,new EvacNodeData(5,0));
    hm.put(3,new EvacNodeData(3,0));
    TD_sol.evacNodesList=hm;
    TD_sol.computeTime = 1000;
    TD_sol.objectiveValue = 37;
    TD_sol.method = "resolu à la main";
    TD_sol.other = "Olivier - on connait une meilleure solution";
    Checker TD_checker = new Checker();
    TD_checker.check(TD_data, TD_sol);
    //TD_checker.printState();
    }

}
