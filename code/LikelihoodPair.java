

public class LikelihoodPair {
	private double likelihood;
	private int index;

	public LikelihoodPair(double likelihood, int index) {
		setLikelihood(likelihood);
		setIndex(index);
	}

	public double getLikelihood() {
		return likelihood;
	}

	public void setLikelihood(double likelihood) {
		this.likelihood = likelihood;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
