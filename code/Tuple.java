

public class Tuple {
	private int rowIndex;
	private int attrNum;
	private String[] args;
	private int[] status;

	public Tuple(int attrNum) {
		setAttrNum(attrNum);
		args = new String[attrNum];
		status = new int[attrNum];
	}

	public void buildTuple(int rowIndex, String[] vals) {
		setRowIndex(rowIndex);

		if (vals.length != attrNum) {
			System.out.println("Inconsistent attrNum !");
		}

		for (int i = 0; i < attrNum; ++i) {
			args[i] = vals[i];
		}
	}

	public void setAttrNum(int attrNum) {
		this.attrNum = attrNum;
	}


	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public void setStatusbyIndex(int attrIndex, int state) {
		this.status[attrIndex] = state;
	}

	public int[] getStatus() {
		return status;
	}

	public String[] getAllData() {
		return args;
	}

	public String getDataByIndex(int attrIndex) {
		return args[attrIndex];
	}

	public void clear() {
		for (int i = 0; i < attrNum; ++i) {
			status[i] = 1;
		}
	}
}
