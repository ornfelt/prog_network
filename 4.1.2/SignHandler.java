import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.spec.SecretKeySpec;

/**
 * This class handles signing data
 * @author Jonas Ornfelt
 *
 */

public class SignHandler {

	//main method that attempts to sign file
	public static void main(String[] args) {
		
		String fileName;
		String myPrivatekey;
		String signature;
		
		try {
			//save parameters as variables
			fileName = args[0];
			myPrivatekey = args[1];
			signature = args[2];
			//get private key
			PrivateKey privateKey = readPrivateKeyFromFile(myPrivatekey);
			//get filecontent
			String fileContent = readFile(fileName);
			//sign the file
			signFile(fileContent, privateKey, signature);
			
		}catch(Exception e) { e.printStackTrace(); }
	}
	
	//sign data with given privatekey and save signature in incoming file name
	private static void signFile(String data, PrivateKey pk, String signatureFileName) {
		try {
			 FileOutputStream fos = new FileOutputStream(signatureFileName);
			 ObjectOutputStream oos = new ObjectOutputStream(fos);
			 Signature s = Signature.getInstance("SHA256withDSA", "SUN");
			 
			 s.initSign(pk);
			 byte buf[] = data.getBytes();
			 s.update(buf);
			 System.out.println("buf : " + new String(data));
			 //sign and save
			 oos.writeObject(s.sign());
			 } catch (Exception e) {
			 e.printStackTrace( );
			 }
	}
	
	//get privatekey from file
	public static PrivateKey readPrivateKeyFromFile(String fileName) throws IOException {
		PrivateKey pk = null;
		FileInputStream fileStream = null;
		ObjectInputStream ois = null;
		
		try {
			fileStream = new FileInputStream(fileName);
			 ois = new ObjectInputStream(fileStream);
			 DSAPrivateKeySpec ks = new DSAPrivateKeySpec(
			 (BigInteger) ois.readObject(),
			 (BigInteger) ois.readObject(),
			 (BigInteger) ois.readObject(),
			 (BigInteger) ois.readObject());
			 KeyFactory kf = KeyFactory.getInstance("DSA");
			 pk = kf.generatePrivate(ks);
			 System.out.println("Got private key!");
			 } catch (Exception e) {
			 e.printStackTrace( );
			 }
		finally {
			if(ois != null) {
				ois.close();
				if(fileStream != null) {
					fileStream.close();
				}
			}
		}
		return pk;
	}
		
	//read file and get string of the content
	private static String readFile(String path) throws IOException {
	System.out.println("trying to read file: " + path);
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded);
	}
	
	//this method saves the incoming signature string to the file called filePath
	private static void saveSignatureToFile(String filePath, String sig) throws IOException {
		PrintWriter textToWrite = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
        textToWrite.print(sig);
        textToWrite.close();
	}
}
