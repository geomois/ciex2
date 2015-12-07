package ci;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
			ArrayList<ArrayList<Double>> inputDataToTrain = new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>> outputDataToTrain = new ArrayList<ArrayList<Double>>();
			// init NN
			DefaultDriverGenome genome = new DefaultDriverGenome();
			genome.loadSavedNN();
			drivers[0] = genome;

			// Start a race
			DefaultRace race = new DefaultRace();
			for (int i = 0; i < 5; i++) {
				race.setTrack(AbstractRace.DefaultTracks.getTrack(0));
				race.laps = 1;
				// for speedup set withGUI to false
				results = race.runRace(drivers, true);
				inputDataToTrain.addAll(((DefaultDriver) drivers[0].getDriver()).getInput());
				outputDataToTrain.add(((DefaultDriver) drivers[0].getDriver()).speed());
			}
			saveToFile(inputDataToTrain,outputDataToTrain);
			// Save genome/nn
			DriversUtils.storeGenome(drivers[0]);
		}
		// create a checkpoint this allows you to continue this run later
		DriversUtils.createCheckpoint(this);
		// DriversUtils.clearCheckpoint();
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

	public void train() {
		// init NN
		int nTracks = 2;
		DefaultDriverGenome genome = new DefaultDriverGenome();
		drivers[0] = genome;
		// Start a race
		DefaultRace race = new DefaultRace();
		for (int i = 0; i < nTracks; i++) {
			race.setTrack(AbstractRace.DefaultTracks.getTrack(0));
			race.laps = 1;
			// for speedup set withGUI to false

			results = race.trainGenome(drivers, true);
//			drivers[0].trainNN(((trainingDriver) drivers[0].getDriver()).getInput(), ((trainingDriver) drivers[0].getDriver()).getOutput());
		}
		drivers[0].saveNN();
		// Save genome/nn
		DriversUtils.storeGenome(drivers[0]);
	}

	public static void main(String[] args) {

		// Set path to torcs.properties
		// TorcsConfiguration.getInstance().initialize(new
		// File("C:\\Users\\George\\git\\ci\\ci\\torcs.properties"));
		TorcsConfiguration.getInstance()
				.initialize(new File("E:\\eclipse java\\eclipse workspace\\ci\\torcs.properties"));
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
			algorithm.train();
		} else if (args.length > 0 && args[0].equals("-show-race")) {
			new DefaultRace().showBestRace();
		} else if (args.length > 0 && args[0].equals("-human")) {
			new DefaultRace().raceBest();
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

}