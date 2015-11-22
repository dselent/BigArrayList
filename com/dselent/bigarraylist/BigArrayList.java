
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
import java.util.ArrayList;
import java.util.List;

/**
 * A BigArrayList acts the same way a regular {@link java.util.ArrayList} would for data sizes that cannot fit in memory all at once.
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
public class BigArrayList<E>
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
		MMAP_FST_OBJECT;
	}

	/**
	 * Constructs a BigArrayList with default values for the number of cache block, size of each cache block, and disk path.
	 */
	public BigArrayList()
	{
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(this);
		
		int numCacheBlocks = cacheMapping.getNumberOfBlocks();
		arrayLists = new ArrayList<List<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(cacheMapping.getTableSize());
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
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(memoryPath, this);
		
		int numCacheBlocks = cacheMapping.getNumberOfBlocks();
		arrayLists = new ArrayList<List<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(cacheMapping.getTableSize());
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
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(memoryPath, this);
		
		int numCacheBlocks = cacheMapping.getNumberOfBlocks();
		arrayLists = new ArrayList<List<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(cacheMapping.getTableSize());
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
	 * @param numberOfBlocks Number of cache blocks stored in memory at a given time
	 */
	public BigArrayList(int blockSize, int numberOfBlocks)
	{
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(blockSize, numberOfBlocks, this);
		
		int numCacheBlocks = cacheMapping.getNumberOfBlocks();
		arrayLists = new ArrayList<List<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(cacheMapping.getTableSize());
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
	 * @param numberOfBlocks Number of cache blocks stored in memory at a given time
	 * @param ioType The type of IO to use
	 */
	public BigArrayList(int blockSize, int numberOfBlocks, IOTypes ioType)
	{
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(blockSize, numberOfBlocks, this);
		
		int numCacheBlocks = cacheMapping.getNumberOfBlocks();
		arrayLists = new ArrayList<List<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(cacheMapping.getTableSize());
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
	 * @param numberOfBlocks Number of cache blocks stored in memory at a given time
	 * @param memoryPath The folder path to write to
	 */
	public BigArrayList(int blockSize, int numberOfBlocks, String memoryPath)
	{
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(blockSize, numberOfBlocks, memoryPath, this);
		
		int numCacheBlocks = cacheMapping.getNumberOfBlocks();
		arrayLists = new ArrayList<List<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(cacheMapping.getTableSize());
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
	 * @param numberOfBlocks Number of cache blocks stored in memory at a given time
	 * @param memoryPath The folder path to write to
	 * @param ioType The type of IO to use
	 */
	public BigArrayList(int blockSize, int numberOfBlocks, String memoryPath, IOTypes ioType)
	{
		softMapping = new SoftMapping<E>();
		cacheMapping = new CacheMapping<E>(blockSize, numberOfBlocks, memoryPath, this);
		
		int numCacheBlocks = cacheMapping.getNumberOfBlocks();
		arrayLists = new ArrayList<List<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			ArrayList<E> arrayList = new ArrayList<E>();
			arrayList.ensureCapacity(cacheMapping.getTableSize());
			arrayLists.add(arrayList);
		}

		wholeListSize = 0;
		this.ioType = ioType;
		liveObject = true;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets the ArrayList at the given index
	 * 
	 * @param index The index of the ArrayList/Cache block to set
	 * @param arrayList The new ArrayList/Cache block
	 */
	public void setArrayList(int index, ArrayList<E> arrayList)
	{
		arrayLists.set(index, arrayList);
	}

	/**
	 * Returns the ArrayList at the given index
	 * 
	 * @param index The index of the ArrayList/Cache block to get
	 * @return The ArrayList/Cache block
	 */
	protected List<E> getArrayList(int index)
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
			int usedCacheBlocks = cacheMapping.getNumberOfUsedBlocks(virtualSize);
					
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
				while(cacheBlock.size() < cacheMapping.getTableSize() && nextFileNumber < usedCacheBlocks)
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
			int usedCacheBlocks = cacheMapping.getNumberOfUsedBlocks(virtualSize);
				
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
				while(cacheBlock.size() < cacheMapping.getTableSize() && !done)
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
		int usedCacheBlocks = cacheMapping.getNumberOfUsedBlocks(virtualSize);
		
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
