
package com.laytonsmith.PureUtilities.ClassLoading;

import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ProgressIterator;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This file represents a jar, and can tell you what annotations
 * are available on each class, method, and field. This class has methods
 * to serialize and deserialize from json, a descriptor, which can be used to
 * rebuild this class with.
 */
public class ClassDiscoveryURLCache {
	
	private final List<ClassMirror<?>> list;

	/**
	 * Creates a new ClassDiscoveryURLCache. This operation may
	 * take a long time, depending on the size of the url that needs scanning.
	 * @param url 
	 */
	public ClassDiscoveryURLCache(URL url){
		this(url, (ProgressIterator)null);
	}
	
	/**
	 * Creates a new ClassDiscoveryURLCache. This operation
	 * may take a long time, depending on the size of the url that needs scanning.
	 * The ProgressIterator can be null, but if provided is passed into the 
	 * internal ClassDiscovery object that is used.
	 * @param url
	 * @param progress 
	 */
	public ClassDiscoveryURLCache(URL url, ProgressIterator progress){
		list = new ArrayList<ClassMirror<?>>();
		ClassDiscovery discovery = new ClassDiscovery();
		discovery.setProgressIterator(progress);
		//Double check to ensure that this is null, otherwise
		//we would get stuck in an infinite loop.
		discovery.setClassDiscoveryCache(null);
		discovery.addDiscoveryLocation(url);
		for(ClassMirror m : discovery.getKnownClasses(url)){
			list.add(m);
		}
	}
	
	/**
	 * Creates a new ClassDiscoveryURLCache object from a descriptor that was
	 * created earlier with writeDescriptor. The url may be null, but if
	 * provided, will be used as a fallback in case an error occurs
	 * with the descriptor.
	 * @param url
	 * @param descriptor
	 * @throws IOException 
	 */
	public ClassDiscoveryURLCache(URL url, InputStream descriptor) throws IOException, ClassNotFoundException{
		List<ClassMirror<?>> _list;
		ObjectInputStream ois = new ObjectInputStream(descriptor);
		try {
			_list = (List<ClassMirror<?>>) ois.readObject();
		} catch(ClassNotFoundException ex){
			if(url != null){
				//We can recover from this one, but it won't be instant.
				_list = new ClassDiscoveryURLCache(url).list;
			} else {
				throw ex;
			}
		}
		ois.close();
		this.list = _list;
	}
	
	public void writeDescriptor(OutputStream out) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(list);
		oos.close();
	}
	
	@Override
	public String toString(){
		return "[" + ClassDiscoveryURLCache.class.getSimpleName() + ": " + list.size() + "]";
	}
	
	/**
	 * Package private, no copy is made.
	 * @return
	 */
	/* package */ List<ClassMirror<?>> getClasses(){
		return list;
	}
	
	/**
	 * Returns the classes in this cache.
	 * @return 
	 */
	public List<ClassMirror> getClassList(){
		return new ArrayList<ClassMirror>(list);
	}
	
}
