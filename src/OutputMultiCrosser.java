
public class OutputMultiCrosser extends OutputMerger {

	protected int avg_run_length;
	
	
	public OutputMultiCrosser(Alphabet alphabet, int run_length) {
		super(alphabet);
		this.avg_run_length = run_length;
	}

	/**
	 * input: AAAAAAAAAAAAAAAAA BBBBBBBBBBBBBBBBB
	 * <p>
	 * output: AAABBBBAAAAABBBBA BBBAAAABBBBBAAAAB
	 */
	@Override
	public Output[] getChildren(Output o1, Output o2) {
		int length = Math.min(o1.size(), o2.size());
		Output res1 = new Output(length, o1.getGenerationNum()+1);
		Output res2 = new Output(length, o1.getGenerationNum()+1);
		
		boolean swapped = random.nextBoolean();
		double swap_chance = 1 / ((double)avg_run_length);
		for(int i = 0; i < length; i++) {
			if(random.nextDouble() <= swap_chance) {
				swapped = !swapped;
			}
			if(swapped) {
				res1.setValue(i, o2.getValue(i));
				res2.setValue(i, o1.getValue(i));
			} else {
				res1.setValue(i, o1.getValue(i));
				res2.setValue(i, o2.getValue(i));
			}
		}
		
		return new Output[] {res1, res2};
	}
}
