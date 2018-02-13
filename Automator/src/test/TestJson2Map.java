package test;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJson2Map {

	public static void main(String[] args) {

		try {

			ObjectMapper mapper = new ObjectMapper();
			String json = ""
					+ "{"
					+ "\"application\":{\"name\":\"appl00\", \"type\":\"AUTO\"},"
					+ "\"task\":{\"cmd\":1, \"args\":[10, \"ciccio\", 27, 0]}"
					+ "}";
			
			

			Map<String, Object> map = null;

			// convert JSON string to Map
			map = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});

			System.out.println("ORIG := " + json);
			System.out.println("J2M  := " + map);
			System.out.println("M2J  := " + mapper.writeValueAsString(map));
			System.out.println("M2J  := " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));

		}
		catch (JsonGenerationException e) {
			e.printStackTrace();
		}
		catch (JsonMappingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
