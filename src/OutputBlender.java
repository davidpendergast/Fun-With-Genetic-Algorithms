
public class OutputBlender extends OutputMerger {

	public OutputBlender(Alphabet alphabet) {
		super(alphabet);
	}

	@Override
	public Output[] getChildren(Output o1, Output o2) {
		
		int length = o1.size();
		
		Output res1 = new Output(length, o1.getGeneration()+1);
		Output res2 = new Output(length, o1.getGeneration()+1);
		for(int i = 0; i < length; i++) {
			if(random.nextBoolean()) {
				res1.setValue(i, o1.getValue(i));
				res2.setValue(i, o2.getValue(i));
			} else {
				res1.setValue(i, o2.getValue(i));
				res2.setValue(i, o1.getValue(i));
			}
		}
		
		return new Output[] {res1, res2};
	}

}
