
package com.laytonsmith.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CoreProfile {
	
	public static void main(String[] args){
		long start = System.currentTimeMillis();
		List<Long> array = new ArrayList<Long>();
		for(long i = 0; i < 100000; i++){
			array.add(i);
			System.out.println(i);
		}
		long finish = System.currentTimeMillis();
		System.out.println(finish - start + "ms");
		
		/*
		 Equivalent mscript:
		 
		 @start = time()
		 @array = array()
		 for(1..1000000, @i,
		 	@array[] = @i
			sys_out(@i)
		 )
		 @finish = time()
		 sys_out(@finish - @start . 'ms')
		 */
	}
	
}
