package Heuristic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MDAHeuristic {
	
	public static Document mainDoc = null;
	public static int HEURISTIC = 0;
	public static final int HEURISTIC_MDA = 0;
	public static final int SPLIT_NAIVE = 1;
	public static final int MERGE_NAIVE = 2;
	public static final int HEURISTIC_NAIVE = 3;
	public static final int HEURISTIC_MERGE_SPLIT_NAIVE =4 ;
	public static final int HEURISTIC_SPLIT_MERGE_NAIVE = 5;
	public static final int SPLIT_HEURISTIC = 6;
	public static List<String> ReferencesList = new ArrayList<String> ();
	public static List<String> KeysList = new ArrayList<String>();
	public static List<ArrayList<String>> queriesList = new ArrayList<ArrayList<String>>();
	public static List<ArrayList<String>> queriesListTemp = new ArrayList<ArrayList<String>>();
	public static final ArrayList<String> SIGNATURES = new ArrayList<String>();
	//public static final String FOLDER = "Count";
	
    public static FileWriter writer;
    
	public static void main(String[] args) {
		
		try {
			for(int h=2; h<=2; h++) {
				HEURISTIC = h;
				String strategy = null;
				if(h==0) {
					strategy = "HEURISTIC_MDA";
				}else if(h==1) {
					strategy = "SPLIT_NAIVE";
				}else if(h==2) {
					strategy = "MERGE_NAIVE";
				}else if(h==3) {
					strategy = "HEURISTIC_NAIVE";
				}else if(h==4) {
					strategy = "HEURISTIC_MERGE_SPLIT_NAIVE";
				}else if(h==5) {
					strategy = "HEURISTIC_SPLIT_MERGE_NAIVE";
				}else {
					strategy = "SPLIT_HEURISTIC";
				}
				String nbRow = "r1";
				for(int i=1; i<=4; i++) {
					String nbCpt = "c" + i;
					for(int j=2; j<=4; j++) {
						queriesListTemp.clear();
						String nbKey = "k" + j;
						String pathname = "output/"+ nbCpt + "/" + nbRow + "/" + nbKey + "/";
						String filename = nbCpt + "_" + nbRow + "_" + nbKey;
						File xmldoc = new File(pathname+filename+".xml");
						DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuild = dbFact.newDocumentBuilder();
						Document doc = dBuild.parse(xmldoc);
						Document doc2 = dBuild.newDocument();
						readQueries(pathname,filename);
						//System.out.println(queriesList);
			    		writer = new FileWriter("ModelsHeuristic/Common Models/signatures.txt", false);

						//create DataStore
						Element DataStore2 = doc2.createElement(doc.getDocumentElement().getNodeName());
						doc2.appendChild(DataStore2);
						
						//create DataStore Name
						Attr dsName2 = doc2.createAttribute("dsName");
						String attrDS2 = doc.getDocumentElement().getAttribute("dsName");
				        dsName2.setValue(attrDS2);
				        DataStore2.setAttributeNode(dsName2); 
				        
				        //create namespace
				        Attr namespace2 = doc2.createAttribute("xmlns:xsi");
						String attrNS2 = doc.getDocumentElement().getAttribute("xmlns:xsi");
						namespace2.setValue(attrNS2);
				        DataStore2.setAttributeNode(namespace2);
						
				        mainDoc = doc2;
				        
				        String path = "ModelsHeuristic/Common Models/";
					    
					    NodeList references = doc.getElementsByTagName("reference");
					    for(int r=0; r<references.getLength(); r++) {
					    	Node reference = references.item(r);
					    	ReferencesList.add(((Element)reference).getAttribute("rName"));
					    }
					    Collections.sort(ReferencesList);
					   // System.out.println("main test");
					    heuristic(doc, path, -1);
					    writer.flush();
						writer.close();
						
					    WriteToCSVFile(filename, strategy);
					}
				}
			}
			
		}catch(Exception e) {e.printStackTrace();}

	}
	
	public static void heuristic(Document doc, String path, int lastRef) {
		//System.out.println(path);
		switch(HEURISTIC) {
		case HEURISTIC_MDA:MergeSplitHeuristic(doc,path, lastRef);break;
		case SPLIT_NAIVE:SplitNaive(doc,path, lastRef);break;
		case MERGE_NAIVE:MergeNaive(doc,path, lastRef);break;
		case HEURISTIC_NAIVE:MergeSplitNaive(doc,path, lastRef);break;
		case HEURISTIC_MERGE_SPLIT_NAIVE:MergeStrategy(doc, path, lastRef);break;
		case HEURISTIC_SPLIT_MERGE_NAIVE:SplitStrategy(doc, path, lastRef);break;
		case SPLIT_HEURISTIC:heuristicSplit(doc,path, lastRef);break;
		//case HEURISTIC_MDA:heuristicMDA(doc,path);break;
		}
	}
	
	public static void MergeSplitNaive(Document doc, String path, int lastRef) {
		MergeNaive(doc,path, lastRef);
		SplitNaive(doc,path, lastRef);
	}
	
	public static void MergeSplitHeuristic(Document doc, String path, int lastRef) {
		heuristicMerge(doc, path, lastRef);
		heuristicSplit(doc, path, lastRef);
	}
	
	
	public static void MergeStrategy(Document doc, String path, int lastRef) {
		heuristicMerge(doc, path, lastRef);
		SplitNaive(doc, path, lastRef);
	}
	
	public static void SplitStrategy(Document doc, String path, int lastRef) {
		MergeNaive(doc, path, lastRef);
		heuristicSplit(doc, path, lastRef);
	}
	
	public static void MergeNaive(Document doc, String path, int lastRef) {
		
		try {
			NodeList references2 = doc.getElementsByTagName("reference");
	    	        
	    	for(int i=0; i<references2.getLength(); i++) {
		    	
		    	Node ref = references2.item(i);
				String source = ((Element) ref).getAttribute("source");
				String target = ((Element) ref).getAttribute("target");
				
				MergeNaive(source,target, doc, ref, path, lastRef);
				MergeNaive(target, source, doc, ref, path, lastRef);
				//System.out.println("function test");
		    }
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public static void SplitNaive(Document doc, String path, int lastRef) {
		
		try {
			NodeList rows = doc.getElementsByTagName("row");
			for(int r=0; r<rows.getLength(); r++) {
		    	Node row = rows.item(r);
		    	if(row.getParentNode().getNodeName().compareTo("concepts") == 0) {
		    		NodeList keyvalues = ((Element)row).getElementsByTagName("key_value");
			    	int count = 0;
			    	for(int kv=0; kv<keyvalues.getLength(); kv++) {
			    		Node keyvalue = keyvalues.item(kv);
			    		if(((Element)keyvalue.getParentNode()).getAttribute("rName").compareTo(((Element)row).getAttribute("rName")) == 0) {
					    	count = count+1;
			    		}
			    		if(count>=3) {
				    		SplitNaive(doc, row, path, lastRef);
				    	}
			    	}
		    	}
		    }
		}catch(Exception e) {e.printStackTrace();}
	}

	public static void heuristicMerge(Document doc, String path, int lastRef) {
		
		try {
			NodeList references = doc.getElementsByTagName("reference");
			for(int i=0; i<references.getLength(); i++) {
		    	Node ref = references.item(i);
				String source = ((Element) ref).getAttribute("source");
				String target = ((Element) ref).getAttribute("target");
				//String sourcePK = getPK(source, doc);
				//String targetPK = getPK(target, doc);
				int r = ReferencesList.indexOf(((Element)ref).getAttribute("rName"));
				//System.out.println(KeysList + " " + r + " " + lastRef + " " + sourcePK + " " + targetPK );
				if(r>lastRef && (comparator (KeysList, source) && comparator (KeysList, target))) {
					//System.out.println("source: " + sourcePK);
					Merge(source,target, doc, ref, path, r);
					Merge(target, source, doc, ref, path, r);
				}
				
		    }
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public static boolean comparator (List<String> keys, String comp){
		for(String s : keys)
			if (comp.contains(s))
				return true;
		return false;
	}
	
	public static String getPK(String target, Document doc) {
		String keyPK = null;
		NodeList keys = doc.getElementsByTagName("key");
    	for(int i=0; i<keys.getLength(); i++) {
    		Node key = keys.item(i);
    		if((((Element)key.getParentNode()).getAttribute("kvName")).compareTo(target) == 0) {
    			NodeList keys1 = ((Element)key.getParentNode().getParentNode()).getElementsByTagName("key");
    			for(int j=0; j<keys1.getLength(); j++) {
    				Node key1 = keys1.item(j);
    				if((((Element)key1.getParentNode().getParentNode()).getAttribute("rName").compareTo(((Element)key.getParentNode().getParentNode()).getAttribute("rName")) == 0) && (((Element)key1).getAttribute("kType").compareTo("Oid") == 0) ) {
    					keyPK = ((Element)key1).getAttribute("kName");
    				}
    			}
    		}
    	}
    	return keyPK; 
	}


	public static void Merge(String source, String target, Document doc, Node ref, String path, int lastRef) {
		try {
			Document doc2 = createDocument();
			NodeList references = doc.getElementsByTagName("reference");
			NodeList concepts = doc.getElementsByTagName("concepts");
			//NodeList keyvalues1 = doc.getElementsByTagName("key_value");
			String filename = "Merge_"+source+"_"+target;
			String paths = path+filename+"/";
			
			for(int c=0; c<concepts.getLength(); c++) {
				Node concept = concepts.item(c);
				//create concept
				Element cpt1 = doc2.createElement(concept.getNodeName());
				doc2.getDocumentElement().appendChild(cpt1);
				
				//create cName attribute
				Attr cName = doc2.createAttribute("cName");
		        cName.setValue(((Element) concept).getAttribute("cName"));
		        cpt1.setAttributeNode(cName);
		        
		        Copy(doc2, ((Element)concept), cpt1);
			}
			
			for(int i=0; i<references.getLength(); i++) {
				Node reference = references.item(i);
				String rName1 = ((Element)reference).getAttribute("rName");
				String rName2 = ((Element)ref).getAttribute("rName");
				if(!rName1.equals(rName2)){
					
					//create reference
					Element ref1 = doc2.createElement(reference.getNodeName());
					doc2.getDocumentElement().appendChild(ref1);
					
					//create ref name
					Attr rName = doc2.createAttribute("rName");
					rName.setValue(rName1);
					ref1.setAttributeNode(rName);
					
					//create source attribute
					Attr src = doc2.createAttribute("source");
					src.setValue(((Element) reference).getAttribute("source"));
					ref1.setAttributeNode(src);
					
					//create target attribute
					Attr tar = doc2.createAttribute("target");
					tar.setValue(((Element) reference).getAttribute("target"));
					ref1.setAttributeNode(tar);
					
				}
			}
			NodeList keyvalues = doc2.getElementsByTagName("key_value");
			NodeList keys = doc2.getElementsByTagName("key");
			Node key, keyS = null, keyT = null;
			for(int k=0; k<keys.getLength(); k++) {
				key = keys.item(k);
				String kName = ((Element)key).getAttribute("kName");
				if(kName.compareTo(source) == 0) {
					keyS = key;
					if(keyT != null) break;
				} else if(kName.compareTo(target) == 0) {
					keyT = key;
					if(keyS != null) break;
				}
			}
			if(keyS != null && keyT != null)
				CreateCV(doc2, keyS, keyT);
			else
				throw new Exception("reference incomplete : ");
			
			NodeList concepts1 = doc2.getElementsByTagName("concepts");
			for(int c = 0; c<concepts1.getLength(); c++) {
				Node concept = concepts1.item(c);
				NodeList children = concept.getChildNodes();
				if(children.getLength() ==0) {
					concept.getParentNode().removeChild(concept);
				}
			}

			NodeList values = doc2.getElementsByTagName("value");
			for(int v = 0; v<values.getLength(); v++) {
				Node value = values.item(v);
				if(((Element)value).getAttribute("xsi:type").compareTo("metamodel:ComplexValue") == 0) {
					NodeList children = value.getChildNodes();
					if(children.getLength() ==0) {
						value.getParentNode().getParentNode().removeChild(value.getParentNode());
					}
				}
			}
			
			for(int kv = 0; kv<keyvalues.getLength(); kv++) {
				Node keyvalue = keyvalues.item(kv);
				if((((Element)keyvalue).getAttribute("kvName")).compareTo(((Element)keyvalue.getParentNode().getParentNode()).getAttribute("kvName")) == 0) {
					keyvalue.getParentNode().removeChild(keyvalue);
				}
			}
			//Document doc3 = doc2;
			createSignature(doc2, paths, filename);
			//CreateOutputFile(doc2, paths, filename);
			heuristic(doc2, paths, lastRef);
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public static void heuristicSplit(Document doc, String path, int lastRef) {
		
		try {
			NodeList rows = doc.getElementsByTagName("row");
			for(int r=0; r<rows.getLength(); r++) {
		    	Node row = rows.item(r);
		    	if(row.getParentNode().getNodeName().compareTo("concepts") == 0) {
		    		NodeList keys= ((Element)row).getElementsByTagName("key");
			    	int count = 0;
			    	for(int k=0; k<keys.getLength(); k++) {
			    		Node key = keys.item(k);
			    		//Node key = ((Element)keyvalue).getElementsByTagName("key").item(0);
			    		
			    		if(((Element)key.getParentNode().getParentNode()).getAttribute("rName").compareTo(((Element)row).getAttribute("rName")) == 0 && (!(((Element)key).getAttribute("kType").compareTo("Oid") == 0))) {
					    	count += 1;
			    		}
			    	}
			    	if(count>=2) {
			    		Split(doc, row, path, count, lastRef);
			    	}
		    	}
		    }
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public static void Split(Document doc, Node row, String path, int count, int lastRef) {
		try {
			//ArrayList<ArrayList<String>> keysAll = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> keysAll = new ArrayList<ArrayList<String>>();
			
			ArrayList<String> keysList = new ArrayList<String>();
			ArrayList<String> KeysList1 = new ArrayList<String>();
					
			NodeList keys = ((Element)row).getElementsByTagName("key");
			for(int i=0; i<keys.getLength(); i++) {
				Node key = keys.item(i);
				String kName = ((Element)key).getAttribute("kName");
				if(((Element)key.getParentNode().getParentNode()).getAttribute("rName").compareTo(((Element)row).getAttribute("rName")) == 0) {
					if(!(((Element)key).getAttribute("kType").equals("Oid"))){
						keysList.add(kName);
					}
				}
			}
			//System.out.println("queriesList: " + queriesList);
			//System.out.println("KeysList: " + KeysList);
			System.out.println("keysList: " + keysList);
			for(int i=0; i<keysList.size(); i++) {
				if(!(KeysList.contains(keysList.get(i)))) {
					ArrayList<String> keysDraft = new ArrayList<String>();
					keysDraft.add(keysList.get(i));
					keysAll.add(keysDraft);
				}
			}
			System.out.println("keysAll1: " + keysAll);
			for(int j=0; j<queriesList.size(); j++) {
				ArrayList<String> querykeys = new ArrayList<String>();
				for(String s : queriesList.get(j)) {
					for(int i=0;i<keys.getLength(); i++) {
						Node key = keys.item(i);
						if((((s.compareTo(((Element)key).getAttribute("kName")) == 0) && (!(((Element)key).getAttribute("kType").compareTo("Oid") == 0))))){
							if(!querykeys.contains(s))	
								{querykeys.add(s);}
						}
					}
					
				}
				/*for(int l=querykeys.size()-1; l>=0; l--) {
					boolean found = false;
					for(int i=0; i<keys.getLength(); i++) {
						Node key = keys.item(i);
						if(((Element)key).getAttribute("kName").compareTo(querykeys.get(l)) == 0){
							found = true;
						}
					}
					if(!found) {
						querykeys.remove(l);
					}
				}*/
				System.out.println("querykeys: " + querykeys + ", keysAll2: "+keysAll + " , count " + count);
				boolean exist = false;
				for(int ka=0; ka<keysAll.size(); ka++) {
					if(keysAll.get(ka).containsAll(querykeys)) {
						exist = true;
						break;
					}
				}
				if(querykeys.size() != 0 && !exist && querykeys.size() < count) {
					keysAll.add(querykeys);
				}
			}
			System.out.println("keysAll2: " + keysAll);
			String rName = ((Element)row).getAttribute("rName");
			Node concept = row.getParentNode();
			
			ArrayList<String> keysTemp1 = new ArrayList<String>();
			ArrayList<String> keysTemp2 = new ArrayList<String>();
			ArrayList<String> keysTemp3 = new ArrayList<String>();
			for(String s : keysList) {
				keysTemp3.add(s);
			}
			for(int ka1=0; ka1<keysAll.size(); ka1++) {
				keysTemp1 = keysAll.get(ka1);
				keysTemp3.removeAll(keysTemp1);
				Collections.sort(keysTemp3);
				for(int ka2=0; ka2<keysAll.size(); ka2++) {
					keysTemp2 = keysAll.get(ka2);
					Collections.sort(keysTemp2);
					if(keysTemp3.equals(keysTemp2)) {
						keysAll.remove(keysTemp2);
					}
				}
			}
			System.out.println("keysAll3: " + keysAll);
			for(int ka=0; ka<keysAll.size(); ka++) {
				int kn = ka+1;
				String filename = "Split_"+rName+"_"+kn;
				String paths = path+filename+"/";
				KeysList1 = keysAll.get(ka);
				ArrayList<String> KeysList2 = new ArrayList<String>();
				for(int k1=0; k1<keysList.size(); k1++) {
					KeysList2.add(keysList.get(k1));
				}
				for(int k1=0; k1<KeysList1.size(); k1++) {
					KeysList2.remove(KeysList1.get(k1));
				}
				//System.out.println("KeysList1: " + KeysList1);
				//System.out.println("KeysList2: " + KeysList2);
				Document doc2 = createDocument();
				/*NodeList rows = doc.getElementsByTagName("row");
				boolean found=false;
				ArrayList<String> keysTemp = new ArrayList<String>();
				for(int r=0; r<rows.getLength(); r++) {
					Node row1 = rows.item(r);
					String rName1 = ((Element)row1).getAttribute("rName");
					NodeList keys1 = ((Element)row1).getElementsByTagName("key");
					for(int k=0; k<keys.getLength(); k++) {
						Node key1 = keys1.item(k);
						String kName1 = ((Element)key1).getAttribute("kName");
						if(((Element)key1.getParentNode().getParentNode()).getAttribute("rName").compareTo(rName1) == 0) {
							keysTemp.add(kName1);
						}
					}
					if(keysTemp.containsAll(KeysList1)) {
						found = true;
					}
				}
				if(!found) {*/
				if(!(queriesListTemp.contains(KeysList1))) {
					System.out.println("queriesListTemp before : " + queriesListTemp);
					Collections.sort(KeysList1);
					queriesListTemp.add(KeysList1);
					System.out.println("queriesListTemp after : " + queriesListTemp);
					Split_copy(doc, doc2, KeysList1, KeysList2, doc2.getDocumentElement(), row, concept);
					createSignature(doc2, paths, filename);
					heuristic(doc2, paths, lastRef);
				}
				
				//}
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	/*public static void Split(Document doc, Node row, String path, int count, int lastRef) {
		try {
			ArrayList<ArrayList<String>> keysAll = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> keysAll1 = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> keysAll2 = new ArrayList<ArrayList<String>>();
			
			//System.out.println("test");
			ArrayList<String> keysList = new ArrayList<String>();
			ArrayList<String> keysList1 = new ArrayList<String>();
					
			NodeList keys = ((Element)row).getElementsByTagName("key");
			int k=1;
			float size;
			for(int i=0; i<keys.getLength(); i++) {
				Node key = keys.item(i);
				if(((Element)key.getParentNode().getParentNode()).getAttribute("rName").compareTo(((Element)row).getAttribute("rName")) == 0) {
					String kName = ((Element)key).getAttribute("kName");
					if(!((Element)key).getAttribute("kType").equals("Oid")) {
						keysList.add(kName);
					}
				}
			}
			for(int i=0; i<keysList.size(); i++) {
				if(!(KeysList.contains(keysList.get(i)))) {
					ArrayList<String> keysDraft = new ArrayList<String>();
					keysDraft.add(keysList.get(i));
					keysAll1.add(keysDraft);
				}
			}
			//System.out.println(" " +KeysList);
			size = (keysList.size())/2;
			while(k<=size) {
				keysAll = updateLists(k, keysList);
				for(int ka=0; ka<keysAll.size(); ka++) {
					keysAll2.add(keysAll.get(ka));                                      
				}
				k = k+1;
			}
			for(int ka=0; ka<keysAll1.size(); ka++) {
				if(keysAll2.contains(keysAll1.get(ka))) {
					keysAll2.remove(keysAll1.get(ka));
				}
			}
			String rName = ((Element)row).getAttribute("rName");
			Node concept = row.getParentNode();
			for(int ka=0; ka<keysAll2.size(); ka++) {
				//output files' name
				int kn = ka+1;
				String filename = "Split_"+rName+"_"+kn;
				String paths = path+filename+"/";
				keysList1 = keysAll2.get(ka);
				ArrayList<String> KeysList2 = new ArrayList<String>();
				for(int k1=0; k1<keysList.size(); k1++) {
					KeysList2.add(keysList.get(k1));
				}
				for(int k1=0; k1<keysList1.size(); k1++) {
					KeysList2.remove(keysList1.get(k1));
				}
					
				//System.out.println(" " +(((Element)row).getAttribute("rName")));
				//System.out.println(" " +KeysList2);
				Document doc2 = createDocument();
				Split_copy(doc, doc2, keysList1, KeysList2, doc2.getDocumentElement(), row, concept);
				
				String signature;
				signature = createSignature(doc2, paths, filename);
				//CreateOutputFile(doc2, paths, filename);
				heuristic(doc2, paths, lastRef);
			}	
		}
		catch (Exception e) {e.printStackTrace();}
	}*/
	
	public static Document Split_copy(Document doc, Document doc2, ArrayList<String> keysList1, ArrayList<String> keysList2, Element DataStore, Node row0, Node concept1) {
		
		try {
				
	        //List of concepts & references
	        NodeList references = doc.getElementsByTagName("reference");
			NodeList concepts = doc.getElementsByTagName("concepts");
			
			
			for(int i=0; i<concepts.getLength(); i++) {
				Node concept = concepts.item(i);
				
				//create concept
				Element cpt1 = doc2.createElement(concept.getNodeName());
				DataStore.appendChild(cpt1);
				
				//create cName attribute
				Attr cName = doc2.createAttribute("cName");
		        cName.setValue(((Element) concept).getAttribute("cName"));
		        cpt1.setAttributeNode(cName);
		        
				if((((Element) concept).getAttribute("cName")).equals(((Element) concept1).getAttribute("cName")) ) {
					//create row1
			        NodeList rows = ((Element) concept).getElementsByTagName("row");
			        for(int r=0; r<rows.getLength(); r++) {
			        	Node row = rows.item(r);
			        	if((((Element) row.getParentNode()).getAttribute("cName")).compareTo((((Element) concept).getAttribute("cName"))) == 0) {
			        		if((((Element) row).getAttribute("rName")).compareTo(((Element) row0).getAttribute("rName")) == 0) {
				        		
				        		//create row1
				        		Element row1 = doc2.createElement(row.getNodeName());
					            cpt1.appendChild(row1);
					            
					            //create rName attribute
					            Attr rName = doc2.createAttribute("rName");
					    		String attrR = ((Element) row).getAttribute("rName")+"_"+1;
					    		rName.setValue(attrR);
					            row1.setAttributeNode(rName);
					            
					            //create row1 key values
					            createRow(row, row1, doc2, keysList1);
					            
					            //create row2
				        		Element row2 = doc2.createElement(row.getNodeName());
					            cpt1.appendChild(row2);
					            
					            //create rName attribute
					            Attr rName2 = doc2.createAttribute("rName");
					    		String attrR2 = ((Element) row).getAttribute("rName")+"_"+2;
					    		rName2.setValue(attrR2);
					            row2.setAttributeNode(rName2);
					            
					            //create row2 key values
					            createRow(row, row2, doc2, keysList2);
					            
			        		}else {
					           		CopyRow(doc, doc2, ((Element) row), cpt1);
					        }
			        	}
			        	
			        }
			        
				}else {
					//System.out.println(c + ((Element) concept).getAttribute("cName"));
					//c = c+1;
			        Copy(doc2, ((Element)concept), cpt1);
			        
 				}
			}
			for(int i=0; i<references.getLength(); i++) {
				Node reference = references.item(i);
				
				//create reference
				Element ref1 = doc2.createElement(reference.getNodeName());
				DataStore.appendChild(ref1);
					
				//create ref name
				Attr rName = doc2.createAttribute("rName");
				rName.setValue(((Element) reference).getAttribute("rName"));
				ref1.setAttributeNode(rName);
					
				//create source attribute
				Attr src = doc2.createAttribute("source");
				src.setValue(((Element) reference).getAttribute("source"));
				ref1.setAttributeNode(src);
					
				//create target attribute
				Attr tar = doc2.createAttribute("target");
				tar.setValue(((Element) reference).getAttribute("target"));
				ref1.setAttributeNode(tar);
					
			}
			
		}catch(Exception e) {e.printStackTrace();}
		return doc2;
	}
	
	public static void  createRow(Node row, Node row1, Document doc2, ArrayList<String> keysList) {
	
		NodeList keyvalues = ((Element)row).getElementsByTagName("key_value");
	    for(int kv=0; kv<keyvalues.getLength(); kv++) {
	    	
	    	Node key_value = keyvalues.item(kv);
	    	if(((Element)key_value.getParentNode()).getAttribute("rName").compareTo(((Element)row).getAttribute("rName")) ==0){
	    		Node key = ((Element)key_value).getElementsByTagName("key").item(0);
	       		if(((Element)key).getAttribute("kType").compareTo("Oid") == 0) {
	       			//create key_value
	    	        Element kv1 = doc2.createElement(key_value.getNodeName());
	    	        row1.appendChild(kv1);
	    	        				        
	    	        //create kvname attribute
	    	        Attr kvName = doc2.createAttribute("kvName");
	    			String attrKV = ((Element)key_value).getAttribute("kvName");
	    			kvName.setValue(attrKV);
	    	        kv1.setAttributeNode(kvName);
	
	    	        //----------------------------------------------------------
	    	       
	    	        //create key
	    	        Element k1 = doc2.createElement(key.getNodeName());
	    	        kv1.appendChild(k1);
	    	        
	    	        //create kName attribute
	    	        Attr kName = doc2.createAttribute("kName");
	    			String attrKn = ((Element)key).getAttribute("kName");
	    			kName.setValue(attrKn);
	    	        k1.setAttributeNode(kName);
	    	        
	    	        //create kType attribute
	    	        Attr kType = doc2.createAttribute("kType");
	    			String attrKt = ((Element)key).getAttribute("kType");
	    			kType.setValue(attrKt);
	    	        k1.setAttributeNode(kType);
	    	
	    	        //----------------------------------------------------------
	    	         
	    	        Node value = ((Element)key_value).getElementsByTagName("value").item(0);
	    	        
	    	        String attrXsi = ((Element)value).getAttribute("xsi:type");
	    	        
	    	        //create value
	    	        Element v1 = doc2.createElement(value.getNodeName());
	    		    kv1.appendChild(v1);
	    		       
	    		    //create vName attribute
	    		    Attr vName = doc2.createAttribute("vName");
	    			String attrVn = ((Element)value).getAttribute("vName");
	    			vName.setValue(attrVn);
	    		    v1.setAttributeNode(vName);
	    		        
	    		     //create value child attribute
	    		     Attr xsi_type = doc2.createAttribute("xsi:type");
	    		     xsi_type.setValue(attrXsi);
	    		     v1.setAttributeNode(xsi_type);
	    		        
	    		     if( attrXsi.equals("metamodel:ComplexValue")) {     
	    		       	Copy(doc2, ((Element)value), v1); 
	    		     } 
	    	    }
	    	}
	    	
	    }
	    for(int k=0; k<keysList.size(); k++) {
	    	 for(int kv=0; kv<keyvalues.getLength(); kv++) {
	    		 Node key_value = keyvalues.item(kv);
	         	 Node key = ((Element)key_value).getElementsByTagName("key").item(0);
	        		if(((Element)key).getAttribute("kName").equals(keysList.get(k))) {
	        			//create key_value
	                    Element kv1 = doc2.createElement(key_value.getNodeName());
	                    row1.appendChild(kv1);
	                    				        
	                    //create kvname attribute
	                    Attr kvName = doc2.createAttribute("kvName");
	            		String attrKV = ((Element)key_value).getAttribute("kvName");
	            		kvName.setValue(attrKV);
	                    kv1.setAttributeNode(kvName);
	
	                    //----------------------------------------------------------
	                   
	                    //create key
	                    Element k1 = doc2.createElement(key.getNodeName());
	                    kv1.appendChild(k1);
	                    
	                    //create kName attribute
	                    Attr kName = doc2.createAttribute("kName");
	            		String attrKn = ((Element)key).getAttribute("kName");
	            		kName.setValue(attrKn);
	                    k1.setAttributeNode(kName);
	                    
	                    //create kType attribute
	                    Attr kType = doc2.createAttribute("kType");
	            		String attrKt = ((Element)key).getAttribute("kType");
	            		kType.setValue(attrKt);
	                    k1.setAttributeNode(kType);
	
	                    //----------------------------------------------------------
	                     
	                    Node value = ((Element)key_value).getElementsByTagName("value").item(0);
	                    
	                    String attrXsi = ((Element)value).getAttribute("xsi:type");
	                    
	                    //create value
	                    Element v1 = doc2.createElement(value.getNodeName());
	            	    kv1.appendChild(v1);
	            	       
	            	    //create vName attribute
	            	    Attr vName = doc2.createAttribute("vName");
	            		String attrVn = ((Element)value).getAttribute("vName");
	            		vName.setValue(attrVn);
	            	    v1.setAttributeNode(vName);
	            	        
	            	     //create value child attribute
	            	     Attr xsi_type = doc2.createAttribute("xsi:type");
	            	     xsi_type.setValue(attrXsi);
	            	     v1.setAttributeNode(xsi_type);
	            	        
	            	     if( attrXsi.equals("metamodel:ComplexValue")) {     
	            	       	Copy(doc2, ((Element)value), v1); 
	            	     }
	        		}
	    		 
	    	 }
	    }
	}
	
	public static void CopyRow(Document doc, Document d, Element row, Element cpt){
		try {
	        Node  key_value, key, value = null;
			Element row1, kv1, k1, v1 = null;
			String rName1 = row.getAttribute("rName");
			//create row
			row1 = d.createElement("row");
			cpt.appendChild(row1);
			
			//create row name
			Attr rName = d.createAttribute("rName");
			rName.setValue(rName1);
	        row1.setAttributeNode(rName);
	        
	                NodeList keyvalues = row.getElementsByTagName("key_value");
	                //System.out.println(keyvalueNames);
	                for(int i=0; i<keyvalues.getLength(); i++) {
	                	key_value = keyvalues.item(i);
	                	if(key_value.getParentNode().getParentNode().getNodeName().equals("concepts")){
	                		String attrKV = ((Element)key_value).getAttribute("kvName");
	                		//create key_value
	            			kv1 = d.createElement(key_value.getNodeName());
	            	        row1.appendChild(kv1);
	            	        				        
	            	        //create kvname attribute
	            	        Attr kvName = d.createAttribute("kvName");
	            			kvName.setValue(attrKV);
	            	        kv1.setAttributeNode(kvName);
	            	
	            	        //----------------------------------------------------------
	            	       
	            	        //create key
	            	        key = ((Element)key_value).getElementsByTagName("key").item(0);
	            	        k1 = d.createElement(key.getNodeName());
	            	        kv1.appendChild(k1);
	            	        
	            	        //create kName attribute
	            	        Attr kName = d.createAttribute("kName");
	            			String attrKn = ((Element)key).getAttribute("kName");
	            			kName.setValue(attrKn);
	            	        k1.setAttributeNode(kName);
	            	        
	            	        //create kType attribute
	            	        Attr kType = d.createAttribute("kType");
	            			String attrKt = ((Element)key).getAttribute("kType");
	            			kType.setValue(attrKt);
	            	        k1.setAttributeNode(kType);
	            	
	            	        //----------------------------------------------------------
	            	         
	            	        value = ((Element)key_value).getElementsByTagName("value").item(0);
	            	        
	            	        String attrXsi = ((Element)value).getAttribute("xsi:type");
	            	        
	            	        //create value
	            	        v1 = d.createElement(value.getNodeName());
	            		    kv1.appendChild(v1);
	            		       
	            		    //create vName attribute
	            		    Attr vName = d.createAttribute("vName");
	            			String attrVn = ((Element)value).getAttribute("vName");
	            			vName.setValue(attrVn);
	            		    v1.setAttributeNode(vName);
	            		        
	            		     //create value child attribute
	            		     Attr xsi_type = d.createAttribute("xsi:type");
	            		     xsi_type.setValue(attrXsi);
	            		     v1.setAttributeNode(xsi_type);
	            		        
	            		     if( attrXsi.equals("metamodel:ComplexValue")) {     
	            		       	CopyCV(d, ((Element)value), v1); 
	            		     }
	                	}
	                }
	        
		}catch(Exception e) {}
	}

	public static void Copy(Document d, Element concept, Element cpt){
		try {
	        //Node  key_value, key, value = null;
			//Element row1, kv1, k1, v1 = null;
			
			//create row
	        NodeList rows = concept.getElementsByTagName("row");
	        for(int r=0; r<rows.getLength(); r++) {
	        	
	        	Node row = rows.item(r);
	        	if((((Element)row.getParentNode()).getAttribute("cName").compareTo(concept.getAttribute("cName")) == 0) || (((Element)row.getParentNode()).getAttribute("vName").compareTo(concept.getAttribute("vName")) == 0)){
	        		Element row1 = d.createElement(row.getNodeName());
	                cpt.appendChild(row1);
	                
	                //create rName attribute
	                Attr rName = d.createAttribute("rName");
	        		rName.setValue(((Element) row).getAttribute("rName"));
	                row1.setAttributeNode(rName);
	                
	                //----------------------------------------------------------
	                //ArrayList<String> keyvalueNames = new ArrayList<String>();
	                NodeList keyvalues = ((Element)row).getElementsByTagName("key_value");
	                //System.out.println(keyvalueNames);
	                for(int i=0; i<keyvalues.getLength(); i++) {
	                	Node key_value = keyvalues.item(i);
	                	if((((Element)key_value.getParentNode()).getAttribute("rName")).compareTo(((Element)row).getAttribute("rName")) == 0){
	                		String attrKV = ((Element)key_value).getAttribute("kvName");
	                		//create key_value
	            			Element kv1 = d.createElement(key_value.getNodeName());
	            	        row1.appendChild(kv1);
	            	        				        
	            	        //create kvname attribute
	            	        Attr kvName = d.createAttribute("kvName");
	            			kvName.setValue(attrKV);
	            	        kv1.setAttributeNode(kvName);
	            	
	            	        //----------------------------------------------------------
	            	       
	            	        //create key
	            	        Node key = ((Element)key_value).getElementsByTagName("key").item(0);
	            	        Element k1 = d.createElement(key.getNodeName());
	            	        kv1.appendChild(k1);
	            	        
	            	        //create kName attribute
	            	        Attr kName = d.createAttribute("kName");
	            			String attrKn = ((Element)key).getAttribute("kName");
	            			kName.setValue(attrKn);
	            	        k1.setAttributeNode(kName);
	            	        
	            	        //create kType attribute
	            	        Attr kType = d.createAttribute("kType");
	            			String attrKt = ((Element)key).getAttribute("kType");
	            			kType.setValue(attrKt);
	            	        k1.setAttributeNode(kType);
	            	
	            	        //----------------------------------------------------------
	            	         
	            	        Node value = ((Element)key_value).getElementsByTagName("value").item(0);
	            	        
	            	        String attrXsi = ((Element)value).getAttribute("xsi:type");
	            	        
	            	        //create value
	            	        Element v1 = d.createElement(value.getNodeName());
	            		    kv1.appendChild(v1);
	            		       
	            		    //create vName attribute
	            		    Attr vName = d.createAttribute("vName");
	            			String attrVn = ((Element)value).getAttribute("vName");
	            			vName.setValue(attrVn);
	            		    v1.setAttributeNode(vName);
	            		        
	            		     //create value child attribute
	            		     Attr xsi_type = d.createAttribute("xsi:type");
	            		     xsi_type.setValue(attrXsi);
	            		     v1.setAttributeNode(xsi_type);
	            		        
	            		     if( attrXsi.equals("metamodel:ComplexValue")) { 
	            		    	NodeList rowsCV = ((Element)value).getElementsByTagName("row");
	            		    	for(int j=0; j<rowsCV.getLength(); j++) {
	            		    		Node rowCV = rowsCV.item(j);
	            		    		if((((Element)rowCV.getParentNode()).getAttribute("vName")).compareTo(((Element)value).getAttribute("vName")) ==0){
	            		        		
	            		    			//create row
	            		    			Element rowCV1 = d.createElement(rowCV.getNodeName());
	            		                v1.appendChild(rowCV1);
	            		                
	            		                //create rName attribute
	            		                Attr rNameCV = d.createAttribute("rName");
	            		                rNameCV.setValue(((Element) rowCV).getAttribute("rName"));
	            		                rowCV1.setAttributeNode(rNameCV);
	            		                
	            		                CopyCV(d, ((Element)rowCV), rowCV1); 
	            		    		}  
	            		    	}
	            		     }
	                	}
	                }
	            }
	        }
		}catch(Exception e) {}
		
	}
	public static void CreateCV(Document d, Node kSource, Node kTarget) {
		try {
		Element parentSource = (Element)(kSource.getParentNode().getParentNode());
		Element parentTarget = (Element)(kTarget.getParentNode().getParentNode());
		
		//create Key_Value
	    Element tarKV = d.createElement("key_value");
	    parentSource.appendChild(tarKV);
		    
	    //create key_value name
	    Attr kvName = d.createAttribute("kvName");
		kvName.setValue("kv_"+((Element)kTarget).getAttribute("kName"));
		tarKV.setAttributeNode(kvName);
		
		//create Key
	    Element k1 = d.createElement("key");
	    tarKV.appendChild(k1);

	    //create key name
		Attr kName = d.createAttribute("kName");
		kName.setValue(((Element)kTarget).getAttribute("kName"));
		k1.setAttributeNode(kName);
			
		//create kType attribute
	    Attr kType = d.createAttribute("kType");
		kType.setValue(((Element)kTarget).getAttribute("kType"));
		k1.setAttributeNode(kType);
			
	    //create Value
		Element v1 = d.createElement("value");
		tarKV.appendChild(v1);
		    
	    //create vName attribute
	    Attr vName = d.createAttribute("vName");
		vName.setValue("v_"+((Element)kTarget).getAttribute("kName"));
		v1.setAttributeNode(vName);
		    
		Attr xsi_type = d.createAttribute("xsi:type");
		xsi_type.setValue("metamodel:ComplexValue");
	    v1.setAttributeNode(xsi_type);
	    
	    //create row
	    Element tarRow = d.createElement("row");
	    v1.appendChild(tarRow);
	    
	    //create rName
	    Attr rName = d.createAttribute("rName");
		rName.setValue("r_"+((Element)kTarget).getAttribute("kName"));
		tarRow.setAttributeNode(rName);
		CopyCV(d, parentTarget, tarRow);
		
		parentTarget.getParentNode().removeChild(parentTarget);

		}catch(Exception e) {e.printStackTrace();System.exit(0);}
	}
	
	public static void CopyCV(Document doc, Element row, Element tarRow) {
		NodeList keyvalues = ((Element)row).getElementsByTagName("key_value");
        for(int kv=0; kv<keyvalues.getLength(); kv++) {
        	
        	Node key_value = keyvalues.item(kv);
        	if((((Element)key_value.getParentNode()).getAttribute("rName")).compareTo(((Element)row).getAttribute("rName")) == 0){
            	//create key_value
    	        Element kv1 = doc.createElement(key_value.getNodeName());
    	        tarRow.appendChild(kv1);
    	        				        
    	        //create kvname attribute
    	        Attr kvName = doc.createAttribute("kvName");
    			String attrKV = ((Element)key_value).getAttribute("kvName");
    			kvName.setValue(attrKV);
    	        kv1.setAttributeNode(kvName);
    	
    	        //----------------------------------------------------------
    	       
    	        //create key
    	        Node key = ((Element)key_value).getElementsByTagName("key").item(0);
    	        Element k1 = doc.createElement(key.getNodeName());
    	        kv1.appendChild(k1);
    	        
    	        //create kName attribute
    	        Attr kName = doc.createAttribute("kName");
    			String attrKn = ((Element)key).getAttribute("kName");
    			kName.setValue(attrKn);
    	        k1.setAttributeNode(kName);
    	        
    	        //create kType attribute
    	        Attr kType = doc.createAttribute("kType");
    			String attrKt = ((Element)key).getAttribute("kType");
    			kType.setValue(attrKt);
    	        k1.setAttributeNode(kType);
    	
    	        //----------------------------------------------------------
    	         
    	        Node value = ((Element)key_value).getElementsByTagName("value").item(0);
    	        
    	        String attrXsi = ((Element)value).getAttribute("xsi:type");
    	        
    	        //create value
    	        Element v1 = doc.createElement(value.getNodeName());
    		    kv1.appendChild(v1);
    		       
    		    //create vName attribute
    		    Attr vName = doc.createAttribute("vName");
    			String attrVn = ((Element)value).getAttribute("vName");
    			vName.setValue(attrVn);
    		    v1.setAttributeNode(vName);
    		        
    		     //create value child attribute
    		     Attr xsi_type = doc.createAttribute("xsi:type");
    		     xsi_type.setValue(attrXsi);
    		     v1.setAttributeNode(xsi_type);
    		        
    		     if( attrXsi.compareTo("metamodel:ComplexValue") == 0) {     
    		    	NodeList rowsCV = ((Element)value).getElementsByTagName("row");
     		    	for(int j=0; j<rowsCV.getLength(); j++) {
     		    		Node rowCV = rowsCV.item(j);
     		    		if(((Element)rowCV.getParentNode()).getAttribute("vName").compareTo(((Element)value).getAttribute("vName")) ==0){
     		        		
     		    			//create row
     		    			Element rowCV1 = doc.createElement("row");
     		                v1.appendChild(rowCV1);
     		                
     		                //create rName attribute
     		                Attr rNameCV = doc.createAttribute("rName");
     		                rNameCV.setValue(((Element)rowCV).getAttribute("rName"));
     		                rowCV1.setAttributeNode(rNameCV);
     		                
     		                CopyCV(doc, ((Element)rowCV), rowCV1); 
     		    		}  
     		    	} 
    		     } 
        	}
        }  	
	}
	
	
	public static String rowSignature(Element concept) {
		
		String signature = "";
		NodeList rows = concept.getElementsByTagName("row");
		
		ArrayList<String> rowList1 = new ArrayList<String>();
		//ArrayList<String> rowList2 = new ArrayList<String>();
		for(int r=0; r<rows.getLength(); r++) {
			Node row = rows.item(r);
			if(((Element)row.getParentNode()).getAttribute("cName").compareTo(concept.getAttribute("cName")) == 0) {
				rowList1.add(((Element)row).getAttribute("rName"));
			}else if(((Element)row.getParentNode()).getAttribute("vName").compareTo(concept.getAttribute("vName")) == 0) {
				rowList1.add(((Element)row).getAttribute("rName"));
			}
		}
		Collections.sort(rowList1);
		for(int r=0; r<rows.getLength(); r++){
			Node row = rows.item(r);
			for(int i=0; i<rowList1.size(); i++) {
				if(((Element)row).getAttribute("rName").compareTo(rowList1.get(i)) == 0) {
					signature += "(";
					NodeList keys = ((Element)row).getElementsByTagName("key");
					for(int k=0; k<keys.getLength(); k++) {
						Node key = keys.item(k);
						if(((Element)key.getParentNode().getParentNode()).getAttribute("rName").compareTo(((Element)row).getAttribute("rName")) == 0) {
							NodeList nestedRows = ((Element)key.getParentNode()).getElementsByTagName("row");
							if(nestedRows.getLength() == 0) {
								signature = signature + ((Element)key).getAttribute("kName") + ",";
							}else {
								signature += ((Element)key).getAttribute("kName");
								signature += rowSignature(((Element)key.getParentNode()));
							}
						
						}
					}
				signature += "),";
				}
			}
		}
		return signature;
	}

	/*public static void createSignature(Document doc2, String path, String filename) {
		
		try {
			String signature;

			NodeList concepts = doc2.getElementsByTagName("concepts");

			ArrayList<String> signatures = new ArrayList<String>();
			ArrayList<String> conceptList = new ArrayList<String>();
			
			for(int c=0; c<concepts.getLength(); c++) {
				Node concept = concepts.item(c);
				conceptList.add(((Element)concept).getAttribute("cName"));
			}
			Collections.sort(conceptList);
			
			for(int i=0; i<conceptList.size(); i++) {
				String conceptSignature = "";
				for(int c=0; c<concepts.getLength(); c++) {
					Node concept = concepts.item(c);
					if(((Element)concept).getAttribute("cName").compareTo(conceptList.get(i)) == 0) {
						conceptSignature = (((Element)concept).getAttribute("cName") + "{");
						String rowSignature = rowSignature((Element)concept);
						conceptSignature = conceptSignature + rowSignature;
						conceptSignature = conceptSignature + "},";
					}
				}
				signatures.add(conceptSignature);
			}

			writeSignature(signatures, path, filename);
		}catch(Exception e) {e.printStackTrace();System.exit(0);}
		
	}*/
	
	public static String createSignature(Document doc2, String path, String filename) {
		String signature = "";
		try {
			
	        //Document doc3 = createDocument();
			NodeList concepts = doc2.getElementsByTagName("concepts");
			NodeList references = doc2.getElementsByTagName("reference");
			//NodeList keyvalues = doc2.getElementsByTagName("key_value");
			//NodeList rows = doc2.getElementsByTagName("row");
			
			ArrayList<String> signatures = new ArrayList<String>();
			ArrayList<String> referenceList = new ArrayList<String>();
			ArrayList<String> conceptList = new ArrayList<String>();
			//ArrayList<String> rowList = new ArrayList<String>();
			
			for(int c=0; c<concepts.getLength(); c++) {
				Node concept = concepts.item(c);
				conceptList.add(((Element)concept).getAttribute("cName"));
			}
			Collections.sort(conceptList);
			
			for(int i=0; i<conceptList.size(); i++) {
				String conceptSignature = "";
				//System.out.println(i + " " + (conceptList.get(i)));
				for(int c=0; c<concepts.getLength(); c++) {
					Node concept = concepts.item(c);
					if(((Element)concept).getAttribute("cName").compareTo(conceptList.get(i)) == 0) {
						conceptSignature = (((Element)concept).getAttribute("cName") + "{");
						String rowSignature = rowSignature((Element)concept);
						conceptSignature = conceptSignature + rowSignature;
						conceptSignature = conceptSignature + "},";
					}
				}
				signatures.add(conceptSignature);
			}
			for(int r=0; r<references.getLength(); r++) {
				Node reference = references.item(r);
				referenceList.add(((Element)reference).getAttribute("rName"));
			}
			Collections.sort(referenceList);
			
			for(int r=0; r<referenceList.size(); r++) {
				String referenceSignature = "";
				//System.out.println(i + " " + (conceptList.get(i)));
				for(int i=0; i<references.getLength(); i++) {
					Node reference = references.item(i);
					if(((Element)reference).getAttribute("rName").compareTo(referenceList.get(r)) == 0) {
						referenceSignature = (((Element)reference).getAttribute("rName") + "(");
						//String rowSignature = rowSignature((Element)concept);
						referenceSignature = referenceSignature + ((Element)reference).getAttribute("source");
						referenceSignature = referenceSignature + ((Element)reference).getAttribute("target") + "),";
					}
				}
				signatures.add(referenceSignature);
			}
			//CreateOutputFile(doc2, path, filename);
			for(int i=0; i<signatures.size(); i++) {
	        	signature += signatures.get(i);
	        }
			writeSignature(signature, path, filename);
		}catch(Exception e) {e.printStackTrace();}
		return signature;
	}
	
	public static void writeSignature(String signature, String path, String filename) throws IOException {
		//write on signatures in file
        writer.write(signature);
        writer.write("\r\n");   // write new line
        writer.write(path+filename);
        writer.write("\r\n");
        writer.flush();
	}
	
	public static void WriteToCSVFile(String DMname, String strategy){
		try {	
			//read from the signatures' file
			FileReader reader = new FileReader("ModelsHeuristic/Common Models/signatures.txt");
	        BufferedReader bufferedReader = new BufferedReader(reader);
	        ArrayList<String> signatures = new ArrayList<String>();
	        String line;

	        while ((line = bufferedReader.readLine()) != null) {
	            signatures.add(line);
	        }
	        reader.close();
	        
			FileWriter fw = new FileWriter("ModelsHeuristic/Common Models/Count.csv",true);
			
			int count = signatures.size()/2 + 1;
			//fw.write("data model\tHeuristic\tTotal DM\tNb split\tNb merge\tNb unique");
			fw.write("\n"+DMname+"\t"+strategy+"\t"+count);
			int countM = 0, countS = 0;
			for(int i=1; i<signatures.size(); i=i+2) {
				String chemin = signatures.get(i);
				String fichier = chemin.substring(chemin.lastIndexOf("/"));
				
				if(fichier.contains("Merge")) {
					countM = countM + 1;
				}
				if(fichier.contains("Split")) {
					countS = countS + 1;
				}
			}
			ArrayList<String> distincts = new ArrayList<String> ();
			for (int i=0;i<signatures.size();i=i+2) {
				if(!distincts.contains(signatures.get(i)))
					distincts.add(signatures.get(i));
			}
			int countR = distincts.size() +1;
			fw.write("\t"+countS+"\t"+countM+"\t"+countR);
			fw.flush();
			fw.close();
	    }catch(Exception e) {
	    	
	    }
	}
	
	public static Document createDocument() {
		
		try {
			DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuild = dbFact.newDocumentBuilder();
			Document doc2 = dBuild.newDocument();
			createDataStore (doc2);
			return doc2;
		}catch(Exception e) {
			return null;
		}
		
	}
	
	public static void createDataStore(Document doc2) {
		
		//create DataStore
		Element DataStore2 = doc2.createElement("DataStore");
		doc2.appendChild(DataStore2);

		//create DataStore Name
		Attr dsName2 = doc2.createAttribute("dsName");
		String attrDS2 = mainDoc.getDocumentElement().getAttribute("dsName");
        dsName2.setValue(attrDS2);
        DataStore2.setAttributeNode(dsName2); 
        
        //create namespace
        Attr namespace2 = doc2.createAttribute("xmlns:xsi");
		String attrNS2 = mainDoc.getDocumentElement().getAttribute("xmlns:xsi");
		namespace2.setValue(attrNS2);
        DataStore2.setAttributeNode(namespace2);
        
       // return DataStore2;
	}
	
	public static void readQueries(String pathname, String filename) {
		try {
			queriesList.clear();
			KeysList.clear();
			//read from the signatures' file
			FileReader reader = new FileReader(pathname+filename+"_queries.txt");
	        BufferedReader bufferedReader = new BufferedReader(reader);
	        
	        String line;
	        
	        while ((line = bufferedReader.readLine()) != null) {
	        	String [] keys = line.split(",");
	        	ArrayList<String> querykeys = new ArrayList<String>();
	        	for(int k=0; k<keys.length; k++) {
	        		if(!(KeysList.contains(keys[k]))) {
	        			KeysList.add(keys[k]);
	        		}
	        		querykeys.add(keys[k]);
	        	}
	        	queriesList.add(querykeys);
	        }
	        reader.close();
		}catch(Exception e){}
	}
	
	public static void MergeNaive(String source, String target, Document doc, Node ref, String path, int lastRef) {
		try {
			
			Document doc2 = createDocument();
			NodeList references = doc.getElementsByTagName("reference");
			NodeList concepts = doc.getElementsByTagName("concepts");
			//NodeList keyvalues1 = doc.getElementsByTagName("key_value");
			String filename = "Merge_"+source+"_"+target;
			String paths = path+filename+"/";
			
			for(int c=0; c<concepts.getLength(); c++) {
				Node concept = concepts.item(c);
				//create concept
				Element cpt1 = doc2.createElement(concept.getNodeName());
				doc2.getDocumentElement().appendChild(cpt1);
				
				//create cName attribute
				Attr cName = doc2.createAttribute("cName");
		        cName.setValue(((Element) concept).getAttribute("cName"));
		        cpt1.setAttributeNode(cName);
		        
		        Copy(doc2, ((Element)concept), cpt1);
			}
			
			for(int i=0; i<references.getLength(); i++) {
				Node reference = references.item(i);
				String rName1 = ((Element)reference).getAttribute("rName");
				String rName2 = ((Element)ref).getAttribute("rName");
				if(!rName1.equals(rName2)){
					
					//create reference
					Element ref1 = doc2.createElement(reference.getNodeName());
					doc2.getDocumentElement().appendChild(ref1);
					
					//create ref name
					Attr rName = doc2.createAttribute("rName");
					rName.setValue(rName1);
					ref1.setAttributeNode(rName);
					
					//create source attribute
					Attr src = doc2.createAttribute("source");
					src.setValue(((Element) reference).getAttribute("source"));
					ref1.setAttributeNode(src);
					
					//create target attribute
					Attr tar = doc2.createAttribute("target");
					tar.setValue(((Element) reference).getAttribute("target"));
					ref1.setAttributeNode(tar);
				}
			}
			NodeList keyvalues = doc2.getElementsByTagName("key_value");
			NodeList keys = doc2.getElementsByTagName("key");
			Node key, keyS = null, keyT = null;
			for(int k=0; k<keys.getLength(); k++) {
				key = keys.item(k);
				String kName = ((Element)key).getAttribute("kName");
				if(kName.compareTo(source) == 0) {
					keyS = key;
					if(keyT != null) break;
				} else if(kName.compareTo(target) == 0) {
					keyT = key;
					if(keyS != null) break;
				}
			}
			if(keyS != null && keyT != null)
				CreateCV(doc2, keyS, keyT);
			else
				throw new Exception("reference incomplete : ");
			
			NodeList concepts1 = doc2.getElementsByTagName("concepts");
			for(int c = 0; c<concepts1.getLength(); c++) {
				Node concept = concepts1.item(c);
				NodeList children = concept.getChildNodes();
				if(children.getLength() ==0) {
					concept.getParentNode().removeChild(concept);
				}
			}
			/*Node k, keyvalueS = null, keyvalueT = null;
			String kvName = null;
			
			for(int kv=0; kv<keyvalues.getLength(); kv++) {
				k = keyvalues.item(kv);
				kvName = ((Element)k).getAttribute("kvName");
				if(kvName.compareTo(source) == 0) {
					keyvalueS = k;
					if(keyvalueT != null) break;
				} else if(kvName.compareTo(target) == 0) {
					keyvalueT = k;
					if(keyvalueS != null) break;
				}
			}
			if(keyvalueS != null && keyvalueT != null)
				CreateCV(doc2, keyvalueS, keyvalueT);
			else
				throw new Exception("reference incomplete : ");
			
			NodeList concepts1 = doc2.getElementsByTagName("concepts");
			for(int c = 0; c<concepts1.getLength(); c++) {
				Node concept = concepts1.item(c);
				NodeList children = concept.getChildNodes();
				if(children.getLength() ==0) {
					concept.getParentNode().removeChild(concept);
				}
			}*/

			NodeList values = doc2.getElementsByTagName("value");
			for(int v = 0; v<values.getLength(); v++) {
				Node value = values.item(v);
				if(((Element)value).getAttribute("xsi:type").compareTo("metamodel:ComplexValue") == 0) {
					NodeList children = value.getChildNodes();
					if(children.getLength() ==0) {
						value.getParentNode().getParentNode().removeChild(value.getParentNode());
					}
				}
			}
			
			for(int kv = 0; kv<keyvalues.getLength(); kv++) {
				Node keyvalue = keyvalues.item(kv);
				if((((Element)keyvalue).getAttribute("kvName")).compareTo(((Element)keyvalue.getParentNode().getParentNode()).getAttribute("kvName")) == 0) {
					keyvalue.getParentNode().removeChild(keyvalue);
				}
			}
			//Document doc3 = doc2;
			String signature;
			signature = createSignature(doc2, paths, filename);
			CreateOutputFile(doc2, paths, filename);
			if(!(SIGNATURES.contains(signature))) {
				SIGNATURES.add(signature);
				heuristic(doc2, paths, lastRef);
			}
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public static void SplitNaive(Document doc, Node row, String path, int lastRef) {
		try {
			ArrayList<ArrayList<String>> keysAll = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> keysAll2 = new ArrayList<ArrayList<String>>();
			
					//System.out.println("test");
					ArrayList<String> KeysList = new ArrayList<String>();
					ArrayList<String> KeysList1 = new ArrayList<String>();
					
					NodeList keys = ((Element)row).getElementsByTagName("key");
					int k=1;
					float size;
					for(int ks=0; ks<keys.getLength(); ks++) {
						Node key = keys.item(ks);
						if(((Element)key.getParentNode().getParentNode()).getAttribute("rName").compareTo(((Element)row).getAttribute("rName")) == 0) {
							String kName = ((Element)key).getAttribute("kName");
							if(!((Element)key).getAttribute("kType").equals("Oid")) {
								KeysList.add(kName);
							}
						}
					}
					System.out.println(" " +KeysList);
					size = (KeysList.size())/2;
					while(k<=size) {
						keysAll = updateLists(k, KeysList);
						for(int ka=0; ka<keysAll.size(); ka++) {
							keysAll2.add(keysAll.get(ka));                                      
						}
						k = k+1;
					}
					String rName = ((Element)row).getAttribute("rName");
					Node concept = row.getParentNode();
					for(int ka=0; ka<keysAll2.size(); ka++) {
						//output files' name
						int kn = ka+1;
						String filename = "Split_"+rName+"_"+kn;
						String paths = path+filename+"/";
						KeysList1 = keysAll2.get(ka);
						ArrayList<String> KeysList2 = new ArrayList<String>();
						for(int k1=0; k1<KeysList.size(); k1++) {
							KeysList2.add(KeysList.get(k1));
						}
						for(int k1=0; k1<KeysList1.size(); k1++) {
							KeysList2.remove(KeysList1.get(k1));
						}
					
						//System.out.println(" " +(((Element)row).getAttribute("rName")));
						System.out.println("KeysList2 " +KeysList2);
						Document doc2 = createDocument();
						Split_copy(doc, doc2, KeysList1, KeysList2, doc2.getDocumentElement(), row, concept);
						
						String signature;
						signature = createSignature(doc2, paths, filename);
						//CreateOutputFile(doc2, paths, filename);
						if(!(SIGNATURES.contains(signature))) {
							SIGNATURES.add(signature);
							heuristic(doc2, paths, lastRef);
						}
					}	
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public static ArrayList<ArrayList<String>> updateLists(int j , ArrayList<String> keys) {	
		
		ArrayList<ArrayList<String>> keysAll = new ArrayList<ArrayList<String>>();
			int i=0;
			if(keys.size()%2 == 0 && j == keys.size()/2 ) {
				for(i=0; i<keys.size()/2; i++){
					int l = j;
					ArrayList<String> keys1 = new ArrayList<String>();
					keys1 = recursiveLists(i, l, keys, keys1);
					keysAll.add(keys1);
				}
			}else {
				for(i=0; i<keys.size(); i++){
					int l = j;
					ArrayList<String> keys1 = new ArrayList<String>();
					keys1 = recursiveLists(i, l, keys, keys1);
					keysAll.add(keys1);
				}
			}
		return keysAll;
	}
	
	public static ArrayList<String> recursiveLists(int i, int j, ArrayList<String> keys, ArrayList<String> keys1) {
		
		if(j>=1 && i<keys.size()) {
			if(i == keys.size()-1) {
				keys1.add(keys.get(i));
				i = 0;
				j = j-1;
				keys1 = recursiveLists(i, j, keys, keys1);
			}else {
			keys1.add(keys.get(i));
			i = i+1;
			j = j-1;
			keys1 = recursiveLists(i, j, keys, keys1);
			}
		}	
		return keys1;
	}
	
	public static void CreateOutputFile(Document doc2, String path, String filename){
		try {
			/*String path = null;
			if(filename.contains("Merge")) {
				path = "Models/Common_Model/Merge/";
			}else if(filename.contains("Split")) {
				path = "Models/Common_Model/Split/";
			}*/
			
			//String pathF = path + filename + "/";
			//create the folder
		    File file = new File(path);
		    file.mkdir();
			
		    String filename_path = path + filename + ".xml";
			// write the content into xml file
			File xmlfile = new File(filename_path);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc2);
			StreamResult result = new StreamResult(xmlfile);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			
			//createFileMD5(xmlfile, filename_path);
		}catch(Exception e) {
			System.out.println(e);
		}
	}
}
