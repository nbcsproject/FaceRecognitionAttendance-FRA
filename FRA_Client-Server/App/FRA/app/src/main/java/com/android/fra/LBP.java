package com.android.fra;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import static org.opencv.imgproc.Imgproc.INTER_NEAREST;

public class LBP {
    //static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    //static {
        //System.loadLibrary("native-lib");
    //}

    private static Vector<Mat> _histograms = new Vector<Mat>();
    private static Vector<String> _labels = new Vector<String>();
    static int[] RUTable=new int[256];

    private static int calc_sum(int r)
    {
        int res_sum;
        res_sum = 0;
        while (r!=0)
        {
            res_sum = res_sum + r % 2;
            r /= 2;
        }
        return res_sum;
    }

    private static void getRUTable(int neighbors)
    {
        int numt = 0, tem_xor = 0;
        int i, j;
        for (i = 0; i< (int)Math.pow(2, neighbors); i++)
        {
            j = i << 1;
            if (j > (int)Math.pow(2, neighbors) - 1)
                j -= ((int)Math.pow(2, neighbors) - 1);
            tem_xor = i ^ j;	// 异或
            numt = calc_sum(tem_xor);//计算异或结果中1的个数，即跳变个数
            if (numt <= 2)
                RUTable[i] = calc_sum(i);
            else
                RUTable[i] = neighbors + 1;
        }
    }

    public static Mat lbp(Mat src) {
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

    public static Mat histc(Mat src, int minVal, int maxVal, boolean normed) {
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

    public static Mat spatial_histogram(Mat src, int min, int numPatterns, int grid_x, int grid_y) {
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

    public static void read_csv_image(String fileName, char separator, int lines, int _grid_x, int _grid_y) {
        int j = 0;
        try (FileReader reader = new FileReader(fileName); BufferedReader br = new BufferedReader(reader)) {
            String line, path, label;
            while ((line = br.readLine()) != null) {
                String[] argline = line.split(String.valueOf(separator));
                path = argline[0];
                label = argline[1];
                if (!path.isEmpty() && !label.isEmpty()) {
                    System.out.println("COMPUTING NUM"+(j+1)+" PIC`S FEATURES...");
                    Mat src = Imgcodecs.imread(path);
                    Imgproc.resize(src, src, new Size(128, 128));
                    Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
                    Imgproc.equalizeHist(src, src);
                    Mat lbp_image = lbp(src);
                    Mat p1 = spatial_histogram(lbp_image, 0, (int)Math.pow(2,8), _grid_x, _grid_y);
                    Mat p = Mat.zeros(1, p1.cols(), CvType.CV_32FC1);
                    for (int i = 0; i < p1.cols(); i++) {
                        p.put(0, i, p1.get(0, i)[0]);
                    }
                    _histograms.addElement(p);
                }
                j++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void read_csv_label(String fileName, char separator, int lines) {
        try (FileReader reader = new FileReader(fileName); BufferedReader br = new BufferedReader(reader)) {
            String line, path, label;
            while ((line = br.readLine()) != null) {
                String[] argline = line.split(String.valueOf(separator));
                path = argline[0];
                label = argline[1];
                if (!path.isEmpty() && !label.isEmpty()) {
                    _labels.addElement(label);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void train(String fileName, char separator, int lines, int _grid_x, int _grid_y) {
        System.out.println("READING TRAIN DATA...");
        read_csv_image(fileName, separator, lines, _grid_x, _grid_y);
        read_csv_label(fileName, separator, lines);
        System.out.println("OPERATING DONE");
    }

    public static void predict(String fileName, char separator, int lines, int _grid_x, int _grid_y) {
        int j = 0;
        int count = 0;
        try (FileReader reader = new FileReader(fileName); BufferedReader br = new BufferedReader(reader)) {
            String line, path, label;
            while ((line = br.readLine()) != null) {
                String[] argline = line.split(String.valueOf(separator));
                path = argline[0];
                label = argline[1];
                if (!path.isEmpty() && !label.isEmpty()) {
                    Mat src = Imgcodecs.imread(path);
                    Imgproc.resize(src, src, new Size(128, 128));
                    Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
                    Imgproc.equalizeHist(src, src);
                    Mat lbp_image = lbp(src);
                    Mat q1 = spatial_histogram(lbp_image, 0, (int)Math.pow(2,8), _grid_x, _grid_y);
                    Mat query = Mat.zeros(1, q1.cols(), CvType.CV_32FC1);
                    for (int i = 0; i < q1.cols(); i++) {
                        query.put(0, i, q1.get(0, i)[0]);
                    }
                    double minDist = Double.MAX_VALUE;
                    int minClass = -1;
                    for (int sampleIdx = 0; sampleIdx < _histograms.size(); sampleIdx++) {
                        double dist = Imgproc.compareHist(_histograms.get(sampleIdx), query, Imgproc.HISTCMP_CHISQR_ALT);
                        if (dist < minDist) {
                            minDist = dist;
                            minClass = Integer.parseInt(_labels.get(sampleIdx));
                        }
                    }
                    System.out.println((j+1)+" "+minClass+" "+Integer.parseInt(label));
                    if (minClass == Integer.parseInt(label)) {
                        count++;
                    }
                }
                j++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Accuracy= "+count*100/(float)lines+"% ("+count+"/"+lines+") (classification)");
    }

    public static int getlinenum(String fileName, char separator) {
        int j = 0;
        try (FileReader reader = new FileReader(fileName); BufferedReader br = new BufferedReader(reader)) {
            String line, path, label;
            while ((line = br.readLine()) != null) {
                String[] argline = line.split(String.valueOf(separator));
                path = argline[0];
                label = argline[1];
                if (!path.isEmpty() && !label.isEmpty()) {
                    j++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return j;
    }

    public static String getfeature(String srcpath, int _grid_x, int _grid_y){
        Mat src = Imgcodecs.imread(srcpath);
        Imgproc.resize(src, src, new Size(128, 128));
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(src, src);
        Mat lbp_image = lbp(src);
        Mat p = spatial_histogram(lbp_image, 0, (int)Math.pow(2,8), _grid_x, _grid_y);
        StringBuilder result=new StringBuilder();
        for(int i=0;i<p.cols();i++){
            result.append(p.get(0, i)[0]).append(" ");
        }
        return result.toString();
    }

    public static double comparedist(String srcpath, String comString, int _grid_x, int _grid_y){
        Mat src = Imgcodecs.imread(srcpath);
        Imgproc.resize(src, src, new Size(128, 128));
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(src, src);
        Mat lbp_image = lbp(src);
        Mat query = spatial_histogram(lbp_image, 0, (int)Math.pow(2,8), _grid_x, _grid_y);
        String[] comStringSplit = comString.split(String.valueOf(" "));
        Mat comhist=Mat.zeros(1, comStringSplit.length, CvType.CV_32FC1);
        for(int i=0;i<comStringSplit.length;i++){
            comhist.put(0, i, Float.parseFloat(comStringSplit[i]));
        }
        double dist = Imgproc.compareHist(comhist, query, Imgproc.HISTCMP_CHISQR_ALT);
        return dist;
    }

    public static void main(String[] args) {
        int trainline = getlinenum("D:\\test\\attrain.txt", ';');
        int predictline = getlinenum("D:\\test\\atpredict.txt", ';');
        int _grid_x = 8, _grid_y = 8;
        train("D:\\test\\attrain.txt", ';', trainline, _grid_x, _grid_y);
        predict("D:\\test\\atpredict.txt", ';', predictline, _grid_x, _grid_y);
    }
}
