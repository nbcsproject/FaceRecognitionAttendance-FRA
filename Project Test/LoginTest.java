package com.android.fra;

import java.util.HashMap;
import java.util.Map;

import android.test.AndroidTestCase;

@RunWith(Parameterized.class)
public class LoginTest extends AndroidTestCase {

	private Map<String, String> map;

	public LoginTest() {
		// TODO Auto-generated constructor stub
	}

	@Parameters.Parameters
	public static Collection primeNumbers() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("account0", "admin");
		map.put("password0", "123456");
		map.put("account1", "1");
		map.put("password1", "1");
		map.put("account2", "1");
		map.put("password2", "123456");
		return map;
	}

	@Test(expected = ParseException.class)
	public void LoginTestWithParameters() {
		LoginActivity loginActivity = new LoginActivity(getContext());
		boolean flag = loginActivity.saveSharePreference("msg", map);
		if (!flag) {
			assertThat(map, hasKey(¡°account0¡±));
			assertThat(map, hasKey(¡°password0¡±));
			assertThat(map, hasKey(¡°account1¡±));
			assertThat(map, hasKey(¡°password1¡±));
			assertThat(map, hasKey(¡°account2¡±));
			assertThat(map, hasKey(¡°password2¡±));
		} else {
			Map<String, String> map = loginActivity.getSharePreference("msg");
			assertThat("account0", equalTo("admin"));
			assertThat("password0", equalTo("123456"));
			assertThat("account1", equalTo("1"));
			assertThat("password1", equalTo("1"));
			assertThat("account1", equalTo("1"));
			assertThat("password1", equalTo("123456"));
		}
	}

}

package com.android.fra;

import com.android.fra.db.Face;
import org.litepal.LitePal;

@RunWith(Parameterized.class)
public class AddTest extends AndroidTestCase {

	public AddTest() {
		// TODO Auto-generated constructor stub
	}

	@Parameters.Parameters
	public static Collection primeNumbers() {
		Face face = new Face();
		Map<String, String> map = new HashMap<String, String>();
		face.setUid("100001");
		face.setName("Trey");
		face.setGender("male");
		face.setPhone("12345678900");
		face.setDepartment("Sony");
		return face;
	}

	@Test(expected = ParseException.class)
	public void AddTestWithParameters() {

		/* Test init */
		faceList = LitePal.where("uid = ?", "100001").find(Face.class);
		assertNotNull(faceList);

		/* Test delete information function */
		assertEquals("100001", face.getUid());
		assertEquals("Trey", face.getName());
		assertEquals("male", face.getGender());
		assertEquals("12345678900", face.getPhone());
		assertEquals("Sony", face.getDepartment());
	}

}