
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

import java.io.IOException;


/**
 * Class that manages the mapping from files on disk to elements in memory for the BigArrayList class.
 * Uses an LRU policy at the cache block level to determine which cache block should be swapped out next.
 * 
 * @author Douglas Selent
 *
 * @param <E> Generic type
 */
class CacheMapping<E>
{
	/**
	 * Default size of cache block = 1,000,000
	 */
	private static final int DEFAULT_BLOCK_SIZE = 1000000;
	
	/**
	 * Default number of cache blocks = 2
	 */
	private static final int DEFAULT_CACHE_BLOCKS = 2;

	/**
	 * The minimum size of a cache block = 5 elements
	 */
	private static final int MIN_CACHE_SIZE = 5;
	
	/**
	 * The maximum size of a cache block = the integer limit of 2^31 - 1
	 */
	private static final int MAX_CACHE_SIZE = Integer.MAX_VALUE;

	/**
	 * The minimum number of cache blocks = 2
	 */
	private static final int MIN_CACHE_BLOCKS = 2;
	
	/**
	 * The maximum number of cache blocks = the integer limit of 2^31 - 1
	 */
	private static final int MAX_CACHE_BLOCKS = Integer.MAX_VALUE;

	/**
	 * The size of the cache blocks
	 */
	private int blockSize;
	
	/**
	 * The number of cache blocks
	 */
	private int cacheBlocks;

	/**
	 * Array storing the next spot to add to for each cache
	 */
	private int[] cacheTableSpots;

	/**
	 * The file number each ArrayList/Cache block is currently storing
	 * Index is the same as the ArrayList index
	 */
	private int[] cacheTableFiles;

	/**
	 * Array of when each block was last used
	 * Stores a list of block numbers sorted from least recently used to most recently used
	 * most recent = end of array
	 */
	private int[] mostRecentlyUsedList;

	/**
	 * Array for each cache block for whether or not it's data has changed
	 * If clean, then it does not need to be written to disk when swapped out
	 * If dirty, then it does need to be written to disk when swapped out
	 */
	private boolean[] dirtyBits;

	/**
	 * Reference to the associated BigArrayList object
	 */
	private BigArrayList<E> bigArrayList;
	
	/**
	 * Reference to the associated FileAccessor object
	 */
	private FileAccessor<E> fileAccessor;

	/**
	 * Constructs a CacheMapping object for the BigArrayList
	 * 
	 * @param theList Associated BigArrayList
	 */
	protected CacheMapping(BigArrayList<E> theList)
	{
		blockSize = DEFAULT_BLOCK_SIZE;
		cacheBlocks = DEFAULT_CACHE_BLOCKS;

		cacheTableSpots = new int[cacheBlocks];
		cacheTableFiles = new int[cacheBlocks];
		mostRecentlyUsedList = new int[cacheBlocks];
		dirtyBits = new boolean[cacheBlocks];

		for(int i=0; i<cacheBlocks; i++)
		{
			cacheTableSpots[i] = 0;
			cacheTableFiles[i] = -1;
			mostRecentlyUsedList[i] = -1;
			dirtyBits[i] = false;
		}

		bigArrayList = theList;
		fileAccessor = new FileAccessor<E>();
	}

	/**
	 * Constructs a CacheMapping object for the BigArrayList with the following parameters
	 * 
	 * @param blockSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks
	 * @param theList Associated BigArrayList
	 */
	protected CacheMapping(int blockSize, int cacheBlocks, BigArrayList<E> theList)
	{
		if(blockSize < MIN_CACHE_SIZE || blockSize > MAX_CACHE_SIZE)
		{
			throw new IllegalArgumentException("Cache size is " + blockSize + " but must be >= " + MIN_CACHE_SIZE + " and <= " + MAX_CACHE_SIZE);
		}

		if(cacheBlocks < MIN_CACHE_BLOCKS || cacheBlocks > MAX_CACHE_BLOCKS)
		{
			throw new IllegalArgumentException("Number of cache blocks is " + cacheBlocks +  " but must be >= " + MIN_CACHE_BLOCKS + " and <= " + MAX_CACHE_BLOCKS);
		}
	
		this.blockSize = blockSize;
		this.cacheBlocks = cacheBlocks;
		cacheTableSpots = new int[cacheBlocks];
		cacheTableFiles = new int[cacheBlocks];
		mostRecentlyUsedList = new int[cacheBlocks];
		dirtyBits = new boolean[cacheBlocks];

		for(int i=0; i<cacheBlocks; i++)
		{
			cacheTableSpots[i] = 0;
			cacheTableFiles[i] = -1;
			mostRecentlyUsedList[i] = -1;
			dirtyBits[i] = false;
		}
		
		bigArrayList = theList;
		fileAccessor = new FileAccessor<E>();
	}

	/**
	 * Constructs a CacheMapping object for the BigArrayList with the following parameters
	 * 
	 * @param memoryFilePath Folder path to where the data should be written
	 * @param theList Associated BigArrayList
	 */
	protected CacheMapping(String memoryFilePath, BigArrayList<E> theList)
	{
		blockSize = DEFAULT_BLOCK_SIZE;
		cacheBlocks = DEFAULT_CACHE_BLOCKS;

		cacheTableSpots = new int[cacheBlocks];
		cacheTableFiles = new int[cacheBlocks];
		mostRecentlyUsedList = new int[cacheBlocks];
		dirtyBits = new boolean[cacheBlocks];

		for(int i=0; i<cacheBlocks; i++)
		{
			cacheTableSpots[i] = 0;
			cacheTableFiles[i] = -1;
			mostRecentlyUsedList[i] = -1;
			dirtyBits[i] = false;
		}

		bigArrayList = theList;
		fileAccessor = new FileAccessor<E>(memoryFilePath);
	}

	/**
	 * Constructs a CacheMapping object for the BigArrayList with the following parameters
	 * 
	 * @param blockSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks
	 * @param memoryPath Folder path to where the data should be written
	 * @param theList Associated BigArrayList
	 */
	protected CacheMapping(int blockSize, int cacheBlocks, String memoryPath, BigArrayList<E> theList)
	{
		if(blockSize < MIN_CACHE_SIZE || blockSize > MAX_CACHE_SIZE)
		{
			throw new IllegalArgumentException("Cache size is " + blockSize + " but must be >= " + MIN_CACHE_SIZE + " and <= " + MAX_CACHE_SIZE);
		}

		if(cacheBlocks < MIN_CACHE_BLOCKS || cacheBlocks > MAX_CACHE_BLOCKS)
		{
			throw new IllegalArgumentException("Number of cache blocks is " + cacheBlocks +  " but must be >= " + MIN_CACHE_BLOCKS + " and <= " + MAX_CACHE_BLOCKS);
		}
		
		this.blockSize = blockSize;
		this.cacheBlocks = cacheBlocks;
		cacheTableSpots = new int[cacheBlocks];
		cacheTableFiles = new int[cacheBlocks];
		mostRecentlyUsedList = new int[cacheBlocks];
		dirtyBits = new boolean[cacheBlocks];

		for(int i=0; i<cacheBlocks; i++)
		{
			cacheTableSpots[i] = 0;
			cacheTableFiles[i] = -1;
			mostRecentlyUsedList[i] = -1;
			dirtyBits[i] = false;
		}

		bigArrayList = theList;
		fileAccessor = new FileAccessor<E>(memoryPath);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return Returns the fileAccessor object
	 */
	protected FileAccessor<E> getFileAccessor()
	{
		return fileAccessor;
	}
	
	/**
	 * @return Returns the size of each cache block
	 */
	protected int getBlockSize()
	{
		return blockSize;
	}

	/**
	 * @return Returns the number of cache blocks
	 */
	protected int getNumberOfBlocks()
	{
		return cacheBlocks;
	}
	
	/**
	 * @return Returns the number of used cache blocks based on the size of the list
	 */
	protected int getNumberOfUsedBlocks()
	{
		long blockSizeLong = blockSize;
		long usedBlocks = (long) Math.ceil(bigArrayList.size() * 1.0 / blockSizeLong * 1.0);
		
		//safe cast, I really doubt there will ever be over 2^31 - 1 blocks
		return (int)usedBlocks;
	}
	
	/**
	 * Returns the minimum of the number of used cache blocks based on the list size or the parameter size
	 * 
	 * @param index A virtual size index
	 * @return The number of used cache blocks
	 */
	protected int getNumberOfUsedBlocks(long index)
	{
		long blockSizeLong = blockSize;
		long usedVirtualBlocks = (long) Math.ceil(index * 1.0 / blockSizeLong * 1.0);
		long usedRealBlocks = getNumberOfUsedBlocks();
		long usedBlocks = Math.max(usedRealBlocks, usedVirtualBlocks);
		
		//safe cast, I really doubt there will ever be over 2^31 - 1 blocks
		return (int)usedBlocks;
	}

	/**
	 * Sets the index to add the next element to for the given cache block
	 * 
	 * @param cacheBlockIndex The index for the cache block
	 * @param indexToAdd The next index to add to for the specified cache block
	 */
	private void setCacheTableSpots(int cacheBlockIndex, int indexToAdd)
	{
		cacheTableSpots[cacheBlockIndex] = indexToAdd;
	}

	/**
	 * Sets the mapping for cache blocks that are in memory to the file number on disk
	 * 
	 * @param index The index of the cache block
	 * @param fileNumber The file number
	 */
	private void setCacheTableFiles(int index, int fileNumber)
	{
		cacheTableFiles[index] = fileNumber;
	}

	/**
	 * @param block The cache block
	 * @return Returns if the cache block is full or not
	 */
	protected boolean isCacheFull(int block)
	{
		boolean full = false;

		if(cacheTableSpots[block] >= blockSize)
		{
			full = true;
		}

		return full;
	}

	/**
	 * Sets the dirty bit for the given cache block index
	 * 
	 * @param blockIndex Index of the cache block
	 * @param dirty Whether or not the block of cache is dirty
	 */
	protected void setDirtyBit(int blockIndex, boolean dirty)
	{
		dirtyBits[blockIndex] = dirty;
	}

	/**
	 * Called by the add method of BigArrayList
	 * Updates meta data associated with adding an element
	 * 
	 * @param cacheBlockIndex Index of the cache block
	 */
	protected void addEntry(int cacheBlockIndex)
	{
		cacheTableSpots[cacheBlockIndex]++;
		updateUsedList(cacheBlockIndex);
	}
	
	/**
	 * Called by the remove method of BigArrayList
	 * Updates meta data associated with removing an element
	 * 
	 * @param cacheBlockIndex Index of the cache block
	 */
	protected void removeEntry(int cacheBlockIndex)
	{
		cacheTableSpots[cacheBlockIndex]--;
		updateUsedList(cacheBlockIndex);
	}

	/**
	 * 
	 * @param index The index of the element
	 * @return Returns the file number where the element at this index would be
	 */
	protected int getFileNumber(long index)
	{
		long blockSizeLong = blockSize;
		long longFileNumber = index / blockSizeLong;
		
		//safe cast, I really doubt there will ever be over 2^31 - 1 files
		return (int)longFileNumber;
	}
	
	/**
	 * Returns the last element index for the given file number
	 * 
	 * @param fileNumber Number of the file
	 * @return Last element index in the file
	 */
	protected long getLastIndexInFile(int fileNumber)
	{
		long blockSizeLong = blockSize;
		long fileNumberLong = fileNumber;
		long index = (blockSizeLong * fileNumberLong) + blockSizeLong - 1;
		return index;
	}

	/**
	 * Returns the cacheTableFiles spot where the current file/cache block is being held
	 * 
	 * @param fileNumber The file number
	 * @return Returns the cacheTableFiles spot where the current file/cache block is being held
	 */
	protected int getCacheBlockSpot(int fileNumber)
	{
		int blockSpot = -1;

		for(int i=0; i<cacheTableFiles.length && blockSpot == -1; i++)
		{
			if(cacheTableFiles[i] == fileNumber)
			{
				blockSpot = i;
			}
		}

		return blockSpot;
	}

	/**
	 * Returns the spot in cache where this element would be
	 * 
	 * @param index The element index
	 * @return Returns the spot in cache where this element would be
	 */
	protected int getSpotInCache(long index)
	{
		long longTableSize = blockSize;
		long spotInFile = index % longTableSize;
		
		//safe cast, cannot be > 2^31 - 1 elements in an ArrayList
		return (int)spotInFile;
	}
	
	/**
	 * Returns if the file is in cache or not
	 * 
	 * @param fileNumber The file number index
	 * @return Returns true if the contents of the file are in cache and false otherwise
	 */
	protected boolean isFileInCache(int fileNumber)
	{
		boolean inCache = false;

		for(int i=0; i<cacheTableFiles.length && !inCache; i++)
		{
			if(cacheTableFiles[i] == fileNumber)
			{
				inCache = true;
			}
		}

		return inCache;
	}

	/**
	 * Returns the first open location to swap a cache block into or -1 if there are no open spots
	 * 
	 * @return Returns the first open location to swap a cache block into or -1 if there are no open spots
	 */
	protected int getFirstOpenCacheBlock()
	{
		int firstOpen = -1;

		for(int i=0; i<cacheTableFiles.length && firstOpen == -1; i++)
		{
			if(cacheTableFiles[i] == -1)
			{
				firstOpen = i;
			}
		}

		return firstOpen;
	}


	/**
	 * Updates the list of most recently used blocks
	 * 
	 * @param blockNumber Block/File number that was just used
	 */
	protected void updateUsedList(int blockNumber)
	{
		int oldPosition = -1;
		int newPosition = mostRecentlyUsedList.length - 1;
		int shiftPosition = 0;

		//find old position if exists
		for(int i=0; i<mostRecentlyUsedList.length; i++)
		{
			if(mostRecentlyUsedList[i] == blockNumber)
			{
				oldPosition = i;
			}

		}

		//set old spot to -1, clear it out
		if(oldPosition != -1)
		{
			mostRecentlyUsedList[oldPosition] = -1;
		}

		//find spot to shift to
		//if open spaces, find first open one
		for(int i=0; i<mostRecentlyUsedList.length; i++)
		{
			if(mostRecentlyUsedList[i] == -1)
			{
				shiftPosition = i;
			}
		}
		
		//shift down
		for(int i=shiftPosition; i<mostRecentlyUsedList.length-1; i++)
		{
			mostRecentlyUsedList[i] = mostRecentlyUsedList[i+1];
		}

		mostRecentlyUsedList[newPosition] = blockNumber;

	}

	/**
	 * @param blockIndex Index to remove from the list
	 */
	private void removeFromUsedList(int blockIndex)
	{
		for(int i=0; i<mostRecentlyUsedList.length; i++)
		{
			if(mostRecentlyUsedList[i] == blockIndex)
			{
				mostRecentlyUsedList[i] = -1;
			}
		}
	}

	/**
	 * Flushes all data in memory to disk
	 */
	protected void flushCache()
	{
		for(int i=0; i<cacheTableFiles.length; i++)
		{
			flushCacheBlock(i);
		}
	}

	/**
	 * Flushes a single cache block to disk
	 * 
	 * @param blockIndex The index of the cache block
	 */
	private void flushCacheBlock(int blockIndex)
	{
		//write to file
		int fileNumber = cacheTableFiles[blockIndex];

		if(dirtyBits[blockIndex])
		{
			try
			{
				if(bigArrayList.getIOType() == BigArrayList.IOTypes.OBJECT)
				{
					fileAccessor.writeToFileObject(fileNumber, blockIndex, bigArrayList);
				}
				else if(bigArrayList.getIOType() == BigArrayList.IOTypes.MMAP_OBJECT)
				{
					fileAccessor.writeToFileMMapObject(fileNumber, blockIndex, bigArrayList);
				}
				else if(bigArrayList.getIOType() == BigArrayList.IOTypes.FST_OBJECT)
				{
					fileAccessor.writeToFileFSTObject(fileNumber, blockIndex, bigArrayList);
				}
				else if(bigArrayList.getIOType() == BigArrayList.IOTypes.MMAP_FST_OBJECT)
				{
					fileAccessor.writeToFileMMapFSTObject(fileNumber, blockIndex, bigArrayList);
				}
				else
				{
					fileAccessor.writeToFileObject(fileNumber, blockIndex, bigArrayList);
				}
				
				setDirtyBit(blockIndex, false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}

		//clear list

		bigArrayList.clearList(blockIndex);

		//remove block from used list
		//clear cache for this block
		//clear table for this block

		removeFromUsedList(blockIndex);
		clearCacheBlock(blockIndex);
	}

	/**
	 * Clears a cache block from memory
	 * Only a soft clear
	 * @param blockToClear The block index
	 */
	private void clearCacheBlock(int blockToClear)
	{
		cacheTableSpots[blockToClear] = 0;
		cacheTableFiles[blockToClear] = -1;
	}

	/**
	 * Brings the content of the given file number into an available cache block
	 * 
	 * @param fileNumber The file number
	 * @return The index of the spot where the cache block was brought into
	 */
	protected int bringFileIntoCache(int fileNumber)
	{
		//clear a spot if there isn't one

		int openCacheBlock = getFirstOpenCacheBlock();

		if(openCacheBlock == -1)
		{
			int blockToFlush = mostRecentlyUsedList[0];
			flushCacheBlock(blockToFlush);
		}

		//read into array list
		//set cacheTableFiles to fileNumber
		//set cacheTableSpots to number of objects read from file
		//update usedList

		openCacheBlock = getFirstOpenCacheBlock();

		readFromFile(fileNumber, openCacheBlock);

		setCacheTableFiles(openCacheBlock, fileNumber);
		setCacheTableSpots(openCacheBlock, bigArrayList.getArraySize(openCacheBlock));

		updateUsedList(openCacheBlock);

		return openCacheBlock;
	}

	/**
	 * Reads the data from the given file number / cache block into the specified cache index
	 * 
	 * @param fileNumber The file number / cache block to read in
	 * @param cacheIndex The cache index to populate with the data from the file
	 */
	private void readFromFile(int fileNumber, int cacheIndex)
	{
		try
		{
			if(bigArrayList.getIOType() == BigArrayList.IOTypes.OBJECT)
			{
				fileAccessor.readFromFileObject(fileNumber, cacheIndex, bigArrayList);
			}
			else if(bigArrayList.getIOType() == BigArrayList.IOTypes.MMAP_OBJECT)
			{
				fileAccessor.readFromFileMMapObject(fileNumber, cacheIndex, bigArrayList);
			}
			else if(bigArrayList.getIOType() == BigArrayList.IOTypes.FST_OBJECT)
			{
				fileAccessor.readFromFileFSTObject(fileNumber, cacheIndex, bigArrayList);
			}
			else if(bigArrayList.getIOType() == BigArrayList.IOTypes.MMAP_FST_OBJECT)
			{
				fileAccessor.readFromFileMMapFSTObject(fileNumber, cacheIndex, bigArrayList);
			}
			else
			{
				fileAccessor.readFromFileObject(fileNumber, cacheIndex, bigArrayList);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Deletes all data from disk
	 * 
	 * @throws IOException If an I/O error occurs
	 */
	protected void clearMemory() throws IOException
	{
		fileAccessor.clearMemory();
	}
}