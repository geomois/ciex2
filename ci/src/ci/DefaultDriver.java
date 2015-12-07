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
	DefaultDriver() {
		initialize();
	}

	public void loadGenome(IGenome genome) {
		driverGenome = (DefaultDriverGenome) genome;
		TopSpeed=driverGenome.getMyNN().getMaxSpeed();
	}

	public void initialize() {
		this.enableExtras(new AutomatedClutch());
		this.enableExtras(new AutomatedGearbox());
		this.enableExtras(new AutomatedRecovering());
		this.enableExtras(new ABS());
		input = new ArrayList<Double>();
		prevSteering=0.0;
		moSteer=new LinkedList<Double>();
	}

	@Override
	public void control(Action action, SensorModel sensors) {
		Double[] NNOutput = new Double[2];
		input.clear();
		for (int i = 0; i < sensors.getTrackEdgeSensors().length; i+=2) {
			input.add(sensors.getTrackEdgeSensors()[i]);
		}
		//central sensor
		input.add(sensors.getTrackEdgeSensors()[9]);
		NNOutput = driverGenome.getNNValue(input);
		desiredSpeed = NNOutput[0];
		
		
		
		action.steering = getCurrentSteering(sensors);
		desiredSpeed = NNOutput[0];
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

	}
	private Double getCurrentSteering(SensorModel sensors) {
		Double bias;
		Double currentSteer;
		System.out.println(sensors.getTrackPosition());
		if (!(sensors.getTrackPosition()<-1.0 || sensors.getTrackPosition()>1.0)) {
			System.out.println("in");
			Double dif = sensors.getTrackEdgeSensors()[0] - sensors.getTrackEdgeSensors()[18];
			if (dif > 0.7 && dif < 2.0) {
				bias = -0.08;
				desiredSpeed += 10.0;
			} else if (dif < -0.7 && dif > -2.0) {
				bias = 0.08;
				desiredSpeed += 10.0;
			} else if (dif < -3.0 && dif > -5.0) {
				desiredSpeed += 5.0;
				bias = 0.1;
			} else if (dif > 3.0 && dif < 5.0) {
				desiredSpeed += 5.0;
				bias = -0.1;
			} else if (dif < -5.0) {
				desiredSpeed -= 20.0;
				bias = 0.12;
			} else if (dif > 5.0) {
				desiredSpeed -= 20.0;
				bias = -0.12;
			} else {
				desiredSpeed += 20;
				bias = 0.0;
			}
			currentSteer = DriversUtils.alignToTrackAxis(sensors, 0.3D) + bias;
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
			this.addPrevSteering(currentSteer);
		}else
			currentSteer=getInTrack(sensors.getTrackPosition());

		return currentSteer;
	}
	
	

	private double getInTrack(double trackPosition) {
		double steering=0.0;
		desiredSpeed-=30.0;
		if(trackPosition>1){
			steering=-0.1;
		}else if(trackPosition<-1){
			steering=0.1;
		}
		return steering;
	}

	private void addPrevSteering(double current){
		if (moSteer.size()==3) {
			moSteer.remove();
		}
		moSteer.add(current);
		prevSteering=0.0;
		for(Double d:moSteer){
			prevSteering+=d;
		}
		prevSteering=prevSteering/(double)moSteer.size();
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
}