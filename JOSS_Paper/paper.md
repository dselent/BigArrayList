---
title: 'BigArrayList:  A Java API which Allows Programmers to Transparently Use Disk Space as Additional Memory'
tags:
  - BigArrayList
  - ArrayList
authors:
 - name: Douglas Selent
   orcid: 0000-0003-2322-6734
   affiliation: 1
affiliations:
 - name: Worcester Polytechnic Institute
   index: 1
date: 6 July 2017
bibliography: paper.bib
---

# Summary

A common problem many programmers face is running out of memory. The solution proposed in this paper discusses a Java library, which uses disk space to function as additional memory. The API mimics the functionality of the existing ArrayList class in Java; thus the data structure in this paper is called BigArrayList. The main goal of the BigArrayList API is to be a generic and easy-to-use solution for programs that require more memory than what is available.

The major research application is to facilitate operations on data sets that are too large for standard analysis tools. Tools such as Excel of SPSS do not support large data sets and specialized software or niche programming languages can be time consuming to learn and use. A BigArrayList can be used similar to the existing ArrayList class to easily perform operations on large data sets. It has been used to merge school data on the percent of students who have free or reduced lunch cost (an indicator of family wealth status) with the ASSISTments 2012-2013 data set [@AssistmentsData2014, @AssistmentsDataScoolLunch2015].  Enabling the use of this feature  for predicting various dependent measures such as student performance and student affect. This task would have otherwise been a difficult and time consuming task without using the BigArrayList library.

BigArrayList can be used to create programs to perform the necessary data operations, without having to worry about the size of the data similar to the R package “bigmemory” [@bigmemory2013]. Bigmemory provides capabilities for working with data sets that are too big to fit into RAM by creating a file on disk to act like additional RAM. Currently bigmemory is specific to the R language and cannot be used on the Windows operating system. BigArrayList is specific to the Java language, a popular general purpose object-oriented programming language, which can be run on any operating system.

Hadoop is also another solution intended for use on large amounts of data, however Hadoop is meant for working with data at a much larger scale, which can be distributed across several computers in a computing cluster using MapReduce technology [@hadoop2010]. BigArrayList is meant to work for the average user on a single standard home computer. It is meant to work with data on the order of several hundred gigabytes and not petabytes across hundreds of computers. BigArrayList is also an easy-to-use Java API where Hadoop is an entire system, with a much steeper learning curve.
Currently some Java solutions exist to map memory to disk. One solution is the MappedByteBuffer class, which has the ability to read and write to memory-mapped files [@mappedbytebuffer2016]. BigArrayList can be used at a higher level, just like an ArrayList. All file I/O is handled automatically for the programmer. A MappedByteBuffer is merely a class to perform the byte I/O to the memory-mapped file, leaving it up to the programmer to provide all the logic in order to use it. A BigArrayList is also able to handle all serializable objects automatically, where a MappedByteBuffer must know the size of the objects in order to work correctly when reading and writing to random access files.

Ehcache is another solution that attempts to use disk space as additional memory [@ehcache2013]. Ehcache requires all objects to be of the type “Element” and cannot store duplicate values. BigArrayList performs faster than Ehcache because no overhead time is spent converting all objects to Elements. Exact performance comparisons could not be made because of errors with the Ehcache library, which could not support basic tests.

BigArrayList combines the functionality of the existing ArrayList class with a cache-like data structure to swap data between memory and disk. To support the main goal (ease-of-use) of this API, all the public functions of a BigArrayList have signatures that are identical to the existing ArrayList function signatures, reducing the learning curve for using this library. All of the disk-to-memory mapping is done internally and is transparent to the programmer.

A BigArrayList is internally made up of an ArrayList of ArrayList objects, which represents the number of cache blocks and the elements in each cache block. The size of the outer ArrayList is equal to the number of cache blocks, where each element represents a cache block. The size of the inner ArrayList is equal to the size of a cache block, where each element is an element of data in the cache block. This is the largest amount of data that can be held in memory at a given time. All other data is stored on disk and swapped into memory when needed.

BigArrayList uses an LRU cache replacement policy to determine which block of data should be swapped out of memory and written to disk. Data is only written to disk if it has changed since it was last read in from disk. All files written to disk are stored in one folder, which can be specified by the programmer.

The BigArrayList API supports three types of serialization (Object, Fast Serialization, and Memory-mapped). The Java language has the built in ability to serialize objects and read and write them to disk. This will be referred to as Object I/O. Fast Serialization (FST)  is an improved version of Object I/O [@fastserialization2017]. FST is faster by registering objects with a serializer and not supporting object versioning.  The following standard list operations are supported: add (to the end of the list), set, get, remove, size, and sort.  Objects being stored must implement the serializable interface.

The image below shows an example of how a BigArrayList maintains a mapping between elements in memory and elements on disk. In this example the BigArrayList is currently holding one-hundred-million elements. The BigArrayList structure consists of four cache blocks, containing ten-million elements each.  The blocks of elements in memory (left-side) are mapped (middle) to files on disk (right-side).

![Image of Cache mapping](https://github.com/dselent/BigArrayList/blob/master/JOSS_Paper/cachemapping.png)

# References
