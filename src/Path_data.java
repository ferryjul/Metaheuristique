import java.util.ArrayList;

class Path_data {
	int origin;
	ArrayList<Integer> following;
	int nb_following;
	int max_rate;

	public Path_data(int o, int nb_f, int rate, ArrayList<Integer> fol) {
		origin = o;
		nb_following = nb_f;
		max_rate = rate;
		following = fol;
	}
	
}