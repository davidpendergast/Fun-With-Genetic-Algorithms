
public class OutputCrosser extends OutputMerger {
	
	public OutputCrosser(Alphabet alphabet) {
		super(alphabet);
	}

	/**
	 * input: AAAAAAAAAAAAAAAAA BBBBBBBBBBBBBBBBB
	 * <p>
	 * output: AAAABBBBBBBBBBBBBB BBBAAAAAAAAAAAAA
	 */
	public Output[] getChildren(Output o1, Output o2) {
		int length = Math.min(o1.size(), o2.size());
		Output res1 = new Output(length, o1.getGenerationNum()+1);
		Output res2 = new Output(length, o1.getGenerationNum()+1);
		
		int cross_at =  random.nextInt(length);
		
		for(int i = 0; i < length; i++) {
			if(i < cross_at) {
				res1.setValue(i, o1.getValue(i));
				res2.setValue(i, o2.getValue(i));
			} else {
				res1.setValue(i, o2.getValue(i));
				res2.setValue(i, o1.getValue(i));
			}
		}
		
		return new Output[]{res1, res2};
	}
}
