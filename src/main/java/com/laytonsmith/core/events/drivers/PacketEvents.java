package com.laytonsmith.core.events.drivers;

import com.comphenix.protocol.PacketType;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractGenericEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.prefilters.CustomPrefilterMatcher;
import com.laytonsmith.core.events.prefilters.EnumPrefilterMatcher;
import com.laytonsmith.core.events.prefilters.PlayerPrefilterMatcher;
import com.laytonsmith.core.events.prefilters.Prefilter;
import com.laytonsmith.core.events.prefilters.PrefilterBuilder;
import com.laytonsmith.core.events.prefilters.StringPrefilterMatcher;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.packetjumper.Comparisons;
import com.laytonsmith.core.packetjumper.Conversions;
import com.laytonsmith.core.packetjumper.PacketDirection;
import com.laytonsmith.core.packetjumper.PacketJumper;
import com.laytonsmith.core.packetjumper.PacketUtils;
import com.laytonsmith.core.packetjumper.ProtocolLibPacketEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.mappingio.tree.MappingTree;

/**
 *
 */
public final class PacketEvents {

	private PacketEvents() {
	}

	public static String docs() {
		return "Contains events related to packet management. PacketJumper must be enabled for these events to fire.";
	}

	private static PrefilterBuilder GetPacketPrefilter() {
		return new PrefilterBuilder()
				.set("protocol", "The protocol of the packet.", new StringPrefilterMatcher<ProtocolLibPacketEvent>() {
					@Override
					protected String getProperty(ProtocolLibPacketEvent event) {
						return event.getPacketEvent().getPacketType().getProtocol().name();
					}
				})
				.set("type", "The packet type.", new StringPrefilterMatcher<ProtocolLibPacketEvent>() {
					@Override
					protected String getProperty(ProtocolLibPacketEvent event) {
						return event.getPacketEvent().getPacketType().name();
					}
				})
				.set("direction", "The packet direction.", new EnumPrefilterMatcher<ProtocolLibPacketEvent, PacketDirection>(PacketDirection.class) {
					@Override
					protected Enum<PacketDirection> getEnum(ProtocolLibPacketEvent event) {
						return event.getPacketEvent().getPacketType().getSender() == PacketType.Sender.CLIENT
								? PacketDirection.IN : PacketDirection.OUT;
					}
				})
				.set("player", "The player that the packet is being sent to/from", new PlayerPrefilterMatcher())
				.set("values", "An associative array of value matches."
						+ " The key should be the field name to match, and the value should be the desired match."
						+ " This only supports exact matches.", new CustomPrefilterMatcher<ProtocolLibPacketEvent>() {
					@Override
					public boolean matches(String key, Mixed value, ProtocolLibPacketEvent event, Target t) {
						if(!(value instanceof CArray)) {
							throw new CREIllegalArgumentException("\"values\" prefilter must be an array.", t);
						}
						Object packet = event.getInternalPacket().getHandle();
						MappingTree tree = PacketJumper.GetMappingTree();
						MappingTree.ClassMapping classMapping
								= tree.getClass(event.getPacketEvent().getPacketType().getPacketClass()
										.getName().replace(".", "/"), PacketJumper.GetServerNamespace());
						if(classMapping == null) {
							throw new CREPluginInternalException("Cannot find packet class.", t);
						}
						for(Mixed k : ((ArrayAccess) value).keySet()) {
							Class clazz = event.getPacketEvent().getPacketType().getPacketClass();
							MappingTree.FieldMapping fm = null;
							do {
								fm = classMapping.getField(k.val(), null, PacketJumper.GetMojangNamespace());
								if(fm != null) {
									break;
								}
								clazz = clazz.getSuperclass();
								if(clazz == Object.class || clazz == Record.class) {
									break;
								}
								classMapping = tree.getClass(clazz.getName().replace(".", "/"),
										PacketJumper.GetServerNamespace());
								if(classMapping == null) {
									throw new CREPluginInternalException("Cannot find packet superclass.", t);
								}
							} while(true);
							if(fm == null) {
								throw new CREIllegalArgumentException("Invalid property \"" + k.val() + "\"", t);
							}
							String tK = fm.getSrcName();
							Mixed v = ((ArrayAccess) value).get(k, t);
							Object oV = Conversions.convertMixedToObject(v, clazz, t);
							Object packetPropertyValue = ReflectionUtils.get(clazz, packet, tK);
							if(packetPropertyValue == null) {
								if(oV != null) {
									return false;
								} else {
									continue;
								}
							}
							if(!Comparisons.IsEqual(oV, packetPropertyValue)) {
								return false;
							}
						}
						return true;
					}

					@Override
					public int getPriority() {
						return 100;
					}

				});
	}

	private static void DoCrossPrefilterValidation(Map<Prefilter<ProtocolLibPacketEvent>, ParseTree> prefilters,
			Environment env) throws ConfigCompileException, ConfigCompileGroupException {
		List<String> prefilterTypes = new ArrayList<>();
		Prefilter<ProtocolLibPacketEvent> valuesPrefilter = null;
		String protocol = null;
		String type = null;
		for(Prefilter<ProtocolLibPacketEvent> p : prefilters.keySet()) {
			prefilterTypes.add(p.getPrefilterName());
			ParseTree parseTree = prefilters.get(p);
			switch(p.getPrefilterName()) {
				case "values":
					valuesPrefilter = p;
					break;
				case "protocol":
					if(parseTree.isConst()) {
						protocol = parseTree.getData().val();
					}
					break;
				case "type":
					if(parseTree.isConst()) {
						type = parseTree.getData().val();
					}
					break;
				default:
					break;
			}
		}

		if(prefilterTypes.contains("values") && (!prefilterTypes.contains("protocol") || !prefilterTypes.contains("type"))) {
			ParseTree valuesParseTree = prefilters.get(valuesPrefilter);
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(valuesParseTree.getFileOptions(),
					new CompilerWarning("\"values\" prefilter cannot be made to work reliably without"
							+ " specifying protocol and type values, as this will match multiple packet"
							+ " types, each with different values, so this cannot be made to work generically,"
							+ " unless you happen to only be registering for a single packet type. Add a 'protocol'"
							+ " and 'type' prefilter also to get rid of this warning.",
							valuesParseTree.getTarget(), null));
			return;
		}

		if(prefilterTypes.contains("values") && protocol != null && type != null) {
			ParseTree node = prefilters.get(valuesPrefilter);
			if(node.getData() instanceof CFunction f) {
				if(f.getFunction() instanceof DataHandling.array || f.getFunction() instanceof DataHandling.associative_array) {
					Set<String> labels = new HashSet<>();
					for(int i = 0; i < node.numberOfChildren(); i++) {
						ParseTree child = node.getChildAt(i);
						if(child.getData() instanceof CFunction centryFunction
								&& centryFunction.getFunction() instanceof com.laytonsmith.core.functions.Compiler.centry) {
							labels.add(((CLabel) child.getChildAt(0).getData()).cVal().val());
						}
					}
					PacketType packetType = PacketUtils.findPacketTypeByCommonName(protocol, type, node.getTarget());
					Set<ConfigCompileException> errors = new HashSet<>();
					propertyLoop:
					for(String property : labels) {
						Class clazz = packetType.getPacketClass();
						MappingTree.FieldMapping fm = null;
						MappingTree tree = PacketJumper.GetMappingTree();
						MappingTree.ClassMapping classMapping
								= tree.getClass(clazz
										.getName().replace(".", "/"), PacketJumper.GetServerNamespace());
						if(classMapping == null) {
							throw new ConfigCompileException("Cannot find packet class.", node.getTarget());
						}
						do {
							fm = classMapping.getField(property, null, PacketJumper.GetMojangNamespace());
							if(fm != null) {
								break;
							}
							clazz = clazz.getSuperclass();
							if(clazz == Object.class || clazz == Record.class) {
								break;
							}
							classMapping = tree.getClass(clazz.getName().replace(".", "/"),
									PacketJumper.GetServerNamespace());
							if(classMapping == null) {
								errors.add(new ConfigCompileException("Cannot find packet superclass.", node.getTarget()));
								continue propertyLoop;
							}
						} while(true);
						if(fm == null) {
							errors.add(new ConfigCompileException("Invalid property \"" + property + "\"", node.getTarget()));
						}
					}
					if(!errors.isEmpty()) {
						throw new ConfigCompileGroupException(errors);
					}
				}
			}
		}

	}

	public abstract static class PacketEvent extends AbstractGenericEvent<ProtocolLibPacketEvent> {

		@Override
		public ProtocolLibPacketEvent convert(CArray manualObject, Target t) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Map<String, Mixed> evaluate(ProtocolLibPacketEvent e) throws EventException {
			Map<String, Mixed> map = evaluate_helper(e);
			map.put("macrotype", new CString("packet", Target.UNKNOWN));
			map.put("player", new CString(e.getPlayer().getName(), Target.UNKNOWN));
			map.put("packet", e.getPacket(Target.UNKNOWN));
			map.put("protocol", new CString(e.getPacketEvent().getPacketType().getProtocol().name(), Target.UNKNOWN));
			map.put("type", new CString(e.getPacketEvent().getPacketType().name(), Target.UNKNOWN));
			map.put("direction", new CString(
					PacketDirection.FromSender(e.getPacketEvent().getPacketType().getSender()).name(), Target.UNKNOWN));
			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, ProtocolLibPacketEvent event) {
			throw new CREUnsupportedOperationException("Cannot modify the packet event, use packet_write to modify"
					+ " individual parameters.", value.getTarget());
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		protected PrefilterBuilder getPrefilterBuilder() {
			return GetPacketPrefilter();
		}

		@Override
		public void validatePrefilters(Map<Prefilter<ProtocolLibPacketEvent>, ParseTree> prefilters, Environment env)
				throws ConfigCompileException, ConfigCompileGroupException {
			DoCrossPrefilterValidation(prefilters, env);
		}

		@Override
		public void cancel(ProtocolLibPacketEvent o, boolean state) {
			o.getPacketEvent().setCancelled(state);
		}
	}

	@api
	public static class packet_received extends PacketEvent {

		@Override
		public String getName() {
			return "packet_received";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fires when a packet is received from a client."
					+ " Cancelling the event will cause the packet to not be processed by the server."
					+ " {player: the player the packet was received from | type: the packet type"
					+ " | fields: an array of fields that map to values which should be handled. Missing fields"
					+ " match any value.}"
					+ " {}"
					+ " {}";
		}

		@Override
		public Driver driver() {
			return Driver.PACKET_RECEIVED;
		}
	}

	@api
	public static class packet_sent extends PacketEvent {

		@Override
		public String getName() {
			return "packet_sent";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fires when a packet is sent from the server."
					+ " Cancelling the event will cause the packet to not be sent to the client."
					+ " {player: the player the packet was received from | type: the packet type"
					+ " | fields: an array of fields that map to values which should be handled. Missing fields"
					+ " match any value.}"
					+ " {}"
					+ " {}";
		}

		@Override
		public Driver driver() {
			return Driver.PACKET_SENT;
		}
	}
}
