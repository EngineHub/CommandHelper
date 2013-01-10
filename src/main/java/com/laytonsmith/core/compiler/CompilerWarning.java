package com.laytonsmith.core.compiler;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;

/**
 * This class enumerates all the possible compiler warnings, and
 * are used to during compilation to trigger or not trigger
 * certain types of warnings, based on the file options set.
 * @author lsmith
 */
public enum CompilerWarning implements Documentation {
	Deprecated("Features that are deprecated are going to be removed at some point in future releases,"
			+ " so as soon as possible, you should remove the usage of the code that is triggering this."
			+ " It is not recommended to supress this warning, since it works as a constant"
			+ " reminder that in some future upgrade, your code WILL break.", CHVersion.V3_3_1),
	UnreachableCode("If code is unreachable, that is, some function above it will cause the code"
			+ " to halt or otherwise return before it could run, for example: die() msg('unreachable'),"
			+ " this warning is triggered."
			+ " Usually this is a sign of debug code, and isn't a big deal, but if it makes it into"
			+ " production code, it may be a sign of accidental debug code left in, and so a warning is"
			+ " triggered. Regardless, the code under the terminal statement is always removed from"
			+ " the compiled version, so there is no performance hit for leaving it in.", CHVersion.V3_3_1),
	UnassignedVariableUsage("In strict mode, this is actually an error, and will halt compilation, but outside"
			+ " of strict mode, it will still always trigger a warning. This happens if you use a variable,"
			+ " but it is not guaranteed to have been assigned something yet. Even if you do an"
			+ " assignment, if it is in a conditional branch, it is not considered to be assigned, unless it is"
			+ " assigned to something in ALL the conditional branches.", CHVersion.V3_3_1),
	AssignmentToItself("If a variable is assigned to itself, this is a useless call: @a = @a. This is"
			+ " more than likely a sign of a mispelled variable, or some other accident"
			+ " so a warning is triggered.", CHVersion.V3_3_1),
	VariableBreak("If a break() contains a variable as the parameter, it is almost never correct. That is because"
			+ " the number of breakable statements that surround it is unlikely to be dynamic (and if they are,"
			+ " this is also a sign of bad programming). Consider the following pseudo code: for(...){ for(...){ break(@x) }}."
			+ " If @x is greater than 2, it will cause a runtime error, but since the break is not hardcoded, the compiler"
			+ " does not have a way of detecting this at compile time. While it still is possible to tightly control"
			+ " the value of @x such that it would never break out more than 2 loops, this is still an indication of"
			+ " improper coding, because each loop likely has a specific purpose, and dynamically breaking out of"
			+ " a certain number of loops would be confusing.", CHVersion.V3_3_1),
	AssignmentInIf("If you are doing a variable assignment inside an if statement, it is likely"
			+ " an accident. Usually, if(@a = @b){ ... } was intended to be"
			+ " if(@a == @b){ ... }. If it truly was not an error, it is better"
			+ " to use @a = @b if(@a){ ... } instead, which will remove this warning, yet"
			+ " still have the same functionality.", CHVersion.V3_3_1), //TODO: Implement this
	UseOfEval("Eval is almost never the solution. Sometimes this is necessary, but only in a very"
			+ " select few cases, such as writing a custom interpreter mode, or something that is"
			+ " intended as a developer debug tool. Otherwise, this likely indicates extremely"
			+ " bad coding. If eval is used at all, this warning is triggered.", CHVersion.V3_3_1), //TODO: Implement this
	StrictModeOff("Strict mode is HIGHLY recommended, so strict mode not being enabled is itself a warning.", CHVersion.V3_3_1), //TODO: Implement this
	AmbiguousUnaryOperators("In the case where prefix/postfix unary operators are used, if not delimited"
			+ " correctly, they can look ambiguous. Consider the code: @a ++ @b. While this code has a"
			+ " well defined execution (sconcat(postinc(@a), @b)) when written this way, it is difficult"
			+ " to read without specifically knowing the order of operation rules. Instead, this code should"
			+ " be written as (@a++) @b. The only case where it is acceptable to leave off the parenthesis"
			+ " is when the identifier is the sole argument in a function call, for instance in a for loop,"
			+ " for(@i = 0, @i < @x, @i++, ...). This is not ambiguous at all, since it is the only"
			+ " operation in that statement. Using the functional notation will not trigger this warning ever,"
			+ " because there is implicit parenthesis usage when not using operators.", CHVersion.V3_3_1), //TODO: Implement this
	MagicNumber("Other than when doing a variable assignments, and a few other occasions,"
			+ " raw numbers can often times be confusing. For"
			+ " instance, in code, if you see @i < 5, what is 5? Without knowing more about the surrounding"
			+ " code, it could be confusing. Instead, consider assigning 5 to a variable. This gives it a"
			+ " name/description, which is useful when you or another programmer look back on the code. This also"
			+ " makes it easier to change, in the event that it is used in multiple places.", CHVersion.V3_3_1), //TODO: Implement this
	ExecutableCodeInInclude("Other than include() and proc(), nothing else should be contained in an include, as"
			+ " this will be removed later. It is a sign of bad programming, because there is no particular"
			+ " guarantee that the code will be run once or multiple times. Only having procs and includes inside"
			+ " an included file thereby guarantees that regardless of how many times it runs, the code will not"
			+ " have different results. Eventually, this will be an error, so you should take measures to reorganize"
			+ " your code to prepare for this eventuality.", CHVersion.V3_3_1), //TODO: Implement this
	BareStrings("If strict mode is on, using bare strings is an error, not a warning, but even in non-strict mode,"
			+ " it is a warning. This is triggered if any string is not quoted. So, the following, while valid,"
			+ " would trigger the warning: if(this string == @thatString){ ... }.", CHVersion.V3_3_1), //TODO: Implement this
	UnquotedSymbols("If using bare strings, unquoted symbols that do not yet have a special meaning are extra evil, because"
			+ " they may be added as a symbol in the future, silently breaking your code.", CHVersion.V3_3_1), //TODO: Implement this
	SupressedWarnings("If a file has supressed any warnings, this warning will be triggered (unless it itself is supressed)."
			+ " There are rarely any cases to leaving in warnings in production code, but during development, it is often times"
			+ " useful to break a few rules to more quickly develop the software, then come back later and clean it up. Since"
			+ " this warning is only triggered once per file, you can safely supress all other warnings except this one during"
			+ " development, and still have a \"note\" to come back to this later.", CHVersion.V3_3_1), //TODO: Implement this
	;
	
	private final String docs;
	private final CHVersion since;
	private CompilerWarning(String docs, CHVersion since){
		this.docs = docs;
		this.since = since;
	}

	public String getName() {
		return name();
	}

	public String docs() {
		return docs;
	}

	public CHVersion since() {
		return since;
	}
}
