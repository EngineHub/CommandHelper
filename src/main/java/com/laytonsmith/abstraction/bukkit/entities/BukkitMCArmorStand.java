package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.entities.MCArmorStand;
import com.laytonsmith.abstraction.enums.MCBodyPart;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.EulerAngle;

import java.util.EnumMap;
import java.util.Map;

public class BukkitMCArmorStand extends BukkitMCLivingEntity implements MCArmorStand {

	ArmorStand as;

	public BukkitMCArmorStand(Entity le) {
		super(le);
		as = (ArmorStand) le;
	}

	@Override
	public Map<MCBodyPart, Vector3D> getAllPoses() {
		Map<MCBodyPart, Vector3D> slots = new EnumMap<>(MCBodyPart.class);
		for(MCBodyPart key : MCBodyPart.values()) {
			switch(key) {
				case Head:
					slots.put(key, getHeadPose());
					break;
				case Torso:
					slots.put(key, getBodyPose());
					break;
				case ArmLeft:
					slots.put(key, getLeftArmPose());
					break;
				case ArmRight:
					slots.put(key, getRightArmPose());
					break;
				case LegLeft:
					slots.put(key, getLeftLegPose());
					break;
				case LegRight:
					slots.put(key, getRightLegPose());
					break;
			}
		}
		return slots;
	}

	@Override
	public void setAllPoses(Map<MCBodyPart, Vector3D> posemap) {
		Vector3D pose;
		for(Map.Entry<MCBodyPart, Vector3D> part : posemap.entrySet()) {
			pose = part.getValue();
			switch(part.getKey()) {
				case Head:
					setHeadPose(pose);
					break;
				case Torso:
					setBodyPose(pose);
					break;
				case ArmLeft:
					setLeftArmPose(pose);
					break;
				case ArmRight:
					setRightArmPose(pose);
					break;
				case LegLeft:
					setLeftLegPose(pose);
					break;
				case LegRight:
					setRightLegPose(pose);
					break;
			}
		}
	}

	@Override
	public Vector3D getBodyPose() {
		EulerAngle pose = as.getBodyPose();
		return new Vector3D(pose.getX(), pose.getY(), pose.getZ());
	}

	@Override
	public void setBodyPose(Vector3D pose) {
		as.setBodyPose(new EulerAngle(pose.X(), pose.Y(), pose.Z()));
	}

	@Override
	public Vector3D getLeftArmPose() {
		EulerAngle pose = as.getLeftArmPose();
		return new Vector3D(pose.getX(), pose.getY(), pose.getZ());
	}

	@Override
	public void setLeftArmPose(Vector3D pose) {
		as.setLeftArmPose(new EulerAngle(pose.X(), pose.Y(), pose.Z()));
	}

	@Override
	public Vector3D getRightArmPose() {
		EulerAngle pose = as.getRightArmPose();
		return new Vector3D(pose.getX(), pose.getY(), pose.getZ());
	}

	@Override
	public void setRightArmPose(Vector3D pose) {
		as.setRightArmPose(new EulerAngle(pose.X(), pose.Y(), pose.Z()));
	}

	@Override
	public Vector3D getLeftLegPose() {
		EulerAngle pose = as.getLeftLegPose();
		return new Vector3D(pose.getX(), pose.getY(), pose.getZ());
	}

	@Override
	public void setLeftLegPose(Vector3D pose) {
		as.setLeftLegPose(new EulerAngle(pose.X(), pose.Y(), pose.Z()));
	}

	@Override
	public Vector3D getRightLegPose() {
		EulerAngle pose = as.getRightLegPose();
		return new Vector3D(pose.getX(), pose.getY(), pose.getZ());
	}

	@Override
	public void setRightLegPose(Vector3D pose) {
		as.setRightLegPose(new EulerAngle(pose.X(), pose.Y(), pose.Z()));
	}

	@Override
	public Vector3D getHeadPose() {
		EulerAngle pose = as.getHeadPose();
		return new Vector3D(pose.getX(), pose.getY(), pose.getZ());
	}

	@Override
	public void setHeadPose(Vector3D pose) {
		as.setHeadPose(new EulerAngle(pose.X(), pose.Y(), pose.Z()));
	}

	@Override
	public boolean hasBasePlate() {
		return as.hasBasePlate();
	}

	@Override
	public void setHasBasePlate(boolean basePlate) {
		as.setBasePlate(basePlate);
	}

	@Override
	public boolean hasGravity() {
		return as.hasGravity();
	}

	@Override
	public void setHasGravity(boolean gravity) {
		as.setGravity(gravity);
	}

	@Override
	public boolean isVisible() {
		return as.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		as.setVisible(visible);
	}

	@Override
	public boolean hasArms() {
		return as.hasArms();
	}

	@Override
	public void setHasArms(boolean arms) {
		as.setArms(arms);
	}

	@Override
	public boolean isSmall() {
		return as.isSmall();
	}

	@Override
	public void setSmall(boolean small) {
		as.setSmall(small);
	}

	@Override
	public Boolean isMarker() {
		return as.isMarker();
	}

	@Override
	public void setMarker(boolean marker) {
		as.setMarker(marker);
	}
}
