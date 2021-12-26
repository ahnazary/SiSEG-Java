import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.jena.rdf.model.RDFNode;

public class FirstLayerQuery extends FeatureVector{
	
	public FirstLayerQuery(String inputAddress, String modelAddress,String SVMMethod) throws IOException {
		super(inputAddress, modelAddress, SVMMethod);		
	}
	
	protected void firstLayerQuery(String method) throws FileNotFoundException {
		
		for (int i = 0; i < keylist.size(); i++) {				
			String word = keylist.get(i);
			System.out.println(word + "          (FirstLayer)");
			
			char keyWordArr[] = new char[word.length()];

			for (int j = 0; j < word.length(); j++) {
				keyWordArr[j] = word.charAt(j);
			}

			for (int j = 0; j < word.length() && isValidStr(word); j++) {
				for (int k = j + 2; k < word.length(); k++) {
					String morphemes = "";

					for (int z = j; z <= k; z++) {
						morphemes += keyWordArr[z];
					}
					
					if (j == 0 && k == word.length() - 1)
						doQuery(method, word ,morphemes, true);
					else
						doQuery(method, word ,morphemes, false);
				}
			}
		}
		
		for (int i = 0; i < ArrayValuesList.size(); i++) {				
			String word = ArrayValuesList.get(i);
			System.out.println(word+ "          (FirstLayer)");
			
			char keyWordArr[] = new char[word.length()];

			for (int j = 0; j < word.length(); j++) {
				keyWordArr[j] = word.charAt(j);
			}

			for (int j = 0; j < word.length() && isValidStr(word); j++) {
				for (int k = j + 2; k < word.length(); k++) {
					String morphemes = "";

					for (int z = j; z <= k; z++) {
						morphemes += keyWordArr[z];
					}
					
					if (j == 0 && k == word.length() - 1)
						doQuery(method, word ,morphemes, true);
					else
						doQuery(method, word ,morphemes, false);
				}
			}
		}

		
		for (int i = 0; i < JSONPairs.size(); i++) {
			
			String word = (String) JSONPairs.get(i).keySet().toArray()[0];
			System.out.println(word+ "          (FirstLayer)");
			
			char keyWordArr[] = new char[word.length()];

			for (int j = 0; j < word.length(); j++) {
				keyWordArr[j] = word.charAt(j);
			}

			for (int j = 0; j < word.length() && isValidStr(word); j++) {
				for (int k = j + 2; k < word.length(); k++) {
					String morphemes = "";

					for (int z = j; z <= k; z++) {
						morphemes += keyWordArr[z];
					}
					
					if (j == 0 && k == word.length() - 1)
						doQuery(method, word ,morphemes, true);
					else
						doQuery(method, word ,morphemes, false);
				}
			}
			
			for ( Map.Entry<String, Object> pair : ((Map<String, Object>) JSONPairs.get(i)).entrySet()) {
				
				if(pair.getValue() instanceof String) {
				
					word = (String) pair.getValue();
					System.out.println(pair.getValue()+ "          (FirstLayer)");
					
					char valueWordArr[] = new char[word.length()];
			
					for (int j = 0; j < word.length(); j++) {
						valueWordArr[j] = word.charAt(j);
					}
			
					for (int j = 0; j < word.length() && isValidStr(word); j++) {
						for (int k = j + 2; k < word.length(); k++) {
							String morphemes = "";
			
							for (int z = j; z <= k; z++) {
								morphemes += valueWordArr[z];
							}
							
							if (j == 0 && k == word.length() - 1)
								doQuery(method, word ,morphemes, true);
							else
								doQuery(method, word ,morphemes, false);
						}
					}
				}
			}
		}	
	}
	
	private void doQuery(String method, String word ,String morphemes, Boolean isFullWord) {
			
			String QueryFileExact = SPARQL_PREFIXES
					+ "SELECT ?subject \n" + "WHERE\n" 
					+ "{\n" 
					+ "{?subject rdfs:label ?object}"
					+ "FILTER (regex(?object, \"" + morphemes + "\", \"i\" ) || contains(str(?subject), '"+morphemes+"')) " 
					+ "}";
			
			String morphemesQueryFile = SPARQL_PREFIXES
					+ "SELECT ?subject \n" + "WHERE\n" + "{\n" + "{?subject rdfs:label ?object}"			
					//+ "FILTER regex(?object, \" " + morphemes + " \", \"i\" ) "
					+ "filter (contains(str(?object), ' "+morphemes+" ') || contains(str(?subject), ' "+morphemes+" '))"
					// +"filter (contains(str(?object), \""+word+"\") || contains(str(?subject),
					// \""+word+"\") || contains(str(?predicate), \""+word+"\"))"
					// +"FILTER (regex(?object, \""+word+"\", \"i\" ) || regex(?predicate,
					// \""+word+"\", \"i\" ) || regex(?subject, \""+word+"\", \"i\" )) "
					// +"filter (contains(str(?object), '"+morphemes+"'))"
					// +"FILTER regex(?object, \" "+morphemes+" \", \"i\" ) "
					// +"FILTER regex(?object, \"\\b"+morphemes+"\\b\" ) "
					// +"FILTER regex(?object,'"+morphemes+"') "
					+ "}";
			
			Map<RDFNode, float[]> singleWordMap = new HashMap<RDFNode, float[]>(); 
			if (isFullWord) {
	
				singleWordMap = resultsMap(QueryFileExact, model, 1f);
				System.out.println(morphemes);
	
				for (Entry<RDFNode, float[]> pair : singleWordMap.entrySet()) {
					
					if(method == "SVM" && SVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]}})) == 1) {
					System.out.println("    Approved by SVM");
					System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
									pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));					
					}
					
					else if (method == "SVM"){
						System.out.println("    Not Approved by SVM");
						System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
										pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));
					}
					else if(method == "WSVM" && WeightedSVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA)}})) == 1) {
						System.out.println("    Approved by Weighted SVM");
						System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
										pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));					
					}
					
					else if (method == "WSVM"){
						System.out.println("    Not Approved by Weighted SVM");
						System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
										pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));
					}
					else if (method != "WSVM" && method != "SVM") {
						System.out.println(" Please choose a valid method for Support Vector Machine. ");
					}
				}
				
			} 
			else {
	
				singleWordMap = resultsMap(morphemesQueryFile, model, surfaceSimilarity(word, morphemes));
				System.out.println(morphemes);
				for (Entry<RDFNode, float[]> pair : singleWordMap.entrySet()) {
					
					if(method == "SVM" && SVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]}})) == 1) {
					System.out.println("    Approved by SVM");
					System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
									pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));
					}
					else if(method == "SVM"){
						System.out.println("    Not Approved by SVM");
						System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
										pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));
					}
					else if(method == "WSVM" && WeightedSVM.classificationResult(MatrixUtils.createRealMatrix(new double[][] {{pair.getValue()[0] , pair.getValue()[1]*WeightedSVM.multiplier(TRAINING_DATA)}})) == 1) {
						System.out.println("    Approved by Weighted SVM" );
						System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
										pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));
					}
					else if(method == "WSVM"){
						System.out.println("    Not Approved by weighted SVM");
						System.out.println(String.format("     Key (URI) is: %s     Value (features Vector) is : %s",
										pair.getKey() + "\n", Arrays.toString(pair.getValue()) + "\n"));
					}
					else if (method != "WSVM" && method != "SVM") {
						System.out.println(" Please choose a valid method for Support Vector Machine. ");
					}
				}
			}
			
			Map<RDFNode, float[]> tempApprovedURIs = getBestNodes(method, singleWordMap);
			for (Entry<RDFNode, float[]> pair : tempApprovedURIs.entrySet()) {
				if(isClassNode(pair.getKey())) {
					approvedURIs.put(pair.getKey(), pair.getValue());
					allParentNodes.add(pair.getKey());
				}
				if(!isClassNode(pair.getKey())) {
					//ArrayList <RDFNode> temp = getClassNode(pair.getKey());
					Set <RDFNode> temp = getClassNode(pair.getKey());
					Iterator<RDFNode> it = temp.iterator();
					
					System.out.println(pair.getKey() + " is approved but not Class Node. ");
					System.out.println( "      Parent Nodes are :  " + temp);
					while( it.hasNext() ) {
						allParentNodes.add(it.next());
					}						
				}	
			}		
		}
	
	
	protected void generateFirstLayerResultsArr() {
		
		for (int i = 0; i < keylist.size(); i++) {
			String word = keylist.get(i);
			char wordArr[] = new char[word.length()];
	
			for (int j = 0; j < word.length(); j++) {
				wordArr[j] = word.charAt(j);
			}
	
			for (int j = 0; j < word.length(); j++) {
				for (int k = j + 2; k < word.length(); k++) {
					String morphemes = "";
	
					for (int z = j; z <= k; z++) {
						morphemes += wordArr[z];
					}
	
					String QueryFileExact = SPARQL_PREFIXES
							+ "SELECT ?subject \n" + "WHERE\n" 
							+ "{\n" 
							+ "{?subject rdfs:label ?object}"
							+ "FILTER (regex(?object, \"" + morphemes + "\", \"i\" ) || contains(str(?subject), '"+morphemes+"')) " 
							+ "}";
					
					String morphemesQueryFile = SPARQL_PREFIXES
							+ "SELECT ?subject \n" + "WHERE\n" + "{\n" + "{?subject rdfs:label ?object}"			
							+ "filter (contains(str(?object), ' "+morphemes+" ') || contains(str(?subject), ' "+morphemes+" '))"
							+ "}";
	
					if (j == 0 && k == word.length() - 1) {
						URIs = resultsArr(QueryFileExact, model);
					} else {
						URIs = resultsArr(morphemesQueryFile, model);
					}
				}
			}
		}
		
		for (int i = 0; i < ArrayValuesList.size(); i++) {
			String word = ArrayValuesList.get(i);
			char wordArr[] = new char[word.length()];
	
			for (int j = 0; j < word.length(); j++) {
				wordArr[j] = word.charAt(j);
			}
	
			for (int j = 0; j < word.length(); j++) {
				for (int k = j + 2; k < word.length(); k++) {
					String morphemes = "";
	
					for (int z = j; z <= k; z++) {
						morphemes += wordArr[z];
					}
	
					String QueryFileExact = SPARQL_PREFIXES
							+ "SELECT ?subject \n" + "WHERE\n" 
							+ "{\n" 
							+ "{?subject rdfs:label ?object}"
							+ "FILTER (regex(?object, \"" + morphemes + "\", \"i\" ) || contains(str(?subject), '"+morphemes+"')) " 
							+ "}";
					
					String morphemesQueryFile = SPARQL_PREFIXES
							+ "SELECT ?subject \n" + "WHERE\n" + "{\n" + "{?subject rdfs:label ?object}"			
							+ "filter (contains(str(?object), ' "+morphemes+" ') || contains(str(?subject), ' "+morphemes+" '))"
							+ "}";
	
					if (j == 0 && k == word.length() - 1) {
						URIs = resultsArr(QueryFileExact, model);
					} else {
						URIs = resultsArr(morphemesQueryFile, model);
					}
				}
			}
		}
		
		for (int i = 0; i < JSONPairs.size(); i++) {
			String word = (String) JSONPairs.get(i).keySet().toArray()[0];
			char wordArr[] = new char[word.length()];
	
			for (int j = 0; j < word.length(); j++) {
				wordArr[j] = word.charAt(j);
			}
	
			for (int j = 0; j < word.length(); j++) {
				for (int k = j + 2; k < word.length(); k++) {
					String morphemes = "";
	
					for (int z = j; z <= k; z++) {
						morphemes += wordArr[z];
					}
	
					String QueryFileExact = SPARQL_PREFIXES
							+ "SELECT ?subject \n" + "WHERE\n" 
							+ "{\n" 
							+ "{?subject rdfs:label ?object}"
							+ "FILTER (regex(?object, \"" + morphemes + "\", \"i\" ) || contains(str(?subject), '"+morphemes+"')) " 
							+ "}";
					
					String morphemesQueryFile = SPARQL_PREFIXES
							+ "SELECT ?subject \n" + "WHERE\n" + "{\n" + "{?subject rdfs:label ?object}"			
							+ "filter (contains(str(?object), ' "+morphemes+" ') || contains(str(?subject), ' "+morphemes+" '))"
							+ "}";
	
					if (j == 0 && k == word.length() - 1) {
						URIs = resultsArr(QueryFileExact, model);
					} else {
						URIs = resultsArr(morphemesQueryFile, model);
					}
				}
			}
			for ( Map.Entry<String, Object> pair : ((Map<String, Object>) JSONPairs.get(i)).entrySet()) {
				
				if(pair.getValue() instanceof String) {
				
					word = (String) pair.getValue();
					System.out.println(pair.getValue()+ "          (FirstLayer)");
					
					char valueWordArr[] = new char[word.length()];
			
					for (int j = 0; j < word.length(); j++) {
						valueWordArr[j] = word.charAt(j);
					}
			
					for (int j = 0; j < word.length() && isValidStr(word); j++) {
						for (int k = j + 2; k < word.length(); k++) {
							String morphemes = "";
			
							String QueryFileExact = SPARQL_PREFIXES
									+ "SELECT ?subject \n" + "WHERE\n" 
									+ "{\n" 
									+ "{?subject rdfs:label ?object}"
									+ "FILTER (regex(?object, \"" + morphemes + "\", \"i\" ) || contains(str(?subject), '"+morphemes+"')) " 
									+ "}";
							
							String morphemesQueryFile = SPARQL_PREFIXES
									+ "SELECT ?subject \n" + "WHERE\n" + "{\n" + "{?subject rdfs:label ?object}"			
									+ "filter (contains(str(?object), ' "+morphemes+" ') || contains(str(?subject), ' "+morphemes+" '))"
									+ "}";
			
							if (j == 0 && k == word.length() - 1) {
								URIs = resultsArr(QueryFileExact, model);
							} else {
								URIs = resultsArr(morphemesQueryFile, model);
							}
						}
					}
				}
			}
		}
	}
}