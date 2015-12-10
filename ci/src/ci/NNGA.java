package ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cicontest.algorithm.abstracts.AbstractRace;

public class NNGA {
	private int nIndividuals;
	private DefaultDriverGenome myDDG;
	private LinkedList<ArrayList<Double[][]>> weights;
	private LinkedList<ArrayList<Double[][]>> ranks;
	private Double mutationProb;

	public NNGA(DefaultDriverGenome myDDG, int nIndividuals, Double mutationProb){
		this.myDDG = myDDG;
	    this.nIndividuals = nIndividuals;
	    this.weights = myDDG.getMyNN().getLastTrainWeights();
	    this.mutationProb = mutationProb;
	    ranks=new LinkedList<ArrayList<Double[][]>>();
	}
	
	public Double GA(){
		Double bestRun = rank();
		mutate();
		myDDG.getMyNN().setWeights(ranks.get(0));
		return bestRun;
	}
	private Double rank() {
		Double lapTime[] = new Double[nIndividuals];
		int r1,r2;
		
		for (int i = 0; i < nIndividuals; i++){
			System.out.println("Start individual: " + Integer.toString(i+1));
			lapTime[i] = raceIndividual(weights.get(i));
			System.out.println("End individual: " + Integer.toString(i+1));
		}

		if (lapTime[0] < lapTime[1]){
			r1 = 0;
			r2 = 1;
		}else{
			r1 = 1;
			r2 = 0;
		}
		for (int i = 2; i < nIndividuals; i++){
			if (lapTime[i] < lapTime[r1]){
				r2 = r1;
				r1 = i;
			} else if(lapTime[i] < lapTime[r2]){
				r2 = i;
			}
		}
		ranks.clear();
		ranks.add(weights.get(r1));
		ranks.add(weights.get(r2));
		
		return lapTime[r1];
	}

	private void mutate() {
		weights.clear();
		for (int i = 0; i < ranks.size(); i++){
			for(int j = 0; j < nIndividuals/2; j++){
				weights.add(flipCoin(ranks.get(i),0.1D));
			}
		}		
	}
	private ArrayList<Double[][]> flipCoin(ArrayList<Double[][]> iweights, Double range) {
		ArrayList<Double[][]> oweights = new ArrayList<Double[][]>();
		Double delta = Math.random()*range;
		Double[][] w;
		for (int i = 0; i < iweights.size(); i++){
			oweights.add(iweights.get(i).clone());
		}
		
		for (int i = 0; i < oweights.size(); i++){
			if (Math.random() > mutationProb){
				w =  oweights.get(i);
				for (int j = 0; j < w.length; j++){
					for (int k = 0; k < w[0].length; k++){
						w[j][k] = w[j][k]+delta;
					}
				}
				oweights.set(i,w);
			}
		}
		return oweights;
	}
	private Double raceIndividual(ArrayList<Double[][]> iWeights){
		DefaultDriverGenome[] drivers = new DefaultDriverGenome[1];
		Double result = 0.0;
		myDDG.getMyNN().setWeights(iWeights);
     
		drivers[0] = myDDG;
	//	String path = "./scenarios";
		//File folder = new File(path);
	//	String[] fileNames = folder.list();
		//String[] trackNames = {"ole-road-1","g-track-3","g-track-2","g-track-1","ruudskogen"};
		for (int i = 0; i < 3; i++) {
			
		//	String s = fileNames[i];
			//copyFileUsingStream("./scenarios/"+s,"C:\\Program Files (x86)\\torcs\\config\\raceman\\quickrace.xml");
			DefaultRace race = new DefaultRace();
			Process p = startBat();
			
//			System.out.println("Start lap: " + Integer.toString(i+1));
			Double lapresult = race.runRace(drivers, true);
//			System.out.println("lap " + Integer.toString(i+1)+ ": " +  lapresult);
			result+= lapresult;
			
		}

		return result/5.0;
	}
	private Process startBat() {
		String pathScriptFile = "./textmode.bat";

        Process process = null;
		try {
			 process = new ProcessBuilder(pathScriptFile).start();
			 InputStream is = process.getInputStream();
		     InputStreamReader isr = new InputStreamReader(is);
		     BufferedReader br = new BufferedReader(isr);
		     String line;
		     
		    
		     while ((line = br.readLine()) != null) {
		    	// System.out.println(line);
		     }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return process;
	}
	private void setTrackinXML(String nameTrack, String categoryTrack, String filepath) {
        try 
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            // Get the root element
            Node quickRace = doc.getFirstChild();

            NodeList sections = doc.getElementsByTagName("section");
            for(int i = 0; i < sections.getLength(); i++)
            {
                for(int j = 0; j < sections.item(i).getAttributes().getLength(); j++)
                {
                    //System.out.println(sections.item(i).getAttributes().item(j));
                    //System.out.println(sections.item(i).getAttributes().item(j).getNodeName());
                    //System.out.println(sections.item(i).getAttributes().item(j).getNodeValue());
                    if(sections.item(i).getAttributes().item(j).getNodeValue().equals("Tracks"))
                    {
                        NodeList childs = sections.item(i).getChildNodes();
                        for(int k = 0; k < childs.getLength(); k++)
                        {
                            if(childs.item(k).getNodeName().equals("section"))
                            {
                                
                                NodeList trackProperties = childs.item(k).getChildNodes();
                                for(int l = 0; l < trackProperties.getLength(); l++)
                                {   
                                    if(trackProperties.item(l).getNodeName().equals("attstr"))
                                    {
                                        if(trackProperties.item(l).getAttributes().item(0).getNodeValue().equals("name"))
                                        {
                                            trackProperties.item(l).getAttributes().getNamedItem("val").setNodeValue(nameTrack);
                                        }
                                        else if( trackProperties.item(l).getAttributes().item(0).getNodeValue().equals("category"))
                                        {
                                            trackProperties.item(l).getAttributes().getNamedItem("val").setNodeValue(categoryTrack);
                                        }
                                    }

                                }
                            }
                            
                        }
    
                    }
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer;
            try {
                transformer = transformerFactory.newTransformer();
           
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(filepath));
                transformer.transform(source, result);
            } catch (TransformerException ex) {
                ex.printStackTrace();
            }
     

       } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
       } catch (IOException ioe) {
            ioe.printStackTrace();
       } catch (SAXException sae) {
            sae.printStackTrace();
       }
    }
	private static void copyFileUsingStream(String source, String dest){
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(new File(source));
	        os = new FileOutputStream(new File(dest));
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	        is.close();
	        os.close();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
	}
	
}
