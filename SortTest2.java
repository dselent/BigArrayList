import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;

//1 billion elements + 100k block size = Total ADD time (ms) = 146683 | 
//1 billion elements + 1 million block size = Total ADD time (ms) = 121145 | Total Sort time (ms) = 3223649
//1 billion elements + 10 million block size = Total ADD time (ms) = 477512 | Total Sort time (ms) = 6089067
public class SortTest2
{
	public static void main(String args[]) throws Exception
	{		
		Random rng = new Random(7);
		
		long listSize = 1000000000l;
		int blockSize = 100000;
		int numberOfBlocks = 2;
			
		BigArrayList<Integer> bal = new BigArrayList<Integer>(blockSize, numberOfBlocks, BigArrayList.IOTypes.FST_OBJECT);

		long start1 = System.currentTimeMillis();
		
		for(long j=0; j<listSize; j++)
		{
			bal.add(rng.nextInt());
		}
		
		long end1 = System.currentTimeMillis();
		long totalTime1 = end1 - start1;
		
		System.out.println("Total ADD time (ms) = " + totalTime1);
		
		long start2 = System.currentTimeMillis();
		bal = BigArrayList.sort(bal);
		long end2 = System.currentTimeMillis();
		long totalTime2 = end2 - start2;

		System.out.println("Total Sort time (ms) = " + totalTime2);
		
		bal.clearMemory();
	
	}
}
