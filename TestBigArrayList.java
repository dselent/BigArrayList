
import java.io.File;
import java.util.GregorianCalendar;
import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;

public class TestBigArrayList
{


	public void test(long testSize, int cacheSize, int cacheBlocks)
	{
		//ADD

		String MEMORY_PATH = "memory";
		int CACHE_SIZE = cacheSize;
		int CACHE_BLOCKS = cacheBlocks;

		long TEST_SIZE = testSize;
		long cacheSizeLong = cacheSize;
		long cacheBlocksLong = cacheBlocks;
		long GET_SIZE_ORDERED = cacheSizeLong * 10;
		long SET_SIZE_ORDERED = cacheSizeLong * 10;
		int GET_SIZE_RANDOM = 10;
		int SET_SIZE_RANDOM = 10;

		if(GET_SIZE_ORDERED >= TEST_SIZE)
		{
			GET_SIZE_ORDERED = TEST_SIZE;
		}

		if(SET_SIZE_ORDERED >= TEST_SIZE)
		{
			SET_SIZE_ORDERED = TEST_SIZE;
		}


		BigArrayList<Long> arrayList = new BigArrayList<Long>(MEMORY_PATH, CACHE_SIZE, CACHE_BLOCKS);

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

		GregorianCalendar c3 = new GregorianCalendar();

		for(long i=0; i<GET_SIZE_ORDERED; i++)
		{
			arrayList.get(i);
		}


		GregorianCalendar c4 = new GregorianCalendar();

		double totalTime1 = c4.getTimeInMillis() - c3.getTimeInMillis();

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

		GregorianCalendar c5 = new GregorianCalendar();

		for(int i=0; i<GET_SIZE_RANDOM; i++)
		{
			arrayList.get(randomElements[i]);
		}


		GregorianCalendar c6 = new GregorianCalendar();

		double totalTime2 = c6.getTimeInMillis() - c5.getTimeInMillis();

		System.out.println("Total GET RANDOM time (ms) = " + totalTime2);

		if(TEST_SIZE > cacheBlocksLong * cacheSizeLong)
		{
			arrayList.flushMemory();
		}

		////////////////////////////////////////////////////////////////////////////////


		//SET ORDERED

		GregorianCalendar c7 = new GregorianCalendar();

		for(long i=0; i<SET_SIZE_ORDERED; i++)
		{
			arrayList.set(i, 1l);
		}


		GregorianCalendar c8 = new GregorianCalendar();

		double totalTime3 = c8.getTimeInMillis() - c7.getTimeInMillis();

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

		GregorianCalendar c9 = new GregorianCalendar();

		for(int i=0; i<SET_SIZE_RANDOM; i++)
		{
			arrayList.set(randomElements1[i], 1l);
		}


		GregorianCalendar c10 = new GregorianCalendar();

		double totalTime4 = c10.getTimeInMillis() - c9.getTimeInMillis();

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


	public static void main(String args[])
	{
		TestBigArrayList t = new TestBigArrayList();

		System.out.println(args[0] + "_" + args[1] + "_" + args[2] + "_" + args[3]);
		t.test(Long.parseLong(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		System.out.println("\n\n");

	}
}