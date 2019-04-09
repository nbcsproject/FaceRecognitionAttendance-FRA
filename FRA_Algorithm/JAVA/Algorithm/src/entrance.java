
public class entrance {
	public static void main(String[] args) {
		traintest traintest = new traintest();
		int trainline = traintest.getlinenum("P:\\attrain.txt", ';');
		int predictline = traintest.getlinenum("P:\\atpredict.txt", ';');
		int _grid_x = 1, _grid_y = 1;
		traintest.train("P:\\attrain.txt", ';', trainline, _grid_x, _grid_y);
		traintest.predict("P:\\atpredict.txt", ';', predictline, _grid_x, _grid_y);
	}
}
