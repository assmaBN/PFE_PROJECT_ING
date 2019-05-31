/*
 * 5. LICENSE ISSUES 
 * The eID Toolkit uses several third-party libraries or code. 
 * Redistributions in any form of the eID Toolkit � even embedded in a compiled application � 
 * must reproduce all the eID Toolkit and third-party�s copyright notices, list of conditions, 
 * disclaimers, and any other materials provided with the distribution. 
 * 
 * 5.1 Disclaimer 
 * This eID Toolkit is provided by the Belgian Government �as is�, and any expressed or implied 
 * warranties, including, but not limited to, the implied warranties of merchantability and fitness 
 * for a particular purpose are disclaimed.  In no event shall the Belgian Government or its 
 * contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential 
 * damages (including, but not limited to, procurement of substitute goods or services; loss of use, 
 * data, or profits; or business interruption) however caused and on any theory of liability, whether 
 * in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of 
 * the use of this Toolkit, even if advised of the possibility of such damage. 
 * However, the Belgian Government will ensure the maintenance of the Toolkit � that is, bug 
 * fixing, and support of new versions of the Electronic Identity card.
 * 
 * Source: DeveloperGuide.pdf
 */
package sopra.belgium.eid.metier;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Base64;


import sopra.belgium.eid.exceptions.EIDException;
import sopra.belgium.eid.exceptions.HashVerificationException;
import sopra.belgium.eid.exceptions.RootVerificationException;
import sopra.belgium.eid.exceptions.SignatureVerificationException;
import sopra.belgium.eid.objects.IDAddress;
import sopra.belgium.eid.objects.IDData;
import sopra.belgium.eid.objects.IDPhoto;
import sopra.belgium.eid.security.Certificate;
import sopra.belgium.eid.security.CertificateChain;
import sopra.belgium.eid.security.CertificateStatus;
import sopra.belgium.eid.security.HardCodedRootCertificate;
import sopra.belgium.eid.security.HardCodedRootCertificateV2;
import sopra.belgium.eid.security.RNCertificate;

/**
 * The beID class is the main interface to perform operations on the Belgian eID
 * card and to perform verification, sign documents, etc... . This interface
 * should be used when trying to perform high-level operations in your own
 * system. Every operation connects to the smart card if not yet connected
 * before performing the operation itself.

 */
public class BeID extends SmartCard {

	/** Indicates whether test cards with an invalid root are enabled */
	private boolean enableTestCard;

	/**
	 * Contains the name of the smart card reader to connect with, leave empty
	 * to connect with the first available smart card reader
	 */
	private String name;
	private byte[] readdata;
	
	/**
	 * Sets up the requirements needed for a valid functioning. The system
	 * connects to the first smart card reader that is found in the system and
	 * that works with the PC/SC driver.
	 * 
	 * @param enableTestCard
	 *            indicates whether test cards with invalid roots are enabled
	 */
	public BeID(final boolean enableTestCard) {
		super();
		this.enableTestCard = enableTestCard;
		this.name = "";
	}


	/**
	 * Returns the ID information from the card currently inserted in the smart
	 * card reader. This data includes the personal information and some ID
	 * specific information.
	 * 
	 * @return the ID information
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 */
	public IDData getIDData() throws EIDException {
		try {
			
			// Connect if not yet connected
			this.connectCard();

			// Read data and signature of ID
			
			byte[] readData;
			byte[] readSignatureData;
			final byte[] fileToRead = { IDData.fgDFID[0], IDData.fgDFID[1],
					IDData.fgDataTag, IDData.fgDataTagID };
			
			final byte[] signatureFileToRead = { IDData.fgDFID[0],
					IDData.fgDFID[1], IDData.fgDataTag, IDData.fgDataTagIDSIG };
			
			readData = super.readFile(fileToRead, IDData.MAX_LEN);
			
			readSignatureData = super.readFile(signatureFileToRead,
					IDData.fgMAX_SIGNATURE_LEN);

			if (verifyRoot()) {
				
				if (verifyRNSignature(readData, readSignatureData)) {
					
					// Return read identity data
					exception="noexception";
					return IDData.parse(readData);
				} else {
					exception = "The data of the ID couldn't be verified correctly against it's signature";
					
					throw new SignatureVerificationException("ID");
				}
				
			} else {
				exception = "The root not verified";
				throw new RootVerificationException();
			}
		} catch (EIDException e) {
			// We don't need another wrap around
			throw e;
		} catch (Exception e) {
			throw new EIDException(e);
		}
	}
	
	


	/**
	 * Returns the address of the holder of the eID card currently inserted in
	 * the smart card reader.
	 * 
	 * @return the address of the holder
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 */
	public IDAddress getIDAddress() throws EIDException {
		try {
			// Connect if not yet connected
			this.connectCard();

			// Read the address data, the ID signature and the address signature
			byte[] readDataRaw;
			byte[] readIDSignature;
			byte[] readAddrSignature;
			final byte[] fileToRead = { IDAddress.fgDFID[0],
					IDAddress.fgDFID[1], IDAddress.fgDataTag,
					IDAddress.fgDataTagADDR };
			final byte[] idSigFileToRead = { IDData.fgDFID[0],
					IDData.fgDFID[1], IDData.fgDataTag, IDData.fgDataTagIDSIG };
			final byte[] addrSigFileToRead = { IDAddress.fgDFID[0],
					IDAddress.fgDFID[1], IDAddress.fgDataTag,
					IDAddress.fgDataTagADDRSIG };

			readIDSignature = super.readFile(idSigFileToRead,
					IDAddress.fgMAX_SIGNATURE_LEN);
			readAddrSignature = super.readFile(addrSigFileToRead,
					IDAddress.fgMAX_SIGNATURE_LEN);
			readDataRaw = super.readFile(fileToRead, IDAddress.MAX_LEN);

			// Trim trailing zeroes of read data
			int indexLastNonZero = -1;
			for (int i = readDataRaw.length - 1; i >= 0; i--) {
				
				if (readDataRaw[i] != 0) {
					indexLastNonZero = i;
					
					break;
				}
			}
			final byte[] readData = new byte[indexLastNonZero + 1];
			System.arraycopy(readDataRaw, 0, readData, 0, indexLastNonZero + 1);
			
			// Append the ID signature
			byte[] fullData = new byte[readData.length + readIDSignature.length];
			System.arraycopy(readData, 0, fullData, 0, readData.length);
			System.arraycopy(readIDSignature, 0, fullData, readData.length,
					readIDSignature.length);

			// Verify the root and the signature
			if (verifyRoot()) {
				if (verifyRNSignature(fullData, readAddrSignature)) {
					// Return read address data
					return IDAddress.parse(readData);
				} else {
					exception = "The data of the Address couldn't be verified correctly against it's signature";
					throw new SignatureVerificationException("Address");
				}
			} else {
				exception = "The root not verified";
				throw new RootVerificationException();
			}
		} catch (EIDException e) {
			// We don't need another wrap around
			throw e;
		} catch (Exception e) {
			throw new EIDException(e);
		}
	}

	
	public boolean readPhotoData() throws Exception{
		
			// Connect if not yet connected
			this.connectCard();

			// Read file
			byte[] readData = new byte[] {};
			final byte[] fileToRead = { IDPhoto.fgDFID[0], IDPhoto.fgDFID[1],
					IDPhoto.fgDataTag, IDPhoto.fgDataTagPHOTO };

			readData = super.readFile(fileToRead, IDPhoto.MAX_LEN);
			readdata =  readData;
			final IDPhoto photo = IDPhoto.parse(readdata);
			if (verifyRoot()) {
				if (photo.verifyHash(this.getIDData().getHashPhoto())) {
					return true;
				} else {
					throw new HashVerificationException("Photo");
				}
			} else {
				throw new RootVerificationException();
			}
		
	}
	/**
	 * Returns the photo of the holder of the eID card currently inserted in the
	 * smart card reader.
	 * 
	 * @return the photo data
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 */
	public IDPhoto getIDPhoto() throws EIDException {

			return IDPhoto.parse(readdata);
		
	}
	
	
	
	/**
	 * Returns the encoded image to Base64 of the currently inserted smart card.
	 * 
	 * @return the encoded image to Base64
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 */
	public String convertToBase64() throws EIDException
	{
		
			
			//************encode to base64'******************//
			return Base64.getEncoder().encodeToString(readdata);
			
//			//*************************************************//

	}
	

	/**
	 * Returns the certificates in a certificate validation chain.
	 * 
	 * @return the certificate chain
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 */
	public CertificateChain getCertificateChain() throws EIDException {
		try {
			// Connect if not yet connected
			this.connectCard();

			return new CertificateChain(this);
		} catch (Exception e) {
			throw new EIDException(e);
		}
	}

	/**
	 * Returns the national register certificate.
	 * 
	 * @return the RN certificate
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 */
	public RNCertificate getNationalRegisterCertificate() throws EIDException {
		try {
			// Connect if not yet connected
			this.connectCard();

			final RNCertificate rn = new RNCertificate(this);
			
			rn.verify();
			return rn;
		} catch (Exception e) {
			throw new EIDException(e);
		}
	}


	/**
	 * Verifies the root certificate of the smart card.
	 * 
	 * @return whether the verification succeeded or not
	 * @throws IOException
	 *             if the file system is read only and thus the root certificate
	 *             couldn't be serialized
	 * @throws CertificateException
	 *             when the instance couldn't be parsed
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 */
	private boolean verifyRoot() throws CertificateException, IOException,
			EIDException {
		if (enableTestCard) {
			// Test card can't verify signatures correctly so always reply with
			// true
			
			return true;
		} else {
			
			// Verify the root certificate
		    final Certificate root = getCertificateChain().getRootCert();
			final Certificate cert = new HardCodedRootCertificate();
			final Certificate certV2 = new HardCodedRootCertificateV2();

			if (root.getX509Certificate().equals(cert.getX509Certificate()) ||
			        root.getX509Certificate().equals(certV2.getX509Certificate())) {
				return true;
			} else {
				this.getCertificateChain().getRootCert().setStatus(
						CertificateStatus.BEID_CERTSTATUS_INVALID_ROOT);
				return false;
			}
		}
	}

	/**
	 * Verifies the signature by verifying the buffer against the signature,
	 * using the given public key and the algorithm SHA1withRSA. This method is
	 * a helper class for the retrieval of certain types of data.
	 * 
	 * @param data
	 *            is the data to verify
	 * @param signature
	 *            is the signature to verify against the data
	 * @return whether the verification succeeded or not
	 * @throws CertificateException
	 *             when the instance couldn't be parsed
	 * @throws EIDException
	 *             when the operation couldn't be performed successfully, the
	 *             cause of the problem contains a more detailed description
	 * @throws IOException
	 *             is the verification failed because of IO fault
	 * @throws InvalidKeyException
	 *             when the public key is invalid
	 * @throws NoSuchAlgorithmException
	 *             when the SHA1withRSA algorithm isn't supported
	 * @throws SignatureException
	 *             when the signature is invalid
	 */
	private boolean verifyRNSignature(final byte[] data, final byte[] signature)
			throws EIDException, CertificateException, IOException,
			InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		// Verify the RRNDN of the national register certificate if card is no
		// testcard, then verify the data against the signature using the public
		// key
		if (enableTestCard || this.getNationalRegisterCertificate().verify()) {
			// Initialize signature with correct algorithm
			Signature sig = null;
			try {
				
				sig = Signature.getInstance("SHA1withRSA");
				
			} catch (NoSuchAlgorithmException e) {
				// Shouldn't occur
				throw e;
			}

			// Fetch public key of correct certificate
			PublicKey pk = this.getNationalRegisterCertificate()
					.getX509Certificate().getPublicKey();
			// Verify signature and return results
			sig.initVerify(pk);
			sig.update(data, 0, data.length);
			return sig.verify(signature);
		}

		// Failed
		return false;
	}
}