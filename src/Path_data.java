import java.util.ArrayList;

class Path_data {
	int origin;
	int population;
	ArrayList<Integer> following;
	int nb_following;
	int max_rate;
	int uniqueID;

	public Path_data(int o,int pop, int nb_f, int rate, ArrayList<Integer> fol) {
		origin = o;
		population = pop;
		nb_following = nb_f;
		max_rate = rate;
		following = fol;
	}

	public Path_data(int o,int pop, int nb_f, int rate, ArrayList<Integer> fol, int i) {
		origin = o;
		population = pop;
		nb_following = nb_f;
		max_rate = rate;
		following = fol;
		uniqueID = i;
	}
	
	
}