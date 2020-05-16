import java.util.Random;
import java.util.Scanner;

public class Main
{
	static  double h, J, T; //T = temperature (K)
	static final double k_b = 1.38E-23; //Boltzmann constant
	static final int arraysize = 5;
	static States states;

	//made average calculation true to logbook
	//made getPartitionFn and getProbability for this purpose
	//changed parameters of methods to rely on the global States object
	
	/*
	 * I tried to use BigDecimals in order to circumvent the Infinity issue
	 * this did not work- BigDecimal had the physical storage for the numbers,
	 * however the best workaround consisted in making a component of the partition
	 * function a BigDecimal, but then putting e to the power of that was not
	 * possible due to limits in the BigDecimal.pow(n) method (upper limit of 99999 or something)
	 * basically there is no way for this project to work in this language, at least not
	 * without big workarounds.
	 */
	
	public static void main(String[] args)
	{
		states = new States();
		int[][] current_state = new int[arraysize][arraysize];

		//set spins all up
		for(int i = 0; i < current_state.length; i++)
		{
			for(int j = 0; j < current_state[i].length; j++)
				current_state[i][j] = 1;
		}

		states.add(current_state);

		Scanner sc = new Scanner(System.in);
		boolean valid_response = false;

		System.out.println("Select h:");
		while(!valid_response)
		{
			String str = sc.nextLine();
			if(!str.matches("\\d+(\\.\\d+)?"))
				System.out.println("Invalid input- try again: ");
			else
			{
				h = Double.parseDouble(str);
				valid_response = true;
			}
		}
		valid_response = false;
		System.out.println("Select J: ");
		while(!valid_response)
		{
			String str = sc.nextLine();
			if(!str.matches("\\d+(\\.\\d+)?"))
				System.out.println("Invalid input- try again: ");
			else
			{
				J = Double.parseDouble(str);
				valid_response = true;
			}
		}
		valid_response = false;
		System.out.println("Select T: ");
		while(!valid_response)
		{
			String str = sc.nextLine();
			if(!str.matches("\\d+(\\.\\d+)?"))
				System.out.println("Invalid input- try again: ");
			else
			{
				T = Double.parseDouble(str);
				valid_response = true;
			}
		}
		
		sc.close();

		//system is equillibrated
		equillibrate(current_state);
		current_state = states.getStateAtTime(states.getTimestep());
		
		/*System.out.println("System equilibrated.");
		System.out.println("System energy: " + findEnergy(current_state));
		System.out.println("Average energy: " + findAvg("E", states.getTimestep() - 1, states.getTimestep()) + "\n");
		System.out.println("System magnetisation: " + findMagnetisation(current_state));
		System.out.println("Average magnetisation: " + findAvg("M", states.getTimestep() - 1, states.getTimestep()) + "\n");
		System.out.println("System:\n" + getStateAsString(current_state));*/
	}

	public static void equillibrate(int[][] state)
	{
		do
		{
			Random r = new Random();
			int randomi = r.nextInt(state.length); //random coordinates of a state
			int randomj = r.nextInt(state[0].length);
			double deltaE = getDeltaE(state, randomi, randomj);

			if(deltaE < 0)
				state[randomi][randomj] *= -1; //switch the spin
			else if(r.nextDouble() > Math.pow(Math.E, -(deltaE/(k_b*T)))) //if state isn't favourable, still a probability spin is flipped anyway
				state[randomi][randomj] *= -1;

			states.add(state); //save this new state
			states.tick();
		}
		while(Math.abs(findEnergy(state) - findAvg("E", states.getTimestep() - 1, states.getTimestep())) > 0.1  //while change in E and M from their averages > 0.1
				&& Math.abs(findMagnetisation(state) - findAvg("M", states.getTimestep() - 1, states.getTimestep())) > 0.1);
	}

	public static double getDeltaE(int[][] state, int i, int j)
	{
		double deltaE = 0;

		//nearest neighbours (with periodic boundaries)
		if(i + 1 < state.length)
			deltaE += state[i+1][j];
		else
			deltaE += state[0][j];

		if(i-1 >= 0)
			deltaE += state[i-1][j];
		else
			deltaE += state[state.length - 1][j];

		if(j+1 < state[i].length)
			deltaE += state[i][j+1];
		else
			deltaE += state[i][0];

		if(j-1 >= 0)
			deltaE += state[i][j-1];
		else
			deltaE += state[i][state[i].length - 1];



		deltaE *= J;
		deltaE += h;

		deltaE *= 2*state[i][j];

		return deltaE;
	}

	public static double findAvg(String quantity, int t_0, int t_1)
	{
		double avg = 0;

		switch(quantity)
		{
			case "E":
				//find average for E and return it (using equation in logbook)
				for(int t = t_0; t < t_1; t++)
					avg += getProbabilityAtTime(t)*findEnergy(states.getStateHistory().get(t));

				return avg;

			case "M":
				for(int t = t_0; t < t_1; t++)
					avg += getProbabilityAtTime(t)*findMagnetisation(states.getStateAtTime(t));

				return avg;
			default:
				return Double.NaN; //if quantity is unknown, return NaN
		}
	}

	public static double findEnergy(int[][] state)
	{
		int spin_mult_sum = 0;
		int spin_total = 0;

		for(int i = 0; i < state.length; i++)
		{
			for(int j = 0; j < state[i].length; j++)
			{
				//nearest neighbours (with periodic boundaries) delta SiSj calculation
				if(i + 1 < state.length)
					spin_mult_sum += state[i+1][j]*state[i][j];
				else
					spin_mult_sum += state[0][j]*state[i][j];

				if(i-1 >= 0)
					spin_mult_sum += state[i-1][j]*state[i][j];
				else
					spin_mult_sum += state[state.length - 1][j]*state[i][j];

				if(j+1 < state[i].length)
					spin_mult_sum += state[i][j+1]*state[i][j];
				else
					spin_mult_sum += state[i][0]*state[i][j];

				if(j-1 >= 0)
					spin_mult_sum += state[i][j-1]*state[i][j];
				else
					spin_mult_sum += state[i][state[i].length - 1]*state[i][j];

				spin_total += state[i][j];
			}

		}

		double E = (spin_mult_sum*-J) - (spin_total*h);

		return E;
	}

	public static double findMagnetisation(int[][] state)
	{
		double m = 0;

		for(int i = 0; i < state.length; i++)
		{
			for(int j = 0; j < state[i].length; j++)
				m += state[i][j];
		}

		m /= (state.length*state[0].length);

		return m;
	}

	public static String getStateAsString(int[][] state)
	{
		String str = "";

		for(int i = 0; i < state.length; i++)
		{
			for(int j = 0; j < state[i].length; j++)
				str += (state[i][j] == 1 ? "u" : "d");

			str += "\n";
		}

		return str;
	}
	
	//formula for partition function from logbook
	public static double getPartitionFn()
	{
		double Z = 0;
		
		for(int[][] state : states.getStateHistory())
		{
			Z += Math.pow(Math.E, -(findEnergy(state)/(k_b*T)));
			System.out.println(Math.pow(Math.E, -(findEnergy(state)/(k_b*T))));
		}
		
		return Z;
	}
	
	//formula of probability of a state
	public static double getProbabilityAtTime(int timestep)
	{
		double prob = 1/getPartitionFn();
		prob *= Math.pow(Math.E, -(findEnergy(states.getStateAtTime(timestep))/(k_b*T)));
		return prob;
	}
}
