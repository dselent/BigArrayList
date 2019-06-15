
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

import android.annotation.SuppressLint;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A BigArrayList acts the same way a regular {@link ArrayList} would for data sizes that cannot fit in memory all at once.
 * This class can be used just like an ArrayList, with all the file I/O managed automatically and internally.
 * <p>
 * The size and number of cache blocks to be stored in memory can be specified.
 * <p>
 * BigArrayList uses an internal ArrayList of ArrayLists (A list of cache blocks) to map data in memory to disk.
 * The mapping is managed and maintained by the CacheMapping class.
 * The File I/O uses Object input/output streams for classes that implement the serializable interface.
 * An LRU cache policy is used to determine which block of data so swap out of memory.
 * <p>
 * A BigArrayList currently supports adding elements to the end of the list and setting/getting elements
 * <p>
 * Example code is below:
 * <pre>
 * {@code
 * //Construct an instance of BigArrayList with 4 cache blocks of size 100,000 each
 * //to store data in a folder called "memory" relative to the programs starting file path location
 * BigArrayList<Long> arrayList = new BigArrayList<Long>("memory", 100000, 4);
 * 
 * //Add elements to the list
 * for(long i=0; i<1000000; i++)
 * {
 *     arrayList.add(i);
 * }
 * 
 * //Set and get elements
 * arrayList.set(10, 100l);
 * long getElement = arrayList.get(10);
 * 
 * //Clear data from disk when done
 * arrayList.clearMemory();
 * }
 * </pre>
 * <p>
 * @author Douglas Selent
 *
 * @param <E> Generic type
 */
public class BigArrayList<E extends Serializable>
{
	/**
	 * The ArrayList of cache blocks.
	 * Each ArrayList corresponds to a cache block currently in memory
	 */
	private List<List<E>> arrayLists;
	
	/**
	 * The SoftMapping object used to map the soft indices to the hard indices
	 */
	private final SoftMapping<E> softMapping;
	
	/**
	 * The CacheMapping object used to map contents in memory to contents on disk
	 */
	private final CacheMapping<E> cacheMapping;

	/**
	 * Size of the whole list including what is and is not currently in memory
	 */
	private long wholeListSize;
	
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

	//all methods should check this and throw an exception if false
	/**
	 * Whether or not the BigArrayList is still a live object.
	 * The contents of the BigArrayList must be manually cleared from disk using {@link #clearMemory()}.
	 * Doing so, will result in the BigArrayList object still existing in memory
	 * but it will be unusable since all the references point to content that has been deleted.
	 * In this case the BigArrayList is considered a dead object even though it is not technically considered dead by Java.
	 */
	private boolean liveObject;
	
	/**
	 * Type of serialization to use
	 */
	private IOTypes ioType;
	
	/**
	 * Possible IO serialization types
	 * 
	 * @author Doug
	 */
	public enum IOTypes
	{
		OBJECT,
		MMAP_OBJECT,
		FST_OBJECT,
		MMAP_FST_OBJECT
	}

	/**
	 * Constructs a BigArrayList with default values for the number of cache block, size of each cache block, and disk path.
	 */
	public BigArrayList()
	{
		blockSize = DEFAULT_BLOCK_SIZE;
		cacheBlocks = DEFAULT_CACHE_BLOCKS;
		
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this, blockSize, cacheBlocks);
		
		arrayLists = new ArrayList<List<E>>();

		for(int i=0; i<cacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(blockSize);
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = IOTypes.FST_OBJECT;
		liveObject = true;
	}

	/**
	 * Constructor that specifies where BigArrayList should write to disk.
	 * 
	 * @param memoryPath The folder path to write to
	 */
	public BigArrayList(String memoryPath)
	{
		blockSize = DEFAULT_BLOCK_SIZE;
		cacheBlocks = DEFAULT_CACHE_BLOCKS;
		
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this, blockSize, cacheBlocks, memoryPath);
		
		arrayLists = new ArrayList<List<E>>();

		for(int i=0; i<cacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(blockSize);
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = IOTypes.FST_OBJECT;
		liveObject = true;
	}
	
	/**
	 * Constructor that specifies where BigArrayList should write to disk.
	 * 
	 * @param memoryPath The folder path to write to
	 * @param ioType The type of IO to use
	 */
	public BigArrayList(String memoryPath, IOTypes ioType)
	{
		blockSize = DEFAULT_BLOCK_SIZE;
		cacheBlocks = DEFAULT_CACHE_BLOCKS;
		
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this, blockSize, cacheBlocks, memoryPath);
		
		arrayLists = new ArrayList<List<E>>();

		for(int i=0; i<cacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(blockSize);
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = ioType;
		liveObject = true;
	}

	/**
	 * Constructor that specifies the size and number of cache blocks.
	 * 
	 * @param blockSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks stored in memory at a given time
	 */
	public BigArrayList(int blockSize, int cacheBlocks)
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
		
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this, blockSize, cacheBlocks);
		
		arrayLists = new ArrayList<List<E>>();

		for(int i=0; i<cacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(blockSize);
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = IOTypes.FST_OBJECT;
		liveObject = true;
	}
	
	/**
	 * Constructor that specifies the size and number of cache blocks.
	 * 
	 * @param blockSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks stored in memory at a given time
	 * @param ioType The type of IO to use
	 */
	public BigArrayList(int blockSize, int cacheBlocks, IOTypes ioType)
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
		
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this, blockSize, cacheBlocks);
		
		arrayLists = new ArrayList<List<E>>();

		for(int i=0; i<cacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(blockSize);
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = ioType;
		liveObject = true;
	}


	/**
	 * Constructor that specifies  the size and number of cache blocks as well as the folder path to write to.
	 * 
	 * @param blockSize cacheSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks stored in memory at a given time
	 * @param memoryPath The folder path to write to
	 */
	public BigArrayList(int blockSize, int cacheBlocks, String memoryPath)
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
		
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this, blockSize, cacheBlocks, memoryPath);
		
		arrayLists = new ArrayList<List<E>>();

		for(int i=0; i<cacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(blockSize);
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = IOTypes.FST_OBJECT;
		liveObject = true;
	}
	
	/**
	 * Constructor that specifies  the size and number of cache blocks as well as the folder path to write to.
	 * 
	 * @param blockSize cacheSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks stored in memory at a given time
	 * @param memoryPath The folder path to write to
	 * @param ioType The type of IO to use
	 */
	public BigArrayList(int blockSize, int cacheBlocks, String memoryPath, IOTypes ioType)
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
		
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this, blockSize, cacheBlocks, memoryPath);
		
		arrayLists = new ArrayList<List<E>>();

		for(int i=0; i<cacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(blockSize);
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = ioType;
		liveObject = true;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return Returns the associated CacheMapping object
	 */
	protected CacheMapping<E> getCacheMapping()
	{
		return cacheMapping;
	}
	
	/**
	 * @return Returns the number of blocks in memory at a time
	 */
	public int getNumberOfBlocks()
	{
		return cacheBlocks;
	}
	
	/**
	 * @return Returns the block size
	 */
	public int getBlockSize()
	{
		
		return blockSize;
	}
	
	/**
	 * @return Returns the number of used cache blocks based on the size of the list
	 */
	protected int getNumberOfUsedBlocks()
	{
		long blockSizeLong = blockSize;
		long usedBlocks = (long) Math.ceil(this.size() * 1.0 / blockSizeLong * 1.0);
		
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
	 * @return Returns the file location of the memory storage
	 */
	public String getFilePath()
	{
		return cacheMapping.getFileAccessor().getMemoryFilePath();
	}
	
	/**
	 * Sets the ArrayList at the given index
	 * 
	 * @param index The index of the ArrayList/Cache block to set
	 * @param arrayList The new ArrayList/Cache block
	 */
	public void setList(int index, List<E> arrayList)
	{
		arrayLists.set(index, arrayList);
	}

	/**
	 * Returns the ArrayList at the given index
	 * 
	 * @param index The index of the ArrayList/Cache block to get
	 * @return The ArrayList/Cache block
	 */
	protected List<E> getList(int index)
	{
		return arrayLists.get(index);
	}

	/**
	 * Used by the CacheMapping class for swapping caches.
	 * @param index The index of the cache block to clear data from
	 */
	protected void clearList(int index)
	{
		arrayLists.get(index).clear();
	}

	/**
	*
	 * @return The size of the BigArrayList
	 */
	public long size()
	{
		return wholeListSize;
	}
	
	/**
	 * 
	 * @return The IO serialization type
	 */
	public IOTypes getIOType()
	{
		return ioType;
	}

	/**
	 * Returns the size of the ArrayList at the specified index
	 * 
	 * @param index The index of the ArrayList/Cache block
	 * @return The size of ArrayList/Cache block
	 */
	protected int getArraySize(int index)
	{
		return arrayLists.get(index).size();
	}

	/**
	 * Whether or no the object is live
	 * @return True if the object is live, false otherwise
	 */
	public boolean isLive()
	{
		return liveObject;
	}

	/**
	 * Used to delete the memory file.
	 * The object should not be used anymore once this method is called
	 * 
	 * @throws IOException For I/O error
	 */
	public void clearMemory() throws IOException
	{
		cacheMapping.clearMemory();
		liveObject = false;
	}

	/**
	 * Flushes all data in memory to disk
	 */
	public void flushMemory()
	{
		cacheMapping.flushCache();
	}
		
	/**
	 * Sorts the BigArrayList.  Note that the usage mimics Collections.sort() except that the sorted list is returned.
	 * The caller must set their list to equal the return value (similar to String concatenation), ex:  sortedList = BigArrayList.sort(sortedList);
	 * 
	 * @param unsortedList The list to be sorted
	 * @return The list in sorted order
	 * @throws IOException
	 */
	@SuppressLint("NewApi")
	public static<T extends Comparable<? super T> & Serializable> BigArrayList<T> sort(BigArrayList<T> unsortedList) throws IOException
	{
		return sort(unsortedList, Comparator.naturalOrder());
	}
	
	/**
	 * Sorts the BigArrayList.  Note that the usage mimics Collections.sort() except that the sorted list is returned.
	 * The caller must set their list to equal the return value (similar to String concatenation), ex:  sortedList = BigArrayList.sort(sortedList);
	 * 
	 * @param unsortedList unsortedList The list to be sorted
	 * @param comparator How to compare the elements in the list
	 * @return The list in sorted order
	 * @throws IOException
	 */
	public static<T extends Serializable> BigArrayList<T> sort(BigArrayList<T> unsortedList, Comparator<? super T> comparator) throws IOException
	{
		if(unsortedList.size() <= 1)
		{
			return unsortedList;
		}
		else
		{
			unsortedList.purgeActionBuffer();
				
			CacheMapping<T> unsortedCacheMapping = unsortedList.getCacheMapping();
			int blockSize = unsortedList.getBlockSize();
			int cacheBlocks = unsortedList.getNumberOfBlocks();
			int usedCacheBlocks = unsortedList.getNumberOfUsedBlocks();
			String filePath = unsortedList.getFilePath();
			IOTypes ioType = unsortedList.getIOType();
			
			BigArrayList<T> sortedList = new BigArrayList<T>(blockSize, cacheBlocks, filePath, ioType);
				
			for(int i=usedCacheBlocks-1; i>=0; i--)
			{
				int cacheBlockSpot = -1;
	
				if(!unsortedCacheMapping.isFileInCache(i))
				{
					unsortedCacheMapping.bringFileIntoCache(i);
				}
		
				cacheBlockSpot = unsortedCacheMapping.getCacheBlockSpot(i);
				unsortedCacheMapping.setDirtyBit(cacheBlockSpot, true);
				Collections.sort(unsortedList.getList(cacheBlockSpot), comparator);
			}	
				
			if(usedCacheBlocks > 1)
			{
				int currentRun = 0;
				long totalRuns = 64 - Long.numberOfLeadingZeros(usedCacheBlocks-1);
					
				while(currentRun < totalRuns)
				{
					sortedList = merge(unsortedList, comparator, currentRun);
		
					unsortedList.clearMemory();
						
					unsortedList = sortedList;
					currentRun++;
				}
			}
			else
			{
				sortedList = unsortedList;
			}
				
			return sortedList;
		}
	}
	
	/**
	 * Internal function used to sort.  This is the step to merge two sorted pieces together into a single sorted list.
	 * 
	 * @param unsortedList The list
	 * @param comparator How to compare the elements
	 * @param currentRun What run step the merge is being used on.  This is to determine what merge pieces to merge and their sizes
	 * @return The unsorted list with the sorted merged pieces.
	 */
	private static<T extends Serializable> BigArrayList<T> merge(BigArrayList<T> unsortedList, Comparator<? super T> comparator, int currentRun)
	{
		int blockSize = unsortedList.getBlockSize();
		int cacheBlocks = unsortedList.getNumberOfBlocks();
		long usedCacheBlocksLong = unsortedList.getNumberOfUsedBlocks();
		String filePath = unsortedList.getFilePath();
		IOTypes ioType = unsortedList.getIOType();
		
		BigArrayList<T> sortedList = new BigArrayList<T>(blockSize, cacheBlocks, filePath, ioType);
		
		int blockIncrement = ipow(2, currentRun);
		
		for(long i=0; i<usedCacheBlocksLong; i=i+blockIncrement+blockIncrement)
		{
			long mergePiece1 = i;
			long mergePiece2 = -1;
			long mergePiece3 = -1;
			
			long firstElementStart = mergePiece1*blockSize;
			long secondElementStart = -1;
			
			long firstElementEnd = -1;
			long secondElementEnd = -1;
			
			long currentMergeElements = 0L;
			long totalMergeElements = -1;
			
			if(i+blockIncrement < usedCacheBlocksLong)
			{
				mergePiece2 = i+blockIncrement;
				mergePiece3 = i+blockIncrement+blockIncrement;
				
				secondElementStart = mergePiece2*blockSize;
				firstElementEnd = secondElementStart;
				
				if(unsortedList.size() >= mergePiece3*blockSize)
				{
					secondElementEnd = mergePiece3*blockSize;
				}
				else
				{
					secondElementEnd = unsortedList.size();
				}
				
				totalMergeElements = secondElementEnd - firstElementStart;
			}
			else
			{				
				if(unsortedList.size() >= (i+blockIncrement)*blockSize)
				{
					firstElementEnd = (i+blockIncrement)*blockSize;
				}
				else
				{
					firstElementEnd = unsortedList.size();
				}
				
				totalMergeElements = firstElementEnd - firstElementStart;
			}
						
	
			long index1 = firstElementStart;
			long index2 = secondElementStart;
						
			if(mergePiece2 == -1 || mergePiece2 >= usedCacheBlocksLong)
			{
				T element1 = unsortedList.get(index1);

				while(currentMergeElements < totalMergeElements)
				{
					sortedList.add(element1);
					index1++;
					
					if(index1 < firstElementEnd)
					{
						element1 = unsortedList.get(index1);
					}
					
					currentMergeElements++;
				}
			}
			else
			{
				T element1 = unsortedList.get(index1);
				T element2 = unsortedList.get(index2);
				
				while(currentMergeElements < totalMergeElements)
				{
					if(index2 >= secondElementEnd)
					{
						sortedList.add(element1);
						index1++;
						
						if(index1 < firstElementEnd)
						{
							element1 = unsortedList.get(index1);
						}
					}
					else if(index1 >= firstElementEnd)
					{
						sortedList.add(element2);
						index2++;
						
						if(index2 < secondElementEnd)
						{
							element2 = unsortedList.get(index2);
						}
					}
					else
					{
						if(comparator.compare(element1, element2) <= 0)
						{
							sortedList.add(element1);
							index1++;
							
							if(index1 < firstElementEnd)
							{
								element1 = unsortedList.get(index1);
							}
						}
						else
						{
							sortedList.add(element2);
							index2++;
							
							if(index2 < secondElementEnd)
							{
								element2 = unsortedList.get(index2);
							}
						}
					}
					
					currentMergeElements++;
				}
			}
		}
		
		return sortedList;
	}
	
	
	//skipping bound checks, trusting the caller to know that the result will not be greater an integer
	/**
	 * Internal function used by sorting.
	 * modified function from http://stackoverflow.com/questions/101439/the-most-efficient-way-to-implement-an-integer-based-power-function-powint-int
	 * 
	 * @param base Base number
	 * @param exp Exponent
	 * @return Ceiling of base^exp as a power of two
	 */
	private static int ipow(int base, int exp)
	{
	    int result = 1;
	    
	    while (exp != 0)
	    {
	        if ((exp & 1) == 1)
	        {
	            result *= base;
	        }
	        
	        exp >>= 1;
	        base *= base;
	    }

	    return result;
	}

	/**
	 * Purges all actions in the queue
	 */
	private void purgeActionBuffer()
	{
		if(softMapping.getBufferSize() > 0)
		{
			long startIndex = softMapping.getShiftIndex(0);			
				
			//first index
			int fileNumber = cacheMapping.getFileNumber(startIndex);
			int nextFileNumber = fileNumber+1;
			long virtualSize = wholeListSize + softMapping.getLastShiftAmount();
			int usedCacheBlocks = getNumberOfUsedBlocks(virtualSize);
					
			int cacheBlockSpot = -1;
			int nextCacheBlockSpot = -1;

			if(!cacheMapping.isFileInCache(fileNumber))
			{
				cacheBlockSpot = cacheMapping.bringFileIntoCache(fileNumber);
			}
			else
			{
				cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);
				cacheMapping.updateUsedList(cacheBlockSpot);
			}
				
			//assume there will be changes, this assumption is not always true?
			cacheMapping.setDirtyBit(cacheBlockSpot, true);
				
			if(!cacheMapping.isFileInCache(nextFileNumber))
			{
				nextCacheBlockSpot = cacheMapping.bringFileIntoCache(nextFileNumber);
				cacheMapping.setDirtyBit(nextCacheBlockSpot, true);
			}
			else
			{
				nextCacheBlockSpot = cacheMapping.getCacheBlockSpot(nextFileNumber);
				cacheMapping.setDirtyBit(nextCacheBlockSpot, true);
			}
				
			//SHOULD FIND MAX SHIFT TOO
			while(nextFileNumber < usedCacheBlocks)
			{		
				List<E> cacheBlock = arrayLists.get(cacheBlockSpot);
				List<E> nextCacheBlock = arrayLists.get(nextCacheBlockSpot);
					
				//SHOULD FIND MAX SHIFT TOO
				while(cacheBlock.size() < getBlockSize() && nextFileNumber < usedCacheBlocks)
				{
					if(nextCacheBlock.size() > 0)
					{
						cacheBlock.add(nextCacheBlock.remove(0));
						cacheMapping.removeEntry(nextCacheBlockSpot);
						cacheMapping.addEntry(cacheBlockSpot);
					}
					else
					{
						cacheMapping.updateUsedList(cacheBlockSpot);
						nextFileNumber++;
							
						//not reached end of blocks
						if(nextFileNumber < usedCacheBlocks)
						{
							//next file is not in cache
							if(!cacheMapping.isFileInCache(nextFileNumber))
							{
								nextCacheBlockSpot = cacheMapping.bringFileIntoCache(nextFileNumber);
							}
							else
							{
								nextCacheBlockSpot = cacheMapping.getCacheBlockSpot(nextFileNumber);
							}
							
							nextCacheBlock = arrayLists.get(nextCacheBlockSpot);
							cacheMapping.setDirtyBit(nextCacheBlockSpot, true);
						}
					}
				}

				fileNumber++;
				
				if(fileNumber == nextFileNumber)
				{
					nextFileNumber = fileNumber + 1;
				}
						
				//if not at end
				if(nextFileNumber < usedCacheBlocks)
				{				
					if(!cacheMapping.isFileInCache(fileNumber))
					{
						cacheBlockSpot = cacheMapping.bringFileIntoCache(fileNumber);
					}
					else
					{
						cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);
						cacheMapping.updateUsedList(cacheBlockSpot);
					}

					cacheMapping.setDirtyBit(cacheBlockSpot, true);
							
					if(!cacheMapping.isFileInCache(nextFileNumber))
					{
						nextCacheBlockSpot = cacheMapping.bringFileIntoCache(nextFileNumber);
					}
					else
					{
						nextCacheBlockSpot = cacheMapping.getCacheBlockSpot(nextFileNumber);	
					}

					cacheMapping.setDirtyBit(nextCacheBlockSpot, true);
				}
			}

			softMapping.removeAllShifts();
		}
	}


	/**
	 * Purges action queue for all consecutive blocks in cache starting at startIndex
	 * 
	 * @param startIndex The block index to start at
	 */
	private void purgeActionBufferInCache(long startIndex)
	{
		if(softMapping.getBufferSize() > 0)
		{
			boolean done = false;
			
			//first index
			int fileNumber = cacheMapping.getFileNumber(startIndex);
			int nextFileNumber = fileNumber+1;
			long virtualSize = wholeListSize + softMapping.getLastShiftAmount();
			int usedCacheBlocks = getNumberOfUsedBlocks(virtualSize);
				
			int cacheBlockSpot = -1;
			int nextCacheBlockSpot = -1;

			if(!cacheMapping.isFileInCache(fileNumber))
			{
				cacheBlockSpot = cacheMapping.bringFileIntoCache(fileNumber);
			}
			else
			{
				cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);
			}
			
			//assume there will be changes, this assumption is not always true?
			cacheMapping.setDirtyBit(cacheBlockSpot, true);
			
			if(!cacheMapping.isFileInCache(nextFileNumber))
			{
				done = true;
			}
			else
			{
				nextCacheBlockSpot = cacheMapping.getCacheBlockSpot(nextFileNumber);
				cacheMapping.setDirtyBit(nextCacheBlockSpot, true);
			}
			
			long lastIndexInBlock = cacheMapping.getLastIndexInFile(fileNumber);
			long lastIndexInNextBlock = cacheMapping.getLastIndexInFile(nextFileNumber);
			
			while(nextFileNumber < usedCacheBlocks && !done)
			{
				long shiftAmount = softMapping.getCurrentShiftAmount(lastIndexInBlock);
				softMapping.removeShift(lastIndexInBlock);
				
				//count of shifts done into current cache block
				long shiftCount = 0;
				
				//count of shifts done from next cache block
				long nextShiftCount = 0;
				
				List<E> cacheBlock = arrayLists.get(cacheBlockSpot);
				List<E> nextCacheBlock = arrayLists.get(nextCacheBlockSpot);
				
				//shift down to current block
				while(cacheBlock.size() < getBlockSize() && !done)
				{
					if(nextCacheBlock.size() > 0)
					{
						cacheBlock.add(nextCacheBlock.remove(0));
						cacheMapping.removeEntry(nextCacheBlockSpot);
						cacheMapping.addEntry(cacheBlockSpot);
						shiftCount++;
						nextShiftCount++;
					}
					else
					{
						//next block is empty, reset nextShiftCount
						if(nextShiftCount > 0)
						{
							softMapping.addShift(lastIndexInNextBlock, nextShiftCount);
							nextShiftCount = 0;
						}

						nextFileNumber++;
						
						//not reached end of blocks
						if(nextFileNumber < usedCacheBlocks)
						{
							//next file is not in cache
							if(!cacheMapping.isFileInCache(nextFileNumber))
							{
								done = true;
							}
							else
							{
								nextCacheBlockSpot = cacheMapping.getCacheBlockSpot(nextFileNumber);
								nextCacheBlock = arrayLists.get(nextCacheBlockSpot);
								lastIndexInNextBlock = cacheMapping.getLastIndexInFile(nextFileNumber);
								cacheMapping.setDirtyBit(nextCacheBlockSpot, true);
							}
						}
						else
						{
							done = true;
						}
					}
				}
				
				//may have ended loop without doing all shifts for current block
				//add back in
				if(shiftAmount - shiftCount > 0)
				{
					softMapping.addShift(lastIndexInBlock, shiftAmount-shiftCount);
				}
				else
				{
					//current block = full
					
					fileNumber++;
					
					if(fileNumber == nextFileNumber)
					{
						nextFileNumber = fileNumber + 1;
					}
					
					//if not at end
					if(nextFileNumber < usedCacheBlocks)
					{				
						//add remaining shifts to next block
						//no shifts to add to current block since it was filled
						softMapping.addShift(lastIndexInNextBlock, nextShiftCount);
						
						if(!cacheMapping.isFileInCache(fileNumber))
						{
							done = true;
						}
						else
						{
							cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);
							lastIndexInBlock = cacheMapping.getLastIndexInFile(fileNumber);
							cacheMapping.setDirtyBit(cacheBlockSpot, true);
						}
						
						if(!cacheMapping.isFileInCache(nextFileNumber))
						{
							done = true;
						}
						else
						{
							nextCacheBlockSpot = cacheMapping.getCacheBlockSpot(nextFileNumber);
							lastIndexInNextBlock = cacheMapping.getLastIndexInFile(nextFileNumber);
							cacheMapping.setDirtyBit(nextCacheBlockSpot, true);
						}
					}
				}				
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Adds an element to the end of the list.
	 * Analogous to the add method of the ArrayList class
	 * 
	 * @param element The element to add
	 * @return If the element was added or not
	 */
	public boolean add(E element)
	{
		boolean added = false;

		long adjustedIndex = softMapping.getAdjustedIndex(wholeListSize);
		int lastFile = cacheMapping.getFileNumber(adjustedIndex);
		int cacheBlockSpot = -1;

		if(!cacheMapping.isFileInCache(lastFile))
		{
			//bring last file into cache
			cacheMapping.bringFileIntoCache(lastFile);
		}

		cacheBlockSpot = cacheMapping.getCacheBlockSpot(lastFile);

		//no -1 check, assumed it was brought in
		//if last file is not full
		if(!cacheMapping.isCacheFull(cacheBlockSpot))
		{
			//add to last array list and update cache entry
			added = arrayLists.get(cacheBlockSpot).add(element);

			if(added)
			{
				cacheMapping.addEntry(cacheBlockSpot);
				cacheMapping.setDirtyBit(cacheBlockSpot, true);
				wholeListSize++;
			}
			
		}
		else
		{
			throw new RuntimeException("Failed to add " + element + " at the end of the list");
		}

		return added;
	}

	/**
	 * Gets an element at the specified index.
	 * Analogous to the get method of the ArrayList class
	 * 
	 * @param index The index
	 * @return The element
	 */
	public E get(long index)
	{
		if(index < 0 || index >= wholeListSize)
		{
			throw new IndexOutOfBoundsException(" " + index + " ");
		}


		//if index not in cache and not greater than max
			//bring corresponding file in cache

		long adjustedIndex = softMapping.getAdjustedIndex(index);
		int fileNumber = cacheMapping.getFileNumber(adjustedIndex);

		if(!cacheMapping.isFileInCache(fileNumber))
		{
			cacheMapping.bringFileIntoCache(fileNumber);
		}

		int cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);

		//find cache that index is in
		//find cache spot
		//get from the cache spot

		int spotInCache = cacheMapping.getSpotInCache(adjustedIndex);

		return arrayLists.get(cacheBlockSpot).get(spotInCache);
	}

	/**
	 * Analogous to the get method of the ArrayList class
	 * 
	 * @param index The index
	 * @return Returns the element at the specified index
	 */
	public E get(int index)
	{
		long longIndex = index;
		return get(longIndex);
	}
	
	
	/**
	 * Analogous to the remove method of the ArrayList class
	 * 
	 * @param index The index
	 * @return Returns the element removed at the specified index
	 */
	public E remove(long index)
	{
		if(index < 0 || index >= wholeListSize)
		{
			throw new IndexOutOfBoundsException(" " + index + " ");
		}
		
		//can possibly add something to the buffer
		//safest place to clear the buffer is here
		if(softMapping.isBufferFull() || softMapping.isShiftMaxed())
		{
			purgeActionBuffer();
		}
		
		long adjustedIndex = softMapping.getAdjustedIndex(index);
		int fileNumber = cacheMapping.getFileNumber(adjustedIndex);

		if(!cacheMapping.isFileInCache(fileNumber))
		{
			cacheMapping.bringFileIntoCache(fileNumber);
		}
		
		int cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);
		int spotInCache = cacheMapping.getSpotInCache(adjustedIndex);
		long virtualSize = wholeListSize + softMapping.getLastShiftAmount();
		int usedCacheBlocks = getNumberOfUsedBlocks(virtualSize);
		
		List<E> cacheBlock = arrayLists.get(cacheBlockSpot);		
		E element = cacheBlock.remove(spotInCache);
		cacheMapping.removeEntry(cacheBlockSpot);
		cacheMapping.setDirtyBit(cacheBlockSpot, true);
		
		//need to shift other lists down to the one where an element was just removed
			//update SoftMapping for the remove action

		long lastIndexInBlock = cacheMapping.getLastIndexInFile(fileNumber);
		
		//if this block is the last block, no need to care about unnecessary shifts
		if((fileNumber+1) < usedCacheBlocks)
		{			
			softMapping.addShift(lastIndexInBlock, 1);
			purgeActionBufferInCache(lastIndexInBlock);
		}

		wholeListSize--;
		
		return element;
	}

	/**
	 * Analogous to the remove method of the ArrayList class
	 * 
	 * @param index The index
	 * @return Returns the element removed at the specified index
	 */
	public E remove(int index)
	{
		long longIndex = index;
		return remove(longIndex);
	}

	/**
	 * Sets the element at the specified index
	 * Analogous to the set method of the ArrayList class
	 * 
	 * @param index The index
	 * @param element The new element
	 * @return The new element at the specified index
	 */
	public E set(long index, E element)
	{
		if(index < 0 || index >= wholeListSize)
		{
			throw new IndexOutOfBoundsException(" " + index + " ");
		}

		//if index not in cache and not greater than max
			//bring corresponding file in cache

		long adjustedIndex = softMapping.getAdjustedIndex(index);
		int fileNumber =  cacheMapping.getFileNumber(adjustedIndex);

		if(!cacheMapping.isFileInCache(fileNumber))
		{
			cacheMapping.bringFileIntoCache(fileNumber);
		}

		int cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);

		//find cache that index is in
		//find cache spot
		//get from the cache spot

		int spotInCache = cacheMapping.getSpotInCache(adjustedIndex);
		cacheMapping.setDirtyBit(cacheBlockSpot, true);

		return arrayLists.get(cacheBlockSpot).set(spotInCache, element);
	}

	/**
	 * Sets the element at the specified index
	 * Analogous to the set method of the ArrayList class
	 * 
	 * @param index The index
	 * @param element The new element
	 * @return The new element at the specified index
	 */
	public E set(int index, E element)
	{
		long longIndex = index;
		return set(longIndex, element);
	}
	
	
	/**
	 * Returns if the list is empty.
	 * 
	 * @return True is the list is empty, false otherwise
	 */
	public boolean isEmpty()
	{
		boolean empty = true;
		
		if(size() > 0)
		{
			empty = false;
		}
		
		return empty;
	}


	//hashCode cannot be implemented correctly due to contents being on disk and out of sight from memory

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object otherObject)
	{
		
		boolean isEqual = true;
		
		if(otherObject == null)
		{
			isEqual = false;
		}
		else if (this == otherObject)
		{
			isEqual = true;
		}
		else if(!(otherObject instanceof BigArrayList))
		{
			isEqual = false;
		}
		else
		{
			BigArrayList otherBigArrayList = (BigArrayList) otherObject;
			
			if(wholeListSize != otherBigArrayList.size())
			{
				isEqual = false;
			}
			else if(liveObject != otherBigArrayList.isLive())
			{
				isEqual = false;
			}
			else if(ioType != otherBigArrayList.ioType)
			{
				isEqual = false;
			}
			else
			{
				for(long i=0; i<wholeListSize && isEqual; i++)
				{
					if(!get(i).equals(otherBigArrayList.get(i)))
					{
						isEqual = false;
					}
				}
			}
		}
		
		return isEqual;
	}

}
