
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Skeleton;

/**
 *
 * @author jb_aero
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCSkeletonType.class,
		forConcreteEnum=Skeleton.SkeletonType.class
		)
public class BukkitMCSkeletonType extends EnumConvertor<MCSkeletonType, Skeleton.SkeletonType> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType();
		}
		return instance;
	}
}