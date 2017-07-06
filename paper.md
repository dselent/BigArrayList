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

A BigArrayList is a Java library that allows programmers to easily store and operate on data that is too large to store in memory. It has the same function signatures as the existing ArrayList class and automatically handles all I/O operations to reduce the learning curve for programmers. The BigArrayList data structure maps a group of ArrayList objects stored in memory, to a set of files on disk. This allows programmers to work with larger amounts of data without the need to create their own I/O mechanism. The goal of this library is to provide a generic and easy-to-use solution that automatically uses disk space as extra memory for data that is too large to store in memory. A common use for this library is for operating on large data sets such as [ASSISTments data sets](https://sites.google.com/site/assistmentsdata/home), which the library was originally used for.
DOI <insert DOI here>.

# References
