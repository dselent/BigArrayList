import java.io.IOException;
import java.util.ArrayList;

import com.dselent.bigarraylist.BigArrayList;

public class ReferenceTest
{
	  public static void main(String args[]) throws IOException
	  {
	    //create a BigArrayList of Longs
	    //cache block size = 1 million
	    //cache blocks = 4
		BigArrayList<A> bal = new BigArrayList<A>(1000000, 4);
		//ArrayList<A> bal = new ArrayList<A>();
	    ArrayList<A> al = new ArrayList<A>();
				
	    //add 10 million elements
	    for(long i=0; i<10000000; i++) 
	    {
	    	A a = new A();
	    	B b = new B();
	    	b.setData(i);
	    	a.setB(b);
	    	bal.add(a);
	    	al.add(a);
	    }
	    
	    System.gc();
				
	    al.get(5).getB().setData(9999);
	    
	    //get the element at index 5
	    System.out.println(bal.get(5));
				
	    //set the element at index 5
	    //bal.set(5, 100l);
				
	    //get the element at index 5
	    System.out.println(al.get(5));
			
	    //clear contents on disk
	    bal.clearMemory();
	  }

}
