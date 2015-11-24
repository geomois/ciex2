package ci;
import java.io.*;
import java.util.ArrayList;

public class NeuralNetwork implements Serializable {

	// We could build it more abstractly in order to be able to add/remove nodes
	// and layers easily

	private ArrayList<Double> inputNodes;
	private ArrayList<Double> hiddenNodes;
	private ArrayList<Double> outputNodes;
	private ArrayList<Double> errorOutput;
	private ArrayList<Double> sumErrorDerivWeight;
	private Double[][] w1;
	private Double[][] w2;
	// Setting hidden nodes to be 2 can be related to acceleration and braking
	// Setting hidden nodes to be 3 can be related to turn right or left or go
	// straight
	private static int hiddenLNo = 2;
	private static int outputLNo = 2;
	private static double learningRate = 0.1;
	private static double bias = 1; // Activates the sigmoid

	public NeuralNetwork() {
		inputNodes = new ArrayList<Double>();
		hiddenNodes = new ArrayList<Double>();
		outputNodes = new ArrayList<Double>();
		errorOutput = new ArrayList<Double>();
		sumErrorDerivWeight = new ArrayList<Double>();
		for (int i = 0; i < hiddenLNo; i++) {
			hiddenNodes.add((double) 0);
			for (int j = 0; j < outputLNo; j++) {
				// Bias sumErrorDeriv will be calculated separately
				sumErrorDerivWeight.add((double) 0);
			}
		}
		for (int i = 0; i < outputLNo; i++) {
			outputNodes.add((double) 0);
			errorOutput.add((double) 0);
		}
	}

	private static final long serialVersionUID = -88L;

	public void trainNetwork(ArrayList<ArrayList<Double>> input, ArrayList<ArrayList<Double>> output) {
		// The +1 refers to the bias
		w1=initializeWeights(input.get(0).size() + 1, hiddenLNo, w1);
		w2=initializeWeights(hiddenLNo + 1, outputLNo, w2);
		for (int i = 0; i < input.size(); i++) {
			Double[] tempInput = new Double[input.get(i).size()];
			tempInput = input.get(i).toArray(tempInput);
			Double[] tempOutput = new Double[output.get(i).size()];
			tempOutput = output.get(i).toArray(tempOutput);
			train(tempInput, tempOutput);
		}
	}

	private void train(Double[] input, Double[] output) {
		inputNodes.clear();
		for (int i = 0; i < input.length; i++) {
			inputNodes.add(input[i]);
		}
		inputNodes.add(bias);
		forwardProp(input, output);
		backProp(input, output);
	}

	private ArrayList<Double> forwardProp(Double[] input, Double[] output) {
		for (int i = 0; i < hiddenLNo; i++) {
			for (int j = 0; j < inputNodes.size(); j++) {
				if (j == inputNodes.size() - 1) {
					// Bias term
					hiddenNodes.set(i, hiddenNodes.get(i) + w1[j][i] * bias);
				} else {
					hiddenNodes.set(i, hiddenNodes.get(i) + w1[j][i] * input[j]);
				}
			}
			double temp = hiddenNodes.get(i);
			hiddenNodes.set(i, (1 / (1 + Math.exp(temp))));
		}
		for (int i = 0; i < outputLNo; i++) {
			for (int j = 0; j < hiddenLNo + 1; j++) {
				if (j == hiddenLNo) {
					outputNodes.set(i, outputNodes.get(i) + w2[j][i] * bias);
				} else {
					outputNodes.set(i, outputNodes.get(i) + w2[j][i] * hiddenNodes.get(j));
				}
			}
			double temp = outputNodes.get(i);
			outputNodes.set(i, (1 / (1 + Math.exp(temp))));
			if(output!=null)
				errorOutput.set(i, output[i] - outputNodes.get(i));
		}
		return outputNodes;
	}

	private void backProp(Double[] input, Double[] output) {
		// newWeight=oldWeigth+learningRate*input*(output*(1-output))(derivative of sigmoid)*error
		for (int i = 0; i < outputLNo; i++) {
			// Assuming that all hidden nodes are connected to all the output nodes
			for (int j = 0; j < hiddenLNo + 1; j++) {
				double temp = outputNodes.get(i);
				if (j == hiddenLNo) {
					w2[j][i] = w2[j][i] + learningRate * bias * temp * (1 - temp) * errorOutput.get(i);
				} else {
					w2[j][i] = w2[j][i] + learningRate * hiddenNodes.get(j) * temp * (1 - temp) * errorOutput.get(i);
				}
				// Calculating �d_k*w_jk where d_k=g'*error
				// I am not sure if we should use the old or the updated weight
				// here I use the updated
				sumErrorDerivWeight.set(j,
						sumErrorDerivWeight.get(i) + (temp * (1 - temp) * errorOutput.get(i) * w2[j][i]));
			}
		}

		for (int i = 0; i < hiddenLNo; i++) {
			for (int j = 0; j < inputNodes.size(); j++) {
				double temp = hiddenNodes.get(i);
				if (j == inputNodes.size() - 1) {
					w1[j][i] = w1[j][i] + learningRate * bias * temp * (1 - temp) * sumErrorDerivWeight.get(i);
				} else {
					w1[j][i] = w1[j][i]
							+ learningRate * inputNodes.get(j) * temp * (1 - temp) * sumErrorDerivWeight.get(i);
				}
			}
		}
	}

	public Double[] getValues(Double[] input){
		if(w1!=null && w2!=null){
			Double[] temp=new Double[outputLNo];
			temp=this.forwardProp(input, null).toArray(temp);
			return temp;
		}else{
			return null;
		}
	}
	
	// Weight matrix= w[number inputs][number outputs]
	private Double[][] initializeWeights(int length, int length2, Double[][] w) {
		w = new Double[length][length2];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length2; j++) {
				w[i][j] = (double) Math.random();
			}
		}
		return w;
	}

	// Store the state of this neural network
	public void storeGenome() {
		ObjectOutputStream out = null;
		try {
			// create the memory folder manually
			// out = new ObjectOutputStream(new
			// FileOutputStream("/users/edwinlima/git/ci/memory/mydriver.mem"));
			out = new ObjectOutputStream(new FileOutputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem"));
			// out = new ObjectOutputStream(new FileOutputStream("E:\\eclipse
			// java\\eclipse workspace\\git\\ci\\memory\\mydriver.mem"));
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
			// f_in = new
			// FileInputStream("/users/edwinlima/git/ci/memory/mydriver.mem");
			f_in = new FileInputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem");
			// f_in = new FileInputStream("E:\\eclipse java\\eclipse
			// workspace\\git\\ci\\memory\\mydriver.mem");
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
