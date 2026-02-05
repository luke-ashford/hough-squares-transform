import java.io.*;
import java.util.*;

public class SquareHough {
	public static void main(String[] args) {
		if (args.length != 7) {
			System.out.println("Invalid or improper inputs");
			System.out.println("Input .PGM file, Size length of the square sides, Delta Theta, f1 2D Hough threshold, f2 3D Hough threshold, f3 back projection threshold, DoG (L) or DoG of Sobel (E)");
			System.exit(-1);
		}
		
		String fileNameIn = args[0];
		int edgeLength = Integer.parseInt(args[1]);
		int deltaTheta = Integer.parseInt(args[2]);
		double f1 = Double.parseDouble(args[3]);
		double f2 = Double.parseDouble(args[4]);
		double f3 = Double.parseDouble(args[5]);
		String linesOrEdges = args[6];
		
		if (!linesOrEdges.equals("E") && !linesOrEdges.equals("L")) {
			System.out.println("Invalid input for Lines (L) or Edges (E)");
			System.out.println("Please specify L for DoG, or E for SobelDoG");
			System.exit(-1);
		}
		
		Image input = new Image();
		input.ReadPGM(fileNameIn);
		
		if (input.width > 1000 || input.height > 1000) {
			System.out.println("Invalid file input");
			System.out.println("Input file cannot be greater than 1000 pixels in width or height");
			System.exit(-1);
		}
		
		String fileNameOut = linesOrEdges.equals("E") ? "SobelDoG.pgm" : "DoG.pgm";
		Image dogInput = linesOrEdges.equals("E") ? Sobel.computeSobel(input) : input;
		
		Image dogOutput = DifferenceOfGaussian.computeDoG(dogInput);
		dogOutput.WritePGM(fileNameOut);
		
		Double[][] orientations = OrientationMoments.computeOrientations(dogOutput);
				
		int[][] accumulator2D = TwoDHoughSpace.computeHoughSpace(input, dogOutput, orientations, deltaTheta, f1);
		
		computeSquareHoughSpace(input, dogOutput, accumulator2D, edgeLength, f2, f3);
	}
	
	static void computeSquareHoughSpace(Image input, Image dogOutput, int[][] accumulator2D, int edgeLength, double f2, double f3) {		
		int rhoBins = (int) (2 * Math.hypot(input.width /2.0, input.height /2.0)) + 1;
		int thetaBins = 180;
		double halfEdge = edgeLength / 2.0;
		
		int[][][] accumulator3D = new int[input.width][input.height][thetaBins];
		int accumulatorMax = 0;
				
		for (int x = (int) halfEdge; x < input.width - (int) halfEdge; x++) {
			for (int y = (int) halfEdge; y < input.height - (int) halfEdge; y++) {
				double normalX = x - input.width / 2.0;
				double normalY = y - input.height / 2.0;
				
				for (int thetaDeg = 0; thetaDeg < thetaBins; thetaDeg++) {
					double theta = Math.toRadians(thetaDeg);
					double cos = Math.cos(theta);
					double sin = Math.sin(theta);
					
					int rho1 = (int) (normalX * cos + normalY * sin + halfEdge + rhoBins / 2.0);
					int rho2 = (int) (normalX * (-sin) + normalY * cos + halfEdge + rhoBins / 2.0);
					int rho3 = (int) (normalX * cos + normalY * sin - halfEdge + rhoBins / 2.0);
					int rho4 = (int) (normalX * (-sin) + normalY * cos - halfEdge + rhoBins / 2.0);
					
					if (rho1 >= 0 && rho2 >= 0 && rho3 >= 0 && rho4 >= 0 && rho1 < rhoBins && rho2 < rhoBins && rho3 < rhoBins && rho4 < rhoBins) {
						accumulator3D[x][y][thetaDeg] = Math.min(
							Math.min(accumulator2D[thetaDeg][rho1], accumulator2D[(thetaDeg + 90) % thetaBins][rho2]), 
							Math.min(accumulator2D[thetaDeg][rho3], accumulator2D[(thetaDeg + 90) % thetaBins][rho4])
						);
						accumulatorMax = accumulator3D[x][y][thetaDeg] > accumulatorMax ? accumulator3D[x][y][thetaDeg] : accumulatorMax;
					}
				}
			}
		}
		
		int[][][] peaks = new int[input.width][input.height][thetaBins];
		ImagePPM output = new ImagePPM(input.depth, input.width, input.height);
		
		for (int n = 0; n < 3; n++) {
			for (int x = 0; x < input.width; x++) {
				for (int y = 0; y < input.height; y++) {
					output.pixels[n][x][y] = input.pixels[x][y];
				}
			}
		}
		int backProjectionMax = 0;
		
		for (int x = (int) halfEdge; x < input.width - (int) halfEdge; x++) {
			for (int y = (int) halfEdge; y < input.height - (int) halfEdge; y++) {
				for (int thetaDeg = 0; thetaDeg < thetaBins - 18; thetaDeg++) {
					if (accumulator3D[x][y][thetaDeg] >= (accumulatorMax * f2)) {
					
						int currentMax = 0;
						int maxX = 0;
						int maxY = 0;
						int maxTheta = 0;
						
						for (int xx = -9; xx < 10; xx++) {
							for (int yy = -9; yy < 10; yy++) {
								for (int t = 0; t < 19; t++) {
									if (accumulator3D[x+xx][y+yy][thetaDeg+t] > currentMax) {
										currentMax = accumulator3D[x+xx][y+yy][thetaDeg+t];
										maxX = x+xx;
										maxY = y+yy;
										maxTheta = thetaDeg+t;
									}
									
								}
							}
						}
						
						if (currentMax >= (accumulatorMax * f2) && peaks[maxX][maxY][maxTheta] == 0) {
							double theta = Math.toRadians(maxTheta);
							double cos = Math.cos(theta);
							double sin = Math.sin(theta);
							
							int[] p1 = new int[] {
								(int) (maxX + ((- halfEdge) * cos - (- halfEdge) * sin)), 
								(int) (maxY + ((- halfEdge) * sin + (- halfEdge) * cos))
							};
							int[] p2 = new int[] {
								(int) (maxX + (halfEdge * cos - (- halfEdge) * sin)), 
								(int) (maxY + (halfEdge * sin + (- halfEdge) * cos))
							};
							int[] p3 = new int[] {
								(int) (maxX + (halfEdge * cos - halfEdge * sin)), 
								(int) (maxY + (halfEdge * sin + halfEdge * cos))
							};
							int[] p4 = new int[] {
								(int) (maxX + ((- halfEdge) * cos - halfEdge * sin)),
								(int) (maxY + ((- halfEdge) * sin + halfEdge * cos))
							};
							
							int backProjectSum = 0;
							
							backProjectSum += backProjectAndplotLine(output, dogOutput, p3, p4, new int[] {255, 0, 0});
							backProjectSum += backProjectAndplotLine(output, dogOutput, p3, p2, new int[] {255, 0, 0});
							backProjectSum += backProjectAndplotLine(output, dogOutput, p4, p1, new int[] {255, 0, 0});
							backProjectSum += backProjectAndplotLine(output, dogOutput, p2, p1, new int[] {255, 0, 0});
							
							backProjectionMax = backProjectSum > backProjectionMax ? backProjectSum : backProjectionMax;
							peaks[maxX][maxY][maxTheta] = backProjectSum;
							
						}
						
					}
				}
			}
		}
		
		output.WritePPM("squares.ppm");
		
		for (int x = (int) halfEdge; x < input.width - (int) halfEdge; x++) {
			for (int y = (int) halfEdge; y < input.height - (int) halfEdge; y++) {
				for (int thetaDeg = 0; thetaDeg < thetaBins; thetaDeg++) {
					if (peaks[x][y][thetaDeg] > f3 * backProjectionMax) {
						double theta = Math.toRadians(thetaDeg);
						double cos = Math.cos(theta);
						double sin = Math.sin(theta);
						
						int[] p1 = new int[] {
							(int) (x + ((- halfEdge) * cos - (- halfEdge) * sin)), 
							(int) (y + ((- halfEdge) * sin + (- halfEdge) * cos))
						};
						int[] p2 = new int[] {
							(int) (x + (halfEdge * cos - (- halfEdge) * sin)), 
							(int) (y + (halfEdge * sin + (- halfEdge) * cos))
						};
						int[] p3 = new int[] {
							(int) (x + (halfEdge * cos - halfEdge * sin)), 
							(int) (y + (halfEdge * sin + halfEdge * cos))
						};
						int[] p4 = new int[] {
							(int) (x + ((- halfEdge) * cos - halfEdge * sin)),
							(int) (y + ((- halfEdge) * sin + halfEdge * cos))
						};
												
						backProjectAndplotLine(output, dogOutput, p3, p4, new int[] {0, 0, 255});
						backProjectAndplotLine(output, dogOutput, p3, p2, new int[] {0, 0, 255});
						backProjectAndplotLine(output, dogOutput, p4, p1, new int[] {0, 0, 255});
						backProjectAndplotLine(output, dogOutput, p2, p1, new int[] {0, 0, 255});
					}
				}
			}
		}
		
		output.WritePPM("backproject.ppm");
	}
	
	static int backProjectAndplotLine(ImagePPM output, Image dog, int[] p1, int[] p2, int[] rgb) {
		int backProjectSum = 0;
		
		int x1 = p1[0];
		int y1 = p1[1];
		int x2 = p2[0];
		int y2 = p2[1];
		
		int dx = Math.abs(x2 - x1);
		int dy = -Math.abs(y2 - y1);
		int sx = x1 < x2 ? 1 : -1;
		int sy = y1 < y2 ? 1 : -1;
		int err = dx + dy;

		while (true) {
			if (0 <= x1 && x1 < output.width && 0 <= y1 && y1 < output.height) {
				output.pixels[0][x1][y1] = rgb[0];
				output.pixels[1][x1][y1] = rgb[1];
				output.pixels[2][x1][y1] = rgb[2];
				backProjectSum += dog.pixels[x1][y1];
			}

			if (x1 == x2 && y1 == y2) break;

			int e2 = 2 * err;
			if (e2 >= dy) { err += dy; x1 += sx; }
			if (e2 <= dx) { err += dx; y1 += sy; }
		}
		
		return backProjectSum;
	}
}