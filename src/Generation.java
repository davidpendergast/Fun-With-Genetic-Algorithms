import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class Generation implements Iterable<Output> {
	private List<Output> outputs;
	private List<Long> fitnessSums;	//TODO not public
	public long total_sum = 0;
	private Random random = new Random();
	private boolean isSorted = false;
	private static Comparator<Output> sorter = new Comparator<Output>() {

		@Override
		public int compare(Output o1, Output o2) {
			if(o1.getFitness() == -1 || o2.getFitness() == -1) {
				throw new RuntimeException("Cannot compare output with uncalculated fitness.");
			}
			return o2.getFitness() - o1.getFitness();
		}
		
	};
	
	public Generation() {
		outputs = new ArrayList<Output>();
		fitnessSums = new ArrayList<Long>();
	}
	
	public void add(Output output) {
		outputs.add(output);
	}
	
	public void calculateFitnesses(FitnessFunction function) {
		for(Output o : outputs) {
			o.calculateFitness(function);
		}
		sortByFitness();
		fillFitnessSums();
	}
	
	private void sortByFitness() {
		Collections.sort(outputs, sorter);
		isSorted = true;
	}
	
	private void fillFitnessSums() {
		fitnessSums.clear();
		long val = 0;
		for(Output o : outputs) {
			val += o.getFitness();
			fitnessSums.add(val);
		}
		total_sum = val;	
	}
	
	private int indexBehind(long val) {
		//yeah its linear whatever
		for(int i = 0; i < fitnessSums.size(); i++) {
			if(val <= fitnessSums.get(i)) {
				return i;
			}
		}
		System.out.println("Generation.indexBehind, something went wrong.");
		return 0;
	}
	
	public Output getBestOutput() {
		if(isSorted && outputs.size() > 0) {
			return outputs.get(0);
		} else {
			return null;
		}
	}
	
	public boolean contains(Output output) {
		return outputs.contains(output);
	}
	
	public Output getRandomOutput() {
		return outputs.get(random.nextInt(outputs.size()));
	}

	@Override
	public Iterator<Output> iterator() {
		return outputs.iterator();
	}
	
	public Generation getNextGeneration(OutputMerger merger) {
		return getNextGeneration(merger, false);
	}
	public Generation getNextGeneration(OutputMerger merger, boolean best_persists) {
		if(!isSorted) {
			throw new RuntimeException("Fitnesses must be calculated before the next generation can be produced.");
		}
		Generation gen = new Generation(); 
		int size = outputs.size();
		
		for(int i = 0; i < size/2; i++) {
			long rand1 = Math.abs(random.nextLong()) % total_sum;
			long rand2 = Math.abs(random.nextLong()) % total_sum;
			int rand_idx_1 = indexBehind(rand1);
			int rand_idx_2 = indexBehind(rand2);
			
			//System.out.println("Chose numbers: "+rand1+" --> "+rand_idx_1+", "+rand2+" --> "+rand_idx_2);
			
			Output[] children = merger.merge(outputs.get(rand_idx_1), outputs.get(rand_idx_2));
			
			gen.add(children[0]);
			gen.add(children[1]);
		}
		
		if(best_persists && !gen.contains(this.getBestOutput())) {
			gen.outputs.remove(random.nextInt(gen.outputs.size()));
			Output best = this.getBestOutput().clone();
			gen.add(best);
		}
		
		return gen;
	}
	
	public static Generation getFirstGeneration(int size, int output_size, Alphabet alphabet) {
		Generation gen = new Generation();
		for(int i = 0; i < size; i++) {
			gen.add(Output.getRandomOutput(output_size, 1, alphabet));
		}
		return gen;
	}
	
	public String toString() {
		StringBuilder res = new StringBuilder("[");
		for(Output o : outputs) {
			res.append(o.toString());
			res.append(",");
		}
		if(res.charAt(res.length()-1) == ',') {
			res.setLength(res.length()-1);
		}
		res.append("]");
		return res.toString();
	}
	
	public List<Integer> getFitnesses() {
		List<Integer> res = new ArrayList<Integer>();
		for(Output o : outputs) {
			res.add(o.getFitness());
		}
		return res;
	}

}
