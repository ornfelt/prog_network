import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

//this class handles encrypting with AES SHA-256 algorithm
public class EncryptHandler {
	
	//my encryption variables
	private static final String ALGO = "AES";
	private static final String PASS_HASH_ALGO = "SHA-256";
	private static final String SECRET_KEY_FILE = "secretKey.key";
	
	//main method that tries to encrypt
	public static void main(String[] args) {
		
		String fileName;
		String key;
		String encryptedFileName;
		
		try {
			fileName = args[0];
			key = args[1];
			encryptedFileName = args[2];
			
			System.out.println("Getting key from: " + key);
			String keyFromFile = readFile(key);
			
			String fileContent = readFile(fileName);
			
			//list below can also be used 
			//List<String> lines = Files.readAllLines(Paths.get(key), StandardCharsets.US_ASCII);
		
			Key secretKey = buildKey(keyFromFile.toCharArray());
			
			//saveText(encryptedFileName, encrypt(secretKey, fileContent));
			printString(encryptedFileName, encrypt(secretKey, fileContent));
			
		}catch(Exception e) { e.printStackTrace(); }
	}
	
	//build key method
	private static Key buildKey(char[] password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    MessageDigest digester = MessageDigest.getInstance(PASS_HASH_ALGO);
	    digester.update(String.valueOf(password).getBytes("UTF-8"));
	    byte[] key = digester.digest();
	    SecretKeySpec spec = new SecretKeySpec(key, ALGO);
	    return spec;
	  }
	
	//read file and get string of the content
	static String readFile(String path, Charset encoding) throws IOException
	{
	System.out.println("trying to read file: " + path);
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded, encoding);
	}
	
	//read file and get string of the content
	static String readFile(String path) throws IOException
	{
	System.out.println("trying to read file: " + path);
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded);
	}
	
	public static void printString(String filePath, String text) throws IOException {
        PrintWriter textToWrite = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
        textToWrite.print(text);
        textToWrite.close();
    }
	
	//the following method saves the key to a file with incoming filename
	private static void saveText(String fileName, String text) throws IOException {
		//stream variables
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try {
			System.out.println("Generating " + fileName + "...");
			fos = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			oos.writeObject(text);
			System.out.println(fileName + " generated successfully!");
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
	
	//this method encrypts the incoming string 
	public static String encrypt(Key key, String data) throws Exception {
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(data.getBytes());
		//String encryptedValue = new BASE64Encoder().encode(encVal);
		//String encryptedValue = new String(encVal);
		String encryptedValue = Base64.getEncoder().encodeToString(encVal);
		byte[] encryptedValueByte = Base64.getEncoder().encode(encVal);
		return new String(encryptedValueByte);
	}
}
