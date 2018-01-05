package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
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
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.ObjectType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
@typeof("array")
public class CArray extends Construct implements ArrayAccess {

    public static final CClassType TYPE = CClassType.get("array");
    private boolean associative_mode = false;
    private long next_index = 0;
    private List<Construct> array;
    private SortedMap<String, Construct> associative_array;
    private String mutVal;
    private CArray parent = null;
    private boolean valueDirty = true;

    public CArray(Target t) {
        this(t, 0, (Construct[]) null);
    }

    public CArray(Target t, Construct... items) {
        this(t, 0, items);
    }

    public CArray(Target t, int initialCapacity) {
        this(t, initialCapacity, (Construct[]) null);
    }

    public CArray(Target t, Collection<Construct> items) {
        this(t, 0, getArray(items));
    }

    public CArray(Target t, int initialCapacity, Collection<Construct> items) {
        this(t, initialCapacity, getArray(items));
    }

    public CArray(Target t, int initialCapacity, Construct... items) {
        super("{}", ConstructType.ARRAY, t);
        if (initialCapacity == -1) {
            associative_mode = true;
        } else if (items != null) {
            for (Construct item : items) {
                if (item instanceof CEntry) {
                    //it's an associative array
                    associative_mode = true;
                    break;
                }
            }
        }
        associative_array = new TreeMap<>(comparator);
        array = associative_mode ? new ArrayList<Construct>() : initialCapacity > 0 ? new ArrayList<Construct>(initialCapacity) : items != null ? new ArrayList<Construct>(items.length) : new ArrayList<Construct>();
        if (associative_mode) {
            if (items != null) {
                for (Construct item : items) {
                    if (item instanceof CEntry) {
                        associative_array.put(normalizeConstruct(((CEntry) item).ckey), ((CEntry) item).construct);
                    } else {
                        int max = Integer.MIN_VALUE;
                        for (String key : associative_array.keySet()) {
                            try {
                                int i = Integer.parseInt(key);
                                max = java.lang.Math.max(max, i);
                            } catch (NumberFormatException e) {
                            }
                        }
                        if (max == Integer.MIN_VALUE) {
                            max = -1; //Special case, there are no integer indexes in here yet.
                        }
                        associative_array.put(Integer.toString(max + 1), item);
                        if (item instanceof CArray) {
                            ((CArray) item).parent = this;
                        }
                    }
                }
            }
        } else {
            if (items != null) {
                for (Construct item : items) {
                    array.add(item);
                    if (item instanceof CArray) {
                        ((CArray) item).parent = this;
                    }
                }
            }
            this.next_index = array.size();
        }
        regenValue();
    }

    /**
     * Returns if this array is in associative mode or not.
     *
     * @return
     */
    @Override
    public boolean isAssociative() {
        return associative_mode;
    }

    /**
     * Returns the backing array.
     *
     * @return
     */
    protected List<Construct> getArray() {
        return array;
    }

    /**
     * Returns a List based on the array. This is only applicable if this is a normal array.
     *
     * @return
     */
    public List<Construct> asList() {
        if (inAssociativeMode()) {
            throw new RuntimeException("asList can only be called on a normal array");
        } else {
            return new ArrayList<Construct>(array);
        }
    }

    /**
     * Returns the backing associative array.
     *
     * @return
     */
    protected SortedMap<String, Construct> getAssociativeArray() {
        return associative_array;
    }

    private static Construct[] getArray(Collection<Construct> items) {
        Construct c[] = new Construct[items.size()];
        int count = 0;
        for (Construct cc : items) {
            c[count++] = cc;
        }
        return c;
    }

    /**
     * @return Whether or not this array is operating in associative mode
     */
    public boolean inAssociativeMode() {
        return associative_mode;
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

    public static CArray GetAssociativeArray(Target t, Construct[] args) {
        return new CArray(t, -1, args);
    }

    /**
     * This must be called every time the underlying model is changed, which sets the toString value to dirty, which
     * means that the value will be regenerated next time it is requested.
     */
    private void regenValue() {
        if (valueDirty) {
            return; // All parents must be dirty too
        }
        regenValue(new HashSet<CArray>());
    }

    private void regenValue(Set<CArray> arrays) {
        if (arrays.contains(this)) {
            return; //Recursive, so don't continue.
        }
        valueDirty = true;
        if (parent != null) {
            arrays.add(this);
            parent.regenValue(arrays);
        }
    }

    /**
     * Reverses the array in place, if it is a normal array, otherwise, if associative, it throws an exception.
     *
     * @param t
     */
    public void reverse(Target t) {
        if (!associative_mode) {
            Collections.reverse(array);
            regenValue();
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
    public final void push(Construct c, Target t) {
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
    public void push(Construct c, Integer index, Target t) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (!associative_mode) {
            if (index != null) {
                array.add(index, c);
            } else {
                array.add(c);
            }
            next_index++;
        } else {
            if (index != null) {
                throw new IllegalArgumentException("Cannot insert into an associative array");
            }
            int max = 0;
            for (String key : associative_array.keySet()) {
                try {
                    int i = Integer.parseInt(key);
                    max = java.lang.Math.max(max, i);
                } catch (NumberFormatException e) {
                }
            }
            if (c instanceof CEntry) {
                associative_array.put(Integer.toString(max + 1), ((CEntry) c).construct());
            } else {
                associative_array.put(Integer.toString(max + 1), c);
            }
        }
        if (c instanceof CArray) {
            ((CArray) c).parent = this;
        }
        regenValue();
    }

    /**
     * Returns the key set for this array. If it's an associative array, it simply returns the key set of the map,
     * otherwise it generates a set of CInts from 0 to size-1, and returns that.
     *
     * @return
     */
    @Override
    public Set<Construct> keySet() {
        Set<Construct> set = !associative_mode ? new LinkedHashSet<Construct>(array.size()) : new LinkedHashSet<Construct>(associative_array.size());
        if (!associative_mode) {
            for (int i = 0; i < array.size(); i++) {
                set.add(new CInt(i, Target.UNKNOWN));
            }
        } else {
            for (String key : associative_array.keySet()) {
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
        Set<String> set = !associative_mode ? new LinkedHashSet<String>(array.size()) : new HashSet<String>(associative_array.size());
        if (!associative_mode) {
            for (int i = 0; i < array.size(); i++) {
                set.add(Integer.toString(i));
            }
        } else {
            set = associative_array.keySet();
        }
        return set;
    }

    private void setAssociative() {
        associative_array = new TreeMap<>(comparator);
        for (int i = 0; i < array.size(); i++) {
            associative_array.put(Integer.toString(i), array.get(i));
        }
        associative_mode = true;
        array = null; // null out the original array container so it can be GC'd
    }

    /**
     *
     * @param index
     * @param c
     */
    public void set(Construct index, Construct c, Target t) {
        if (!associative_mode) {
            if (index instanceof CNull) {
                // Invalid normal array index
                setAssociative();
            } else {
                try {
                    int indx = Static.getInt32(index, t);
                    if (indx > next_index || indx < 0) {
                        // Out of range
                        setAssociative();
                    } else if (indx == next_index) {
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
        if (associative_mode) {
            associative_array.put(normalizeConstruct(index), c);
        }
        if (c instanceof CArray) {
            ((CArray) c).parent = this;
        }
        regenValue();
    }

    public final void set(int index, Construct c, Target t) {
        this.set(new CInt(index, t), c, t);
    }

    /* Shortcuts */
    public final void set(String index, Construct c, Target t) {
        set(new CString(index, t), c, t);
    }

    public final void set(String index, String value, Target t) {
        set(index, new CString(value, t), t);
    }

    public final void set(String index, String value) {
        set(index, value, Target.UNKNOWN);
    }

    @Override
    public Construct get(Construct index, Target t) {
        if (!associative_mode) {
            try {
                return array.get(Static.getInt32(index, t));
            } catch (IndexOutOfBoundsException e) {
                throw new CREIndexOverflowException("The element at index \"" + index.val() + "\" does not exist", t, e);
            }
        } else {
            Construct val = associative_array.get(normalizeConstruct(index));
            if (val != null) {
                if (val instanceof CEntry) {
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

    public final Construct get(long index, Target t) {
        return this.get(new CInt(index, t), t);
    }

    @Override
    public final Construct get(int index, Target t) {
        return this.get(new CInt(index, t), t);
    }

    @Override
    public final Construct get(String index, Target t) {
        return this.get(new CString(index, t), t);
    }

    public boolean containsKey(String c) {
        if (associative_mode) {
            return associative_array.containsKey(c);
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

    public boolean contains(Construct c) {
        if (associative_mode) {
            return associative_array.containsValue(c);
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
    public CArray indexesOf(Construct value) {
        CArray ret = new CArray(Target.UNKNOWN);
        if (associative_mode) {
            for (String key : associative_array.keySet()) {
                if (BasicLogic.equals.doEquals(associative_array.get(key), value)) {
                    ret.push(new CString(key, Target.UNKNOWN), Target.UNKNOWN);
                }
            }
        } else {
            for (int i = 0; i < array.size(); i++) {
                if (BasicLogic.equals.doEquals(array.get(i), value)) {
                    ret.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN);
                }
            }
        }
        return ret;
    }

    @Override
    public String val() {
        if (valueDirty) {
            getString(new Stack<CArray>(), this.getTarget());
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
        if (!valueDirty) {
            return mutVal;
        }
        StringBuilder b = new StringBuilder();
        b.append("{");
        if (!inAssociativeMode()) {
            for (int i = 0; i < this.size(); i++) {
                Mixed value = this.get(i, t);
                String v;
                if (value instanceof CArray) {
                    if (arrays.contains((CArray) value)) {
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
                if (i > 0) {
                    b.append(", ");
                }
                b.append(v);
            }
        } else {
            boolean first = true;
            for (String key : this.stringKeySet()) {
                if (!first) {
                    b.append(", ");
                }
                first = false;
                String v;
                if (this.get(key, t) == null) {
                    v = "null";
                } else {
                    Mixed value = this.get(key, t);
                    if (value instanceof CArray) {
                        if (arrays.contains(((CArray) value))) {
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
        if (associative_mode) {
            return associative_array.size();
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
        clone.associative_mode = associative_mode;
        if (!associative_mode) {
            if (array != null) {
                clone.array = new ArrayList<Construct>(this.array);
            }
        } else if (associative_array != null) {
            clone.associative_array = new TreeMap<String, Construct>(this.associative_array);
        }
        clone.regenValue();
        return clone;
    }

    public CArray deepClone(Target t) {
        CArray clone = deepClone(this, t, new ArrayList<CArray[]>());
        return clone;
    }

    private static CArray deepClone(CArray array, Target t, ArrayList<CArray[]> cloneRefs) {

        // Return the clone reference if this array has been cloned before (both clones will have the same reference).
        for (CArray[] refCouple : cloneRefs) {
            if (refCouple[0] == array) {
                return refCouple[1];
            }
        }

        // Create the clone to put array in and add it to the cloneRefs list.
        CArray clone = new CArray(t, (int) array.size());
        clone.associative_mode = array.associative_mode;
        cloneRefs.add(new CArray[]{array, clone});

        // Iterate over the array, recursively calling this method to perform a deep clone.
        for (Construct key : array.keySet()) {
            Construct value = array.get(key, t);
            if (value instanceof CArray) {
                value = deepClone((CArray) value, t, cloneRefs);
            }
            clone.set(key, value, t);
        }
        return clone;
    }

    private String normalizeConstruct(Construct c) {
        if (c instanceof CArray) {
            throw new CRECastException("Arrays cannot be used as the key in an associative array", c.getTarget());
        } else if (c instanceof CString || c instanceof CInt) {
            return c.val();
        } else if (c instanceof CNull) {
            return "";
        } else if (c instanceof CBoolean) {
            if (((CBoolean) c).getBoolean()) {
                return "1";
            } else {
                return "0";
            }
        } else if (c instanceof CLabel) {
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
    public Construct remove(int i) {
        return remove(new CInt(i, Target.UNKNOWN));
    }

    /**
     * Removes the value at the specified string key.
     *
     * @param s
     * @return
     */
    public Construct remove(String s) {
        return remove(new CString(s, Target.UNKNOWN));
    }

    /**
     * Removes the value at the specified key
     *
     * @param construct
     * @return
     */
    public Construct remove(Construct construct) {
        String c = normalizeConstruct(construct);
        Construct ret;
        if (!associative_mode) {
            try {
                ret = array.remove(Integer.parseInt(c));
                next_index--;
            } catch (NumberFormatException e) {
                throw new CRECastException("Expecting an integer, but received \"" + c + "\" (were you expecting an associative array? This array is a normal array.)", construct.getTarget());
            } catch (IndexOutOfBoundsException e) {
                throw new CRERangeException("Cannot remove the value at '" + c + "', as no such index exists in the array", construct.getTarget());
            }
        } else {
            ret = associative_array.remove(c);
        }
        regenValue();
        return ret;
    }

    /**
     * Removes all values that are equal to the specified construct from this array
     *
     * @param construct
     */
    public void removeValues(Construct construct) {
        if (associative_mode) {
            Iterator<Construct> it;
            it = associative_array.values().iterator();
            while (it.hasNext()) {
                Construct c = it.next();
                if (BasicLogic.equals.doEquals(c, construct)) {
                    it.remove();
                }
            }
        } else {
            for (int i = array.size() - 1; i >= 0; i--) {
                Construct c = array.get(i);
                if (BasicLogic.equals.doEquals(c, construct)) {
                    array.remove(i);
                }
            }
        }
        regenValue();
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
            if (value < 0) {
                return -1;
            } else if (value > 0) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public int compare(String o1, String o2) {
            // Null checks!
            if (o1 == null && o2 != null) {
                return -1;
            } else if (o1 == null) {
                return 0;
            } else if (o2 == null) {
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
            try{
                int i1 = Integer.parseInt(o1);
                int i2 = Integer.parseInt(o2);
                //They're both integers, do an integer comparison
                return new Integer(i1).compareTo(new Integer(i2));
            } catch(NumberFormatException e){
                try{
                    double d1 = Double.parseDouble(o1);
                    double d2 = Double.parseDouble(o2);
                    //They're both doubles, do a double comparison
                    return new Double(d1).compareTo(new Double(d2));
                } catch(NumberFormatException ee){
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
    public Construct slice(int begin, int end, Target t) {
        return new ArrayHandling.array_get().exec(t, null, new CSlice(begin, end, t));
    }

    @Override
    public String docs() {
        return "An array is a data type, which contains any number of other values.";
    }

    @Override
    public Version since() {
        return CHVersion.V3_0_1;
    }

    public enum SortType {
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

    public void sort(final SortType sort) {
        List<Construct> list = array;
        if (this.associative_mode) {
            list = new ArrayList(associative_array.values());
            this.associative_array.clear();
            this.associative_array = null;
            this.associative_mode = false;
            CHLog.GetLogger().Log(CHLog.Tags.GENERAL, LogLevel.VERBOSE, "Attempting to sort an associative array; key values will be lost.", this.getTarget());
        }
        Collections.sort(array, new Comparator<Construct>() {
            @Override
            public int compare(Construct o1, Construct o2) {
                //o1 < o2 -> -1
                //o1 == o2 -> 0
                //o1 > o2 -> 1
                for (int i = 0; i < 2; i++) {
                    Construct c;
                    if (i == 0) {
                        c = o1;
                    } else {
                        c = o2;
                    }
                    if (c instanceof CArray) {
                        throw new CRECastException("Cannot sort an array of arrays.", CArray.this.getTarget());
                    }
                    if (!(c instanceof CBoolean || c instanceof CString || c instanceof CInt
                            || c instanceof CDouble || c instanceof CNull)) {
                        throw new CREFormatException("Unsupported type being sorted: " + c.getCType(), CArray.this.getTarget());
                    }
                }
                if (o1 instanceof CNull || o2 instanceof CNull) {
                    if (o1 instanceof CNull && o2 instanceof CNull) {
                        return 0;
                    } else if (o1 instanceof CNull) {
                        return "".compareTo(o2.getValue());
                    } else {
                        return o1.val().compareTo("");
                    }
                }
                if (o1 instanceof CBoolean || o2 instanceof CBoolean) {
                    if (Static.getBoolean(o1) == Static.getBoolean(o2)) {
                        return 0;
                    } else {
                        int oo1 = Static.getBoolean(o1) == true ? 1 : 0;
                        int oo2 = Static.getBoolean(o2) == true ? 1 : 0;
                        return (oo1 < oo2) ? -1 : 1;
                    }
                }
                //At this point, things will either be numbers or strings
                switch (sort) {
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

            public int compareRegular(Construct o1, Construct o2) {
                if (Static.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o1))
                        && Static.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o2))) {
                    return compareNumeric(o1, o2);
                } else if (Static.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o1))) {
                    //The first is a number, the second is a string
                    return -1;
                } else if (Static.getBoolean(new DataHandling.is_numeric().exec(Target.UNKNOWN, null, o2))) {
                    //The second is a number, the first is a string
                    return 1;
                } else {
                    //They are both strings
                    return compareString(o1.val(), o2.val());
                }
            }

            public int compareNumeric(Construct o1, Construct o2) {
                double d1 = Static.getNumber(o1, o1.getTarget());
                double d2 = Static.getNumber(o2, o2.getTarget());
                return Double.compare(d1, d2);
            }

            public int compareString(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        this.array = list;
        this.regenValue();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Clears all the values out of this array
     */
    public void clear() {
        this.array.clear();
        this.associative_array.clear();
        this.next_index = 0;
        this.parent = null;
        this.valueDirty = true;
    }

    public void ensureCapacity(int capacity) {
        ((ArrayList) array).ensureCapacity(capacity);
    }

    @Override
    public CClassType[] getSuperclasses() {
        return new CClassType[]{Mixed.TYPE};
    }

    @Override
    public CClassType[] getInterfaces() {
        return new CClassType[]{ArrayAccess.TYPE};
    }

}
