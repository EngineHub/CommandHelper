package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.Keyword;

/**
 * A handler that changes synchronized(arg) { code } format to synchronized(arg, code).
 *
 * @author Pieter12345
 */
@Keyword.keyword("synchronized")
public class SynchronizedKeyword extends SimpleBlockKeywordFunction {

    @Override
    protected Integer[] getFunctionArgumentCount() {
	return new Integer[]{1};
    }

    @Override
    public String docs() {
	return "Defines a synchronization block";
    }

    @Override
    public Version since() {
	return CHVersion.V3_3_2;
    }

}
