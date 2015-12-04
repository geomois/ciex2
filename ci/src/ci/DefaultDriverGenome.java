package ci;

import java.util.ArrayList;

import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNetwork myNN = new NeuralNetwork(19,4,2);
	private trainingDriver myTD;
    
    public NeuralNetwork getMyNN() {
        return myNN;
    }
	public void trainNN(ArrayList<ArrayList<Double>> input, ArrayList<ArrayList<Double>> output) {
		myNN.train(input, output);
	}
	
	public Double[] getNNValue(ArrayList<Double> input){
			Double[] temp=new Double[input.size()];
			temp=myNN.getValues(input.toArray(temp));
			return temp;
	}
	
	public void setDriver(trainingDriver trainingDriver) {
		myTD = trainingDriver;
	}
	public trainingDriver getDriver() {
		// TODO Auto-generated method stub
		return myTD;
	}
	
	public void loadSavedNN(){
		myNN=myNN.loadGenome();
	}
	
	public void saveNN(){
		myNN.storeGenome();
	}
}

