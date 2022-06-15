package com.laytonsmith.PureUtilities.ClassLoading;

/**
 *
 */
public abstract class DynamicEnum<Abstracted extends Enum, Concrete> {

	protected Abstracted abstracted; // field name reflectively accessed
	protected Concrete concrete;

	public DynamicEnum(Abstracted abstracted, Concrete concrete) {
		this.abstracted = abstracted;
		this.concrete = concrete;
	}

	public abstract String name();

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
