# BigArrayList

A BigArrayList is basically an ArrayList that can handle a much larger amount of data.  It is used the same way as a regular ArrayList (same function signatures), however it stores data that cannot fit into memory on disk.  Currently a BigArrayList supports the following operations.

1. Adding elements to the end of the list
2. Getting and Setting elements
3. Removing elements from the list

## BigArrayList Size

The number of elements a BigArrayList can hold is 2^63-1 elements.  This number is currently considered a theoretical limit, since it would take too much time and space to store that many elements. A more practical limit would be based on the combination of available RAM and disk space because the amount of space will likely be the limiting factor.

## Internal Working

BigArrayList is internally made up of an ArrayList of ArrayList objects, which represents the number of cache blocks and the elements in each cache block. The size of the outer ArrayList is equal to the number of cache blocks, where each element represents a cache block. The size of the inner ArrayList is equal to the size of a cache block, where each element is an element of data in the cache block. This is the largest amount of data that can be held in memory at a given time. All other data is stored on disk and swapped into memory when needed. Default values are provided for the number of cache blocks and their size; however the programmer has the option to set these values.

BigArrayList uses an LRU cache replacement policy to determine which block of data should be swapped out of memory and written to disk. Data is only written to disk if it has changed since it was read in from disk. This is a small optimization to prevent unnecessary file I/O for content that has not changed (likely due to read-only operations).

## Where data is stored

All files written to disk are stored in one folder, which can be specified by the programmer. Each BigArrayList instance has its own file prefix in order to distinguish one instance from another. This is done automatically by analyzing the files in the designated folder. The first file will always be named in the form of “<memoryInstanceNumber >_memory_0.jobj", where the variable “memoryInstanceNumber” uniquely defines the instance. A loop in the program starts with this variable set to zero and will continue to loop and increment the variable until a file name does not exist. This allows for multiple BigArrayList objects to be used in a single program as well as an array of BigArrayList objects.


## Types of serialization

1. Regular Object
2. Memory-mapped
3. [FST](https://github.com/RuedigerMoeller/fast-serialization/wiki/Serialization) 
4. Memory-mapped + FST

## Code Example

    
    import com.dselent.bigarraylist.BigArrayList;
    
    public class SimpleExample
    {
      public static void main(String args[]) throws Exception
      {
        //create a BigArrayList of Longs
        //cache block size = 1 million
        //cache blocks = 4
        BigArrayList<Long> bal = new BigArrayList<Long>(1000000, 4);
			
        //add 10 million elements
        for(long i=0; i<10000000; i++)
        {
          bal.add(i);
        }
			
        //get the element at index 5
        System.out.println(bal.get(5));
			
        //set the element at index 5
        bal.set(5, 100l);
			
        //get the element at index 5
        System.out.println(bal.get(5));
		
        //clear contents on disk
        bal.clearMemory();
      }
    }


## How to Use

Use BigArrayList as if it were a regular ArrayList.  There are more constructor options to specify the amount of data in memory and fewer functions.  Make sure to call "clearMemory()" when done using the object.  Download the javadocs in the doc folder for more information and the BigArrayList.jar file to conveniently add the library to existing projects.

## Notes + Warnings
Random operations are slow and should be avoided.

Some types of serialization will clear the contents on disk automatically when your program terminates and some will not.  It is recommended to use the "clearMemory()" function when you are done using the BigArrayList.  If your program crashes for any reason, you are responsible to clear any contents on disk.

You should treat storing any element retrieved from a BigArrayList as if it were a copy-by-value.  The reason for this is because the content in a BigArrayList can be serialized and deserialized during any operation.  Therefore, upon deserialization, a new object is created.  Any old references in the program are now referencing a different object than what is being stored in the BigArrayList.  If you retrieve an element from a BigArrayList and change it, make sure to save it back to the list.

## How to Build
Import normally as a Gradle project which will handle all dependencies (current fast serialization library).  The SimpleTest.java file can be run as a standard Java application to test the build.
