package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCTransformation;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 */
public class BukkitMCTransformation implements MCTransformation {

	Transformation transformation;

	public BukkitMCTransformation(Transformation transformation) {
		this.transformation = transformation;
	}

	@Override
	public Vector3f getTranslation() {
		return transformation.getTranslation();
	}

	@Override
	public Quaternionf getLeftRotation() {
		return transformation.getLeftRotation();
	}

	@Override
	public Vector3f getScale() {
		return transformation.getScale();
	}

	@Override
	public Quaternionf getRightRotation() {
		return transformation.getRightRotation();
	}
}
