package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author cailin
 */
@typeof("secure_string")
public class CSecureString extends CString {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final CClassType TYPE = CClassType.get("secure_string");

    private byte[] encrypted;
    private Cipher decrypter;
    private int encLength;

    public CSecureString(char[] val, Target t) {
	super("**secure string**", t);
	construct(ArrayUtils.charToBytes(val));
    }

    public CSecureString(CArray val, Target t) {
	super("**secure string**", t);
	construct(CArrayToByteArray(val, t));
    }

    private void construct(byte[] val) {
	try {
	    SecureRandom rand = SecureRandom.getInstanceStrong();
	    byte[] keyBytes = new byte[128];
	    rand.nextBytes(keyBytes);
	    byte[] ivBytes = new byte[128];
	     SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
	    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
	    Cipher encrypter = Cipher.getInstance("DES/CBC/PKCS5Padding");
	    decrypter = Cipher.getInstance("DES/CBC/PKCS5Padding");
	    encrypter.init(Cipher.ENCRYPT_MODE, key, ivSpec);
	    decrypter.init(Cipher.DECRYPT_MODE, key, ivSpec);
	    encrypted = new byte[encrypter.getOutputSize(val.length)];
	    encLength = encrypter.update(val, 0, val.length, encrypted, 0);
	    encLength += encrypter.doFinal(encrypted, encLength);
	} catch (NoSuchAlgorithmException | NoSuchPaddingException
		| InvalidKeyException | InvalidAlgorithmParameterException
		| ShortBufferException | IllegalBlockSizeException
		| BadPaddingException ex) {
	    throw new RuntimeException(ex);
	}
    }

    private static byte[] CArrayToByteArray(CArray val, Target t) {
	List<Byte> cval = new ArrayList<>((int) val.size());
	if (val.isAssociative()) {
	    throw new CREFormatException("Expected a normal array in secure string, but an associative one was passed in", t);
	}
	for (int i = 0; i < val.size(); i++) {
	    String c = val.get(i, t).val();
	    if (c.length() != 1) {
		throw new CREFormatException("The array passed in must be an array of single character strings", t);
	    }
	    for(byte b : c.getBytes()) {
		cval.add(b);
	    }
	}
	return ArrayUtils.unbox(cval.toArray(new Byte[cval.size()]));
    }

    public char[] getDecryptedCharArray() {
	try {
	    byte[] decrypted = new byte[decrypter.getOutputSize(encLength)];
	    int decLen = decrypter.update(encrypted, 0, encLength, decrypted, 0);
	    decrypter.doFinal(decrypted, decLen);
	    return ArrayUtils.bytesToChar(decrypted);
	} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException ex) {
	    throw new RuntimeException(ex);
	}
    }

    @Override
    public String docs() {
	return "A secure_string is a string which cannot normally be toString'd, and whose underlying representation"
		+ " is encrypted in memory. This should be used for storing passwords or other sensitive data which"
		+ " should in no cases be stored in plain text. Since this extends string, it can generally be used in"
		+ " place of a string, and when done so, cannot accidentally be exposed (via logs or exception messages,"
		+ " or other accidental exposure) unless it is specifically instructed to decrypt and switch to a char"
		+ " array. While this cannot by itself ensure security of the value, it can help prevent most accidental"
		+ " exposures of data by intermediate code. When exported as a string (or imported as a string) other"
		+ " code must be written to ensure safety of those systems.";
    }

    @Override
    public Version since() {
	return CHVersion.V3_3_2;
    }

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{CString.TYPE};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
