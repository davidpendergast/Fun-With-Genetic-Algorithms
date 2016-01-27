import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class BallShooter {
	
	public static final int WIDTH = 1500;
	public static final int HEIGHT = 600;
	
	public static final boolean TERMINATE_ON_OPTIMAL = false;
	public static final boolean BEST_PERSISTS = false;
	
	public static final int MAX_BALLS = 5;
	public static final int MIN_BALLS = 5;
	
	public static final int GENERATION_SIZE = 300;
	public static final int OUTPUT_LENGTH = 46;
	
	public static final float BALL_DAMPENING = 0.1f;
	public static final float EDGE_DAMPENING = 0.4f;
	
	public static final float SPEED_PUNISHMENT = .25f;
	
	public static final int BALL_VALUE = 200;
	public static final int BALL_TOUCH_BONUS = 400;
	
	public static final double MUTATION_CHANCE = 0.05;
	public static final double CROSSOVER_CHANCE = .7;
	
	public static final Ball ball = new Ball(15,300,10);
	public static final Level level = new Level();
	public static final int target = (int)(Math.random() * (WIDTH - 100) + 50);
	public static final long DELAY_MILLIS = 5;
	public static final int DELAY_NANO = 0;
	public static final int TIME_LIMIT = 50;
	public static final float TICK_TIME = 0.01f;
	
	public static final boolean use_landing_target = false;
	
	public static final int[][] path_map = new int[WIDTH][HEIGHT];
	public static final BufferedImage background = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
	
	
	public static final CopyOnWriteArrayList<Integer> hits = new CopyOnWriteArrayList<Integer>();
	
	final static JFrame frame = new JFrame();
	final static JPanel panel = new JPanel(){
		public void paint(Graphics g) {
			paintPanel(g,this);
		}
	};
	
	public static void main(String[] args) {
		panel.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		frame.add(panel);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		Alphabet nums = Alphabet.getBits();
		//OutputMerger merger = new OutputMultiCrosser(nums, 10);
		OutputMerger merger = new OutputBlender(nums);
		Displayer displayer = getDisplayer();
		merger.setMutationChance(MUTATION_CHANCE);
		merger.setSwitchChance(CROSSOVER_CHANCE);
		Simulation simul = new Simulation(getFitnessFunction(), nums, merger, displayer, GENERATION_SIZE, OUTPUT_LENGTH);
		if(TERMINATE_ON_OPTIMAL) {
			int max_fitness = level.numBalls() * BALL_VALUE;
			simul.setStoppingFitness(max_fitness);
		}
		simul.setBestPersists(BEST_PERSISTS);
		simul.setOutputFormatter(getOutputFormatter());
		simul.run();
	}
	
	public static FitnessFunction getFitnessFunction() {
		return new FitnessFunction() {

			@Override
			public int getFitness(Output output) {
				return run(output, false);	
			}
			
		};
	}
	
	private static Displayer getDisplayer() {
		return new Displayer() {

			@Override
			public void display(Output output) {
				Graphics g = background.getGraphics();
				g.setColor(new Color(25,25,25));
				g.fillRect(0, 0, WIDTH, HEIGHT);
				for(int x = 0; x < WIDTH; x++) {
					for(int y = 0; y < HEIGHT; y++) {
						if(path_map[x][y] > 0) {
							switch(path_map[x][y]) {
							case 1:
								g.setColor(new Color(75,75,125));
								break;
							case 2:
								g.setColor(new Color(100,100,150));
								break;
							case 3:
								g.setColor(new Color(125,125,175));
								break;
							case 4:
								g.setColor(new Color(150,150,200));
								break;
							case 5:
								g.setColor(new Color(175,175,225));
								break;
							default:
								g.setColor(new Color(200,200,255));
							}
							g.fillRect(x, y, 1, 1);
							
						}
					}
				}
				clearPathMap();
				
				run(output, true);
			}
			
		};
	}
	
	private static int run(Output output, boolean display) {
		ball.reset();
		level.reset();
		
		double[] nums = decodeOutput(output);
		ball.vx = (float)nums[0];
		ball.vy = (float)nums[1];
		
		int initial_vx = Math.abs((int)ball.vx);
		int initial_vy = Math.abs((int)ball.vy);
		
		long time = System.currentTimeMillis();
		
		while(Math.abs(ball.vx) > 0.001 || Math.abs(ball.vy) > 1) {
			ball.applyPhysics(TICK_TIME);
			if(display){
				panel.repaint();
				sleep();	
			} else {
				incPathMap((int)ball.x, (int)ball.y);
				if(System.currentTimeMillis() - time > TIME_LIMIT) {
					System.out.println("Output went overtime!");
					return 0;
				}
			}
		}
		
		hits.add((int)ball.x);
		if(display) {
			hits.clear();
		}
		
		float dx = ball.x - target; 
		float dy = 0;
		
		int fitness = use_landing_target ? 300 - (int)Math.sqrt(dx*dx + dy*dy) : 300;
		for(Ball b : level.balls) {
			fitness += BALL_VALUE - (int)(b.distance.floatValue());
			if(b.distance.floatValue() == 0) {
				fitness += BALL_TOUCH_BONUS;
			}
		}
		fitness -= initial_vx*SPEED_PUNISHMENT;
		fitness -= initial_vy*SPEED_PUNISHMENT;
		return fitness;
	}
	
	private static double[] decodeOutput(Output output) {
//		float vx = (float)((output.getValue(0)-'0')*100 + (output.getValue(1)-'0')*10 + (output.getValue(2)-'0') + (output.getValue(3)-'0')*0.1 + (output.getValue(4)-'0')*0.01);
//		float vy = -(float)((output.getValue(5)-'0')*100 + (output.getValue(6)-'0')*10 + (output.getValue(7)-'0') + (output.getValue(8)-'0')*0.1 + (output.getValue(9)-'0')*0.01);
		boolean[] bools = new boolean[output.size()];
		for(int i = 0; i < bools.length; i++) {
			bools[i] = toBool(output.getValue(i));
		}
		int len = bools.length / 2;
		boolean[] arr1 = Arrays.copyOfRange(bools, 0, len);
		boolean[] arr2 = Arrays.copyOfRange(bools, len, len*2);
		
		return new double[]{toSignedDouble(arr1, 1000), toSignedDouble(arr2, 1000)};
	}
	
	private static OutputFormatter getOutputFormatter() {
		return new OutputFormatter() {

			@Override
			public String format(Output o) {
				double[] nums = decodeOutput(o);
				return nums[0] +", "+nums[1]+" = "+o;
			}
			
		};
	}
	
	private static boolean toBool(char c) {
		return c == '1';
	}
	
	private static double toSignedDouble(boolean[] arr, int dec_places) {
		if(arr.length <= 1) {
			return 0;
		}
		int mult = arr[0] ? -1 : 1;
		int res = grayToBinary32(toInt(Arrays.copyOfRange(arr, 1, arr.length)));
		return ((double)mult*res)/((double) dec_places);
	}
	
	private static int toInt(boolean[] arr) {
		int n = 0;
		for(boolean b : arr) {
			n = (n << 1) | (b ? 1 : 0);
		}
		return n;
	}
	
	private static int grayToBinary32(int num) {
		num = num ^ (num >> 16);
	    num = num ^ (num >> 8);
	    num = num ^ (num >> 4);
	    num = num ^ (num >> 2);
	    num = num ^ (num >> 1);
	    return num;
	    
	}
	
	private static void paintPanel(Graphics g, JPanel p) {
		int height = p.getHeight();
		int width = p.getWidth();
		
		g.drawImage(background, 0, 0, null);
		
		g.setColor(Color.BLACK);
		g.fillRect(0, level.floor_level, width, height-level.floor_level);
		g.setColor(Color.BLACK);
		g.fillOval((int)(ball.x-ball.radius), (int)(ball.y-ball.radius), ball.radius*2, ball.radius*2);
		if(use_landing_target) {
			g.setColor(Color.GREEN);
			for(Integer i : hits) {
				g.fillRect(i, level.floor_level, 1, height-level.floor_level);
			}
		}
		for(Ball b : level.balls) {
			g.setColor(b.distance == 0f ? Color.GREEN : new Color(Math.min((int)(b.distance.floatValue()), 255), 125, 0));//new Color((int)(b.distance % 255), 255, 0));
			g.fillOval((int)(b.x-b.radius), (int)(b.y-b.radius), b.radius*2, b.radius*2);
		}
		if(use_landing_target) {
			g.setColor(Color.RED);
			g.fillRect(target-2, level.floor_level, 4, height-level.floor_level);
		}
	}
	
	private static void incPathMap(int x, int y) {
		if(x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
			path_map[x][y] += 1;
		}
	}
	
	private static void clearPathMap() {
		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < HEIGHT; y++) {
				path_map[x][y] = 0;
			}
		}
	}
	
	private static class Ball {
		public int radius;
		public float x_orig;
		public float y_orig;
		public float x;
		public float y;
		
		public float vx;
		public float vy;
		
		public float ay = 500;
		
		public Float distance;
		
		public Ball(int x_orig, int y_orig, int radius) {
			this.x_orig = x_orig;
			this.y_orig = y_orig;
			this.radius = radius;
			reset();
		}
		
		public void reset() {
			x = x_orig;
			y = y_orig;
			vx = 0;
			vy = 0;
			distance = 1000000f;
		}
		
		public void hitFloor() {
			vy = -(int)(vy * .6);
			vx = (int)(vx * .9);
		}
		
		public void hitWall() {
			vx = -(int)(vx * .75);
		}
		
		public void applyPhysics(float dt) {
			x = x + vx*dt;
			vy = vy + ay*dt;
			y = y + vy*dt;
			
			if(x - radius < 0) {
				hitWall();
				x = radius;
			}
			if(x + radius > WIDTH) {
				hitWall();
				x = WIDTH - radius;
			}
			if(y + radius > level.floor_level) {
				hitFloor();
				y = level.floor_level - radius;
			}
			if(y - radius < 0) {
				hitFloor();
				y = radius;
			}
			for(Ball b : level.balls) {
				if(ballCollide(this, b)) {
					b.distance = 0f;
					solveCollision(this, b);
				} else {
					b.distance = Math.min(b.distance, distance(this, b));
				}
			}
		}
	}
	
	private static class Level {
		public int floor_level;
		
		ArrayList<Ball> balls = new ArrayList<Ball>();
		
		public Level() {
			int num_balls = (int)(Math.random()*(MAX_BALLS-MIN_BALLS) + MIN_BALLS);
			for(int i = 0; i < num_balls; i++){
				Ball new_ball = new Ball((int)(Math.random() * (WIDTH - 100) + 50), (int)(Math.random() * (HEIGHT - 100) + 50), (int)(Math.random()*20 + 10));
				boolean collides = false;
				for(Ball b : balls) {
					if(ballCollide(new_ball, b)) {
						collides = true;
						break;
					}
				}
				if(!collides) {
					balls.add(new_ball);
				} 
			}
			reset();
		}
		
		public int numBalls() {
			return balls.size();
		}
		
		public void reset() {
			floor_level = HEIGHT;
			for(Ball b : balls) {
				b.distance = 1000000f;
			}
		}
	}
	
	public static void sleep() {
		try {
			Thread.sleep(DELAY_MILLIS, DELAY_NANO);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean ballCollide(Ball b1, Ball b2) {
		return distance(b1, b2) < b1.radius + b2.radius;
	}
	
	private static void solveCollision(Ball b1, Ball b2) {
		float dx = b2.x - b1.x;
		float dy = b2.y - b1.y;
		
		float mag = (float)Math.sqrt(dx*dx + dy*dy);
		if(mag < 0.00001f)
			return;
		
		dx /= mag;
		dy /= mag;
		
		float vx = b1.vx;
		float vy = b1.vy;
		
		float angle = Math.abs(angleBetween(vx, vy, dx, dy));
		if(angle > Math.PI/2) {
			angle = (float)Math.PI - angle;
		}
		float ratio = (float)((Math.PI/2-angle) / (Math.PI/2));
		
		b1.vx = (float)(b1.vx - b1.vx*BALL_DAMPENING*ratio);
		b1.vy = (float)(b1.vy - b1.vy*BALL_DAMPENING*ratio);
		
		vx = b1.vx;
		vy = b1.vy;
		
		b1.vx = vx - 2*(vx*dx + vy*dy)*dx;
		b1.vy = vy - 2*(vx*dx + vy*dy)*dy;
		
		b1.x = b2.x - dx*(b1.radius + b2.radius);
		b1.y = b2.y - dy*(b1.radius + b2.radius);
	}
	
	private static float distance(Ball b1, Ball b2) {
		float dx = b1.x - b2.x;
		float dy = b1.y - b2.y;
		return (float)Math.sqrt(dx*dx + dy*dy);
	}
	
	private static float angleBetween(float x1, float y1, float x2, float y2) {
		float mag1 = (float)Math.sqrt(x1*x1 + y1*y1);
		float mag2 = (float)Math.sqrt(x2*x2 + y2*y2);
		
		if(mag1 < 0.0001f || mag2 < 0.0001f) {
			return 0;
		}
		
		x1 /= mag1;
		y1 /= mag1;
		
		x2 /= mag2;
		y2 /= mag2;
		
		float cosTheta = x1*x2 + y1*y2;
		cosTheta = Math.min(1, cosTheta);
		cosTheta = Math.max(cosTheta, -1);
		return (float)Math.acos(cosTheta);
	}
}
