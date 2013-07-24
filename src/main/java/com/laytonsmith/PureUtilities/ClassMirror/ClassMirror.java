
package com.laytonsmith.PureUtilities.ClassMirror;

import com.laytonsmith.PureUtilities.ClassUtils;
import com.laytonsmith.PureUtilities.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
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
		@Deprecated
		@TestAnnotation(theValue = "The \"field\" annotation value")
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
		
		private String[] aaaa(String[] one, int[][] a, String two, int... three){
			return new String[]{};
		}
		
		@TestAnnotation(theValue = "Test Value Method")
		public void myMethod(){
			
		}
		
		public void myMethodP(String s){
			
		}
		
	}
	
	public interface TestInterface {
		void myMethod();
	}
	
	public interface TestInterface2 extends TestInterface {
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface TestAnnotation{
		String theValue();
		String unused() default "default";
	}
	
	public static void main(String[] argv) throws Exception {
		File f = new File("target/classes/" + TestClass.class.getName().replace(".", "/") + ".class");
		ClassMirror cr = new ClassMirror(f);
		System.out.println("Modifiers: " + cr.getModifiers().toString());
		System.out.println("JVM Name: " + cr.getJVMClassName());
		System.out.println("Class Name: " + cr.getClassName());
		System.out.println("Super class: " + cr.getSuperClass());
		System.out.println("Interfaces: " + cr.getInterfaces());
		System.out.println("Has annotation TestAnnotation: " + cr.hasAnnotation(TestAnnotation.class));
		System.out.println("Is annotation visible? " + cr.isAnnotationVisible(TestAnnotation.class));
		System.out.println("Fields: " + StringUtils.Join(cr.getFields(), "\n"));
		System.out.println("Field my2DFinalArray: " + cr.getField("my2DFinalArray").getAnnotation(TestAnnotation.class).getDefinedValuesWithDefault());
		System.out.println("Class annotation TestAnnotation.unused: " + cr.loadAnnotation(TestAnnotation.class).unused());
		System.out.println("Methods: " + Arrays.toString(cr.getMethods()));
		System.out.println("myMethod @TestAnnotation.theValue: " + cr.getMethod("myMethod").loadAnnotation(TestAnnotation.class).theValue());
		System.out.println("Does it extend from ClassInfo? " + cr.directlyExtendsFrom(ClassInfo.class));
		System.out.println("Does it extend from TestInterface? " + cr.directlyExtendsFrom(TestInterface.class));
		System.out.println("Does it extend from Serializable? " + cr.directlyExtendsFrom(java.io.Serializable.class));
		System.out.println("Does it extend from Object? " + cr.directlyExtendsFrom(Object.class));
		
		File f2 = new File("target/classes/" + TestInterface2.class.getName().replace(".", "/") + ".class");
		ClassMirror cr2 = new ClassMirror(f2);
		System.out.println("TestInterface2 super classes: " + cr2.getSuperClass());
		System.out.println("TestInterface2 interfaces: " + cr2.getInterfaces());
	}
	
	/////*************************** END TEST STUFF
	
	ClassInfo info = new ClassInfo();
	org.objectweb.asm.ClassReader reader;
	
	/**
	 * Creates a ClassMirror object for a given input stream representing
	 * a class file.
	 * @param is
	 * @throws IOException 
	 */
	public ClassMirror(InputStream is) throws IOException {
		reader = new org.objectweb.asm.ClassReader(is);
		parse();
	}
	
	/**
	 * Creates a ClassMirror object for a given class file.
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
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
	
	public ClassReferenceMirror getSuperClass(){
		return new ClassReferenceMirror("L" + info.superClass + ";");
	}
	
	public List<ClassReferenceMirror> getInterfaces(){
		List<ClassReferenceMirror> l = new ArrayList<ClassReferenceMirror>();
		for(String inter : info.interfaces){
			l.add(new ClassReferenceMirror("L" + inter + ";"));
		}
		return l;
	}
	
	/**
	 * Returns true if this class contains the annotation specified. 
	 * TODO: Note that to get the annotation information, at this point it is
	 * required to actually load the class.
	 * @param annotation
	 * @return 
	 */
	public boolean hasAnnotation(Class<? extends Annotation> annotation){
		String name = ClassUtils.getJVMName(annotation);
		for(AnnotationMirror a : info.annotations){
			if(a.getType().getJVMName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Because ClassMirror works with annotations that were declared as
	 * either {@link RetentionPolicy#CLASS} or {@link RetentionPolicy#RUNTIME},
	 * you may also want to check visibility. If this returns false, then the
	 * class does have the annotation, but {@link Class#getAnnotation(java.lang.Class)}
	 * would return false. If the class doesn't have the annotation, null is returned.
	 * @param annotation
	 * @return 
	 */
	public Boolean isAnnotationVisible(Class<? extends Annotation> annotation){
		String name = ClassUtils.getJVMName(annotation);
		for(AnnotationMirror a : info.annotations){
			if(a.getType().getJVMName().equals(name)){
				return a.isVisible();
			}
		}
		return null;
	}
	
	/**
	 * Returns the annotation defined on this class.
	 * @param clazz
	 * @return 
	 */
	public AnnotationMirror getAnnotation(Class<? extends Annotation> clazz){
		String name = ClassUtils.getJVMName(clazz);
		for(AnnotationMirror a : info.annotations){
			if(a.getType().getJVMName().equals(name)){
				return a;
			}
		}
		return null;
	}
	
	/**
	 * Loads the corresponding Annotation type for this field
	 * or method. This actually loads the Annotation class into memory.
	 * This is equivalent to getAnnotation(type).getProxy(type), however
	 * this checks for null first, and returns null instead of causing a NPE.
	 * @param <T>
	 * @param type
	 * @return 
	 */
	public <T extends Annotation> T loadAnnotation(Class<T> type) {
		AnnotationMirror mirror = getAnnotation(type);
		if(mirror == null){
			return null;
		}
		return mirror.getProxy(type);
	}
	
	/**
	 * Returns the fields in this class. This works like 
	 * {@link Class#getDeclaredFields()}, as only the methods in
	 * this class are loaded.
	 * @return 
	 */
	public FieldMirror[] getFields(){
		return info.fields.toArray(new FieldMirror[info.fields.size()]);
	}
	
	/**
	 * Returns the field, given by name. This does not traverse the
	 * Object hierarchy, unlike {@link Class#getField(java.lang.String)}.
	 * @param name
	 * @return 
	 */
	public FieldMirror getField(String name) throws NoSuchFieldException {
		for(FieldMirror m : info.fields){
			if(m.getName().equals(name)){
				return m;
			}
		}
		throw new NoSuchFieldException("The field \"" + name + "\" was not found.");
	}
	
	/**
	 * Returns the methods in this class. This traverses the parent Object
	 * heirarchy if the methods are apart of the visible interface, as well as
	 * private methods in this class itself.
	 * @return 
	 */
	public MethodMirror[] getMethods(){
		return info.methods.toArray(new MethodMirror[info.methods.size()]);
	}
	
	/**
	 * Returns the method, given by name. This traverses the parent Object heirarchy
	 * if the methods are apart of the visible interface, as well as private methods
	 * in this class itself.
	 * @param name
	 * @param params
	 * @return 
	 */
	public MethodMirror getMethod(String name, Class...params) throws NoSuchMethodException{
		List<ClassReferenceMirror> crmParams = new ArrayList<ClassReferenceMirror>();
		for(Class p : params){
			crmParams.add(new ClassReferenceMirror(ClassUtils.getJVMName(p)));
		}
		for(MethodMirror m : getMethods()){
			if(m.getName().equals(name) && m.getParams().equals(crmParams)){
				return m;
			}
		}
		throw new NoSuchMethodException("No method matching the signature " + name + "(" + StringUtils.Join(crmParams, ", ") + ") was found.");
	}
	
	/**
	 * Loads the class into memory and returns the class object. For this
	 * call to succeed, the class must otherwise be on the class path. The standard
	 * class loader is used, and the class is initialized.
	 * @return 
	 */
	public Class<?> loadClass() throws ClassNotFoundException{
		return info.classReferenceMirror.loadClass();
	}
	
	/**
	 * Loads the class into memory and returns the class object. For this
	 * call to succeed, the classloader specified must be able to find the class.
	 * @param loader
	 * @return 
	 */
	public Class<?> loadClass(ClassLoader loader, boolean initialize) throws ClassNotFoundException{
		return info.classReferenceMirror.loadClass(loader, initialize);
	}
	
	/**
	 * Returns true if this class either extends or implements the class
	 * specified, or is the same as that class. Note that if it transiently
	 * extends from this class, it can't necessarily find that information without
	 * actually loading the intermediate class, so this is a less useful method
	 * than {@link Class#isAssignableFrom(java.lang.Class)}, however, in combination
	 * with a system that is aware of all classes in a class ecosystem, this can
	 * be used to piece together that information without actually loading the
	 * classes.
	 * @param superClass
	 * @return 
	 */
	public boolean directlyExtendsFrom(Class superClass){
		String name = superClass.getName().replace(".", "/");
		if(info.superClass.equals(name)){
			return true;
		}
		for(String in : info.interfaces){
			if(in.equals(name)){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return (isInterface()?
				"interface":
				(isEnum()?"enum":"class")) 
				+ " " + getClassName();
	}
	
	private static class ClassInfo implements ClassVisitor {
		
		public int version;
		public int access;
		public ModifierMirror modifiers;
		public String name;
		public String signature;
		public String superClass;
		public String[] interfaces;
		public List<AnnotationMirror> annotations = new ArrayList<AnnotationMirror>();
		public boolean isInterface = false;
		public boolean isEnum = false;
		public ClassReferenceMirror classReferenceMirror;
		public List<FieldMirror> fields = new ArrayList<FieldMirror>();
		public List<MethodMirror> methods = new ArrayList<MethodMirror>();

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
			//We know we aren't an array or a primitive, so we just add L...; to make
			//the binary name, which is what ClassReferenceMirror expects.
			this.classReferenceMirror = new ClassReferenceMirror("L" + name + ";");
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
			AnnotationMirror am = new AnnotationMirror(new ClassReferenceMirror(desc), visible);
			annotations.add(am);
			return new AnnotationV(am);
		}

		public void visitAttribute(Attribute attr) {
			
		}

		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			
		}

		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			final FieldMirror fm = new FieldMirror(new ModifierMirror(ModifierMirror.Type.FIELD, access),
					new ClassReferenceMirror(desc), name, value);
			fields.add(fm);
			return new FieldVisitor() {

				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					AnnotationMirror m = new AnnotationMirror(new ClassReferenceMirror(desc), visible);
					fm.addAnnotation(m);
					return new AnnotationV(m);
				}

				public void visitAttribute(Attribute attr) {
					
				}

				public void visitEnd() {
					
				}
			};
		}
		
		private static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile("^\\((.*)\\)(.*)$");

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if("<init>".equals(name) || "<clinit>".equals(name)){
				//For now, we aren't interested in constructors or static initializers
				return null;
			}
			
			Matcher m = METHOD_SIGNATURE_PATTERN.matcher(desc);
			if(!m.find()){
				//The desc type didn't match?
				throw new Error("No match found for " + desc);
			}
			String inner = m.group(1);
			String ret = m.group(2);
			List<ClassReferenceMirror> params = new ArrayList<ClassReferenceMirror>();
			//Parsing the params list is a bit more complicated than it should be.
			StringBuilder b = new StringBuilder();
			boolean inObject = false;
			for(char c : inner.toCharArray()){
				b.append(c);
				if(inObject){
					if(c == ';'){
						inObject = false;
						params.add(new ClassReferenceMirror(b.toString()));
						b = new StringBuilder();
					}
				} else {
					if(c == 'L'){
						inObject = true;
					} else if(c != '['){
						params.add(new ClassReferenceMirror(b.toString()));
						b = new StringBuilder();
					} //otherwise, it's an array, continue.
				}
			}
			final MethodMirror mm = new MethodMirror(classReferenceMirror, new ModifierMirror(ModifierMirror.Type.METHOD, access),
					new ClassReferenceMirror(ret), name, params, (access & Opcodes.ACC_VARARGS) > 0);
			methods.add(mm);
			return new MethodVisitor() {

				public AnnotationVisitor visitAnnotationDefault() {
					return null;
				}

				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					AnnotationMirror am = new AnnotationMirror(new ClassReferenceMirror(desc), visible);
					mm.addAnnotation(am);
					return new AnnotationV(am);
				}

				public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
					return null;
				}

				public void visitAttribute(Attribute attr) {
					
				}

				public void visitCode() {
					
				}

				public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
					
				}

				public void visitInsn(int opcode) {
					
				}

				public void visitIntInsn(int opcode, int operand) {
					
				}

				public void visitVarInsn(int opcode, int var) {
					
				}

				public void visitTypeInsn(int opcode, String type) {
					
				}

				public void visitFieldInsn(int opcode, String owner, String name, String desc) {
					
				}

				public void visitMethodInsn(int opcode, String owner, String name, String desc) {
					
				}

				public void visitJumpInsn(int opcode, Label label) {
					
				}

				public void visitLabel(Label label) {
					
				}

				public void visitLdcInsn(Object cst) {
					
				}

				public void visitIincInsn(int var, int increment) {
					
				}

				public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
					
				}

				public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
					
				}

				public void visitMultiANewArrayInsn(String desc, int dims) {
					
				}

				public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
					
				}

				public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
					
				}

				public void visitLineNumber(int line, Label start) {
					
				}

				public void visitMaxs(int maxStack, int maxLocals) {
					
				}

				public void visitEnd() {
					
				}
			};
		}

		public void visitEnd() {
			
		}
		
	}
	
	private static class AnnotationV implements AnnotationVisitor {
		
		private AnnotationMirror mirror;
		public AnnotationV(AnnotationMirror mirror){
			this.mirror = mirror;
		}

		public void visit(String name, Object value) {
			mirror.addAnnotationValue(name, value);
		}

		public void visitEnum(String name, String desc, String value) {
			
		}

		public AnnotationVisitor visitAnnotation(String name, String desc) {
			return null;
		}

		public AnnotationVisitor visitArray(String name) {
			return null;
		}

		public void visitEnd() {
			
		}
		
	}
	
}
