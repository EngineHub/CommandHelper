package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPose;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Pose;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCPose.class,
		forConcreteEnum = Pose.class
)
public class BukkitMCPose extends EnumConvertor<MCPose, Pose> {

	private static BukkitMCPose instance;

	public static BukkitMCPose getConvertor() {
		if(instance == null) {
			instance = new BukkitMCPose();
		}
		return instance;
	}
}
