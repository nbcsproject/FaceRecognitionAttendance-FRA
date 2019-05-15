package com.android.fra;

import android.test.AndroidTestCase;

@RunWith(Parameterized.class)
public class GetMethodTest extends AndroidTestCase {
	private Face face;

	public GetMethodTest() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		face = new Face("1000001", "100001", "Nohara", "male", "12345678900", "Hiroshi@gmail.com", "Futaba Corporation",
				"Director", true, "2019.05.06 19:30:31", "1", "2019.05.06 19:29:31");
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	public void testGetPid() {
		assertEquals("1000001", Face.getPid());
	}

	public void testGetUid() {
		assertEquals("100001", Face.getUid());
	}

	public void testGetName() {
		assertEquals("Nohara", testStudent.getName());
	}

	public void testGetGender() {
		assertEquals("male", Face.getGender());
	}

	public void testGetPhone() {
		assertEquals("12345678900", Face.getPhone());
	}

	public void testGetEmail() {
		assertEquals("Hiroshi@gmail.com", Face.getEmail());
	}

	public void testGetDepartment() {
		assertEquals("Futaba Corporation", Face.getDepartment());
	}

	public void testGetPost() {
		assertEquals("Director", Face.getPost());
	}

	public void testGetValid() {
		assertTrue(Face.getValid());
	}

	public void testGetModTime() {
		assertEquals("2019.05.06 19:30:31", Face.getModeTime());
	}

	public void testGetCheckStatus() {
		assertEquals("1", Face.getCheckStatus());
	}

	public void testGetCurrentChecktime() {
		assertEquals("2019.05.06 19:29:31", Face.getCurrentChecktime());
	}

}