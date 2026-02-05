import java.util.*;

public class TwoDHoughSpace {
	static int[][] computeHoughSpace(Image input, Image dogInput, Double[][] orientations, int deltaTheta, double f1) {
		double halfDiag = Math.hypot(input.width /2.0, input.height /2.0);
		int accYSize = (int) (2 * halfDiag) + 1;
		
		int[][] accumulator = new int[180][accYSize];
		int accumulatorMax = 0;
		
		for (int x = 0; x < input.width; x++) {
			for (int y = 0; y < input.height; y++) {
				if (dogInput.pixels[x][y] > 0 && orientations[x][y] != null) {
					double thetaMin = orientations[x][y] - deltaTheta;
					double thetaMax = orientations[x][y] + deltaTheta;
					
					double normalX = x - input.width / 2.0;
					double normalY = y - input.height / 2.0;
					
					for (double n = thetaMin; n <= thetaMax; n++) {
						double nRound = n < 0.01 ? Math.floor(n * 1000.0) / 1000.0 : n;
						
						double thetaDeg = nRound < 0.0 ? 180 + nRound : (nRound >= 180 ? nRound - 180 : nRound);
						
						double theta = Math.toRadians(thetaDeg);
						
						double rho = normalX * Math.cos(theta) + normalY * Math.sin(theta);
						
						int iX = (int) thetaDeg;
						int iY = (int) Math.floor(rho + halfDiag);
						
						accumulator[iX][iY] += 1 * dogInput.pixels[x][y];
						
						accumulatorMax = accumulatorMax < accumulator[iX][iY] ? accumulator[iX][iY] : accumulatorMax;
					}
				}
			}
		}
		
		outputAccumulator(accumulator, accumulatorMax, input.depth, 180, accYSize);
		findPeaks(input, accumulator, accumulatorMax, f1, 180, accYSize);
		
		return accumulator;
	}
	
	static void outputAccumulator(int[][] accumulator, int accumulatorMax, int depth, int width, int height) {
		Image output = new Image(depth, height, width);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				output.pixels[y][x] = (int) (((double) accumulator[x][y] / accumulatorMax) * 255);
			}
		}
		
		output.WritePGM("accumulator.pgm");
	}
	
	static void findPeaks(Image input, int[][] accumulator, int accumulatorMax, double f1, int width, int height) {
		int[][] peaks = new int[width][height];
		ImagePPM output = new ImagePPM(input.depth, input.width, input.height);
		
		for (int n = 0; n < 3; n++) {
			for (int x = 0; x < input.width; x++) {
				for (int y = 0; y < input.height; y++) {
					output.pixels[n][x][y] = input.pixels[x][y];
				}
			}
		}
		
		for (int x = 0; x < width - 18; x++) {
			for (int y = 0; y < height - 18; y++) {
				int currentMax = 0;
				int maxX = 0;
				int maxY = 0;
				
				for (int xx = 0; xx < 19; xx++) {
					for (int yy = 0; yy < 19; yy++) {
						
						if (accumulator[x+xx][y+yy] > currentMax) {
							currentMax = accumulator[x+xx][y+yy];
							maxX = x+xx;
							maxY = y+yy;
						}
						
					}
				}
				
				if (currentMax >= (accumulatorMax * f1) && peaks[maxX][maxY] == 0) {
					peaks[maxX][maxY] = 1;
					double theta = Math.toRadians(maxX);
					double rho = maxY - (height / 2.0);
					
					double cos = Math.cos(theta);
					double sin = Math.sin(theta);
					
					double x0 = ((rho - (- input.width / 2.0) * cos) / sin) + input.height / 2.0;
					double xMax = ((rho - (input.width / 2.0) * cos) / sin) + input.height / 2.0;
					double y0 = ((rho - (- input.height / 2.0) * sin) / cos) + input.width / 2.0;
					double yMax = ((rho - (input.height / 2.0) * sin) / cos) + input.width / 2.0;
					
					int[] p1 = {-1, -1};
					int[] p2 = {-1, -1};
					
					if (x0 >= 0 && x0 <= input.height) {
						p1[0] = 0;
						p1[1] = (int) x0;
					}
					
					if (xMax >= 0 && xMax <= input.height) {
						if (p1[0] == -1) {
							p1[0] = input.width - 1;
							p1[1] = (int) xMax;
						} else {
							p2[0] = input.width - 1;
							p2[1] = (int) xMax;
						}
					}
					
					if (p2[1] == -1 && y0 >= 0 && y0 <= input.width) {
						if (p1[0] == -1) {
							p1[0] = (int) y0;
							p1[1] = 0;
						} else {
							p2[0] = (int) y0;
							p2[1] = 0;
						}
					}
					
					if (p2[1] == -1 && p1[1] != -1 && yMax >= 0 && yMax <= input.width) {
						p2[0] = (int) yMax;
						p2[1] = input.height - 1;
					}
					
					if (p2[1] != -1) {
						plotLine(output, p1, p2);
					}
				}
			}
		}
		
		output.WritePPM("lines.ppm");
		
	}
	
	static void plotLine(ImagePPM output, int[] p1, int[] p2) {
		int dx = Math.abs(p2[0] - p1[0]);
		int dy = -Math.abs(p2[1] - p1[1]);
		int sx = p1[0] < p2[0] ? 1 : -1;
		int sy = p1[1] < p2[1] ? 1 : -1;
		int err = dx + dy;

		while (true) {
			if (0 <= p1[0] && p1[0] < output.width && 0 <= p1[1] && p1[1] < output.height) {
				output.pixels[0][p1[0]][p1[1]] = 0;
				output.pixels[1][p1[0]][p1[1]] = 255;
				output.pixels[2][p1[0]][p1[1]] = 0;
			}

			if (p1[0] == p2[0] && p1[1] == p2[1]) break;

			int e2 = 2 * err;
			if (e2 >= dy) { err += dy; p1[0] += sx; }
			if (e2 <= dx) { err += dx; p1[1] += sy; }
		}
	}
}