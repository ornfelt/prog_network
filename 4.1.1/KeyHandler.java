import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.spec.SecretKeySpec;

//this class handles encryption/decryption keys
public class KeyHandler {
	
	private static final String ALGO = "AES";
	private static final String PASS_HASH_ALGO = "SHA-256";
	//private static final String SECRET_KEY_FILE = "secretKey.key";
	
	//main method that creates key from user input
	public static void main(String[] args) {
		String key;
		try {
			key = args.length > 0 ? args[0] : "MySecretKey";
			System.out.println("Creating key from: " + key);
			char[] keyChars = key.toCharArray();
			
			Key secretKey = buildKey(keyChars);
			System.out.println("secretKey: " + secretKey.toString());
			
			printString(key, key);
			//saveKey(key, key);
			
		}catch(Exception e) { e.printStackTrace(); }
	}
	
	//build key via char array of password
	private static Key buildKey(char[] password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    MessageDigest digester = MessageDigest.getInstance(PASS_HASH_ALGO);
	    digester.update(String.valueOf(password).getBytes("UTF-8"));
	    byte[] key = digester.digest();
	    SecretKeySpec spec = new SecretKeySpec(key, ALGO);
	    return spec;
	  }
	
	//this seems to be working better than saveKey below
	public static void printString(String filePath, String text) throws IOException {
		
		/*
		if(!filePath.contains(".key")) {
			filePath += ".key";
		}
		*/
		
        PrintWriter textToWrite = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
        textToWrite.print(text);
        textToWrite.close();
    }
	
	//the following method saves the key to a file with incoming filename
	private static void saveKey(String fileName, String key) throws IOException {
		//stream variables
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		/*
		if(!fileName.contains(".key")) {
			fileName += ".key";
		}
		*/
		
		try {
			System.out.println("Generating " + fileName + "...");
			fos = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			oos.writeObject(key);
			System.out.println(fileName + " generated successfully! Key: " + key);
			
		}catch(Exception e ) { e.printStackTrace(); }
		finally {
			if(oos != null) {
				oos.close();
				if(fos != null) {
					fos.close();
				}
			}
		}
	}
}
