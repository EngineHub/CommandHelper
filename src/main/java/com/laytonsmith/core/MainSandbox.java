package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ExhaustiveVisitor;

/**
 * This class is for testing concepts
 */
public class MainSandbox {

    public static interface UserID {}
    public static class PhoneNumber implements UserID {}
    public static abstract class GeneratedID implements UserID {}
    public static class GeneratedIDV1 extends GeneratedID {}
    public static class GeneratedIDV2 extends GeneratedID {}

    //@ExhaustiveVisitor.VisitorInfo(directSubclassOnly = false)
    public static class CanadianVisitor extends ExhaustiveVisitor<UserID> {
	public void visit(PhoneNumber n) {
	    System.out.println("Canadian PhoneNumber");
	}

	public void visit(GeneratedIDV1 id) {
	    System.out.println("Canadian GeneratedIDV1");
	}

	public void visit(GeneratedIDV2 id) {
	    System.out.println("Canadian GeneratedIDV2");
	}

    }

    //@ExhaustiveVisitor.VisitorInfo(directSubclassOnly = false)
    public static class AmericanVisitor extends ExhaustiveVisitor<UserID> {
	public void visit(PhoneNumber n) {
	    System.out.println("American PhoneNumber");
	}

	public void visit(GeneratedIDV1 id) {
	    System.out.println("American GeneratedIDV1");
	}

	public void visit(GeneratedIDV2 id) {
	    System.out.println("American GeneratedIDV1");
	}
    }

    public static void main(String[] argv) throws Exception {
	UserID id = new GeneratedIDV2();
	new CanadianVisitor().visit(id);
    }

}
