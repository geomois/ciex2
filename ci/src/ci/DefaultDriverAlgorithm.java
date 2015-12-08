package ci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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

import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import race.TorcsConfiguration;

public class DefaultDriverAlgorithm extends AbstractAlgorithm {

	private static final long serialVersionUID = 654963126362653L;

	DefaultDriverGenome[] drivers = new DefaultDriverGenome[1];
	int[] results = new int[1];

	public Class<? extends Driver> getDriverClass() {
		return DefaultDriver.class;
	}

	public void run(boolean continue_from_checkpoint) {
		if (!continue_from_checkpoint) {
			// init NN
			DefaultDriverGenome genome = new DefaultDriverGenome();
			genome.loadSavedNN();
			drivers[0] = genome;

			// Start a race
			setTrackinXML("alpine-1","road",".\\scenarios\\sc1.xml");
			startBat();
			DefaultRace race = new DefaultRace();
			race.setTrack(AbstractRace.DefaultTracks.getTrack(0));
			race.laps = 1;
			// for speedup set withGUI to false
			Double result = race.runRace(drivers, true);

			// Save genome/nn
			DriversUtils.storeGenome(drivers[0]);
		}
		// create a checkpoint this allows you to continue this run later
		DriversUtils.createCheckpoint(this);
		// DriversUtils.clearCheckpoint();
	}

	public void train(boolean file) {
		DefaultDriverGenome genome = new DefaultDriverGenome();
		drivers[0] = genome;
		ArrayList<ArrayList<Double>> inputDataToTrain = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> outputDataToTrain = new ArrayList<ArrayList<Double>>();
		// Start a race
		DefaultRace race = new DefaultRace();
		if (!file) {
			int nTracks = 1;
			for (int i = 0; i < nTracks; i++) {
				race.setTrack(AbstractRace.DefaultTracks.getTrack(0));
				race.laps = 1;
				// for speedup set withGUI to false

				results = race.trainGenome(drivers, true);
				inputDataToTrain.addAll(((trainingDriver) drivers[0].getDriver()).getInput());
				outputDataToTrain.addAll(((trainingDriver) drivers[0].getDriver()).getOutput());
			}
			drivers[0].trainNN(inputDataToTrain, outputDataToTrain);
			saveToFile(inputDataToTrain, outputDataToTrain);
		} else {
			ArrayList<ArrayList<Double>> temp = new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>> output = new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>> input = new ArrayList<ArrayList<Double>>();
			// String path = "C:/Users/George/Desktop/ci train data";
			String path = "C:/Users/11126957/Desktop/ci train data";
			File folder = new File(path);
			String[] fileNames = folder.list();

			for (String name : fileNames) {
				temp = parseTrainingDataFromFile(path + "\\" + name);
				for (int i = 0; i < temp.size(); i++) {
					if (i % 2 == 1) {
						output.add(temp.get(i));
					} else {
						input.add(temp.get(i));
					}
				}
			}
			drivers[0].trainNN(input, output);
		}
		drivers[0].saveNN();
		// Save genome/nn
		DriversUtils.storeGenome(drivers[0]);
	}

	private ArrayList<ArrayList<Double>> parseTrainingDataFromFile(String path) {
		BufferedReader br = null;
		String CurrentLine;
		ArrayList<ArrayList<Double>> readOut = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> temp = new ArrayList<Double>();

		try {
			br = new BufferedReader(new FileReader(path));

			try {
				while ((CurrentLine = br.readLine()) != null) {
					if (!CurrentLine.isEmpty()) {
						String[] numbers = CurrentLine.split("\\s+");
						temp = new ArrayList<Double>();
						for (String s : numbers)
							temp.add(Double.parseDouble(s.trim()));
						readOut.add(temp);
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		return readOut;
	}

	public static void main(String[] args) {

		// Set path to torcs.properties
		// TorcsConfiguration.getInstance().initialize(new
		// File("C:/Users/George/git/ciex2/ci/torcs.properties"));
		TorcsConfiguration.getInstance()
				.initialize(new File("C:\\yoel\\java\\ciex2\\ci\\torcs.properties"));
		/*
		 *
		 * Start without arguments to run the algorithm Start with -train train
		 * NN Start with -continue to continue a previous run Start with -show
		 * to show the best found Start with -show-race to show a race with 10
		 * copies of the best found Start with -human to race against the best
		 * found
		 *
		 */
		DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();
		DriversUtils.registerMemory(algorithm.getDriverClass());
		if (args.length > 0 && args[0].equals("-show")) {
			new DefaultRace().showBest();
		} else if (args.length > 0 && args[0].equals("-train")) {
			algorithm.train(false);
		} else if (args.length > 0 && args[0].equals("-trainfromfile")) {
			algorithm.train(true);
//		} else if (args.length > 0 && args[0].equals("-traintofile")) {
//			algorithm.();
		} else if (args.length > 0 && args[0].equals("-show-race")) {
			new DefaultRace().showBestRace();
		} else if (args.length > 0 && args[0].equals("-human")) {
			new DefaultRace().raceBest();
		} else if (args.length > 0 && args[0].equals("-methedriver")) {
			algorithm.meRun();
		} else if (args.length > 0 && args[0].equals("-continue")) {
			if (DriversUtils.hasCheckpoint()) {
				DriversUtils.loadCheckpoint().run(true);
			} else {
				algorithm.run();
			}
		} else {
			algorithm.run();
		}
	}

//	private void runWithAccel() {
//		// init NN
//		DefaultDriverGenome genome = new DefaultDriverGenome();
//		genome.loadSavedNN();
//		drivers[0] = genome;
//
//		// Start a race
//		DefaultRace race = new DefaultRace();
//		race.setTrack(AbstractRace.DefaultTracks.getTrack(0));
//		race.laps = 1;
//		// for speedup set withGUI to false
//		results = race.runWithAccel(drivers, true);
//
//		// Save genome/nn
//		DriversUtils.storeGenome(drivers[0]);
//		// create a checkpoint this allows you to continue this run later
//		DriversUtils.createCheckpoint(this);
//		// DriversUtils.clearCheckpoint();
//
//	}

	private void meRun() {
		DefaultDriverGenome genome = new DefaultDriverGenome();
		drivers[0] = genome;
		ArrayList<ArrayList<Double>> inputDataToTrain = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> outputDataToTrain = new ArrayList<ArrayList<Double>>();
		// Start a race
		DefaultRace race = new DefaultRace();
		int nTracks =10;
		BufferedReader br = null;
		for (int i = 0; i < nTracks; i++) {
			race.setTrack(AbstractRace.DefaultTracks.getTrack(0));
			race.laps = 1;
			results = race.meRunRace(drivers, true);
			try {
				br = new BufferedReader(new InputStreamReader(System.in));
				String s;
				System.out.println("Save? :");
				while ((s = br.readLine()) == null) {
					System.out.println("Save? :");
				}
				if (s.equals("y")) {
					inputDataToTrain.addAll(((MeTheDriver) drivers[0].getDriver()).getInput());
					outputDataToTrain.addAll(((MeTheDriver) drivers[0].getDriver()).getOutput());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		saveToFile(inputDataToTrain,outputDataToTrain);
		drivers[0].trainNN(inputDataToTrain, outputDataToTrain);

		drivers[0].saveNN();
		// Save genome/nn
		DriversUtils.storeGenome(drivers[0]);
		System.exit(0);
	}
	
	private void saveToFile(ArrayList<ArrayList<Double>> input, ArrayList<ArrayList<Double>> speed) {
		try {
			FileWriter fw;
			Calendar cal = Calendar.getInstance();
	        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");

			File file = new File("C:/Users/11126957/Desktop/ci train data/traindata"+sdf.format(cal.getTime()).toString()+".txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				fw = new FileWriter(file.getAbsoluteFile());
			} else {
				fw = new FileWriter(file.getAbsoluteFile(), true);
			}

			BufferedWriter bw = new BufferedWriter(fw);

			for (int i=0;i<input.size();i++) {
				for (Double a : input.get(i)) {
					bw.write(a.toString() + " ");
				}
				bw.write("\n");
				bw.write(speed.get(i).get(0).toString());
//				bw.write(steering.get(i).toString() + " ");
				bw.write("\n");
			}


			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
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
	private void startBat() {
		String pathScriptFile = new File("").getAbsolutePath() + "\\textmode.bat";

        Process process;
		try {
			 process = new ProcessBuilder(pathScriptFile).start();
			 InputStream is = process.getInputStream();
		     InputStreamReader isr = new InputStreamReader(is);
		     BufferedReader br = new BufferedReader(isr);
		     String line;
		    
		     while ((line = br.readLine()) != null) {
		    	 System.out.println(line);
		     }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}