import java.io.*;

public class CreateSetTestScripts
{

	public CreateSetTestScripts()
	{
		//create arrays of valid parameter values
		//loop through arrays
		
		long[] numberOfElements = new long[5];
		numberOfElements[0] = 10000l;
		numberOfElements[1] = 1000000l;
		numberOfElements[2] = 100000000l;
		numberOfElements[3] = 10000000000l;
		numberOfElements[4] = 1000000000000l;

		long[] tableSizes = new long[6];
		tableSizes[0] = 10000;
		tableSizes[1] = 100000;
		tableSizes[2] = 1000000;
		tableSizes[3] = 10000000;
		tableSizes[4] = 100000000;
		tableSizes[5] = 1000000000;

		long[] cacheBlocks = new long[6];
		cacheBlocks[0] = 2;
		cacheBlocks[1] = 4;
		cacheBlocks[2] = 8;
		cacheBlocks[3] = 64;
		cacheBlocks[4] = 512;
		cacheBlocks[5] = 4096;

		int trials = 5;

		for(int i=0; i<numberOfElements.length; i++)
		{
			
			for(int l=0; l<trials && numberOfElements[i] < 10000000000l; l++)
			{
				String line = "java -Xmx10000M SetTestOrderedArrayList " + numberOfElements[i] + " " + (l+1) + " >> SetTestOrderedOutput.txt";
				line = line.concat("\n");
				line = line.concat("timeout /t 2");

				try
				{
					FileWriter file = new FileWriter("SetTests.bat", true);
					file.write(line);
					file.write(System.getProperty("line.separator"));
					file.flush();
					file.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.exit(0);
				}
			}
			
		}


		for(int i=0; i<numberOfElements.length; i++)
		{
			
			for(int l=0; l<trials && numberOfElements[i] < 10000000000l; l++)
			{
				String line = "java -Xmx10000M SetTestRandomArrayList " + numberOfElements[i] + " " + (l+1) + " >> SetTestRandomOutput.txt";
				line = line.concat("\n");
				line = line.concat("timeout /t 2");

				try
				{
					FileWriter file = new FileWriter("SetTests.bat", true);
					file.write(line);
					file.write(System.getProperty("line.separator"));
					file.flush();
					file.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.exit(0);
				}
			}
			
		}


		for(int i=0; i<numberOfElements.length; i++)
		{
			for(int j=0; j<tableSizes.length; j++)
			{
				for(int k=0; k<cacheBlocks.length; k++)
				{
					for(int l=0; l<trials; l++)
					{
						if(numberOfElements[i] / tableSizes[j] <= 10000 && tableSizes[j] * cacheBlocks[k] <= 1000000000)
						{
							String line = "java -Xmx10000M SetTestOrderedBigArrayList " + numberOfElements[i] + " " + tableSizes[j] + " " + cacheBlocks[k] + " " + (l+1) + " >> SetTestOrderedBigOutput.txt";
							line = line.concat("\n");
							line = line.concat("timeout /t 2");

							try
							{
								FileWriter file = new FileWriter("SetTests.bat", true);
								file.write(line);
								file.write(System.getProperty("line.separator"));
								file.flush();
								file.close();
							}
							catch(Exception e)
							{
								e.printStackTrace();
								System.exit(0);
							}
						}
					}
				}
			}
		}


		for(int i=0; i<numberOfElements.length; i++)
		{
			for(int j=0; j<tableSizes.length; j++)
			{
				for(int k=0; k<cacheBlocks.length; k++)
				{
					for(int l=0; l<trials; l++)
					{
						if(numberOfElements[i] / tableSizes[j] <= 10000  && tableSizes[j] * cacheBlocks[k] <= 1000000000)
						{
							String line = "java -Xmx10000M SetTestRandomBigArrayList " + numberOfElements[i] + " " + tableSizes[j] + " " + cacheBlocks[k] + " " + (l+1) + " >> SetTestRandomBigOutput.txt";
							line = line.concat("\n");
							line = line.concat("timeout /t 2");

							try
							{
								FileWriter file = new FileWriter("SetTests.bat", true);
								file.write(line);
								file.write(System.getProperty("line.separator"));
								file.flush();
								file.close();
							}
							catch(Exception e)
							{
								e.printStackTrace();
								System.exit(0);
							}
						}
					}
				}
			}
		}

	}

	public static void main(String args[])
	{
		
		new CreateSetTestScripts();
	}
}