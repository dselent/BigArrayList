
import java.io.File;
import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;

public class Test
{
	private Random random;
	private static final int RANDOM_SEED = 0;

	public Test()
	{
		random = new Random(RANDOM_SEED);
	}

	//basic test to see if adding and getting work correctly
	public void test1() throws Exception
	{
		String MEMORY_PATH = "memory";
		int CACHE_SIZE = 1000000;
		int CACHE_BLOCKS = 4;
		BigArrayList.IOTypes IO_TYPE = BigArrayList.IOTypes.FST_OBJECT;

		long TEST_SIZE = 100000000;

		BigArrayList<Long> arrayList = new BigArrayList<Long>(CACHE_SIZE, CACHE_BLOCKS, MEMORY_PATH, IO_TYPE);

		for(long i=0; i<TEST_SIZE; i++)
		{
			arrayList.add(i);
		}

		System.out.println("Done adding");
	
		for(long i=0; i<TEST_SIZE; i++)
		{
			if(i != arrayList.get(i))
			{
				System.out.println("BAD GET" + i);
				System.out.println(arrayList.get(i));
				System.exit(-1);
			}
		}
	
		System.out.println("Done getting");

		arrayList.clearMemory();
	}

	//basic test to see if setting works correctly
	//random numbers only work in int range
	public void test2() throws Exception
	{
		String MEMORY_PATH = "memory";
		int CACHE_SIZE = /*1000*/10000000;
		int CACHE_BLOCKS = /*100*/4;
		BigArrayList.IOTypes IO_TYPE = BigArrayList.IOTypes.MMAP_FST_OBJECT;

		long TEST_SIZE = 100000000;
		int TEST_SIZE_INT = 100000000;
		int randomSets = 10;

		BigArrayList<Long> arrayList = new BigArrayList<Long>(CACHE_SIZE, CACHE_BLOCKS, MEMORY_PATH, IO_TYPE);

		for(long i=0; i<TEST_SIZE; i++)
		{
			arrayList.add(i);
		}

		System.out.println("Done adding");

		long[] elementArray = new long[randomSets];
		long[] valueArray = new long[randomSets];

		//set random elements
		for(int i=0; i<randomSets; i++)
		{
			long randomElement = random.nextInt(TEST_SIZE_INT);
			long randomValue = random.nextLong();

			arrayList.set(randomElement, randomValue);

			elementArray[i] = randomElement;
			valueArray[i] = randomValue;
		}

		System.out.println("Done Setting");

		//verify sets
		for(int i=0; i<randomSets; i++)
		{
			if(valueArray[i] != arrayList.get(elementArray[i]))
			{
				System.out.println("BAD SET " + i);
				System.out.println(elementArray[i]);
				System.out.println(valueArray[i]);
				System.out.println(arrayList.get(elementArray[i]));
				System.exit(-1);
			}
		}

		System.out.println("Done Verifying");

		arrayList.clearMemory();
	}

	public void addTest(long testSize, int cacheSize, int cacheBlocks) throws Exception
	{
		String MEMORY_PATH = "memory";
		int CACHE_SIZE = cacheSize;
		int CACHE_BLOCKS = cacheBlocks;
		BigArrayList.IOTypes IO_TYPE = BigArrayList.IOTypes.OBJECT;

		long TEST_SIZE = testSize;

		BigArrayList<Long> arrayList = new BigArrayList<Long>(CACHE_SIZE, CACHE_BLOCKS, MEMORY_PATH, IO_TYPE);

		long start = System.currentTimeMillis();

		for(long i=0; i<TEST_SIZE; i++)
		{
			arrayList.add(i);
		}

		long end = System.currentTimeMillis();

		double totalTime = end - start;
		double averageTime = totalTime / TEST_SIZE;

		System.out.println("Total time (ms) = " + totalTime);
		System.out.println("Average time to add (ms) = " + averageTime);


    	Runtime runtime = Runtime.getRuntime();

    	long totalMemory = runtime.totalMemory();
    	long freeMemory = runtime.freeMemory();

		System.out.println("Memory used (bytes) = " + (totalMemory - freeMemory));
		System.out.println("Folder Size (bytes) " + getFolderSize());

		arrayList.clearMemory();
	}

	public long getFolderSize()
	{
		long size = 0;
		File memoryFolder = new File("memory");
		File files[] = memoryFolder.listFiles();

    		for(int i=0; i<files.length; i++)
		{
			size = size + files[i].length();
		}

		return size;
	}


	public static void main(String args[]) throws Exception
	{
		long start = System.currentTimeMillis();
		
		Test t = new Test();
		t.test1();
		//t.test2();
		System.out.println(args[0] + "_" + args[1] + "_" + args[2]);
		//t.addTest(Long.parseLong(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		System.out.println("\n\n");
		
		long end = System.currentTimeMillis();
		
		long totalTime = (end - start);

		System.out.println("Total time (ms) = " + totalTime);
		

	}
}