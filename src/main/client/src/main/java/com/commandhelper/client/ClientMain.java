package com.commandhelper.client;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.MapBuilder;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.Installer;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.telemetry.DefaultTelemetry;
import com.laytonsmith.core.telemetry.Telemetry;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ClientMain.MODID)
public class ClientMain {
	// Define mod id in a common place for everything to reference

	public static final String MODID = "commandhelperforgeclient";
	// Directly reference a slf4j logger
	private static final Logger LOGGER = LogUtils.getLogger();

	public ClientMain() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		MinecraftForge.EVENT_BUS.addListener((ClientChatEvent event) -> {
			if(event.getMessage().startsWith("/")) {
				if (event.isCancelable()) {
					event.setCanceled(true);
				}
			}
		});

		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		Implementation.setServerType(Implementation.Type.FORGE_CLIENT);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		// Some common setup code
		LOGGER.info("CommandHelperForgeClient starting up.");
		doCommonStartup();
	}

	private static Set<String> startups = new HashSet<>();

	private void doCommonStartup() {
		ClassDiscoveryCache cdc = new ClassDiscoveryCache(ForgeClientFileLocations.getDefault().getCacheDirectory());
		URL fs = null;
		if("runClient".equals(System.getProperty("commandhelper.startupMode"))) {
			try {
				// Startup happened from gradle runClient.
				fs = new File("../build/libs/commandhelperforgeclient-1.0.0.jar").toURI().toURL();
			} catch (MalformedURLException ex) {
				//
			}
		} else {
			fs = ClassDiscovery.GetClassContainer(ClientMain.class);
		}

		ClassDiscovery.getDefaultInstance().setClassDiscoveryCache(cdc);
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(fs);
		ClassDiscovery.getDefaultInstance().getKnownClasses();
		ForgeConvertor conv;
		StaticLayer.ClearAllRunnables();

//		ExtensionManager.Initialize(ClassDiscovery.getDefaultInstance());
	}

	private void doStartup(String ip) {
		if(startups.contains(ip)) {
			return;
		}

		try {
			Prefs.init(ForgeClientFileLocations.getDefault().getPreferencesFile());
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return;
		}

		Prefs.SetColors();
		Installer.Install(ForgeClientFileLocations.getDefault().getConfigDirectory());

		MSLog.initialize(ForgeClientFileLocations.getDefault().getConfigDirectory());

		Telemetry.GetDefault().initializeTelemetry();
		Telemetry.GetDefault().doNag();
		Telemetry.GetDefault().log(DefaultTelemetry.StartupModeMetric.class,
				MapBuilder.start("mode", Implementation.GetServerType().getBranding()), null);

		startups.add(ip);
	}

	private AliasCore ac;

	@SubscribeEvent
	public void serverLifecycleEvent(ServerLifecycleEvent event) throws Exception {
		LOGGER.info("*********" + event.getServer().getClass() + " " + event.getServer().getLocalIp());
		if(event instanceof ServerAboutToStartEvent) {
			doStartup(event.getServer().getLocalIp());
		} else if(event instanceof ServerStartingEvent) {
			ac = new AliasCore(ForgeClientFileLocations.getDefault());
		} else if(event instanceof ServerStoppingEvent) {
			startups.remove(event.getServer().getLocalIp());
		}
	}

	@SubscribeEvent
	public void loggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
		LOGGER.info(event.getClass().toString());
		ForgeEnvironment forgeEnv = new ForgeEnvironment();
		ac.reload(forgeEnv.getPlayer(), null, forgeEnv, true);
	}


}
