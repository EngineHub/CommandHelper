package com.laytonsmith.PureUtilities.ClassLoading;

/**
 *
 */
public abstract class DynamicEnum<Abstracted extends Enum, Concrete> {

	protected Abstracted abstracted;
	protected Concrete concrete;

	public DynamicEnum(Abstracted abstracted, Concrete concrete) {
		this.abstracted = abstracted;
		this.concrete = concrete;
	}

	public abstract String name();

	/**
	 * @return always returns the concrete name
	 */
	public abstract String concreteName();

	/**
	 * Override me in API
	 *
	 * @return
	 */
	public Abstracted getAbstracted() {
		return abstracted;
	}

	/**
	 * Override me in Implementation if needed
	 *
	 * @return
	 */
	public Concrete getConcrete() {
		return concrete;
	}

	@Override
	public String toString() {
		return name();
	}
}
