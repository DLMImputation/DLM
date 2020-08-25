

import java.util.Comparator;


public class ComparatorLikelihoodPair implements Comparator<LikelihoodPair> {
	@Override
	public int compare(LikelihoodPair lp1, LikelihoodPair lp2) {
		// TODO Auto-generated method stub
		double likelihood1 = lp1.getLikelihood();
		double likelihood2 = lp2.getLikelihood();
		if (likelihood1 < likelihood2) {
			return 1;
		} else if (likelihood1 > likelihood2) {
			return -1;
		}

		return 0;
	}

}
