import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dselent.bigarraylist.BigArrayList;


public class MCTest 
{
	public static void main(String args[]) throws Exception
	{
		/*
		int testSize = 10;
		
		int blockSize = 5;
		int cachBlocks = 2;
		//int bufferSize = 10;
		BigArrayList.IOTypes ioType = BigArrayList.IOTypes.OBJECT;
		
		BigArrayList<Long> bal = new BigArrayList<Long>(blockSize, cachBlocks, ioType);
		
		for(long i=0; i<testSize; i++)
		{
			bal.add(i);
		}
		
		bal.remove(1);
		bal.remove(4);
		
		for(long i=0; i<bal.size(); i++)
		{
			System.out.println(bal.get(i));
		}
		
		bal.clearMemory();
		*/
		
		//run 1 million random tests with parameters in valid ranges

		
		int testRuns = 1000000;
		int minBlockSize = 5;
		int maxBlockSize = 1000;
		int minCachBlocks = 2;
		int maxCacheBlocks = 20;
		int minActions = 2;
		int maxActions = 100000;

		Random random = new Random(0);

		for(int i=0; i<testRuns; i++)
		{
			int blockSize = random.nextInt(maxBlockSize-minBlockSize) + minBlockSize;
			int cacheBlocks = random.nextInt(maxCacheBlocks-minCachBlocks) + minCachBlocks;
			int actions = random.nextInt(maxActions-minActions) + minActions;

			BigArrayList<Integer> bigArrayList = new BigArrayList<Integer>(blockSize, cacheBlocks);
			List<Integer> arrayList = new ArrayList<Integer>();

			for(int j=0; j<actions; j++)
			{
				//System.out.println(i + " " + j);
				
				if(j % 10000 == 0)
				{
					System.out.println(i + " " + j);
				}
				
				
				int action = random.nextInt(4);
							
				if(action == 0 || arrayList.size() == 0)
				{
					//add elements to end
					
					int num1 = random.nextInt();
					arrayList.add(num1);
					bigArrayList.add(num1);
					
					if(j < actions/2)
					{
						int num2 = random.nextInt();
						arrayList.add(num2);
						bigArrayList.add(num2);
					}
					
					if(arrayList.size() != bigArrayList.size())
					{
						System.out.println("NOT EQUAL SIZES");
						System.out.println(i);
						System.out.println(j);
						throw new Exception("ERROR");
					}
					
				}
				else if(action == 1)
				{
					//get an element

					int listSize = arrayList.size();

					int getIndex = random.nextInt(listSize);

					long number1 = arrayList.get(getIndex);
					long number2 = bigArrayList.get(getIndex);
					
					if(number1 != number2)
					{
						System.out.println("NOT EQUAL");
						System.out.println(i);
						System.out.println(j);
						System.out.println(number1);
						System.out.println(number2);
						System.out.println(getIndex);
						throw new Exception("ERROR");
					}
					
					if(arrayList.size() != bigArrayList.size())
					{
						System.out.println("NOT EQUAL SIZES");
						System.out.println(i);
						System.out.println(j);
						throw new Exception("ERROR");
					}
				}
				else if(action == 2)
				{
					//set an element

					int listSize = arrayList.size();

					int setIndex = random.nextInt(listSize);
					int randomNumber = random.nextInt();

					arrayList.set(setIndex, randomNumber);
					bigArrayList.set(setIndex, randomNumber);
					
					if(arrayList.size() != bigArrayList.size())
					{
						System.out.println("NOT EQUAL SIZES");
						System.out.println(i);
						System.out.println(j);
						throw new Exception("ERROR");
					}
				}
				else if(action == 3)
				{
					//remove an element

					int listSize = arrayList.size();

					int removeIndex = random.nextInt(listSize);

					int number1 = arrayList.remove(removeIndex);
					int number2 = bigArrayList.remove(removeIndex);

					if(number1 != number2)
					{
						System.out.println("NOT EQUAL");
						System.out.println(i);
						System.out.println(j);
						System.out.println(number1);
						System.out.println(number2);
						System.out.println(removeIndex);
						throw new Exception("ERROR");
					}
					
					if(j > actions/2 && arrayList.size() > 0)
					{
						int listSize2 = arrayList.size();
						int removeIndex2 = random.nextInt(listSize2);
						
						int number1_2 = arrayList.remove(removeIndex2);
						int number2_2 = bigArrayList.remove(removeIndex2);

						if(number1_2 != number2_2)
						{
							System.out.println("NOT EQUAL");
							System.out.println(i);
							System.out.println(j);
							System.out.println(number1_2);
							System.out.println(number2_2);
							throw new Exception("ERROR");
						}
					}
					
					if(arrayList.size() != bigArrayList.size())
					{
						System.out.println("NOT EQUAL SIZES");
						System.out.println(i);
						System.out.println(j);
						throw new Exception("ERROR");
					}
					
				}
					
				
				//don't remove this check, not in same loop as the following one
				//remove for testing only or performance
				/*
				for(int k=0; k<arrayList.size(); k++)
				{
					int number1 = arrayList.get(k);
					int number2 = bigArrayList.get(k);

					if(number1 != number2)
					{
						System.out.println("NOT EQUAL");
						System.out.println(i);
						System.out.println(k);
						System.out.println(number1);
						System.out.println(number2);
						throw new Exception("ERROR");
					}
				}*/
				
				
			}

			for(int j=0; j<arrayList.size(); j++)
			{
				int number1 = arrayList.get(j);
				int number2 = bigArrayList.get(j);

				if(number1 != number2)
				{
					System.out.println("NOT EQUAL");
					System.out.println(i);
					System.out.println(j);
					System.out.println(number1);
					System.out.println(number2);
					throw new Exception("ERROR");
				}
			}
			
			bigArrayList.clearMemory();
		}
	}
}
