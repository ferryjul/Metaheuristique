import java.util.ArrayList;
import java.util.HashMap;

public class Test {

static Boolean debug = false;
public static void main(String[] args) {

    if(debug) { // Simple example for debugging
        /* On crée l'exemple du TD */
        dataReader reader = new dataReader();
        SolutionIO solIO = new SolutionIO();
        data TD_data = new data();
        TD_data = reader.Convert_File_In_Data("donneetest.full");
        TD_data.read_data();
        /* On crée la solution du TD */
        Solution TD_sol = new Solution();
        /*TD_sol.instanceName="example TD";
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
        */
        TD_sol = solIO.read("solutiontest.txt");
        int test = solIO.write(TD_sol, "testwriteIo.txt");
        if (test==0)
        {
            System.out.println("Fichier solution créé \n");
        }
        else{
            System.out.println("[ERROR] Fichier solution fail \n");
        }
        Checker TD_checker = new Checker();
        TD_checker.check(TD_data, TD_sol);
        System.out.println("Valeur d'une borne inférieure : " + computeInfSup.computeInf(TD_data));
        System.out.println("Valeur d'une borne supérieure : " + computeInfSup.computeSup(TD_data));
        Solution infSol = computeInfSup.computeInfSolution(TD_data);
        int test2 = solIO.write(infSol, "testwriteIoInf.txt");
        if (test2==0)
        {
            System.out.println("Fichier solution inf créé \n");
        }
        else{
            System.out.println("[ERROR] Fichier solution inf fail \n");
        }
        Solution supSol = computeInfSup.computeSupSolution(TD_data);
        int test3 = solIO.write(supSol, "testwriteIoSup.txt");
        if (test3==0)
        {
            System.out.println("Fichier solution sup créé \n");
        }
        else{
            System.out.println("[ERROR] Fichier solution sup fail \n");
        }
        System.out.println("\n----- Test de la solution de la borne inf ----- ");
        Checker TD_checker1 = new Checker();
        TD_checker1.check(TD_data, infSol);
        System.out.println("\n----- Test de la solution de la borne sup ----- ");
        Checker TD_checker2 = new Checker();
        TD_checker2.check(TD_data, supSol);
        //TD_checker.printState();
        (new LocalSearch()).localSearch(TD_data, "exempleTD");
    }
    else {
        String inst = "sparse_10_30_3_1_I";
        String name = inst + ".full";
        // String name = "medium_10_30_3_5_I.full";
        String fileData = "../InstancesInt/" + name;   
        /* On crée l'exemple du TD */
        dataReader reader = new dataReader();
        SolutionIO solIO = new SolutionIO();
        data TD_data = new data();
        TD_data = reader.Convert_File_In_Data(fileData);
        TD_data.read_data();     
        System.out.println("Valeur d'une borne inférieure : " + computeInfSup.computeInf(TD_data));
        System.out.println("Valeur d'une borne supérieure : " + computeInfSup.computeSup(TD_data));
        Solution infSol = computeInfSup.computeInfSolution(TD_data);
        int test2 = solIO.write(infSol, "testwriteIoInf.txt");
        if (test2==0)
        {
            System.out.println("Fichier solution inf créé \n");
        }
        else{
            System.out.println("[ERROR] Fichier solution inf fail \n");
        }
        Solution supSol = computeInfSup.computeSupSolution(TD_data);
        int test3 = solIO.write(supSol, "testwriteIoSup.txt");
        if (test3==0)
        {
            System.out.println("Fichier solution sup créé \n");
        }
        else{
            System.out.println("[ERROR] Fichier solution sup fail \n");
        }
        System.out.println("\n----- Test de la solution de la borne inf ----- ");
        Checker TD_checker1 = new Checker();
        TD_checker1.check(TD_data, infSol);
        System.out.println("\n----- Test de la solution de la borne sup ----- ");
        Checker TD_checker2 = new Checker();
        TD_checker2.check(TD_data, supSol);
        //TD_checker.printState();
        (new LocalSearch()).localSearch(TD_data, inst);
    }

    }

}
