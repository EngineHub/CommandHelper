/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.laytonsmith.core.api;
import java.math.BigInteger;

/**
 * @author Layton
 */
public class Crypto {

    public static String docs() {
        return "Provides common cryptographic functions";
    }

    @api
    public static class rot13 implements Function {

        public String getName() {
            return "rot13";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {val} Returns the rot13 version of val. Note that rot13(rot13(val)) returns val";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
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
            return new CString(b.toString(), line_num, f);
        }
    }

    @api
    public static class md5 implements Function {

        public String getName() {
            return "md5";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {val} Returns the md5 hash of the specified string. The md5 hash is no longer considered secure, so you should"
                    + " not use it for storage of sensitive data, however for general hashing, it is a quick and easy solution. md5 is"
                    + " a one way hashing algorithm.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(args[0].val().getBytes());
                String hash = toHex(digest.digest()).toLowerCase();
                return new CString(hash, line_num, f);
            } catch (NoSuchAlgorithmException ex) {
                throw new ConfigRuntimeException("An error occured while trying to hash your data", ExceptionType.PluginInternalException, line_num, f, ex);
            }
        }
    }

    @api
    public static class sha1 implements Function {

        public String getName() {
            return "sha1";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {val} Returns the sha1 hash of the specified string. Note that sha1 is considered more secure than md5, and is"
                    + " typically used when storing sensitive data. It is a one way hashing algorithm.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
                digest.update(args[0].val().getBytes());
                String hash = toHex(digest.digest()).toLowerCase();
                return new CString(hash, line_num, f);
            } catch (NoSuchAlgorithmException ex) {
                throw new ConfigRuntimeException("An error occured while trying to hash your data", ExceptionType.PluginInternalException, line_num, f, ex);
            }
        }
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
