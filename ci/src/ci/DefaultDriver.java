package ci;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	// private NeuralNetwork neuralNetwork = new NeuralNetwork();
	private ArrayList<Double> input;
	private ArrayList<ArrayList<Double>> inputAr;
	private ArrayList<Double> speedAr;
	private ArrayList<Double> steeringAr;
	private DefaultDriverGenome driverGenome;
	private double prevSteering;

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
		inputAr = new ArrayList<ArrayList<Double>>();
		speedAr = new ArrayList<Double>();
		steeringAr = new ArrayList<Double>();
		prevSteering = 0.0;
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
		// System.out.println(desiredSpeed.toString());
		Double bias;
		Double dif = input.get(0).doubleValue() - input.get(input.size() - 1).doubleValue();
		if (dif > 0.7 && dif < 2.0) {
			bias = -0.08;
			desiredSpeed += 40.0;
		} else if (dif < -0.7 && dif > -2.0) {
			bias = 0.08;
			desiredSpeed += 40.0;
		} else if (dif < -3.0 && dif > -5.0) {
			desiredSpeed += 20.0;
			bias = 0.1;
		} else if (dif > 3.0 && dif < 5.0) {
			desiredSpeed += 20.0;
			bias = -0.1;
		} else if (dif < -5.0) {
			desiredSpeed += 15.0;
			bias = 0.12;
		} else if (dif > 5.0) {
			desiredSpeed += 15.0;
			bias = -0.12;
		} else {
			desiredSpeed += 55;
			bias = 0.0;
		}
		Double currentSteer = DriversUtils.alignToTrackAxis(sensors, 0.2D) + bias;
		dif = Math.abs(prevSteering) - Math.abs(currentSteer);
		if (dif > 0.3)
			if ((prevSteering > 0 && currentSteer < 0) || (prevSteering < 0 && currentSteer > 0))
				currentSteer = currentSteer * 0.06;
			else
				currentSteer = currentSteer * 0.5;
		else if (dif > 0.1)
			if ((prevSteering > 0 && currentSteer < 0) || (prevSteering < 0 && currentSteer > 0))
				currentSteer = currentSteer * 0.09;
		// TODO: take in considaration the mean of the last 3-5 steerings
		prevSteering = currentSteer;

		action.steering = currentSteer * 1.02;
		System.out.println(bias.toString() + " " + dif + " " + Double.toString(action.steering));
		if (sensors.getSpeed() > desiredSpeed) {
			action.accelerate = 0.0D;
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() > desiredSpeed + 5.0) {
			action.accelerate = 0.0D;
			action.brake = 1.0D;
		}
		if (sensors.getSpeed() <= desiredSpeed) {
			action.accelerate = (desiredSpeed - sensors.getSpeed() / 5) / desiredSpeed;
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() < (desiredSpeed * 5/6)) {
			action.accelerate = 1.0D;
			action.brake = 0.0D;
		}

		inputAr.add(input);
		speedAr.add(desiredSpeed);
		steeringAr.add(action.steering);
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

	public ArrayList<ArrayList<Double>> getInput() {
		// TODO Auto-generated method stub
		return inputAr;
	}

	public ArrayList<Double> speed() {
		// TODO Auto-generated method stub
		return speedAr;
	}

	public ArrayList<Double> getSteering() {
		// TODO Auto-generated method stub
		return steeringAr;
	}
}