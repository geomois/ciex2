package ci;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNetwork myNN;
    private trainingDriver myTD;
    private ArrayList<double[]> inputs  = new ArrayList<double[]>();
    private ArrayList<double[]> outputs = new ArrayList<double[]>();
	private int nTracks = 0;
    
    public void numTracksStored(){
    	System.out.println("Tracks stored: " + nTracks );
    }
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
	public void storeGenome() {
		
		ObjectOutputStream out = null;
		try {
			// create the memory folder manually
			// out = new ObjectOutputStream(new
			// FileOutputStream("/users/edwinlima/git/ci/memory/mydriver.mem"));
			out = new ObjectOutputStream(new FileOutputStream("C:/yoel/java/ciex2/ci/memory/genome.mem"));
//			 out = new ObjectOutputStream(new FileOutputStream("E:\\eclipse
//			 java\\eclipse workspace\\git\\ci\\memory\\mydriver.mem"));
			//out = new ObjectOutputStream(new FileOutputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			nTracks++;
			out.writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static DefaultDriverGenome loadGenome() {

		// Read from disk using FileInputStream
		FileInputStream f_in = null;
		ObjectInputStream obj_in = null;
		try {

        	f_in = new FileInputStream("C:/yoel/java/ciex2/ci/memory/genome.mem");
        	obj_in = new ObjectInputStream(f_in);
        	return (DefaultDriverGenome) obj_in.readObject();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}

