import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class JumpMan {
	private static final int NUM_OBSTACLES = 20;
	
	private static final float MAN_RUN_SPEED = 5;
	private static final float MAN_MIN_SPEED = 5;
	private static final float MAN_MAX_SPEED = 40;
	private static final float MAN_JUMP_SPEED = 30;
	private static final float MAN_GRAVITY = 3;
	
	private static final int SCREEN_WIDTH = 640;
	private static final int SCREEN_HEIGHT = 480;
	private static final int LEVEL_WIDTH = 6000;
	
	private static final double TICK_LENGTH = 0.1;
	private static final int TICK_DELAY_MS = 4;
	
	private static final int FLOOR_LEVEL = 15;
	
	private static final int OUTPUT_SIZE = 6000;
	private static final int GENERATION_SIZE = 5000;
	
	private static final float MUTATION_CHANCE = 0.01f;
	private static final float CROSSOVER_CHANCE = 0.7f;
	
	private static JPanel panel;
	private static JFrame frame;
	
	private static Level level;
	private static Man man;
	
	private static Random random = new Random();
	
	public static void main(String[] args) {
		level = new Level();
		man = new Man();
		
		frame = new JFrame();
		panel = new JPanel() {
			public void paint(Graphics g) {
				paintPanel(g, this);
			}
		};
		panel.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
		frame.add(panel);
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		Alphabet alphabet = new Alphabet('0', '1', '2', '3', '4');
		alphabet.setWeights(20,1,1,10,10);
		OutputPairChooser chooser = OutputPairChooser.getNormalDistChooser(2.0);
		OutputMerger merger = new OutputMultiCrosser(alphabet, 40);
		merger.setMutationChance(MUTATION_CHANCE);
		merger.setSwitchChance(CROSSOVER_CHANCE);
		Simulation simul = new Simulation(getFitnessFunction(), alphabet, chooser, merger, getDisplayer(), GENERATION_SIZE, OUTPUT_SIZE);
		simul.setOutputFormatter(getFormatter());
		simul.run();
	}
	
	private static void paintPanel(Graphics g, JPanel p) {
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, p.getWidth(), p.getHeight());
		
		g.setColor(Color.BLACK);
		g.fillRect(0, level.floor_level, p.getWidth(), p.getHeight()-level.floor_level);
	
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect((int)man.x - level.camera_x, (int)man.y, (int)man.w, (int)man.h);
		
		g.setColor(Color.RED);
		for(Obstacle ob : level.obstacles) {
			g.fillRect(ob.x - level.camera_x, ob.y, ob.h, ob.h);
		}
		
		g.setColor(Color.GRAY);
		for(int i = 0; i < SCREEN_WIDTH / 100 + 2; i++) {
			g.fillRect(i*100 - level.camera_x % 100, level.floor_level, 4,  p.getHeight()-level.floor_level);
		}
		
		g.setColor(Color.GREEN.darker());
		g.fillRect(LEVEL_WIDTH - level.camera_x, level.floor_level, 8,  p.getHeight()-level.floor_level);
	}
	
	private static FitnessFunction getFitnessFunction() {
		return new FitnessFunction() {

			@Override
			public int getFitness(Output output) {
				return run(output,false);
			}
			
		};
	}
	
	private static Displayer getDisplayer() {
		return new Displayer() {

			@Override
			public void display(Output output) {
				run(output, true);
			}
			
		};
	}
	
	private static OutputFormatter getFormatter() {
		return new OutputFormatter() {

			@Override
			public String format(Output o) {
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < o.size() && i < 1000; i++) {
					sb.append(o.getValue(i));
				}
				return sb.toString();
			}
			
		};
	}
	
	private static int run(Output output, boolean display) {
		boolean isRunning = true;
		int i = 0;
		
		level.reset();
		man.reset();
		
		while(isRunning) {
			switch(output.getValue(i)){
			case '1':
				man.lowJump();
				break;
			case '2':
				man.highJump();
				break;
			case '3':
				man.slowDown();
			case '4':
				man.speedUp();
			default:
				//do nothing
			}
			man.update();
			
			if(display) {
				level.camera_x = (int)man.x - 64;
				panel.repaint();
				try {
					Thread.sleep(TICK_DELAY_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			i++;
			
			if(level.collidesWithObstacle(man)) {
				isRunning = false;
			}
			
			if(man.x > LEVEL_WIDTH) {
				man.x = LEVEL_WIDTH;
				isRunning = false;
			}
			
		}
		
		return (int)man.x;
	}
	
	public static class Level {
		public List<Obstacle> obstacles = new ArrayList<Obstacle>();
		public int camera_x;
		public int floor_level = SCREEN_HEIGHT - FLOOR_LEVEL;
		
		public Level() {
			reset();
			for(int i = 0; i < NUM_OBSTACLES; i++) {
				obstacles.add(new Obstacle());
			}
		}
		
		public void reset() {
			camera_x = 0;
		}
		
		public boolean collidesWithObstacle(Man man) {
			for(Obstacle ob : obstacles) {
				if(ob.collides(man)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	public static class Obstacle {
		public int x,y;
		public int w,h;
		
		public Obstacle() {
			x = 200 + random.nextInt(LEVEL_WIDTH - 200);
			w = 16 + random.nextInt(49);
			h = 16 + random.nextInt(49);
			y = SCREEN_HEIGHT - FLOOR_LEVEL - h;
		}
		
		public boolean collides(Man man) {
			return !(man.x > x + w || man.x + man.w < x || man.y > y + h || man.y + man.h < y);
		}
		
	}
	
	public static class Man {
		public float x,y;
		public float w=16, h=32;
		public float vx,vy;
		public float ay = MAN_GRAVITY;
		public boolean grounded;
		
		public Man() {
			reset();
		}
		
		public void reset() {
			x = 0;
			y = level.floor_level-h;
			vx = MAN_RUN_SPEED;
			vy = 0;
			grounded = false;
		}
		
		public void lowJump() {
			if(grounded) {
				vy = -3*MAN_JUMP_SPEED / 4;
				grounded = false;
			}
		}
		
		public void highJump() {
			if(grounded) {
				vy = -MAN_JUMP_SPEED;
				grounded = false;
			}
		}
		
		public void slowDown() {
			if(vx > MAN_MIN_SPEED) {
				vx -= 5;
			}
		}
		
		public void speedUp() {
			if(vx < MAN_MAX_SPEED) {
				vx += 5;
			}
		}
		
		public void update() {
			x += TICK_LENGTH * vx;
			
			vy += ay*TICK_LENGTH;
			y += vy*TICK_LENGTH;
			
			if(y + h >= level.floor_level) {
				y = level.floor_level - h;
				vy = 0;
				grounded = true;
			}
		}
	}
	
	

}
