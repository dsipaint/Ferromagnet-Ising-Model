import java.util.LinkedList;

public class States
{
	private LinkedList<int[][]> state_history;
	private int timestep;

	public States()
	{
		state_history = new LinkedList<int[][]>();
		timestep = 0;
	}

	public void add(int[][] state)
	{
		state_history.add(state);
	}

	public void tick()
	{
		timestep++;
	}

	public int[][] getStateAtTime(int timestep)
	{
		return state_history.get(timestep);
	}

	public int getTimestep()
	{
		return timestep;
	}

	public LinkedList<int[][]> getStateHistory()
	{
		return state_history;
	}
}
