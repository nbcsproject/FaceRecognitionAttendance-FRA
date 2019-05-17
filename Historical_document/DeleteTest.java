package com.android.fra;

import com.android.fra.db.Face;
import org.litepal.LitePal;

@RunWith(Parameterized.class)
public class DeleteTest extends ActivityInstrumentationTestCase<DeleteActivity> {

	private Instrumentation mInst = null;
	private List<Face> faceList = new ArrayList<>();

	public DeleteTest() {
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
	public void DeleteTestWithParameters() {
		DeleteActivity deleteActivity = new DeleteActivity(getContext());

		/* Init local saved information */
		Face face = new Face();
		face.setUid("110000");
		face.setName("Jane");
		face.setPhone("12345678900");
		face.setDepartment("Futaba");
		face.save();

		/* Test init */
		faceList = LitePal.where("uid = ?", "110000").find(Face.class);
		assertNotNull(faceList);

		/* Test delete information function */
		currentUid = "110000";
		tap(R.id.delete_button);
		faceList = LitePal.where("uid = ?", "110000").find(Face.class);
		assertNull(faceList);
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
