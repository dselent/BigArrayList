
import java.util.ArrayList;
import java.util.Random;

public class TestArrayList
{

	public void test(long testSize)
	{
		//ADD

		long TEST_SIZE = testSize;
		int NUMBER_OF_ITERATIONS = 100;

		ArrayList<Long> arrayList = new ArrayList<Long>();

		long start1 = System.currentTimeMillis();

		for(long i=0; i<TEST_SIZE; i++)
		{
			arrayList.add(i);
		}

		long end1 = System.currentTimeMillis();

		long totalTime1 = end1 - start1;

		System.out.println("Total ADD time (ms) = " + totalTime1);

    		Runtime runtime = Runtime.getRuntime();

    		long totalMemory = runtime.totalMemory();
    		long freeMemory = runtime.freeMemory();

		System.out.println("Memory used (bytes) = " + (totalMemory - freeMemory));

		//////////////////////////////////////////////////////////////////////////////////

		//GET ORDERED

		long start2 = System.currentTimeMillis();

		for(int iterations=0; iterations<NUMBER_OF_ITERATIONS; iterations++)
		{
			for(int i=0; i<TEST_SIZE; i++)
			{
				arrayList.get(i);
			}
		}

		long end2 = System.currentTimeMillis();

		long totalTime2 = end2 - start2;

		System.out.println("Total GET ORDERED time (ms) = " + totalTime2);


		///////////////////////////////////////////////////////////////////////////////////
		
		//GET RANDOM

		int[] randomElements = new int[(new Long(TEST_SIZE)).intValue()];
		Random r = new Random(0);

		for(int i=0; i<TEST_SIZE; i++)
		{
			randomElements[i] = r.nextInt((new Long(TEST_SIZE)).intValue());
		}

		long start3 = System.currentTimeMillis();

		for(int iterations=0; iterations<NUMBER_OF_ITERATIONS; iterations++)
		{
			for(int i=0; i<TEST_SIZE; i++)
			{
				arrayList.get(randomElements[i]);
			}
		}

		long end3 = System.currentTimeMillis();

		long totalTime3 = end3 - start3;

		System.out.println("Total GET RANDOM time (ms) = " + totalTime3);

		////////////////////////////////////////////////////////////////////////////////

		//SET ORDERED

		long start4 = System.currentTimeMillis();

		for(int iterations=0; iterations<NUMBER_OF_ITERATIONS; iterations++)
		{
			for(int i=0; i<TEST_SIZE; i++)
			{
				arrayList.set(i, 1l);
			}
		}

		long end4 = System.currentTimeMillis();

		long totalTime4 = end4 - start4;

		System.out.println("Total SET ORDERED time (ms) = " + totalTime4);

		////////////////////////////////////////////////////////////////////////////////

		//SET RANDOM

		int[] randomElements1 = new int[(new Long(TEST_SIZE)).intValue()];
		Random r1 = new Random(0);

		for(int i=0; i<TEST_SIZE; i++)
		{
			randomElements1[i] = r1.nextInt((new Long(TEST_SIZE)).intValue());
		}

		long start5 = System.currentTimeMillis();

		for(int iterations=0; iterations<NUMBER_OF_ITERATIONS; iterations++)
		{
			for(int i=0; i<TEST_SIZE; i++)
			{
				arrayList.set(randomElements1[i], 1l);
			}
		}

		long end4 = System.currentTimeMillis();

		long totalTime5 = end5 - start5;

		System.out.println("Total SET RANDOM time (ms) = " + totalTime5);		
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