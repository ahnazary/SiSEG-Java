import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.RDFNode;

public class JSONLDGenerator {
	
	String JSONContent; 
	String JSONFilepath;
	static String JSONLDFilePath;
	Map<RDFNode, float[]> approvedURIs = new HashMap<RDFNode, float[]>();
	
	@SuppressWarnings("static-access")
	public JSONLDGenerator(String JSONFilepath, String JSONLDFilePath, Map<RDFNode, float[]> approvedURIs){
		
		this.JSONLDFilePath = JSONLDFilePath;
		this.approvedURIs = approvedURIs;
		this.JSONFilepath =JSONFilepath;
		
	}
	
	public void Start() throws IOException {
		JSONContent = readFile(JSONFilepath);
		generateJSONLD(JSONContent, approvedURIs);
		
	}
	
	
	private void generateJSONLD(String JSONContent, Map<RDFNode, float[]> approvedURIs) {
		int i = 0;
		int j = 0;
		String tempStr;
		Scanner scanner = new Scanner(JSONContent);
		while (scanner.hasNextLine()) {
			i++;
			String line = scanner.nextLine();
			
			if(i < countLines(JSONContent)-1) {
				appendStrToFile(JSONLDFilePath, line);
			}
			if(i == countLines(JSONContent)-1) {
				tempStr = line + ',';
				appendStrToFile(JSONLDFilePath, tempStr);
			}


			if(i == countLines(JSONContent)) {
				appendStrToFile(JSONLDFilePath, "  \"@context\": [");
				
				for (Entry<RDFNode, float[]> pair : approvedURIs.entrySet()) {
					
					if(j < approvedURIs.size()-1) {
						tempStr = "    " + '"' + pair.getKey().toString() + '"' + ',';
						appendStrToFile(JSONLDFilePath, tempStr);
					}
					
					
					else if(j == approvedURIs.size()-1 ) {
						tempStr = "    " +'"' + pair.getKey().toString() +'"'+ "\n  ]"+"\n}";
						appendStrToFile(JSONLDFilePath, tempStr);

					}
					j++;
				}	
				if(approvedURIs.size() == 0) {
					tempStr = "    " + "  ]"+"\n}";
					appendStrToFile(JSONLDFilePath, tempStr);
				}
			}
		}
		scanner.close();
	}
	
	private static int countLines(String str){
		   String[] lines = str.split("\r\n|\r|\n");
		   return  lines.length;
		}
	
	private String readFile(String filePath) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader (filePath));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
	
	private static void appendStrToFile(String filePath, String str) {
		try (FileWriter fw = new FileWriter(JSONLDFilePath, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			out.println(str);

		} catch (IOException e) {
			System.out.println("Exception Occurred" + e);
		}
	}
	public String getJSONLDFilePath() {
		return JSONLDFilePath;
	}
}