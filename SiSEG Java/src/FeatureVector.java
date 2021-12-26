import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;

public class FeatureVector {
	
	private String inputAddress;
	private String modelAddress;
	private String SVMMethod;
	private static String input;
	protected final Model model;
	
	protected static ArrayList<RDFNode> URIs = new ArrayList<RDFNode>();
	protected static ArrayList<HashMap<String, Object>> JSONPairs = new ArrayList<HashMap<String,Object>>();
	protected static ArrayList<String> keylist = new ArrayList<String>();
	protected static ArrayList<String> ArrayValuesList = new ArrayList<String>();
	protected static ArrayList<RDFNode> allParentNodes = new ArrayList<RDFNode>();
	
	protected static Map<RDFNode, float[]> approvedURIs = new HashMap<RDFNode, float[]>();
	
	protected ArrayList<String> bannedURIs = new ArrayList<String>();
	public static ArrayList<String> bannedStrs = new ArrayList<String>();
		
	static final double [][][] TRAINING_DATA = {{{0.5555, 0.04175} , {+1}}, 							
												{{0.4165, 0.06217} , {+1}},
												{{0.4154, 0.05565} , {+1}},
												{{0.5239, 0.06456} , {+1}},
												{{0.4894, 0.04456} , {+1}},
												{{0.3495, 0.04456} , {+1}},
												{{0.4136, 0.03411} , {+1}},
												{{0.7794, 0.05411} , {+1}},
												{{0.4469, 0.07411} , {+1}},
												{{0.2269, 0.09411} , {+1}},
												{{0.2269, 0.05411} , {+1}},
											
												{{0.1465, 0.01789} , {-1}},
												{{0.1469, 0.00568} , {-1}},
												{{0.0462, 0.01567} , {-1}},
												{{0.0465, 0.00176} , {-1}},
												{{0.2654, 0.03519} , {-1}},
												{{0.2465, 0.02299} , {-1}},
												{{0.2796, 0.02274} , {-1}},
												{{0.3945, 0.01274} , {-1}},
												{{0.1954, 0.03238} , {-1}},
												{{0.6954, 0.00828} , {-1}},
												};
	
	public final String SPARQL_PREFIXES = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX om: <http://www.wurvoc.org/vocabularies/om-1.8/> "
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
			+ "PREFIX time: <http://www.w3.org/2006/time#> "
			+ "PREFIX saref: <https://w3id.org/saref#>  "
			+ "PREFIX saref: <https://saref.etsi.org/core/>  "
			+ "PREFIX schema: <http://schema.org/>  "
			+ "PREFIX dcterms: <http://purl.org/dc/terms/>  "
			+ "PREFIX base: <http://def.isotc211.org/iso19150-2/2012/base#> "
			+ "PREFIX oboe-core: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#> "
			+ "PREFIX sosa: <http://www.w3.org/ns/sosa/> "
			+ "PREFIX sosa-om: <http://www.w3.org/ns/sosa/om#> "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
			+ "PREFIX dct: <http://purl.org/dc/terms/> \n"
			+ "PREFIX iso19156-gfi: <http://def.isotc211.org/iso19156/2011/GeneralFeatureInstance#> \n"
			+ "PREFIX iso19156-om: <http://def.isotc211.org/iso19156/2011/Observation#> \n"
			+ "PREFIX iso19156-sf: <http://def.isotc211.org/iso19156/2011/SamplingFeature#> \n"
			+ "PREFIX iso19156-sfs: <http://def.isotc211.org/iso19156/2011/SpatialSamplingFeature#> \n"
			+ "PREFIX iso19156-sp: <http://def.isotc211.org/iso19156/2011/Specimen#> \n"
			+ "PREFIX iso19156_gfi: <http://def.isotc211.org/iso19156/2011/GeneralFeatureInstance#> \n"
			+ "PREFIX iso19156_sf: <http://def.isotc211.org/iso19156/2011/SamplingFeature#> "
			+ "PREFIX xml: <http://www.w3.org/XML/1998/namespace> "
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
			+ "PREFIX terms: <http://purl.org/dc/terms/> "
			+ "PREFIX vann: <http://purl.org/vocab/vann/> "
			+ "PREFIX webprotege: <http://webprotege.stanford.edu/> "
			+ "Prefix : <https://w3id.org/saref#>";

	@SuppressWarnings("deprecation")
	FeatureVector(String inputAddress, String modelAddress,String SVMMethod) throws IOException {
		this.inputAddress = inputAddress;
		this.modelAddress = modelAddress;
		this.SVMMethod = SVMMethod;
		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		model = FileManager.get().loadModel(modelAddress); // model that query request is sent to

	}
	
	@SuppressWarnings("unused")
	public void start() throws IOException {
		
		
		
		ReadJSON rs = new ReadJSON(inputAddress);
		JSONPairs = rs.getJSONPairs();
		keylist = rs.getKeys();
		ArrayValuesList = rs.getArrayValues();
		
		final SVM svm = new SVM(TRAINING_DATA);
		final WeightedSVM wsvm = new WeightedSVM(TRAINING_DATA);
		
//		for(int i = 0; i < TRAINING_DATA.length; i++) {
//			System.out.println(TRAINING_DATA[i][0][0] + " " + TRAINING_DATA[i][0][1]*WeightedSVM.multiplier(TRAINING_DATA) + " " + TRAINING_DATA[i][1][0]);
//		}

		FirstLayerQuery firstLayerQuery = new FirstLayerQuery(inputAddress, modelAddress, SVMMethod); 
		SecondLayerQuery secondLayerQuery = new SecondLayerQuery(inputAddress, modelAddress, SVMMethod); 
		//DateTimeQuery dateTimeQuery = new DateTimeQuery(inputAddress, modelAddress, SVMMethod); 
		
		firstLayerQuery.generateFirstLayerResultsArr();
		secondLayerQuery.generateMorphemesResultsArr();
		//dateTimeQuery.generatedateTimeResultsArr();
		
		firstLayerQuery.firstLayerQuery(SVMMethod);
		secondLayerQuery.morphemesQuery(SVMMethod);
		//dateTimeQuery.dateTimeQuery(SVMMethod);
		
		System.out.println("-------------------------------------------------------------------------- \n");
		
		System.out.println(keylist + "\n");
		System.out.println(JSONPairs + "\n");
		System.out.println(ArrayValuesList + "\n");
		
		if(allParentNodes != null) {
			try {
			approvedURIs.put(mostCommonNode(allParentNodes), null);
			}
			catch(Exception e) {
				System.out.println("No related Node or URI found to annotate !");
			}
		}
		
		for (Entry<RDFNode, float[]> pair : approvedURIs.entrySet()) {
			System.out.println("Approved URI is: " +  pair.getKey() + "   (" + getPrefName(pair.getKey())+")" +" \n    Feature Vector is:  " + Arrays.toString(pair.getValue()));	
			System.out.println("        Is Class ? " + isClassNode(pair.getKey()));
			if(!isClassNode(pair.getKey()))
				System.out.println("Parent Class Nodes are : "+ getClassNode(pair.getKey()) +"\n");		
			System.out.println("\n");
		}	
	}
	
	
	
	// all of the URIs generated are stored in this ArrayList
	protected ArrayList<RDFNode> resultsArr(String inputQuery, Model model) {
		Query query = QueryFactory.create(inputQuery);

		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet resultsOutput = qExe.execSelect();

		for (; resultsOutput.hasNext();) {

			QuerySolution soln = resultsOutput.nextSolution();
			RDFNode subject = soln.get("subject");
			URIs.add(subject);
		}
		return URIs;
	}

	// this Map saves a specific URI with its corresponding FeatureVector
	protected Map<RDFNode, float[]> resultsMap(String inputQuery, Model model, float similarityFeature) {
		Query query = QueryFactory.create(inputQuery);

		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet resultsOutput = qExe.execSelect();

		Map<RDFNode, float[]> map = new HashMap<RDFNode, float[]>();

		while( resultsOutput.hasNext()) {

			QuerySolution soln = resultsOutput.nextSolution();
			RDFNode subject = soln.get("subject");

			float popularity = popularity(subject, URIs);

			float[] floatArray = { similarityFeature, popularity };
			map.put(subject, floatArray);
		}
		qExe.close();
		return map;
	}
	
	protected Map<RDFNode, float[]> getBestNodes(String method, Map<RDFNode, float[]> singleWordMap){
		double highestDistance = 0.0;
		Map<RDFNode, float[]> tempApprovedURIs = new HashMap<RDFNode, float[]>();
		for (Entry<RDFNode, float[]> pair : singleWordMap.entrySet()) {
			if(isValidURI(pair.getKey())) {
				if(method == "WSVM" && WeightedSVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA)) > highestDistance && 
						WeightedSVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA)}})) == 1)
				{
					highestDistance = WeightedSVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA));
					tempApprovedURIs.clear();
					tempApprovedURIs.put(pair.getKey(), pair.getValue());
				}	
				else if(method == "WSVM" && WeightedSVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA)) == highestDistance && 
						WeightedSVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA)}})) == 1)
				{
					highestDistance = WeightedSVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA));
					tempApprovedURIs.put(pair.getKey(), pair.getValue());
				}	
				else if(method == "SVM" && SVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]) > highestDistance && 
						SVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]}})) == 1)
				{
					highestDistance = WeightedSVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA));
					tempApprovedURIs.clear();
					tempApprovedURIs.put(pair.getKey(), pair.getValue());
				}	
				else if(method == "SVM" && SVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA)) == highestDistance && 
						SVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]}})) == 1)
				{
					highestDistance = WeightedSVM.distanceToLine(pair.getValue()[0],pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA));
					tempApprovedURIs.put(pair.getKey(), pair.getValue());
				}	
			}
		}
		return tempApprovedURIs;
	}

	
	public static RDFNode mostCommonNode(ArrayList<RDFNode> input) {
	
	    Map<RDFNode, Integer> map = new HashMap<>();

	    for (RDFNode t : input) {
	        Integer val = map.get(t);
	        map.put(t, val == null ? 1 : val + 1);
	    }
	    for (Entry<RDFNode, Integer> e : map.entrySet()) {
	        System.out.println(e.getKey() + " = " + e.getValue() + "\n");
	    }
	    
	    System.out.println("-------------------------------------------------------------------------- \n");

	    Entry<RDFNode, Integer> max = null;

	    for (Entry<RDFNode, Integer> e : map.entrySet()) {
	        if (max == null || e.getValue() > max.getValue())
	            max = e;
	    }
	    
	    return max.getKey();
	}
	
	static RDFNode mostFrequent(ArrayList<RDFNode> input)
    {
         
        // Insert all elements in hash
        Map<RDFNode, Integer> hp = new HashMap<RDFNode, Integer>();
         
        for(int i = 0; i < input.size(); i++)
        {
            RDFNode key = input.get(i);
            if(hp.containsKey(key))
            {
                int freq = hp.get(key);
                freq++;
                hp.put(key, freq);
            }
            else
            {
                hp.put(key, 1);
            }
        }
         
        // find max frequency.
        int max_count = 0;
        RDFNode res = null; 
        
        for(Entry<RDFNode, Integer> val : hp.entrySet())
        {
            if (max_count < val.getValue())
            {
                res = val.getKey();
                max_count = val.getValue();
            }
        }
         
        return res;
    }
	
	protected float surfaceSimilarity(String word, String morphemes) {

		float z = ((float) morphemes.length()) / ((float) word.length());
		return z;

	}

	private float popularity(RDFNode URI, ArrayList<RDFNode> URIs) {

		int repetition = 0;
		for (int i = 0; i < URIs.size(); i++) {
			if (URI.equals(URIs.get(i))) {
				repetition++;
			}
		}

		float result;
		result = ((float) repetition) / ((float) URIs.size());
		return result;
	}

	@SuppressWarnings("unused")
	private static void appendStrToFile(String filePath, String str) {
		try (FileWriter fw = new FileWriter("Output", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			out.println(str);

		} catch (IOException e) {
			System.out.println("Exception Occurred" + e);
		}
	}

	
	
	protected boolean isClassNode(RDFNode inputNode) {
		
		String inputStr = inputNode.toString();
		
		String queryStr = SPARQL_PREFIXES
				+ "SELECT ?object \n" + "WHERE\n" + "{\n {" 
				+ "?subject rdf:type ?object}"
				+ "  FILTER (?subject = <"+ inputStr+ ">) "
				+ "}";
		
		Query query = QueryFactory.create(queryStr);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet resultsOutput = qExe.execSelect();
		
		for (; resultsOutput.hasNext();) {
			QuerySolution soln = resultsOutput.nextSolution();
			RDFNode object = soln.get("object");
			if(object.toString().equals("http://www.w3.org/2002/07/owl#Class")) {
				return true;
			}
		}
		return false;
	}
	
	private String getPrefName(RDFNode inputNode) {
		
		String result = "";
		String inputStr = inputNode.toString();
		
		String queryStr = SPARQL_PREFIXES	
				+ "select ?subject (group_concat(?prefixedName ; separator = \"\") as ?prefName) where {\n"
				+ "  values (?prefix ?ns) { \n"
				+ "    ( \":\" <https://w3id.org/saref#> )\n"
				+ "    ( \"saref:\" <https://w3id.org/saref#> )\n"
				+ "    ( \"saref:\" <https://saref.etsi.org/core/> )\n"
				+ "    ( \"xsd:\" <http://www.w3.org/2001/XMLSchema#> )\n"
				+ "    ( \"rdfs:\" <http://www.w3.org/2000/01/rdf-schema#> )\n"
				+ "    ( \"owl:\" <http://www.w3.org/2002/07/owl#> )\n"
				+ "    ( \"foaf:\" <http://xmlns.com/foaf/0.1/> )\n"
				+ "    ( \"time:\" <http://www.w3.org/2006/time#> )\n"
				+ "    ( \"schema:\" <http://schema.org/> )\n"
				+ "    ( \"dcterms:\" <http://purl.org/dc/terms/> )\n"
				+ "    ( \"om:\" <http://www.wurvoc.org/vocabularies/om-1.8/> )\n"
				+ "    ( \"rdf:\" <http://www.w3.org/1999/02/22-rdf-syntax-ns#> )\n"
				+ "    ( \"dc:\" <http://purl.org/dc/elements/1.1/> )\n"
				+ "    ( \"dct:\" <http://purl.org/dc/terms/> )\n"
				+ "    ( \"iso19156-gfi:\" <http://def.isotc211.org/iso19156/2011/GeneralFeatureInstance#> )\n"
				+ "    ( \"iso19156-om:\" <http://def.isotc211.org/iso19156/2011/Observation#> )\n"
				+ "    ( \"iso19156-sf:\" <http://def.isotc211.org/iso19156/2011/SamplingFeature#> )\n"
				+ "    ( \"iso19156-sfs:\" <http://def.isotc211.org/iso19156/2011/SpatialSamplingFeature#> )\n"
				+ "    ( \"iso19156-sp:\" <http://def.isotc211.org/iso19156/2011/Specimen#> )\n"
				+ "    ( \"iso19156_gfi:\" <http://def.isotc211.org/iso19156/2011/GeneralFeatureInstance#> )\n"
				+ "    ( \"iso19156_sf:\" <http://def.isotc211.org/iso19156/2011/SamplingFeature#> )\n"
				+ "    ( \"sosa-om:\" <http://www.w3.org/ns/sosa/om#> )\n"
				+ "    ( \"oboe-core:\" <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#> )\n"
				+ "    ( \"webprotege:\" <http://webprotege.stanford.edu/> )\n"
				+ "    ( \"terms:\" <http://purl.org/dc/terms/> )\n"
				+ "    ( \"skos:\" <http://www.w3.org/2004/02/skos/core#> )\n"
				+ "    ( \"vann:\" <http://purl.org/vocab/vann/> )\n"
				+ "    ( \"xml:\" <http://www.w3.org/XML/1998/namespace> )\n"
				
				
				+ "  }\n"
				+ "  ?subject ?predicate ?object .\n"
				+ "  FILTER (?subject = <"+ inputStr+ ">) "
				+ "  bind( if( strStarts( str(?subject), str(?ns) ),\n"
				+ "            concat( ?prefix, strafter( str(?subject), str(?ns) )),\n"
				+ "            \"\" ) \n"
				+ "        as ?prefixedName )\n"
				+ "}\n"
				+ "group by ?subject\n"
				+ "order by ?subject";
			
		Query query = QueryFactory.create(queryStr);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet resultsOutput = qExe.execSelect();
		
		for (; resultsOutput.hasNext();) {
			QuerySolution soln = resultsOutput.nextSolution();
			RDFNode prefName = soln.get("prefName");
			
			result = maxSubString(prefName.toString());	
		}
		qExe.close();
		return result;
	}

	protected Set<RDFNode> getClassNode(RDFNode inputNode) {
			
		String prefNodeStr = getPrefName(inputNode);
		//ArrayList<RDFNode> result = new ArrayList<RDFNode>();
		Set<RDFNode> result = new HashSet<RDFNode>();
		
		String queryStr = SPARQL_PREFIXES
				+ "SELECT ?subject \n" + "WHERE\n" + "{\n" + "{"
				+ "?subject ?predicate ?object}"
				+ "  FILTER (?object ="+ prefNodeStr + ") "
				//+ "?subject ?a " + prefNodeStr + "}"
				+ "}";
		
		String queryStrBlankNode = SPARQL_PREFIXES
				+ "SELECT ?subject \n" + "WHERE\n" + "{\n" + "{"
				//+ "?subject ?a [?b "+prefNodeStr+"]}"
				+ "?subject ?a [?b ?object]}"
				+ "  FILTER (?object ="+ prefNodeStr + ") "
				+ "}";
		
		System.out.println(prefNodeStr);
		Query query = QueryFactory.create(queryStr);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet resultsOutput = qExe.execSelect();
		
		while( resultsOutput.hasNext()) {
			QuerySolution soln = resultsOutput.nextSolution();
			RDFNode subject = soln.get("subject");
			
			
			if(isClassNode(subject) && !subject.isAnon())
				result.add(subject);
			
			else if (subject.isAnon()) {	
				
				Query queryAnon = QueryFactory.create(queryStrBlankNode);
				QueryExecution qExeAnon = QueryExecutionFactory.create(queryAnon, model);
				ResultSet resultsOutputAnon = qExeAnon.execSelect();
				
				while( resultsOutputAnon.hasNext()) {
					
					QuerySolution soln2 = resultsOutputAnon.nextSolution();
					RDFNode subject2 = soln2.get("subject");
					result.add(subject2);					
				}
				qExeAnon.close();
			}
		}
		qExe.close();
		return result;
		
	}

	protected boolean isValidURI(RDFNode inputRDFNode) {	
		
		//URIS which we want to be excluded from the result are added here
		bannedURIs.add("https://w3id.org/saref"); 
		bannedURIs.add("http://www.w3.org/ns/sosa/om"); 
				
		for(int i = 0; i < bannedURIs.size(); i++) {
			if(bannedURIs.get(i).equals(inputRDFNode.toString())) 
				return false;	
		}	
		
		return true;
	}
	
	protected static boolean isValidStr(String input) {
		bannedStrs.add("type"); 
		bannedStrs.add("unit");
		bannedStrs.add("measure");
		bannedStrs.add("relation");
		bannedStrs.add("value");
		bannedStrs.add("relationship");
		bannedStrs.add("property");
		bannedStrs.add("the");
		bannedStrs.add("has");
		bannedStrs.add("and");
		bannedStrs.add("add");
		for(int i = 0; i < bannedStrs.size(); i++) {
			if (input.equalsIgnoreCase(bannedStrs.get(i)))
				return false;
		}
		return true;
	}
	
	protected static boolean isValidDate(String inDate) {

		boolean result = false;
		;
		ArrayList<String> validFormats = new ArrayList<String>();
		validFormats.add("HH:mm:ss");
		validFormats.add("dd-MM-yyyy HH:mm:ss:ms");
		validFormats.add("dd-MM-yyyy HH:mm:ss");
		validFormats.add("dd-MM-yyyy HH:mm");
		validFormats.add("dd-MM-yyyy");

		for (int i = 0; i < validFormats.size(); i++) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(validFormats.get(i));
			dateFormat.setLenient(false);
			try {
				dateFormat.parse(inDate.trim());
			} catch (ParseException pe) {

				if (i == validFormats.size())
					result = false;

				else
					continue;
			}
			result = true;
			break;
		}
		return result;
	}
	
	private static String maxSubString(String inputString){
        int maxSubStringLength = calculateMaxSubStringLength( inputString);
        return inputString.substring(0,maxSubStringLength);
    }

    private static int calculateMaxSubStringLength(String inputString) {
        int result = 0;
        for(int i= inputString.length();  i>0 ; i--){
            if(inputString.length() < i *2)
                continue;
            if (inputString.substring(0,i).equals(inputString.substring(i , 2*i)))
            result = i;
        }
        return result;
    }
    
    
	
	public ArrayList<RDFNode> getURIs() {
		return URIs;	
	}

	public static String getInput() {
		return input;
	}

	public static void setInput(String input) {
		FeatureVector.input = input;
	}
	public Map<RDFNode, float[]> getApprovedURIs(){
		return approvedURIs;
	}
	public String getInputAddress() {
		return inputAddress;
	}
}
