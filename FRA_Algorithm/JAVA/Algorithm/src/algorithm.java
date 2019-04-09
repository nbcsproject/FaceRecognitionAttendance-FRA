import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

public class algorithm {
	public Mat lbp(Mat src) {
		Size size = new Size(src.rows() - 2, src.cols() - 2);
		Mat dst = Mat.zeros(size, CvType.CV_8UC1);
		for (int i = 1; i < src.rows() - 1; i++) {
			for (int j = 1; j < src.cols() - 1; j++) {
				double[] center = src.get(i, j);
				char code = 0;
				code |= (src.get(i - 1, j - 1)[0] >= center[0] ? 1 : 0) << 7;
				code |= (src.get(i - 1, j)[0] >= center[0] ? 1 : 0) << 6;
				code |= (src.get(i - 1, j + 1)[0] >= center[0] ? 1 : 0) << 5;
				code |= (src.get(i, j + 1)[0] >= center[0] ? 1 : 0) << 4;
				code |= (src.get(i + 1, j + 1)[0] >= center[0] ? 1 : 0) << 3;
				code |= (src.get(i + 1, j)[0] >= center[0] ? 1 : 0) << 2;
				code |= (src.get(i + 1, j - 1)[0] >= center[0] ? 1 : 0) << 1;
				code |= (src.get(i, j - 1)[0] >= center[0] ? 1 : 0) << 0;
				dst.put(i - 1, j - 1, code);
			}
		}
		return dst;
	}
}
