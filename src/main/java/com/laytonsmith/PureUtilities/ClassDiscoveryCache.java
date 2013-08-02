
package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.ClassMirror.AnnotationMirror;
import com.laytonsmith.PureUtilities.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassMirror.ClassReferenceMirror;
import com.laytonsmith.PureUtilities.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.ClassMirror.MethodMirror;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This file represents a jar, and can tell you what annotations
 * are available on each class, method, and field. This class has methods
 * to serialize and deserialize from json, a descriptor, which can be used to
 * rebuild this class with.
 */
public class ClassDiscoveryCache {
	private final List<ClassMirror> list;
	private final URL url;

	public ClassDiscoveryCache(URL url){
		this.url = url;
		list = new ArrayList<ClassMirror>();
		ClassDiscovery discovery = new ClassDiscovery();
		discovery.addDiscoveryLocation(url);
		for(ClassMirror m : discovery.getKnownClasses(url)){
			list.add(m);
		}
	}
	
	public ClassDiscoveryCache(URL url, InputStream descriptor) throws IOException{
		this.url = url;
		List<ClassMirror> _list;
		ObjectInputStream ois = new ObjectInputStream(descriptor);
		try {
			_list = (List<ClassMirror>) ois.readObject();
		} catch(ClassNotFoundException ex){
			//We can recover from this one, but it won't be instant.
			_list = new ClassDiscoveryCache(url).list;
		}
		this.list = _list;
	}
	
	public URL getURL(){
		return url;
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
//	private Map<AnnotationMirror, List<ClassMirror>> classes;
//	private Map<AnnotationMirror, List<MethodMirror>> methods;
//	private Map<AnnotationMirror, List<FieldMirror>> fields;
//	private Map<ClassMirror, List<ClassReferenceMirror>> extensions;
//
//	/**
//	 * Creates a new ClassDiscoveryCache with the specified ClassDiscovery instance.
//	 * The URL will be added to the instance. If the ClassDiscovery instance already has
//	 * a cache for this url, it is replaced with this one.
//	 * @param url
//	 * @param discovery 
//	 */
//	public ClassDiscoveryCache(URL url, ClassDiscovery discovery){
//		classes = new HashMap<AnnotationMirror, List<ClassMirror>>();
//		methods = new HashMap<AnnotationMirror, List<MethodMirror>>();
//		fields = new HashMap<AnnotationMirror, List<FieldMirror>>();
//		extensions = new HashMap<ClassMirror, List<ClassReferenceMirror>>();
//		discovery.removeCacheForURL(url);
//		discovery.addDiscoveryLocation(url);
//		for(ClassMirror c : discovery.getKnownClasses(url)){
//			for (Iterator<AnnotationMirror> it = c.getAnnotations().iterator(); it.hasNext();) {
//				AnnotationMirror cAnnotation = it.next();
//				if(!classes.containsKey(cAnnotation)){
//					classes.put(cAnnotation, new ArrayList<ClassMirror>());
//				}
//				classes.get(cAnnotation).add(c);
//			}
//			for(MethodMirror mm : c.getMethods()){
//				for(AnnotationMirror m : mm.getAnnotations()){
//					if(!methods.containsKey(m)){
//						methods.put(m, new ArrayList<MethodMirror>());
//					}
//					methods.get(m).add(mm);
//				}
//			}
//			for(FieldMirror f : c.getFields()){
//				for(AnnotationMirror m : f.getAnnotations()){
//					if(!fields.containsKey(m)){
//						fields.put(m, new ArrayList<FieldMirror>());
//					}
//					fields.get(m).add(f);
//				}
//			}
//			List<ClassReferenceMirror> supers = new ArrayList<ClassReferenceMirror>();
//			supers.add(c.getSuperClass());
//			supers.addAll(c.getInterfaces());
//			extensions.put(c, supers);
//		}
//		discovery.addCacheForURL(url, this);
//	}
//	
//	/**
//	 * Given the string returned by writeDescriptor, creates a new ClassDiscoveryCache.
//	 * It is added to the ClassDiscovery instance provided.
//	 * @param url
//	 * @param descriptor 
//	 * @param discovery 
//	 * @throws java.io.IOException 
//	 */
//	public ClassDiscoveryCache(URL url, InputStream descriptor, ClassDiscovery discovery) throws IOException{
//		ObjectInputStream in = new ObjectInputStream(descriptor);
//		try {
//			classes = (Map<AnnotationMirror, List<ClassMirror>>) in.readObject();
//			extensions = (Map<ClassMirror, List<ClassReferenceMirror>>) in.readObject();
//			fields = (Map<AnnotationMirror, List<FieldMirror>>) in.readObject();
//			methods = (Map<AnnotationMirror, List<MethodMirror>>) in.readObject();
//		} catch (ClassNotFoundException ex) {
//			//We can recover from this one.
//			ClassDiscoveryCache cd = new ClassDiscoveryCache(url, discovery);
//			//That just added it, so let's remove it back
//			discovery.removeCacheForURL(url);
//			//Now assign what it found
//			classes = cd.classes;
//			extensions = cd.extensions;
//			fields = cd.fields;
//			methods = cd.methods;
//		}
//		discovery.addCacheForURL(url, this);
//	}
//	
//	/**
//	 * Writes to an output stream which may be written out to file system, which can be
//	 * used with the {@link #ClassDiscoveryCache(java.net.URL, java.io.InputStream, com.laytonsmith.PureUtilities.ClassDiscovery) } constructor
//	 * to immediately create a new instance.
//	 * @param out
//	 * @throws java.io.IOException
//	 */
//	public void writeDescriptor(OutputStream out) throws IOException{
//		ObjectOutputStream output = new ObjectOutputStream(out);
//		output.writeObject(classes);
//		output.writeObject(extensions);
//		output.writeObject(fields);
//		output.writeObject(methods);
//	}
//
//	@Override
//	public String toString() {
//		return "[ClassDiscoveryCache: classes: " + classes.size() + "; extensions: " + extensions.size() 
//				+ "; fields: " + fields.size() + "; methods: " + methods.size() + ";]";
//	}
	
}
