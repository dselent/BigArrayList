
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Random;

public class TestArrayList
{

	public void test(long testSize)
	{
		//ADD

		long TEST_SIZE = testSize;

		ArrayList<Long> arrayList = new ArrayList<Long>();

		GregorianCalendar c1 = new GregorianCalendar();

		for(long i=0; i<TEST_SIZE; i++)
		{
			arrayList.add(i);
		}

		GregorianCalendar c2 = new GregorianCalendar();

		double totalTime = c2.getTimeInMillis() - c1.getTimeInMillis();

		System.out.println("Total ADD time (ms) = " + totalTime);

    		Runtime runtime = Runtime.getRuntime();

    		long totalMemory = runtime.totalMemory();
    		long freeMemory = runtime.freeMemory();

		System.out.println("Memory used (bytes) = " + (totalMemory - freeMemory));

		//////////////////////////////////////////////////////////////////////////////////

		//GET ORDERED

		GregorianCalendar c3 = new GregorianCalendar();

		for(int i=0; i<TEST_SIZE; i++)
		{
			arrayList.get(i);
		}

		GregorianCalendar c4 = new GregorianCalendar();

		double totalTime1 = c4.getTimeInMillis() - c3.getTimeInMillis();

		System.out.println("Total GET ORDERED time (ms) = " + totalTime1);


		///////////////////////////////////////////////////////////////////////////////////
		
		//GET RANDOM

		int[] randomElements = new int[(new Long(TEST_SIZE)).intValue()];
		Random r = new Random(0);

		for(int i=0; i<TEST_SIZE; i++)
		{
			randomElements[i] = r.nextInt((new Long(TEST_SIZE)).intValue());
		}

		GregorianCalendar c5 = new GregorianCalendar();

		for(int i=0; i<TEST_SIZE; i++)
		{
			arrayList.get(randomElements[i]);
		}

		GregorianCalendar c6 = new GregorianCalendar();

		double totalTime2 = c6.getTimeInMillis() - c5.getTimeInMillis();

		System.out.println("Total GET RANDOM time (ms) = " + totalTime2);

		////////////////////////////////////////////////////////////////////////////////

		//SET ORDERED

		GregorianCalendar c7 = new GregorianCalendar();

		for(int i=0; i<TEST_SIZE; i++)
		{
			arrayList.set(i, 1l);
		}

		GregorianCalendar c8 = new GregorianCalendar();

		double totalTime3 = c8.getTimeInMillis() - c7.getTimeInMillis();

		System.out.println("Total SET ORDERED time (ms) = " + totalTime3);

		////////////////////////////////////////////////////////////////////////////////

		//SET RANDOM

		int[] randomElements1 = new int[(new Long(TEST_SIZE)).intValue()];
		Random r1 = new Random(0);

		for(int i=0; i<TEST_SIZE; i++)
		{
			randomElements1[i] = r1.nextInt((new Long(TEST_SIZE)).intValue());
		}

		GregorianCalendar c9 = new GregorianCalendar();

		for(int i=0; i<TEST_SIZE; i++)
		{
			arrayList.set(randomElements1[i], 1l);
		}

		GregorianCalendar c10 = new GregorianCalendar();

		double totalTime4 = c10.getTimeInMillis() - c9.getTimeInMillis();

		System.out.println("Total SET RANDOM time (ms) = " + totalTime4);		
	}


	public static void main(String args[])
	{
		TestArrayList t = new TestArrayList();
		
		//input size, trial number
		System.out.println(args[0] + "_" + args[1]);
		t.test(Long.parseLong(args[0]));
		System.out.println("\n\n");

	}
}