#include <iostream>

using namespace std;

extern int getlinenum(string fileName, char separator);
extern void train(string fileName, char separator, int lines, int _grid_x, int _grid_y);
extern void predict(string fileName, char separator, int lines, int _grid_x, int _grid_y);

void main(){
	int trainline = getlinenum("P:\\attrain.txt", ';');
	int predictline = getlinenum("P:\\atpredict.txt", ';');
	//int Rin = 1, Rex = 4;
	int _grid_x = 1, _grid_y = 1;
	train("P:\\attrain.txt", ';', trainline, _grid_x, _grid_y);
	predict("P:\\atpredict.txt", ';', predictline, _grid_x, _grid_y);
	system("pause");
}