package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CSecureString;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 */
@core
public class Crypto {

	public static String docs() {
		return "Provides common cryptographic functions. Many functions in this class are aware of and compatible"
				+ " with secure_string (where specified in the function documentation). In these cases, if the"
				+ " argument passed in is a secure_string, it is first decrypted and the underlying string is used"
				+ " rather than the default string value \"**secure string**\".";
	}

	private static CString getHMAC(String algorithm, Target t, Mixed[] args) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(args[0].val().getBytes(), algorithm);
			Mac mac = Mac.getInstance(algorithm);
			mac.init(signingKey);
			byte[] hmac = mac.doFinal(getByteArrayFromArg(args[1]));
			String hash = StringUtils.toHex(hmac).toLowerCase();
			return new CString(hash, t);
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			throw new CREPluginInternalException("An error occured while trying to hash your data", t, ex);
		}
	}

	private static byte[] getByteArrayFromArg(Mixed c) {
		byte[] val;
		if(c.isInstanceOf(CSecureString.TYPE)) {
			val = ArrayUtils.charToBytes(((CSecureString) c).getDecryptedCharArray());
		} else {
			val = c.val().getBytes();
		}
		return val;
	}

	@api
	public static class rot13 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "rot13";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {val} Returns the rot13 version of val. Note that rot13(rot13(val)) returns val";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String s = args[0].val();
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if(c >= 'a' && c <= 'm') {
					c += 13;
				} else if(c >= 'n' && c <= 'z') {
					c -= 13;
				} else if(c >= 'A' && c <= 'M') {
					c += 13;
				} else if(c >= 'A' && c <= 'Z') {
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
				new ExampleScript("Basic usage", "rot13('fgevat')")};
		}

	}

	@api
	public static class md5 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "md5";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {val} Returns the md5 hash of the specified string. The md5 hash is no longer considered secure, so you should"
					+ " not use it for storage of sensitive data, however for general hashing, it is a quick and easy solution. md5 is"
					+ " a one way hashing algorithm. This function is aware of and compatible with secure_string.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				byte[] val = getByteArrayFromArg(args[0]);
				MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
				digest.update(val);
				String hash = StringUtils.toHex(digest.digest()).toLowerCase();
				return new CString(hash, t);
			} catch (NoSuchAlgorithmException ex) {
				throw new CREPluginInternalException("An error occured while trying to hash your data", t, ex);
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
				new ExampleScript("Basic usage", "md5('String')")};
		}
	}

	@api
	public static class sha1 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "sha1";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {val} Returns the sha1 hash of the specified string. Note that sha1 is considered more secure than md5,"
					+ " but is also not considered secure. sha-256 should be used instead for storing sensitive"
					+ " data. It is a one way hashing algorithm. This function is aware of and compatible with"
					+ " secure_string.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				byte[] val = getByteArrayFromArg(args[0]);
				MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
				digest.update(val);
				String hash = StringUtils.toHex(digest.digest()).toLowerCase();
				return new CString(hash, t);
			} catch (NoSuchAlgorithmException ex) {
				throw new CREPluginInternalException("An error occured while trying to hash your data", t, ex);
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
				new ExampleScript("Basic usage", "sha1('String')")};
		}
	}

	@api
	public static class sha256 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "sha256";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {val} Returns the sha256 hash of the specified string. Note that sha256 is considered more secure than sha1 and md5, and is"
					+ " typically used when storing sensitive data. It is a one way hashing algorithm. This function"
					+ " is aware of and compatible with secure_string.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				byte[] val = getByteArrayFromArg(args[0]);
				MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
				digest.update(val);
				String hash = StringUtils.toHex(digest.digest()).toLowerCase();
				return new CString(hash, t);
			} catch (NoSuchAlgorithmException ex) {
				throw new CREPluginInternalException("An error occured while trying to hash your data", t, ex);
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
				new ExampleScript("Basic usage", "sha256('String')")};
		}

	}

	@api
	public static class sha512 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "sha512";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {val} Returns the sha512 hash of the specified string. Note that sha512"
					+ " is considered more secure than sha1 and md5 (and sha256, because it takes longer to calculate),"
					+ " and is typically used when storing sensitive data. It is a one way hashing algorithm. This"
					+ " function is aware of and compatible with secure_string.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				byte[] val = getByteArrayFromArg(args[0]);
				MessageDigest digest = java.security.MessageDigest.getInstance("SHA-512");
				digest.update(val);
				String hash = StringUtils.toHex(digest.digest()).toLowerCase();
				return new CString(hash, t);
			} catch (NoSuchAlgorithmException ex) {
				throw new CREPluginInternalException("An error occured while trying to hash your data", t, ex);
			}
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					Optimizable.OptimizationOption.CONSTANT_OFFLINE,
					Optimizable.OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "sha512('string')"),
				new ExampleScript("Basic usage", "sha512('String')")};
		}

	}

	@api
	public static class bcrypt extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int log_rounds = 5;
			if(args.length == 2) {
				log_rounds = Static.getInt32(args[1], t);
			}
			try {
				String val;
				if(args[0].isInstanceOf(CSecureString.TYPE)) {
					val = new String(((CSecureString) args[0]).getDecryptedCharArray());
				} else {
					val = args[0].val();
				}
				String hash = BCrypt.hashpw(val, BCrypt.gensalt(log_rounds));
				return new CString(hash, t);
			} catch (IllegalArgumentException ex) {
				throw new CRERangeException(ex.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "bcrypt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "string {val, [workload]} Encrypts a value using bcrypt, using the specified workload, or 5 if none"
					+ " provided. BCrypt is supposedly more secure than SHA1, and"
					+ " certainly more secure than md5. Note that using bcrypt is slower, which is one of its security"
					+ " advantages, however, setting the workload to higher numbers"
					+ " will take exponentially more time. A workload of 5 is a moderate operation, which should"
					+ " complete in under a second, however, setting it to 10 will take"
					+ " many seconds, and setting it to 15 will take a few minutes. The workload must be between 5"
					+ " and 31. See the documentation for check_bcrypt for full usage. Bcrypt is recommended for"
					+ " password hashing, whereas sha-* functions are not. This function is aware of and compatible"
					+ " with secure_string.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bcrypt('string')", ":$2a$05$aBMYDJAu6C3O.142N/n7yO6Dl3KC0L/zHUEZnOXQuaX13XUKec8Gy"),
				new ExampleScript("Basic usage", "bcrypt('String')", ":$2a$05$jYm.4yath40V2DqjipWSje3Ed0ZNLO8IcDjIF50PJoPvWSmF1J7L2")};
		}

	}

	@api
	public static class check_bcrypt extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String val;
			if(args[0].isInstanceOf(CSecureString.TYPE)) {
				val = new String(((CSecureString) args[0]).getDecryptedCharArray());
			} else {
				val = args[0].val();
			}
			return CBoolean.get(BCrypt.checkpw(val, args[1].val()));
		}

		@Override
		public String getName() {
			return "check_bcrypt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {plaintext, hash} Checks to see if this plaintext password does in fact hash to the hash"
					+ " specified. Unlike md5 or sha1, simply comparing hashes won't work."
					+ " This function is aware of and compatible with secure_string.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@plain, 'plaintext')\nassign(@hash, bcrypt(@plain))\n"
				+ "msg(if(check_bcrypt(@plain, @hash), 'They match!', 'They do not match!'))\n"
				+ "msg(if(check_bcrypt('notTheRightPassword', @hash), 'They match!', 'They do not match!'))")};
		}

	}

	@api
	public static class base64_encode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CByteArray ba = Static.getByteArray(args[0], t);
			byte[] data = ba.asByteArrayCopy();
			data = Base64.encodeBase64(data);
			return CByteArray.wrap(data, t);
		}

		@Override
		public String getName() {
			return "base64_encode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "byte_array {byteData} Encodes the given byte_array data into a base 64 byte_array.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "string_from_bytes(base64_encode(string_get_bytes('A string')))")
			};
		}

	}

	@api
	public static class base64_decode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CByteArray ba = Static.getByteArray(args[0], t);
			byte[] data = ba.asByteArrayCopy();
			data = Base64.decodeBase64(data);
			return CByteArray.wrap(data, t);
		}

		@Override
		public String getName() {
			return "base64_decode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "byte_array {base64data} Decodes the base 64 encoded byte_array data back into the original byte_array data.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "string_from_bytes(base64_decode(string_get_bytes('QSBzdHJpbmc=')))")
			};
		}

	}

	@api
	public static class hmac_md5 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "hmac_md5";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "string {key, val} Returns the md5 HMAC of the specified string using the provided key. This function"
					+ " is aware of and compatible with secure_string.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return getHMAC("HmacMD5", t, args);
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
				new ExampleScript("Basic usage", "hmac_md5('secret_key', 'string')")};
		}
	}

	@api
	public static class hmac_sha1 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "hmac_sha1";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "string {key, val} Returns the sha1 HMAC of the specified string using the provided key. This function"
					+ " is aware of and compatible with secure_string.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return getHMAC("HmacSHA1", t, args);
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
				new ExampleScript("Basic usage", "hmac_sha1('secret_key', 'string')")};
		}
	}

	@api
	public static class hmac_sha256 extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "hmac_sha256";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "string {key, val} Returns the sha256 HMAC of the specified string using the provided key. This"
					+ " function is aware of and compatible with secure_string.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return getHMAC("HmacSHA256", t, args);
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
				new ExampleScript("Basic usage", "hmac_sha256('secret_key', 'string')")};
		}
	}

}
