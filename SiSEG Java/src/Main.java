import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class Main {
	
	public static void main(String[] args) throws IOException, ParseException {
	
		long startTime = System.nanoTime();
		
		FeatureVector FV = new FeatureVector("/home/amirhossein/Documents/GitHub/semantic-broker/Broker/Samples/Floor-example.json","Sargon.ttl", "WSVM"); // 3rd field should be either "WSVM" or "SVM"	
		FV.start();
		System.out.println("Total number of processed Nodes : " + FV.getURIs().size());
		
		JSONLDGenerator Test = new JSONLDGenerator(FV.getInputAddress(), "JSON-LD", FV.getApprovedURIs());
		File file = new File(Test.getJSONLDFilePath()); 
		file.delete();
		Test.Start();
		
		long endTime = System.nanoTime();
		System.out.println("Total Runtime : " + (endTime - startTime)/1000000 + " milliseconds");
	}			
}