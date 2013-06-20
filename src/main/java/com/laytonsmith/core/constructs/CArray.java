

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.ArrayHandling;
import com.laytonsmith.core.functions.BasicLogic;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MEnum;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.Operators;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that represents a dynamic array.
 * 
 * For subclasses, the ArrayAccess methods are the most commonly
 * overridden methods. There are several overloaded methods in this
 * class, you need only to override the non-final ones for the
 * same effect.
 */
@typename("array")
public class CArray extends Construct implements ArrayAccess, Iterable<Mixed>, Operators.Equality {

    private boolean associative_mode = false;
    private long next_index = 0;
    private List<Mixed> array;
    private SortedMap<String, Mixed> associative_array;
    private String mutVal;
    CArray parent = null;
	private boolean valueDirty = true;
    
    
    public CArray(Target t){
        this(t, (Mixed[])null);
    }
	
	public CArray(Target t, Collection<Mixed> items){
		this(t, getArray(items));
	}
	
	/**
	 * Returns if this array is in associative mode or not.
	 * @return 
	 */
	protected boolean isAssociative(){
		return associative_mode;
	}
	
	/**
	 * Returns the backing array.
	 * @return 
	 */
	protected List<Mixed> getArray(){
		return array;
	}
	
	/**
	 * Returns a List based on the array. This is only applicable if this
	 * is a normal array.
	 * @return 
	 */
	public List<Mixed> asList(){
		if(inAssociativeMode()){
			throw new RuntimeException("asList can only be called on a normal array");
		} else {
			return new ArrayList<Mixed>(array);
		}
	}
	
	/**
	 * Returns the backing associative array.
	 * @return 
	 */
	protected SortedMap<String, Mixed> getAssociativeArray(){
		return associative_array;
	}
	
	private static Mixed [] getArray(Collection<Mixed> items){
		Mixed c [] = new Mixed[items.size()];
		int count = 0;
		for(Mixed cc : items){
			c[count++] = cc;
		}
		return c;
	}

    public CArray(Target t,  Mixed... items) {
        super(null, t);
        if(items != null){
            for(Mixed item : items){
                if(item instanceof CEntry){
                    //it's an associative array
                    associative_mode = true;
                    break;
                }
            }
        }
        associative_array = new TreeMap<String, Mixed>(comparator);
        array = new ArrayList<Mixed>();
        if(associative_mode){
            if(items != null){
                for(Mixed item : items){
                    if(item instanceof CEntry){
                        associative_array.put(normalizeConstruct(((CEntry)item).ckey), ((CEntry)item).construct);
                    } else {
                        int max = Integer.MIN_VALUE;            
                        for (String key : associative_array.keySet()) {
                            try{
                                int i = Integer.parseInt(key);
                                max = java.lang.Math.max(max, i);
                            } catch(NumberFormatException e){}
                        }
                        if(max == Integer.MIN_VALUE){
                            max = -1; //Special case, there are no integer indexes in here yet.
                        }
                        associative_array.put(Integer.toString(max + 1), item);
                        if(item instanceof CArray){
                            ((CArray)item).parent = this;
                        }
                    }
                }
            }
        } else {
            if(items != null){
                for(Mixed item : items){
                    array.add(item);
                    if(item instanceof CArray){
                        ((CArray)item).parent = this;
                    }
                }
            }
            this.next_index = array.size();
        }
        regenValue();
    }

    /**
     * @return Whether or not this array is operating in associative mode
     */
    public boolean inAssociativeMode() {
        return associative_mode;
    }
	
	/**
	 * Returns a new empty CArray that is in associative mode.
	 * @param t
	 * @return 
	 */
	public static CArray GetAssociativeArray(Target t){
		return new CArray(t).forceAssociativeMode();
	}
	
	public static CArray GetAssociativeArray(Target t, Mixed[] args){
		return new CArray(t, args).forceAssociativeMode();
	}
    
    /**
     * This should only be used when copying an array that is already known to be associative, so integer keys will
     * remain associative.
     */
    private CArray forceAssociativeMode(){
        if(associative_array == null){
            associative_array = new TreeMap<String, Mixed>();
        }
        associative_mode = true;
		return this;
    }

	/**
	 * This must be called every time the underlying model is changed, which
	 * sets the toString value to dirty, which means that the value will be regenerated
	 * next time it is requested.
	 */
    private void regenValue() {		
        valueDirty = true;
		if(parent != null){
			parent.regenValue();
		}
    }
	
	/**
	 * Reverses the array in place, if it is a normal array, otherwise, if associative, it throws
	 * an exception.
	 */
	public void reverse(){
		if(!associative_mode){
			Collections.reverse(array);
			regenValue();
		} else {
			throw new ConfigRuntimeException("Cannot reverse an associative array.", ExceptionType.CastException, getTarget());
		}
	}

	/**
	 * Pushes a new Construct onto the end of the array.
	 * @param c 
	 */
	public void push(Mixed c){
		push(c, null);
	}
    /**
     * Pushes a new Construct onto the end of the array. If the index is specified, this works like
	 * a "insert" operation, in that all values are shifted to the right, starting with the value
	 * at that index. If the array is associative though, you MUST send null, otherwise an
	 * {@link IllegalArgumentException} is thrown. Ideally, you should use {@link #set} anyways
	 * for an associative array.
     * @param c The Construct to add to the array
	 * @throws IllegalArgumentException If index is not null, and this is an associative array.
	 * @throws IndexOutOfBoundsException If the index is not null, and the index specified is out of
	 * range.
     */
    public void push(Mixed c, Integer index) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (!associative_mode) {
			if(index != null){
				array.add(index, c);
			} else {
				array.add(c);
			}
            next_index++;
        } else {
			if(index != null){
				throw new IllegalArgumentException("Cannot insert into an associative array");
			}
            int max = 0;            
            for (String key : associative_array.keySet()) {
                try{
                    int i = Integer.parseInt(key);
                    max = java.lang.Math.max(max, i);
                } catch(NumberFormatException e){}
            }
            if(c instanceof CEntry){
                associative_array.put(Integer.toString(max + 1), ((CEntry)c).construct());
            } else {
                associative_array.put(Integer.toString(max + 1), c);
            }
        }
        if(c instanceof CArray){
            ((CArray)c).parent = this;
        }
        regenValue();
    }
    
    /**
     * Returns the key set for this array. If it's an associative array, it simply returns
     * the key set of the map, otherwise it generates a set real quick from 0 - size-1, and
     * returns that.
     * @return 
     */
    public Set<String> keySet(){
        Set<String> set = !associative_mode?new LinkedHashSet<String>(array.size()):new HashSet<String>(associative_array.size());
        if(!associative_mode){            
            for(int i = 0; i < array.size(); i++){
                set.add(Integer.toString(i));
            }
        } else {
            set = associative_array.keySet();
        }        
        return set;
    }

    /**
     * 
     * @param index
     * @param c 
     */
    public void set(CPrimitive index, Mixed c, Target t) {
        if (!associative_mode) {
            try {
                int indx = index.castToInt32(t);
                if (indx > next_index || indx < 0) {
                    throw new ConfigRuntimeException("", ExceptionType.IndexOverflowException, Target.UNKNOWN);
                } else if(indx == next_index){
                    this.push(c);
                } else {
                    array.set(indx, c);
                }
            } catch (ConfigRuntimeException e) {
                //Not a number. Convert to associative.
                associative_array = new TreeMap<String, Mixed>(comparator);
                for (int i = 0; i < array.size(); i++) {
                    associative_array.put(Integer.toString(i), array.get(i));
                }
                associative_mode = true;
                array = null; // null out the original array container so it can be GC'd
            }
        }
        if (associative_mode) {
            associative_array.put(normalizeConstruct(index), c);
        }
        if(c instanceof CArray){
            ((CArray)c).parent = this;
        }
        regenValue();
    }
    
    public final void set(int index, Mixed c, Target t){
        this.set(new CInt(index, Target.UNKNOWN), c, t);
    }
    /* Shortcuts */
    
    public final void set(String index, Mixed c, Target t){
        set(new CString(index, (c instanceof Construct?((Construct)c).getTarget():Target.UNKNOWN)), c, t);
    }
    
    public final void set(String index, String value, Target t){
        set(index, new CString(value, t), t);
    }
    
    public final void set(String index, String value){
        set(index, value, Target.UNKNOWN);
    }

    public Mixed get(CPrimitive index, Target t) {
        if(!associative_mode){
            try {
                return array.get(index.castToInt32(t));
            } catch (IndexOutOfBoundsException e) {
                throw new ConfigRuntimeException("The element at index \"" + index.val() + "\" does not exist", ExceptionType.IndexOverflowException, t);
            }
        } else {
            if(associative_array.containsKey(normalizeConstruct(index))){
                Mixed val = associative_array.get(normalizeConstruct(index));
                if(val instanceof CEntry){
                    return ((CEntry)val).construct();
                }
                return val;
            } else {
                throw new ConfigRuntimeException("The element at index \"" + index.val() + "\" does not exist", ExceptionType.IndexOverflowException, t);
            }
        }
    }
    
    public final Mixed get(long index, Target t){
        return this.get(new CInt(index, t), t);
    }
    
    public final Mixed get(String index, Target t){
        return this.get(new CString(index, t), t);
    }
    
    public final Mixed get(String index){
        return this.get(index, Target.UNKNOWN);
    }
    
    public final Mixed get(long index){
        return this.get(index, Target.UNKNOWN);
    }
    
	@Override
    public boolean containsKey(String c){
        if(associative_mode){
            return associative_array.containsKey(c);
        } else {
			try{
				Integer i = Integer.valueOf(c);
                return array.size() > i;
			} catch(NumberFormatException e){
				return false;
			}
        }
    }
    
    public final boolean containsKey(int i){
        return this.containsKey(Integer.toString(i));
    }
    
	/**
	 * Returns true if this array contains the specified value
	 * @param c
	 * @return 
	 */
    public boolean contains(Construct c){
        if(associative_mode){
            return associative_array.containsValue(c);
        } else {
			return array.contains(c);
        }
    }
    
    public final boolean contains(String c){
        return contains(new CString(c, Target.UNKNOWN));
    }
    
    public final boolean contains(int i){
        return contains(new CString(Integer.toString(i), Target.UNKNOWN));
    }
	
	/**
	 * Returns an array of the keys of all the values that are
	 * equal to the value specified
	 * @param value
	 * @return 
	 */
	public CArray indexesOf(Mixed value){
		CArray ret = new CArray(Target.UNKNOWN);
		if(associative_mode){
			for(String key : associative_array.keySet()){
				if(BasicLogic.equals.doEquals(associative_array.get(key), value)){
					ret.push(new CString(key, Target.UNKNOWN));
				}
			}
		} else {
			for(int i = 0; i < array.size(); i++){
				if(BasicLogic.equals.doEquals(array.get(i), value)){
					ret.push(new CInt(i, Target.UNKNOWN));
				}
			}
		}
		return ret;
	}		

    @Override
    public String toString() {
		if(valueDirty){
			StringBuilder b = new StringBuilder();
			b.append("{");
			if (!associative_mode) {
				for (int i = 0; i < array.size(); i++) {
					if (i > 0) {
						b.append(", ");
						b.append(array.get(i).val());
					} else {
						b.append(array.get(i).val());
					}
				}
			} else {
				boolean first = true;
				for(String key : associative_array.keySet()){
					if(!first){
						b.append(", ");
					}
					first = false;
					b.append(key).append(": ").append(associative_array.get(key).val());
				}
			}
			b.append("}");
			mutVal = b.toString();
			if(parent != null){
				parent.regenValue();
			}
			valueDirty = false;
		}
        return mutVal;
    }

    public int size() {
        if(associative_mode){
            return associative_array.size();
        } else {
            return array.size();
        }
    }

    @Override
    public CArray doClone() {
        CArray clone;
		try {
			clone = (CArray) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
        clone.associative_mode = associative_mode;
        if(!associative_mode){
            if (array != null) {
                clone.array = new ArrayList<Mixed>(this.array);
            }
        } else {
            if(associative_array != null){
                clone.associative_array = new TreeMap<String, Mixed>(this.associative_array);
            }
        }
        clone.regenValue();
        return clone;
    }
    
    private String normalizeConstruct(Construct c){
        if(c instanceof CArray){
            throw new ConfigRuntimeException("Arrays cannot be used as the key in an associative array", ExceptionType.CastException, c.getTarget());
        } else if(c instanceof CString || c instanceof CInt){
            return c.val();
        } else if(c == null){
            return "";
        } else if(c instanceof CBoolean){
            if(((CBoolean)c).castToBoolean()){
                return "1";
            } else {
                return "0";
            }
        } else if(c instanceof CLabel){
            return normalizeConstruct(((CLabel)c).cVal());
        } else {
            return c.val();
        }
    }

	/**
	 * Removes a value from this array, given the specified key.
	 * @param key
	 * @return The value removed
	 */
	public Mixed remove(int key, Target t){
		return remove(Integer.toString(key), t);
	}
	
	/**
	 * Removes a value from this array, given the specified key.
	 * @param key
	 * @return The value removed
	 */
    public Mixed remove(String key, Target t) {
        String c = key;
        Mixed ret;
        if(!associative_mode){
            try{
                ret = array.remove(Integer.parseInt(c));
				next_index--;
            } catch(NumberFormatException e){ 
                throw new ConfigRuntimeException("Expecting an integer, but received " + c + " (were you expecting an associative array? This array is a normal array.)", ExceptionType.CastException, t);
            } catch(IndexOutOfBoundsException e){
                throw new ConfigRuntimeException("Cannot remove the value at '" + c + "', as no such index exists in the array", ExceptionType.RangeException, t);
            }
        } else {
            ret = associative_array.remove(c);
        }
        regenValue();
        return ret;
    }
	
	/**
	 * Removes all values that are equal to the specified construct
	 * from this array
	 * @param construct 
	 */
	public void removeValues(Mixed construct){
		if(associative_mode){
			Iterator<Mixed> it;
			it = associative_array.values().iterator();
			while(it.hasNext()){
				Mixed c = it.next();
				if(BasicLogic.equals.doEquals(c, construct)){
					it.remove();
				}
			}
		} else {
			for(int i = array.size() - 1; i >= 0; i--){
				Mixed c = array.get(i);
				if(BasicLogic.equals.doEquals(c, construct)){
					array.remove(i);
				}
			}
		}
		regenValue();
	}
    
    private Comparator<String> comparator = new Comparator<String>(){

        public int compare(String o1, String o2) {
            //Due to a dumb behavior in Double.parseDouble, 
            //we need to check to see if there are non-digit characters in
            //the keys, and if so, do a string comparison.
            if(o1.matches(".*[^0-9\\.]+.*") || o2.matches(".*[^0-9\\.]+.*")){
                return o1.compareTo(o2);
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
                    return o1.compareTo(o2);
                }
            }
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

    public boolean canBeAssociative() {
        return true;
    }

    public Mixed slice(int begin, int end, Target t) {
        return new ArrayHandling.array_get().exec(t, null, new CSlice(begin, end, t));
    }

	public String typeName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

	@Override
	public CPrimitive primitive(Target t) {
		throw new ConfigRuntimeException("Arrays cannot be cast to a primitive value.", ExceptionType.CastException, t);
	}

	public Iterator<Mixed> iterator() {
		return new Iterator<Mixed>() {
			
			//Freeze the iteration order now
			private List<String> keySet = new ArrayList<String>(keySet());
			private int index = 0;

			public boolean hasNext() {
				return index < keySet.size() - 1;
			}

			public Mixed next() {
				return get(keySet.get(++index));
			}

			public void remove() {
				CArray.this.remove(keySet.get(index), Target.UNKNOWN);
			}
		};
	}

	public boolean operatorEquals(Mixed m) {
		if(this == m){
			return true;
		}
		CArray other = (CArray)m;
		if(other.size() != this.size()){
			return false;
		} else if(!other.keySet().equals(this.keySet())){
			return false;
		} else {
			//Ok, the lengths are the same, the keys are the same, so now check
			//all the values for equivalence
			for(String key : other.keySet()){
				if(!com.laytonsmith.core.functions.BasicLogic.equals.doEquals(other.get(key), this.get(key))){
					return false;
				}
			}
			return true;
		}
	}

	public boolean operatorTestEquals(Class<? extends Mixed> clazz) {
		return CArray.class.isAssignableFrom(clazz);
	}
    
	@typename("SortType")
    public enum SortType implements MEnum {
        /**
         * Sorts the elements without converting types first. If a non-numeric
         * string is compared to a numeric string, it is compared as a string,
         * otherwise, it's compared as a natural ordering.
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
        STRING_IC;
		
		public Object value() {
			return this;
		}

		public String val() {
			return name();
		}

		public boolean isNull() {
			return false;
		}
		
		public Target getTarget(){
			return Target.UNKNOWN;
		}
		
		public SortType doClone(){
			return this;
		}
		
		public boolean isDynamic(){
			return false;
		}
		
		public void destructor(){
			
		}

		public String typeName() {
			return this.getClass().getAnnotation(typename.class).value();
		}

		public CPrimitive primitive(Target t) throws ConfigRuntimeException {
			throw new Error();
		}

		public boolean isImmutable() {
			return true;
		}
	}
    public void sort(final SortType sort, final Target t){
        List<Mixed> list = array;
        if(this.associative_mode){
            list = new ArrayList(associative_array.values());
            this.associative_array.clear();
            this.associative_array = null;
            this.associative_mode = false;
            CHLog.GetLogger().Log(CHLog.Tags.GENERAL, LogLevel.VERBOSE, "Attempting to sort an associative array; key values will be lost.", this.getTarget());
        }
        Collections.sort(array, new Comparator<Mixed>() {
            public int compare(Mixed o1, Mixed o2) {
                //o1 < o2 -> -1
                //o1 == o2 -> 0
                //o1 > o2 -> 1
                for(int i = 0; i < 2; i++){
                    Mixed c;
                    if(i == 0){
                        c = o1;
                    } else {
                        c = o2;
                    }
                    if(c instanceof CArray){
                        throw new ConfigRuntimeException("Cannot sort an array of arrays.", ExceptionType.CastException, CArray.this.getTarget());
                    }
                    if(!(c instanceof CBoolean || c instanceof CString || c instanceof CInt || 
                            c instanceof CDouble || c == null)){
                        throw ConfigRuntimeException.CreateUncatchableException("Unsupported type being sorted: " + c.getClass().getSimpleName(), CArray.this.getTarget());
                    }
                }
                if(o1 == null || o2 == null){
                    if(o1 == null && o2 == null){
                        return 0;
                    } else if(o1 == null){
                        return "".compareTo(o2.val());
                    } else {
                        return o1.val().compareTo("");
                    }
                }
                if(o1 instanceof CBoolean && o2 instanceof CPrimitive || o2 instanceof CBoolean && o1 instanceof CPrimitive){
					CPrimitive p1 = (CPrimitive)o1;
					CPrimitive p2 = (CPrimitive)o2;
                    if(p1.castToBoolean() == p2.castToBoolean()){
                        return 0;
                    } else {
                        int oo1 = p1.castToBoolean()==true?1:0;
                        int oo2 = p2.castToBoolean()==true?1:0;
                        return (oo1 < oo2) ? -1 : 1;
                    }
                }
                //At this point, things will either be numbers or strings
                switch(sort){
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
            public int compareRegular(Mixed o1, Mixed o2){
                if(new DataHandling.is_numeric().exec(t, null, o1).castToBoolean()
                        && new DataHandling.is_numeric().exec(t, null, o2).castToBoolean()){
                    return compareNumeric(o1, o2);
                } else if(new DataHandling.is_numeric().exec(t, null, o1).castToBoolean()){
                    //The first is a number, the second is a string
                    return -1;
                } else if(new DataHandling.is_numeric().exec(t, null, o2).castToBoolean()){
                    //The second is a number, the first is a string
                    return 1;
                } else {
                    //They are both strings
                    return compareString(o1.val(), o2.val());
                }
            }
            public int compareNumeric(Mixed o1, Mixed o2){
                double d1 = ((CPrimitive)o1).castToDouble(t);
                double d2 = ((CPrimitive)o2).castToDouble(t);
                return Double.compare(d1, d2);
            }
            public int compareString(String o1, String o2){
                return o1.compareTo(o2);
            }
        });
        this.array = list;  
        this.regenValue();
    }
	
	public boolean isEmpty(){
		return size() == 0;
	}
}
