
public class Simulation {
	public FitnessFunction function;
	public Alphabet alphabet;
	public OutputMerger merger;
	public OutputPairChooser chooser;
	public int generation_size;
	public int output_length;
	
	private int stopping_fitness = -1;
	private boolean best_persists = false;
	private OutputFormatter formatter = null;
	
	public Displayer displayer;
	
	public Simulation(FitnessFunction function, Alphabet alphabet, OutputPairChooser chooser, OutputMerger merger, Displayer displayer, int generation_size, int output_length) {
		this.function = function;
		this.alphabet = alphabet;
		this.generation_size = generation_size;
		this.output_length = output_length;
		this.merger = merger;
		this.chooser = chooser;
		this.displayer = displayer;
	}
	
	public void setOutputFormatter(OutputFormatter formatter) {
		this.formatter = formatter;
	}
	
	public void setBestPersists(boolean boo) {
		this.best_persists = boo;
	}
	
	public void setStoppingFitness(int fitness) {
		this.stopping_fitness = fitness;
	}
	
	public void run() {
		Generation current_gen = Generation.getFirstGeneration(generation_size, output_length, alphabet);
		boolean isFinished = false;
		Output best = null;
		int gen_num = 1;
		while(!isFinished) {
			current_gen.calculateFitnesses(function);
			best = current_gen.getBestOutput();
			
			System.out.print("gen: "+gen_num+"\tfitness: "+best.getFitness()+"\toutput: ");
			if(formatter != null) {
				System.out.println(formatter.format(best));
			} else {
				System.out.println(best);
			}
			
			displayer.display(best);
			if(stopping_fitness != -1 && best.getFitness() >= stopping_fitness) {
				isFinished = true;
				continue;
			}
			gen_num++;
			
			current_gen = current_gen.getNextGeneration(chooser, merger, best_persists);
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Optimal Solution Found!");
		while(true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			displayer.display(best);
			
		}
	}
}
