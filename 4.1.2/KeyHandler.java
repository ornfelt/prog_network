import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * This class handles keys for signing and verifiying
 * @author Jonas Ornfelt
 */

public class KeyHandler {
	
	//key variables
	private static final String PUBLIC_KEY_FILE = "Public.key";
	private static final String PRIVATE_KEY_FILE = "Private.key";
	private static final String ALGO = "DSA";
	
	//main method that tries to create private and public key files
	public static void main(String[] args) {
		
		String myPrivateKey;
		String myPublicKey;
		try {
			myPrivateKey = args.length > 0 ? args[0] : PRIVATE_KEY_FILE;
			myPublicKey = args.length > 1 ? args[1] : PUBLIC_KEY_FILE;
			
			System.out.println("myprivatekey: " + myPrivateKey + ", mypublickey: " + myPublicKey);
			
			 KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
			 kpg.initialize(512, new SecureRandom( ));
			 KeyPair kp = kpg.generateKeyPair( );
			 Class spec = Class.forName(
			 "java.security.spec.DSAPrivateKeySpec");
			 KeyFactory kf = KeyFactory.getInstance("DSA");
			 
			 DSAPrivateKeySpec ks = (DSAPrivateKeySpec)
			 kf.getKeySpec(kp.getPrivate( ), spec);
			 
			 FileOutputStream fos = new FileOutputStream(myPrivateKey);
			 ObjectOutputStream oos = new ObjectOutputStream(fos);
			 oos.writeObject(ks.getX());
			 oos.writeObject(ks.getP());
			 oos.writeObject(ks.getQ());
			 oos.writeObject(ks.getG());
			 
			 Class spec2 = Class.forName("java.security.spec.DSAPublicKeySpec");
			 
			 DSAPublicKeySpec ks2 = (DSAPublicKeySpec)kf.getKeySpec(kp.getPublic(), spec2);
					 
			 FileOutputStream fos2 = new FileOutputStream(myPublicKey);
			 ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
			 oos2.writeObject(ks2.getY());
			 oos2.writeObject(ks2.getP());
			 oos2.writeObject(ks2.getQ());
			 oos2.writeObject(ks2.getG());
			
		}catch(Exception e) { e.printStackTrace(); }
	}
	
	//the following method saves the keys
		private static void saveKeys(String fileName, BigInteger p, BigInteger q, BigInteger g, BigInteger xy) throws IOException {
			//stream variables
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			
			//try to save
			try {
				System.out.println("Generating " + fileName + "...");
				fos = new FileOutputStream(fileName);
				oos = new ObjectOutputStream(new BufferedOutputStream(fos));
				oos.writeObject(p);
				oos.writeObject(q);
				oos.writeObject(g);
				oos.writeObject(xy);
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
}
