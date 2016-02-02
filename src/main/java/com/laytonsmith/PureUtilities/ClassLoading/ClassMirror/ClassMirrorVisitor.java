package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.laytonsmith.PureUtilities.Common.StringUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class ClassMirrorVisitor extends ClassVisitor {

    private final ClassMirror.ClassInfo classInfo;

    ClassMirrorVisitor(ClassMirror.ClassInfo info) {
        super(Opcodes.ASM5);
        this.classInfo = info;
    }

    public ClassMirrorVisitor() {
        this(new ClassMirror.ClassInfo());
    }

    public ClassMirror<?> getMirror(URL source) {
        Preconditions.checkState(done, "Not done visiting %s", classInfo.name == null ? "none" : classInfo.name);
        return new ClassMirror<Object>(this.classInfo, source);
    }

    private boolean done;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        Preconditions.checkState(!done, "Can't visit %s, because we already visited %s", name, classInfo.name); // Ensure we never visit more than one class
        if ((access & ACC_ENUM) == ACC_ENUM) classInfo.isEnum = true;
        if ((access & ACC_INTERFACE) == ACC_INTERFACE) classInfo.isInterface = true;
        classInfo.modifiers = new ModifierMirror(ModifierMirror.Type.CLASS, access);
        classInfo.name = name;
        classInfo.classReferenceMirror = new ClassReferenceMirror(Type.getObjectType(name).getDescriptor());
        classInfo.superClass = superName;
        classInfo.interfaces = interfaces;
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
                value
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
        if (STATIC_INITIALIZER_PATTERN.matcher(name).matches()) return null; // Ignore static initializers
		if(ConstructorMirror.INIT.matches(name)){
			// We want to replace the V at the end with the parent class type.
			// Yes, technically a constructor really does return void, but.. not really.
			desc = StringUtils.replaceLast(desc, "V", classInfo.classReferenceMirror.getJVMName());
		}
        List<ClassReferenceMirror> parameterMirrors = new ArrayList<>();
        for (Type type : Type.getArgumentTypes(desc)) {
            parameterMirrors.add(new ClassReferenceMirror(type.getDescriptor()));
        }
        AbstractMethodMirror _methodMirror;
		if(ConstructorMirror.INIT.equals(name)){
			_methodMirror = new ConstructorMirror(
					classInfo.classReferenceMirror, 
					new ModifierMirror(ModifierMirror.Type.METHOD, access),
					new ClassReferenceMirror(Type.getReturnType(desc).getDescriptor()), 
					name, 
					parameterMirrors, 
					(access & ACC_VARARGS) == ACC_VARARGS,
					(access & ACC_SYNTHETIC) == ACC_SYNTHETIC
			);
		} else {
			_methodMirror = new MethodMirror(
                classInfo.classReferenceMirror,
                    new ModifierMirror(ModifierMirror.Type.METHOD, access),
                new ClassReferenceMirror(Type.getReturnType(desc).getDescriptor()),
                name,
                parameterMirrors,
                (access & ACC_VARARGS) == ACC_VARARGS,
                (access & ACC_SYNTHETIC) == ACC_SYNTHETIC
			);
		}
		final AbstractMethodMirror methodMirror = _methodMirror;
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
            if (value instanceof Type) value = ((Type) value).getDescriptor();
            mirror.addAnnotationValue(name, value);
            super.visit(name, value);
        }
    }
}
