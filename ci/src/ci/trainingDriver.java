package ci;


import java.io.Serializable;
import java.util.ArrayList;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;


public class trainingDriver extends AbstractDriver implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	NeuralNetwork NN;
    ArrayList<double[]> input;
    ArrayList<double[]> output;
    trainingDriver() {
        initialize();
    }

    public void loadGenome(IGenome genome) {
    }

    public void initialize(){
       this.enableExtras(new AutomatedClutch());
       this.enableExtras(new AutomatedGearbox());
       this.enableExtras(new AutomatedRecovering());
       this.enableExtras(new ABS());
       input=new ArrayList<double[]>();
       output=new ArrayList<double[]>();
    }
    @Override
    public void control(Action action, SensorModel sensors) {
        // Example of a bot that drives pretty well; you can use this to generate data
    	
       double outputs[] = new double[3];
               
       input.add(sensors.getTrackEdgeSensors());
    	action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
        if(sensors.getSpeed() > 60.0D) {
            action.accelerate = 0.0D;
            action.brake = 0.0D;
        }

        if(sensors.getSpeed() > 70.0D) {
            action.accelerate = 0.0D;
            action.brake = 1.0D;
        }

        if(sensors.getSpeed() <= 60.0D) {
            action.accelerate = (80.0D - sensors.getSpeed()) / 80.0D;
            action.brake = 0.0D;
        }

        if(sensors.getSpeed() < 30.0D) {
            action.accelerate = 1.0D;
            action.brake = 0.0D;
        
        }
        
        outputs[0] = action.accelerate;
        outputs[1] = action.brake;
        outputs[2] = (action.steering+1.0D)/2.0D;
       
        output.add(outputs);
    }

	public String getDriverName() {
        return "XVII training";
    }

    public void controlQualification(Action action, SensorModel sensors) { }

    public void controlRace(Action action, SensorModel sensors) {}

    public void defaultControl(Action action, SensorModel sensors){}

    @Override
    public double getSteering(SensorModel sensorModel) {
        return 0;
    }

    @Override
    public double getAcceleration(SensorModel sensorModel) {
        return 0;
    }

    public double getBraking(SensorModel sensorModel){
        return 0;
    }

	public ArrayList<double[]> getInput() {
		return input;
	}

	public ArrayList<double[]> getOutput() {
		return output;
	}
}