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

public class DefaultDriver extends AbstractDriver {

	private ArrayList<Double> input;
	private DefaultDriverGenome driverGenome;
	Double desiredSpeed;
	private Double lapTime;
	private LinkedList<Double> prevReadingLeft;
	private LinkedList<Double> prevReadingMid;
	private LinkedList<Double> prevReadingRight;
	private double width;
//	private double[] smooth = { 0.5, 0.65, 0.75, 0.8, 0.95 };
	private double[] grade = { 0.8, 0.88, 0.9 };
	private double steerConstant = 0.4;
	private static boolean verbose = false;

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
		input = new ArrayList<Double>(11);
//		prevSteering = 0.0;
		prevReadingLeft = new LinkedList<Double>();
		prevReadingMid = new LinkedList<Double>();
		prevReadingRight = new LinkedList<Double>();
		lapTime = 0.0;
	}

	@Override
	public void control(Action action, SensorModel sensors) {
		double NNOutput;
		input = new ArrayList<Double>(11);

		for (int i = 0; i < sensors.getTrackEdgeSensors().length; i += 2) {
			input.add(sensors.getTrackEdgeSensors()[i]);
		}
		input.add(sensors.getTrackEdgeSensors()[9]);
		width = input.get(0) + input.get(9);

		NNOutput = driverGenome.getNNValue(input);
		action.steering = getCurrentSteering(sensors);
		desiredSpeed = NNOutput;
		if (verbose)
			System.out.println(desiredSpeed.toString());
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
		if (sensors.getSpeed() < (desiredSpeed * 5 / 6)) {
			action.accelerate = 1.0D;
			action.brake = 0.0D;
		}

		Double currTime = sensors.getCurrentLapTime();
		if (currTime > lapTime)
			lapTime = currTime;

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
		if (input.get(9) > temp) {
			diffScale = Math.abs((input.get(9) - temp) / width);
			// scale
			return (steerConstant * (0.5 + 0.45 * diffScale));
		} else
			return 0.0;
	}

	private double moveLeft(double grading) {
//		double grading = 1;
//		double diff;
//		if (grade == 100)
//			grading = 0.8;
//		else if (grade == 50)
//			grading = 0.88;
//		else if (grade == 20)
//			grading = 0.9;
//		else if (grade == 10)
//			grading = 0.95;
//		else
//			grading = 1;

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
		if (input.get(9) > temp) {
			diffScale = Math.abs((input.get(9) - temp) / width);
			// scale
			return -(steerConstant * (0.5 + 0.45 * diffScale));
		} else
			return 0.0;
	}

	private Double getCurrentSteering(SensorModel sensors) {
		double leftFront = input.get(4);
		double rightFront = input.get(5);
		double mid = input.get(input.size() - 1);
		double position = sensors.getTrackPosition();
//		double alignment = sensors.getAngleToTrackAxis();
		double distance = (getMo(prevReadingRight, leftFront) + getMo(prevReadingRight, mid)
				+ getMo(prevReadingRight, rightFront)) / 3.0;
		double speed = 0;

		if (position < 0.95 && position > -0.95) {
			// Consider width is the percentage, 0% is left, 100% is right
			if (distance > 100) {
				if (leftTurn(leftFront, rightFront, mid) == 1)
					speed = moveRight(100);
				else if (leftTurn(leftFront, rightFront, mid) == 2)
					speed = moveLeft(100);
				else if (leftTurn(leftFront, rightFront, mid) == -2)
					speed = moveLeft(100);
				else if (leftTurn(leftFront, rightFront, mid) == -1)
					speed = moveRight(100);
				else
					speed = 0.0;
			} else if (distance < 60 && distance > 30) {
				if (leftTurn(leftFront, rightFront, mid) == 1)
					speed = moveLeft(50);
				else if (leftTurn(leftFront, rightFront, mid) == 2)
					speed = moveRight(50);
				else if (leftTurn(leftFront, rightFront, mid) == -2)
					speed = moveRight(50);
				else if (leftTurn(leftFront, rightFront, mid) == -1)
					speed = moveLeft(50);
				else
					speed = 0.0;
			} else if (distance < 30 && distance > 15) {
				if (leftTurn(leftFront, rightFront, mid) == 1)
					speed = moveLeft(20);
				else if (leftTurn(leftFront, rightFront, mid) == 2)
					speed = moveRight(20);
				else if (leftTurn(leftFront, rightFront, mid) == -2)
					speed = moveRight(20);
				else if (leftTurn(leftFront, rightFront, mid) == -1)
					speed = moveLeft(20);
				else
					speed = 0.0;
			} else if (distance < 15 && distance > 5) {
				if (leftTurn(leftFront, rightFront, mid) == 1)
					speed = moveLeft(10);
				else if (leftTurn(leftFront, rightFront, mid) == 2)
					speed = moveRight(10);
				else if (leftTurn(leftFront, rightFront, mid) == -2)
					speed = moveRight(10);
				else if (leftTurn(leftFront, rightFront, mid) == -1)
					speed = moveLeft(10);
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

	public Double getLapTime() {
		// TODO Auto-generated method stub
		return lapTime;
	}
}