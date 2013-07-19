
package com.laytonsmith.PureUtilities.ClassMirror;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This class gathers information about a class, without actually loading 
 * the class into memory. Most of the methods in {@link java.lang.Class} are
 * available in this class (or have an equivalent Mirror version).
 */
public class ClassMirror {
	
	public static enum TestEnum {
		ONE, TWO;
	}
	
	@TestAnnotation(theValue = "Test Value Class")
	@Deprecated
	public static class TestClass extends ClassInfo implements Cloneable, TestInterface {
		public int myPublicInt;
		private int myPrivateInt;
		private static int myPrivateStaticInt;
		String myString;
		private static int [] my1DArray;
		private static final int [][] my2DFinalArray;
		public static final double myDouble = 8.0;
		public static final long myLong = 9001;
		private static final float myFloat = 4.0F;
		private static final float NaN = Float.NaN;
		private static final float posInf = Float.POSITIVE_INFINITY;
		private static final float negInf = Float.NEGATIVE_INFINITY;
		private static final int myInt = 1;
		static {
			System.out.println("Static initialization block");
			my2DFinalArray = new int[0][0];
		}
		public TestClass(String s){
		}
		
		@TestAnnotation(theValue = "Test Value Method")
		public void myMethod(){
			
		}
		
	}
	
	public interface TestInterface {
		void myMethod();
	}
	
	public static @interface TestAnnotation{
		String theValue();
	}
	
	public static void main(String[] argv) throws Exception {
		File f = new File("target/classes/" + TestClass.class.getName().replace(".", "/") + ".class");
		ClassMirror cr = new ClassMirror(f);
		System.out.println("Modifiers: " + cr.getModifiers().toString());
		System.out.println("JVM Name: " + cr.getJVMClassName());
		System.out.println("Class Name: " + cr.getClassName());
	}
	
	/////*************************** END TEST STUFF
	
	ClassInfo info = new ClassInfo();
	org.objectweb.asm.ClassReader reader;
	public ClassMirror(InputStream is) throws IOException {
		reader = new org.objectweb.asm.ClassReader(is);
		parse();
	}
	
	public ClassMirror(File file) throws FileNotFoundException, IOException {
		this(new FileInputStream(file));
	}
	
	private void parse(){
		reader.accept(info, org.objectweb.asm.ClassReader.SKIP_CODE 
				| org.objectweb.asm.ClassReader.SKIP_DEBUG 
				| org.objectweb.asm.ClassReader.SKIP_FRAMES);
	}
	
	/**
	 * Returns the modifiers on this class.
	 * @return 
	 */
	public ModifierMirror getModifiers(){
		return info.modifiers;
	}
	
	/**
	 * Returns the name of this class as recognized by the JVM, not the
	 * common class name. Use {@link #getClassName()} instead, if you want
	 * the common name.
	 * @return 
	 */
	public String getJVMClassName(){
		return info.name;
	}
	
	public String getClassName(){
		return info.name.replaceAll("[/$]", ".");
	}
	
	public boolean isEnum(){
		return info.isEnum;
	}
	
	public boolean isInterface(){
		return info.isInterface;
	}
	
	private static class ClassInfo implements ClassVisitor {
		
		public int version;
		public int access;
		public ModifierMirror modifiers;
		public String name;
		public String signature;
		public String superClass;
		public String[] interfaces;
		public List<String> classAnnotations = new ArrayList<String>();
		public List<Boolean> classAnnotationVisibility = new ArrayList<Boolean>();
		public boolean isInterface = false;
		public boolean isEnum = false;
		public ClassReferenceMirror classReferenceMirror;

		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			if((access & Opcodes.ACC_ENUM) > 0){
				isEnum = true;
			}
			if((access & Opcodes.ACC_INTERFACE) > 0){
				isInterface = true;
			}
			this.version = version;
			this.access = access;
			this.modifiers = new ModifierMirror(ModifierMirror.Type.CLASS, access);
			this.name = name;
			this.classReferenceMirror = new ClassReferenceMirror(name);
			this.signature = signature;
			this.superClass = superName;
			this.interfaces = interfaces;
		}

		public void visitSource(String source, String debug) {
			//Ignored
		}

		public void visitOuterClass(String owner, String name, String desc) {
			//Ignored
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			System.out.println("annotation desc: " + desc);
			System.out.println("annotation visible: " + visible);
			return null;
		}

		public void visitAttribute(Attribute attr) {
			System.out.println("attribute: " + attr.toString());
		}

		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			
		}

		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			System.out.println("Field:");
			System.out.println("modifiers: " + new ModifierMirror(ModifierMirror.Type.FIELD, access).toString());
			System.out.println("name: " + name);
			System.out.println("desc: " + desc);
			System.out.println("signature: " + signature);
			System.out.println("value: " + (value == null?"null":"(" + value.getClass() + ") " + value));
			return null;
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return null;
		}

		public void visitEnd() {
			
		}
		
	}
	
}
