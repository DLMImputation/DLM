
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import Jama.Matrix;

public class DLM {

	private Database db;
	private int K;
	private int Can;
	private int L;
	private double EPSILON = 0.0000000001;
	private HashMap<RegCompModel, RegModelParams> regCompModelParamsMap;
	private HashMap<Position, ArrayList<String>> positionCanMap;
	private ArrayList<Tuple> tpList;
	private ArrayList<Cell> cells;
	private HashMap<Position, Cell> cellMap;
	private HashMap<TuplePair, double[]> deltasMaxMap;
	private HashMap<TuplePair, double[]> deltasMinMap;

	private String[][] dbVals;
	private int[][] flags;

	private ArrayList<Integer> rowIndexList;
	private ArrayList<Integer> misRowIndexList;
	private ArrayList<Integer> compRowIndexList;
	private ArrayList<ArrayList<Integer>> misRowAttrIndexList;
	private ArrayList<ArrayList<Integer>> misAttrRowIndexList;

	public DLM(Database db) {
		setDatabase(db);
		setCells(db.getCells());
		tpList = db.getTpList();

		setCellMap();
		regCompModelParamsMap = new HashMap<>();
		positionCanMap = new HashMap<>();
		deltasMaxMap = new HashMap<TuplePair, double[]>();
		deltasMinMap = new HashMap<TuplePair, double[]>();
	}

	public void setParams(int K, int L, int Can) {
		setK(K);
		setL(L);
		setCan(Can);
	}

	public HashMap<Position, Cell> mainDLM() {
		initVals();

		genCellCans();

		learnModels();

		calcDeltas();

		impute();
		return cellMap;
	}

	private void initVals() {
		int size = db.getLength();
		int attrNum = db.getAttrNum();

		dbVals = new String[size][attrNum];
		flags = new int[size][attrNum];

		rowIndexList = new ArrayList<>();
		misRowIndexList = new ArrayList<>();
		compRowIndexList = new ArrayList<>();
		misRowAttrIndexList = new ArrayList<ArrayList<Integer>>();
		misAttrRowIndexList = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < attrNum; ++i) {
			misAttrRowIndexList.add(new ArrayList<Integer>());
		}

		ArrayList<Integer> tmpList = null;
		Tuple tp = null;
		int misRowIndex = -1;

		boolean hasMis = false;
		for (int i = 0; i < size; ++i) {
			tp = tpList.get(i);
			String[] datas = tp.getAllData();
			rowIndexList.add(i);
			hasMis = false;
			for (int j = 0; j < attrNum; ++j) {
				dbVals[i][j] = datas[j];
				if (datas[j] == null) {
					flags[i][j] = 0;
					hasMis = true;
					if ((misRowIndex = misRowIndexList.indexOf(i)) == -1) {
						misRowIndexList.add(i);
						tmpList = new ArrayList<>();
						tmpList.add(j);
						misRowAttrIndexList.add(tmpList);
					} else {
						misRowAttrIndexList.get(misRowIndex).add(j);
					}
					misAttrRowIndexList.get(j).add(i);
				} else {
					flags[i][j] = 1;
				}
			}
			if (!hasMis) {
				compRowIndexList.add(i);
			}
		}
	}

	public void calcDeltas() {
		int compRowNum = compRowIndexList.size();
		int misRowNum = misRowIndexList.size();
		int attrNum = db.getAttrNum();
		String[] vals1, vals2;
		String val1, val2;
		double dis;
		for (int ci = 0; ci < compRowNum; ci++) {
			int compRowIndex = compRowIndexList.get(ci);
			vals1 = dbVals[compRowIndex];
			for (int mi = 0; mi < misRowNum; mi++) {
				int misRowIndex = misRowIndexList.get(mi);
				vals2 = dbVals[misRowIndex];
				ArrayList<Integer> misAttrIndexList = misRowAttrIndexList.get(mi);
				TuplePair tp = new TuplePair(compRowIndex, misRowIndex);
				double[] deltasMax = new double[attrNum];
				double[] deltasMin = new double[attrNum];
				for (int attrIndex = 0; attrIndex < attrNum; attrIndex++) {
					val1 = vals1[attrIndex];
					if (misAttrIndexList.contains(attrIndex)) {
						double deltaMax = -Double.MAX_VALUE;
						double deltaMin = Double.MAX_VALUE;
						Position position = new Position(misRowIndex, attrIndex);
						ArrayList<String> canList = positionCanMap.get(position);
						for (int cani = 0; cani < canList.size(); cani++) {
							val2 = canList.get(cani);
							if (Assist.isNumber(val1) && Assist.isNumber(val2)) {
								dis = Assist.normNumDis(Double.parseDouble(val1), Double.parseDouble(val2),
										db.getMaxVals()[attrIndex], db.getMinVals()[attrIndex]);
							} else {
								dis = Assist.normStrDis(val1, val2);
							}
							if (dis < deltaMin) {
								deltaMin = dis;
							}
							if (dis > deltaMax) {
								deltaMax = dis;
							}
						}
						deltasMax[attrIndex] = deltaMax;
						deltasMin[attrIndex] = deltaMin;
					} else {
						val2 = vals2[attrIndex];
						if (Assist.isNumber(val1) && Assist.isNumber(val2)) {
							dis = Assist.normNumDis(Double.parseDouble(val1), Double.parseDouble(val2),
									db.getMaxVals()[attrIndex], db.getMinVals()[attrIndex]);
						} else {
							dis = Assist.normStrDis(val1, val2);
						}
						deltasMax[attrIndex] = dis;
						deltasMin[attrIndex] = dis;
					}
				}
				deltasMaxMap.put(tp, deltasMax);
				deltasMinMap.put(tp, deltasMin);
			}
		}
	}

	public void impute() {
		int misRowNum = misRowIndexList.size();
		Position position;
		Cell cell;
		int misRowIndex;
		for (int ri = 0; ri < misRowNum; ri++) {
			misRowIndex = misRowIndexList.get(ri);
			ArrayList<Integer> misAttrIndexList = misRowAttrIndexList.get(ri);
			for (int mi = 0; mi < misAttrIndexList.size(); mi++) {
				int misAttrIndex = misAttrIndexList.get(mi);
				position = new Position(misRowIndex, misAttrIndex);
				cell = cellMap.get(position);
				ArrayList<String> canList = positionCanMap.get(position);
				double maxLikelihood = -Double.MAX_VALUE;
				int maxCanIndex = -1;
				double tmpLikelihood;
				for (int cani = 0; cani < canList.size(); cani++) {
					String canVal = canList.get(cani);
					tmpLikelihood = localProfiling(misRowIndex, misAttrIndex, canVal);
					if (tmpLikelihood > maxLikelihood) {
						maxLikelihood = tmpLikelihood;
						maxCanIndex = cani;
					}
				}
				String modify = canList.get(maxCanIndex);
				cell.setModify(modify);
			}
		}
	}

	public double localProfiling(int misRowIndex, int misAttrIndex, String canVal) {
		int compRowNum = compRowIndexList.size();

		ArrayList<RegModel> models = null;
		models = findAllRegModels();

		double[] tupleLikelihoods = new double[compRowNum];

		int[] topKIndexes = new int[K];
		double[] topKLikelihoods = new double[K];

		for (int ci = 0; ci < compRowNum; ci++) {
			int compRowIndex = compRowIndexList.get(ci);
			TuplePair tp = new TuplePair(compRowIndex, misRowIndex);
			double[] deltasMax = deltasMaxMap.get(tp);
			double[] deltasMin = deltasMinMap.get(tp);
			tupleLikelihoods[ci] = calcLikelihood(misRowIndex, misAttrIndex, canVal, compRowIndex, deltasMax, deltasMin,
					models);
		}
		findTopK(tupleLikelihoods, topKIndexes, topKLikelihoods);
		double sumLikelihood = 0;
		for (int ki = 0; ki < K; ki++) {
			sumLikelihood += topKLikelihoods[ki];
		}
		return sumLikelihood;
	}

	private void findTopK(double[] totalLikelihoods, int[] topKIndexes, double[] topKLikelihoods) {
		if (topKLikelihoods.length == 0) {
			return;
		}
		int length = topKIndexes.length;
		if (length > compRowIndexList.size()) {
			for (int i = 0; i < compRowIndexList.size(); i++) {
				int rowIndex = compRowIndexList.get(i);
				topKIndexes[i] = rowIndex;
				topKLikelihoods[i] = totalLikelihoods[i];
			}
		} else {
			for (int i = 0; i < length; ++i) {
				int rowIndex = compRowIndexList.get(i);
				topKIndexes[i] = rowIndex;
				topKLikelihoods[i] = totalLikelihoods[i];
			}
			int minIndex = getMinIndexfromK(topKLikelihoods);
			double minVal = topKLikelihoods[minIndex];

			double dis;
			for (int i = length; i < compRowIndexList.size(); ++i) {
				int rowIndex = compRowIndexList.get(i);
				dis = totalLikelihoods[i];
				if (dis > minVal) {
					topKIndexes[minIndex] = rowIndex;
					topKLikelihoods[minIndex] = dis;

					minIndex = getMinIndexfromK(topKLikelihoods);
					minVal = topKLikelihoods[minIndex];
				}
			}
		}

		ArrayList<LikelihoodPair> lpList = new ArrayList<>();
		LikelihoodPair lp = null;
		for (int i = 0; i < length; ++i) {
			lp = new LikelihoodPair(topKLikelihoods[i], topKIndexes[i]);
			lpList.add(lp);
		}
		Collections.sort(lpList, new ComparatorLikelihoodPair());

		for (int i = 0; i < length; ++i) {
			lp = lpList.get(i);
			topKIndexes[i] = lp.getIndex();
			topKLikelihoods[i] = lp.getLikelihood();
		}
	}

	private int getMinIndexfromK(double[] vals) {
		int index = -1;

		double min = Double.MAX_VALUE;
		for (int i = 0; i < vals.length; ++i) {
			if (vals[i] < min) {
				min = vals[i];
				index = i;
			}
		}

		return index;
	}

	private double calcLikelihood(int misRowIndex, int misAttrIndex, String canVal, int compRowIndex,
			double[] deltasMax, double[] deltasMin, ArrayList<RegModel> models) {

		int attrX;
		String xVal1, xVal2;

		double[] betas;
		double sigma2;
		double dis;

		double totalLikelihood = 0;
		for (int mi = 0; mi < models.size(); mi++) {
			RegModel regModel = models.get(mi);
			int[] attrXs = regModel.getAttrXs();
			int attrY = regModel.getAttrY();
			int attrXNum = attrXs.length;

			RegCompModel regCompModel = new RegCompModel(compRowIndex, attrXs, attrY);
			RegModelParams regModelParams = regCompModelParamsMap.get(regCompModel);
			betas = regModelParams.getBetas();
			sigma2 = regModelParams.getSigma2();
			sigma2 += EPSILON;

			double intercept = betas[0];
			double epsilonMax = -intercept;
			double epsilonMin = -intercept;

			if (attrY == misAttrIndex) {
				String yVal1, yVal2;
				yVal1 = dbVals[compRowIndex][attrY];
				yVal2 = canVal;
				if (Assist.isNumber(yVal1) && Assist.isNumber(yVal2)) {
					dis = Assist.normNumDis(Double.parseDouble(yVal1), Double.parseDouble(yVal2),
							db.getMaxVals()[attrY], db.getMinVals()[attrY]);
				} else {
					dis = Assist.normStrDis(yVal1, yVal2);
				}
				epsilonMax += dis;
				epsilonMin += dis;
			} else {
				epsilonMax += deltasMax[attrY];
				epsilonMin += deltasMin[attrY];
			}

			for (int j = 0; j < attrXNum; j++) {
				attrX = attrXs[j];
				if (attrX == misAttrIndex) {
					xVal1 = dbVals[compRowIndex][attrX];
					xVal2 = canVal;
					if (Assist.isNumber(xVal1) && Assist.isNumber(xVal2)) {
						dis = Assist.normNumDis(Double.parseDouble(xVal1), Double.parseDouble(xVal2),
								db.getMaxVals()[attrX], db.getMinVals()[attrX]);
					} else {
						dis = Assist.normStrDis(xVal1, xVal2);
					}
					epsilonMax = epsilonMax - betas[j + 1] * dis;
					epsilonMin = epsilonMin - betas[j + 1] * dis;
				} else {
					if (betas[j + 1] > 0) {
						epsilonMax = epsilonMax - betas[j + 1] * deltasMin[attrX];
						epsilonMin = epsilonMin - betas[j + 1] * deltasMax[attrX];
					} else {
						epsilonMax = epsilonMax - betas[j + 1] * deltasMax[attrX];
						epsilonMin = epsilonMin - betas[j + 1] * deltasMin[attrX];
					}
				}
			}

			double likelihood = 0;
			if (epsilonMin > 0) {
				likelihood = -0.5 * Math.log(2 * Math.PI * sigma2) - (0.5 / sigma2) * epsilonMin * epsilonMin;
			} else if (epsilonMax < 0) {
				likelihood = -0.5 * Math.log(2 * Math.PI * sigma2) - (0.5 / sigma2) * epsilonMax * epsilonMax;
			} else {
				likelihood = -0.5 * Math.log(2 * Math.PI * sigma2);
			}
			totalLikelihood += likelihood;
		}
		return totalLikelihood;
	}

	public void learnModels() {
		int comRowNum = compRowIndexList.size();
		ArrayList<RegModel> models = null;
		models = findAllRegModels();
		for (int mi = 0; mi < models.size(); mi++) {
			RegModel regModel = models.get(mi);
			int[] attrXs = regModel.getAttrXs();
			int attrY = regModel.getAttrY();
			int attrXNum = attrXs.length;
			int columnSize = attrXNum + 1;
			boolean isSingular = false;
			double[][] subDistances = new double[comRowNum][db.getLength()];
			int[][] sIndexes = new int[comRowNum][L];
			double[][] sDistances = new double[comRowNum][L];
			int[] lnnIndexes;
			int comRowIndex;
			for (int ci = 0; ci < comRowNum; ci++) {
				comRowIndex = compRowIndexList.get(ci);
				RegCompModel regCompModel = new RegCompModel(comRowIndex, attrXs, attrY);
				calcDisNorm(comRowIndex, subDistances[ci]);
				findCompKnn(subDistances[ci], sIndexes[ci], sDistances[ci]);
				lnnIndexes = sIndexes[ci];
				double[] beta = new double[columnSize];
				double[][] x = new double[L][columnSize];
				double[][] y = new double[L][1];
				int attrX;
				String xVal1, xVal2;
				String yVal1, yVal2;
				for (int li = 0; li < L; li++) {
					int lRowIndex = lnnIndexes[li];
					for (int xi = 0; xi < attrXNum; xi++) {
						attrX = attrXs[xi];
						xVal1 = dbVals[comRowIndex][attrX];
						xVal2 = dbVals[lRowIndex][attrX];
						if (Assist.isNumber(xVal1) && Assist.isNumber(xVal2)) {
							x[li][xi + 1] = Assist.normNumDis(Double.parseDouble(xVal1), Double.parseDouble(xVal2),
									db.getMaxVals()[attrX], db.getMinVals()[attrX]);
						} else {
							x[li][xi + 1] = Assist.normStrDis(xVal1, xVal2);
						}
					}
					x[li][0] = 1;
					yVal1 = dbVals[comRowIndex][attrY];
					yVal2 = dbVals[lRowIndex][attrY];
					if (Assist.isNumber(yVal1) && Assist.isNumber(yVal2)) {
						y[li][0] = Assist.normNumDis(Double.parseDouble(yVal1), Double.parseDouble(yVal2),
								db.getMaxVals()[attrY], db.getMinVals()[attrY]);
					} else {
						y[li][0] = Assist.normStrDis(yVal1, yVal2);
					}
				}
				isSingular = false;
				Matrix lxMatrix = new Matrix(x);
				Matrix lyMatrix = new Matrix(y);
				Matrix betaMatrix = null;
				try {
					betaMatrix = learnParamsOLS(lxMatrix, lyMatrix);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					isSingular = true;
				}
				if (!isSingular) {
					for (int i = 0; i < columnSize; ++i) {
						beta[i] = betaMatrix.get(i, 0);
					}
				} else {
					System.out.println("Singular Matrix!!!");
				}

				double sigma = Math.sqrt(calcModelResidual(beta, attrY, lxMatrix, lyMatrix, L));

				RegModelParams regModelParams = new RegModelParams();
				regModelParams.setBetas(beta);
				regModelParams.setSigma2(sigma * sigma);
				regCompModelParamsMap.put(regCompModel, regModelParams);
			}
		}
	}

	private Matrix learnParamsOLS(Matrix xMatrix, Matrix yMatrix) {
		int attrXNum = xMatrix.getColumnDimension();
		Matrix beta = new Matrix(attrXNum, 1);

		Matrix xMatrixT = xMatrix.transpose();

		Matrix aMatrix = xMatrixT.times(xMatrix);
		Matrix bMatrix = xMatrixT.times(yMatrix);

		double alpha[][] = new double[attrXNum][attrXNum];
		for (int i = 0; i < attrXNum; i++) {
			alpha[i][i] = EPSILON;
		}
		Matrix alphaMatrix = new Matrix(alpha);
		aMatrix = aMatrix.plus(alphaMatrix);
		Matrix middleMatrix = aMatrix.inverse();
		beta = middleMatrix.times(bMatrix);

		return beta;
	}

	private void findCompKnn(double[] distances, int[] knnIndexes, double[] knnDistances) {
		if (knnDistances.length == 0) {
			return;
		}

		int length = knnIndexes.length;
		if (length > compRowIndexList.size()) {
			for (int i = 0; i < compRowIndexList.size(); i++) {
				int rowIndex = compRowIndexList.get(i);
				knnIndexes[i] = rowIndex;
				knnDistances[i] = distances[rowIndex];
			}
		} else {
			for (int i = 0; i < length; ++i) {
				int rowIndex = compRowIndexList.get(i);
				knnIndexes[i] = rowIndex;
				knnDistances[i] = distances[rowIndex];
			}
			int maxIndex = getMaxIndexfromK(knnDistances);
			double maxVal = knnDistances[maxIndex];

			double dis;
			for (int i = length; i < compRowIndexList.size(); ++i) {
				int rowIndex = compRowIndexList.get(i);
				dis = distances[rowIndex];
				if (dis < maxVal) {
					knnIndexes[maxIndex] = rowIndex;
					knnDistances[maxIndex] = dis;

					maxIndex = getMaxIndexfromK(knnDistances);
					maxVal = knnDistances[maxIndex];
				}
			}
		}

		ArrayList<KnnPair> kpList = new ArrayList<>();
		KnnPair kp = null;
		for (int i = 0; i < length; ++i) {
			kp = new KnnPair(knnDistances[i], knnIndexes[i]);
			kpList.add(kp);
		}
		Collections.sort(kpList, new ComparatorKnnPair());

		for (int i = 0; i < length; ++i) {
			kp = kpList.get(i);
			knnIndexes[i] = kp.getIndex();
			knnDistances[i] = kp.getDistance();
		}
	}

	private int getMaxIndexfromK(double[] vals) {
		int index = -1;

		double max = -1;
		for (int i = 0; i < vals.length; ++i) {
			if (vals[i] > max) {
				max = vals[i];
				index = i;
			}
		}

		return index;
	}

	private double calcModelResidual(double[] phis, int attrY, Matrix xMatrix, Matrix yMatrix, int size) {
		double sigma = 0;

		double[][] x = xMatrix.getArray();
		double[][] y = yMatrix.getArray();

		double estimate, residual;
		for (int i = 0; i < size; ++i) {
			estimate = 0;
			for (int j = 0; j < phis.length; ++j) {
				estimate += phis[j] * x[i][j];
			}
			residual = estimate - y[i][0];
			sigma += residual * residual;
		}

		sigma = sigma / size;
		return sigma;
	}

	private double calcNormDisBtwTwoTp(int rowIndex1, int[] status1, int rowIndex2) {
		int attrNum = db.getAttrNum();
		String[] vals1 = dbVals[rowIndex1];
		String[] vals2 = dbVals[rowIndex2];

		double dis;
		int weight;
		double sumUp = 0, sumDown = 0;
		double numVal1, numVal2;
		String val1, val2;

		for (int attri = 0; attri < attrNum; attri++) {
			val1 = vals1[attri];
			val2 = vals2[attri];
			weight = status1[attri];
			sumDown += weight;
			if (weight == 0) {
				continue;
			}
			if (Assist.isNumber(val1) && Assist.isNumber(val2)) {
				numVal1 = Double.parseDouble(val1);
				numVal2 = Double.parseDouble(val2);
				dis = Assist.normNumDis(numVal1, numVal2, db.getMaxVals()[attri], db.getMinVals()[attri]);
			} else {
				dis = Assist.normStrDis(val1, val2);
			}
			sumUp += dis * dis * weight;
		}
		if (sumDown == 0) {
			dis = Double.MAX_VALUE;
		} else if (sumUp == 0) {
			dis = EPSILON;
		} else {
			dis = Math.sqrt(sumUp / sumDown);
		}
		return dis;
	}

	private void calcDisNorm(int rowIndex, double[] distances) {
		int[] status = flags[rowIndex];
		int compSize = compRowIndexList.size();
		for (int ci = 0; ci < compSize; ++ci) {
			int compRowIndex = compRowIndexList.get(ci);
			double dis = calcNormDisBtwTwoTp(rowIndex, status, compRowIndex);
			distances[compRowIndex] = dis;
		}
		distances[rowIndex] = Double.MAX_VALUE;
	}

	public void genCellCans() {
		int misRowNum = misRowIndexList.size();
		Position position;
		int rowIndex;
		double[][] subDistances = new double[misRowNum][db.getLength()];
		int[][] sIndexes = new int[misRowNum][Can];
		double[][] sDistances = new double[misRowNum][Can];
		int[] knnIndexes;
		for (int ri = 0; ri < misRowNum; ri++) {
			rowIndex = misRowIndexList.get(ri);
			ArrayList<Integer> misAttrIndexList = misRowAttrIndexList.get(ri);
			for (int mi = 0; mi < misAttrIndexList.size(); mi++) {
				int misAttrIndex = misAttrIndexList.get(mi);
				position = new Position(rowIndex, misAttrIndex);
				calcDisNorm(rowIndex, subDistances[ri]);
				findCompKnn(subDistances[ri], sIndexes[ri], sDistances[ri]);
				ArrayList<String> canList = new ArrayList<>();
				knnIndexes = sIndexes[ri];
				String canVal;
				for (int ki = 0; ki < Can; ki++) {
					int kIndex = knnIndexes[ki];
					canVal = dbVals[kIndex][misAttrIndex];
					if (!canList.contains(canVal)) {
						canList.add(canVal);
					}
				}
				positionCanMap.put(position, canList);
			}
		}
	}

	private ArrayList<RegModel> findAllRegModels() {
		ArrayList<RegModel> models = new ArrayList<>();
		int attrNum = db.getAttrNum();
		for (int attrY = 0; attrY < attrNum; attrY++) {
			int[] attrXs = getAttrXsFromAttrY(attrY);
			RegModel regModel = new RegModel(attrXs, attrY);
			if (!models.contains(regModel)) {
				models.add(regModel);
			}
		}
		return models;
	}

	private int[] getAttrXsFromAttrY(int attrY) {
		int attrNum = db.getAttrNum();
		int attrXNum = attrNum - 1;
		int[] attrXs = new int[attrXNum];
		int curIndex = 0;

		for (int attri = 0; attri < attrNum; ++attri) {
			if (attrY == attri) {
				continue;
			}
			attrXs[curIndex++] = attri;
		}

		return attrXs;
	}

	public void setDatabase(Database db) {
		this.db = db;
	}

	public void setCells(ArrayList<Cell> cells) {
		this.cells = cells;
	}

	private void setCellMap() {
		cellMap = new HashMap<>();
		int missNum = cells.size();

		Cell cell = null;
		for (int i = 0; i < missNum; ++i) {
			cell = cells.get(i);
			cellMap.put(cell.getPosition(), cell);
		}
	}

	public void setK(int k) {
		K = k;
	}

	public void setCan(int can) {
		Can = can;
	}

	public void setL(int l) {
		L = l;
	}

}
