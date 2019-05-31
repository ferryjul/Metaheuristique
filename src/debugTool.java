import java.util.ArrayList;
import java.util.HashMap;

public class debugTool {

static Boolean debugTool = false;
public static void main(String[] args) {

        String inst = "sparse_10_30_3_10_I";
        String name = inst + ".full";
        // String name = "medium_10_30_3_5_I.full";
        String fileData = "../InstancesInt/" + name;   
        /* On crée l'exemple du TD */
        dataReader reader = new dataReader();
        SolutionIO solIO = new SolutionIO();
        data TD_data = new data();
        TD_data = reader.Convert_File_In_Data(fileData);
        TD_data.read_data();     
        Solution sTested = solIO.read("../Generated_best_solutions/test_Tim_sparse_10_30_3_10_I");
        Checker c = new Checker();
        c.debugState = 2;
        c.check(TD_data, sTested);
        System.out.println("Inf : " + computeInfSup.computeInf(TD_data));
    }

}
