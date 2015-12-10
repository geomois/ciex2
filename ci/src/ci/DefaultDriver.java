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
	private double[] smooth = { 0.7, 0.75, 0.8, 0.88, 0.95  };
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
		desiredSpeed = NNOutput-NNOutput*Math.abs(action.steering)*0.5;
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
		if (mid > 150) {
			ret = 0;
		} else {
			if (left > right)
				ret = 1;
			else
				ret = 2;
		}
		return ret;
	}

	private double moveRight(double grading) {
		double diffScale;
		double temp = width - (width * grading);
		if (this.input.get(9) > temp) {
			diffScale = Math.abs((this.input.get(9) - temp) / width);
			// scale
			return (steerConstant * (smooth[0] + (smooth[smooth.length-1] - smooth[0]) * diffScale));
		} else
			return 0.0;
	}

	private Double getCurrentSteering(SensorModel sensors) {
		double leftFront = getMo(prevReadingRight, input.get(4));
		double rightFront = getMo(prevReadingRight, input.get(5));
		double mid = getMo(prevReadingRight, input.get(input.size() - 1));
		double position = sensors.getTrackPosition();
		double distance = (leftFront + mid + rightFront) / 3.0;
		double speed = 0;
		// Consider width is the percentage, 0% is left, 100% is right

		if (position < 0.9 && position > -0.9) {
			if (mid > 150) {
				speed = 0.0;
			} else {
				int pick = leftTurn(leftFront, rightFront, mid);
				if (distance > 50) {
					speed = moveRight(grade[0]);
					speed = pick * (Math.abs(pick - 2) * speed + ((pick - 1) / 2) * -speed);
				} else if (distance > 30) {
					speed = moveRight(grade[1]);
					speed = pick * (Math.abs(pick - 2) * -speed + ((pick - 1) / 2) * speed);
				} else if (distance < 30 && distance > 15) {
					speed = moveRight(grade[2]);
					speed = pick * (Math.abs(pick - 2) * -speed + ((pick - 1) / 2) * speed);
				}
			}
		} else {
//			double bias=(Math.abs(position)-0.8)*1.9;
			if (position > 0)
				return DriversUtils.alignToTrackAxis(sensors, 0.3D) - 0.3;
			else
				return DriversUtils.alignToTrackAxis(sensors, 0.3D) + 0.3;
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