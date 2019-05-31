import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.Object;

/**
Calculation was first not intented to be multi threaded, so we kept all the original code in this file.
*/

public class LocalSearch {

    public static volatile ArrayList<Solution> values = new ArrayList<Solution>();
    public static volatile Integer counter = 0;
    private int stepValue = 1;
    int compressNb = 0;
    int compressRNb = 0;
    int initVal; 
    Boolean maxRatesComputed = false;
    Boolean respDueDates = false;
    HashMap<Integer, Integer> maxEvacRates;

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
        System.out.println("[Rate augment phase " + compressRNb + "] ... (initial value = " + bestValue + ")");
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
                        System.out.println("[Rate augment phase " + compressRNb + "] Best solution found (cost = " + val + ")");
                    }
                    bestValue = val;
                    bestSol = s;
                }
            }
    
        }
        System.out.println("[Rate augment phase " + compressRNb + "] Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");
        compressRNb++;
        return bestSol;
    }

    private ArrayList<Solution> modifyRates(data d, Solution sol) {
        System.out.println("Trying to reduce rates to allow parallelization...");
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
                    System.out.println("node " + aNode + " was evacuating at rate " + sol.evacNodesList.get(aNode).evacRate + " now at rate " + bestSol.evacNodesList.get(aNode).evacRate);
                }
                bestSol.objectiveValue+=factor;
                //System.out.println("factor = " + factor);
                for(Integer anotherEvacNode : bestSol.evacNodesList.keySet()) { // We shift all following evacuations
                    if(bestSol.evacNodesList.get(aNode).beginDate < bestSol.evacNodesList.get(anotherEvacNode).beginDate) {
                        System.out.println("shifting node " + anotherEvacNode + " from date " + bestSol.evacNodesList.get(anotherEvacNode).beginDate + " to " + (bestSol.evacNodesList.get(anotherEvacNode).beginDate+factor));
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
        System.out.println("[MAIN] Will try to improve solution at cost " + bestValue + " (former cost was " + test + "))");
        ArrayList<Solution> explSolsList = new ArrayList<Solution>();
        int max_iterations = 0; // Useless now
        int i = max_iterations;
        while(foundBest || i > 0) {
            System.out.println("new loop");
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
                            System.out.println("\n\nShould not be printed...");
                            explSolsListBis.add(sol);
                        } 
                    }     
                                            
                }   
            } else {
                System.out.println("Solution was not improved ; trying to improve generated solutions (loop " + i + " improving " + explSolsList.size() + " former sols)");
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
                    System.out.println("> Will iterate over " + solList.size());
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
        System.out.println("Explored " + nb + " rate diminutions.");
        System.out.println("Best solution found at cost " + bestValue + "(explored  " + nb + " solutions)");       
        return bestSol;
    }

    public ArrayList<Integer> generateRandomSeq(HashMap<Integer, Path_data> evacPaths) {
        ArrayList<Integer> retList = new ArrayList<Integer>();
        // generate list of values of seq
        ArrayList<Integer> vals = new ArrayList<Integer>();
        for(Integer aNode : evacPaths.keySet()) {
            vals.add(aNode);
        }
        // generate random variation of seq
        Random rand = new Random();
        while(vals.size() != 0) {
            int randomIndex = rand.nextInt(vals.size());
            int newVal = vals.get(randomIndex);
            retList.add(newVal);
            vals.remove(randomIndex);
        }
        return retList;
    }

    public void localSearch(data d, String name) { // We will try to implement multi-start
        Long startTime = java.lang.System.currentTimeMillis();
        int multiStartNbPoints = 1;
        Boolean useMultiThreading = true;
        int nbThreads = 2;
        System.out.println("Generating multi start points...");
        ArrayList<Solution> starts = new ArrayList<Solution>();
        Solution s = computeInfSup.computeSupSolution(d);
        if((new Checker()).check(d,s).endingEvacTime > 0) {
            starts.add(s);
            System.out.println("Generated the sup solution at cost " + s.objectiveValue + "! :)");
         } else { // Simple précaution, ne devrait pas arriver
             System.out.println("Generated a wrong solution ! :( (Cost was supposed to be " + s.objectiveValue + " )");
         }
        for(int i = 0 ; i < multiStartNbPoints ; i++) {
             ArrayList<Integer> randomPerm = generateRandomSeq(d.evac_paths);
             Solution sol = new Solution();
             sol.other = "Solution for a sup solution random permutation (should be always possible for real)";
             sol.method = "Automatically generated by LocalSearch.java methods";
             sol.instanceName = "Sup Solution auto-generated";
             sol.nature = true; // In general case
             sol.evacNodesNB = d.evac_paths.size();
             sol.evacNodesList = new HashMap<Integer,EvacNodeData>();
             int totalTime = 0;
             System.out.println(" --------------------------- Permutation " + (i+1));
             for(int evacNode : randomPerm) {
                Path_data evacPath = d.evac_paths.get(evacNode); 
                System.out.println("evacNode = " + evacNode);              
                int travel_time = 0;
                // we compute the sum of the travel times of each edge on the path
                int currNode = evacPath.origin;
                int minCapa = Integer.MAX_VALUE;
                for(int nextNode : evacPath.following) {
                    travel_time+=d.getEdgeLength(currNode, nextNode);
                    if(d.getEdgeCapa(currNode, nextNode) < minCapa) {
                        minCapa = d.getEdgeCapa(currNode, nextNode);
                    }
                    currNode = nextNode;
                }
                int evacTime = evacPath.population / minCapa;
                if((evacPath.population % minCapa) != 0) {
                    evacTime++; // Pour "faire passer" le dernier paquet
                }
                evacTime+=travel_time; // durée de trajet des paquets jusqu'au point sûr
                
                int beginTime = totalTime; 
                totalTime+=evacTime; // next evacuation will begin while this one is totally finished
                sol.evacNodesList.put(evacNode, new EvacNodeData(minCapa, beginTime));
        
             }
             sol.objectiveValue = totalTime;
             if((new Checker()).check(d,sol).endingEvacTime > 0) {
                starts.add(sol);
                System.out.println("Generated a good solution at cost " + totalTime + "! :)");
             } else { // Simple précaution, ne devrait pas arriver
                 System.out.println("Generated a wrong solution ! :(");
             }
            
        }
        int bestVal = Integer.MAX_VALUE;
        Solution bestSolutionComputed = new Solution();
        if(useMultiThreading) {
            int k = 0;
            int thr_c = 0;
            ArrayList<Thread> threadList = new ArrayList<Thread>();
            while(k < starts.size()) {
                if(thr_c < nbThreads) {
                    Solution sol = starts.get(k);
                    Thread t = new Thread(new localSearchCalculations(d, sol, k));
                    threadList.add(t);
                    t.start();
                    System.out.println("[MAIN THREAD] Launching new Thread");
                    thr_c++;
                    k++;
                } else {
                    for(Thread t : threadList) {
                        try {
                            t.join();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        threadList.remove(t);
                        thr_c--;
                        System.out.println(counter + " threads have finished working");
                        break; // Pour en re créer un desuite
                    }
                }
            }
            for(Thread t : threadList) {
                try {
                    t.join();
                } catch(Exception e) {
                    e.printStackTrace();
                }              
                System.out.println(counter + " threads have finished working");
            }
            /*for(Solution sol : starts) {
                Thread t = new Thread(new localSearchCalculations(d, sol, k));
                threadList.add(t);
                t.start();
                k++;
            }
            //while(counter != multiStartNbPoints+1) {System.out.println("counter = " + counter);}
            for(Thread t : threadList) {
                try {
                    t.join();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                System.out.println(counter + " threads have finished working");
            }*/
            for(Solution aVal : values) {
                if(aVal.objectiveValue < bestVal) {
                    bestVal = aVal.objectiveValue;
                    bestSolutionComputed = aVal;
                }
            }
        } else {
            for(Solution sol : starts) {
                System.out.println("------------------------------------");
                Solution aVal = localSearchIntern(d,  sol);
                if(aVal.objectiveValue < bestVal) {
                    bestVal = aVal.objectiveValue;
                    bestSolutionComputed = aVal;
                }
            }
        }
        Long endTime = java.lang.System.currentTimeMillis();
        System.out.println("Local search took " + (((Long)(endTime - startTime)).intValue())/1000 + " seconds");      
        System.out.println("End of local search ; best value reached = " + bestVal + " (tried " + starts.size() + " starting points)");   
       /* for(Solution sol : starts) {
            Checker ch = (new Checker());
            ch.debugState = 2;
            ch.check(d, sol);
        }*/
        // Save the result
        SolutionIO solIO = new SolutionIO();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
	    Date date = new Date();
        if(useMultiThreading) {
            bestSolutionComputed.other = "Solution generated using multi-threading";
        } else {
            bestSolutionComputed.other = "Solution generated without using multi-threading";
        }
        bestSolutionComputed.instanceName = name;
        bestSolutionComputed.computeTime = (((Long)(endTime - startTime)).intValue())/1000;
        //int test = solIO.write(bestSolutionComputed, "../Generated_best_solutions/" + name + dateFormat.format(date) );
        int test = sortSolution.sortAndSaveSol(bestSolutionComputed, d, "../Generated_best_solutions/reportExamples/"+(name+dateFormat.format(date)));
        if (test==0)
        {
            System.out.println("Fichier solution créé \n");
        }
        else{
            System.out.println("[ERROR] Fichier solution fail \n");
        }
    }
}