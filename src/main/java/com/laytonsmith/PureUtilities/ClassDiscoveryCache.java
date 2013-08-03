
package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.ClassMirror.ClassMirror;
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
public class ClassDiscoveryCache {
	private final List<ClassMirror<?>> list;

	public ClassDiscoveryCache(URL url){
		list = new ArrayList<ClassMirror<?>>();
		ClassDiscovery discovery = new ClassDiscovery();
		discovery.addDiscoveryLocation(url);
		for(ClassMirror m : discovery.getKnownClasses(url)){
			list.add(m);
		}
	}
	
	/**
	 * Creates a new ClassDiscoveryCache object from a descriptor that was
	 * created earlier with writeDescriptor. The url may be null, but if
	 * provided, will be used as a fallback in case an error occurs
	 * with the descriptor.
	 * @param url
	 * @param descriptor
	 * @throws IOException 
	 */
	public ClassDiscoveryCache(URL url, InputStream descriptor) throws IOException, ClassNotFoundException{
		List<ClassMirror<?>> _list;
		ObjectInputStream ois = new ObjectInputStream(descriptor);
		try {
			_list = (List<ClassMirror<?>>) ois.readObject();
		} catch(ClassNotFoundException ex){
			if(url != null){
				//We can recover from this one, but it won't be instant.
				_list = new ClassDiscoveryCache(url).list;
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
		return "[" + ClassDiscoveryCache.class.getSimpleName() + ": " + list.size() + "]";
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
