package ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.util.LinkedList;

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
	}
	public Double GA(){
		Double bestRun = rank();
		mutate();
		return bestRun;
	}
	private Double rank() {
		Double lapTime[] = new Double[nIndividuals];
		int r1,r2;
		
		for (int i = 0; i < nIndividuals; i++){
			lapTime[i] = raceIndividual(weights.get(i));
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
	private Double raceIndividual(ArrayList<Double[][]> iWeights) {
		DefaultDriverGenome[] drivers = new DefaultDriverGenome[1];
		Double result;
		myDDG.getMyNN().setWeights(iWeights);

		drivers[0] = myDDG;
		startBat();
		DefaultRace race = new DefaultRace();
		race.setTrack(AbstractRace.DefaultTracks.getTrack(0));
		race.laps = 1;
		// for speedup set withGUI to false
		result = race.runRace(drivers, true);
        System.out.println(result);
		
		return result;
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
