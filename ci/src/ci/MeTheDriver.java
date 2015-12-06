package ci;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JTextField;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;

public class MeTheDriver extends AbstractDriver implements KeyListener {

	private static Double TopSpeed;
	// private NeuralNetwork neuralNetwork = new NeuralNetwork();
	private ArrayList<Double> input;
	private DefaultDriverGenome driverGenome;
	private Double prevSteering;
	private Double desiredSpeed;
	private Queue<Double> moSteer;
	private double steeringKey;
	private double speedKey;
	fooFrame f;

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
		input = new ArrayList<Double>();
		prevSteering = 0.0;
		moSteer = new LinkedList<Double>();
		steeringKey=0.0;
		speedKey=0.0;
		f=new fooFrame();
//		new Thread(f).start();
	}

	@Override
	public void control(Action action, SensorModel sensors) {
		action.steering = f.getSteering();
		action.accelerate=f.getSpeed();
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

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int id = e.getID();
		if (id == KeyEvent.VK_RIGHT) {
			steeringKey = 1.0;
		} else if (id == KeyEvent.VK_LEFT) {
			steeringKey = -1.0;
		} else if (id == KeyEvent.VK_DOWN) {
			this.speedKey = -1.0;
		} else if (id == KeyEvent.VK_UP) {
			this.speedKey = 1.0;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int id = e.getID();
		if (id == KeyEvent.VK_RIGHT) {
			steeringKey = 0.0;
		} else if (id == KeyEvent.VK_LEFT) {
			steeringKey = 0.0;
		} else if (id == KeyEvent.VK_DOWN) {
			this.speedKey = 0.0;
		} else if (id == KeyEvent.VK_UP) {
			this.speedKey = 0.0;
		}
	}
}