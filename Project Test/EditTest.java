package com.android.fra;

import com.android.fra.db.Face;
import org.litepal.LitePal;
import android.widget.EditText;

@RunWith(Parameterized.class)
public class EditTest extends ActivityInstrumentationTestCase<EditActivity> {

	private Instrumentation mInst = null;
	private List<Face> faceList = new ArrayList<>();

	public EditTest() {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		mActivity = getActivity();
		mInst = getInstrumentation();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void EditTestWithParameters() {
		EditActivity editActivity = new EditActivity(getContext());
		EditText editName = editActivity.findViewById(R.id.editText_name);
		EditText editPhone = editActivity.findViewById(R.id.editText_phone);
		EditText editDepartment = editActivity.findViewById(R.id.editText_department);

		/* Init local saved information */
		editName.setText("0");
		editPhone.setText("12345678900");
		editDepartment.setText("1");
		tap(R.id.save_edit);
		faceList = LitePal.findAll(Face.class);
		for (Face face : faceList) {
			assertEquals("0", face.getName());
			assertEquals("12345678900", face.getPhone());
			assertEquals("1", face.getDepartment());
		}

		/* Test edit information function */
		editName.setText("1");
		editPhone.setText("98765432100");
		editDepartment.setText("0");
		tap(R.id.save_edit);
		faceList = LitePal.findAll(Face.class);
		for (Face face : faceList) {
			assertEquals("1", face.getName());
			assertEquals("98765432100", face.getPhone());
			assertEquals("0", face.getDepartment());
		}
	}

	private void press(int keycode) {
		mInst.sendKeyDownUpSync(keycode);
	}

	private boolean tap(int id) {
		View view = mActivity.findViewById(id);
		if (view != null) {
			TouchUtils.clickView(this, view);
			return true;
		}
		return false;
	}

}