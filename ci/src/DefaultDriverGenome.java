
import java.util.ArrayList;

import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNetwork myNN = new NeuralNetwork();
    
    public NeuralNetwork getMyNN() {
        return myNN;
    }
	public void trainNN(ArrayList<ArrayList<Double>> input, ArrayList<ArrayList<Double>> output) {
		myNN.trainNetwork(input, output);
	}
}

