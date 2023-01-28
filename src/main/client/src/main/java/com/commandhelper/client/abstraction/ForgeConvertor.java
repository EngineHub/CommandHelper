package com.commandhelper.client.abstraction;

import com.laytonsmith.abstraction.AbstractConvertor;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@convert(type = Implementation.Type.FORGE_CLIENT)
public class ForgeConvertor extends AbstractConvertor {

	@Override
	public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Class GetServerEventMixin() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEnchantment[] GetEnchantmentValues() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEnchantment GetEnchantmentByName(String name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCServer GetServer() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCItemStack GetItemStack(MCMaterial type, int qty) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCItemStack GetItemStack(String type, int qty) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCPotionData GetPotionData(MCPotionType type, boolean extended, boolean upgraded) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCAttributeModifier GetAttributeModifier(MCAttribute attr, UUID id, String name, double amt, MCAttributeModifier.Operation op, MCEquipmentSlot slot) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void Startup(CommandHelperPlugin chp) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCMaterial[] GetMaterialValues() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCMaterial GetMaterialFromLegacy(String name, int data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCMaterial GetMaterialFromLegacy(int id, int data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCMaterial GetMaterial(String name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCMetadataValue GetMetadataValue(Object value, MCPlugin plugin) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEntity GetCorrectEntity(MCEntity e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCItemMeta GetCorrectMeta(MCItemMeta im) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MCEntity> GetEntitiesAt(MCLocation loc, double radius) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventory GetEntityInventory(MCEntity entity) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventory GetLocationInventory(MCLocation location) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventoryHolder CreateInventoryHolder(String id, String title) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCNote GetNote(int octave, MCTone tone, boolean sharp) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCColor GetColor(int red, int green, int blue) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCColor GetColor(String colorName, Target t) throws CREFormatException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCPattern GetPattern(MCDyeColor color, MCPatternShape shape) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCFireworkBuilder GetFireworkBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCPluginMeta GetPluginMeta() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCRecipe GetNewRecipe(String key, MCRecipeType type, MCItemStack result) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCRecipe GetRecipe(MCRecipe unspecific) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String GetPluginName() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCPlugin GetPlugin() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String GetUser(Environment env) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
