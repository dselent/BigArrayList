import java.io.IOException;

import com.dselent.bigarraylist.BigArrayList;


public class SimpleExample
{
  public static void main(String args[]) throws IOException
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



