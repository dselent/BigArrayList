
/*
 * BigArrayList
 * Copyright (C) 2015  Douglas Selent
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.dselent.bigarraylist;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;


//FST
//https://github.com/RuedigerMoeller/fast-serialization/wiki/Serialization

/**
 * Class that reads and writes the contents of the BigArrayList to/from disk.
 * Each BigArrayList is given a unique identifier so multiple BigArrayList objects can be used at once.
 * <p>
 * The files are names with the following convention
 * <p>
 * (filePath)(memory instance)"_memory_"(file/block number)(file extension)
 * <p>
 * Examples:<br>
 * "memory\0_memory_0.jobj<br>
 * "memory\0_memory_1.jobj<br>
 * 
 * @author Douglas Selent
 *
 * @param <E> Generic type
 */
class FileAccessor<E>
{
	/**
	 * Unique identifier for the files associated with a specific instance of BigArrayList
	 */
	private static int nextMemoryInstance = 0;
	
	/**
	 * Default folder path string = "memory"
	 */
	private final String DEFAULT_MEMORY_FILE_PATH = "memory";
	
	/**
	 * Default file extension = ".jobj"
	 */
	private final String DEFAULT_MEMORY_FILE_EXTENSION = ".jobj";
	
	/**
	 * Default buffer size for file I/O = 262,144 bytes
	 */
	private final int BUFFER_SIZE = 262144;

	//1024 = 141465
	//4096 = 135921
	//16384 = 118468
	//65536 = 114287
	//262144 = 112789
	//1048576 = 131448
	//16777216 = 124169

	/**
	 * The File object holding the folder path for the memory contents
	 */
	private final File memoryFolder;
	
	/**
	 * A string for the file path
	 */
	private String memoryPath;
	
	/**
	 * File extension
	 */
	private String memoryExtension;
	
	/**
	 * Memory instance for this object
	 */
	private int memoryInstance;
	
	/**
	 * FST configuration for using FST object serialization
	 */
	private static FSTConfiguration fstConfiguration;
	
	/**
	 * Constructs a FileAccessor object with the default file path to store contents on disk
	 */
	public FileAccessor()
	{
		memoryPath = DEFAULT_MEMORY_FILE_PATH;
		memoryExtension = DEFAULT_MEMORY_FILE_EXTENSION;
		memoryFolder = new File(memoryPath);

		try
		{
			if(!memoryFolder.exists())
			{
				memoryFolder.mkdir();
			}

			memoryInstance = findMemoryInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		fstConfiguration = FSTConfiguration.createDefaultConfiguration();
	}

	/**
	 * Constructs a FileAccessor object with the specified file path to store contents on disk
	 * 
	 * @param memoryPath The folder path to read and write to
	 */
	public FileAccessor(String memoryPath)
	{
		this.memoryPath = memoryPath;
		memoryExtension = DEFAULT_MEMORY_FILE_EXTENSION;
		memoryFolder = new File(memoryPath);

		try
		{
			if(!memoryFolder.exists())
			{
				memoryFolder.mkdir();
			}

			memoryInstance = findMemoryInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		fstConfiguration = FSTConfiguration.createDefaultConfiguration();
	}

	/**
	 * Returns if the file exists or not
	 * 
	 * @param fileNumber The file number
	 * @return Returns true if the file with the given file number exists false otherwise
	 */
	protected boolean doesFileExist(int fileNumber)
	{
		boolean exists = false;

		File file = new File(memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension);

		if(file.exists())
		{
			exists = true;
		}

		return exists;
	}

	/**
	 * Finds a unique instance number for this FileAccessor object
	 * 
	 * Searches numbers in ascending order to find the corresponding named file that does not exist.
	 * @return Returns the unique memory instance that this FileAccessor object can use
	 */
	private int findMemoryInstance()
	{
		int memoryInstanceNumber = nextMemoryInstance;

		File memoryFile = new File(memoryPath + File.separator + memoryInstanceNumber + "_memory_" + "0" + memoryExtension);

		while(memoryFile.exists())
		{
			memoryInstanceNumber++;
			memoryFile = new File(memoryPath + File.separator + memoryInstanceNumber + "_memory_" + "0" + memoryExtension);
		}

		nextMemoryInstance = memoryInstanceNumber+1;
		return memoryInstanceNumber;
	}

	/**
	 * Creates a file with the given file number
	 * 
	 * @param fileNumber The file number
	 */
	protected void createFile(int fileNumber)
	{
		int memoryInstance = findMemoryInstance();
		File file = new File(memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension);

		try
		{
			if(!file.exists())
			{
				file.createNewFile();
			}
			else
			{
				throw new Exception("File already exists " + file.toString());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Deletes the file with the given path
	 * 
	 * @param filePath The file path
	 */
	private void deleteFile(String filePath)
	{
		File file = new File(filePath);

		if(file.exists())
		{
			file.delete();
		}
	}

	/**
	 * Deletes the file with the given number
	 * 
	 * @param fileNumber The file number
	 */
	protected void deleteFile(int fileNumber)
	{
		String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;
		deleteFile(filePath);
	}

	/**
	 * Returns the file path where the BigArrayList is writing to disk
	 * 
	 * @return Returns the file path where the BigArrayList is writing to disk
	 */
	protected String getMemoryFilePath()
	{
		return memoryPath;
	}


	/**
	 * Reads the contents of a cache block into memory using buffered I/O with standard object streams
	 * Reads in the size (number of elements) as the first object
	 * 
	 * @param fileNumber The file to read from
	 * @param cacheSpot The location in cache to read into (the ArrayList index)
	 * @param arrayList The BigArrayList object
	 * @throws IOException For I/O errors
	 * @throws ClassNotFoundException If no such class exists
	 */
	@SuppressWarnings("unchecked")
	//must use unchecked warning because ObjectInputStream doesn't use generic typing
	protected void readFromFileObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException, ClassNotFoundException
	{
		String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;
		File file = new File(filePath);
		
		if(file.exists())
		{
			FileInputStream fileInputStream = new FileInputStream(filePath);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER_SIZE);
			ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
			
			try
			{
				arrayList.setArrayList(cacheSpot, (ArrayList<E>)objectInputStream.readObject());
			}
			catch(IOException ioe)
			{
				throw ioe;
			}
			catch (ClassNotFoundException ce)
			{
				throw ce;
			}
			finally
			{
				//explicitly closing all streams to be safe
				fileInputStream.close();
				bufferedInputStream.close();
				objectInputStream.close();
			}
		}
		else
		{
			arrayList.setArrayList(cacheSpot, new ArrayList<E>());
		}
	}
	
	/**
	 * Reads the contents of a cache block into memory using memory mapped files with object streams
	 * Reads in the size (number of elements) as the first object
	 * @param fileNumber The file to read from
	 * @param cacheSpot The location in cache to read into (the ArrayList index)
	 * @param arrayList The BigArrayList object
	 * @throws IOException For I/O errors
	 * @throws ClassNotFoundException If no such class exists
	 */
	//must clean up byte buffer
	@SuppressWarnings("unchecked")
	protected void readFromFileMMapObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException, ClassNotFoundException
	{
			String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;
			File file = new File(filePath);
			
			if(file.exists())
			{
				RandomAccessFile tempFile = new RandomAccessFile(filePath, "rw");
				
				long tempFileLength = tempFile.length();
				int fileLength = -1;
				
				if(tempFile.length() > Integer.MAX_VALUE)
				{
					tempFile.close();
					throw new IllegalArgumentException(tempFileLength + " cannot be cast to an int");
				}
				else
				{
					fileLength = (int) tempFileLength;
					
					MappedByteBuffer tempByteBuffer = tempFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, tempFile.length());
					tempByteBuffer.load();
				
					byte[] byteArray = new byte[fileLength];
					tempByteBuffer.get(byteArray);
				
					ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
					ObjectInputStream objectInputStream = new ObjectInputStream(bais);
				
					try
					{
						arrayList.setArrayList(cacheSpot, (ArrayList<E>)objectInputStream.readObject());
					}
					catch(IOException ioe)
					{
						throw ioe;
					}
					finally
					{
					
						bais.close();
						objectInputStream.close();
				
						tempByteBuffer.clear();
						tempByteBuffer = null;
						tempFile.close();
						System.gc();
					}
				}
			}
			else
			{
				arrayList.setArrayList(cacheSpot, new ArrayList<E>());
			}

	}
	
	/**
	 * Reads a cache block from disk using FST object streams
	 * 
	 * @param fileNumber The file number to read from
	 * @param cacheSpot The block to write to disk
	 * @param arrayList The BigArrayList
	 * @throws IOException For I/O errors
	 * @throws ClassNotFoundException If no such class exists
	 */
	@SuppressWarnings("unchecked")
	protected void readFromFileFSTObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException, ClassNotFoundException
	{
		String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;
		File file = new File(filePath);
		
		if(file.exists())
		{
			FileInputStream fileInputStream = new FileInputStream(filePath);
			FSTObjectInput fstObjectInputStream = new FSTObjectInput(fileInputStream);
			
			try
			{
				arrayList.setArrayList(cacheSpot, (ArrayList<E>)fstObjectInputStream.readObject());
			}
			catch(IOException ioe)
			{
				throw ioe;
			}
			catch (ClassNotFoundException ce)
			{
				throw ce;
			}
			finally
			{
				//need to close both streams, I think this is a java bug
				//expected behavior = closing the fstObjectInputStream would also close the fileInputStream, but it does not
				//Consequence = unable to delete files
				fileInputStream.close();
				fstObjectInputStream.close();
			}
		}
		else
		{
			arrayList.setArrayList(cacheSpot, new ArrayList<E>());
		}

	}
	
	/**
	 * Reads the contents of a cache block into memory using memory mapped files with FST serialization
	 * Reads in the size (number of elements) as the first object
	 * 
	 * @param fileNumber The file to read from
	 * @param cacheSpot The location in cache to read into (the ArrayList index)
	 * @param arrayList The BigArrayList object
	 * @throws IOException For I/O errors
	 * @throws ClassNotFoundException If no such class exists
	 */
	//must clean up byte buffer
	@SuppressWarnings("unchecked")
	protected void readFromFileMMapFSTObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException, ClassNotFoundException
	{
			String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;
			File file = new File(filePath);
			
			if(file.exists())
			{
				RandomAccessFile tempFile = new RandomAccessFile(filePath, "rw");
				
				long tempFileLength = tempFile.length();
				int fileLength = -1;
				
				if(tempFile.length() > Integer.MAX_VALUE)
				{
					tempFile.close();
					throw new IllegalArgumentException(tempFileLength + " cannot be cast to an int");
				}
				else
				{
					fileLength = (int) tempFileLength;
					
					MappedByteBuffer tempByteBuffer;
					
					try
					{
						tempByteBuffer = tempFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, tempFile.length());
						tempByteBuffer.load();
					
						byte[] byteArray = new byte[fileLength];
						tempByteBuffer.get(byteArray);
						arrayList.setArrayList(cacheSpot, (ArrayList<E>)fstConfiguration.asObject(byteArray));
						
						tempByteBuffer.clear();
					}
					catch(IOException ioe)
					{
						throw ioe;
					}
					finally
					{			
						
						tempByteBuffer = null;
						tempFile.close();
						System.gc();
					}
				}
			}
			else
			{
				arrayList.setArrayList(cacheSpot, new ArrayList<E>());
			}
	}

	/**
	 * Writes a cache block to disk using buffered I/O with standard object streams
	 * 
	 * @param fileNumber The file number to write to
	 * @param cacheSpot The block to write to disk
	 * @param arrayList The BigArrayList
	 * @throws IOException For I/O errors
	 * @throws ClassNotFoundException If no such class exists
	 */
	protected void writeToFileObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException
	{
		String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;

		File tempFile = new File(filePath);
		
		if(!arrayList.getArrayList(cacheSpot).isEmpty())
		{
			tempFile.deleteOnExit();
			
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
			
			try
			{
				objectOutputStream.writeObject(arrayList.getArrayList(cacheSpot));
				objectOutputStream.flush();			
			}
			catch(IOException ioe)
			{
				throw ioe;
			}
			finally
			{
				//explicitly closing all streams to be safe
				fileOutputStream.close();
				bufferedOutputStream.close();
				objectOutputStream.close();
			}
		}
		else
		{
			tempFile.delete();
		}
	}	
	
	/**
	 * Writes a cache block to disk using memory mapped files with object streams
	 * <p>
	 * Will not automatically delete files when the program ends.  Programmer must call {@link com.dselent.bigarraylist.BigArrayList.clearMemory}
	 * 
	 * @param fileNumber The file number to write to
	 * @param cacheSpot The block to write to disk
	 * @param arrayList The BigArrayList
	 * @throws IOException For I/O errors
	 * @throws ClassNotFoundException If no such class exists
	 */
	//must clean up byte buffer and suggest to garbage collect it or else the files cannot be deleted
	protected void writeToFileMMapObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException
	{
		String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;

		if(!arrayList.getArrayList(cacheSpot).isEmpty())
		{
			RandomAccessFile tempFile = new RandomAccessFile(filePath, "rw");	
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
			MappedByteBuffer tempByteBuffer;
			
			try
			{
				objectOutputStream.writeObject(arrayList.getArrayList(cacheSpot));
				objectOutputStream.flush();
				
				byte[] byteArray = baos.toByteArray();
				tempByteBuffer = tempFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, byteArray.length);
				tempByteBuffer.put(byteArray);
				tempByteBuffer.force();
				tempByteBuffer.clear();
			}
			catch(IOException ioe)
			{
				throw ioe;
			}
			finally
			{
				objectOutputStream.close();
	
				tempByteBuffer = null;
				tempFile.close();
				System.gc();
			}
		}
		else
		{
			File tempFile = new File(filePath);
			tempFile.delete();
		}

	}
	
	/**
	 * Writes a cache block to disk using FST object output streams
	 * 
	 * @param fileNumber The file number to write to
	 * @param cacheSpot The block to write to disk
	 * @param arrayList The BigArrayList
	 * @throws IOException For I/O errors
	 * @throws ClassNotFoundException If no such class exists
	 */
	protected void writeToFileFSTObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException
	{
		String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;

		File tempFile = new File(filePath);
		
		if(!arrayList.getArrayList(cacheSpot).isEmpty())
		{
			tempFile.deleteOnExit();
		
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			FSTObjectOutput fstObjectOutputStream = new FSTObjectOutput(fileOutputStream);

			try
			{
				byte byteArray[] = fstConfiguration.asByteArray(arrayList.getArrayList(cacheSpot));
				fstObjectOutputStream.write(byteArray);
				fstObjectOutputStream.flush();
			}
			catch(IOException ioe)
			{
				throw ioe;
			}
			finally
			{
				fileOutputStream.close();
				fstObjectOutputStream.close();
			}
		}
		else
		{
			tempFile.delete();
		}
	}
	

	/**
	 * Writes a cache block to disk using memory mapped files with FST object output streams
	 * <p>
	 * Will not automatically delete files when the program ends.  Programmer must call {@link com.dselent.bigarraylist.BigArrayList.clearMemory}
	 * 
	 * @param fileNumber The file number to write to
	 * @param cacheSpot The block to write to disk
	 * @param arrayList The BigArrayList
	 * @throws IOException For I/O errors
	 */
	protected void writeToFileMMapFSTObject(int fileNumber, int cacheSpot, BigArrayList<E> arrayList) throws IOException
	{
		String filePath = memoryPath + File.separator + memoryInstance + "_memory_" + fileNumber + memoryExtension;

		if(!arrayList.getArrayList(cacheSpot).isEmpty())
		{
			RandomAccessFile tempFile = new RandomAccessFile(filePath, "rw");
			MappedByteBuffer tempByteBuffer;
			
			try
			{
				byte byteArray[] = fstConfiguration.asByteArray(arrayList.getArrayList(cacheSpot));
				tempByteBuffer = tempFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, byteArray.length);
				tempByteBuffer.put(byteArray);
				tempByteBuffer.force();
				tempByteBuffer.clear();
			}
			catch(IOException ioe)
			{
				throw ioe;
			}
			finally
			{
				tempByteBuffer = null;
				tempFile.close();
				System.gc();
			}
		}
		else
		{
			File tempFile = new File(filePath);
			tempFile.delete();
		}
	}
	
	/**
	 * Deletes all files associated with the current BigArrayList object
	 * @throws IOException When the file cannot be deleted
	 */
	protected void clearMemory() throws IOException
	{		
		//get all files associated with this memory instance and delete them

		File[] fileList = memoryFolder.listFiles();

		for(int i=0; i<fileList.length; i++)
		{
			String path = fileList[i].getAbsolutePath();

			if(path.startsWith(memoryFolder.getAbsolutePath() + File.separator + memoryInstance))
			{
				boolean deleted = fileList[i].delete();

				if(!deleted)
				{
					throw new IOException("Unable to delete file: " + path);
				}
			}
		}

		//don't delete the folder, other things may be using it

	}

}