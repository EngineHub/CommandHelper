package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.ArrayHandling;
import com.laytonsmith.core.functions.BasicLogic;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.ObjectModifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

/**
 * A class that represents a dynamic array.
 *
 * For subclasses, the ArrayAccess methods are the most commonly overridden methods. There are several overloaded
 * methods in this class, you need only to override the non-final ones for the same effect.
 */
@typeof("ms.lang.array")
public class CArray extends Construct implements Iterable<Mixed>, Booleanish,
		com.laytonsmith.core.natives.interfaces.Iterable {

	public static final CClassType TYPE = CClassType.get(CArray.class);
	private boolean associativeMode = false;
	private long nextIndex = 0;
	private List<Mixed> array;
	private SortedMap<String, Mixed> associativeArray;
	private String mutVal;
	private CArray parent = null;
	private boolean valueDirty = true;

	public CArray(Target t) {
		this(t, 0, (Mixed[]) null);
	}

	public CArray(Target t, Mixed... items) {
		this(t, 0, items);
	}

	public CArray(Target t, int initialCapacity) {
		this(t, initialCapacity, (Mixed[]) null);
	}

	public CArray(Target t, Collection<Mixed> items) {
		this(t, 0, getArray(items));
	}

	public CArray(Target t, int initialCapacity, Collection<Mixed> items) {
		this(t, initialCapacity, getArray(items));
	}

	public CArray(Target t, int initialCapacity, Mixed... items) {
		super("{}", ConstructType.ARRAY, t);
		if(initialCapacity == -1) {
			associativeMode = true;
		} else if(items != null) {
			for(Mixed item : items) {
				if(item instanceof CEntry) {
					//it's an associative array
					associativeMode = true;
					break;
				}
			}
		}
		associativeArray = new TreeMap<>(comparator);
		array = associativeMode ? new ArrayList<>() : initialCapacity > 0 ? new ArrayList<>(initialCapacity) : items != null ? new ArrayList<>(items.length) : new ArrayList<>();
		if(associativeMode) {
			if(items != null) {
				for(Mixed item : items) {
					if(item instanceof CEntry) {
						associativeArray.put(normalizeConstruct(((CEntry) item).ckey), ((CEntry) item).construct);
					} else {
						int max = Integer.MIN_VALUE;
						for(String key : associativeArray.keySet()) {
							try {
								int i = Integer.parseInt(key);
								max = java.lang.Math.max(max, i);
							} catch (NumberFormatException e) {
							}
						}
						if(max == Integer.MIN_VALUE) {
							max = -1; //Special case, there are no integer indexes in here yet.
						}
						associativeArray.put(Integer.toString(max + 1), item);
						if(item.isInstanceOf(CArray.TYPE)) {
							((CArray) item).parent = this;
						}
					}
				}
			}
		} else {
			if(items != null) {
				for(Mixed item : items) {
					array.add(item);
					if(item.isInstanceOf(CArray.TYPE)) {
						((CArray) item).parent = this;
					}
				}
			}
			this.nextIndex = array.size();
		}
		setDirty();
	}

	/**
	 * Returns if this array is in associative mode or not.
	 *
	 * @return
	 */
	@Override
	public boolean isAssociative() {
		return associativeMode;
	}

	/**
	 * Returns the backing array.
	 *
	 * @return
	 */
	protected List<Mixed> getArray() {
		return array;
	}

	private static Mixed[] getArray(Collection<Mixed> items) {
		Mixed c[] = new Mixed[items.size()];
		int count = 0;
		for(Mixed cc : items) {
			c[count++] = cc;
		}
		return c;
	}

	/**
	 * Returns a List based on the array. This is only applicable if this is a normal array.
	 *
	 * @return
	 */
	public List<Mixed> asList() {
		if(inAssociativeMode()) {
			throw new RuntimeException("asList can only be called on a normal array");
		} else {
			return new ArrayList<>(array);
		}
	}

	/**
	 * Returns the backing associative array.
	 *
	 * @return
	 */
	protected SortedMap<String, Mixed> getAssociativeArray() {
		return associativeArray;
	}

	/**
	 * @return Whether or not this array is operating in associative mode
	 */
	public boolean inAssociativeMode() {
		return associativeMode;
	}

	/**
	 * Returns a new empty CArray that is in associative mode.
	 *
	 * @param t
	 * @return
	 */
	public static CArray GetAssociativeArray(Target t) {
		return new CArray(t, -1);
	}

	public static CArray GetAssociativeArray(Target t, Mixed[] args) {
		return new CArray(t, -1, args);
	}

	/**
	 * This must be called every time the underlying model is changed, which sets the toString value to dirty, which
	 * means that the value will be regenerated next time it is requested.
	 */
	private void setDirty() {
		if(valueDirty) {
			return; // All parents must be dirty too
		}
		setDirty(new HashSet<>());
	}

	private void setDirty(Set<CArray> dirtied) {
		if(dirtied.contains(this)) {
			return; //Recursive, so don't continue.
		}
		valueDirty = true;
		if(parent != null) {
			dirtied.add(this);
			parent.setDirty(dirtied);
		}
	}

	/**
	 * Reverses the array in place, if it is a normal array, otherwise, if associative, it throws an exception.
	 *
	 * @param t
	 */
	public void reverse(Target t) {
		if(!associativeMode) {
			Collections.reverse(array);
			setDirty();
		} else {
			throw new CRECastException("Cannot reverse an associative array.", t);
		}
	}

	/**
	 * Pushes a new Construct onto the end of the array.
	 *
	 * @param c
	 * @param t
	 */
	public final void push(Mixed c, Target t) {
		push(c, null, t);
	}

	/**
	 * Pushes a new Construct onto the end of the array. If the index is specified, this works like a "insert"
	 * operation, in that all values are shifted to the right, starting with the value at that index. If the array is
	 * associative though, you MUST send null, otherwise an {@link IllegalArgumentException} is thrown. Ideally, you
	 * should use {@link #set} anyways for an associative array.
	 *
	 * @param c The Construct to add to the array
	 * @throws IllegalArgumentException If index is not null, and this is an associative array.
	 * @throws IndexOutOfBoundsException If the index is not null, and the index specified is out of range.
	 */
	public void push(Mixed c, Integer index, Target t) throws IllegalArgumentException, IndexOutOfBoundsException {
		if(!associativeMode) {
			if(index != null) {
				array.add(index, c);
			} else {
				array.add(c);
			}
			nextIndex++;
		} else {
			if(index != null) {
				throw new IllegalArgumentException("Cannot insert into an associative array");
			}
			int max = 0;
			for(String key : associativeArray.keySet()) {
				try {
					int i = Integer.parseInt(key);
					max = java.lang.Math.max(max, i);
				} catch (NumberFormatException e) {
				}
			}
			if(c instanceof CEntry) {
				associativeArray.put(Integer.toString(max + 1), ((CEntry) c).construct());
			} else {
				associativeArray.put(Integer.toString(max + 1), c);
			}
		}
		if(c.isInstanceOf(CArray.TYPE)) {
			((CArray) c).parent = this;
		}
		setDirty();
	}

	/**
	 * Returns the key set for this array. If it's an associative array, it simply returns the key set of the map,
	 * otherwise it generates a set of CInts from 0 to size-1, and returns that.
	 *
	 * @return
	 */
	@Override
	public Set<Mixed> keySet() {
		Set<Mixed> set;
		if(!associativeMode) {
			set = new LinkedHashSet<>(array.size());
			for(int i = 0; i < array.size(); i++) {
				set.add(new CInt(i, Target.UNKNOWN));
			}
		} else {
			set = new LinkedHashSet<>(associativeArray.size());
			for(String key : associativeArray.keySet()) {
				set.add(new CString(key, Target.UNKNOWN));
			}
		}
		return set;
	}

	/**
	 * Returns the string based key set for this array. If it's an associative array, it simply returns the key set of
	 * the map, otherwise it generates a set from 0 to size-1, toStrings the integers, and returns that.
	 *
	 * @return
	 */
	public Set<String> stringKeySet() {
		if(!associativeMode) {
			Set<String> set = new LinkedHashSet<>(array.size());
			for(int i = 0; i < array.size(); i++) {
				set.add(Integer.toString(i));
			}
			return set;
		} else {
			return associativeArray.keySet();
		}
	}

	private void setAssociative() {
		associativeArray = new TreeMap<>(comparator);
		for(int i = 0; i < array.size(); i++) {
			associativeArray.put(Integer.toString(i), array.get(i));
		}
		associativeMode = true;
		array = null; // null out the original array container so it can be GC'd
	}

	/**
	 *
	 * @param index
	 * @param c
	 */
	public void set(Mixed index, Mixed c, Target t) {
		if(!associativeMode) {
			if(index instanceof CNull) {
				// Invalid normal array index
				setAssociative();
			} else {
				try {
					int indx = Static.getInt32(index, t);
					if(indx > nextIndex || indx < 0) {
						// Out of range
						setAssociative();
					} else if(indx == nextIndex) {
						this.push(c, t);
					} else {
						array.set(indx, c);
					}
				} catch (ConfigRuntimeException e) {
					// Not a number
					setAssociative();
				}
			}
		}
		if(associativeMode) {
			associativeArray.put(normalizeConstruct(index), c);
		}
		if(c.isInstanceOf(CArray.TYPE)) {
			((CArray) c).parent = this;
		}
		setDirty();
	}

	public final void set(int index, Mixed c, Target t) {
		this.set(new CInt(index, t), c, t);
	}

	/* Shortcuts */
	public final void set(String index, Mixed c, Target t) {
		set(new CString(index, t), c, t);
	}

	public final void set(String index, String value, Target t) {
		set(index, new CString(value, t), t);
	}

	public final void set(String index, String value) {
		set(index, value, Target.UNKNOWN);
	}

	@Override
	public Mixed get(Mixed index, Target t) {
		if(!associativeMode) {
			try {
				return array.get(Static.getInt32(index, t));
			} catch (IndexOutOfBoundsException e) {
				throw new CREIndexOverflowException("The element at index \"" + index.val() + "\" does not exist", t, e);
			}
		} else {
			Mixed val = associativeArray.get(normalizeConstruct(index));
			if(val != null) {
				if(val instanceof CEntry) {
					return ((CEntry) val).construct();
				}
				return val;
			} else {
				//Create this so we can at least attach a stacktrace.
				@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
				IndexOutOfBoundsException ioobe = new IndexOutOfBoundsException();
				throw new CREIndexOverflowException("The element at index \"" + index.val() + "\" does not exist", t, ioobe);
			}
		}
	}

	public final Mixed get(long index, Target t) {
		return this.get(new CInt(index, t), t);
	}

	@Override
	public final Mixed get(int index, Target t) {
		return this.get(new CInt(index, t), t);
	}

	@Override
	public final Mixed get(String index, Target t) {
		return this.get(new CString(index, t), t);
	}

	public boolean containsKey(String c) {
		if(associativeMode) {
			return associativeArray.containsKey(c);
		} else {
			try {
				return Integer.valueOf(c) < array.size();
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	public final boolean containsKey(int i) {
		return this.containsKey(Integer.toString(i));
	}

	public boolean contains(Mixed c) {
		if(associativeMode) {
			return associativeArray.containsValue(c);
		} else {
			return array.contains(c);
		}
	}

	public final boolean contains(String c) {
		return contains(new CString(c, Target.UNKNOWN));
	}

	public final boolean contains(int i) {
		return contains(new CString(Integer.toString(i), Target.UNKNOWN));
	}

	/**
	 * Returns an array of the keys of all the values that are equal to the value specified
	 *
	 * @param value
	 * @return
	 */
	public CArray indexesOf(Mixed value) {
		CArray ret = new CArray(Target.UNKNOWN);
		if(associativeMode) {
			for(String key : associativeArray.keySet()) {
				if(BasicLogic.equals.doEquals(associativeArray.get(key), value)) {
					ret.push(new CString(key, Target.UNKNOWN), Target.UNKNOWN);
				}
			}
		} else {
			for(int i = 0; i < array.size(); i++) {
				if(BasicLogic.equals.doEquals(array.get(i), value)) {
					ret.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN);
				}
			}
		}
		return ret;
	}

	@Override
	public String val() {
		if(valueDirty) {
			getString(new Stack<>(), this.getTarget());
		}
		return mutVal;
	}

	@Override
	public String toString() {
		return val();
	}

	/**
	 * Returns a string version of this array. The arrays that have been accounted for so far are stored in arrays, to
	 * prevent recursion. Subclasses may override this method if a more efficient or concise string can be generated.
	 *
	 * @param arrays The values accounted for so far
	 * @param t
	 * @return
	 */
	protected String getString(Stack<CArray> arrays, Target t) {
		if(!valueDirty) {
			return mutVal;
		}
		StringBuilder b = new StringBuilder();
		b.append("{");
		if(!inAssociativeMode()) {
			for(int i = 0; i < this.size(); i++) {
				Mixed value = this.get(i, t);
				String v;
				if(value.isInstanceOf(CArray.TYPE)) {
					if(arrays.contains(value)) {
						//Check for recursion
						v = "*recursion*";
					} else {
						arrays.add(((CArray) value));
						v = ((CArray) value).getString(arrays, t);
						arrays.pop();
					}
				} else {
					v = value.val();
				}
				if(i > 0) {
					b.append(", ");
				}
				b.append(v);
			}
		} else {
			boolean first = true;
			for(String key : this.stringKeySet()) {
				if(!first) {
					b.append(", ");
				}
				first = false;
				String v;
				if(this.get(key, t) == null) {
					v = "null";
				} else {
					Mixed value = this.get(key, t);
					if(value.isInstanceOf(CArray.TYPE)) {
						if(arrays.contains(value)) {
							v = "*recursion*";
						} else {
							arrays.add(((CArray) value));
							v = ((CArray) value).getString(arrays, t);
						}
					} else {
						v = value.val();
					}
				}
				b.append(key).append(": ").append(v);
			}
		}
		b.append("}");
		mutVal = b.toString();
		valueDirty = false;
		return mutVal;
	}

	@Override
	public long size() {
		if(associativeMode) {
			return associativeArray.size();
		} else {
			return array.size();
		}
	}

	@Override
	public CArray clone() {
		CArray clone;
		try {
			clone = (CArray) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
		clone.associativeMode = associativeMode;
		if(!associativeMode) {
			if(array != null) {
				clone.array = new ArrayList<>(this.array);
			}
		} else if(associativeArray != null) {
			clone.associativeArray = new TreeMap<>(this.associativeArray);
		}
		clone.setDirty();
		return clone;
	}

	public CArray deepClone(Target t) {
		return deepClone(this, t, new ArrayList<>());
	}

	private static CArray deepClone(CArray array, Target t, ArrayList<CArray[]> cloneRefs) {

		// Return the clone reference if this array has been cloned before (both clones will have the same reference).
		for(CArray[] refCouple : cloneRefs) {
			if(refCouple[0] == array) {
				return refCouple[1];
			}
		}

		// Create the clone to put array in and add it to the cloneRefs list.
		CArray clone = new CArray(t, (int) array.size());
		clone.associativeMode = array.associativeMode;
		cloneRefs.add(new CArray[]{array, clone});

		// Iterate over the array, recursively calling this method to perform a deep clone.
		for(Mixed key : array.keySet()) {
			Mixed value = array.get(key, t);
			if(value.isInstanceOf(CArray.TYPE)) {
				value = deepClone((CArray) value, t, cloneRefs);
			}
			clone.set(key, value, t);
		}
		return clone;
	}

	private String normalizeConstruct(Mixed c) {
		if(c.isInstanceOf(CArray.TYPE)) {
			throw new CRECastException("Arrays cannot be used as the key in an associative array", c.getTarget());
		} else if(c.isInstanceOf(CString.TYPE) || c.isInstanceOf(CInt.TYPE)) {
			return c.val();
		} else if(c instanceof CNull) {
			return "";
		} else if(c.isInstanceOf(CBoolean.TYPE)) {
			if(((CBoolean) c).getBoolean()) {
				return "1";
			} else {
				return "0";
			}
		} else if(c instanceof CLabel) {
			return normalizeConstruct(((CLabel) c).cVal());
		} else {
			return c.val();
		}
	}

	/**
	 * Removes the value at the specified integer key.
	 *
	 * @param i
	 * @return
	 */
	public Mixed remove(int i) {
		return remove(new CInt(i, Target.UNKNOWN));
	}

	/**
	 * Removes the value at the specified string key.
	 *
	 * @param s
	 * @return
	 */
	public Mixed remove(String s) {
		return remove(new CString(s, Target.UNKNOWN));
	}

	/**
	 * Removes the value at the specified key
	 *
	 * @param construct
	 * @return
	 */
	public Mixed remove(Mixed construct) {
		String c = normalizeConstruct(construct);
		Mixed ret;
		if(!associativeMode) {
			try {
				ret = array.remove(Integer.parseInt(c));
				nextIndex--;
			} catch (NumberFormatException e) {
				throw new CRECastException("Expecting an integer, but received \"" + c
						+ "\" (were you expecting an associative array? This array is a normal array.)", construct.getTarget());
			} catch (IndexOutOfBoundsException e) {
				throw new CRERangeException("Cannot remove the value at '" + c
						+ "', as no such index exists in the array", construct.getTarget());
			}
		} else {
			ret = associativeArray.remove(c);
			if(ret == null) {
				return CNull.NULL;
			}
		}
		setDirty();
		return ret;
	}

	/**
	 * Removes all values that are equal to the specified construct from this array
	 *
	 * @param construct
	 */
	public void removeValues(Mixed construct) {
		if(associativeMode) {
			Iterator<Mixed> it;
			it = associativeArray.values().iterator();
			while(it.hasNext()) {
				Mixed c = it.next();
				if(BasicLogic.equals.doEquals(c, construct)) {
					it.remove();
				}
			}
		} else {
			for(int i = array.size() - 1; i >= 0; i--) {
				Mixed c = array.get(i);
				if(BasicLogic.equals.doEquals(c, construct)) {
					array.remove(i);
				}
			}
		}
		setDirty();
	}

	/**
	 * Creates a new, empty array, with the same type. Note to subclasses: By default, this method expects a constructor
	 * that accepts a {@link Target}. If this assumption is not valid, you may override this method as needed.
	 *
	 * @param t
	 * @return
	 */
	public CArray createNew(Target t) {
		try {
			Constructor<CArray> con = (Constructor<CArray>) this.getClass().getConstructor(Target.class);
			try {
				return con.newInstance(t);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(this.typeof() + " does not support creating a new value.");
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Comparator<String> comparator = new Comparator<String>() {

		private int normalize(int value) {
			if(value < 0) {
				return -1;
			} else if(value > 0) {
				return 1;
			} else {
				return 0;
			}
		}

		@Override
		public int compare(String o1, String o2) {
			// Null checks!
			if(o1 == null && o2 != null) {
				return -1;
			} else if(o1 == null) {
				return 0;
			} else if(o2 == null) {
				return 1;
			}
			// This fixes a bug where occasionally (I can't totally figure out the pattern) a value
			// would be missing from the list. I think this is ok in all cases, except that it may
			// change the order of certain associative array's key display, however, this has never
			// been a guaranteed property of the arrays.
			return normalize(o1.compareTo(o2));
			/*
			//Due to a dumb behavior in Double.parseDouble,
			//we need to check to see if there are non-digit characters in
			//the keys, and if so, do a string comparison.
			if(o1.matches(".*[^0-9\\.]+.*") || o2.matches(".*[^0-9\\.]+.*")){
				return normalize(o1.compareTo(o2));
			}
			try {
				int i1 = Integer.parseInt(o1);
				int i2 = Integer.parseInt(o2);
				//They're both integers, do an integer comparison
				return new Integer(i1).compareTo(new Integer(i2));
			} catch (NumberFormatException e){
				try {
					double d1 = Double.parseDouble(o1);
					double d2 = Double.parseDouble(o2);
					//They're both doubles, do a double comparison
					return new Double(d1).compareTo(new Double(d2));
				} catch (NumberFormatException ee){
					//Just do a string comparison
					return normalize(o1.compareTo(o2));
				}
			}*/
		}

	};

	@Override
	public boolean isDynamic() {
		//The CArray is static, despite what you might first think.
		//The only way to get a static array is to use the array function,
		//which WILL return a static array. A function that takes an array
		//as an argument will accept the static array, and can be optimized possibly,
		//however, it is likely that the array is stored in a variable, which of couse
		//is NOT static. So, if just the array function is run, it's static, if the static
		//array is put into a variable, the staticness is lost (as it is with a number or string)
		return false;
	}

	@Override
	public boolean canBeAssociative() {
		return true;
	}

	@Override
	public Mixed slice(int begin, int end, Target t) {
		return new ArrayHandling.array_get().exec(t, null, new CSlice(begin, end, t));
	}

	@Override
	public String docs() {
		return "An array is a data type, which contains any number of other values.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_0_1;
	}

	@Override
	public Iterator<Mixed> iterator() {
		if(associativeMode) {
			throw new RuntimeException("iterator() cannot be called on an associative array");
		} else {
			return array.iterator();
		}
	}

	@MEnum("ms.lang.ArraySortType")
	public enum ArraySortType {
		/**
		 * Sorts the elements without converting types first. If a non-numeric string is compared to a numeric string,
		 * it is compared as a string, otherwise, it's compared as a natural ordering.
		 */
		REGULAR,
		/**
		 * All strings are considered numeric, that is, 001 comes before 2.
		 */
		NUMERIC,
		/**
		 * All values are considered strings.
		 */
		STRING,
		/**
		 * All values are considered strings, but the comparison is case-insensitive.
		 */
		STRING_IC
	}

	public void sort(final ArraySortType sort) {
		if(this.associativeMode) {
			array = new ArrayList(associativeArray.values());
			this.associativeArray.clear();
			this.associativeArray = null;
			this.associativeMode = false;
			MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.VERBOSE, "Attempting to sort an associative array; key values will be lost.", this.getTarget());
		}
		array.sort(new Comparator<Mixed>() {
			@Override
			public int compare(Mixed o1, Mixed o2) {
				//o1 < o2 -> -1
				//o1 == o2 -> 0
				//o1 > o2 -> 1
				for(int i = 0; i < 2; i++) {
					Mixed c;
					if(i == 0) {
						c = o1;
					} else {
						c = o2;
					}
					if(c.isInstanceOf(CArray.TYPE)) {
						throw new CRECastException("Cannot sort an array of arrays.", CArray.this.getTarget());
					}
					if(!(c.isInstanceOf(CBoolean.TYPE) || c.isInstanceOf(CString.TYPE) || c.isInstanceOf(CInt.TYPE)
							|| c.isInstanceOf(CDouble.TYPE) || c instanceof CNull || c.isInstanceOf(CClassType.TYPE))) {
						throw new CREFormatException("Unsupported type being sorted: " + c.typeof(), CArray.this.getTarget());
					}
				}
				if(o1 instanceof CNull || o2 instanceof CNull) {
					if(o1 instanceof CNull && o2 instanceof CNull) {
						return 0;
					} else if(o1 instanceof CNull) {
						return "".compareTo(o2.val());
					} else {
						return o1.val().compareTo("");
					}
				}
				if(o1.isInstanceOf(CBoolean.TYPE) || o2.isInstanceOf(CBoolean.TYPE)) {
					if(ArgumentValidation.getBoolean(o1, Target.UNKNOWN) == ArgumentValidation.getBoolean(o2, Target.UNKNOWN)) {
						return 0;
					} else {
						int oo1 = ArgumentValidation.getBoolean(o1, Target.UNKNOWN) ? 1 : 0;
						int oo2 = ArgumentValidation.getBoolean(o2, Target.UNKNOWN) ? 1 : 0;
						return (oo1 < oo2) ? -1 : 1;
					}
				}
				//At this point, things will either be numbers or strings
				switch(sort) {
					case REGULAR:
						return compareRegular(o1, o2);
					case NUMERIC:
						return compareNumeric(o1, o2);
					case STRING:
						return compareString(o1.val(), o2.val());
					case STRING_IC:
						return compareString(o1.val().toLowerCase(), o2.val().toLowerCase());
				}
				throw ConfigRuntimeException.CreateUncatchableException("Missing implementation for " + sort.name(), Target.UNKNOWN);
			}

			public int compareRegular(Mixed o1, Mixed o2) {
				if(ArgumentValidation.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o1), Target.UNKNOWN)
						&& ArgumentValidation.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o2), Target.UNKNOWN)) {
					return compareNumeric(o1, o2);
				} else if(ArgumentValidation.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o1), Target.UNKNOWN)) {
					//The first is a number, the second is a string
					return -1;
				} else if(ArgumentValidation.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o2), Target.UNKNOWN)) {
					//The second is a number, the first is a string
					return 1;
				} else {
					//They are both strings
					return compareString(o1.val(), o2.val());
				}
			}

			public int compareNumeric(Mixed o1, Mixed o2) {
				double d1 = Static.getNumber(o1, o1.getTarget());
				double d2 = Static.getNumber(o2, o2.getTarget());
				return Double.compare(d1, d2);
			}

			public int compareString(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		this.setDirty();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Clears all the values out of this array
	 */
	public void clear() {
		this.array.clear();
		this.associativeArray.clear();
		this.nextIndex = 0;
		this.parent = null;
		this.valueDirty = true;
	}

	public void ensureCapacity(int capacity) {
		((ArrayList) array).ensureCapacity(capacity);
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.FINAL);
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Booleanish.TYPE, com.laytonsmith.core.natives.interfaces.Iterable.TYPE};
	}

	@Override
	public boolean getBooleanValue(Target t) {
		return size() > 0;
	}

}
