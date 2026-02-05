public class Sobel {
	// Sobel kernel for Horizontal edges
	static double[][] sobelXKernel = new double[][] {
		{-1, 0, 1},
		{-2, 0, 2},
		{-1, 0, 1}
	};
	
	// Sobel kernel for verticle edges
	static double[][] sobelYKernel = new double[][] {
		{-1, -2, -1},
		{0, 0, 0},
		{1, 2, 1}
	};
	
	static Image computeSobel(Image input) {
		Image output = new Image(input.depth, input.width, input.height);
		int min = 2147483647;
		int max = 0;
		
		// Loop through input to calculate both verticle and horizontal Sobels
		// Then find the absolute values and combine them
		for (int x = 0; x < input.width - 2; x++) {
			for (int y = 0; y < input.height - 2; y++) {
				double xSum = 0;
				double ySum = 0;
				
				// Loop through each Sobel kernel
				for (int yy = 0; yy < 3; yy++) {
					for (int xx = 0; xx < 3; xx++) {
						xSum += sobelXKernel[xx][yy] * input.pixels[x+xx][y+yy];
						ySum += sobelYKernel[xx][yy] * input.pixels[x+xx][y+yy];
					}
				}
				
				int sum = (int) (Math.abs(xSum) + Math.abs(ySum));
				min = sum < min ? sum : min;
				max = sum > max ? sum : max;
				
				output.pixels[x+1][y+1] = sum;
			}
		}
		
		// Rescale to 0-255
		max = max - min;
		for (int x = 0; x < input.width - 2; x++) {
			for (int y = 0; y < input.height - 2; y++) {
				output.pixels[x+1][y+1] = 255 - (int) (255 * ((double) (output.pixels[x+1][y+1] - min) / max));
			}
		}
		
		return output;
	}
}