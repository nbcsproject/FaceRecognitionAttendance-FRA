package com.android.fra;

import com.android.fra.db.Face;
import org.litepal.LitePal;
import android.widget.EditText;

@RunWith(Parameterized.class)
public class RegisterTest extends ActivityInstrumentationTestCase<RegisterActivity> {

	private Instrumentation mInst = null;
	private List<Face> faceList = new ArrayList<>();

	public RegisterTest() {
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
	public void RegisterTestWithParameters() {
		RegisterActivity registerActivity = new RegisterActivity(getContext());
		EditText registerName = registerActivity.findViewById(R.id.registerText_name);
		EditText registerPhone = registerActivity.findViewById(R.id.registerText_phone);
		EditText registerDepartment = registerActivity.findViewById(R.id.registerText_department);

		/* Test local cache function */
		registerName.setText("Wanya");
		registerPhone.setText("19919919919");
		registerDepartment.setText("Sony");
		tap(R.id.register_button);
		faceList = LitePal.findAll(Face.class);
		for (Face face : faceList) {
			assertEquals("Wanya", face.getName());
			assertEquals("19919919919", face.getPhone());
			assertEquals("Sony", face.getDepartment());
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