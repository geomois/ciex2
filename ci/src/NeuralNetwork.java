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
	private static int outputLNo=1; //DO NOT CHANGE VALUE =1
	private static double learningRate=0.1;
	private static double bias=0;
	
	public NeuralNetwork(){ 
		inputNodes=new ArrayList<Double>();
		hiddenNodes=new ArrayList<Double>();
		outputNodes=new ArrayList<Double>();
		error=new ArrayList<Double>();
		for(int i=0; i<hiddenLNo;i++){
			hiddenNodes.add((double) 0);
		}
		for(int i=0; i<outputLNo;i++){
			outputNodes.add((double) 0);
			error.add((double) 0);
		}
	}
	
	private static final long serialVersionUID = -88L;
	
	public void trainNetwork(ArrayList<ArrayList<Double>> input,ArrayList<Double> output) {
		for(int i=0;i<input.size();i++){
			Double[] tempInput=new Double[input.get(i).size()];
			tempInput=input.get(i).toArray(tempInput);
			Double[] tempOutput=new Double[1];
			tempOutput[0]=output.get(i);
			train(tempInput,tempOutput);
		}
	}
	
	private void train(Double[] input, Double[] output){
//		The +1 refers to the bias
		for(int i=0;i<input.length+1;i++){
			inputNodes.add(input[i]);
		}
		initializeWeights(input.length+1,hiddenLNo,w1);
		initializeWeights(hiddenLNo+1,outputLNo,w2);
		forwardProp(input,output);
		backProp(input,output);
	}
	
	private void forwardProp(Double[] input, Double[] output) {
		for(int i=0; i<hiddenLNo;i++){
			for(int j=0;j<inputNodes.size()+1;j++){
				if (j==inputNodes.size()-1) {
//					Bias term
					hiddenNodes.set(i, hiddenNodes.get(i) + w1[j][i] * bias);
				}else{
					hiddenNodes.set(i, hiddenNodes.get(i) + w1[j][i] * input[j]);
				}
			}
			double temp=hiddenNodes.get(i);
			hiddenNodes.set(i, (1/(1+Math.exp(temp))));
		}
		for(int i=0; i<outputLNo;i++){
			for(int j=0;j<hiddenLNo+1;j++){
				if (j==hiddenLNo) {
					outputNodes.set(i, outputNodes.get(i) + w2[j][i] * bias);
				}else{
					outputNodes.set(i, outputNodes.get(i) + w2[j][i] * input[j]);
				}
			}
			double temp=outputNodes.get(i);
			outputNodes.set(i, (1/(1+Math.exp(temp))));
			error.set(i, output[i]-outputNodes.get(i));
		}
	}
	
	private void backProp(Double[] input, Double[] output) {
//		newWeight=oldWeight+ learningRate*input*output*(1-output)*error
		for(int i=0; i<outputLNo;i++){
//			Assuming that all hidden nodes are connected to all the output nodes
			for(int j=0;j<hiddenLNo+1;j++){
				double temp=outputNodes.get(i);
				if (j==hiddenLNo) {
					w2[j][i] = w2[j][i] + learningRate * bias * temp * (1 - temp) * error.get(i);
				}else{
					w2[j][i] = w2[j][i] + learningRate * hiddenNodes.get(j) * temp * (1 - temp) * error.get(i);
				}
			}
		}
		for(int i=0; i<hiddenLNo;i++){
			for(int j=0;j<inputNodes.size()+1;j++){
				double temp=hiddenNodes.get(i);
				if (j==inputNodes.size()-1) {
					w1[j][i] = w1[j][i] + learningRate * bias * temp * (1 - temp) * error.get(i);
				}else{
					w1[j][i] = w1[j][i] + learningRate * inputNodes.get(j) * temp * (1 - temp) * error.get(i);
				}
			}
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
			out = new ObjectOutputStream(new FileOutputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem"));
//			out = new ObjectOutputStream(new FileOutputStream("E:\\eclipse java\\eclipse workspace\\git\\ci\\memory\\mydriver.mem"));
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
			 f_in = new FileInputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem");
//			 f_in = new FileInputStream("E:\\eclipse java\\eclipse workspace\\git\\ci\\memory\\mydriver.mem");
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
