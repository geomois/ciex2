package ci;

import java.util.ArrayList;

import cicontest.torcs.controller.Driver;
import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNetwork myNN = new NeuralNetwork();
	private Driver myTD;
    
    public NeuralNetwork getMyNN() {
        return myNN;
    }
	public void trainNN(ArrayList<ArrayList<Double>> input, ArrayList<ArrayList<Double>> output) {
		myNN.trainNetwork(input, output);
	}
	
	public double getNNValue(ArrayList<Double> input){
			Double[] temp=new Double[input.size()];
			return myNN.getValues(input.toArray(temp));
	}
	
	public void setDriver(Driver trainingDriver) {
		myTD = trainingDriver;
	}
	public Driver getDriver() {
		// TODO Auto-generated method stub
		return myTD;
	}
	
	public void loadSavedNN(){
		myNN=myNN.loadGenome();
	}
	
	public void saveNN(){
		myNN.storeGenome();
	}
	public void saveGA(int i) {
		myNN.storeGA();
		
	}
}

