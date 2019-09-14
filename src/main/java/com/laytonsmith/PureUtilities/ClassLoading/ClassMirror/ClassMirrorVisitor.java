package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.laytonsmith.PureUtilities.Common.StringUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ACC_VARARGS;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

public class ClassMirrorVisitor extends ClassVisitor {

	private final ClassMirror.ClassInfo<Object> classInfo;

	ClassMirrorVisitor(ClassMirror.ClassInfo<Object> info) {
		super(Opcodes.ASM5);
		this.classInfo = info;
	}

	public ClassMirrorVisitor() {
		this(new ClassMirror.ClassInfo<Object>());
	}

	public ClassMirror<?> getMirror(URL source) {
		if(!done) {
			throw new IllegalStateException(String.format(
					"Not done visiting %s", classInfo.name == null ? "none" : classInfo.name
			));
		}
		return new ClassMirror<>(this.classInfo, source);
	}

	private boolean done;

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if(done) {
			// Ensure we never visit more than one class
			throw new IllegalStateException(String.format(
					"Can't visit %s, because we already visited %s", name, classInfo.name
			));
		}
		if((access & ACC_ENUM) == ACC_ENUM) {
			classInfo.isEnum = true;
		}
		if((access & ACC_INTERFACE) == ACC_INTERFACE) {
			classInfo.isInterface = true;
		}
		classInfo.modifiers = new ModifierMirror(ModifierMirror.Type.CLASS, access);
		classInfo.name = name;
		classInfo.classReferenceMirror = new ClassReferenceMirror(Type.getObjectType(name).getDescriptor());
		classInfo.superClass = superName;
		classInfo.interfaces = interfaces;
		if(signature != null) {
			boolean inGeneric = false;
			ClassReferenceMirror<Object> top = null;
			StringBuilder buffer = new StringBuilder();
			for(char c : signature.toCharArray()) {
				if(c == '<') {
					inGeneric = true;
					top = new ClassReferenceMirror<>(buffer.toString() + ";");
					classInfo.genericParameters.put(top, new ArrayList<>());
					buffer = new StringBuilder();
					continue;
				}
				if(c == '>') {
					inGeneric = false;
					top = null;
					buffer = new StringBuilder();
					continue;
				}
				if(c == ';') {
					if(buffer.toString().isEmpty()) {
						// this happens at Lclass<Lwhatever;>; because ; always follows >
						continue;
					}
					if(inGeneric) {
						classInfo.genericParameters
								.get(top).add(new ClassReferenceMirror<>(buffer.toString() + ";"));
						buffer = new StringBuilder();
					} else {
						classInfo.genericParameters
								.put(new ClassReferenceMirror<>(buffer.toString() + ";"), new ArrayList<>());
					}
					continue;
				}
				buffer.append(c);
			}
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		final AnnotationMirror mirror = new AnnotationMirror(new ClassReferenceMirror(desc), visible);
		return new AnnotationMirrorVisitor(super.visitAnnotation(desc, visible), mirror) {
			@Override
			public void visitEnd() {
				classInfo.annotations.add(mirror);
				super.visitEnd();
			}
		};
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		final FieldMirror fieldMirror = new FieldMirror(
				classInfo.classReferenceMirror,
				new ModifierMirror(ModifierMirror.Type.FIELD, access),
				new ClassReferenceMirror(desc),
				name,
				value,
				signature
		);
		return new FieldVisitor(ASM5, super.visitField(access, name, desc, signature, value)) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				final AnnotationMirror annotationMirror = new AnnotationMirror(new ClassReferenceMirror(desc), visible);
				return new AnnotationMirrorVisitor(super.visitAnnotation(desc, visible), annotationMirror) {
					@Override
					public void visitEnd() {
						fieldMirror.addAnnotation(annotationMirror);
						super.visitEnd();
					}
				};
			}

			@Override
			public void visitEnd() {
				classInfo.fields.add(fieldMirror);
				super.visitEnd();
			}
		};
	}

	private static final Pattern STATIC_INITIALIZER_PATTERN = Pattern.compile("<clinit>");

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if(STATIC_INITIALIZER_PATTERN.matcher(name).matches()) {
			return null; // Ignore static initializers
		}
		if(ConstructorMirror.INIT.matches(name)) {
			// We want to replace the V at the end with the parent class type.
			// Yes, technically a constructor really does return void, but.. not really.
			desc = StringUtils.replaceLast(desc, "V", classInfo.classReferenceMirror.getJVMName());
		}
		List<ClassReferenceMirror> parameterMirrors = new ArrayList<>();
		for(Type type : Type.getArgumentTypes(desc)) {
			parameterMirrors.add(new ClassReferenceMirror(type.getDescriptor()));
		}
		final AbstractMethodMirror methodMirror;
		if(ConstructorMirror.INIT.equals(name)) {
			methodMirror = new ConstructorMirror(
					classInfo.classReferenceMirror,
					new ModifierMirror(ModifierMirror.Type.METHOD, access),
					new ClassReferenceMirror(Type.getReturnType(desc).getDescriptor()),
					name,
					parameterMirrors,
					(access & ACC_VARARGS) == ACC_VARARGS,
					(access & ACC_SYNTHETIC) == ACC_SYNTHETIC,
					signature
			);
		} else {
			methodMirror = new MethodMirror(
					classInfo.classReferenceMirror,
					new ModifierMirror(ModifierMirror.Type.METHOD, access),
					new ClassReferenceMirror(Type.getReturnType(desc).getDescriptor()),
					name,
					parameterMirrors,
					(access & ACC_VARARGS) == ACC_VARARGS,
					(access & ACC_SYNTHETIC) == ACC_SYNTHETIC,
					signature
			);
		}

		return new MethodVisitor(ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				final AnnotationMirror annotationMirror = new AnnotationMirror(new ClassReferenceMirror<>(desc), visible);
				return new AnnotationMirrorVisitor(super.visitAnnotation(desc, visible), annotationMirror) {
					@Override
					public void visitEnd() {
						methodMirror.addAnnotation(annotationMirror);
					}
				};
			}

			@Override
			public void visitLineNumber(int line, Label start) {
				// Each line of code will be visited here. We only want the lowest line number though, since that's
				// the closest to the method declaration line, which is what we're really after.
				int lowest = methodMirror.getLineNumber();
				if(lowest == 0) {
					methodMirror.setLineNumber(line);
				} else {
					methodMirror.setLineNumber(Math.min(lowest, line));
				}
			}

			@Override
			public void visitEnd() {
				classInfo.methods.add(methodMirror);
				super.visitEnd();
			}
		};
	}

	@Override
	public void visitEnd() {
		this.done = true;
		super.visitEnd();
	}

	private static class AnnotationMirrorVisitor extends AnnotationVisitor {

		private final AnnotationMirror mirror;

		public AnnotationMirrorVisitor(AnnotationVisitor next, AnnotationMirror mirror) {
			super(ASM5, next);
			this.mirror = mirror;
		}

		@Override
		public void visit(String name, Object value) {
			if(value instanceof Type) {
				value = ((Type) value).getClassName();
			}
			mirror.addAnnotationValue(name, value);
			super.visit(name, value);
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			return new ArrayAnnotationVisitor(name, mirror);
		}

	}

	private static class ArrayAnnotationVisitor extends AnnotationVisitor {
		private final AnnotationMirror mirror;
		private final List<Object> types = new ArrayList<>();
		private final String name;
		private Class<?> type;

		public ArrayAnnotationVisitor(String name, AnnotationMirror mirror) {
			super(ASM5);
			this.name = name;
			this.mirror = mirror;
		}



		@Override
		public void visit(String name, Object value) {
			type = value.getClass();
			if(value instanceof Type) {
				value = ((Type) value).getClassName();
				type = String.class;
			}
			types.add(value);
			super.visit(name, value);
		}

		@Override
		public void visitEnd() {
			// The underlying type is necessarily null if we did not get any values. This should still be ok,
			// but code needs to check for this value in array types.
			Object array = null;
			if(type != null) {
				array = ArrayUtils.cast(types.toArray(new Object[types.size()]),
					ClassUtils.getArrayClassFromType(type));
			}
			mirror.addAnnotationValue(name, array);
			super.visitEnd();
		}


	}
}
