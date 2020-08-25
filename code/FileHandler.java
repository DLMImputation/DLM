import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
public class FileHandler {

	private String PATH = "data/";

	public Database readData(String input) {
		Database db = new Database();
		ArrayList<Cell> cells = new ArrayList<>();

		try {
			FileReader fr = new FileReader(PATH + input);
			BufferedReader br = new BufferedReader(fr);

			String line = null;
			String[] vals = null;

			String title = br.readLine();
			int attrNum = title.split(",").length;
			db.setAttrNum(attrNum);
			boolean[] isNumerical = new boolean[attrNum];
			double[] minVals = new double[attrNum];
			double[] maxVals = new double[attrNum];
			for (int attri = 0; attri < attrNum; attri++) {
				isNumerical[attri] = true;
				minVals[attri] = Double.MAX_VALUE;
				maxVals[attri] = -Double.MAX_VALUE;
			}

			int rowIndex = 0;
			while ((line = br.readLine()) != null) {
				vals = line.split(",");
				String[] data = new String[attrNum];
				for (int i = 0; i < attrNum; ++i) {
					if (i >= vals.length || vals[i].equals("")) {
						Position pos = new Position(rowIndex, i);
						Cell cell = new Cell(pos);
						cells.add(cell);
					} else {
						data[i] = vals[i].trim();
						if (!Assist.isNumber(data[i])) {
							isNumerical[i] = false;
						} else {
							double tmpVal = Double.parseDouble(data[i]);
							if (tmpVal < minVals[i]) {
								minVals[i] = tmpVal;
							}
							if (tmpVal > maxVals[i]) {
								maxVals[i] = tmpVal;
							}
						}
					}
				}
				Tuple tp = new Tuple(attrNum);
				tp.buildTuple(rowIndex, data);
				db.addTuple(tp);
				rowIndex++;
			}
			db.setIsNumerical(isNumerical);
			db.setMaxVals(maxVals);
			db.setMinVals(minVals);
			int size = db.getLength();
			db.setFlags(new int[size][attrNum]);
			db.setCells(cells);
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		db.clear();
		return db;
	}

}
