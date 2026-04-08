package com.laytonsmith.core.asm;

import com.laytonsmith.core.asm.metadata.IRMetadata;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class IRBuilder {
	Set<LLVMFunction> functionsUsed = new HashSet<>();

	List<String> lines = new ArrayList<>();
	List<Target> targets = new ArrayList<>();

	List<String> metadata = new ArrayList<>();

	public IRBuilder() {

	}

	private int fillTarget = -1;

	public void appendLine(Target t, String line) {
		// This is a great place to put a breakpoint if you aren't sure where a line of IR is coming from.
		if(fillTarget >= 0) {
			lines.set(fillTarget, line);
			fillTarget = -1;
		} else {
			lines.add(line);
			targets.add(t);
		}
	}

	/**
	 * Reserves a line slot at the current position in the IR output. The slot contains a null
	 * placeholder and must be filled later via {@link #fillReservedLine(int, Runnable)}. This is
	 * used when a line (such as a conditional branch) cannot be fully constructed until blocks
	 * that follow it have been generated (and their label numbers are known).
	 * @param t The target for source location tracking.
	 * @return The index of the reserved slot, for use with {@link #fillReservedLine(int, Runnable)}.
	 */
	public int reserveLine(Target t) {
		int index = lines.size();
		lines.add(null);
		targets.add(t);
		return index;
	}

	/**
	 * Fills a previously reserved line slot by executing the given action. The action should
	 * call exactly one Gen method (e.g. {@code gen.br} or {@code gen.brCond}), which will
	 * write its output into the reserved slot instead of appending to the end.
	 * @param index The index returned by {@link #reserveLine(Target)}.
	 * @param action A runnable that calls a Gen method to produce the line.
	 */
	public void fillReservedLine(int index, Runnable action) {
		fillTarget = index;
		action.run();
		if(fillTarget >= 0) {
			throw new IllegalStateException(
					"fillReservedLine action did not produce any output");
		}
	}

	public void appendLines(Target t, String... lines) {
		for(String line : lines) {
			appendLine(t, line);
		}
	}

	/**
	 * Appends a label line (e.g. "42:") for the given label number.
	 * @param t The target for source location tracking.
	 * @param label The label number (as returned by {@link LLVMEnvironment#getGotoLabel()}).
	 */
	public void appendLabel(Target t, int label) {
		appendLine(t, label + ":");
	}

	public void appendLines(Target t, List<String> lines) {
		for(String line : lines) {
			appendLine(t, line);
		}
	}

	public String renderStartupCode(Environment env) {
		StringBuilder b = new StringBuilder();
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);

		IRBuilder newBuilder = new IRBuilder();

		for(LLVMFunction f : functionsUsed) {
			Target t = new Target(0, new File("/" + f.getName() + " startup code"), 0);
			f.addStartupCode(newBuilder, env, t);
		}

		return newBuilder.renderIR(env);
	}

	public String renderIR(Environment env) {
		StringBuilder b = new StringBuilder();
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		int padding = 0;
		if(llvmenv.isOutputIRCodeTargetLoggingEnabled()) {
			// Figure up the longest line, to determine padding.
			for(String line : lines) {
				padding = Math.max(padding, line.length());
			}
		}

		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			Target t = targets.get(i);
			b.append(AsmUtil.formatLine(t, llvmenv, line, padding + 2));
		}
		return b.toString();
	}

	public void setFinalMetadata(Environment env) {
		Set<IRMetadata> metadataRefs = env.getEnv(LLVMEnvironment.class).getMetadataRegistry().getAllMetadata();
		for(IRMetadata d : metadataRefs) {
			metadata.add(d.getDefinition());
		}
	}

	public Gen generator(Target t, Environment env) {
		return this.new Gen(t, env);
	}

	/**
	 * Integer comparison predicates for the icmp instruction.
	 */
	public enum ICmpPredicate {
		EQ("eq"),
		NE("ne"),
		/** Unsigned greater than */
		UGT("ugt"),
		/** Unsigned greater or equal */
		UGE("uge"),
		/** Unsigned less than */
		ULT("ult"),
		/** Unsigned less or equal */
		ULE("ule"),
		/** Signed greater than */
		SGT("sgt"),
		/** Signed greater or equal */
		SGE("sge"),
		/** Signed less than */
		SLT("slt"),
		/** Signed less or equal */
		SLE("sle");

		private final String ir;

		ICmpPredicate(String ir) {
			this.ir = ir;
		}

		@Override
		public String toString() {
			return ir;
		}
	}

	/**
	 * Floating point comparison predicates for the fcmp instruction.
	 */
	public enum FCmpPredicate {
		/** Ordered and equal */
		OEQ("oeq"),
		/** Ordered and greater than */
		OGT("ogt"),
		/** Ordered and greater or equal */
		OGE("oge"),
		/** Ordered and less than */
		OLT("olt"),
		/** Ordered and less or equal */
		OLE("ole"),
		/** Ordered and not equal */
		ONE("one"),
		/** Ordered (no NaNs) */
		ORD("ord"),
		/** Unordered (either NaN) */
		UNO("uno"),
		/** Unordered or equal */
		UEQ("ueq"),
		/** Unordered or greater than */
		UGT("ugt"),
		/** Unordered or greater or equal */
		UGE("uge"),
		/** Unordered or less than */
		ULT("ult"),
		/** Unordered or less or equal */
		ULE("ule"),
		/** Unordered or not equal */
		UNE("une");

		private final String ir;

		FCmpPredicate(String ir) {
			this.ir = ir;
		}

		@Override
		public String toString() {
			return ir;
		}
	}

	public class Gen {

		Target t;
		Environment env;
		LLVMEnvironment llvmenv;

		Gen(Target t, Environment env) {
			this.t = t;
			this.env = env;
			this.llvmenv = env.getEnv(LLVMEnvironment.class);
		}

		/**
		 * Creates an alloca statement.
		 * @param id The id
		 * @param type
		 */
		public void alloca(int id, IRType type) {
			// TODO: Look into the alignment
			IRBuilder.this.appendLine(t, "%" + id + " = alloca " + type.getIRType());
		}

		/**
		 * Used to store variables into an alloca. {@code into} is the identifier for the alloca statement,
		 * and type and id are what you're storing.
		 * @param type
		 * @param id
		 * @param into
		 */
		public void store(IRType type, int id, int into) {
			store(type, type.getIRType() + " %" + id, into);
		}

		/**
		 * Used to store a constant value into an alloca. {@code into} is the identifier for the alloca statement,
		 * and type and value are what you're storing. Note that the value can be anything, and is added as is
		 * to the statement.
		 * @param type
		 * @param value
		 * @param into
		 */
		public void store(IRType type, String value, int into) {
			IRBuilder.this.appendLine(t, "store " + value
					+ ", " + type.getIRType() + "* %" + into);
		}

		/**
		 * Combines an alloca and store into one call.
		 * @param allocaId The reference to the alloca call
		 * @param type The type of value
		 * @param storeId The value that will be stored in memory.
		 */
		public void allocaAndStore(int allocaId, IRType type, int storeId) {
			alloca(allocaId, type);
			store(type, storeId, allocaId);
		}

		/**
		 * Combines an alloca and store into one call, storing an arbitrary value.
		 * @param allocaId The reference to the alloca call
		 * @param type The type of value
		 * @param storeValue
		 */
		public void allocaAndStore(int allocaId, IRType type, String storeValue) {
			alloca(allocaId, type);
			store(type, storeValue, allocaId);
		}

		/**
		 * Creates a load statement.
		 * @param loadInto The reference to load into (That is, {@code x} in {@code %x = load...}).
		 * @param type The type of value
		 * @param allocaId The reference to the alloca call
		 */
		public void load(int loadInto, IRType type, int allocaId) {
			IRBuilder.this.appendLine(t, "%" + loadInto + " = load " + type.getIRType() + ", " + type.getIRType() + "* %" + allocaId);
		}

		/**
		 * Allocas, stores, and re-loads a value. The loaded id is returned, to make construction easier.
		 * @param allocaId The reference to use for the alloca call
		 * @param type The type of value
		 * @param storeId The reference to store.
		 * @param loadId The reference to use for the load call.
		 * @return
		 */
		public int allocaStoreAndLoad(int allocaId, IRType type, int storeId, int loadId) {
			allocaAndStore(allocaId, type, storeId);
			load(loadId, type, allocaId);
			return loadId;
		}

		/**
		 * Allocas, stores, and re-loads an arbitrary. The loaded id is returned, to make construction easier.
		 * @param allocaId The reference to use for the alloca call
		 * @param type The type of value
		 * @param storeValue The value to store.
		 * @param loadId The reference to use for the load call
		 * @return
		 */
		public int allocaStoreAndLoad(int allocaId, IRType type, String storeValue, int loadId) {
			allocaAndStore(allocaId, type, storeValue);
			load(loadId, type, allocaId);
			return loadId;
		}

		// --- Comparison instructions ---

		/**
		 * Integer comparison of two registers.
		 * @return The result variable (always i1).
		 */
		public int icmp(ICmpPredicate predicate, IRType operandType, int lhs, int rhs) {
			int result = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
			IRBuilder.this.appendLine(t, "%" + result + " = icmp " + predicate + " "
					+ operandType.getIRType() + " %" + lhs + ", %" + rhs);
			return result;
		}

		/**
		 * Integer comparison of a register against a constant.
		 * @return The result variable (always i1).
		 */
		public int icmp(ICmpPredicate predicate, IRType operandType, int lhs, String rhsConst) {
			int result = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
			IRBuilder.this.appendLine(t, "%" + result + " = icmp " + predicate + " "
					+ operandType.getIRType() + " %" + lhs + ", " + rhsConst);
			return result;
		}

		/**
		 * Floating point comparison of two registers.
		 * @return The result variable (always i1).
		 */
		public int fcmp(FCmpPredicate predicate, IRType operandType, int lhs, int rhs) {
			int result = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
			IRBuilder.this.appendLine(t, "%" + result + " = fcmp " + predicate + " "
					+ operandType.getIRType() + " %" + lhs + ", %" + rhs);
			return result;
		}

		/**
		 * Floating point comparison of a register against a constant.
		 * @return The result variable (always i1).
		 */
		public int fcmp(FCmpPredicate predicate, IRType operandType, int lhs, String rhsConst) {
			int result = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
			IRBuilder.this.appendLine(t, "%" + result + " = fcmp " + predicate + " "
					+ operandType.getIRType() + " %" + lhs + ", " + rhsConst);
			return result;
		}

		// --- Logical instructions ---

		/**
		 * Bitwise AND of two registers.
		 * @return The result variable.
		 */
		public int and(IRType type, int lhs, int rhs) {
			int result = llvmenv.getNewLocalVariableReference(type);
			IRBuilder.this.appendLine(t, "%" + result + " = and " + type.getIRType()
					+ " %" + lhs + ", %" + rhs);
			return result;
		}

		/**
		 * Bitwise OR of two registers.
		 * @return The result variable.
		 */
		public int or(IRType type, int lhs, int rhs) {
			int result = llvmenv.getNewLocalVariableReference(type);
			IRBuilder.this.appendLine(t, "%" + result + " = or " + type.getIRType()
					+ " %" + lhs + ", %" + rhs);
			return result;
		}

		// --- Select instruction ---

		/**
		 * Select between two register values based on an i1 condition.
		 * @return The result variable.
		 */
		public int select(int condition, IRType valueType, int trueVal, int falseVal) {
			int result = llvmenv.getNewLocalVariableReference(valueType);
			IRBuilder.this.appendLine(t, "%" + result + " = select i1 %" + condition
					+ ", " + valueType.getIRType() + " %" + trueVal
					+ ", " + valueType.getIRType() + " %" + falseVal);
			return result;
		}

		/**
		 * Select between two arbitrary value references based on an i1 condition.
		 * The references are inserted as-is after the type prefix.
		 * @return The result variable.
		 */
		public int select(int condition, IRType valueType, String trueRef, String falseRef) {
			int result = llvmenv.getNewLocalVariableReference(valueType);
			IRBuilder.this.appendLine(t, "%" + result + " = select i1 %" + condition
					+ ", " + valueType.getIRType() + " " + trueRef
					+ ", " + valueType.getIRType() + " " + falseRef);
			return result;
		}

		// --- Cast instructions ---

		// --- Branch instructions ---

		/**
		 * Unconditional branch to a label.
		 * @param label The label to branch to.
		 */
		public void br(int label) {
			IRBuilder.this.appendLine(t, "br label %" + label);
		}

		/**
		 * Conditional branch based on an i1 condition.
		 * @param condition The i1 condition variable.
		 * @param trueLabel The label to branch to if true.
		 * @param falseLabel The label to branch to if false.
		 */
		public void brCond(int condition, int trueLabel, int falseLabel) {
			IRBuilder.this.appendLine(t, "br i1 %" + condition
					+ ", label %" + trueLabel + ", label %" + falseLabel);
		}

		// --- Cast instructions ---

		/**
		 * Bitcast between types of the same bit width.
		 * @return The result variable.
		 */
		public int bitcast(IRType fromType, int value, IRType toType) {
			int result = llvmenv.getNewLocalVariableReference(toType);
			IRBuilder.this.appendLine(t, "%" + result + " = bitcast "
					+ fromType.getIRType() + " %" + value + " to " + toType.getIRType());
			return result;
		}

		/**
		 * Signed integer to floating point conversion.
		 * @return The result variable.
		 */
		public int sitofp(IRType fromType, int value, IRType toType) {
			int result = llvmenv.getNewLocalVariableReference(toType);
			IRBuilder.this.appendLine(t, "%" + result + " = sitofp "
					+ fromType.getIRType() + " %" + value + " to " + toType.getIRType());
			return result;
		}

		/**
		 * Floating point extension to a wider float type.
		 * @return The result variable.
		 */
		public int fpext(IRType fromType, int value, IRType toType) {
			int result = llvmenv.getNewLocalVariableReference(toType);
			IRBuilder.this.appendLine(t, "%" + result + " = fpext "
					+ fromType.getIRType() + " %" + value + " to " + toType.getIRType());
			return result;
		}

		/**
		 * Integer to pointer conversion.
		 * @return The result variable.
		 */
		public int inttoptr(IRType fromType, int value, IRType toType) {
			int result = llvmenv.getNewLocalVariableReference(toType);
			IRBuilder.this.appendLine(t, "%" + result + " = inttoptr "
					+ fromType.getIRType() + " %" + value + " to " + toType.getIRType());
			return result;
		}

		// --- Aggregate instructions ---

		/**
		 * Extract a field from an aggregate (struct) value.
		 * @return The result variable.
		 */
		public int extractvalue(IRType structType, int structVar, int index, IRType resultType) {
			int result = llvmenv.getNewLocalVariableReference(resultType);
			IRBuilder.this.appendLine(t, "%" + result + " = extractvalue "
					+ structType.getIRType() + " %" + structVar + ", " + index);
			return result;
		}

		// --- ms_value tag category checks ---

		/**
		 * Checks whether an ms_value tag variable belongs to the given category.
		 * Emits one icmp per boxable IRType in that category, ORed together.
		 * @param category The category to check for.
		 * @param tagVar The i8 tag variable to test.
		 * @return An i1 result variable that is true if the tag matches.
		 */
		private int isTagCategory(IRType.Category category, int tagVar) {
			int result = -1;
			for(IRType type : IRType.values()) {
				if(type.isBoxable() && type.getCategory() == category) {
					int cmp = icmp(ICmpPredicate.EQ, IRType.INTEGER8,
							tagVar, type.getBoxTagString());
					if(result == -1) {
						result = cmp;
					} else {
						result = or(IRType.INTEGER1, result, cmp);
					}
				}
			}
			if(result == -1) {
				throw new IllegalArgumentException(
						"No boxable types found for category: " + category);
			}
			return result;
		}

		/**
		 * Checks whether an ms_value tag represents a float type.
		 * @param tagVar The i8 tag variable to test.
		 * @return An i1 result variable.
		 */
		public int isFloatTag(int tagVar) {
			return isTagCategory(IRType.Category.FLOAT, tagVar);
		}

		/**
		 * Checks whether an ms_value tag represents a string type.
		 * @param tagVar The i8 tag variable to test.
		 * @return An i1 result variable.
		 */
		public int isStringTag(int tagVar) {
			return isTagCategory(IRType.Category.POINTER, tagVar);
		}

		/**
		 * Checks whether an ms_value tag represents a boolean (i1) type.
		 * @param tagVar The i8 tag variable to test.
		 * @return An i1 result variable.
		 */
		public int isBoolTag(int tagVar) {
			return icmp(ICmpPredicate.EQ, IRType.INTEGER8,
					tagVar, IRType.INTEGER1.getBoxTagString());
		}

		/**
		 * Checks whether an ms_value tag represents null.
		 * @param tagVar The i8 tag variable to test.
		 * @return An i1 result variable.
		 */
		public int isNullTag(int tagVar) {
			return icmp(ICmpPredicate.EQ, IRType.INTEGER8,
					tagVar, IRType.MS_NULL.getBoxTagString());
		}

	}
}
