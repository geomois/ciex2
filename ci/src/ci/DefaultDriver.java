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

	private static Double TopSpeed;
	// private NeuralNetwork neuralNetwork = new NeuralNetwork();
	private ArrayList<Double> input;
	private DefaultDriverGenome driverGenome;
	private Double prevSteering;
	Double desiredSpeed;
	private Queue<Double> moSteer;
	private Double lapTime;
	private double prevPosition;

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
		prevSteering = 0.0;
		moSteer = new LinkedList<Double>();
		prevPosition = 0.0;
	}

	@Override
	public void control(Action action, SensorModel sensors) {
		Double[] NNOutput = new Double[2];
		input.clear();
		double count1, count2;
		count1 = count2 = 0.0;
		for (int i = 0; i < sensors.getTrackEdgeSensors().length; i += 2) {
			input.add(sensors.getTrackEdgeSensors()[i]);
			if (i < 10)
				count1 += input.get(input.size() - 1);
			else
				count2 += input.get(input.size() - 1);
		}

		// System.out.println((count1/8.0)+" "+(count1/8.0));
		// System.out.println(input.get(4) + " " + sensors.getTrackPosition() +
		// " " + input.get(5));

		// central sensor
		input.add(sensors.getTrackEdgeSensors()[9]);
		NNOutput = driverGenome.getNNValue(input);
		desiredSpeed = NNOutput[0];

		action.steering = getCurrentSteering(sensors);
		desiredSpeed = NNOutput[0];
		// System.out.println(desiredSpeed.toString());
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
		lapTime = sensors.getCurrentLapTime();

	}

	private Double getCurrentSteering(SensorModel sensors) {
		Double currentSteer = null;
		// System.out.println(sensors.getAngleToTrackAxis());
		// System.out.println(sensors.getTrackPosition());
		// if (!(sensors.getTrackPosition()<-1.0 ||
		// sensors.getTrackPosition()>1.0)) {
		// Double dif = sensors.getTrackEdgeSensors()[0] -
		// sensors.getTrackEdgeSensors()[18];
		// if (dif > 0.7 && dif < 2.0) {
		// bias = -0.08;
		// desiredSpeed += 10.0;
		// } else if (dif < -0.7 && dif > -2.0) {
		// bias = 0.08;
		// desiredSpeed += 10.0;
		// } else if (dif < -3.0 && dif > -5.0) {
		// desiredSpeed += 5.0;
		// bias = 0.1;
		// } else if (dif > 3.0 && dif < 5.0) {
		// desiredSpeed += 5.0;
		// bias = -0.1;
		// } else if (dif < -5.0) {
		// desiredSpeed -= 20.0;
		// bias = 0.12;
		// } else if (dif > 5.0) {
		// desiredSpeed -= 20.0;
		// bias = -0.12;
		// } else {
		// desiredSpeed += 20;
		// bias = 0.0;
		// }
		// currentSteer = DriversUtils.alignToTrackAxis(sensors, 0.3D) + bias;
		// Double dif1 = dif;
		// dif = Math.abs(prevSteering) - Math.abs(currentSteer);
		// if (dif > 0.3)
		// if ((prevSteering > 0 && currentSteer < 0) || (prevSteering < 0 &&
		// currentSteer > 0))
		// currentSteer = currentSteer * 0.06;
		// else
		// currentSteer = currentSteer * 0.5;
		// else if (dif > 0.1)
		// if ((prevSteering > 0 && currentSteer < 0) || (prevSteering < 0 &&
		// currentSteer > 0))
		// currentSteer = currentSteer * 0.09;
		// this.addPrevSteering(currentSteer);
		// }else
		// currentSteer=getInTrack(sensors.getTrackPosition());
		boolean verbose = true;
		double leftFront = input.get(4);
		double rightFront = input.get(5);
		double position = sensors.getTrackPosition();
		double bias = 1.0;
		double base=0.0;
		double alignment=sensors.getAngleToTrackAxis();
		// System.out.println(leftFront + " " + sensors.getTrackPosition() + " "
		// + rightFront);
		 if((prevPosition> 0.45 && prevPosition< 0.9) || ((prevPosition<-0.45 && prevPosition>-0.9))){
			 if((alignment>(base+0.035) && alignment<(base+0.045)) || (alignment<-(base+0.035) && alignment>-(base+0.045)))
				 bias=1.1;
			 else if((alignment>base+0.045 && alignment<base+0.055) || (alignment<-(base+0.045) && alignment>-(base+0.055)))
				 bias=1.2;
			 else if((alignment>base+0.055 && alignment<base+0.065) || (alignment<-(base+0.055) && alignment>-(base+0.065)))
				 bias=1.3;
			 else if((alignment>base+0.065 && alignment<base+0.075) || (alignment<-(base+0.065) && alignment>-(base+0.075)))
				 bias=1.4;
			 else if(alignment>base+0.075 || (alignment<-(base+0.075)))
				 bias=1.5;
		 }
		 System.out.println(sensors.getAngleToTrackAxis()+" "+"bias: "+bias);
		
		
		if (!(position > 0.45 || position < -0.45)) {
			if (position > 0) {
				currentSteer = -0.05 * bias;
			} else {
				currentSteer = 0.05 * bias;
			}
			if (!verbose)
				System.out.println("in" + currentSteer);
		} else if ((position > 0.65 && position < 0.7) || (position < -0.65 && position > -0.7)) {
			if (position > 0) {
				currentSteer = -0.1 * bias;
			} else {
				currentSteer = 0.1 * bias;
			}
			if (!verbose)
				System.out.println("1mid" + currentSteer);
		} else if ((position > 0.7 && position < 0.75) || (position < -0.7 && position > -0.75)) {
			if (position > 0) {
				currentSteer = -0.2 * bias;
			} else {
				currentSteer = 0.2 * bias;
			}
			if (!verbose)
				System.out.println("2mid" + currentSteer);
		} else if ((position > 0.75 && position < 0.85) || (position < -0.75 && position > -0.85)) {
			if (position > 0) {
				currentSteer = -0.3 * bias;
			} else {
				currentSteer = 0.3 * bias;
			}
			if (!verbose)
				System.out.println("3mid" + currentSteer);
		} else if (position > 0.9 || position < -0.9) {
			if (position > 0) {
				currentSteer = -0.4 * bias;
			} else {
				currentSteer = 0.4 * bias;
			}
			if (!verbose)
				System.out.println("end" + currentSteer);
		} else {
			currentSteer = DriversUtils.alignToTrackAxis(sensors, 0.4D) * bias;
			if (!verbose)
				System.out.println("out" + currentSteer);
		}

		prevPosition = position;
		return currentSteer;
	}

	private double getInTrack(double trackPosition) {
		double steering = 0.0;
		desiredSpeed -= 30.0;
		if (trackPosition > 1) {
			steering = -0.1;
		} else if (trackPosition < -1) {
			steering = 0.1;
		}
		return steering;
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