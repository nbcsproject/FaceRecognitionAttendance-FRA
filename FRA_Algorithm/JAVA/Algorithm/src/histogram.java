import java.util.Arrays;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class histogram {
	private Mat histc(Mat src, int minVal, int maxVal, boolean normed) {
		Mat result = new Mat();
		MatOfInt histSize = new MatOfInt(maxVal - minVal + 1);
		MatOfFloat histRange = new MatOfFloat(minVal, maxVal + 1);
		MatOfInt channels = new MatOfInt(0);
		src.convertTo(src, CvType.CV_8UC1);
		Imgproc.calcHist(Arrays.asList(src), channels, new Mat(), result, histSize, histRange);
		if (normed) {
			for (int index = 0; index < result.rows(); index++) {
				result.put(index, 0, result.get(index, 0)[0] / (float)src.total());
			}
		}
		return result;
	}

	public Mat spatial_histogram(Mat src, int min, int numPatterns, int grid_x, int grid_y) {
		int width = src.cols() / grid_x;
		int height = src.rows() / grid_y;
		Size size = new Size(numPatterns - min, grid_x * grid_y);
		Mat result = Mat.zeros(size, CvType.CV_32FC1);
		if (src.empty()) {
			return result.reshape(1, 1);
		}
		int resultRowIdx = 0;
		for (int i = 0; i < grid_y; i++) {
			for (int j = 0; j < grid_x; j++) {
				Range range_height = new Range(i * height, (i + 1) * height);
				Range range_width = new Range(j * width, (j + 1) * width);
				Mat src_cell = new Mat(src, range_height, range_width);
				Mat cell_hist = histc(src_cell, min, (numPatterns - 1), true);
				Mat result_row=result.row(resultRowIdx);
				cell_hist.reshape(1,1).convertTo(result_row, CvType.CV_32FC1);
				resultRowIdx++;
			}
		}
		return result.reshape(1, 1);
	}
}
