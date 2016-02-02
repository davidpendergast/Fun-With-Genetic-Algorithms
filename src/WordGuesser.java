
public class WordGuesser {
	//public final static String target = "Testing with a short, reasonable sentence.";//
	public final static String target = "Testing with punctuation, UPPERCASE LETTERS, and of course, some numbers! 3.142957 and 2.7182818 are approximations of some very special constants. Lets add another sentence to add more complexity. And maybe another one as well.";
	
	
	public static void main(String[] args) {
		FitnessFunction function = getFitnessFunction();
		Alphabet alphabet = Alphabet.combine(null, 
				Alphabet.getLowerCaseLetters(), 
				Alphabet.getUpperCaseLetters(),
				Alphabet.getPunctuation(),
				Alphabet.getNumbers());
		Generation current_gen = Generation.getFirstGeneration(5000, target.length(), alphabet);
		OutputMerger merger = new OutputMultiCrosser(alphabet, 5);
		merger.setMutationChance(0.001);
		merger.setSwitchChance(.7);
		OutputPairChooser chooser = OutputPairChooser.getNormalDistChooser(0.5);
		boolean isFinished = false;
		int gen_num = 1;
		while(!isFinished) {
			current_gen.calculateFitnesses(function);
			Output best = current_gen.getBestOutput();
			System.out.println("gen: "+gen_num+"\tfitness: "+best.getFitness()+"\toutput: "+best);
			if(best.getFitness() == target.length()) {
				isFinished = true;
				continue;
			}
			gen_num++;
			
			current_gen = current_gen.getNextGeneration(chooser,merger);
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static FitnessFunction getFitnessFunction() {
		return new FitnessFunction() {
			@Override
			public int getFitness(Output output) {
				int count = 0;
				for(int i = 0; i < output.size() && i < target.length(); i++) {
					if(output.getValue(i) != target.charAt(i)) {
						//count += (target.length()- i) + 1;
						count++;
					}
				}
				return (target.length() - count);
			}
		};
	}
}
