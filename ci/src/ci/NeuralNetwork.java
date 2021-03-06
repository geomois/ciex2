package ci;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class NeuralNetwork implements Serializable {

	// We could build it more abstractly in order to be able to add/remove nodes
	// and layers easily

//	private ArrayList<Double> inputNodes;
	private ArrayList<Double> hiddenNodes;
	private ArrayList<Double> outputNodes;
	private ArrayList<Double> errorOutput;
	private ArrayList<Double> sumErrorDerivWeight;
	private Double[][] w1 = null;
	private Double[][] w2 = null;
	// Setting hidden nodes to be 2 can be related to acceleration and braking
	// Setting hidden nodes to be 3 can be related to turn right or left or go
	// straight
	private static int hiddenLNo = 4;
	private static int outputLNo = 1;
	private static int Adding = 1;
	private static double learningRate = 0.1;
	private static double bias = 1; // Activates the sigmoid
	private Double Dmin;
	private Double Dmax;
	private LinkedList<ArrayList<Double[][]>> lastTrainedWeights;
	private int inputSize = 11;

	public NeuralNetwork() {
//		inputNodes = new ArrayList<Double>(inputSize + 1);
		hiddenNodes = new ArrayList<Double>(hiddenLNo);
		outputNodes = new ArrayList<Double>(outputLNo);
		errorOutput = new ArrayList<Double>(outputLNo);
		sumErrorDerivWeight = new ArrayList<Double>(hiddenLNo * (outputLNo + Adding));
		this.lastTrainedWeights = new LinkedList<ArrayList<Double[][]>>();
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

		Double[][] tempInput = new Double[input.size()][input.get(0).size()];
		Double[][] tempOutput = new Double[output.size()][output.get(0).size()];
		for (int i = 0; i < input.size(); i++) {
			tempInput[i] = input.get(i).toArray(tempInput[i]);
			tempOutput[i] = output.get(i).toArray(tempOutput[i]);
		}

		for (int j = 0; j < 300; j++) {
			for (int i = 0; i < input.size(); i++) {
				train(tempInput[i], scale(tempOutput[i], 0.0, 1.0, Dmin, Dmax));
				// train(tempInput, tempOutput);
			}

			// if(lastTrainedWeights.size()==50 &&
			// !(isLearning(w1,input.get(0).size() + 1,hiddenLNo) ||
			// isLearning(w2,hiddenLNo + 1,outputLNo))){
			// System.out.println(j);
			// break;
			// }

			if (lastTrainedWeights.size() == 10) {
				lastTrainedWeights.remove();
			}
			ArrayList<Double[][]> temp = new ArrayList<Double[][]>();
			temp.add(w1);
			temp.add(w2);
			lastTrainedWeights.add(temp);
		}
	}

	private boolean isLearning(Double[][] w, int a, int b) {
		for (int i = 0; i < a; i++) {
			for (int j = 0; j < b; j++) {
				if (b == hiddenLNo) {
					if (w[i][j] != lastTrainedWeights.getLast().get(0)[i][j])
						return true;
					else
						return false;
				} else {
					if (w[i][j] != lastTrainedWeights.getLast().get(1)[i][j])
						return true;
					else
						return false;
				}
			}
		}
		return false;
	}

	private void train(Double[] input, Double[] output) {
//		inputNodes = new ArrayList<Double>(inputSize + 1);
		hiddenNodes = new ArrayList<Double>(hiddenLNo);
		outputNodes = new ArrayList<Double>(outputLNo);
		sumErrorDerivWeight = new ArrayList<Double>(hiddenLNo * (outputLNo + Adding));
//		for (int i = 0; i < input.length - 1; i++) {
//			inputNodes.add(  input[i]);
//		}
//		inputNodes.set(input.length - 1, bias);
		for (int i = 0; i < hiddenLNo; i++) {
			hiddenNodes.add( (double) 0);
			for (int j = 0; j < outputLNo + Adding; j++) {
				// Bias sumErrorDeriv will be calculated separately
				sumErrorDerivWeight.add( (double) 0);
			}
		}
		for (int i = 0; i < outputLNo; i++)
			outputNodes.add(  (double) 0);

		forwardProp(input);
		backProp(input, output);
	}
	private ArrayList<Double> forwardProp(Double[] input) {
		for (int i = 0; i < hiddenLNo; i++) {
			for (int j = 0; j < inputSize+1; j++) {
				if (j == inputSize) {
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
//			double temp1 = (1.0 / (1.0 + Math.exp(-outputNodes.get(i))));
			outputNodes.set(i, (1.0 / (1.0 + Math.exp(-outputNodes.get(i)))));
		}
		return outputNodes;
	}

	private void backProp(Double[] input, Double[] output) {
		double sum = 0.0;

		for (int i = 0; i < outputLNo; i++) {
			double temp2 = outputNodes.get(i);
			// errorOutput.set(i, output[i] - temp2);
			errorOutput.set(i, temp2 * (1.0 - temp2) * (output[i] - temp2));
		}
		for (int i = 0; i < hiddenLNo + 1; i++) {
			for (int j = 0; j < outputLNo; j++)
				sum += w2[i][j] * errorOutput.get(j);

			if (i != hiddenLNo) {
				sumErrorDerivWeight.set(i, hiddenNodes.get(i) * (1.0 - hiddenNodes.get(i)) * sum);
			} else {
				sumErrorDerivWeight.set(i, bias * sum);
			}
		}

		for (int i = 0; i < outputLNo; i++) {
			for (int j = 0; j < hiddenLNo + 1; j++) {
				if (j == hiddenLNo) {
					w2[j][i] = w2[j][i] + learningRate * bias * errorOutput.get(i);
				} else {
					w2[j][i] = w2[j][i] + learningRate * hiddenNodes.get(j) * errorOutput.get(i);
				}
			}
		}

		for (int i = 0; i < hiddenLNo; i++) {
			for (int j = 0; j < inputSize+1; j++) {
				if (j == inputSize) {
					w1[j][i] = w1[j][i] + learningRate * bias * sumErrorDerivWeight.get(i);
				} else {
					w1[j][i] = w1[j][i] + learningRate * input[j] * sumErrorDerivWeight.get(i);
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

	private double deScale(double input, double min, double max, double Dmin, double Dmax) {
		return (min + (max - min) * (input - Dmin) / (Dmax - Dmin));
	}

	public double getValues(Double[] input) {
		if (w1 != null && w2 != null) {
			double temp = 0.0;
			hiddenNodes = new ArrayList<Double>(hiddenLNo);
			outputNodes = new ArrayList<Double>(outputLNo);
			for (int i = 0; i < hiddenLNo; i++) {
				hiddenNodes.add((double) 0);
			}
			outputNodes.add((double) 0);

			temp = this.forwardProp(input).get(0);
			temp = deScale(temp, Dmin, Dmax, 0.0, 1.0);
			return temp;
		} else {
			return 0.0;
		}
	}

	public LinkedList<ArrayList<Double[][]>> getLastTrainWeights() {
		return (LinkedList<ArrayList<Double[][]>>) lastTrainedWeights;
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
			// FileOutputStream("C:/Users/George/git/ciex2/ci/memory/mydriver.mem"));
			out = new ObjectOutputStream(new FileOutputStream("./memory/mydriver.mem"));
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
			f_in = new FileInputStream("./memory/mydriver.mem");
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

	public void setWeights(ArrayList<Double[][]> iWeights) {
		w1 = iWeights.get(0);
		w2 = iWeights.get(1);
	}
	public ArrayList<Double[][]> getWeights() {
		ArrayList<Double[][]> w = new ArrayList<Double[][]>();
		w.add(w1);
		w.add(w2);
		return w;
	}

	public void storeGA() {
		ObjectOutputStream out = null;
		try {
			// create the memory folder manually
			// out = new ObjectOutputStream(new
			// FileOutputStream("/users/edwinlima/git/ci/memory/mydriver.mem"));
			// out = new ObjectOutputStream(new
			// FileOutputStream("C:/Users/George/git/ciex2/ci/memory/mydriver.mem"));
			out = new ObjectOutputStream(new FileOutputStream("./memory/mydriverGA.mem"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
