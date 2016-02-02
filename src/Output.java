import java.util.Arrays;


public class Output {
	
	private int length;
	private char[] data;
	private int fitness = -1;
	private int generation_num;
	
	public Output(int length, int generation) {
		this.length = length;
		this.data = new char[length];
	}
	
	public Output(char[] data, int generation) {
		this.length = data.length;
		this.data = data;
		this.generation_num = generation;
	}
	
	public int size() {
		return this.length;
	}
	
	public int getGenerationNum() {
		return generation_num;
	}
	
	public void setValue(int index, char value) {
		if(index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("index="+index+" not in range [0,"+length+"]");
		} else {
			data[index] = value;
		}
	}
	
	public char getValue(int index) {
		if(index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("index="+index+" not in range [0,"+length+"]");
		} else {
			return data[index];
		}
	}
	
	public int getFitness() {
		return this.fitness;
	}
	
	public void calculateFitness(FitnessFunction function) {
		if(fitness != -1) {
			throw new RuntimeException("Fitness already calculated!");
		} else {
			int num = function.getFitness(this);
			fitness = num > 0 ? num : 1;	//fitness must be positive
		}
	}
	
	public static Output getRandomOutput(int length, int gen, Alphabet alphabet) {
		Output output = new Output(length, gen);
		for(int i = 0; i < length; i++) {
			output.setValue(i, alphabet.getRandomChar());
		}
		return output;
	}
	
	public String toString() {
		return new String(data);
	}
	
	public Output createChild() {
		return new Output(this.data, this.generation_num + 1);
	}
	
	public boolean equals(Object other) {
		if(other instanceof Output) {
			return Arrays.equals(this.data, ((Output)other).data);
		} else {
			return super.equals(other);
		}
	}
	
	

}
