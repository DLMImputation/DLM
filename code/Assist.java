

public class Assist {

	public static boolean isNumber(String str) {
		String reg = "-?[0-9]+(\\.[0-9]+)?";
		return str.matches(reg);
	}

	public static double normNumDis(double a, double b, double maxVal, double minVal) {
		double interval = Math.abs(a - b);
		double norNumDis = interval / (maxVal - minVal);
		return norNumDis;
	}

	public static double normStrDis(String word1, String word2) {
		int len1 = word1.length(), len2 = word2.length();
		int[][] dp = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}
		for (int i = 0; i <= len2; i++) {
			dp[0][i] = i;
		}

		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (word1.charAt(i - 1) == word2.charAt(j - 1))
					dp[i][j] = dp[i - 1][j - 1];
				else
					dp[i][j] = 1 + Math.min(dp[i][j - 1], Math.min(dp[i - 1][j], dp[i - 1][j - 1]));
			}
		}
		double gelStrDis = (double) dp[len1][len2];
		double norStrDis = (2 * gelStrDis) / (len1 + len2 + gelStrDis);
		return norStrDis;
	}

}
