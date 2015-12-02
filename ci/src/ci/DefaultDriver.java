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
    private ArrayList<Double> input;
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
       input=new ArrayList<Double>();
    }

    @Override
    public void control(Action action, SensorModel sensors) {
       
    	Double [] NNOutput=new Double[2];
    	input.clear();
//        input.add(sensors.getTrackEdgeSensors()[8]);
//        input.add(sensors.getTrackEdgeSensors()[10]);
//        input.add(sensors.getTrackEdgeSensors()[9]);
    	for(int i=0;i<sensors.getTrackEdgeSensors().length;i++){
    		input.add(sensors.getTrackEdgeSensors()[i]);
    	}
    	NNOutput=driverGenome.getNNValue(input);
    	Double desiredSpeed=NNOutput[0];
    	Double desiredSteering=NNOutput[1];
    	System.out.println(desiredSpeed.toString());
//    	System.out.println(desiredSteering.toString());
    	if(sensors.getSpeed() > desiredSpeed) {
            action.accelerate = 0.0D;
            action.brake = 0.0D;
        }
        if(sensors.getSpeed() > desiredSpeed+10.0D) {
            action.accelerate = 0.0D;
            action.brake = 0.9D;
        }
        if(sensors.getSpeed() <= desiredSpeed) {
            action.accelerate = (desiredSpeed - sensors.getSpeed()/4) / desiredSpeed;
            action.brake = 0.0D;
        }
        if(sensors.getSpeed() < (desiredSpeed*3/4)) {
            action.accelerate = 1.0D;
            action.brake = 0.0D;
        }
//    	if(sensors.getSpeed()<NNOutput[0])
//    		action.accelerate=0.5D;
//    	else if(sensors.getSpeed()>NNOutput[0])
//    		action.brake=0.5D;
    	
    	action.steering=DriversUtils.alignToTrackAxis(sensors,0.5D);
//    	action.steering=NNOutput[1]*0.7D;
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