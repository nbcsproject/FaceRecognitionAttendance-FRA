package com.android.fra;

import android.graphics.Bitmap;

import com.android.fra.db.Face;

import org.litepal.LitePal;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.List;

public class LBP {

    private static Mat lbp(Mat src) {
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

    private static Mat histc(Mat src, int minVal, int maxVal, boolean normed) {
        Mat result = new Mat();
        MatOfInt histSize = new MatOfInt(maxVal - minVal + 1);
        MatOfFloat histRange = new MatOfFloat(minVal, maxVal + 1);
        MatOfInt channels = new MatOfInt(0);
        src.convertTo(src, CvType.CV_8UC1);
        Imgproc.calcHist(Arrays.asList(src), channels, new Mat(), result, histSize, histRange);
        if (normed) {
            for (int index = 0; index < result.rows(); index++) {
                result.put(index, 0, result.get(index, 0)[0] / (float) src.total());
            }
        }
        return result;
    }

    private static Mat spatial_histogram(Mat src, int min, int numPatterns, int grid_x, int grid_y) {
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
                Mat result_row = result.row(resultRowIdx);
                cell_hist.reshape(1, 1).convertTo(result_row, CvType.CV_32FC1);
                resultRowIdx++;
            }
        }
        return result.reshape(1, 1);
    }

    public static String getFeature(Bitmap srcBitmap, int _grid_x, int _grid_y) {
        Size size = new Size(srcBitmap.getWidth(), srcBitmap.getHeight());
        Mat src = Mat.zeros(size, CvType.CV_8UC4);
        Utils.bitmapToMat(srcBitmap, src, false);
        Imgproc.resize(src, src, new Size(128, 128));
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(src, src);
        Mat lbp_image = lbp(src);
        Mat p = spatial_histogram(lbp_image, 0, (int) Math.pow(2, 8), _grid_x, _grid_y);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < p.cols(); i++) {
            result.append(p.get(0, i)[0]).append(" ");
        }
        return result.toString();
    }

    public String getFaceOwner(String srcFeature, String pid, double threshold) {
        String[] srcStringSplit = srcFeature.split(" ");
        Mat query = Mat.zeros(1, srcStringSplit.length, CvType.CV_32FC1);
        for (int i = 0; i < srcStringSplit.length; i++) {
            query.put(0, i, Float.parseFloat(srcStringSplit[i]));
        }

        List<Face> faces = LitePal.where("pid = ?", pid).find(Face.class);
        double minDist = java.lang.Double.MAX_VALUE;
        String faceOwner = " ";
        for (Face face : faces) {
            String comStringFeature = face.getFeature();
            String[] comStringSplit = comStringFeature.split(String.valueOf(" "));
            Mat comHist = Mat.zeros(1, comStringSplit.length, CvType.CV_32FC1);
            for (int i = 0; i < comStringSplit.length; i++) {
                comHist.put(0, i, Float.parseFloat(comStringSplit[i]));
            }
            double dist = Imgproc.compareHist(comHist, query, Imgproc.HISTCMP_CHISQR_ALT);
            if (dist < minDist) {
                minDist = dist;
                faceOwner = face.getUid();
            }
        }
        if (minDist <= threshold) {
            return faceOwner;
        } else {
            return "NoFaceOwner";
        }
    }

}
