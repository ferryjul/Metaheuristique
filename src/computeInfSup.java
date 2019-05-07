import java.util.ArrayList;

public class computeInfSup {

    private static int computeSumTimes(ArrayList<Integer> list) {
        int totalTime = 0;
        for(int time : list) {
            totalTime += time;
        }
        return totalTime;
    }

    private static int findMaxTime(ArrayList<Integer> list) {
        int max_time = Integer.MIN_VALUE;
        for(int time : list) {
            if(time > max_time) {
                max_time = time;
            }
        }
        return max_time;
    }
    private static ArrayList<Integer> computeEvacTimes(data inData){
        ArrayList<Integer> sol = new ArrayList<Integer>();
        for(Path_data aPath : inData.evac_paths) { // iterates over all evac paths
            // we first find min capacity value on path
            int minTime = aPath.population / aPath.max_rate;
            if((aPath.population % aPath.max_rate) != 0) {
                minTime++; // Pour "faire passer" le dernier paquet
            }
            int travel_time = 0;
            // we compute the sum of the travel times of each edge on the path
            int currNode = aPath.origin;
            for(int nextNode : aPath.following) {
                travel_time+=inData.getEdgeLength(currNode, nextNode);
                currNode = nextNode;
            }
            minTime+=travel_time; // durée de trajet des paquets jusqu'au point sûr
            sol.add(minTime);
        }
        return sol;
    }

    public static int computeInf(data inData) {
        ArrayList<Integer> evacTimes = computeEvacTimes(inData);
        int max_time = findMaxTime(evacTimes);
        return max_time;
    }

    public static int computeSup(data inData) {
        ArrayList<Integer> evacTimes = computeEvacTimes(inData);
        int sum_times = computeSumTimes(evacTimes);
        return sum_times;
    }

    /**
    @return a list whose first element is the inf and second element is the sup
    */
    public static ArrayList<Integer> computeInfAndSup(data inData) { // Because computeInf and computeMax perform the same calculations
        ArrayList<Integer> sol = new ArrayList<Integer>();
        ArrayList<Integer> evacTimes = computeEvacTimes(inData);
        int max_time = findMaxTime(evacTimes);
        int sum_times = computeSumTimes(evacTimes);
        sol.add(max_time);
        sol.add(sum_times);
        return sol;
    }

}
