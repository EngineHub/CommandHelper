package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.events.MCLightningStrikeEvent;
import com.laytonsmith.abstraction.events.MCThunderChangeEvent;
import com.laytonsmith.abstraction.events.MCWeatherChangeEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;

import java.util.Map;

/**
 * @author jacobwgillespie
 */
public class WeatherEvents {

    @api
    public static class lightning_strike extends AbstractEvent {

        @Override
        public String getName() {
            return "lightning_strike";
        }

        @Override
        public String docs() {
            return "{world: <string match> the world | location: <location match> the lightning strike location"
                    + " | is_effect: <boolean match> whether the strike was real or just an effect}"
                    + " Fires when lightning strikes or the lightning strike effect occurs."
                    + " {world: the name of the world in which the strike occurred | id: the lightning entityID"
                    + " | location: locationArray of the event | is_effect: the data value for the block being changed}"
                    + " {}"
                    + " {world|location|is_effect}";
        }

        @Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
            if (event instanceof MCLightningStrikeEvent) {
                MCLightningStrikeEvent e = (MCLightningStrikeEvent) event;
                Prefilters.match(prefilter, "world", e.getWorld().getName(), Prefilters.PrefilterType.MACRO);
                Prefilters.match(prefilter, "location", e.getLightning().getLocation(), Prefilters.PrefilterType.LOCATION_MATCH);
                Prefilters.match(prefilter, "is_effect", e.getLightning().isEffect(), Prefilters.PrefilterType.BOOLEAN_MATCH);
                return true;
            }
            return false;
        }

        @Override
        public BindableEvent convert(CArray manualObject, Target t) {
            return null;
        }

        @Override
        public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
            if (event instanceof MCLightningStrikeEvent) {
                MCLightningStrikeEvent e = (MCLightningStrikeEvent) event;
                Target t = Target.UNKNOWN;
                Map<String, Construct> ret = evaluate_helper(e);
                ret.put("world", new CString(e.getWorld().getName(), t));
                ret.put("id", new CString(e.getLightning().getUniqueId().toString(), t));
                ret.put("location", ObjectGenerator.GetGenerator().location(e.getLightning().getLocation()));
                ret.put("is_effect", CBoolean.GenerateCBoolean(e.getLightning().isEffect(), t));
                return ret;
            } else {
                throw new EventException("Could not convert to MCLightningStrikeEvent");
            }
        }

        @Override
        public Driver driver() {
            return Driver.LIGHTNING_STRIKE;
        }

        @Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            return false;
        }

        @Override
        public Version since() {
            return CHVersion.V3_3_1;
        }

    }

    @api
    public static class thunder_change extends AbstractEvent {

        @Override
        public String getName() {
            return "thunder_change";
        }

        @Override
        public String docs() {
            return "{world: <string match> the world | has_thunder: <boolean match> if it is thundering}"
                    + " Fires when thunder begins or ends."
                    + " {world: the name of the world in which thunder is changing | has_thunder: if the world is"
                    + " thundering}"
                    + " {}"
                    + " {world|has_thunder}";
        }

        @Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
            if (event instanceof MCThunderChangeEvent) {
                MCThunderChangeEvent e = (MCThunderChangeEvent) event;
                Prefilters.match(prefilter, "world", e.getWorld().getName(), Prefilters.PrefilterType.MACRO);
                Prefilters.match(prefilter, "has_thunder", e.toThunderState(), Prefilters.PrefilterType.BOOLEAN_MATCH);
                return true;
            }
            return false;
        }

        @Override
        public BindableEvent convert(CArray manualObject, Target t) {
            return null;
        }

        @Override
        public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
            if (event instanceof MCThunderChangeEvent) {
                MCThunderChangeEvent e = (MCThunderChangeEvent) event;
                Target t = Target.UNKNOWN;
                Map<String, Construct> ret = evaluate_helper(e);
                ret.put("world", new CString(e.getWorld().getName(), t));
                ret.put("has_thunder", CBoolean.GenerateCBoolean(e.toThunderState(), t));
                return ret;
            } else {
                throw new EventException("Could not convert to MCThunderChangeEvent");
            }
        }

        @Override
        public Driver driver() {
            return Driver.THUNDER_CHANGE;
        }

        @Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            return false;
        }

        @Override
        public Version since() {
            return CHVersion.V3_3_1;
        }

    }

    @api
    public static class weather_change extends AbstractEvent {

        @Override
        public String getName() {
            return "weather_change";
        }

        @Override
        public String docs() {
            return "{world: <string match> the world | has_rain: <boolean match> if it is raining}"
                    + " Fires when rain starts or stops."
                    + " {world: the name of the world in which the weather changed | has_rain: if it is raining}"
                    + " {}"
                    + " {world|has_rain}";
        }

        @Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
            if (event instanceof MCWeatherChangeEvent) {
                MCWeatherChangeEvent e = (MCWeatherChangeEvent) event;
                Prefilters.match(prefilter, "world", e.getWorld().getName(), Prefilters.PrefilterType.MACRO);
                Prefilters.match(prefilter, "is_effect", e.toWeatherState(), Prefilters.PrefilterType.BOOLEAN_MATCH);
                return true;
            }
            return false;
        }

        @Override
        public BindableEvent convert(CArray manualObject, Target t) {
            return null;
        }

        @Override
        public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
            if (event instanceof MCWeatherChangeEvent) {
                MCWeatherChangeEvent e = (MCWeatherChangeEvent) event;
                Target t = Target.UNKNOWN;
                Map<String, Construct> ret = evaluate_helper(e);
                ret.put("world", new CString(e.getWorld().getName(), t));
                ret.put("has_rain", CBoolean.GenerateCBoolean(e.toWeatherState(), t));
                return ret;
            } else {
                throw new EventException("Could not convert to MCWeatherChangeEvent");
            }
        }

        @Override
        public Driver driver() {
            return Driver.WEATHER_CHANGE;
        }

        @Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            return false;
        }

        @Override
        public Version since() {
            return CHVersion.V3_3_1;
        }

    }

}
