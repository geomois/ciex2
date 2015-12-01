package ci;

import java.util.ArrayList;

import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNetwork myNN;
    private trainingDriver myTD;
    private ArrayList<double[]> inputs  = new ArrayList<double[]>();
    private ArrayList<double[]> outputs = new ArrayList<double[]>();
    
    public NeuralNetwork getMyNN() {
        return myNN;
    }
	public void trainNN() {
		myNN = new NeuralNetwork(inputs.get(0).length, 4, outputs.get(0).length);
		
		for (int e = 0; e < 400; e++){
			for(int i = 0; i < inputs.size(); i++){
				myNN.train(inputs.get(i), outputs.get(i));
			}
		}
		myNN.storeGenome();
	}
	
	public double[] getNNValue(double[] input){
			return myNN.getValues(input);
	}
	public void setDriver(trainingDriver trainingDriver) {
		myTD = trainingDriver;
		
	}
	public trainingDriver getDriver() {
		// TODO Auto-generated method stub
		return myTD;
	}
	public void addIO(ArrayList<double[]> in, ArrayList<double[]> out){
		for (int i = 0; i < in.size(); i++){
			this.inputs.add(in.get(i));
			this.outputs.add(out.get(i));
		}

		
	}
	public void loadSavedNN(){
		myNN=NeuralNetwork.loadGenome();
	}
}

