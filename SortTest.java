import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;


public class SortTest
{
	//generate a random list size = 10 - 2050
	//generate a random block size = 50 - 3000
	//generate a random number of blocks 2 - 100
	//add elements
	//sort elements
	//test all are same
	public static void main(String args[]) throws Exception
	{
		int testRuns = 1000000;
		
		Random rng = new Random(7);
		
		for(int i=0; i<testRuns; i++)
		{
			int listSize = rng.nextInt(2040) + 10;
			int blockSize = rng.nextInt(2950) + 50;
			int numberOfBlocks = rng.nextInt(98) + 2;
			
			ArrayList<Integer> al = new ArrayList<Integer>();
			BigArrayList<Integer> bal = new BigArrayList<Integer>(blockSize, numberOfBlocks, BigArrayList.IOTypes.FST_OBJECT);

			for(int j=0; j<listSize; j++)
			{
				int nextInt = rng.nextInt();
				
				al.add(nextInt);
				bal.add(nextInt);
			}
			
			Collections.sort(al);
			BigArrayList<Integer> sortedList = BigArrayList.sort(bal);
			
			for(int j=0; j<listSize; j++)
			{
				int num1 = al.get(j);
				int num2 = sortedList.get(j);
				
				if(num1 != num2)
				{
					System.out.println("NOT EQUAL");
					System.out.println(i);
					System.out.println(j);
					System.out.println(num1);
					System.out.println(num2);
					throw new Exception("ERROR");
				}
			}
						
			sortedList.clearMemory();
			
			if(i % 10000 == 0)
			{
				System.out.println("Run = " + i);
			}
		}
		
		System.out.println("Test Successful");
	
	}
}
