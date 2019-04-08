#include <iostream>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

std::vector<Mat> _histograms;
Mat _labels;

extern Mat lbp(Mat src);
extern Mat DCP1(Mat src, int Rin, int Rex);
extern Mat DCP2(Mat src, int Rin, int Rex);
extern Mat spatial_histogram(Mat src, int min, int numPatterns, int grid_x, int grid_y);

void read_csv_image(string fileName, char separator, int lines, int _grid_x, int _grid_y){
	int j = 0;
	ifstream file(fileName, ifstream::in);
	string line, path, label;
	while (getline(file, line)){
		stringstream lines(line);
		getline(lines, path, separator);
		getline(lines, label);
		if (!path.empty() && !label.empty()){
			cout << "COMPUTING NUM" << j + 1 << " PIC`S FEATURES..." << endl;
			Mat src = imread(path);
			resize(src, src, cv::Size(128, 128));
			cvtColor(src, src, CV_BGR2GRAY);
			equalizeHist(src, src);

			/* If you use LBP algorithm, caculate the characteristic histogram using below method:*/

			Mat lbp_image = lbp(src);
			Mat p1 = spatial_histogram(lbp_image, 0, (int)pow(2, 8), _grid_x, _grid_y);
			Mat p = Mat::zeros(1, p1.cols, CV_32FC1);
			for (int i = 0; i < p1.cols; i++){
				p.at<float>(i) = p1.at<float>(i);
			}

			/* If you use DCP algorithm, caculate the characteristic histogram using below method
			(You need to define Rin and Rex in functions first):*/

			//Mat dcp_image1 = DCP1(src, Rin, Rex);
			//Mat dcp_image2 = DCP2(src, Rin, Rex);
			//Mat p1 = spatial_histogram(dcp_image1, 0, (int)pow(2, 8), _grid_x, _grid_y);
			//Mat p2 = spatial_histogram(dcp_image2, 0, (int)pow(2, 8), _grid_x, _grid_y);
			//Mat p = Mat::zeros(1, p1.cols + p2.cols, CV_32FC1);
			//for (int index1 = 0; index1 < p1.cols; index1++){
			//	p.at<float>(index1) = p1.at<float>(index1);
			//}
			//for (int index2 = p1.cols; index2 < p1.cols + p2.cols; index2++){
			//	p.at<float>(index2) = p2.at<float>(index2 - p1.cols);
			//}

			_histograms.push_back(p);
		}
		j++;
	}
	file.close();
}

void read_csv_label(string fileName, char separator, int lines){
	ifstream file(fileName, ifstream::in);
	string line, path, label;
	while (getline(file, line)){
		stringstream lines(line);
		getline(lines, path, separator);
		getline(lines, label);
		if (!path.empty() && !label.empty()){
			_labels.push_back(atoi(label.c_str()));
		}
	}
	file.close();
}

void train(string fileName, char separator, int lines, int _grid_x, int _grid_y){
	cout << "READING TRAIN DATA..." << endl;
	read_csv_image(fileName, separator, lines, _grid_x, _grid_y);
	read_csv_label(fileName, separator, lines);
	cout << "OPERATING DONE" << endl;
}

void predict(string fileName, char separator, int lines, int _grid_x, int _grid_y){
	int j = 0;
	int count = 0;
	ifstream file(fileName, ifstream::in);
	string line, path, label;
	while (getline(file, line)){
		stringstream lines(line);
		getline(lines, path, separator);
		getline(lines, label);
		if (!path.empty() && !label.empty()){
			Mat src = imread(path);
			resize(src, src, cv::Size(128, 128));
			cvtColor(src, src, CV_BGR2GRAY);
			equalizeHist(src, src);

			/* If you use LBP algorithm, caculate the characteristic histogram using below method:*/

			Mat lbp_image = lbp(src);
			Mat q1 = spatial_histogram(lbp_image, 0, (int)pow(2, 8), _grid_x, _grid_y);
			Mat query = Mat::zeros(1, q1.cols, CV_32FC1);
			for (int i = 0; i < q1.cols; i++){
				query.at<float>(i) = q1.at<float>(i);
			}

			/* If you use DCP algorithm, caculate the characteristic histogram using below method
			(You need to define Rin and Rex in functions first):*/

			//Mat dcp_image1 = DCP1(src, Rin, Rex);
			//Mat dcp_image2 = DCP2(src, Rin, Rex);
			//Mat q1 = spatial_histogram(dcp_image1, 0, (int)pow(2, 8), _grid_x, _grid_y);
			//Mat q2 = spatial_histogram(dcp_image2, 0, (int)pow(2, 8), _grid_x, _grid_y);
			//Mat query = Mat::zeros(1, q1.cols + q2.cols, CV_32FC1);
			//for (int index1 = 0; index1 < q1.cols; index1++){
			//	query.at<float>(index1) = q1.at<float>(index1);
			//}
			//for (int index2 = q1.cols; index2 < q1.cols + q2.cols; index2++){
			//	query.at<float>(index2) = q2.at<float>(index2 - q1.cols);
			//}

			double minDist = DBL_MAX;
			int minClass = -1;
			for (size_t sampleIdx = 0; sampleIdx < _histograms.size(); sampleIdx++){
				double dist = compareHist(_histograms[sampleIdx], query, HISTCMP_CHISQR_ALT);
				if (dist < minDist){
					minDist = dist;
					minClass = _labels.at<int>((int)sampleIdx);
				}
			}
			cout << j + 1 << "   " << minClass << "   " << atoi(label.c_str()) << endl;
			if (minClass == atoi(label.c_str())){
				count++;
			}
		}
		j++;
	}
	file.close();
	cout << "Accuracy = " << (double)(count * 100 / (double)lines) << "%";
	cout << " (" << count << "/" << lines << ") (classification)" << endl;
}

int getlinenum(string fileName, char separator){
	int j = 0;
	ifstream file(fileName, ifstream::in);
	string line, path, label;
	while (getline(file, line)){
		stringstream lines(line);
		getline(lines, path, separator);
		getline(lines, label);
		if (!path.empty() && !label.empty()){
			j++;
		}
	}
	file.close();
	return j;
}