/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Layton
 */
public class Crypto {

    @api
    public static class md5 extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {val} Returns the md5 hash of the specified string. The md5 hash is no longer considered secure, so you should"
                    + " not use it for storage of sensitive data, however for general hashing, it is a quick and easy solution. md5 is"
                    + " a one way hashing algorithm.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(args[0].val().getBytes());
                String hash = toHex(digest.digest()).toLowerCase();
                return new CString(hash, t);
            } catch (NoSuchAlgorithmException ex) {
                throw new ConfigRuntimeException("An error occured while trying to hash your data", ExceptionType.PluginInternalException, t, ex);
            }
        }

        public String getName() {
            return "md5";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }
    }

    @api
    public static class rot13 extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {val} Returns the rot13 version of val. Note that rot13(rot13(val)) returns val";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
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

        public String getName() {
            return "rot13";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }
    }

    @api
    public static class sha1 extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {val} Returns the sha1 hash of the specified string. Note that sha1 is considered more secure than md5, and is"
                    + " typically used when storing sensitive data. It is a one way hashing algorithm.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
                digest.update(args[0].val().getBytes());
                String hash = toHex(digest.digest()).toLowerCase();
                return new CString(hash, t);
            } catch (NoSuchAlgorithmException ex) {
                throw new ConfigRuntimeException("An error occured while trying to hash your data", ExceptionType.PluginInternalException, t, ex);
            }
        }

        public String getName() {
            return "sha1";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }
    }

    public static String docs() {
        return "Provides common cryptographic functions";
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
