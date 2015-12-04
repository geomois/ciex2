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
	private Double[][] w1 = null;
	private Double[][] w2 = null;
	// Setting hidden nodes to be 2 can be related to acceleration and braking
	// Setting hidden nodes to be 3 can be related to turn right or left or go
	// straight
	private static int hiddenLNo = 8;
	private static int outputLNo = 1;
	private static int Adding = 1;
	private static double learningRate = 0.1;
	private static double bias = 1; // Activates the sigmoid
	private double maxSpeed;
	private Double Dmin;
	private Double Dmax;

	public NeuralNetwork() {
		inputNodes = new ArrayList<Double>();
		hiddenNodes = new ArrayList<Double>();
		outputNodes = new ArrayList<Double>();
		errorOutput = new ArrayList<Double>();
		sumErrorDerivWeight = new ArrayList<Double>();
		for (int i = 0; i < hiddenLNo; i++) {
			hiddenNodes.add((double) 0);
			for (int j = 0; j < outputLNo + Adding; j++) {
				// Bias sumErrorDeriv will be calculated separately
				sumErrorDerivWeight.add((double) 0);
			}
		}
		for (int i = 0; i < outputLNo; i++) {
			outputNodes.add((double) 0);
			errorOutput.add((double) 0);
		}
		Dmin = 0.0;
		Dmax = 0.0;
	}

	private static final long serialVersionUID = -88L;

	public void trainNetwork(ArrayList<ArrayList<Double>> input, ArrayList<ArrayList<Double>> output) {
		if (w1 == null && w2 == null) {
			// The +1 refers to the bias
			w1 = initializeWeights(input.get(0).size() + 1, hiddenLNo, w1);
			w2 = initializeWeights(hiddenLNo + 1, outputLNo, w2);
		}

		for (int i = 0; i < input.size(); i++) {
			Double[] tempOutput = new Double[output.get(i).size()];
			tempOutput = output.get(i).toArray(tempOutput);
			MinMax(tempOutput);
		}

		for (int i = 0; i < input.size(); i++) {
			Double[] tempInput = new Double[input.get(i).size()];
			tempInput = input.get(i).toArray(tempInput);
			Double[] tempOutput = new Double[output.get(i).size()];
			tempOutput = output.get(i).toArray(tempOutput);

			train(tempInput, scale(tempOutput, -1.0, 1.0, Dmin, Dmax));
//			train(tempInput, tempOutput);
		}
		outputNodes.clear();
		hiddenNodes.clear();
		inputNodes.clear();
		sumErrorDerivWeight.clear();
	}

	private void train(Double[] input, Double[] output) {
		inputNodes.clear();
		hiddenNodes.clear();
		outputNodes.clear();
		for (int i = 0; i < input.length; i++) {
			inputNodes.add(input[i]);
		}
		for (int i = 0; i < hiddenLNo; i++) {
			hiddenNodes.add((double) 0);
			for (int j = 0; j < outputLNo + Adding; j++) {
				// Bias sumErrorDeriv will be calculated separately
				sumErrorDerivWeight.add((double) 0);
			}
		}
		for (int i = 0; i < outputLNo; i++)
			outputNodes.add((double) 0);
		inputNodes.add(bias);
		forwardProp(input, output);
		for (int i = 0; i < 100; i++) {
			backProp(input, output);
		}
	}

	private ArrayList<Double> forwardProp(Double[] input, Double[] output) {

		for (int i = 0; i < hiddenLNo; i++) {
			for (int j = 0; j < inputNodes.size(); j++) {
				if (j == inputNodes.size() - 1) {
					hiddenNodes.set(i, hiddenNodes.get(i) + w1[j][i] * bias);
				} else {
					hiddenNodes.set(i, hiddenNodes.get(i) + w1[j][i] * input[j]);
				}
			}

			double temp = (1.0 / (1.0 + Math.exp(-hiddenNodes.get(i))));
			hiddenNodes.set(i, temp);
		}
		for (int i = 0; i < outputLNo; i++) {
			for (int j = 0; j < hiddenLNo + 1; j++) {
				if (j == hiddenNodes.size()) {
					outputNodes.set(i, outputNodes.get(i) + w2[j][i] * bias);
				} else {
					outputNodes.set(i, outputNodes.get(i) + w2[j][i] * hiddenNodes.get(j));
				}
			}
			double temp1 = (1.0 / (1.0 + Math.exp(-outputNodes.get(i))));
			outputNodes.set(i, temp1);
		}
		return outputNodes;
	}

	private void backProp(Double[] input, Double[] output) {
		for (int i = 0; i < outputLNo; i++) {
			if (output != null) {
				double temp2 = outputNodes.get(i);
				// TODO:investigate error
				errorOutput.set(i, temp2 * (1.0D - temp2) * (output[i] - temp2));

//				 temp2=(1.0 / (1.0 + Math.exp(-output[i])));
//				 errorOutput.set(i, temp2 * (1.0D - temp2) * (output[i] -
//				 temp2));
			}
			// newWeight=oldWeigth+learningRate*input*(output*(1-output))(derivative
			// of sigmoid)*error
			for (int j = 0; j < hiddenLNo + 1; j++) {
				double temp = outputNodes.get(i);
				if (j == hiddenLNo) {
					w2[j][i] = w2[j][i] + learningRate * bias * temp * (1.0 - temp) * errorOutput.get(i);
				} else {
					w2[j][i] = w2[j][i] + learningRate * hiddenNodes.get(j) * temp * (1.0 - temp) * errorOutput.get(i);
				}
				// Calculating �d_k*w_jk where d_k=g'*error
				// I am not sure if we should use the old or the updated weight
				// here I use the updated
				sumErrorDerivWeight.set(j,
						sumErrorDerivWeight.get(i) + (temp * (1.0 - temp) * errorOutput.get(i) * w2[j][i]));
			}
		}

		for (int i = 0; i < hiddenLNo; i++) {
			for (int j = 0; j < inputNodes.size(); j++) {
				double temp1 = hiddenNodes.get(i);
				if (j == inputNodes.size() - 1) {
					w1[j][i] = w1[j][i] + learningRate * bias * temp1 * (1.0 - temp1) * sumErrorDerivWeight.get(i);
				} else {
					w1[j][i] = w1[j][i]
							+ learningRate * inputNodes.get(j) * temp1 * (1.0 - temp1) * sumErrorDerivWeight.get(i);
				}
			}
		}
	}

	public double getMaxSpeed() {
		return Dmax;
	}

	private void MinMax(Double[] input) {
		for (int i = 0; i < input.length; i += 2) {
			if (input[i] < Dmin) {
				Dmin = input[i];
			}
			if (input[i] > Dmax) {
				Dmax = input[i];
			}
		}
	}

	private Double[] scale(Double[] input, double min, double max, double Dmin, double Dmax) {
		Double[] out = input.clone();

		for (int i = 0; i < input.length; i += 2) {
			out[i] = min + (max - min) * (input[i] - Dmin) / (Dmax - Dmin);
		}
		return out;
	}

	public Double[] getValues(Double[] input) {
		if (w1 != null && w2 != null) {
			Double[] temp = new Double[outputLNo];
			inputNodes.clear();
			hiddenNodes.clear();
			outputNodes.clear();
			for (int i = 0; i < input.length; i++) {
				inputNodes.add(input[i]);
			}
			for (int i = 0; i < hiddenLNo; i++) {
				hiddenNodes.add((double) 0);
				for (int j = 0; j < outputLNo + Adding; j++) {
					// Bias sumErrorDeriv will be calculated separately
					sumErrorDerivWeight.add((double) 0);
				}
			}
			for (int i = 0; i < outputLNo; i++)
				outputNodes.add((double) 0);
			inputNodes.add(bias);
			temp = this.forwardProp(input, null).toArray(temp);
			Double t = scale(temp, Dmin, Dmax, -1.0, 1.0)[0];
			temp[0] = scale(temp, Dmin, Dmax, -1.0, 1.0)[0];
			// temp[1] = scale(temp,,)[0];
			return temp;
		} else {
			return null;
		}
	}

	// Weight matrix= w[number inputs][number outputs]
	private Double[][] initializeWeights(int length, int length2, Double[][] w) {
		w = new Double[length][length2];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length2; j++) {
				w[i][j] = (double) Math.random() - 0.5;
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
			// out = new ObjectOutputStream(new
			// FileOutputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem"));
			out = new ObjectOutputStream(
					new FileOutputStream("C:\\Users\\11126957\\git\\ciex2\\ci\\memory\\mydriver.mem"));
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
			// f_in = new
			// FileInputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem");
			// f_in = new
			// FileInputStream("C:\\Users\\11126957\\Desktop\\memory\\mydriver.mem");
			f_in = new FileInputStream("C:\\Users\\11126957\\git\\ciex2\\ci\\memory\\mydriver.mem");
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
