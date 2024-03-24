package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.compiler.signature.SignatureBuilder;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 */
public class Easings {
	public static String docs() {
		return "Easing related functions. Easings are based on the easings listed at http://easings.net, with"
				+ " the addition of the LINEAR easing, which just returns x.";
	}

	@api
	public static class easing extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
			};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			com.laytonsmith.core.Easings.EasingType type
					= ArgumentValidation.getEnum(args[0], com.laytonsmith.core.Easings.EasingType.class, t);
			double x = ArgumentValidation.getDouble(args[1], t);
			double ret = com.laytonsmith.core.Easings.GetEasing(type, x);
			return new CDouble(ret, Target.UNKNOWN);
		}

		@Override
		public String getName() {
			return "easing";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "double {EasingType type, double x} Given an easing type, and a duration percentage x, returns the"
					+ " given resulting interpolation value. ----"
					+ " Easing type may be one of " + StringUtils.Join(com.laytonsmith.core.Easings.EasingType.values(), ", ", ", or ")
					+ " and x must be a double between 0 and 1, or it is clamped. The return value may be less than"
					+ " zero or above one, depending on the easing algorithm. See http://easings.net/ for visual"
					+ " examples.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CACHE_RETURN,
					OptimizationOption.NO_SIDE_EFFECTS,
					OptimizationOption.CONSTANT_OFFLINE);
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CDouble.TYPE)
					.param(CClassType.getForEnum(com.laytonsmith.core.Easings.EasingType.class),
							"type", "The easing type.")
					.param(CDouble.TYPE, "x", "The duration percentage.")
					.build();
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with LINEAR interpolation.", "for(@x = 0, @x <= 1, @x += 0.1) {\n"
						+ "\tmsg(@x . ': ' . easing('LINEAR', @x));\n"
						+ "}"),
				new ExampleScript("Usage with EASE_IN_SINE interpolation.", "for(@x = 0, @x <= 1, @x += 0.1) {\n"
						+ "\tmsg(@x . ': ' . easing('EASE_IN_SINE', @x));\n"
						+ "}"),
			};
		}
	}

	@api
	@seealso({easing.class})
	public static class ease_between_loc extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
			};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CArray start = ArgumentValidation.getArray(args[0], t);
			double startX = ArgumentValidation.getDouble(start.get("x", t), t);
			double startY = ArgumentValidation.getDouble(start.get("y", t), t);
			double startZ = ArgumentValidation.getDouble(start.get("z", t), t);
			CArray finish = ArgumentValidation.getArray(args[1], t);
			double finishX = ArgumentValidation.getDouble(finish.get("x", t), t);
			double finishY = ArgumentValidation.getDouble(finish.get("y", t), t);
			double finishZ = ArgumentValidation.getDouble(finish.get("z", t), t);
			com.laytonsmith.core.Easings.EasingType type
					= ArgumentValidation.getEnum(args[2], com.laytonsmith.core.Easings.EasingType.class, t);
			double x = ArgumentValidation.getDouble(args[3], t);
			double percentage = com.laytonsmith.core.Easings.GetEasing(type, x);
			CArray result = new CArray(Target.UNKNOWN);
			result.set("x", startX + (finishX - startX) * percentage);
			result.set("y", startY + (finishY - startY) * percentage);
			result.set("z", startZ + (finishZ - startZ) * percentage);
			return result;
		}

		@Override
		public String getName() {
			return "ease_between_loc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{4};
		}

		@Override
		public String docs() {
			return "array {array start, array finish, EasingType type, double x} Given an easing type, and a"
					+ " duration percentage x, returns the"
					+ " given resulting interpolated distance. ----"
					+ " For instance, given the location arrays representing x: 0 and x: 100, with a LINEAR"
					+ " easing and 0.25 duration, a location array of x: 25 would be returned."
					+ " Easing type may be one of " + StringUtils.Join(com.laytonsmith.core.Easings.EasingType.values(), ", ", ", or ")
					+ " and x must be a double between 0 and 1, or it is clamped. The return value may be less than"
					+ " zero or above one, depending on the easing algorithm. See http://easings.net/ for visual"
					+ " examples. The start and finish arrays must contain x, y, and z parameters, but don't necessarily"
					+ " have to represent locations.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CArray.TYPE)
					.param(CArray.TYPE, "start", "The starting position.")
					.param(CArray.TYPE, "finish", "The ending position.")
					.param(CClassType.getForEnum(com.laytonsmith.core.Easings.EasingType.class), "type", "The easing type.")
					.param(CDouble.TYPE, "x", "The duration percentage.")
					.build();
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[] {
				new ExampleScript("Usage with a LINEAR interpolation.", "@start = array(x: 0, y: 0, z: 0);\n"
						+ "@finish = array(x: 0, y: 100, z: 0);\n"
						+ "for(@i = 0, @i <= 1, @i += 0.1) {\n"
						+ "\tmsg(ease_between_loc(@start, @finish, 'LINEAR', @i);\n"
						+ "}"),
				new ExampleScript("Usage with a EASE_IN_CUBIC interpolation.", "@start = array(x: 0, y: 0, z: 0);\n"
						+ "@finish = array(x: 0, y: 100, z: 0);\n"
						+ "for(@i = 0, @i <= 1, @i += 0.1) {\n"
						+ "\tmsg(ease_between_loc(@start, @finish, 'EASE_IN_CUBIC', @i);\n"
						+ "}")
			};
		}
	}

//	@api
//	public static class ease_between_time extends AbstractFunction {
//
//		@Override
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[]{
//				CRECastException.class,
//			};
//		}
//
//		@Override
//		public boolean isRestricted() {
//			return false;
//		}
//
//		@Override
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
//			CArray start = ArgumentValidation.getArray(args[0], t);
//			double startX = ArgumentValidation.getDouble(start.get("x", t), t);
//			double startY = ArgumentValidation.getDouble(start.get("y", t), t);
//			double startZ = ArgumentValidation.getDouble(start.get("z", t), t);
//			CArray finish = ArgumentValidation.getArray(args[1], t);
//			double finishX = ArgumentValidation.getDouble(finish.get("x", t), t);
//			double finishY = ArgumentValidation.getDouble(finish.get("y", t), t);
//			double finishZ = ArgumentValidation.getDouble(finish.get("z", t), t);
//			com.laytonsmith.core.Easings.EasingType type
//					= ArgumentValidation.getEnum(args[2], com.laytonsmith.core.Easings.EasingType.class, t);
//			long timeMs = ArgumentValidation.getInt(args[3], t);
//			long framerateMs = ArgumentValidation.getInt(args[4], t);
//			Callable callback = ArgumentValidation.getObject(args[5], t, Callable.class);
//			if(framerateMs <= 0) {
//				throw new CRERangeException("framerateMs must be greater than 0.", t);
//			}
//			if(framerateMs > timeMs) {
//				throw new CRERangeException("framerateMs must be less than or equal to timeMS.", t);
//			}
//			long startTime = System.currentTimeMillis();
//			final AtomicInteger step = new AtomicInteger(0);
//			final MutableObject<Runnable> r = new MutableObject<>();
//
//			r.setObject(() -> {
//				int s = step.getAndIncrement();
//				long expectedTime = ((s + 1) * framerateMs) + startTime;
//
//				StaticLayer.SetFutureRunnable(env.getEnv(StaticRuntimeEnv.class).GetDaemonManager(), 0, r.getObject());
//			});
//			StaticLayer.SetFutureRunnable(env.getEnv(StaticRuntimeEnv.class).GetDaemonManager(), 0, r.getObject());
//			return CVoid.VOID;
//		}
//
//		@Override
//		public String getName() {
//			return "ease_between_time";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{6};
//		}
//
//		@Override
//		public String docs() {
//			return "void {array start, array finish, EasingType type, int timeMs, int framerateMs, Callable<int, array> callback}"
//					+ " Runs the callback at the appropriate time with the given easing, such that the animation"
//					+ " completes in the given number of ms. ----"
//					+ " Easing type may be one of " + StringUtils.Join(com.laytonsmith.core.Easings.EasingType.values(), ", ", ", or ")
//					+ ". See http://easings.net/ for visual"
//					+ " examples. The start and finish arrays must contain x, y, and z parameters, but don't necessarily"
//					+ " have to represent locations. timeMs represents the total time the process should run for,"
//					+ " and framerateMs represents how often it should \"tick\". The callback will receive the"
//					+ " interpolated location array. If the callback returns -1, the animation will halt. Note that"
//					+ " the framerate is lower priority than the total time, that is, if the animation is running"
//					+ " behind, the framerate will be shortened, to ensure that the total time of the animation is"
//					+ " completed by the time timeMs has elapsed. framerateMs must be greater than 0, and less than"
//					+ " timeMs. If framerateMs is not a multiple of timeMs, the last frame is truncated.";
//		}
//
//		@Override
//		public Version since() {
//			return MSVersion.V3_3_5;
//		}
//
//		@Override
//		public FunctionSignatures getSignatures() {
//			return new SignatureBuilder(CVoid.TYPE)
//					.param(CArray.TYPE, "start", "The starting position.")
//					.param(CArray.TYPE, "finish", "The ending position.")
//					.param(CClassType.getForEnum(com.laytonsmith.core.Easings.EasingType.class), "type", "The easing type.")
//					.param(CInt.TYPE, "timeMs", "The total time of the animation.")
//					.param(CInt.TYPE, "framerateMs", "The time between frames.")
//					.param(Callable.TYPE, "callback", "The callback that receives the interpolated location.")
//					.build();
//		}
//
//		@Override
//		public ExampleScript[] examples() throws ConfigCompileException {
//			return super.examples(); //To change body of generated methods, choose Tools | Templates.
//		}
//	}
}
