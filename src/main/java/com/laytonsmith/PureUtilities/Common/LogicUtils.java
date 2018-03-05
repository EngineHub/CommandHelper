package com.laytonsmith.PureUtilities.Common;

/**
 * Provides various functions for making some logic statements easier to read.
 *
 */
public class LogicUtils {

	public static Compare get(Object obj) {
		return new Compare(obj);
	}

	public static class Compare {

		Object obj;

		Compare(Object obj) {
			this.obj = obj;
		}

		/**
		 * Returns true if the element is equals to ANY value in the list. LogicUtils.get(a).equalsAny(b, c, d) is
		 * equivalent to a == b || a == c || a == d
		 *
		 * @param o
		 * @return
		 */
		public boolean equalsAny(Object... o) {
			if(obj == null) {
				for(Object oo : o) {
					if(oo == null) {
						return true;
					}
				}
				return false;
			}
			for(Object oo : o) {
				if(obj.equals(oo)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Returns true if the element is equal to NONE of the values on the list. LogicUtils.get(a).equalsNone(b, c, d)
		 * is equivalent to a != b && a != c && a != d
		 */
		public boolean equalsNone(Object... o) {
			if(obj == null) {
				for(Object oo : o) {
					if(oo == null) {
						return false;
					}
				}
				return true;
			}
			for(Object oo : o) {
				if(obj.equals(oo)) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Returns true if the elements are equal
		 *
		 * @param obj
		 * @return
		 */
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Compare)) {
				return false;
			}
			if(obj == null) {
				return obj == o;
			} else {
				return obj.equals(o);
			}
		}

		@Override
		public int hashCode() {
			if(obj == null) {
				return 0;
			} else {
				return obj.hashCode();
			}
		}

	}
}
