import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.*;
import java.util.HashMap;
import java.io.FileWriter;


public class sortSolution {

    /*
        Sort the evacuation paths to meet online checker's requirements ;
        Writes the corresponding file at given name
    */
    public static int sortAndSaveSol(Solution sol, data d, String name) {
        FileWriter fWrit;
        File filew = new File(name);
        try{
        filew.delete();
        fWrit = new FileWriter(filew, true);
        fWrit.write(sol.instanceName+"\n");
        fWrit.write(sol.evacNodesNB+"\n");

        for(int i = 0 ; i<sol.evacNodesNB; i++) {
            for(int origin : sol.evacNodesList.keySet())
            {
                if(d.evac_paths.get(origin).uniqueID == i) {
                    fWrit.write(origin+" "+sol.evacNodesList.get(origin).evacRate+" "+sol.evacNodesList.get(origin).beginDate+"\n");
                }
            }
        }

        /* /////////////// */

        if (sol.nature)
        {
            fWrit.write("valid \n");
        }
        else
        {
            fWrit.write("invalid \n");
        }
        fWrit.write(sol.objectiveValue+"\n");
        fWrit.write(sol.computeTime+"\n");
        fWrit.write(sol.method+"\n");
        fWrit.write(sol.other);
        fWrit.flush();
        fWrit.close();
        return 0;

        }
        catch (IOException e){
            System.err.println("Create FileWRITER FAIL");
            e.printStackTrace();
            return 1;
        }
    }
}