/**
 * 
 */
package test;

import static org.junit.Assert.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

/**
 * @author Administrator
 *TODO
 */
public class Test4 {

	@Test
	public void test() {
		 JSONObject jsonObject = null;
		 JSONArray array = new JSONArray();
		 	for(int i = 0; i < 4;i++){
		 		jsonObject = new JSONObject();
		 		jsonObject.put("name", new String("ASDFGHJKL").charAt(i));
		 		//jsonObject.put("name", i);
		 		array.add(jsonObject);
		 	}
		 	System.out.println(array);
	}

}
