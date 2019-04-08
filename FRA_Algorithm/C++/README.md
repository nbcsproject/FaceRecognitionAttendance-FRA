# LBP&DCP(C++)

## algorithm.cpp

​    Encapsulating the LBP algorithm and the DCP algorithm, the texture features of the specified image can be obtained by calling any one of  `Mat LBP(Mat src)`, `Mat DCP1(Mat src, int Rin, int Rex)` or `Mat DCP2(Mat src, int Rin, int Rex)`.

## histogram.cpp

​    Calculating a one-dimensional histogram of a feature image.

## traintest.cpp

​    Train the training set according to the specified CSV file and test its recognition rate **(Note: When using the DCP algorithm, you need to modify the program to concatenate the histogram)**.

## main.cpp

​    Program entry **(Note: When using the DCP algorithm, it is recommended to set Rin to 1 and Rex to 4)**.

​    **(Note: The default grid_x and grid_y are taken as 1. If you want better recognition accurcy, set the value of grid_x and grid_y to 8. But note that this will bring more calculation work and program running time.)**

## Reference

* T. Ojala, M. Pietikäinen, and D. Harwood (1996), "A Comparative Study of Texture Measures with Classification Based on Feature Distributions", Pattern Recognition, vol. 29, pp. 51-59.

* Changxing Ding, Jonghyun Choi, Dacheng Tao, and Larry S. Davis, `Multi-Directional Multi-Level Dual-Cross Patterns for Robust Face Recognition', Vol.38, No.3, pp.518-531, IEEE TPAMI 2016.