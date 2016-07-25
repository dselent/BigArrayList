import java.io.*;

public class QuickReplace
{

	public QuickReplace()
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("TestNormalOutput.txt"));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter("zAddOutput.txt"));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter("zGetOrderedOutput.txt"));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter("zGetRandomOutput.txt"));
			BufferedWriter bw4 = new BufferedWriter(new FileWriter("zSetOrderedOutput.txt"));
			BufferedWriter bw5 = new BufferedWriter(new FileWriter("zSetRandomOutput.txt"));
			BufferedWriter bw6 = new BufferedWriter(new FileWriter("zMemoryOutput.txt"));
			BufferedWriter bw7 = new BufferedWriter(new FileWriter("zDiskSpaceOutput.txt"));

			String line = "";
			String line2 = "";
			int count = 0;

			while((line = br.readLine()) != null)
			{
				if(line.startsWith("Total ADD time"))
				{
					line2 = line.substring(line.indexOf("=")+2);

					bw1.write(line2);
					bw1.write(System.getProperty("line.separator"));
				}
				else if(line.startsWith("Total GET ORDERED time"))
				{
					line2 = line.substring(line.indexOf("=")+2);

					bw2.write(line2);
					bw2.write(System.getProperty("line.separator"));
				}
				else if(line.startsWith("Total GET RANDOM time"))
				{
					line2 = line.substring(line.indexOf("=")+2);

					bw3.write(line2);
					bw3.write(System.getProperty("line.separator"));
				}
				else if(line.startsWith("Total SET ORDERED time"))
				{
					line2 = line.substring(line.indexOf("=")+2);

					bw4.write(line2);
					bw4.write(System.getProperty("line.separator"));
				}
				else if(line.startsWith("Total SET RANDOM time"))
				{
					line2 = line.substring(line.indexOf("=")+2);

					bw5.write(line2);
					bw5.write(System.getProperty("line.separator"));
				}
				else if(line.startsWith("Memory used"))
				{
					line2 = line.substring(line.indexOf("=")+2);

					bw6.write(line2);
					bw6.write(System.getProperty("line.separator"));
				}
				else if(line.startsWith("Folder size"))
				{
					line2 = line.substring(line.indexOf("=")+2);

					bw7.write(line2);
					bw7.write(System.getProperty("line.separator"));
				}

				count++;

				if(count%10000 == 0)
				{
					System.out.println(count);
				}
			}

			bw1.flush();
			bw1.close();

			bw2.flush();
			bw2.close();

			bw3.flush();
			bw3.close();

			bw4.flush();
			bw4.close();

			bw5.flush();
			bw5.close();

			bw6.flush();
			bw6.close();

			bw7.flush();
			bw7.close();

			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}

	}

	public static void main(String args[])
	{
		new QuickReplace();
	}
}