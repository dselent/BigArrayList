
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

import java.util.ArrayList;

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
	private ArrayList<ArrayList<E>> arrayLists;
	
	/**
	 * The CacheMapping object used to map contents in memory to contents on disk.
	 */
	private CacheMapping cacheMapping;

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
	 * Constructs a BigArrayList with default values for the number of cache block, size of each cache block, and disk path.
	 */
	public BigArrayList()
	{
		cacheMapping = new CacheMapping(this);
		int numCacheBlocks = cacheMapping.getNumberOfCacheBlocks();
		arrayLists = new ArrayList<ArrayList<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			arrayLists.add(new ArrayList<E>());
		}

		wholeListSize = 0;
		liveObject = true;
	}

	/**
	 * Constructor that specifies where BigArrayList should write to disk.
	 * @param memoryFilePath The folder path to write to
	 */
	public BigArrayList(String memoryFilePath)
	{
		cacheMapping = new CacheMapping(memoryFilePath, this);
		int numCacheBlocks = cacheMapping.getNumberOfCacheBlocks();
		arrayLists = new ArrayList<ArrayList<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			arrayLists.add(new ArrayList<E>());
		}

		wholeListSize = 0;
		liveObject = true;
	}

	/**
	 * Constructor that specifies the size and number of cache blocks.
	 * @param cacheSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks stored in memory at a given time
	 */
	public BigArrayList(int cacheSize, int cacheBlocks)
	{
		cacheMapping = new CacheMapping(cacheSize, cacheBlocks, this);
		int numCacheBlocks = cacheMapping.getNumberOfCacheBlocks();
		arrayLists = new ArrayList<ArrayList<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			arrayLists.add(new ArrayList<E>());
		}

		wholeListSize = 0;
		liveObject = true;
	}

	/**
	 * Constructor that specifies  the size and number of cache blocks as well as the folder path to write to.
	 * @param memoryFilePath The folder path to write to
	 * @param cacheSize cacheSize Size of each cache block
	 * @param cacheBlocks Number of cache blocks stored in memory at a given time
	 */
	public BigArrayList(String memoryFilePath, int cacheSize, int cacheBlocks)
	{
		cacheMapping = new CacheMapping(cacheSize, cacheBlocks, memoryFilePath, this);
		int numCacheBlocks = cacheMapping.getNumberOfCacheBlocks();
		arrayLists = new ArrayList<ArrayList<E>>(numCacheBlocks);

		for(int i=0; i<numCacheBlocks; i++)
		{
			arrayLists.add(new ArrayList<E>());
		}

		wholeListSize = 0;
		liveObject = true;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Not used yet
	 * @param index The index of the ArrayList/Cache block to set
	 * @param arrayList The new ArrayList/Cache block
	 */
	/*
	public void setArrayList(int index, ArrayList<E> arrayList)
	{
		arrayLists.set(index, arrayList);
	}
	*/

	/**
	 * Returns the size of the ArrayList
	 * 
	 * @param index The index of the ArrayList/Cache block to get
	 * @return The ArrayList/Cache block
	 */
	protected ArrayList<E> getArrayList(int index)
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
	public void clearMemory()
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

	///////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////


	//@Override
	/**
	 * Adds an element to the end of the list.
	 * Analogous to the add method of the ArrayList class
	 * @param element The element to add
	 * @return If the element was added or not
	 */
	public boolean add(E element)
	{
		boolean added = false;
		int lastFile = cacheMapping.getCacheFileNumber(wholeListSize);
		int cacheBlockSpot = -1;

		if(!cacheMapping.isFileInCache(lastFile))
		{
			//bring last file into cache
			cacheMapping.bringFileIntoCache(lastFile, true);

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

		return added;
	}

	/**
	 * Gets an element at the specified index.
	 * Analogous to the get method of the ArrayList class
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

		int fileNumber = cacheMapping.getCacheFileNumber(index);

		if(!cacheMapping.isFileInCache(fileNumber))
		{
			cacheMapping.bringFileIntoCache(fileNumber, false);
		}

		int cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);

		//find cache that index is in
		//find cache spot
		//get from the cache spot

		int spotInCache = cacheMapping.getSpotInCache(index);

		return arrayLists.get(cacheBlockSpot).get(spotInCache);
	}

	/**
	 * Analogous to the get method of the ArrayList class
	 * @param index The index
	 * @return Returns the element at the specified index
	 */
	public E get(int index)
	{
		return get(new Long(index));
	}

	/**
	 * Sets the element at the specified index
	 * Analogous to the set method of the ArrayList class
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

		int fileNumber =  cacheMapping.getCacheFileNumber(index);

		if(!cacheMapping.isFileInCache(fileNumber))
		{
			cacheMapping.bringFileIntoCache(fileNumber, false);
		}

		int cacheBlockSpot = cacheMapping.getCacheBlockSpot(fileNumber);

		//find cache that index is in
		//find cache spot
		//get from the cache spot

		int spotInCache = cacheMapping.getSpotInCache(index);
		cacheMapping.setDirtyBit(cacheBlockSpot, true);

		return arrayLists.get(cacheBlockSpot).set(spotInCache, element);
	}

	/**
	 * Sets the element at the specified index
	 * Analogous to the set method of the ArrayList class
	 * @param index The index
	 * @param element The new element
	 * @return The new element at the specified index
	 */
	public E set(int index, E element)
	{
		return set(new Long(index), element);
	}
	
	
	/**
	 * Returns if the list is empty.
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
