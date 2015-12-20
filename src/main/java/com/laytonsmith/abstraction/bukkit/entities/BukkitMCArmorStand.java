package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCArmorStand;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntityEquipment;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.enums.MCBodyPart;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.EnumMap;
import java.util.Map;

public class BukkitMCArmorStand extends BukkitMCLivingEntity implements MCArmorStand {

	ArmorStand as;
	ArmorStandEquipmentProxy ase;

	public BukkitMCArmorStand(Entity le) {
		super((LivingEntity) le);
		as = (ArmorStand) le;
		ase = new ArmorStandEquipmentProxy(as);
	}

	/**
	 * LivingEntity#getEquipment returns null for ArmorStands, so we need to supply our own.
	 * Needed for get|set_mob_equipment.
	 *
	 * @return Custom implementation of org.bukkit.inventory.EntityEquipment
	 */
	@Override
	public MCEntityEquipment getEquipment() {
		return new BukkitMCEntityEquipment(ase);
	}

	@Override
	public Map<MCBodyPart, Vector3D> getAllPoses() {
		Map<MCBodyPart, Vector3D> slots = new EnumMap<MCBodyPart, Vector3D>(MCBodyPart.class);
		for (MCBodyPart key : MCBodyPart.values()) {
			switch (key) {
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
		Vector3D pose = null;
		for (MCBodyPart key : posemap.keySet()) {
			pose = posemap.get(key);
			switch (key) {
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
	public MCItemStack getItemInHand() {
		return new BukkitMCItemStack(as.getItemInHand());
	}

	@Override
	public void setItemInHand(MCItemStack item) {
		as.setItemInHand(item == null ? null : ((BukkitMCItemStack) item).asItemStack());
	}

	@Override
	public MCItemStack getBoots() {
		return new BukkitMCItemStack(as.getBoots());
	}

	@Override
	public void setBoots(MCItemStack item) {
		as.setBoots(item == null ? null : ((BukkitMCItemStack) item).asItemStack());
	}

	@Override
	public MCItemStack getLeggings() {
		return new BukkitMCItemStack(as.getLeggings());
	}

	@Override
	public void setLeggings(MCItemStack item) {
		as.setLeggings(item == null ? null : ((BukkitMCItemStack) item).asItemStack());
	}

	@Override
	public MCItemStack getChestplate() {
		return new BukkitMCItemStack(as.getChestplate());
	}

	@Override
	public void setChestplate(MCItemStack item) {
		as.setChestplate(item == null ? null : ((BukkitMCItemStack) item).asItemStack());
	}

	@Override
	public MCItemStack getHelmet() {
		return new BukkitMCItemStack(as.getHelmet());
	}

	@Override
	public void setHelmet(MCItemStack item) {
		as.setHelmet(item == null ? null : ((BukkitMCItemStack) item).asItemStack());
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
		// Added in 1.8.7
		if(ReflectionUtils.hasMethod(as.getClass(), "isMarker", null)){
			return as.isMarker();
		}
		return null;
	}

	@Override
	public void setMarker(boolean marker) {
		// Added in 1.8.7
		if(ReflectionUtils.hasMethod(as.getClass(), "setMarker", null, boolean.class)){
			as.setMarker(marker);
		}
	}

	/**
	 * @author jb_aero
	 *         <p/>
	 *         Custom implementation of EntityEquipment
	 *         This remaps calls to EntityEquipment's methods to those of the associated ArmorStand
	 *         <p/>
	 *         Methods related to droprates are unmodified as they do nothing, but are not needed anyway.
	 */
	private class ArmorStandEquipmentProxy implements EntityEquipment {

		ArmorStand holder;

		ArmorStandEquipmentProxy(ArmorStand stand) {
			holder = stand;
		}

		@Override
		public Entity getHolder() {
			return holder;
		}

		@Override
		public ItemStack getItemInHand() {
			return holder.getItemInHand();
		}

		@Override
		public void setItemInHand(ItemStack itemStack) {
			holder.setItemInHand(itemStack);
		}

		@Override
		public ItemStack getHelmet() {
			return holder.getHelmet();
		}

		@Override
		public void setHelmet(ItemStack itemStack) {
			holder.setHelmet(itemStack);
		}

		@Override
		public ItemStack getChestplate() {
			return holder.getChestplate();
		}

		@Override
		public void setChestplate(ItemStack itemStack) {
			holder.setChestplate(itemStack);
		}

		@Override
		public ItemStack getLeggings() {
			return holder.getLeggings();
		}

		@Override
		public void setLeggings(ItemStack itemStack) {
			holder.setLeggings(itemStack);
		}

		@Override
		public ItemStack getBoots() {
			return holder.getBoots();
		}

		@Override
		public void setBoots(ItemStack itemStack) {
			holder.setBoots(itemStack);
		}

		@Override
		public ItemStack[] getArmorContents() {
			return new ItemStack[]{getItemInHand(), getBoots(), getLeggings(), getChestplate(), getHelmet()};
		}

		@Override
		public void setArmorContents(ItemStack[] itemStacks) {
			switch (itemStacks.length) {
				case 5:
					setHelmet(itemStacks[4]);
				case 4:
					setChestplate(itemStacks[3]);
				case 3:
					setLeggings(itemStacks[2]);
				case 2:
					setBoots(itemStacks[1]);
				case 1:
					setItemInHand(itemStacks[0]);
				case 0:
					return;
				default:
					setHelmet(itemStacks[4]);
					setChestplate(itemStacks[3]);
					setLeggings(itemStacks[2]);
					setBoots(itemStacks[1]);
					setItemInHand(itemStacks[0]);
			}
		}

		@Override
		public void clear() {
			setHelmet(null);
			setChestplate(null);
			setLeggings(null);
			setBoots(null);
			setItemInHand(null);
		}

		@Override
		public float getItemInHandDropChance() {
			return 0;
		}

		@Override
		public void setItemInHandDropChance(float v) {

		}

		@Override
		public float getHelmetDropChance() {
			return 0;
		}

		@Override
		public void setHelmetDropChance(float v) {

		}

		@Override
		public float getChestplateDropChance() {
			return 0;
		}

		@Override
		public void setChestplateDropChance(float v) {

		}

		@Override
		public float getLeggingsDropChance() {
			return 0;
		}

		@Override
		public void setLeggingsDropChance(float v) {

		}

		@Override
		public float getBootsDropChance() {
			return 0;
		}

		@Override
		public void setBootsDropChance(float v) {

		}
	}
}
