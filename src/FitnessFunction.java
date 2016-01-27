
public interface FitnessFunction {
	/**
	 * 
	 * @return integer greater than or equal to zero which determines the successfulness of the output. 
	 * A number closer to zero illustrates a more successful output.
	 */
	public int getFitness(Output output);
}
