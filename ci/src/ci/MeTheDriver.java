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

public class MeTheDriver extends AbstractDriver{

	private static Double TopSpeed;
	// private NeuralNetwork neuralNetwork = new NeuralNetwork();
	private ArrayList<ArrayList<Double>> input;
	private DefaultDriverGenome driverGenome;
	ArrayList<ArrayList<Double>> output;
	fooFrame f;
	private double currentAccel=0.0;

	MeTheDriver() {
		initialize();
	}

	public void loadGenome(IGenome genome) {
		driverGenome = (DefaultDriverGenome) genome;
		TopSpeed = driverGenome.getMyNN().getMaxSpeed();
	}

	public void initialize() {
		this.enableExtras(new AutomatedClutch());
		this.enableExtras(new AutomatedGearbox());
		this.enableExtras(new AutomatedRecovering());
		this.enableExtras(new ABS());
		input = new ArrayList<ArrayList<Double>>();
		output=new ArrayList<ArrayList<Double>>();
		f=new fooFrame();
//		new Thread(f).start();
	}

	@Override
	public void control(Action action, SensorModel sensors) {
		action.steering = f.getSteering();
		currentAccel=f.getSpeed();
		if (currentAccel>=0.0) {
			action.accelerate=currentAccel;
		}else{
			action.brake=currentAccel;
		}
		ArrayList<Double> temp = new ArrayList<Double>();
		for (int i = 0; i < sensors.getTrackEdgeSensors().length; i+=2) {
			temp.add(sensors.getTrackEdgeSensors()[i]);
		}
		input.add(temp);
		ArrayList<Double> temp2 = new ArrayList<Double>();
		temp2.add(sensors.getSpeed());
		output.add(temp2);
	}

	public String getDriverName() {
		return "XVII";
	}

	public void controlQualification(Action action, SensorModel sensors) {
	}

	public void controlRace(Action action, SensorModel sensors) {
	}

	public void defaultControl(Action action, SensorModel sensors) {
	}

	@Override
	public double getAcceleration(SensorModel sensorModel) {
		return 0;
	}

	public double getBraking(SensorModel sensorModel) {
		return 0;
	}

	@Override
	public double getSteering(SensorModel arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ArrayList<ArrayList<Double>> getInput() {
		return input;
	}

	public ArrayList<ArrayList<Double>> getOutput() {
		return output;
	}
}