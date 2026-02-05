# Hough Squares Transform, a University Assignment
Completed as a part of CM3113 Computer Vision module, led by [Paul Rosin](https://github.com/rosinpl).

[Image.Java](https://github.com/rosinpl/rosinpl.github.io/blob/main/CM3113/LABS/Image.java) and 
[ImagePPM.Java](https://github.com/rosinpl/rosinpl.github.io/blob/main/CM3113/LABS/ImagePPM.java) 
are not my own, and can be sourced from Paul Rosin's GitHub via the attached links. 

This project was a part of my coursework for the CM3113 module, with the aim of detecting straight lines and squares within bitmap images.
The results of which are outputted into varying files, as explained below.
The code uses methods such as Sobel filtering, Difference of Gaussians, Orientational Moments, the Hough Transform and the Hough Squares Transform.
The aim of the code is the detect edges within an image, plot lines relative to those edges, and then plot squares relative to those lines, backprojecting over the initial edge detection as a measure of accuracy.

### Dependencies:
 - JVM 25, probably compatible with earlier versions
 - Python3.12, probably compatible with earlier versions however
 - [Pillow 12.1.0](https://pypi.org/project/pillow/), install via pip

### Initialisation:
 - Build classes using `javac HoughTransform.java`
 - Create a Python VM `python -m venv venv` (Optional)
 - Activate Python VM `cd venv/Scripts && activate.bat && cd ../..` (Optional - dependent on step above)
 - Install pillow `pip install pillow`

### Usage:

To run the program you will do `java HoughTransform p1 p2 p3 p4 p5 p6 p7` where p1-p7 are the input parameters

#### Parameters:
- p1: Input image file name, should be in current directory. .PGM file
- p2: Expected length in pixels of a side of a square within the image
- p3: The range of θ to be searched around the estimated orientation moment within the rho-theta hough paramaterisation 
  - 5 is the recommended value
- p4: p4 is a factor of the minimum acceptable Hough accumulator value, multiplied against the maxmimum Hough space value
  - 0.25 is the recommended value
- p5: Threshold for peaks in the Square Hough Space
  - 0.75 is the recommended value
- p6: Threshold for retention of squares during back projection
  - 0.75 is the recommended value
- p7: Option to choose the edge detection method, either a Difference of Gaussian (L) or the Difference of Gaussian against an inverted Sobel edge magnitude (E)
  - L or E are the only input options

With parameters correctly optimised, running the program should produce 5 output files

#### Outputs:
 - DoG/SobelDoG.pgm - The edge detection result
 - accumulator.pgm – The Hough Space, rescaled
 - lines.ppm – Green lines overlaid on the input image, detected via the Hough Transform
 - squares.ppm – Red squares overlaid on the input image, detected via the Square Hough Transform
 - backprojection.ppm – Blue squares backprojected onto squares.ppm

Due to the unconventional nature of the inputs and outputs being .PPM and .PGM files, the python script `pgmView.py` is included for viewing the image files.
Provided dependencies are installed, you can run the script via `python pgmView.py`
The script will open all output files (assuming p7 is L), but the script can be easily changed to open fewer or more files as you please.