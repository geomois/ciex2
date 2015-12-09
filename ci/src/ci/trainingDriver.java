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
	private LinkedList<Double> prevReadingLeft;
	private LinkedList<Double> prevReadingMid;
	private LinkedList<Double> prevReadingRight;
	private double width;
	private double[] smooth = { 0.5, 0.65, 0.75, 0.8, 0.95 };
	private double[] grade = { 0.8, 0.88, 0.9 };
	private double steerConstant = 0.5;
	ArrayList<Double> temp;

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
		prevReadingLeft = new LinkedList<Double>();
		prevReadingMid = new LinkedList<Double>();
		prevReadingRight = new LinkedList<Double>();
	}

	@Override
	public void control(Action action, SensorModel sensors) {
		// Example of a bot that drives pretty well; you can use this to
		// generate data

		temp = new ArrayList<Double>();
		// temp.add(sensors.getTrackEdgeSensors()[8]);
		// temp.add(sensors.getTrackEdgeSensors()[10]);
		// temp.add(sensors.getTrackEdgeSensors()[9]);
		for (int i = 0; i < sensors.getTrackEdgeSensors().length; i += 2) {
			temp.add(sensors.getTrackEdgeSensors()[i]);
		}
		// central sensor
		temp.add(sensors.getTrackEdgeSensors()[9]);
		width = temp.get(0) + temp.get(9);
		input.add(temp);
		ArrayList<Double> temp2 = new ArrayList<Double>();
		temp2.add(sensors.getSpeed());
		temp2.add(action.steering);
		output.add(temp2);

		desiredSpeed = 80.0;
		getElev(sensors);
		action.steering = getCurrentSteering(sensors);

		if (sensors.getSpeed() > desiredSpeed) {
			action.accelerate = 0.0D;
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() > desiredSpeed + 20.0) {
			action.accelerate = 0.0D;
			action.brake = 1.0D;
		}
		if (sensors.getSpeed() <= desiredSpeed) {
			action.accelerate = (desiredSpeed + 90 - sensors.getSpeed()) / (desiredSpeed + 90);
			action.brake = 0.0D;
		}
		if (sensors.getSpeed() < desiredSpeed / 3) {
			action.accelerate = 1.0D;
			action.brake = 0.0D;
		}
	}

	private void getElev(SensorModel sensors) {
		Double dif = sensors.getTrackEdgeSensors()[9];
		if (dif > 60) {
			desiredSpeed += 60.0;
		} else if (dif > 50) {
			desiredSpeed += 40.0;
		} else if (dif > 40) {
			desiredSpeed += 30.0;
		} else if (dif > 30) {
			desiredSpeed += 20.0;
		} else if (dif > 20) {
			desiredSpeed += 10.0;
		} else {
			desiredSpeed += 10;
		}
	}

	private double getMo(LinkedList<Double> queue, double lastObject) {
		double mo = 0.0;
		if (queue.size() <= 10 && !queue.isEmpty())
			queue.remove();
		queue.add(lastObject);
		for (int i = 0; i < queue.size(); i++) {
			mo += queue.get(i);
		}
		return mo / (double) queue.size();
	}

	private int leftTurn(double left, double right, double mid) {
		int ret = 0;
		left = getMo(prevReadingLeft, left);
		mid = getMo(prevReadingMid, mid);
		right = getMo(prevReadingRight, right);
		if (mid > 60) {
			ret = 0;
		} else if (Math.abs(left - right) > 20) {
			if (left > right)
				ret = 1;
			else
				ret = 2;
		} else if (left > right)
			if (left >= mid)
				ret = 1;
			else {
				ret = -1;
			}
		else if (right >= mid)
			ret = 2;
		else {
			ret = -2;
		}
		return ret;
	}

	private double moveRight(double grading) {
		double diffScale;
		double temp = width - (width * grading);
		if (this.temp.get(9) > temp) {
			diffScale = Math.abs((this.temp.get(9) - temp) / width);
			// scale
			return (steerConstant * (0.5 + 0.45 * diffScale));
		} else
			return 0.0;
	}

	private double moveLeft(double grading) {
		// double grading = 1;
		// double diff;
		// if (grade == 100)
		// grading = 0.8;
		// else if (grade == 50)
		// grading = 0.88;
		// else if (grade == 20)
		// grading = 0.9;
		// else if (grade == 10)
		// grading = 0.95;
		// else
		// grading = 1;

		// if (input.get(0) > width - (width * grading)) {
		// diff = input.get(0) - (width - (width * grading));
		// if (Math.abs(diff / width) > 0.8)
		// return -steerConstant * smooth[smooth.length - 1];
		// else if (Math.abs(diff / width) > 0.6)
		// return -steerConstant * smooth[3];
		// else if (Math.abs(diff / width) > 0.4)
		// return -steerConstant * smooth[2];
		// else if (Math.abs(diff / width) > 0.25)
		// return -steerConstant * smooth[1];
		// else
		// return -steerConstant * smooth[0];
		double diffScale;
		double temp = width - (width * grading);
		if (this.temp.get(9) > temp) {
			diffScale = Math.abs((this.temp.get(9) - temp) / width);
			// scale
			return -(steerConstant * (0.5 + 0.45 * diffScale));
		} else
			return 0.0;
	}

	private Double getCurrentSteering(SensorModel sensors) {
		Double currentSteer = null;
		boolean verbose = true;
		double leftFront = temp.get(4);
		double rightFront = temp.get(5);
		double mid = temp.get(temp.size() - 1);
		double position = sensors.getTrackPosition();
		double alignment = sensors.getAngleToTrackAxis();
		double distance = (getMo(prevReadingRight, leftFront) + getMo(prevReadingRight, mid)
				+ getMo(prevReadingRight, rightFront)) / 3.0;
		double extra = sensors.getAngleToTrackAxis();
		double speed = 0;


		if (position < 0.95 && position > -0.95) {
			// Consider width is the percentage, 0% is left, 100% is right
			if (distance > 100) {
				if (leftTurn(leftFront, rightFront, mid) == 1)
					speed = moveRight(grade[0]);
				else if (leftTurn(leftFront, rightFront, mid) == 2)
					speed = moveLeft(grade[0]);
				else if (leftTurn(leftFront, rightFront, mid) == -2)
					speed = moveLeft(grade[0]);
				else if (leftTurn(leftFront, rightFront, mid) == -1)
					speed = moveRight(grade[0]);
				else
					speed = 0.0;
			} else if (distance < 60 && distance > 30) {
				if (leftTurn(leftFront, rightFront, mid) == 1)
					speed = moveLeft(grade[1]);
				else if (leftTurn(leftFront, rightFront, mid) == 2)
					speed = moveRight(grade[1]);
				else if (leftTurn(leftFront, rightFront, mid) == -2)
					speed = moveRight(grade[1]);
				else if (leftTurn(leftFront, rightFront, mid) == -1)
					speed = moveLeft(grade[1]);
				else
					speed = 0.0;
			} else if (distance < 30 && distance > 15) {
				if (leftTurn(leftFront, rightFront, mid) == 1)
					speed = moveLeft(grade[2]);
				else if (leftTurn(leftFront, rightFront, mid) == 2)
					speed = moveRight(grade[2]);
				else if (leftTurn(leftFront, rightFront, mid) == -2)
					speed = moveRight(grade[2]);
				else if (leftTurn(leftFront, rightFront, mid) == -1)
					speed = moveLeft(grade[2]);
				else
					speed = 0.0;
			}
		} else {
			if (position >0)
				speed = DriversUtils.alignToTrackAxis(sensors, 0.3D) - 0.1;
			else
				speed = DriversUtils.alignToTrackAxis(sensors, 0.3D) + 0.1;
		}
		if (speed == 0.0)
			return DriversUtils.alignToTrackAxis(sensors, 0.3D);
		else
			return speed;
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