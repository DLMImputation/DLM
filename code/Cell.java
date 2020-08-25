

public class Cell {
	private Position position;
	private String truth;
	private String modify;

	public Cell(Position position) {
		setPosition(position);
		setModify("0");
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public String getTruth() {
		return truth;
	}

	public void setTruth(String truth) {
		this.truth = truth;
	}

	public String getModify() {
		return modify;
	}

	public void setModify(String modify) {
		this.modify = modify;
	}
}
