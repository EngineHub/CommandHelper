package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.List;

/**
 * A keyword is a value in code that knows how to transform certain code into other formats, at a lexer/compiler level.
 * During lexing, if a keyword is registered, the compiler will mark it as such (as compared to a bare string) and then
 * during the compilation stage, once the keyword is reached in the stream, the stream (and keyword location) will be
 * passed to the keyword handler for processing. In order for keywords to be dynamically introduced, the keyword
 * implementation must be tagged with the keyword annotation.
 */
public abstract class Keyword implements Documentation {

    private static final String __CBRACE__ = new com.laytonsmith.core.functions.Compiler.__cbrace__().getName();

    protected Keyword() {
	//
    }

    /**
     * Sent upon reaching a keyword in the parse tree. The full list of arguments at the current stack depth, as well as
     * the keyword position will be sent, and the keyword is allowed to make any necessary changes, including throwing a
     * {@link ConfigCompileException} if necessary.
     *
     * @param list The argument list at the current depth as it currently exists. Note that the list will have already
     * been lightly processed.
     * @param keywordPosition The keyword position
     * @return The position at which the compiler should continue processing from. Often times this will just be
     * {@code keywordPosition}, but may be different if need be.
     * @throws ConfigCompileException If the tree is in an invalid state, and the keyword needs to cause an exception to
     * be thrown.
     */
    public abstract int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException;

    /**
     * Returns the keyword name, or null, if this class isn't tagged with the @keyword annotation.
     *
     * @return
     */
    public final String getKeywordName() {
	keyword k = this.getClass().getAnnotation(keyword.class);
	return k == null ? null : k.value();
    }

	/**
	 * Convenience function to allow keywords to more easily check if this is a valid code block. If not, a
	 * {@link ConfigCompileException} is thrown for you. A code block is considered valid if it has is a cbrace device,
	 * and has 0 or 1 arguments. The message is what should be shown if the node is not a cbrace device at all. If the
	 * argument count is wrong, this function creates the error message.
	 *
	 * @param node The node to check
	 * @param message The message to display if the node is not a cbrace device.
	 * @throws ConfigCompileException
	 */
	protected void validateCodeBlock(ParseTree node, String message) throws ConfigCompileException {
		// Note: If any of these checks are changed, the isValidCodeBlock(ParseTree node) method has to be updated too
		// to keep it in sync.
		if(node.getChildren().size() > 1) {
			throw new ConfigCompileException("Unexpected number of arguments in code block", node.getTarget());
		}
		if(!isCodeBlock(node)) {
			throw new ConfigCompileException(message, node.getTarget());
		}
	}

	/**
	 * Returns true if {@link #validateCodeBlock(com.laytonsmith.core.ParseTree, java.lang.String)} would not cause an
	 * exception to be thrown. This is useful for conditionally doing something with the keyword if a code block exists,
	 * which is often the case for functions that are also keywords.
	 *
	 * @param node
	 * @return
	 */
	protected boolean isValidCodeBlock(ParseTree node) {
		return node.getChildren().size() <= 1 && isCodeBlock(node);
	}

    /**
     * Returns true if the node is a code block or not. The argument count to the block is not considered.
     *
     * @param node
     * @return
     */
    protected static boolean isCodeBlock(ParseTree node) {
	return node.getData() instanceof CFunction && node.getData().val().equals(__CBRACE__);
    }

    /**
     * Returns a CNull, if the node is empty, or the first argument to the node
     *
     * @param node
     * @return
     */
    protected static ParseTree getArgumentOrNull(ParseTree node) {
	if (node.getChildren().isEmpty()) {
	    return new ParseTree(CNull.NULL, node.getFileOptions());
	} else {
	    return node.getChildAt(0);
	}
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface keyword {

	/**
	 * The name of the keyword.
	 *
	 * @return
	 */
	String value();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Documentation>[] seeAlso() {
	return new Class[]{};
    }

    @Override
    public URL getSourceJar() {
	return ClassDiscovery.GetClassContainer(this.getClass());
    }

    @Override
    public String getName() {
	return this.getClass().getAnnotation(keyword.class).value();
    }


}
