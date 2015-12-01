package ci;
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

public class DefaultDriver extends AbstractDriver {

//    private NeuralNetwork neuralNetwork = new NeuralNetwork();
	private DefaultDriverGenome driverGenome;
    DefaultDriver() {
        initialize();
    }

    public void loadGenome(IGenome genome) {
    	driverGenome=(DefaultDriverGenome) genome;
    }

    public void initialize(){
       this.enableExtras(new AutomatedClutch());
       this.enableExtras(new AutomatedGearbox());
       this.enableExtras(new AutomatedRecovering());
       this.enableExtras(new ABS());
    }

    @Override
    public void control(Action action, SensorModel sensors) {
    	double [] NNOutput;
       
    	NNOutput=driverGenome.getNNValue(sensors.getTrackEdgeSensors());
       
    	action.accelerate = NNOutput[1];
    	action.brake      = NNOutput[2];
    	action.steering   =(NNOutput[3]*2.0D)-1.0D;
    	
    	

    }

	public String getDriverName() {
        return "XVII";
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
}