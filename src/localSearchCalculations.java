import java.util.ArrayList;
import java.util.HashMap;

public class localSearchCalculations implements Runnable {

    private int stepValue = 1;
    int compressNb = 0;
    int compressRNb = 0;
    int debug = 2;
    int initVal; 
    Boolean maxRatesComputed = false;
    Boolean respDueDates = false; // set true if you want to generate solutions taking into account due dates
    HashMap<Integer, Integer> maxEvacRates;

    data globalData;
    Solution globalSol;
    Integer index;

    public localSearchCalculations(data d, Solution s, Integer index) {
        this.globalData = d;
        this.globalSol = s;
        this.index = index;
    }

    public void run() {
        Solution aVal = localSearchIntern(globalData,  globalSol);
        synchronized(LocalSearch.values) {
            LocalSearch.values.add(aVal);
        }
        synchronized(LocalSearch.counter) {
            LocalSearch.counter++;
        }
    }

    private ArrayList<Solution> computeNeighbours(data d, Solution initSol) {
        //System.out.println("Computing Neighbours...");
        ArrayList<Solution> neighbours = new ArrayList<Solution>();
        for(int evacNode : initSol.evacNodesList.keySet()) {
            int startDate = initSol.evacNodesList.get(evacNode).beginDate;
            int rate = initSol.evacNodesList.get(evacNode).evacRate;
            //System.out.println("start date = " + startDate + " ; stepValue = " + stepValue);
            if(startDate >= stepValue) {
                Solution bestSol = new Solution(initSol);
                bestSol.evacNodesList.put(evacNode, new EvacNodeData(rate, startDate-stepValue));
                //System.out.println("Node " + evacNode + " will evacuate at time " + (startDate-stepValue) + " instead of " + (startDate));
                neighbours.add(bestSol);
            }
        }
        return neighbours;
    }

    private Solution findBest(data d, Solution sol) {
        this.stepValue = sol.objectiveValue/2;
        int bestValue = sol.objectiveValue;
       // System.out.println("[Compression phase " + compressNb + "] Looking for best compression... (initial value = " + bestValue + ")");
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
                   //    System.out.println("[Compression phase " + compressNb + "] Best solution found (cost = " + val + ")");
                    }
                    bestValue = val;
                    bestSol = s;
                }
            }
            if(!foundBest) {
                stepValueInt = stepValue / 2;
            }
        }
        if(debug >= 2) {
            System.out.println("[THREAD" + index + "] " + "[Compression phase " + compressNb + "] Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");
        }
        compressNb++;
        return findBestRate(d, bestSol);
    }

    private ArrayList<Solution> computeNeighboursRate(data d, Solution initSol) {
        //System.out.println("Computing Neighbours...");
        ArrayList<Solution> neighbours = new ArrayList<Solution>();
        for(int evacNode : initSol.evacNodesList.keySet()) {
            int startDate = initSol.evacNodesList.get(evacNode).beginDate;
            int rate = initSol.evacNodesList.get(evacNode).evacRate;
            //System.out.println("start date = " + startDate + " ; stepValue = " + stepValue);
            if(maxEvacRates.get(evacNode) > rate) {
                Solution bestSol = new Solution(initSol);
                bestSol.evacNodesList.put(evacNode, new EvacNodeData(rate+1, startDate));
                //System.out.println("Node " + evacNode + " will evacuate at time " + (startDate-stepValue) + " instead of " + (startDate));
                neighbours.add(bestSol);
            }
        }
        return neighbours;
    }

    private Solution findBestRate(data d, Solution sol) {
        if(!maxRatesComputed) { // if first time then we compute max rates once
            maxEvacRates = new HashMap<Integer, Integer>();
            for(Path_data evacPath : d.evac_paths.values()) {
                int currNode = evacPath.origin;
                int minCapa = Integer.MAX_VALUE;
                for(int nextNode : evacPath.following) {                   
                    if(d.getEdgeCapa(currNode, nextNode) < minCapa) {
                        minCapa = d.getEdgeCapa(currNode, nextNode);
                    }
                    currNode = nextNode;
                }              
                maxEvacRates.put(evacPath.origin, minCapa);
            }
            maxRatesComputed = true;
        } // Now maxEvacRates(x) is the max rate possible for evac path of node x
        int bestValue = sol.objectiveValue;
      //  System.out.println("[Rate augment phase " + compressRNb + "] ... (initial value = " + bestValue + ")");
        Solution bestSol = sol;
        int nb = 0;    
        Boolean foundBest = true;
        while(foundBest) { // stepValue accélère la première compression (car la borne sup est très grossière) puis vaut 1 pour la suite
            foundBest = false;

            for(Solution s : computeNeighboursRate(d, bestSol)) {
                Checker ch = new Checker();
                nb++;
                int val = ch.check(d, s).endingEvacTime;
                s.objectiveValue = val;
                if((val > 0) && (val <= bestValue)) {
                    foundBest = true;
                    if(val != bestValue)  {
                    //    System.out.println("[Rate augment phase " + compressRNb + "] Best solution found (cost = " + val + ")");
                    }
                    bestValue = val;
                    bestSol = s;
                }
            }
    
        }
        if(debug >= 2) {
            System.out.println("[THREAD" + index + "] " + "[Rate augment phase " + compressRNb + "] Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");
        }
        compressRNb++;
        return bestSol;
    }

    private ArrayList<Solution> modifyRates(data d, Solution sol) {
        if(debug >= 2) {
            System.out.println("[THREAD" + index + "] " + "Trying to reduce rates to allow parallelization...");
        }
        // find where the limitant edge is
        ArrayList<Solution> r = new ArrayList<Solution>();
        Checker ch = (new Checker(true));
        //ch.debugState = 1;
        CheckerReturn solAnalysis = ch.check(d, sol);
        if(solAnalysis.endingEvacTime != -1) { // If the solution is valid, something went wrong
            Checker ch2 = (new Checker(true)); // So we display it
            ch2.debugState = 1;
            ch2.check(d, sol);
        }
        for(int aNode : solAnalysis.problematicNodes) { // Unfair reductions loop
            Solution bestSol = new Solution(sol);
            //System.out.println("pb at node " + aNode + " (exceedent flow = " + solAnalysis.exceedentFlow + ")");
            if(sol.evacNodesList.get(aNode).evacRate - solAnalysis.exceedentFlow > 0) {
                bestSol.evacNodesList.put(aNode, new EvacNodeData(sol.evacNodesList.get(aNode).evacRate - solAnalysis.exceedentFlow, sol.evacNodesList.get(aNode).beginDate));
                //System.out.println("Node " + aNode + " will now evacuate at rate " + bestSol.evacNodesList.get(aNode).evacRate + " instead of " + sol.evacNodesList.get(aNode).evacRate);
                /* Now we try to make it feasible by shifting following tasks*/
                int factor1 = (d.evac_paths.get(aNode).population)/(bestSol.evacNodesList.get(aNode).evacRate);
                if((d.evac_paths.get(aNode).population)%(bestSol.evacNodesList.get(aNode).evacRate) != 0) {
                    factor1++;
                }
                int factor2 = (d.evac_paths.get(aNode).population)/(sol.evacNodesList.get(aNode).evacRate);
                if((d.evac_paths.get(aNode).population)%(sol.evacNodesList.get(aNode).evacRate) != 0) {
                    factor2++;
                }
                int factor = factor1 - factor2 + 2;
                if(factor < 0) { // Should not happen...
                  //  System.out.println("node " + aNode + " was evacuating at rate " + sol.evacNodesList.get(aNode).evacRate + " now at rate " + bestSol.evacNodesList.get(aNode).evacRate);
                }
                bestSol.objectiveValue+=factor;
                //System.out.println("factor = " + factor);
                for(Integer anotherEvacNode : bestSol.evacNodesList.keySet()) { // We shift all following evacuations
                    if(bestSol.evacNodesList.get(aNode).beginDate < bestSol.evacNodesList.get(anotherEvacNode).beginDate) {
                       // System.out.println("shifting node " + anotherEvacNode + " from date " + bestSol.evacNodesList.get(anotherEvacNode).beginDate + " to " + (bestSol.evacNodesList.get(anotherEvacNode).beginDate+factor));
                        bestSol.evacNodesList.put(anotherEvacNode, new EvacNodeData(bestSol.evacNodesList.get(anotherEvacNode).evacRate, bestSol.evacNodesList.get(anotherEvacNode).beginDate + factor));
                    }
                }
                if(factor < 2*sol.objectiveValue) { // if generated solution is not too long (arbitrary measure)
                    r.add(bestSol);
                }
                /* to be removed : many informations display */
                /*System.out.println("SOLUTION GEN ANALYSIS :");
                System.out.println("Original begin time was : " + sol.evacNodesList.get(aNode).beginDate);
                System.out.println("factor was : " + factor + " and original ending time was : " + (sol.evacNodesList.get(aNode).beginDate + factor2));
                System.out.println("new ending time is : " + (sol.evacNodesList.get(aNode).beginDate + factor1));
                Checker chk = (new Checker(true));
                chk.debugState = 1;
                chk.check(d, bestSol);*/

            }
        }
        int total = solAnalysis.exceedentFlow;
        int average = (total/(solAnalysis.problematicNodes.size()));
        int rest = total%(solAnalysis.problematicNodes.size());
        // We also compute a "fair" solution
        Boolean first = true;
        int minBeginDate = Integer.MAX_VALUE;
        int maxFactor = Integer.MIN_VALUE;
        Solution bestSolAv = new Solution(sol);
        Boolean make = true; // Because this solution constr is often impossible like this
        for(int aNode : solAnalysis.problematicNodes) {
            if(first) { // choix arbitraire : si nbre non multiple on donne plus au premier noeud à évacuer
                first = false;
                bestSolAv.evacNodesList.put(aNode, new EvacNodeData(sol.evacNodesList.get(aNode).evacRate - average - rest, sol.evacNodesList.get(aNode).beginDate));
            }
            else {
                bestSolAv.evacNodesList.put(aNode, new EvacNodeData(sol.evacNodesList.get(aNode).evacRate - average, sol.evacNodesList.get(aNode).beginDate));
            }
            int factor1 = (d.evac_paths.get(aNode).population)/(bestSolAv.evacNodesList.get(aNode).evacRate);
            if((d.evac_paths.get(aNode).population)%(bestSolAv.evacNodesList.get(aNode).evacRate) != 0) {
                factor1++;
            }
            int factor2 = (d.evac_paths.get(aNode).population)/(sol.evacNodesList.get(aNode).evacRate);
            if((d.evac_paths.get(aNode).population)%(sol.evacNodesList.get(aNode).evacRate) != 0) {
                factor2++;
            }
            int factor = factor1 - factor2 + 1;
            if(factor < 0) {
                make = false;
                break;
               // System.out.println("Fair alt node " + aNode + " was evacuating at rate " + sol.evacNodesList.get(aNode).evacRate + " now at rate " + bestSolAv.evacNodesList.get(aNode).evacRate);
            } else {
                if(factor > maxFactor) {
                    maxFactor = factor;
                }
                if(sol.evacNodesList.get(aNode).beginDate < minBeginDate) {
                    minBeginDate = sol.evacNodesList.get(aNode).beginDate;
                }
            }           
        }
        bestSolAv.objectiveValue+=maxFactor;
        for(Integer anotherEvacNode : bestSolAv.evacNodesList.keySet()) {
            if(minBeginDate < bestSolAv.evacNodesList.get(anotherEvacNode).beginDate) {
                bestSolAv.evacNodesList.put(anotherEvacNode, new EvacNodeData(bestSolAv.evacNodesList.get(anotherEvacNode).evacRate, bestSolAv.evacNodesList.get(anotherEvacNode).beginDate + maxFactor));
            }
        }
        if(make) {
            if(maxFactor < 2*sol.objectiveValue) { // if generated solution is not too long (arbitrary measure)
                r.add(bestSolAv);
            }
        }
        return r;
    }

  /*  private ArrayList<Solution> trySwaps(data d, Solution s) { // returns a list of solutions with a different order
        ArrayList<Solution> ret = new ArrayList<Solution>();
        for()
        return ret;
    }*/

    private Solution localSearchIntern(data d, Solution s) {
        this.initVal = s.objectiveValue;
        int test = (new Checker()).check(d, s).endingEvacTime;
        Solution baseSol = findBest(d,s);
        /* Due date check */
        Checker checkD = (new Checker());
        checkD.checkDueDate = respDueDates;
        baseSol.objectiveValue = checkD.check(d, baseSol).endingEvacTime;
        int bestValue = baseSol.objectiveValue;
        if(bestValue == -1) {
            Solution sh = new Solution();
            sh.objectiveValue = Integer.MAX_VALUE;
            return sh;
        }
        Solution bestSol = baseSol;
        int nb = 0;
        Boolean foundBest = true;
    //    System.out.println("[MAIN] Will try to improve solution at cost " + bestValue + " (former cost was " + test + "))");
        ArrayList<Solution> explSolsList = new ArrayList<Solution>();
        int max_iterations = 0; // Useless now
        int i = max_iterations;
        while(foundBest || i > 0) {
        //    System.out.println("new loop");
            ArrayList<Solution> explSolsListBis = new ArrayList<Solution>();
            foundBest = false;
            if(i == max_iterations) {        
                stepValue = 1;  // So generated neighbours stay close     
                for(Solution s1 : computeNeighbours(d, bestSol)) { // boucle partant de la solution initiale dans tous les sens
                    ArrayList<Solution> solList = modifyRates(d, s1);
                    for(Solution sol : solList) {
                        //sol.objectiveValue = initVal*4; // Pour ne pas que le checker fail à cause de ça (car on a sûrement allongé la durée de l'évac en diminuant le débit)
                        Checker ch = (new Checker());
                        //ch.debugState = 1;
                        ch.checkDueDate = respDueDates;
                        sol.objectiveValue = ch.check(d,sol).endingEvacTime;
                        if(sol.objectiveValue != -1) {
                            Solution compactedNewSol0 = findBest(d, sol); // 2 cycles pour être sûr de réduire au max
                            Solution compactedNewSol = findBest(d, compactedNewSol0);
                            //Solution compactedNewSol = (d, compactedSol);
                            int val = (new Checker()).check(d, compactedNewSol).endingEvacTime;
                            compactedNewSol.objectiveValue = val;
                            //explSolsList.add(compactedNewSol);
                            if((val > 0) && (val < bestValue)) {
                                if(debug >= 2) {
                                    System.out.println("[THREAD" + index + "] " + "New best solution at cost : " + val);
                                }
                                foundBest = true;
                                bestSol = compactedNewSol;
                                bestValue = val;
                                nb++;
                                i = max_iterations+1;
                            }
                            if(val > 0) {
                                explSolsListBis.add(compactedNewSol);
                            }
                        }
                        else {
                       //     System.out.println("Should not be printed...");
                            explSolsListBis.add(sol);
                        } 
                    }     
                                            
                }   
            } else {
           //     System.out.println("Solution was not improved ; trying to improve generated solutions (loop " + i + " improving " + explSolsList.size() + " former sols)");
                //System.out.println(i + " : " + explSolsList.size());
                for(Solution s1 : explSolsList) { // boucle sur les solutions du coup précédent
                    stepValue = 1;
                    ArrayList<Solution> solList = new ArrayList<Solution>();
                    if(s1.objectiveValue == -1) {
                        solList.add(s1);
                    } else {
                        ArrayList<Solution> neighbours = computeNeighbours(d,s1);
                        //System.out.println("-->" + neighbours.size());
                        for(Solution k : neighbours) {
                            for(Solution l : modifyRates(d, k)) {
                                solList.add(l);
                            }
                        }
                    }
                   // System.out.println("> Will iterate over " + solList.size());
                    for(Solution sol : solList) {
                        sol.objectiveValue = initVal*4; // Pour ne pas que le checker fail à cause de ça (car on a sûrement allongé la durée de l'évac en diminuant le débit)
                        sol.objectiveValue = (new Checker()).check(d,sol).endingEvacTime;
                        if(sol.objectiveValue != -1) {
                            Solution compactedNewSol = findBest(d, sol);
                            int val = (new Checker()).check(d, compactedNewSol).endingEvacTime;
                            //explSolsList.add(compactedNewSol);
                            compactedNewSol.objectiveValue = val;
                            if((val > 0) && (val <= bestValue)) {
                                foundBest = true;
                                bestSol = compactedNewSol;
                                bestValue = val;
                                nb++;
                                i = max_iterations+1;
                            }
                            if(val > 0) {
                                explSolsListBis.add(compactedNewSol);
                            }
                        }
                        else {
                            explSolsListBis.add(sol);
                        } 
                    }                  
                } 
            }
            explSolsList = explSolsListBis;
            //System.out.println(explSolsListBis.size());
            i--;            
        }
        bestSol.objectiveValue = (new Checker()).check(d, bestSol).endingEvacTime;
       // System.out.println("Explored " + nb + " rate diminutions.");
      //  System.out.println("Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");   
      if(debug >= 1) {
        System.out.println("[THREAD" + index + "] " + "Explored " + nb + " rate diminutions.");
        System.out.println("[THREAD" + index + "] " + "Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");     
      }     
        return bestSol;
    }

}