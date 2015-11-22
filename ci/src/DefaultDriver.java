import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

    NeuralNetwork neuralNetwork = new NeuralNetwork();
    ArrayList<ArrayList<Double>> input;
    ArrayList<ArrayList<Double>> output;
    DefaultDriver() {
        initialize();
       // neuralNetwork = neuralNetwork.loadGenome();
    }

    public void loadGenome(IGenome genome) { }

    public void initialize(){
       this.enableExtras(new AutomatedClutch());
       this.enableExtras(new AutomatedGearbox());
       this.enableExtras(new AutomatedRecovering());
       this.enableExtras(new ABS());
       input=new ArrayList<ArrayList<Double>>();
       output=new ArrayList<ArrayList<Double>>();
    }

    private Double calculateSpeed(SensorModel sensors){
    	double speed = 0;
    	sensors.getTrackPosition();
    	
    	return speed;
    }
    @Override
    public void control(Action action, SensorModel sensors) {
        // Example of a bot that drives pretty well; you can use this to generate data
        action.steering = DriversUtils.alignToTrackAxis(sensors, 1);
        if(sensors.getSpeed() > 100.0D) {
            action.accelerate = 0.0D;
            action.brake = 0.0D;
        }
        if(sensors.getSpeed() > 110.0D) {
            action.accelerate = 0.0D;
            action.brake = -1.0D;
        }
        if(sensors.getSpeed() <= 100.0D) {
            action.accelerate = (100.0D - sensors.getSpeed()) / 100.0D;
            action.brake = 0.0D;
        }
        if(sensors.getSpeed() < 50.0D) {
            action.accelerate = 1.0D;
            action.brake = 0.0D;
        }
        System.out.println(action.steering +"steering");
        System.out.println(action.accelerate + "acceleration");
        System.out.println(action.brake + "brake");

        System.out.println(sensors.getSpeed() +"speedIn");
        System.out.println(sensors.getAngleToTrackAxis() + "AngleToTrackAxis");
        System.out.println(sensors.getTrackEdgeSensors()[10] + "TrackEdgeSensors");
        System.out.println(sensors.getTrackEdgeSensors()[9] + "TrackEdgeSensors");
        System.out.println(sensors.getTrackEdgeSensors()[8] + "TrackEdgeSensors");
        System.out.println(sensors.getTrackPosition() + "trackposition");
        
        ArrayList<Double> temp=new ArrayList<Double>();
        temp.add(sensors.getTrackEdgeSensors()[8]);
        temp.add(sensors.getTrackEdgeSensors()[10]);
        temp.add(sensors.getTrackEdgeSensors()[9]);
        input.add(temp);
        ArrayList<Double> temp2=new ArrayList<Double>();
        temp2.add(sensors.getSpeed());
        temp2.add(action.steering);
        output.add(temp2);
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

	public ArrayList<ArrayList<Double>> getInput() {
		return input;
	}

	public ArrayList<ArrayList<Double>> getOutput() {
		return output;
	}
}