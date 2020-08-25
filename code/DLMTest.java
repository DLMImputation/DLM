import java.util.HashMap;

public class DLMTest {

	public static void main(String[] agrs) {
		String dataset = "res";
		String fileName = dataset + "-dirty.data";

		FileHandler fh = new FileHandler();
		Database db = fh.readData(fileName);

		final int K = 10;
		final int L = 10;
		final int Can = 10;

		DLM dlm = new DLM(db);
		dlm.setParams(K, L, Can);
		HashMap<Position, Cell> cellMap = dlm.mainDLM();
		for (Position pos : cellMap.keySet()) {
			Cell cell = cellMap.get(pos);
			System.out.println(cell.getPosition().getRowIndex() + "," + cell.getPosition().getAttrIndex() + ","
					+ cell.getModify());
		}

	}

}
