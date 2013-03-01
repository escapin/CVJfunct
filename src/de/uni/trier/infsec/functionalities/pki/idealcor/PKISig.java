package de.uni.trier.infsec.functionalities.pki.idealcor;

import static de.uni.trier.infsec.utils.MessageTools.copyOf;
import de.uni.trier.infsec.environment.network.NetworkError;
import de.uni.trier.infsec.lib.crypto.CryptoLib;
import de.uni.trier.infsec.lib.crypto.KeyPair;
import de.uni.trier.infsec.utils.MessageTools;

/**
 * Ideal functionality for digital signatures with PKI (Public Key Infrastructure).
 *
 * The intended usage is as follows. An agent who wants to use this functionality to
 * sign messages must first register herself:
 *
 *     PKISig.Signer signer_of_A = PKISig.register(ID_OF_A);
 *
 *  Then, this agent can use this object to sign messages:
 *
 *     byte[] signature = signer_of_A.sign(message);
 *
 * Another agent can verify signatures generated by A as follows:
 *
 *     PKISig.Verifier verifier_for_A = getVerifier(ID_OF_A);
 *     boolean ok = verifier_for_A.verify(signature, message);
 *
 * Note using of the real crypto lib (not the one controlled by the environment)
 * in the implementation of this functionality.
 */
public class PKISig {

	static public class Verifier {
		public final int id;
		protected byte[] verifKey;

		public Verifier(int id, byte[] verifKey) {
			this.id = id;
			this.verifKey = verifKey;
		}

		public boolean verify(byte[] signature, byte[] message) {
			return CryptoLib.verify(message, signature, verifKey);
		}

		public byte[] getVerifKey() {
			return copyOf(verifKey);
		}
	}
	
	static public final class UncorruptedVerifier extends Verifier {
		private Log log;
		
		private UncorruptedVerifier(int id, byte[] verifKey, Log log) {
			super(id,verifKey);
			this.log = log;
		}
		
		public boolean verify(byte[] signature, byte[] message) {
			// verify both that the signature is correc (using the real verification 
			// algorithm) and that the message has been logged as signed
			return CryptoLib.verify(message, signature, verifKey) && log.contains(message);
		}

	}

	/**
	 * An object encapsulating a signing/verification key pair and allowing a user to
	 * create a signature. In this implementation, when a message is signed, a real signature
	 * is created (by an algorithm provided in lib.crypto) an the pair message/signature
	 * is stores in the log.
	 */
	static final public class Signer {
		public final int id;
		private byte[] verifKey;
		private byte[] signKey;
		private Log log;

		public Signer(int id) {
			KeyPair keypair = CryptoLib.generateSignatureKeyPair(); // note usage of the real cryto lib here
			this.signKey = copyOf(keypair.privateKey);
			this.verifKey = copyOf(keypair.publicKey);
			this.id = id;
			this.log = new Log();
		}
		
		public byte[] sign(byte[] message) {
			byte[] signature = CryptoLib.sign(copyOf(message), copyOf(signKey)); // note usage of the real crypto lib here
			// we make sure that the signing has not failed
			if (signature == null) return null;
			// and that the signature is correct
			if( !CryptoLib.verify(copyOf(message), copyOf(signature), copyOf(verifKey)) )
				return null;
			// now we log the message (only!) as signed and return the signature
			log.add(copyOf(message));
			return copyOf(copyOf(signature));
		}

		public Verifier getVerifier() {
			return new UncorruptedVerifier(id, verifKey, log);
		}
	}

	public static void register(Verifier verifier, byte[] pki_domain) throws PKIError, NetworkError {
		PKIForSig.register(verifier, pki_domain);
	}

	public static Verifier getVerifier(int id, byte[] pki_domain) throws NetworkError, PKIError {
		return PKIForSig.getVerifier(id, pki_domain);
	}


	/// IMPLEMENTATION ///

	private static class Log {

		private static class MessageList {
			byte[] message;
			MessageList next;
			public MessageList(byte[] message, MessageList next) {
				this.message = message;
				this.next = next;
			}
		}

		private MessageList first = null;

		public void add(byte[] message) {
			first = new MessageList(message, first);
		}

		boolean contains(byte[] message) {
			for( MessageList node = first;  node != null;  node = node.next ) {
	            if( MessageTools.equal(node.message, message) )
	                return true;
			}
	        return false;
	    }
	}
}
