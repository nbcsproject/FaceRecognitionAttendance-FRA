#include <iostream>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

static Mat histc_(const Mat& src, int minVal = 0, int maxVal = 255, bool normed = false){
	Mat result;
	int histSize = maxVal - minVal + 1;
	float range[] = { static_cast<float>(minVal), static_cast <float> (maxVal + 1) };
	const float* histRange = { range };
	calcHist(&src, 1, 0, Mat(), result, 1, &histSize, &histRange, true, false);
	if (normed){
		result /= (int)src.total();
	}
	return result.reshape(1, 1);
}

static Mat histc(Mat src, int minVal, int maxVal, bool normed){
	switch (src.type()){
	case CV_8SC1:
		return histc_(Mat_<float>(src), minVal, maxVal, normed);
		break;
	case CV_8UC1:
		return histc_(src, minVal, maxVal, normed);
		break;
	case CV_16SC1:
		return histc_(Mat_<float>(src), minVal, maxVal, normed);
		break;
	case CV_16UC1:
		return histc_(src, minVal, maxVal, normed);
		break;
	case CV_32SC1:
		return histc_(Mat_<float>(src), minVal, maxVal, normed);
		break;
	case CV_32FC1:
		return histc_(src, minVal, maxVal, normed);
		break;
	case CV_64FC1:
		return histc_(src, minVal, maxVal, normed);
		break;
	default:
		CV_Error(CV_StsUnmatchedFormats, "This type is not implemented yet.");
		break;
	}
	return Mat();
}

Mat spatial_histogram(Mat src, int min, int numPatterns, int grid_x, int grid_y){
	int width = src.cols / grid_x;
	int height = src.rows / grid_y;
	Mat result = Mat::zeros(grid_x * grid_y, numPatterns - min, CV_32FC1);
	if (src.empty()){
		return result.reshape(1, 1);
	}
	int resultRowIdx = 0;
	for (int i = 0; i < grid_y; i++){
		for (int j = 0; j < grid_x; j++){
			Mat src_cell = Mat(src, Range(i*height, (i + 1)*height), Range(j*width, (j + 1)*width));
			Mat cell_hist = histc(src_cell, min, (numPatterns - 1), true);
			Mat result_row = result.row(resultRowIdx);
			cell_hist.reshape(1, 1).convertTo(result_row, CV_32FC1);
			resultRowIdx++;
		}
	}
	return result.reshape(1, 1);
}