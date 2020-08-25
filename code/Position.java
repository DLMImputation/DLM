

public class Position {
	private int rowIndex;
	private int attrIndex;

	public Position(int rowIndex, int attrIndex) {
		setRowIndex(rowIndex);
		setAttrIndex(attrIndex);
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public int getAttrIndex() {
		return attrIndex;
	}

	public void setAttrIndex(int attrIndex) {
		this.attrIndex = attrIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 101;
		int result = 1;
		result = prime * result + attrIndex;
		result = prime * result + rowIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (attrIndex != other.attrIndex)
			return false;
		if (rowIndex != other.rowIndex)
			return false;
		return true;
	}
}
