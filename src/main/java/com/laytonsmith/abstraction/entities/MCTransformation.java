package com.laytonsmith.abstraction.entities;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Cailin
 */
public interface MCTransformation {

	public Vector3f getTranslation();

	public Quaternionf getLeftRotation();

	public Vector3f getScale();

	public Quaternionf getRightRotation();

}
