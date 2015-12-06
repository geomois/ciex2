package ci;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class fooFrame extends JFrame implements Runnable, KeyListener {
	
	private double speedKey;
	private double steeringKey;
	private KeyListener l;

	public fooFrame(){
		JFrame frame = new JFrame("");
		setSize(200, 200);
		setVisible(true);
		addKeyListener(this);
		setFocusable(true);
	}
	
	@Override
	public void run() {
		while(true){
		}
	}

	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public double getSpeed(){
		return this.speedKey;
	}
	
	public double getSteering(){
		return this.steeringKey;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int id = e.getKeyCode();
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
		int id = e.getKeyCode();
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
