import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

/**
 * This class handles verification of data
 * @author Jonas Ornfelt
 *
 */

public class VerifyHandler {
	
	//main method that attempts to verify data
	public static void main(String[] args) {

		String fileName;
		String myPublicKey;
		String signature;
		
		try {
			//save input parameters
			fileName = args[0];
			myPublicKey = args[1];
			signature = args[2];
			
			String keyFromFile = readFile(myPublicKey);
			//get public key
			PublicKey pubKey = readPublicKeyFromFile(myPublicKey);
			//get file content
			String fileContent = readFile(fileName);
			
			MessageDigest digester = MessageDigest.getInstance("SHA-256");
		    digester.update(fileContent.getBytes());
		    byte[] content = digester.digest();
		    //get signature from file
		    byte[] mySignature = readSignature(signature);
		    //try to verify filecontent with public key and signature
		    verify(fileContent, pubKey, mySignature);
			
		}catch(Exception e) { e.printStackTrace(); }
	}
	
	//returns signature from file input
	private static byte[] readSignature(String input) throws Exception {
		
		 byte signature[] = null;
		 FileInputStream fis = new FileInputStream(input);
		 ObjectInputStream ois = new ObjectInputStream(fis);
		 Object o = ois.readObject();
		 
		 try {
		 signature = (byte []) o;
		 } catch (ClassCastException cce) {
		 cce.printStackTrace();
		 }
		 
		 return signature;
   }
	
	//this boolean returns verification status when public key is used with signature and checked against the data
	private static boolean verify(String data, PublicKey pk, byte[] signature) throws Exception {
		boolean verifies = false;
		
		try {
			 Signature s = Signature.getInstance("SHA256withDSA", "SUN");
			 s.initVerify(pk);
			 s.update(data.getBytes( ));
			 if (s.verify(signature)) {
				 verifies = true;
				 System.out.println("Message is valid");
				 System.out.println(data);
			 }else System.out.println("Message was corrupted");
		 } catch (Exception e) {
			 System.out.println(e);
			 }
		return verifies;
	}
	
	//read file and get string of the content
	private static String readFile(String path) throws IOException {
	System.out.println("trying to read file: " + path);
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded);
	}
	
	//this reads public key from the created file
	public static PublicKey readPublicKeyFromFile(String fileName) throws IOException {
		FileInputStream fileStream = null;
		ObjectInputStream ois = null;
		PublicKey pubKey = null;
		
		try {
			fileStream = new FileInputStream(fileName);
			 ois = new ObjectInputStream(fileStream);
			 DSAPublicKeySpec ks = new DSAPublicKeySpec(
			 (BigInteger) ois.readObject(),
			 (BigInteger) ois.readObject(),
			 (BigInteger) ois.readObject(),
			 (BigInteger) ois.readObject());
			 KeyFactory kf = KeyFactory.getInstance("DSA");
			pubKey = kf.generatePublic(ks);
			System.out.println("Got public key!");
			
		}catch(Exception ex) { ex.printStackTrace(); }
		finally {
			if(ois != null) {
				ois.close();
				if(fileStream != null) {
					fileStream.close();
				}
			}
		}
		return pubKey;
	}
}
