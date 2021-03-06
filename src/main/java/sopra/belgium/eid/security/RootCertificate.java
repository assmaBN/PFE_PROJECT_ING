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
package sopra.belgium.eid.security;

import javax.smartcardio.CardException;

import sopra.belgium.eid.exceptions.CardNotFoundException;
import sopra.belgium.eid.metier.SmartCard;
import sopra.belgium.eid.objects.SmartCardReadable;

/**
 * This class contains the Root certificate which is a certificate for verifying
 * data on a smart card.
 */
public class RootCertificate extends Certificate implements SmartCardReadable {

	/**
	 * Contains the root specific file attributes to read from on the smart card
	 */
	public final static byte[] fgRoot = { fgCertTag, fgDataTagROOT };

	/** Contains the label for the current certificate */
	public final static String fgLabel = "Root";

	/**
	 * Initializes the Root certificate by reading it from the given card
	 * reader.
	 * 
	 * @param card
	 *            is the smart card to fetch the certificate data from
	 * @throws CardException
	 *             when a card related error occurred
	 * @throws CardNotFoundException
	 *             indicates that the card wasn't present in the system or was
	 *             reset
	 */
	public RootCertificate(final SmartCard card) throws CardNotFoundException,
			CardException {
		// Initializes the certificate with the read data and the constant label
		super(card.readFile(new byte[] { Certificate.fgDFCert[0],
						Certificate.fgDFCert[1], fgRoot[0], fgRoot[1] },
						fgMAX_CERT_LEN), fgLabel);
	}

	/**
	 * Initializes the root certificate by the given data.
	 * 
	 * @param contents
	 *            are the contents of the certificate
	 */
	public RootCertificate(final byte[] contents) {
		super(contents, fgLabel);
	}
}
