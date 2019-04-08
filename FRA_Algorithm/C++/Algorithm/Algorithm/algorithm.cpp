#include <iostream>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

template <typename _Tp> static
void lbp_(Mat src, OutputArray _dst) {
	_dst.create(src.rows - 2, src.cols - 2, CV_8UC1);
	Mat dst = _dst.getMat();
	dst.setTo(0);
	for (int i = 1; i < src.rows - 1; i++){
		for (int j = 1; j < src.cols - 1; j++){
			_Tp center = src.at<_Tp>(i, j);
			unsigned char code = 0;
			code |= (src.at<_Tp>(i - 1, j - 1) >= center) << 7;
			code |= (src.at<_Tp>(i - 1, j) >= center) << 6;
			code |= (src.at<_Tp>(i - 1, j + 1) >= center) << 5;
			code |= (src.at<_Tp>(i, j + 1) >= center) << 4;
			code |= (src.at<_Tp>(i + 1, j + 1) >= center) << 3;
			code |= (src.at<_Tp>(i + 1, j) >= center) << 2;
			code |= (src.at<_Tp>(i + 1, j - 1) >= center) << 1;
			code |= (src.at<_Tp>(i, j - 1) >= center) << 0;
			dst.at<unsigned char>(i - 1, j - 1) = code;
		}
	}
}

static void lbp(Mat src, OutputArray dst)
{
	int type = src.type();
	switch (type) {
	case CV_8SC1:   lbp_<char>(src, dst); break;
	case CV_8UC1:   lbp_<unsigned char>(src, dst); break;
	case CV_16SC1:  lbp_<short>(src, dst); break;
	case CV_16UC1:  lbp_<unsigned short>(src, dst); break;
	case CV_32SC1:  lbp_<int>(src, dst); break;
	case CV_32FC1:  lbp_<float>(src, dst); break;
	case CV_64FC1:  lbp_<double>(src, dst); break;
	default:
		string error_msg = format("Using Original Local Binary Patterns for feature extraction only works on single-channel images (given %d). Please pass the image data as a grayscale image!", type);
		CV_Error(CV_StsNotImplemented, error_msg);
		break;
	}
}

Mat lbp(Mat src) {
	Mat dst;
	lbp(src, dst);
	return dst;
}

template <typename _Tp> static
inline void DCP1_(Mat src, OutputArray _dst, int Rin, int Rex){
	int neighbors = 4;
	_dst.create(src.rows - 2 * Rex, src.cols - 2 * Rex, CV_32SC1);
	Mat dst = _dst.getMat();
	dst.setTo(0);
	float pixelA, pixelB;
	for (int k = 0; k < neighbors; k++){
		float Ax = static_cast<float>(Rin * sin(CV_PI * (2 * k) / static_cast<float>(neighbors)));
		float Ay = static_cast<float>(Rin * cos(CV_PI * (2 * k) / static_cast<float>(neighbors)));
		float Bx = static_cast<float>(Rex * sin(CV_PI * (2 * k) / static_cast<float>(neighbors)));
		float By = static_cast<float>(Rex * cos(CV_PI * (2 * k) / static_cast<float>(neighbors)));
		int fAx = static_cast<int>(floor(Ax));
		int fAy = static_cast<int>(floor(Ay));
		int cAx = static_cast<int>(ceil(Ax));
		int cAy = static_cast<int>(ceil(Ay));
		float tAy = Ay - fAy;
		float tAx = Ax - fAx;
		float wA1 = (1 - tAx) * (1 - tAy);
		float wA2 = tAx  * (1 - tAy);
		float wA3 = (1 - tAx) *      tAy;
		float wA4 = tAx  *      tAy;

		int fBx = static_cast<int>(floor(Bx));
		int fBy = static_cast<int>(floor(By));
		int cBx = static_cast<int>(ceil(Bx));
		int cBy = static_cast<int>(ceil(By));
		float tBy = By - fBy;
		float tBx = Bx - fBx;
		float wB1 = (1 - tBx) * (1 - tBy);
		float wB2 = tBx  * (1 - tBy);
		float wB3 = (1 - tBx) *      tBy;
		float wB4 = tBx  *      tBy;
		for (int i = Rex; i < src.rows - Rex; i++){
			for (int j = Rex; j < src.cols - Rex; j++){
				float pixelO = static_cast<float>(src.at<_Tp>(i, j));
				pixelA = static_cast<float>(src.at<_Tp>(i + fAy, j + fAx) * wA1 + src.at<_Tp>(i + fAy, j + cAx) *wA2 + src.at<_Tp>(i + cAy, j + fAx) * wA3 + src.at<_Tp>(i + cAy, j + cAx) *wA4);
				pixelB = static_cast<float>(src.at<_Tp>(i + fBy, j + fBx) * wB1 + src.at<_Tp>(i + fBy, j + cBx) *wB2 + src.at<_Tp>(i + cBy, j + fBx) * wB3 + src.at<_Tp>(i + cBy, j + cBx) *wB4);
				dst.at<int>(i - Rex, j - Rex) += ((pixelA >= pixelO) * 2 + (pixelB >= pixelA)) * (int)pow(4, k);
			}
		}
	}
}

template <typename _Tp> static
inline void DCP2_(Mat src, OutputArray _dst, int Rin, int Rex){
	int neighbors = 4;
	_dst.create(src.rows - 2 * Rex, src.cols - 2 * Rex, CV_32SC1);
	Mat dst = _dst.getMat();
	dst.setTo(0);
	float pixelA, pixelB;
	for (int k = 0; k < neighbors; k++){
		float Ax = static_cast<float>(Rin * sin(CV_PI * (2 * k + 1) / static_cast<float>(neighbors)));
		float Ay = static_cast<float>(Rin * cos(CV_PI * (2 * k + 1) / static_cast<float>(neighbors)));
		float Bx = static_cast<float>(Rex * sin(CV_PI * (2 * k + 1) / static_cast<float>(neighbors)));
		float By = static_cast<float>(Rex * cos(CV_PI * (2 * k + 1) / static_cast<float>(neighbors)));
		int fAx = static_cast<int>(floor(Ax));
		int fAy = static_cast<int>(floor(Ay));
		int cAx = static_cast<int>(ceil(Ax));
		int cAy = static_cast<int>(ceil(Ay));
		float tAy = Ay - fAy;
		float tAx = Ax - fAx;
		float wA1 = (1 - tAx) * (1 - tAy);
		float wA2 = tAx  * (1 - tAy);
		float wA3 = (1 - tAx) *      tAy;
		float wA4 = tAx  *      tAy;

		int fBx = static_cast<int>(floor(Bx));
		int fBy = static_cast<int>(floor(By));
		int cBx = static_cast<int>(ceil(Bx));
		int cBy = static_cast<int>(ceil(By));
		float tBy = By - fBy;
		float tBx = Bx - fBx;
		float wB1 = (1 - tBx) * (1 - tBy);
		float wB2 = tBx  * (1 - tBy);
		float wB3 = (1 - tBx) *      tBy;
		float wB4 = tBx  *      tBy;
		for (int i = Rex; i < src.rows - Rex; i++){
			for (int j = Rex; j < src.cols - Rex; j++){
				float pixelO = static_cast<float>(src.at<_Tp>(i, j));
				pixelA = static_cast<float>(src.at<_Tp>(i + fAy, j + fAx) * wA1 + src.at<_Tp>(i + fAy, j + cAx) *wA2 + src.at<_Tp>(i + cAy, j + fAx) * wA3 + src.at<_Tp>(i + cAy, j + cAx) *wA4);
				pixelB = static_cast<float>(src.at<_Tp>(i + fBy, j + fBx) * wB1 + src.at<_Tp>(i + fBy, j + cBx) *wB2 + src.at<_Tp>(i + cBy, j + fBx) * wB3 + src.at<_Tp>(i + cBy, j + cBx) *wB4);
				dst.at<int>(i - Rex, j - Rex) += ((pixelA >= pixelO) * 2 + (pixelB >= pixelA)) * (int)pow(4, k);
			}
		}
	}
}

static void DCP1(Mat src, OutputArray dst, int Rin, int Rex){
	int srctype = src.type();
	switch (srctype){
	case CV_8SC1:   DCP1_<char>(src, dst, Rin, Rex); break;
	case CV_8UC1:   DCP1_<unsigned char>(src, dst, Rin, Rex); break;
	case CV_16SC1:  DCP1_<short>(src, dst, Rin, Rex); break;
	case CV_16UC1:  DCP1_<unsigned short>(src, dst, Rin, Rex); break;
	case CV_32SC1:  DCP1_<int>(src, dst, Rin, Rex); break;
	case CV_32FC1:  DCP1_<float>(src, dst, Rin, Rex); break;
	case CV_64FC1:  DCP1_<double>(src, dst, Rin, Rex); break;
	default:
		string error_msg = format("Using Original Dual-Cross Patterns for feature extraction only works on single-channel images (given %d). Please pass the image data as a grayscale image!", srctype);
		CV_Error(CV_StsNotImplemented, error_msg);
		break;
	}
}

static void DCP2(Mat src, OutputArray dst, int Rin, int Rex){
	int srctype = src.type();
	switch (srctype){
	case CV_8SC1:   DCP2_<char>(src, dst, Rin, Rex); break;
	case CV_8UC1:   DCP2_<unsigned char>(src, dst, Rin, Rex); break;
	case CV_16SC1:  DCP2_<short>(src, dst, Rin, Rex); break;
	case CV_16UC1:  DCP2_<unsigned short>(src, dst, Rin, Rex); break;
	case CV_32SC1:  DCP2_<int>(src, dst, Rin, Rex); break;
	case CV_32FC1:  DCP2_<float>(src, dst, Rin, Rex); break;
	case CV_64FC1:  DCP2_<double>(src, dst, Rin, Rex); break;
	default:
		string error_msg = format("Using Original Dual-Cross Patterns for feature extraction only works on single-channel images (given %d). Please pass the image data as a grayscale image!", srctype);
		CV_Error(CV_StsNotImplemented, error_msg);
		break;
	}
}

Mat DCP1(Mat src, int Rin, int Rex){
	Mat dst;
	DCP1(src, dst, Rin, Rex);
	return dst;
}

Mat DCP2(Mat src, int Rin, int Rex){
	Mat dst;
	DCP2(src, dst, Rin, Rex);
	return dst;
}
