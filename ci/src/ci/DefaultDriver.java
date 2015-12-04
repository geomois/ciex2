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

	private static final Double TopSpeed = 150.0;
	// private NeuralNetwork neuralNetwork = new NeuralNetwork();
	private ArrayList<Double> input;
	private DefaultDriverGenome driverGenome;
	private Double prevSteering;
	DefaultDriver() {
		initialize();
	}

	public void loadGenome(IGenome genome) {
		driverGenome = (DefaultDriverGenome) genome;
	}

	public void initialize() {
		this.enableExtras(new AutomatedClutch());
		this.enableExtras(new AutomatedGearbox());
		this.enableExtras(new AutomatedRecovering());
		this.enableExtras(new ABS());
		input = new ArrayList<Double>();
		prevSteering=0.0;
	}

	@Override
	public void control(Action action, SensorModel sensors) {

		Double[] NNOutput = new Double[2];
		input.clear();
		// input.add(sensors.getTrackEdgeSensors()[8]);
		// input.add(sensors.getTrackEdgeSensors()[10]);
		// input.add(sensors.getTrackEdgeSensors()[9]);
		for (int i = 0; i < sensors.getTrackEdgeSensors().length; i++) {
			input.add(sensors.getTrackEdgeSensors()[i]);
		}
		NNOutput = driverGenome.getNNValue(input);
		Double desiredSpeed = NNOutput[0];
		Double steering=getSteering(NNOutput[1],sensors);
		
		System.out.println(desiredSpeed.toString());
		System.out.println(NNOutput[1].toString()+" "+steering);
		
//		steering=NNOutput[1];
		action.steering = steering;
		if (sensors.getSpeed() > desiredSpeed) {
			action.accelerate = 0.0D;
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() > desiredSpeed + 10.0D) {
			action.accelerate = 0.0D;
			action.brake = -0.9D;
		}
		if (sensors.getSpeed() <= desiredSpeed) {
			action.accelerate = (desiredSpeed - sensors.getSpeed() / 2) / desiredSpeed;
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() < (desiredSpeed * 1 / 4)) {
			action.accelerate = 1.0D;
			action.brake = 0.0D;
		}

	}

	private Double getSteering(Double double1,SensorModel sensors) {
		Double bias;
		Double dif = input.get(0).doubleValue() - input.get(input.size() - 1).doubleValue();
		if (dif > 0.7 && dif < 2.0) {
			bias = -0.08;
		} else if (dif < -0.7 && dif > -2.0) {
			bias = 0.08;
		} else if (dif < -3.0) {
			bias = 0.1;
		} else if (dif > 3.0) {
			bias = -0.1;
		} else {
			bias = 0.0;
		}
		Double currentSteer = DriversUtils.alignToTrackAxis(sensors, 0.2D) + bias;
		dif=Math.abs(prevSteering) - Math.abs(currentSteer);
		if (dif > 0.4)
			if ((prevSteering > 0 && currentSteer < 0) || (prevSteering < 0 && currentSteer > 0))
				currentSteer = currentSteer * 0.06;
			else
				currentSteer = currentSteer * 0.7;
		else if(dif>0.1)
			if ((prevSteering > 0 && currentSteer < 0) || (prevSteering < 0 && currentSteer > 0))
				currentSteer = currentSteer * 0.09;
//		TODO: take in considaration the mean of the last 3-5 steerings
		prevSteering = currentSteer;

		return currentSteer;
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
	public double getSteering(SensorModel sensorModel) {
		return 0;
	}

	@Override
	public double getAcceleration(SensorModel sensorModel) {
		return 0;
	}

	public double getBraking(SensorModel sensorModel) {
		return 0;
	}
}