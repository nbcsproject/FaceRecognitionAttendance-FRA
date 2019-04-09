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
	
	public Mat DCP1(Mat src, int Rin, int Rex){
		int neighbors = 4;
		Size size = new Size(src.rows() - 2 * Rex, src.cols() - 2 * Rex);
		Mat dst = Mat.zeros(size, CvType.CV_8UC1);
		float pixelA, pixelB;
		for (int k = 0; k < neighbors; k++){
			float Ax = (float)(Rin * Math.sin(Math.PI * (2 * k) / (float)(neighbors)));
			float Ay = (float)(Rin * Math.cos(Math.PI * (2 * k) / (float)(neighbors)));
			float Bx = (float)(Rex * Math.sin(Math.PI * (2 * k) / (float)(neighbors)));
			float By = (float)(Rex * Math.cos(Math.PI * (2 * k) / (float)(neighbors)));
			int fAx = (int)(Math.floor(Ax));
			int fAy = (int)(Math.floor(Ay));
			int cAx = (int)(Math.ceil(Ax));
			int cAy = (int)(Math.ceil(Ay));
			float tAy = Ay - fAy;
			float tAx = Ax - fAx;
			float wA1 = (1 - tAx) * (1 - tAy);
			float wA2 = tAx  * (1 - tAy);
			float wA3 = (1 - tAx) *      tAy;
			float wA4 = tAx  *      tAy;

			int fBx = (int)(Math.floor(Bx));
			int fBy = (int)(Math.floor(By));
			int cBx = (int)(Math.ceil(Bx));
			int cBy = (int)(Math.ceil(By));
			float tBy = By - fBy;
			float tBx = Bx - fBx;
			float wB1 = (1 - tBx) * (1 - tBy);
			float wB2 = tBx  * (1 - tBy);
			float wB3 = (1 - tBx) *      tBy;
			float wB4 = tBx  *      tBy;
			for (int i = Rex; i < src.rows() - Rex; i++){
				for (int j = Rex; j < src.cols() - Rex; j++){
					float pixelO = (float)(src.get(i, j)[0]);
					pixelA = (float)(src.get(i + fAy, j + fAx)[0] * wA1 + src.get(i + fAy, j + cAx)[0] *wA2 + src.get(i + cAy, j + fAx)[0] * wA3 + src.get(i + cAy, j + cAx)[0] *wA4);
					pixelB = (float)(src.get(i + fBy, j + fBx)[0] * wB1 + src.get(i + fBy, j + cBx)[0] *wB2 + src.get(i + cBy, j + fBx)[0] * wB3 + src.get(i + cBy, j + cBx)[0] *wB4);
					dst.put(i - Rex, j - Rex , (int)dst.get(i - Rex, j - Rex)[0] + ((pixelA >= pixelO ? 1 : 0) * 2 + (pixelB >= pixelA ? 1 : 0)) * (int)Math.pow(4, k));
				}
			}
		}
		return dst;
	}

	public Mat DCP2(Mat src, int Rin, int Rex){
		int neighbors = 4;
		Size size = new Size(src.rows() - 2 * Rex, src.cols() - 2 * Rex);
		Mat dst = Mat.zeros(size, CvType.CV_8UC1);
		float pixelA, pixelB;
		for (int k = 0; k < neighbors; k++){
			float Ax = (float)(Rin * Math.sin(Math.PI * (2 * k + 1) / (float)(neighbors)));
			float Ay = (float)(Rin * Math.cos(Math.PI * (2 * k + 1) / (float)(neighbors)));
			float Bx = (float)(Rex * Math.sin(Math.PI * (2 * k + 1) / (float)(neighbors)));
			float By = (float)(Rex * Math.cos(Math.PI * (2 * k + 1) / (float)(neighbors)));
			int fAx = (int)(Math.floor(Ax));
			int fAy = (int)(Math.floor(Ay));
			int cAx = (int)(Math.ceil(Ax));
			int cAy = (int)(Math.ceil(Ay));
			float tAy = Ay - fAy;
			float tAx = Ax - fAx;
			float wA1 = (1 - tAx) * (1 - tAy);
			float wA2 = tAx  * (1 - tAy);
			float wA3 = (1 - tAx) *      tAy;
			float wA4 = tAx  *      tAy;

			int fBx = (int)(Math.floor(Bx));
			int fBy = (int)(Math.floor(By));
			int cBx = (int)(Math.ceil(Bx));
			int cBy = (int)(Math.ceil(By));
			float tBy = By - fBy;
			float tBx = Bx - fBx;
			float wB1 = (1 - tBx) * (1 - tBy);
			float wB2 = tBx  * (1 - tBy);
			float wB3 = (1 - tBx) *      tBy;
			float wB4 = tBx  *      tBy;
			for (int i = Rex; i < src.rows() - Rex; i++){
				for (int j = Rex; j < src.cols() - Rex; j++){
					float pixelO = (float)(src.get(i, j)[0]);
					pixelA = (float)(src.get(i + fAy, j + fAx)[0] * wA1 + src.get(i + fAy, j + cAx)[0] *wA2 + src.get(i + cAy, j + fAx)[0] * wA3 + src.get(i + cAy, j + cAx)[0] *wA4);
					pixelB = (float)(src.get(i + fBy, j + fBx)[0] * wB1 + src.get(i + fBy, j + cBx)[0] *wB2 + src.get(i + cBy, j + fBx)[0] * wB3 + src.get(i + cBy, j + cBx)[0] *wB4);
					dst.put(i - Rex, j - Rex , (int)dst.get(i - Rex, j - Rex)[0] + ((pixelA >= pixelO ? 1 : 0) * 2 + (pixelB >= pixelA ? 1 : 0)) * (int)Math.pow(4, k));
				}
			}
		}
		return dst;
	}
}
