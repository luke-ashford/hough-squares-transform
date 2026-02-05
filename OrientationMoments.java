public class OrientationMoments {
	
	static Double[][] computeOrientations(Image input) {
		Double[][] orientations = new Double[input.width][input.height];
		
		for (int x = 0; x < input.width - 6; x++) {
			for (int y = 0; y < input.height - 6; y++) {
				double[][] sevenBySevenWindow = new double[7][7];
				
				for (int xx = 0; xx < 7; xx++) {
					for (int yy = 0; yy < 7; yy++) {
						sevenBySevenWindow[xx][yy] = input.pixels[x+xx][y+yy];
					}
				}
				double[] centroid = calculateCentroid(sevenBySevenWindow);
				
				if (centroid == null) {
					orientations[x+3][y+3] = null;
					
				} else {
					double mu11 = calculateCentralMoment(sevenBySevenWindow, centroid, 1, 1);
					double mu20 = calculateCentralMoment(sevenBySevenWindow, centroid, 2, 0);
					double mu02 = calculateCentralMoment(sevenBySevenWindow, centroid, 0, 2);
					
					if (mu11 == 0.0 || mu20 - mu02 == 0.0) {
						orientations[x+3][y+3] = null;
						
					} else {
						orientations[x+3][y+3] = Math.toDegrees(0.5 * Math.atan2(2 * mu11, (mu20 - mu02))) + 90.0;
					}
				}
			}
		}
		
		return orientations;
	}
	
	static double calculateMoment(double[][] input, int p, int q) {
		double moment = 0;
		
		for (int x = 0; x < 7; x++) {
			for (int y = 0; y < 7; y++) {
				moment += Math.pow(x, p) * Math.pow(y, q) * input[x][y];
			}
		}
		
		return moment;
	}
	
	static double[] calculateCentroid(double[][] input) {
		double[] centroid = new double[] {0, 0};
		
		double m00 = calculateMoment(input, 0, 0);
		double m10 = calculateMoment(input, 1, 0);
		double m01 = calculateMoment(input, 0, 1);
		
		if (m00 == 0) {
			return null;
		}
				
		centroid[0] = m10 / m00;
		centroid[1] = m01 / m00;
		
		return centroid;
	}
	
	static double calculateCentralMoment(double[][] input, double[] centroid, int p, int q) {
		double xBar = centroid[0];
		double yBar = centroid[1];
		
		double moment = 0;
		
		for (int x = 0; x < 7; x++) {
			for (int y = 0; y < 7; y++) {
				moment += Math.pow((x - xBar), p) * Math.pow((y - yBar), q) * input[x][y];
			}
		}
		
		return moment;
	}
}