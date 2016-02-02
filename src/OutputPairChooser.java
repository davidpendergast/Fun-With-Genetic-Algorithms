import java.util.Random;


public abstract class OutputPairChooser {
	public abstract Output[] choose(Generation gen);
	
	public static OutputPairChooser getNormalDistChooser(final double scaling) {
		return new OutputPairChooser() {

			Random rand = new Random();
			
			@Override
			public Output[] choose(Generation gen) {
				assert gen.isCalculatedAndSorted(): "Generation not calculated and sorted.";
				
				int len = gen.size();
				Output[] result = new Output[2];
				
				for(int i = 0; i < 2; i++) {
					double rand_dub = Math.abs(rand.nextGaussian());
					rand_dub *= scaling;
					
					int index = (int)(rand_dub * len);
					result[i] = gen.get(index % len);
				}
				
				return result;
				
			}
			
		};
	}
	
	public static OutputPairChooser getFitnessProportionalityChooser() {
		return new OutputPairChooser() {
			
			Random rand = new Random();

			@Override
			public Output[] choose(Generation gen) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		
	}
}
