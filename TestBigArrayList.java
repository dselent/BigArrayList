
import java.io.File;
import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;

public class TestBigArrayList
{


	public void test(long testSize, int cacheSize, int cacheBlocks, int ioTypeInt, int warmupIterations) throws Exception
	{
		//ADD

		String MEMORY_PATH = "memory";
		int CACHE_SIZE = cacheSize;
		int CACHE_BLOCKS = cacheBlocks;

		long TEST_SIZE = testSize;
		long cacheSizeLong = cacheSize;
		long cacheBlocksLong = cacheBlocks;
		long GET_SIZE_ORDERED = cacheSizeLong * 1000;
		long SET_SIZE_ORDERED = cacheSizeLong * 1000;
		int GET_SIZE_RANDOM = 1000;
		int SET_SIZE_RANDOM = 1000;

		if(GET_SIZE_ORDERED >= TEST_SIZE)
		{
			GET_SIZE_ORDERED = TEST_SIZE;
		}

		if(SET_SIZE_ORDERED >= TEST_SIZE)
		{
			SET_SIZE_ORDERED = TEST_SIZE;
		}

		BigArrayList.IOTypes ioType;

		if(ioTypeInt == 1)
		{
			ioType = BigArrayList.IOTypes.OBJECT;
		}
		else if(ioTypeInt == 2)
		{
			ioType = BigArrayList.IOTypes.MMAP_OBJECT;
		}
		else if(ioTypeInt == 3)
		{
			ioType = BigArrayList.IOTypes.FST_OBJECT;
		}
		else if(ioTypeInt == 4)
		{
			ioType = BigArrayList.IOTypes.MMAP_FST_OBJECT;
		}
		else
		{
			ioType = BigArrayList.IOTypes.OBJECT;
		}

		for(int i=0; i<warmupIterations; i++)
		{
			BigArrayList<Long> arrayList = new BigArrayList<Long>(CACHE_SIZE, CACHE_BLOCKS, MEMORY_PATH, ioType);
			add(arrayList, TEST_SIZE, true);
			arrayList.clearMemory();
		}
			
		//
		
		BigArrayList<Long> arrayList = new BigArrayList<Long>(CACHE_SIZE, CACHE_BLOCKS, MEMORY_PATH, ioType);

		add(arrayList, TEST_SIZE, false);

		System.gc();
		
    	Runtime runtime = Runtime.getRuntime();

    	long totalMemory = runtime.totalMemory();
    	long freeMemory = runtime.freeMemory();

		System.out.println("Memory used (bytes) = " + (totalMemory - freeMemory));
		System.out.println("Folder Size (bytes) = " + getFolderSize());

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}

		System.gc();
				
		//////////////////////////////////////////////////////////////////////////////////

		//GET ORDERED

		for(int i=0; i<warmupIterations; i++)
		{
			sequentialGet(arrayList, GET_SIZE_ORDERED, true);
			
			if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
			{
				arrayList.flushMemory();
			}
		}
		
		//
		
		sequentialGet(arrayList, GET_SIZE_ORDERED, false);

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}
		
		System.gc();
		
		//////////////////////////////////////////////////////////////////////////////////
		
		
		//SET ORDERED

		for(int i=0; i<warmupIterations; i++)
		{
			sequentialSet(arrayList, SET_SIZE_ORDERED, true);
			
			if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
			{
				arrayList.flushMemory();
			}
		}
		
		//
		
		sequentialSet(arrayList, SET_SIZE_ORDERED, false);

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}
		
		System.gc();

		///////////////////////////////////////////////////////////////////////////////
			

		//GET RANDOM
		
		for(int i=0; i<warmupIterations; i++)
		{
			randomGet(arrayList, GET_SIZE_RANDOM, TEST_SIZE, true);
			
			if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
			{
				arrayList.flushMemory();
			}
		}
		
		//
		
		randomGet(arrayList, GET_SIZE_RANDOM, TEST_SIZE, false);

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}
		
		System.gc();
		
		///////////////////////////////////////////////////////////////////////////////


		//SET RANDOM

		for(int i=0; i<warmupIterations; i++)
		{
			randomSet(arrayList, SET_SIZE_RANDOM, TEST_SIZE, true);
			
			if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
			{
				arrayList.flushMemory();
			}
		}
		
		//
		
		randomSet(arrayList, SET_SIZE_RANDOM, TEST_SIZE, false);
		
		//////////////////////////////////////////////////////////////////////////////

		arrayList.clearMemory();
	}
	
	private void add(BigArrayList<Long> arrayList, long testSize, boolean warmup)
	{		
		if(!warmup)
		{
			System.out.println("START ADD");
		}

		long start = System.currentTimeMillis();
		
		for(long i=0; i<testSize; i++)
		{
			arrayList.add(i);
		}
		
		long end = System.currentTimeMillis();
		
		if(!warmup)
		{
			System.out.println("END ADD");
			long totalTime = end - start;
			System.out.println("Total ADD time (ms) = " + totalTime);
		}
	}
	
	private void sequentialGet(BigArrayList<Long> arrayList, long getSize, boolean warmup)
	{
		if(!warmup)
		{
			System.out.println("START SEQUENTIAL GET");
		}
		
		long start = System.currentTimeMillis();

		for(long i=0; i<getSize; i++)
		{
			arrayList.get(i);
		}

		long end = System.currentTimeMillis();
		
		if(!warmup)
		{
			System.out.println("END SEQUENTIAL GET");
			long totalTime = end - start;
			System.out.println("Total SEQUENTIAL GET time (ms) = " + totalTime);
		}
	}
	
	private void sequentialSet(BigArrayList<Long> arrayList, long setSize, boolean warmup)
	{
		if(!warmup)
		{
			System.out.println("START SEQUENTIAL SET");
		}
		
		long start = System.currentTimeMillis();

		for(long i=0; i<setSize; i++)
		{
			arrayList.set(i, 1l);
		}

		long end = System.currentTimeMillis();
		
		if(!warmup)
		{
			System.out.println("END SEQUENTIAL SET");
			long totalTime = end - start;
			System.out.println("Total SEQUENTIAL SET time (ms) = " + totalTime);
		}
	}
	
	private void randomGet(BigArrayList<Long> arrayList, int getSize, long testSize, boolean warmup)
	{
		
		int[] randomElements = new int[getSize];
		Random r = new Random(0);

		for(int i=0; i<getSize; i++)
		{
			if(testSize >= Integer.MAX_VALUE)
			{
				randomElements[i] = r.nextInt(Integer.MAX_VALUE);
			}
			else
			{
				randomElements[i] = r.nextInt((new Long(testSize)).intValue());
			}
		}
		
		if(!warmup)
		{
			System.out.println("START RANDOM GET");
		}
		
		long start = System.currentTimeMillis();

		for(int i=0; i<getSize; i++)
		{
			arrayList.get(randomElements[i]);
		}

		long end = System.currentTimeMillis();
		
		if(!warmup)
		{
			System.out.println("END RANDOM GET");
			long totalTime = end - start;
			System.out.println("Total RANDOM GET time (ms) = " + totalTime);
		}
	}
	
	private void randomSet(BigArrayList<Long> arrayList, int setSize, long testSize, boolean warmup)
	{
		long[] randomElements = new long[(new Long(setSize)).intValue()];
		Random r1 = new Random(0);

		for(int i=0; i<setSize; i++)
		{
			if(testSize >= Integer.MAX_VALUE)
			{
				randomElements[i] = r1.nextInt(Integer.MAX_VALUE);
			}
			else
			{
				randomElements[i] = r1.nextInt((new Long(testSize)).intValue());
			}
		}
		
		if(!warmup)
		{
			System.out.println("START RANDOM SET");
		}
		
		long start = System.currentTimeMillis();

		for(int i=0; i<setSize; i++)
		{
			arrayList.set(randomElements[i], 1l);
		}

		long end = System.currentTimeMillis();
		
		if(!warmup)
		{
			System.out.println("END RANDOM SET");
			long totalTime = end - start;
			System.out.println("Total RANDOM SET time (ms) = " + totalTime);
		}
	}

	public long getFolderSize()
	{
		long size = 0;
		File memoryFolder = new File("memory");
		File files[] = memoryFolder.listFiles();

		
		if(files.length > 0)
		{
			size = size + files[0].length();

			long actualSize = 4096l;

			while(actualSize < size)
			{
				actualSize = actualSize + 4096;
			}

			size = actualSize * files.length;
		}
		
		return size;
	}


	public static void main(String args[]) throws Exception
	{
		TestBigArrayList t = new TestBigArrayList();

		
		System.out.println(args[0] + "_" + args[1] + "_" + args[2] + "_" + args[3] + "_" + args[4] + "_" + args[5]);
		t.test(Long.parseLong(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
		System.out.println("\n\n");
	}
}