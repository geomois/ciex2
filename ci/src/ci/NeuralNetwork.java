package ci;
import java.io.*;

public class NeuralNetwork implements Serializable {

	
	private static double learningRate = 0.1;
	
	private int nInputs, nHidden, nOutput; 
	private double[/* i */] input, hidden, output;
	private double[/* j */][/* i */] weightL1, weigthL2;  


	public NeuralNetwork(int nInput, int nHidden, int nOutput) {
		this.nInputs = nInput;
        this.nHidden = nHidden;
        this.nOutput = nOutput;

        input = new double[nInput+1];
        hidden = new double[nHidden+1];
        output = new double[nOutput+1];

        weightL1 = new double[nHidden+1][nInput+1];
        weigthL2 = new double[nOutput+1][nHidden+1];

        // Initialize weigths
        generateRandomWeights();
    }
	private void generateRandomWeights() {
	        
	        for(int j=1; j<=nHidden; j++)
	            for(int i=0; i<=nInputs; i++) {
	                weightL1[j][i] = Math.random() - 0.5;
	        }
	
	        for(int j=1; j<=nOutput; j++)
	            for(int i=0; i<=nHidden; i++) {
	                weigthL2[j][i] = Math.random() - 0.5;
	        }
	    }

	private static final long serialVersionUID = -88L;

    /**
     * Train the network with given a pattern.
     * The pattern is passed through the network and the weights are adjusted
     * by backpropagation, considering the desired output.
     *
     * @param pattern the pattern to be learned
     * @param desiredOutput the desired output for pattern
     * @return the network output before weights adjusting
     */
    public double[] train(double[] pattern, double[] desiredOutput) {
        double[] output = passNet(pattern);
        backpropagation(desiredOutput);
        return output;
    }


    /**
     * Passes a pattern through the network. Activatinon functions are logistics.
     *
     * @param pattern pattern to be passed through the network
     * @return the network output for this pattern
     */
    public double[] passNet(double[] pattern) {

        for(int i=0; i<nInputs; i++) {
            input[i+1] = pattern[i];
        }
        
        // Set bias
        input[0] = 1.0;
        hidden[0] = 1.0;

        // Passing through hidden layer
        for(int j=1; j<=nHidden; j++) {
            hidden[j] = 0.0;
            for(int i=0; i<=nInputs; i++) {
                hidden[j] += weightL1[j][i] * input[i];
            }
            hidden[j] = 1.0/(1.0+Math.exp(-hidden[j]));
        }
    
        // Passing through output layer
        for(int j=1; j<=nOutput; j++) {
            output[j] = 0.0;
            for(int i=0; i<=nHidden; i++) {
                output[j] += weigthL2[j][i] * hidden[i];
       	    }
            output[j] = 1.0/(1+0+Math.exp(-output[j]));
        }

        return output;
    }


    /**
     * This method adjust weigths considering error backpropagation. The desired
     * output is compared with the last network output and weights are adjusted
     * using the choosen learn rate.
     *
     * @param desiredOutput desired output for the last given pattern
     */
    private void backpropagation(double[] desiredOutput) {

        double[] errorL2 = new double[nOutput+1];
        double[] errorL1 = new double[nHidden+1];
        double Esum = 0.0;

        for(int i=1; i<=nOutput; i++)  // Layer 2 error gradient
            errorL2[i] = output[i] * (1.0-output[i]) * (desiredOutput[i-1]-output[i]);
	    
               
        for(int i=0; i<=nHidden; i++) {  // Layer 1 error gradient
            for(int j=1; j<=nOutput; j++)
                Esum += weigthL2[j][i] * errorL2[j];

            errorL1[i] = hidden[i] * (1.0-hidden[i]) * Esum;
            Esum = 0.0;
        }
             
        for(int j=1; j<=nOutput; j++)
            for(int i=0; i<=nHidden; i++)
                weigthL2[j][i] += learningRate * errorL2[j] * hidden[i];
         
        for(int j=1; j<=nHidden; j++)
            for(int i=0; i<=nInputs; i++) 
                weightL1[j][i] += learningRate * errorL1[j] * input[i];
    }

	// Store the state of this neural network
	public void storeGenome() {
		ObjectOutputStream out = null;
		try {
			// create the memory folder manually
			// out = new ObjectOutputStream(new
			// FileOutputStream("/users/edwinlima/git/ci/memory/mydriver.mem"));
			out = new ObjectOutputStream(new FileOutputStream("C:/yoel/java/ciex2/ci/memory/driveryoel.mem"));
//			 out = new ObjectOutputStream(new FileOutputStream("E:\\eclipse
//			 java\\eclipse workspace\\git\\ci\\memory\\mydriver.mem"));
			//out = new ObjectOutputStream(new FileOutputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem"));
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
	public static NeuralNetwork loadGenome() {

		// Read from disk using FileInputStream
		FileInputStream f_in = null;
		try {
			// f_in = new
			// FileInputStream("/users/edwinlima/git/ci/memory/mydriver.mem");
        	f_in = new FileInputStream("C:/yoel/java/ciex2/ci/memory/driveryoel.mem");
			// f_in = new FileInputStream("E:\\eclipse java\\eclipse
			// workspace\\git\\ci\\memory\\mydriver.mem");
			//f_in = new FileInputStream("C:\\Users\\George\\git\\ci\\ci\\memory\\mydriver.mem");
			
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
	public double[] getValues(double[] input) {
		// TODO Auto-generated method stub
		return this.passNet(input);
	}
}
