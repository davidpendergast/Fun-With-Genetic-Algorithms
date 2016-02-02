import java.util.Random;


public abstract class OutputMerger {
	protected Random random = new Random();
	protected Alphabet alphabet;
	
	protected double mutation_chance = 0.01;
	protected double switch_chance = 0.7;
	
	public OutputMerger(Alphabet alphabet) {
		this.alphabet = alphabet;
	}
	
	public void setMutationChance(double chance) {
		this.mutation_chance = chance;
	}
	
	public void setSwitchChance(double chance) {
		this.switch_chance = chance;
	}
	
	public Output[] merge(Output o1, Output o2) {
		assert o1.size() == o2.size();
		assert o1.getGenerationNum() == o2.getGenerationNum();
		
		Output[] kids;
		if(random.nextDouble() <= switch_chance) {
			kids = getChildren(o1, o2);
		} else {
			kids = new Output[2];
			kids[0] = o1.createChild();
			kids[1] = o2.createChild();
		}
		
		if(random.nextDouble() <= mutation_chance) {
			mutate(kids[0]);
			mutate(kids[1]);
		}
		
		return kids;
	}
	
	protected void mutate(Output o) {
		for(int i = 0; i < o.size(); i++) {
			if(random.nextDouble() <= mutation_chance) {
				o.setValue(i, alphabet.getRandomChar());
			}
		}
	}
	
	protected abstract Output[] getChildren(Output o1, Output o2);
	
	

}
