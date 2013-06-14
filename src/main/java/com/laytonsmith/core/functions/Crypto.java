

package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.annotations.Ranged;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Set;
import org.mindrot.jbcrypt.BCrypt;

/**
 */
public class Crypto {

    public static String docs() {
        return "Provides common cryptographic functions";
    }

    @api
    public static class rot13 extends AbstractFunction implements Optimizable {

        public String getName() {
            return "rot13";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "Returns the rot13 version of val. Note that rot13(rot13(val)) returns val";
        }
		
		public Argument returnType() {
			return new Argument("Returns the rot13 value", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The string to rot13", CString.class, "val")
				);
		}

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            String s = args[0].val();
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= 'a' && c <= 'm') {
                    c += 13;
                } else if (c >= 'n' && c <= 'z') {
                    c -= 13;
                } else if (c >= 'A' && c <= 'M') {
                    c += 13;
                } else if (c >= 'A' && c <= 'Z') {
                    c -= 13;
                }
                b.append(c);
            }
            return new CString(b.toString(), t);
        }
        
        @Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "rot13('string')"),
				new ExampleScript("Basic usage", "rot13('fgevat')"),
			};
		}
		
		
    }

    @api
    public static class md5 extends AbstractFunction implements Optimizable {

        public String getName() {
            return "md5";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "Returns the md5 hash of the specified string. The md5 hash is no longer considered secure, so you should"
                    + " not use it for storage of sensitive data, however for general hashing, it is a quick and easy solution. md5 is"
                    + " a one way hashing algorithm.";
        }
		
		public Argument returnType() {
			return new Argument("The hashed value", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The string to hash", CString.class, "val")
				);
		}

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(args[0].val().getBytes());
                String hash = toHex(digest.digest()).toLowerCase();
                return new CString(hash, t);
            } catch (NoSuchAlgorithmException ex) {
                throw new ConfigRuntimeException("An error occured while trying to hash your data", ExceptionType.PluginInternalException, t, ex);
            }
        }
        
        @Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "md5('string')"),
				new ExampleScript("Basic usage", "md5('String')"),
			};
		}
    }

    @api
    public static class sha1 extends AbstractFunction implements Optimizable {

        public String getName() {
            return "sha1";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "Returns the sha1 hash of the specified string. Note that sha1 is considered more secure than md5, and is"
                    + " typically used when storing sensitive data. It is a one way hashing algorithm.";
        }
		
		public Argument returnType() {
			return new Argument("The hashed value", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The string to hash", CString.class, "val")
				);
		}

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
                digest.update(args[0].val().getBytes());
                String hash = toHex(digest.digest()).toLowerCase();
                return new CString(hash, t);
            } catch (NoSuchAlgorithmException ex) {
                throw new ConfigRuntimeException("An error occured while trying to hash your data", ExceptionType.PluginInternalException, t, ex);
            }
        }
        
        @Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "sha1('string')"),
				new ExampleScript("Basic usage", "sha1('String')"),
			};
		}
    }
    
    @api public static class sha256 extends AbstractFunction implements Optimizable {

	public String getName() {
            return "sha256";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "Returns the sha256 hash of the specified string. Note that sha256 is considered more secure than sha1 and md5, and is"
                    + " typically used when storing sensitive data. It is a one way hashing algorithm.";
        }
		
		public Argument returnType() {
			return new Argument("The hashed value", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The string to hash", CString.class, "val")
				);
		}

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return null;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                digest.update(args[0].val().getBytes());
                String hash = toHex(digest.digest()).toLowerCase();
                return new CString(hash, t);
            } catch (NoSuchAlgorithmException ex) {
                throw new ConfigRuntimeException("An error occured while trying to hash your data", ExceptionType.PluginInternalException, t, ex);
            }
        }
        
        @Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "sha256('string')"),
				new ExampleScript("Basic usage", "sha256('String')"),
			};
		}
	    
    }
    
    @api public static class bcrypt extends AbstractFunction{

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			String val = list.get("val");
			int workloadFactor = list.getInt("workloadFactor", t);
            String hash = BCrypt.hashpw(val, BCrypt.gensalt(workloadFactor));
            return new CString(hash, t);
        }

        public String getName() {
            return "bcrypt";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "Encrypts a value using bcrypt, using the specified workload, or 5 if none provided. BCrypt is supposedly more secure than SHA1, and"
                    + " certainly more secure than md5. Note that using bcrypt is slower, which is one of its security advantages, however, setting the workload to higher numbers"
                    + " will take exponentially more time. A workload of 5 is a moderate operation, which should complete in under a second, however, setting it to 10 will take"
                    + " many seconds, and setting it to 15 will take a few minutes. See the documentation for check_bcrypt for full usage.";
        }
		
		public Argument returnType() {
			return new Argument("The hashed value", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The string to hash", CString.class, "val"),
					new Argument("The workload factor", CInt.class, "workloadFactor").addAnnotation(new Ranged(1, Integer.MAX_VALUE)).setOptionalDefault(5)
				);
		}

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bcrypt('string')"),
				new ExampleScript("Basic usage", "bcrypt('String')"),
			};
		}
        
    }
    
    @api public static class check_bcrypt extends AbstractFunction{

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            boolean match = BCrypt.checkpw(args[0].val(), args[1].val());
            return new CBoolean(match, t);
        }

        public String getName() {
            return "check_bcrypt";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "Checks to see if this plaintext password does in fact hash to the hash specified. Unlike md5 or sha1, simply comparing hashes won't work. Consider the following usage:"
                    + " assign(@plain, 'plaintext') assign(@hash, bcrypt(@plain)) msg(if(check_bcrypt(@plain, @hash), 'They match!', 'They do not match!'))";
        }
		
		public Argument returnType() {
			return new Argument("true if this plaintext does hash to the given value.", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The plaintext to check", CString.class, "plaintext"),
					new Argument("The hash to compare with", CString.class, "hash")
				);
		}

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@plain, 'plaintext')\nassign(@hash, bcrypt(@plain))\n"
					+ "msg(if(check_bcrypt(@plain, @hash), 'They match!', 'They do not match!'))\n"
					+ "msg(if(check_bcrypt('notTheRightPassword', @hash), 'They match!', 'They do not match!'))"),
			};
		}
        
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
