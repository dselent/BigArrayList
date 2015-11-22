
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

import java.nio.BufferOverflowException;
import java.util.Arrays;


//why are negative numbers being stored?

/**
 * Class that manages the mapping from virtual indices to physical indices in memory
 * Since the remove operation is coalesced some elements are not physically removed until later.
 * This means a virtual index must be mapped to the physical index of the desired element.
 * For example if element 3 is virtually removed, the virtual index for the new element 3 maps to the
 * physical index of 4, where element 3 is currently stored.
 * 
 * @author Douglas Selent
 *
 * @param <E> Generic type
 */
class SoftMapping<E>
{
	/**
	 * Minimum buffer size, or the minimum number of unique shift operations that can be stored
	 */
	private static final int MIN_BUFFER_SIZE = 0;
	
	/**
	 * Maximum buffer size, or the maximum number of unique shift operations that can be stored
	 */
	private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;
	
	//not allowing users to specify their own sizes yet, just to make it simpler for them
	//size of number of unique shift places that can be stored
	
	/**
	 * Default buffer size, or the default number of unique shift operations that can be stored
	 */
	private static final int DEFAULT_BUFFER_SIZE = 10000;
	
	//number of cumulative shifts that can be stored
	//elements that have been removed but not deleted from disk
	//this is to limit the amount of disk space wasted
	
	/**
	 * Default number of shifts that can be stored
	 */
	private static final int DEFAULT_SHIFT_ELEMENT_SIZE = 10000;
	
	/**
	 * Size of the first dimension of the shift buffer (number of columns / pieces of information to store)
	 */
	private static final int INFORMATION_PIECES = 3;
	
	/**
	 * Stores the information related to the virtual index to physical mapping in terms of shift offsets.
	 * 
	 * 0 = current index number
	 * 1 = current shift amount for the index
	 * 2 = cumulative shift amount index
	 */
	private long[][] shiftBuffer;

	/**
	 * Current size of the shift buffer
	 */
	private int currentBufferSize;
	
	/**
	 * Default constructor
	 */
	protected SoftMapping()
	{
		shiftBuffer = new long[INFORMATION_PIECES][DEFAULT_BUFFER_SIZE];
		currentBufferSize = 0;
	}

	/**
	 * Constructor that takes the number of unique shift operations that can be stored
	 * 
	 * @param bufferSize The size of unique shifts that can be stored
	 */
	protected SoftMapping(int bufferSize)
	{
		if(bufferSize < MIN_BUFFER_SIZE || bufferSize > MAX_BUFFER_SIZE)
		{
			throw new IllegalArgumentException("Buffer size is " + bufferSize + " but must be >= " + MIN_BUFFER_SIZE + " and <= " + MAX_BUFFER_SIZE);
		}

		shiftBuffer = new long[INFORMATION_PIECES][bufferSize];
		currentBufferSize = 0;
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return Returns the current size of the shift buffer
	 */
	protected int getBufferSize()
	{
		return currentBufferSize;
	}
	
	/**
	 * @param bufferIndex Index of the last element for a cache block
	 * @return Returns the number of shifts at the given index
	 */
	protected long getShiftIndex(int bufferIndex)
	{
		return shiftBuffer[0][bufferIndex];
	}
	
	/**
	 * @return Returns the total number of shifts in the buffer
	 */
	protected long getLastShiftAmount()
	{
		long lastShiftAmount = 0;
		
		if(currentBufferSize > 0)
		{
			lastShiftAmount = shiftBuffer[2][currentBufferSize-1];
		}
		
		return lastShiftAmount;
	}
	
	//whenever skipping over a cache block with shifts, need to add current shift of that block
	/**
	 * Calculates the physical index for the given virtual index
	 * 
	 * @param adjustedIndex Virtual index
	 * @return Returns the physical index
	 */
	protected long getAdjustedIndex(long adjustedIndex)
	{		
		if(currentBufferSize != 0)
		{
			boolean done = false;
			boolean firstPass = true;
			int previousIndex = -1;
			
			while(!done)
			{
				int searchIndex = Arrays.binarySearch(shiftBuffer[0], 0, currentBufferSize, adjustedIndex);				
				
				if(firstPass)
				{
					if(searchIndex >= 0)
					{
						adjustedIndex = adjustedIndex + shiftBuffer[2][searchIndex];
						previousIndex = searchIndex;
					}
					else if(searchIndex == -1)
					{
						//if end index of first block - block shifts < index
						if(shiftBuffer[0][0] - shiftBuffer[1][0] < adjustedIndex)
						{
							adjustedIndex = adjustedIndex + shiftBuffer[2][0];
							previousIndex = 0;
						}
						else
						{
							done = true;
						}	
					}
					else
					{
						//if end index of next block - next block shifts < index
						if(currentBufferSize > (-searchIndex)-1 && shiftBuffer[0][(-searchIndex)-1] - shiftBuffer[2][(-searchIndex)-1]  < adjustedIndex)
						{
							adjustedIndex = adjustedIndex + shiftBuffer[2][(-searchIndex)-1];
							previousIndex = (-searchIndex)-1;
						}
						else
						{
							adjustedIndex = adjustedIndex + shiftBuffer[2][(-searchIndex)-2];
							//previousIndex = (-searchIndex)-2;
							done = true;
						}
						
					}
					
					firstPass = false;
				}
				else
				{
					if(searchIndex >= 0)
					{
						//add previous shifts and current
						for(int i=previousIndex+1; i<=searchIndex; i++)
						{
							adjustedIndex = adjustedIndex + shiftBuffer[1][i];
						}

						previousIndex = searchIndex;
					}
					else
					{
						//add previous shifts
						for(int i=previousIndex+1; i<(-searchIndex)-1; i++)
						{
							adjustedIndex = adjustedIndex + shiftBuffer[1][i];
						}
						
						//if end index of next block - next block shifts < index
						if(currentBufferSize > (-searchIndex)-1 && shiftBuffer[0][(-searchIndex)-1] - shiftBuffer[1][(-searchIndex)-1]  < adjustedIndex)
						{
								adjustedIndex = adjustedIndex + shiftBuffer[1][(-searchIndex)-1];
								previousIndex = (-searchIndex)-1;
						}
						else
						{
							done = true;
						}
					}
				}				
			}
		}	
		
		return adjustedIndex;
	}

	
	/**
	 * Adds the number of shifts for the given position
	 * 
	 * @param position The position to add the shift to
	 * @param shift The shift amount to add
	 */
	protected void addShift(long position, long shift)
	{
		if(shift == 0)
		{
			throw new IllegalArgumentException("Shift cannot be zero");
		}
		
		//if buffer is not full
		
		if(currentBufferSize < shiftBuffer[0].length)
		{
			//search for position to update or add to buffer
			int searchIndex = Arrays.binarySearch(shiftBuffer[0], 0, currentBufferSize, position);
			
			if(searchIndex >= 0)
			{
				shiftBuffer[1][searchIndex] = shiftBuffer[1][searchIndex] + shift;
			}
			else
			{
				searchIndex = (searchIndex * -1) - 1;
				
				for(int i=currentBufferSize; i>searchIndex; i--)
				{
					shiftBuffer[0][i] = shiftBuffer[0][i-1];
					shiftBuffer[1][i] = shiftBuffer[1][i-1];
				}
				
				shiftBuffer[0][searchIndex] = position;
				shiftBuffer[1][searchIndex] = shift;
				
				currentBufferSize++;
			}
			
			updateShifts(searchIndex, currentBufferSize);
		}
		else
		{
			throw new BufferOverflowException();
		}
	}
	
	/**
	 * Updates the shift buffer.  This is used to keep the cumulative statistics correct.
	 * 
	 * @param startIndex Start index
	 * @param endIndex End index
	 */
	private void updateShifts(int startIndex, int endIndex)
	{
		for(int i=startIndex; i<endIndex; i++)
		{
			if(i == 0)
			{
				shiftBuffer[2][i] = shiftBuffer[1][i];
			}
			else
			{
				shiftBuffer[2][i] = shiftBuffer[1][i] + shiftBuffer[2][i-1];
			}
		}
	}
	
	/**	 * 
	 * @param indexNumber The virtual index number
	 * @return Returns the number of shifts for the given virtual index
	 */
	protected long getCurrentShiftAmount(long indexNumber)
	{
		int searchIndex = Arrays.binarySearch(shiftBuffer[0], 0, currentBufferSize, indexNumber);
		
		if(searchIndex == -1)
		{
			return 0;
		}
		else
		{
			return shiftBuffer[1][searchIndex];
		}
	
	}

	/**
	 * Removes the shift for the given virtual index from the shift buffer.  This assumes the array lists have been correctly managed.
	 * 
	 * @param indexNumber Virtual index number
	 */
	protected void removeShift(long indexNumber)
	{
		int searchIndex = Arrays.binarySearch(shiftBuffer[0], 0, currentBufferSize, indexNumber);
		
		long shiftAmount = shiftBuffer[1][searchIndex];
		
		for(int i=searchIndex; i<currentBufferSize; i++)
		{
			shiftBuffer[0][i] = shiftBuffer[0][i+1];
			shiftBuffer[1][i] = shiftBuffer[1][i+1];
			shiftBuffer[2][i] = shiftBuffer[2][i+1] - shiftAmount;
		}
		
		currentBufferSize--;
	}
	
	/**
	 * Clears the shift buffer and sets all virtual indices equal to the physical indices
	 */
	protected void removeAllShifts()
	{
		for(int i=0; i<currentBufferSize; i++)
		{
			shiftBuffer[0][i] = 0;
			shiftBuffer[1][i] = 0;
			shiftBuffer[2][i] = 0;
		}
		
		currentBufferSize = 0;
	}
	
	/**
	 * @return Returns if the shift buffer is full and no more unique shifts can be stored
	 */
	protected boolean isBufferFull()
	{
		boolean full = false;
		
		if(currentBufferSize >= shiftBuffer[0].length)
		{
			full = true;
		}
		
		return full;
	}
	
	/**
	 * @return Returns if the shift buffer is full and no more shifts (unique or not unique) can be stored
	 */
	protected boolean isShiftMaxed()
	{
		boolean maxed = false;
		
		if(currentBufferSize > 0 && shiftBuffer[2][currentBufferSize-1] >= DEFAULT_SHIFT_ELEMENT_SIZE)
		{
			maxed = true;
		}
		
		return maxed;
	}
}