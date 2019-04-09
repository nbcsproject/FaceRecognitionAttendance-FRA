import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class traintest {
	static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
	
	private Vector<Mat> _histograms = new Vector<Mat>();
	private Vector<String> _labels = new Vector<String>();
	
	algorithm algorithm = new algorithm();
	histogram histogram = new histogram();
		
	private void read_csv_image(String fileName, char separator, int lines, int _grid_x, int _grid_y) {
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
					
					/* If you use LBP algorithm, calculate the characteristic histogram using below method:*/
					
					Mat lbp_image = algorithm.lbp(src);
					Mat p1 = histogram.spatial_histogram(lbp_image, 0, (int) Math.pow(2, 8), _grid_x, _grid_y);
					Mat p = Mat.zeros(1, p1.cols(), CvType.CV_32FC1);
					for (int i = 0; i < p1.cols(); i++) {
						p.put(0, i, p1.get(0, i)[0]);
					}
					
					/* If you use DCP algorithm, calculate the characteristic histogram using below method
					(You need to define Rin and Rex in functions first):*/

					//Mat dcp_image1 = algorithm.DCP1(src, 1, 4);
					//Mat dcp_image2 = algorithm.DCP2(src, 1, 4);
					//Mat p1 = histogram.spatial_histogram(dcp_image1, 0, (int)Math.pow(2, 8), _grid_x, _grid_y);
					//Mat p2 = histogram.spatial_histogram(dcp_image2, 0, (int)Math.pow(2, 8), _grid_x, _grid_y);
					//Mat p = Mat.zeros(1, p1.cols() + p2.cols(), CvType.CV_32FC1);
					//for (int index1 = 0; index1 < p1.cols(); index1++){
					//	p.put(0, index1, p1.get(0, index1)[0]);
					//}
					//for (int index2 = p1.cols(); index2 < p1.cols() + p2.cols(); index2++){
					//	p.put(0, index2, p1.get(0, index2 - p1.cols())[0]);
					//}
					
					_histograms.addElement(p);
				}
				j++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read_csv_label(String fileName, char separator, int lines) {
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
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void train(String fileName, char separator, int lines, int _grid_x, int _grid_y) {
		System.out.println("READING TRAIN DATA...");
		read_csv_image(fileName, separator, lines, _grid_x, _grid_y);
		read_csv_label(fileName, separator, lines);
		System.out.println("OPERATING DONE");
	}

	public void predict(String fileName, char separator, int lines, int _grid_x, int _grid_y) {
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
					
					/* If you use LBP algorithm, calculate the characteristic histogram using below method:*/
					
					Mat lbp_image = algorithm.lbp(src);
					Mat q1 = histogram.spatial_histogram(lbp_image, 0, (int) Math.pow(2, 8), _grid_x, _grid_y);
					Mat query = Mat.zeros(1, q1.cols(), CvType.CV_32FC1);
					for (int i = 0; i < q1.cols(); i++) {
						query.put(0, i, q1.get(0, i)[0]);
					}
					
					/* If you use DCP algorithm, calculate the characteristic histogram using below method
					(You need to define Rin and Rex in functions first):*/

					//Mat dcp_image1 = algorithm.DCP1(src, 1, 4);
					//Mat dcp_image2 = algorithm.DCP2(src, 1, 4);
					//Mat q1 = histogram.spatial_histogram(dcp_image1, 0, (int)Math.pow(2, 8), _grid_x, _grid_y);
					//Mat q2 = histogram.spatial_histogram(dcp_image2, 0, (int)Math.pow(2, 8), _grid_x, _grid_y);
					//Mat query = Mat.zeros(1, q1.cols() + q2.cols(), CvType.CV_32FC1);
					//for (int index1 = 0; index1 < q1.cols(); index1++){
					//	query.put(0, index1, q1.get(0, index1)[0]);
					//}
					//for (int index2 = q1.cols(); index2 < q1.cols() + q2.cols(); index2++){
					//	query.put(0, index2, q1.get(0, index2 - q1.cols())[0]);
					//}
					
					double minDist = java.lang.Double.MAX_VALUE;
					int minClass = -1;
					for (int sampleIdx = 0; sampleIdx < _histograms.size(); sampleIdx++) {
						double dist = Imgproc.compareHist(_histograms.get(sampleIdx), query,Imgproc.HISTCMP_CHISQR_ALT);
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
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		System.out.println("Accuracy= "+count*100/(float)lines+"% ("+count+"/"+lines+") (classification)");
	}

	public int getlinenum(String fileName, char separator) {
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
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return j;
	}
}
