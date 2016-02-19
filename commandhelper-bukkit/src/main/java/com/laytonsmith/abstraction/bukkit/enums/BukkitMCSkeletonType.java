
package com.laytonsmith.abstraction.bukkit.enums;

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

	private static BukkitMCSkeletonType instance;

	public static BukkitMCSkeletonType getConvertor() {
		if (instance == null) {
			instance = new BukkitMCSkeletonType();
		}
		return instance;
	}
}