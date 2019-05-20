import java.util.ArrayList;
import java.lang.Object;

public class LocalSearch {

    private int stepValue = 1;
    int compressNb = 0;
    int initVal; 

    private ArrayList<Solution> computeNeighbours(data d, Solution initSol) {
        ArrayList<Solution> neighbours = new ArrayList<Solution>();
        for(int evacNode : initSol.evacNodesList.keySet()) {
            int startDate = initSol.evacNodesList.get(evacNode).beginDate;
            int rate = initSol.evacNodesList.get(evacNode).evacRate;
            if(startDate >= stepValue) {
                Solution bestSol = new Solution(initSol);
                bestSol.evacNodesList.put(evacNode, new EvacNodeData(rate, startDate-stepValue));
                //System.out.println("Node " + evacNode + " will evacuate at time " + (startDate-stepValue) + " instead of " + (startDate ));
                neighbours.add(bestSol);
            }
        }
        return neighbours;
    }

    private Solution findBest(data d, Solution sol) {
        this.stepValue = sol.objectiveValue/2;
        int bestValue = sol.objectiveValue;
        System.out.println("[Compression phase " + compressNb + "] Looking for best compression... (initial value = " + bestValue + ")");
        Solution bestSol = sol;
        int nb = 0;
        int stepValueInt = stepValue;
        Boolean foundBest = true;
        while(foundBest || (stepValue != 1)) { // stepValue accélère la première compression (car la borne sup est très grossière) puis vaut 1 pour la suite
            foundBest = false;
            stepValue = stepValueInt;
            for(Solution s : computeNeighbours(d, bestSol)) {
                Checker ch = new Checker();
                nb++;
                int val = ch.check(d, s).endingEvacTime;
                s.objectiveValue = val;
                if((val > 0) && (val <= bestValue)) {
                    foundBest = true;
                    if(val != bestValue)  {
                        System.out.println("[Compression phase " + compressNb + "] Best solution found (cost = " + val + ")");
                    }
                    bestValue = val;
                    bestSol = s;
                }
            }
            if(!foundBest) {
                stepValueInt = stepValue / 2;
            }
        }
        System.out.println("[Compression phase " + compressNb + "] Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");
        compressNb++;
        return bestSol;
    }

    private ArrayList<Solution> modifyRates(data d, Solution sol) {
        System.out.println("Trying to reduce rates to allow parallelization...");
        // find where the limitant edge is
        ArrayList<Solution> r = new ArrayList<Solution>();
        sol.objectiveValue = initVal*4;
        CheckerReturn solAnalysis = (new Checker(true)).check(d, sol);
        for(int aNode : solAnalysis.problematicNodes) { // Unfair reductions loop
            Solution bestSol = new Solution(sol);
            //System.out.println("pb at node " + aNode + " (exceedent flow = " + solAnalysis.exceedentFlow + ")");
            if(sol.evacNodesList.get(aNode).evacRate - solAnalysis.exceedentFlow > 0) {
                bestSol.evacNodesList.put(aNode, new EvacNodeData(sol.evacNodesList.get(aNode).evacRate - solAnalysis.exceedentFlow, sol.evacNodesList.get(aNode).beginDate));
                System.out.println("Node " + aNode + " will now evacuate at rate " + bestSol.evacNodesList.get(aNode).evacRate + " instead of " + sol.evacNodesList.get(aNode).evacRate);
                r.add(bestSol);
            }
        }
        int total = solAnalysis.exceedentFlow;
        int average = (total/(solAnalysis.problematicNodes.size()));
        int rest = total%(solAnalysis.problematicNodes.size());
        // We also compute a "fair" solution
        Boolean first = true;
        Solution bestSolAv = new Solution(sol);
        for(int aNode : solAnalysis.problematicNodes) {
            if(first) { // choix arbitraire : si nbre non multiple on donne plus au premier noeud à évacuer
                first = false;
                bestSolAv.evacNodesList.put(aNode, new EvacNodeData(sol.evacNodesList.get(aNode).evacRate - average - rest, sol.evacNodesList.get(aNode).beginDate));
            }
            else {
                bestSolAv.evacNodesList.put(aNode, new EvacNodeData(sol.evacNodesList.get(aNode).evacRate - average, sol.evacNodesList.get(aNode).beginDate));
            }
        }
        r.add(bestSolAv);
        return r;
    }

    public void localSearch(data d, Solution s) {
        this.initVal = s.objectiveValue;
        int test = (new Checker()).check(d, s).endingEvacTime;
        Solution baseSol = findBest(d,s);
        baseSol.objectiveValue = (new Checker()).check(d, baseSol).endingEvacTime;
        int bestValue = baseSol.objectiveValue;
        Solution bestSol = baseSol;
        int nb = 0;
        Boolean foundBest = true;
        System.out.println("[MAIN] Will try to improve solution at cost " + bestValue + " (former cost was " + test + "))");
        ArrayList<Solution> explSolsList = new ArrayList<Solution>();
        while(foundBest) {
            foundBest = false;
            for(Solution s1 : computeNeighbours(d, bestSol)) { // boucle partant de la solution initiale dans tous les sens
                ArrayList<Solution> solList = modifyRates(d, s1);
                for(Solution sol : solList) {
                    sol.objectiveValue = initVal*4; // Pour ne pas que le checker fail à cause de ça (car on a sûrement allongé la durée de l'évac en diminuant le débit)
                    sol.objectiveValue = (new Checker()).check(d,sol).endingEvacTime;
                    if(sol.objectiveValue != -1) {
                        Solution compactedNewSol = findBest(d, sol);
                        int val = (new Checker()).check(d, compactedNewSol).endingEvacTime;
                        //explSolsList.add(compactedNewSol);
                        if((val > 0) && (val <= bestValue)) {
                            foundBest = true;
                            bestSol = compactedNewSol;
                            bestValue = val;
                            nb++;
                        }
                        if(val > 0) {
                            explSolsList.add(compactedNewSol);
                        }
                    }
                    else {
                        //explSolsList.add(sol);
                    } 
                }     
                                           
            }   
            /*
            System.out.println("Trying to improve " + explSolsList.size() + " potential solutions after rates diminution...");
            for(Solution s1 : explSolsList) { // boucle partant de chaque solution bricolée et compactée
                if(s1.objectiveValue == -1) {
                    ArrayList<Solution> solList = modifyRates(d, s1);
                        for(Solution sol : solList) {
                            sol.objectiveValue = initVal; // Pour ne pas que le checker fail à cause de ça (car on a sûrement allongé la durée de l'évac en diminuant le débit)
                            sol.objectiveValue = (new Checker()).check(d,sol).endingEvacTime;
                            if(sol.objectiveValue != -1) {
                                Solution compactedNewSol = findBest(d, sol);
                                int val = (new Checker()).check(d, compactedNewSol).endingEvacTime;
                                if((val > 0) && (val <= bestValue)) {
                                    foundBest = true;
                                    bestSol = compactedNewSol;
                                    bestValue = val;
                                    nb++;
                                }
                            }
                        }
                }
                else {
                    for(Solution s2 : computeNeighbours(d, s1)) {
                        ArrayList<Solution> solList = modifyRates(d, s2);
                        for(Solution sol : solList) {
                            sol.objectiveValue = initVal; // Pour ne pas que le checker fail à cause de ça (car on a sûrement allongé la durée de l'évac en diminuant le débit)
                            sol.objectiveValue = (new Checker()).check(d,sol).endingEvacTime;
                            if(sol.objectiveValue != -1) {
                                Solution compactedNewSol = findBest(d, sol);
                                int val = (new Checker()).check(d, compactedNewSol).endingEvacTime;
                                if((val > 0) && (val <= bestValue)) {
                                    foundBest = true;
                                    bestSol = compactedNewSol;
                                    bestValue = val;
                                    nb++;
                                }
                            }
                        }
                    }
                }
            } */
            //foundBest = false;
            
        }
        (new Checker()).check(d, bestSol);
        System.out.println("Explored " + nb + " rate diminutions.");
        System.out.println("Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");       
    }
}