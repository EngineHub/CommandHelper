package com.laytonsmith.abstraction;

/**
 * All AbstractionObject implementations should know how to both return their underlying object,
 * and construct a new object, given that it is a compatible type. This has the advantage of keeping
 * this logic completely inside that particular object, instead of having a giant cast tree that creates
 * a new concrete class for each type. Further, since supertypes can implement the getHandle method, only
 * subclasses that need to implement the false constructor have to. A template implementation is given in the
 * source below.
 * @author layton
 */
public interface AbstractionObject {
    /**
     * The underlying object that the abstraction object wraps. This can be used in combination with
     * instanceof MCType to determine if the wrapped type can be cast to Type.
     * @return 
     */
    public Object getHandle();
}

/*
public BukkitMC<>(AbstractionObject a){
    this((<>)null);
    if(a instanceof MC<>){
        this.<> = ((<>)a.getHandle());
    } else {
        throw new ClassCastException();
    }
}

public Object getHandle(){
    return <>;
}
 */
