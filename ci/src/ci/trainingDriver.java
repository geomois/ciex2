package ci;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;

public class trainingDriver extends AbstractDriver {

	DefaultDriverGenome ddg;
	NeuralNetwork NN;
	ArrayList<ArrayList<Double>> input;
	ArrayList<ArrayList<Double>> output;
	private Queue<Double> moSteer;
	private double prevSteering;
	private double desiredSpeed;

	trainingDriver() {
		initialize();
	}

	public void loadGenome(IGenome genome) {
	}

	public void initialize() {
		this.enableExtras(new AutomatedClutch());
		this.enableExtras(new AutomatedGearbox());
		this.enableExtras(new AutomatedRecovering());
		this.enableExtras(new ABS());
		input = new ArrayList<ArrayList<Double>>();
		output = new ArrayList<ArrayList<Double>>();
		prevSteering = 0.0;
		moSteer = new LinkedList<Double>();
	}

	@Override
	public void control(Action action, SensorModel sensors) {
		// Example of a bot that drives pretty well; you can use this to
		// generate data

		desiredSpeed = 60.0;
		action.steering = getSteering(0.0, sensors);

		if (sensors.getSpeed() > desiredSpeed) {
			action.accelerate = 0.0D;
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() > desiredSpeed + 5.0) {
			action.accelerate = 0.0D;
			action.brake = 1.0D;
		}
		if (sensors.getSpeed() <= desiredSpeed) {
			action.accelerate = (desiredSpeed+90 - sensors.getSpeed()) / (desiredSpeed+90 );
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() < desiredSpeed / 3) {
			action.accelerate = 1.0D;
			action.brake = 0.0D;
		}

		ArrayList<Double> temp = new ArrayList<Double>();
		// temp.add(sensors.getTrackEdgeSensors()[8]);
		// temp.add(sensors.getTrackEdgeSensors()[10]);
		// temp.add(sensors.getTrackEdgeSensors()[9]);
		for (int i = 0; i < sensors.getTrackEdgeSensors().length; i+=2) {
			temp.add(sensors.getTrackEdgeSensors()[i]);
		}
		//central sensor
		temp.add(sensors.getTrackEdgeSensors()[9]);
		input.add(temp);
		ArrayList<Double> temp2 = new ArrayList<Double>();
		temp2.add(sensors.getSpeed());
		temp2.add(action.steering);
		output.add(temp2);
	}

	private Double getSteering(Double double1, SensorModel sensors) {
		Double bias;
		Double dif = sensors.getTrackEdgeSensors()[0] - sensors.getTrackEdgeSensors()[18];
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
		Double currentSteer = DriversUtils.alignToTrackAxis(sensors, 0.3D) + bias;
		Double dif1 = dif;

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
		this.addPrevSteering(currentSteer);

		return currentSteer;
	}

	private void addPrevSteering(double current) {
		if (moSteer.size() == 3) {
			moSteer.remove();
		}
		moSteer.add(current);
		prevSteering = 0.0;
		for (Double d : moSteer) {
			prevSteering += d;
		}
		prevSteering = prevSteering / (double) moSteer.size();
	}

	public String getDriverName() {
		return "XVII training";
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
		return input;
	}

	public ArrayList<ArrayList<Double>> getOutput() {
		return output;
	}
}