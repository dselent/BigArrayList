import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BigArrayListTest
{
	private static final int NUMBER_OF_OPERATIONS = 4;
	
	private static int testRuns;
	private static int minBlockSize;
	private static int maxBlockSize;
	private static int minCacheBlocks;
	private static int maxCacheBlocks;
	private static int minActions;
	private static int maxActions;
	
	private static Random random;
	private BigArrayList<Integer> bigArrayList;

	@BeforeAll
	static void setUp() throws Exception
	{
		//modify number of test runs as desired
		testRuns = 10;
		minBlockSize = 5;
		maxBlockSize = 1000;
		minCacheBlocks = 2;
		maxCacheBlocks = 20;
		minActions = 2;
		maxActions = 100000;
		
		random = new Random(0);
	}

	@AfterEach
	protected void tearDown() throws Exception
	{
		if(bigArrayList != null)
		{
			bigArrayList.clearMemory();
		}
	}
	
	/**
	 * Monte-carlo test case.  Tests random operations on BigArrayLists with parameters randomized within ranges.
	 */
	@Test
	public void testBigArrayList()
	{
		for(int i=0; i<testRuns; i++)
		{
			System.out.println("Iteration " + i);
			
			int blockSize = random.nextInt(maxBlockSize-minBlockSize) + minBlockSize;
			int cacheBlocks = random.nextInt(maxCacheBlocks- minCacheBlocks) + minCacheBlocks;
			int actions = random.nextInt(maxActions-minActions) + minActions;

			bigArrayList = new BigArrayList<Integer>(blockSize, cacheBlocks);
			List<Integer> arrayList = new ArrayList<>();

			for(int j=0; j<actions; j++)
			{
				int action = random.nextInt(NUMBER_OF_OPERATIONS);
							
				if(action == 0 || arrayList.size() == 0)
				{
					//add elements to end
					
					int num1 = random.nextInt();
					arrayList.add(num1);
					bigArrayList.add(num1);
					
					//add another if it is early
					if(j < actions/2)
					{
						int num2 = random.nextInt();
						arrayList.add(num2);
						bigArrayList.add(num2);
					}
					
					String errorMessage = "(ADD) Sizes not equal: test run iteration = " + i + ", action number = " + j;
					assertEquals((long)arrayList.size(), bigArrayList.size(), errorMessage);
				}
				else if(action == 1)
				{
					//get an element

					int listSize = arrayList.size();

					int getIndex = random.nextInt(listSize);

					long number1 = arrayList.get(getIndex);
					long number2 = bigArrayList.get(getIndex);
					
					String errorMessage = "(GET) Elements not equal: test run iteration = " + i + ", action number = " + j +
							", ArrayList element = " + number1 + ", BigArrayList element = " + number2 + ", index = " + getIndex;
					assertEquals(number1, number2, errorMessage);
					
					String errorMessage2 = "(GET) Sizes not equal: test run iteration = " + i + ", action number = " + j;
					assertEquals(arrayList.size(), bigArrayList.size(), errorMessage2);
				}
				else if(action == 2)
				{
					//set an element

					int listSize = arrayList.size();

					int setIndex = random.nextInt(listSize);
					int randomNumber = random.nextInt();

					arrayList.set(setIndex, randomNumber);
					bigArrayList.set(setIndex, randomNumber);
					
					
					String errorMessage2 = "(SET) Sizes not equal: test run iteration = " + i + ", action number = " + j + ", index = " + setIndex;
					assertEquals(arrayList.size(), bigArrayList.size(), errorMessage2);
				}
				else if(action == 3)
				{
					//remove an element

					int listSize = arrayList.size();

					int removeIndex = random.nextInt(listSize);

					int number1 = arrayList.remove(removeIndex);
					int number2 = bigArrayList.remove(removeIndex);

					String errorMessage = "(REMOVE) Elements not equal: test run iteration = " + i + ", action number = " + j +
							", ArrayList element = " + number1 + ", BigArrayList element = " + number2 + ", index = " + removeIndex;
					assertEquals(number1, number2, errorMessage);
					
					String errorMessage2 = "(REMOVE) Sizes not equal: test run iteration = " + i + ", action number = " + j;
					assertEquals(arrayList.size(), bigArrayList.size(), errorMessage2);
					
					if(j > actions/2 && arrayList.size() > 0)
					{
						int listSize2 = arrayList.size();
						int removeIndex2 = random.nextInt(listSize2);
						
						int number1_2 = arrayList.remove(removeIndex2);
						int number2_2 = bigArrayList.remove(removeIndex2);

						String errorMessage3 = "(REMOVE) Elements not equal: test run iteration = " + i + ", action number = " + j +
								", ArrayList element = " + number1_2 + ", BigArrayList element = " + number2_2 + ", index = " + removeIndex;
						assertEquals(number1, number2, errorMessage3);
						
						String errorMessage4 = "(REMOVE) Sizes not equal: test run iteration = " + i + ", action number = " + j;
						assertEquals(arrayList.size(), bigArrayList.size(), errorMessage4);
					}
				}
					
				//remove for testing performance
				/*
				for(int k=0; k<arrayList.size(); k++)
				{
					int number1 = arrayList.get(k);
					int number2 = bigArrayList.get(k);

					String errorMessage = "(Elements not equal: test run iteration = " + i +
							", ArrayList element = " + number1 + ", BigArrayList element = " + number2 + ", index = " + k;
					assertEquals(errorMessage, number1, number2);
				}*/
				
				
			}

			for(int j=0; j<arrayList.size(); j++)
			{
				int number1 = arrayList.get(j);
				int number2 = bigArrayList.get(j);

				String errorMessage = "(Elements not equal: test run iteration = " + i +
						", ArrayList element = " + number1 + ", BigArrayList element = " + number2 + ", index = " + j;
				assertEquals(number1, number2, errorMessage);
			}
			
			
			Collections.sort(arrayList);
			
			try
			{
				bigArrayList = BigArrayList.sort(bigArrayList);
			}
			catch (IOException e)
			{
				fail("Iteration " + i + ": " + e.getCause().toString());
			}
			
			
			try
			{
				bigArrayList.clearMemory();
			}
			catch(IOException e)
			{
				fail("Iteration " + i + ": " + e.getCause().toString());
			}
			
			for(int j=0; j<arrayList.size(); j++)
			{
				int number1 = arrayList.get(j);
				int number2 = bigArrayList.get(j);

				String errorMessage = "(Elements not equal after sorting: test run iteration = " + i +
						", ArrayList element = " + number1 + ", BigArrayList element = " + number2 + ", index = " + j;
				assertEquals(number1, number2, errorMessage);
			}
		}
	}
	
}