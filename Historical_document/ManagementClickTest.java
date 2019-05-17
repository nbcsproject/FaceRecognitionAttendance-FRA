package com.android.fra;

import android.test.AndroidTestCase;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class ManagementClickTest extends ActivityInstrumentationTestCase2<SpinnerActivity> {

	private static final int ADAPTER_COUNT = 10;
	private static final int TEST_POSITION = 3;
	private static final int INITIAL_POSITION = 0;

	private ManagementActivity mActivity;

	private String mSelection;
	private int mPos;

	private Spinner mSpinner;
	private SpinnerAdapter mAdapterData;

	public ManagementTest() {
		super(ManagementActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);

		mActivity = getActivity();

		mSpinner = (Spinner) mActivity.findViewById(R.id.root_view);
		mAdapterData = mSpinner.getAdapter();
	}

	public void testPreconditions() {
		assertTrue(mSpinner.getOnItemSelectedListener() != null);
		assertTrue(mAdapterData != null);
		assertEquals(mAdapterData.getCount(), ADAPTER_COUNT);
	}

	public void testSpinnerUI() {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				mSpinner.requestFocus();
				mSpinner.setSelection(INITIAL_POSITION);
			}
		});
		this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		for (int i = 0; i < TEST_POSITION; i++) {
			this.sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		}
		this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);

		mPos = mSpinner.getSelectedItemPosition();
		mSelection = (String) mSpinner.getItemAtPosition(mPos);

		assertEquals(resultText, mSelection);
	}
}