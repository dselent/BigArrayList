
import java.io.File;
import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;

public class TestBigArrayList
{


	public void test(long testSize, int cacheSize, int cacheBlocks, int ioTypeInt) throws Exception
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


		BigArrayList<Long> arrayList = new BigArrayList<Long>(CACHE_SIZE, CACHE_BLOCKS, MEMORY_PATH, ioType);

		long start1 = System.currentTimeMillis();

		for(long i=0; i<TEST_SIZE; i++)
		{
			arrayList.add(i);
		}

		long end1 = System.currentTimeMillis();

		long totalTime = end1 - start1;

		System.out.println("Total ADD time (ms) = " + totalTime);


    		Runtime runtime = Runtime.getRuntime();

    		long totalMemory = runtime.totalMemory();
    		long freeMemory = runtime.freeMemory();

		System.out.println("Memory used (bytes) = " + (totalMemory - freeMemory));


		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}

		System.out.println("Folder Size (bytes) = " + getFolderSize());

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}

		
		//////////////////////////////////////////////////////////////////////////////////

		//GET ORDERED

		long start2 = System.currentTimeMillis();

		for(long i=0; i<GET_SIZE_ORDERED; i++)
		{
			arrayList.get(i);
		}


		long end2 = System.currentTimeMillis();

		long totalTime1 = end2 - start2;

		System.out.println("Total GET ORDERED time (ms) = " + totalTime1);

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}

		////////////////////////////////////////////////////////////////////////////////


		//GET RANDOM

		int[] randomElements = new int[GET_SIZE_RANDOM];
		Random r = new Random(0);

		for(int i=0; i<GET_SIZE_RANDOM; i++)
		{
			if(TEST_SIZE >= Integer.MAX_VALUE)
			{
				randomElements[i] = r.nextInt(Integer.MAX_VALUE);
			}
			else
			{
				randomElements[i] = r.nextInt((new Long(TEST_SIZE)).intValue());
			}
		}

		long start3 = System.currentTimeMillis();

		for(int i=0; i<GET_SIZE_RANDOM; i++)
		{
			arrayList.get(randomElements[i]);
		}


		long end3 = System.currentTimeMillis();

		long totalTime2 = end3 - start3;

		System.out.println("Total GET RANDOM time (ms) = " + totalTime2);

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}

		////////////////////////////////////////////////////////////////////////////////


		//SET ORDERED

		long start4 = System.currentTimeMillis();

		for(long i=0; i<SET_SIZE_ORDERED; i++)
		{
			arrayList.set(i, 1l);
		}


		long end4 = System.currentTimeMillis();

		long totalTime3 = end4 - start4;

		System.out.println("Total SET ORDERED time (ms) = " + totalTime3);

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}

		///////////////////////////////////////////////////////////////////////////////

		//SET RANDOM

		long[] randomElements1 = new long[(new Long(SET_SIZE_RANDOM)).intValue()];
		Random r1 = new Random(0);

		for(int i=0; i<SET_SIZE_RANDOM; i++)
		{
			if(TEST_SIZE >= Integer.MAX_VALUE)
			{
				randomElements1[i] = r1.nextInt(Integer.MAX_VALUE);
			}
			else
			{
				randomElements1[i] = r1.nextInt((new Long(TEST_SIZE)).intValue());
			}
		}

		long start5 = System.currentTimeMillis();

		for(int i=0; i<SET_SIZE_RANDOM; i++)
		{
			arrayList.set(randomElements1[i], 1l);
		}


		long end5 = System.currentTimeMillis();

		long totalTime4 = end5 - start5;

		System.out.println("Total SET RANDOM time (ms) = " + totalTime4);

		//////////////////////////////////////////////////////////////////////////////

		
		
		arrayList.clearMemory();
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

		
		System.out.println(args[0] + "_" + args[1] + "_" + args[2] + "_" + args[3] + "_" + args[4]);
		t.test(Long.parseLong(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		System.out.println("\n\n");
	}
}