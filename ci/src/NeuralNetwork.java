import java.io.*;
import java.util.ArrayList;

public class NeuralNetwork implements Serializable {

//	We could build it more abstractly in order to be able to add/remove nodes and layers easily
	
	
	private ArrayList<Double> inputNodes;
	private ArrayList<Double> hiddenNodes;
	private ArrayList<Double> outputNodes;
	private ArrayList<Double> error;
	private double[][] w1;
	private double[][] w2;
//	Setting hidden nodes to be 2 can be related to acceleration and braking
//	Setting hidden nodes to be 3 can be related to turn right or left or go straight
	private static int hiddenLNo=2;
	private static int outputLNo=1;
	private static double learningRate=0.25;
	
	NeuralNetwork(){ 
		inputNodes=new ArrayList<Double>();
		hiddenNodes=new ArrayList<Double>();
		outputNodes=new ArrayList<Double>();
		for(int i=0; i<hiddenLNo;i++){
			hiddenNodes.add((double) 0);
		}
		for(int i=0; i<outputLNo;i++){
			outputNodes.add((double) 0);
			error.add(0.0);
		}
	}
	
	private static final long serialVersionUID = -88L;

	public void train(double[] input, double[] output){
		for(int i=0;i<input.length;i++){
			inputNodes.add(input[i]);
		}
		initializeWeights(input.length,hiddenLNo,w1);
		initializeWeights(hiddenLNo,outputLNo,w2);
		forwardProp(input,output);
		backProp(input,output);
	}
	
	private void forwardProp(double[] input, double[] output) {
		for(int i=0; i<hiddenLNo;i++){
			for(int j=0;j<inputNodes.size();j++){
				hiddenNodes.set(i, hiddenNodes.get(i)+w1[j][i]*input[j]);
			}
			hiddenNodes.set(i, (1/(1+Math.exp(hiddenNodes.get(i)))));
		}
		for(int i=0; i<outputLNo;i++){
			for(int j=0;j<hiddenLNo;j++){
				outputNodes.set(i, outputNodes.get(i)+w2[j][i]*input[j]);
			}
			outputNodes.set(i, (1/(1+Math.exp(outputNodes.get(i)))));
			error.set(i, output[i]-outputNodes.get(i));
		}
	}
	
	private void backProp(double[] input, double[] output) {
//		newWeight=oldWeight+ learningRate*input*output*(1-output)*error
		for(int i=0; i<outputLNo;i++){
//			Assuming that all hidden nodes are connected to all the output nodes
			for(int j=0;j<hiddenLNo;j++){
				double temp=outputNodes.get(i);
				w2[j][i]=w2[j][i]+hiddenNodes.get(j)*temp*(1-temp)*error.get(i);
			}
		}
		for(int i=0; i<hiddenLNo;i++){
			for(int j=0;j<inputNodes.size();j++){
				double temp=hiddenNodes.get(i);
				w1[j][i]=w2[j][i]+inputNodes.get(j)*temp*(1-temp)*error.get(i);
				hiddenNodes.set(i, hiddenNodes.get(i)+w1[j][i]*input[j]);
			}
			hiddenNodes.set(i, (1/(1+Math.exp(hiddenNodes.get(i)))));
		}
	}
// Weight matrix= w[number inputs][number outputs]
	private void initializeWeights(int length,int length2,double[][] w){
		for(int i=0;i<length;i++){
			for(int j=0;j<length2;j++){
				w[i][j]=(double) Math.random();
			}
		}
	}
	//Store the state of this neural network
	public void storeGenome() {
		ObjectOutputStream out = null;
		try {
			//create the memory folder manually
//			out = new ObjectOutputStream(new FileOutputStream("/users/edwinlima/git/ci/memory/mydriver.mem"));
			out = new ObjectOutputStream(new FileOutputStream("E:\\eclipse java\\eclipse workspace\\git\\ci\\memory\\mydriver.mem"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Load a neural network from memory
	 public NeuralNetwork loadGenome() {

		 // Read from disk using FileInputStream
		 FileInputStream f_in = null;
		 try {
//			 f_in = new FileInputStream("/users/edwinlima/git/ci/memory/mydriver.mem");
			 f_in = new FileInputStream("E:\\eclipse java\\eclipse workspace\\git\\ci\\memory\\mydriver.mem");
		 } catch (FileNotFoundException e) {
			 e.printStackTrace();
		 }

		 // Read object using ObjectInputStream
		 ObjectInputStream obj_in = null;
		 try {
			 obj_in = new ObjectInputStream(f_in);
		 } catch (IOException e) {
			 e.printStackTrace();
		 }

		 // Read an object
		 try {
			 return (NeuralNetwork) obj_in.readObject();
		 } catch (IOException e) {
			 e.printStackTrace();
		 } catch (ClassNotFoundException e) {
			 e.printStackTrace();
		 }
		 return null;
	 }
	}
