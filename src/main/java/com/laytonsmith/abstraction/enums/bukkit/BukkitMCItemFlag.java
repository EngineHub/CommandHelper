package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.inventory.ItemFlag;

/**
 *
 * @author jacobwgillespie
 */
@abstractionenum(
        implementation=Implementation.Type.BUKKIT,
        forAbstractEnum=MCItemFlag.class,
        forConcreteEnum=ItemFlag.class
)
public class BukkitMCItemFlag extends EnumConvertor<MCItemFlag, ItemFlag> {

    private static BukkitMCItemFlag instance;

    public static BukkitMCItemFlag getConvertor() {
        if (instance == null) {
            instance = new BukkitMCItemFlag();
        }
        return instance;
    }
}
