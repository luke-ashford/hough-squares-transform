public class DifferenceOfGaussian {
	static int kernelSize = 9;
	
	static Image computeDoG(Image input) {
		double[][] sigma1Kernel = generateKernel(kernelSize, 1);
		double[][] sigma2Kernel = generateKernel(kernelSize, 2);
		int imgCutoff = kernelSize - 1;
		int indexCutoff = imgCutoff / 2;
		
		Image output = new Image(input.depth, input.width, input.height);
		int min = 2147483647;
		int max = 0;
		
		// Difference Of Gaussian
		// Loop Through Input To Calculate Both Gaussians
		for (int x = 0; x < input.width - imgCutoff; x++) {
			for (int y = 0; y < input.height - imgCutoff; y++) {
				double sigma1Sum = 0;
				double sigma2Sum = 0;
				
				// Loop Through Gaussian Kernels
				for (int yy = 0; yy < kernelSize; yy++) {
					for (int xx = 0; xx < kernelSize; xx++) {
						// Sum Each Gaussian Value For Each Pixel
						sigma1Sum += sigma1Kernel[xx][yy] * input.pixels[x+xx][y+yy];
						sigma2Sum += sigma2Kernel[xx][yy] * input.pixels[x+xx][y+yy];
					}
				}
				
				// Subtract To Find The Difference
				int sum = (int) - (sigma1Sum - sigma2Sum);
				min = sum < min ? sum : min;
				max = sum > max ? sum : max;
									
				min = min > 0 ? min : 0;
				output.pixels[x+indexCutoff][y+indexCutoff] = sum > 0 ? sum : 0;
			}
		}
		
		// Rescale to 0-255
		max = max - min;
		for (int x = 0; x < input.width - imgCutoff; x++) {
			for (int y = 0; y < input.height - imgCutoff; y++) {
				output.pixels[x+indexCutoff][y+indexCutoff] = (int) (255 * ((double) (output.pixels[x+indexCutoff][y+indexCutoff] - min) / max));
			}
		}
		
		return output;
	}
	
	static double[][] generateKernel(int size, int sigma) {
		double[][] kernel = new double[size][size];
		double maxIndex = (double) ((size - 1) / 2);
		
		double twoSigmaSquared = 2 * sigma * sigma;
		double oneOverTwoPiSigmaSquared = 1. / (double) (Math.PI * twoSigmaSquared);
		double denominatorialExp = - ((maxIndex*maxIndex + maxIndex*maxIndex) / twoSigmaSquared);
		double denominator = oneOverTwoPiSigmaSquared * Math.exp(denominatorialExp);
		
		double kernelSum = 0;
		
		for (int x = (int) -maxIndex; x < (maxIndex + 1); x++) {
			for (int y = (int) -maxIndex; y < (maxIndex + 1); y++) {
				int iX = x + (int) maxIndex;
				int iY = y + (int) maxIndex;
				
				double numeratorialExp = - ((x*x + y*y) / twoSigmaSquared);
				double numerator = oneOverTwoPiSigmaSquared  * Math.exp(numeratorialExp);
				
				double result = numerator / denominator;
				kernelSum += result;
				kernel[iX][iY] = result;
			}
		}
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				kernel[x][y] = kernel[x][y] / kernelSum;
			}
		}
		
		return kernel;
	}
}