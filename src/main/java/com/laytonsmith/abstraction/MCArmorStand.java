package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.enums.MCBodyPart;

import java.util.Map;

public interface MCArmorStand {

	/**
	 *
	 * @return A map of body part keys and angle vector values
	 */
	public Map<MCBodyPart, Vector3D> getAllPoses();

	/**
	 * Set all poses at once.
	 * @param posemap A map of body part keys and angle vector values
	 */
	public void setAllPoses(Map<MCBodyPart, Vector3D> posemap);

	/**
	 * Returns the item the armor stand is
	 * currently holding
	 *
	 * @return the held item
	 */
	MCItemStack getItemInHand();

	/**
	 * Sets the item the armor stand is currently
	 * holding
	 *
	 * @param item the item to hold
	 */
	void setItemInHand(MCItemStack item);

	/**
	 * Returns the item currently being worn
	 * by the armor stand on its feet
	 *
	 * @return the worn item
	 */
	MCItemStack getBoots();

	/**
	 * Sets the item currently being worn
	 * by the armor stand on its feet
	 *
	 * @param item the item to wear
	 */
	void setBoots(MCItemStack item);

	/**
	 * Returns the item currently being worn
	 * by the armor stand on its legs
	 *
	 * @return the worn item
	 */
	MCItemStack getLeggings();

	/**
	 * Sets the item currently being worn
	 * by the armor stand on its legs
	 *
	 * @param item the item to wear
	 */
	void setLeggings(MCItemStack item);

	/**
	 * Returns the item currently being worn
	 * by the armor stand on its chest
	 *
	 * @return the worn item
	 */
	MCItemStack getChestplate();

	/**
	 * Sets the item currently being worn
	 * by the armor stand on its chest
	 *
	 * @param item the item to wear
	 */
	void setChestplate(MCItemStack item);

	/**
	 * Returns the item currently being worn
	 * by the armor stand on its head
	 *
	 * @return the worn item
	 */
	MCItemStack getHelmet();

	/**
	 * Sets the item currently being worn
	 * by the armor stand on its head
	 *
	 * @param item the item to wear
	 */
	void setHelmet(MCItemStack item);

	/**
	 * Returns the armor stand's body's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @return the current pose
	 */
	Vector3D getBodyPose();

	/**
	 * Sets the armor stand's body's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @param pose the current pose
	 */
	void setBodyPose(Vector3D pose);

	/**
	 * Returns the armor stand's left arm's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @return the current pose
	 */
	Vector3D getLeftArmPose();

	/**
	 * Sets the armor stand's left arm's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @param pose the current pose
	 */
	void setLeftArmPose(Vector3D pose);

	/**
	 * Returns the armor stand's right arm's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @return the current pose
	 */
	Vector3D getRightArmPose();

	/**
	 * Sets the armor stand's right arm's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @param pose the current pose
	 */
	void setRightArmPose(Vector3D pose);

	/**
	 * Returns the armor stand's left leg's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @return the current pose
	 */
	Vector3D getLeftLegPose();

	/**
	 * Sets the armor stand's left leg's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @param pose the current pose
	 */
	void setLeftLegPose(Vector3D pose);

	/**
	 * Returns the armor stand's right leg's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @return the current pose
	 */
	Vector3D getRightLegPose();

	/**
	 * Sets the armor stand's right leg's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @param pose the current pose
	 */
	void setRightLegPose(Vector3D pose);

	/**
	 * Returns the armor stand's head's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @return the current pose
	 */
	Vector3D getHeadPose();

	/**
	 * Sets the armor stand's head's
	 * current pose as a 3D vector of doubles.
	 * Each component is the angle for that axis in radians.
	 *
	 * @param pose the current pose
	 */
	void setHeadPose(Vector3D pose);

	/**
	 * Returns whether the armor stand has
	 * a base plate
	 *
	 * @return whether it has a base plate
	 */
	boolean hasBasePlate();

	/**
	 * Sets whether the armor stand has a
	 * base plate
	 *
	 * @param basePlate whether is has a base plate
	 */
	void setHasBasePlate(boolean basePlate);

	/**
	 * Returns whether gravity applies to
	 * this armor stand
	 *
	 * @return whether gravity applies
	 */
	boolean hasGravity();

	/**
	 * Sets whether gravity applies to
	 * this armor stand
	 *
	 * @param gravity whether gravity should apply
	 */
	void setHasGravity(boolean gravity);

	/**
	 * Returns whether the armor stand should be
	 * visible or not
	 *
	 * @return whether the stand is visible or not
	 */
	boolean isVisible();

	/**
	 * Sets whether the armor stand should be
	 * visible or not
	 *
	 * @param visible whether the stand is visible or not
	 */
	void setVisible(boolean visible);

	/**
	 * Returns whether this armor stand has arms
	 *
	 * @return whether this has arms or not
	 */
	boolean hasArms();

	/**
	 * Sets whether this armor stand has arms
	 *
	 * @param arms whether this has arms or not
	 */
	void setHasArms(boolean arms);

	/**
	 * Returns whether this armor stand is scaled
	 * down
	 *
	 * @return whether this is scaled down
	 */
	boolean isSmall();

	/**
	 * Sets whether this armor stand is scaled
	 * down
	 *
	 * @param small whether this is scaled down
	 */
	void setSmall(boolean small);
}
