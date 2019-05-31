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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.naming.CommunicationException;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import sopra.belgium.eid.exceptions.CardNotFoundException;
import sopra.belgium.eid.exceptions.EIDException;
import sopra.belgium.eid.exceptions.NoReadersFoundException;
import sopra.belgium.eid.objects.SmartCardReadable;

/**
 * The SmartCard class contains the different operations that can be performed
 * on a regular (not specifically e-ID related) smart card. It contains more
 * general operations that are more difficult to work with than those of
 * {@link sopra.belgium.eid.metier.BeID}. While the BeID class connects to the
 * smart card implicitly when performing an operation, this class does not do
 * this. Before doing anything, the method
 * {@link sopra.belgium.eid.metier.SmartCard#connectCard()} should be executed.

 */
public class SmartCard {

	/** Contains the card on which the operations need to be performed */
	private Card card;

	/** Contains the smart card reader with which a connection has been made */
	private CardTerminal terminal;

	/** Contains the channel over which the communication occurs */
	private CardChannel channel;

	/** Contains the ATR (Answer To Reset) of the card */
	private ATR atr;

	/** Indicates whether a successful connection has already been made */
	private boolean isConnected = false;
	
	/** Indicates if there is an exception */
	String exception;


	/**
	 * Sets up the system. Note that you have to call <i>connect()</i> or
	 * <i>connect(name)</i> before being able to perform any operation or
	 * retrieve any data from the smart card.
	 */
	public SmartCard() {
		super();
		isConnected = false;
	}

	
	/**
	 * Clear the terminal list
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * 
	 *
	 * @throws Exception 
	 */

	//************************************************************//
	public void cleanCache() throws  
	ClassNotFoundException, NoSuchFieldException,   IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Class pcscterminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
        Field contextId = pcscterminal.getDeclaredField("contextId");
        contextId.setAccessible(true);

        if(contextId.getLong(pcscterminal) != 0L)
        {
            // First get a new context value
            Class pcsc = Class.forName("sun.security.smartcardio.PCSC");
            Method sCardEstablishContext = pcsc.getDeclaredMethod(
                                               "SCardEstablishContext",
                                               new Class[] {Integer.TYPE }
                                           );
            sCardEstablishContext.setAccessible(true);

            Field sCARD_SCOPE_USER = pcsc.getDeclaredField("SCARD_SCOPE_USER");
            sCARD_SCOPE_USER.setAccessible(true);

            long newId = ((Long)sCardEstablishContext.invoke(pcsc, 
                    new Object[] { sCARD_SCOPE_USER.getInt(pcsc) }
            ));
            contextId.setLong(pcscterminal, newId);
            TerminalFactory factory = TerminalFactory.getDefault();
            CardTerminals terminals = factory.terminals();
            Field fieldTerminals = pcscterminal.getDeclaredField("terminals");
            fieldTerminals.setAccessible(true);
            Class classMap = Class.forName("java.util.Map");
            Method clearMap = classMap.getDeclaredMethod("clear");

            clearMap.invoke(fieldTerminals.get(terminals));
        }
	}
	//**********************************************************//
	
	
	//*****************************************************************//
	/**
	 * Verifie if the card is connected or not
	 * 
	 *@return whether it is already connected
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws ClassNotFoundException 
	 * @throws CommunicationException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoReadersFoundException 
	 * @throws Exception 
	 */
		public boolean verifyCardConnected() throws CardException, CommunicationException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchAlgorithmException, NoReadersFoundException{
			if (!isConnected) {
				cleanCache();
				TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
				List<CardTerminal> terminals = factory.terminals().list();
				if (terminals.size() > 0) {
					// get the first terminal
					terminal = terminals.get(0);
					// establish a connection with the card
					
						terminal.isCardPresent();
					
					
					
				} else {
					throw new NoReadersFoundException();
				}
			}
			return true ;
			}
		
		/**
		 * Verifie if it is a smart card or not
		 * 
		 *@return 
		 * @throws NoReadersFoundException 
		 * @throws CardException 
		 * @throws NoSuchAlgorithmException 
		 * @throws InvocationTargetException 
		 * @throws NoSuchMethodException 
		 * @throws IllegalAccessException 
		 * @throws NoSuchFieldException 
		 * @throws ClassNotFoundException 
		 * @throws Exception 
		 */
		public String verifycard() throws CommunicationException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchAlgorithmException, CardException, NoReadersFoundException{
			if (this.verifyCardConnected()){
				try {
					card = terminal.connect("*");
					atr = card.getATR();
					channel = card.getBasicChannel();
					isConnected = true;
					exception = "it's a smart card";
				}
				catch (Exception e) {
					exception = "not a smart card";
				}
			}
			
				return exception;
		}
		
		//****************************************************************//
	
	/**
	 * Connects the system to the first compatible smart card reader to allow
	 * operations to be performed on the smart card. If the card was already
	 * connected to the system nothing will be done.
	 * @throws Exception 
	 */
	public void connectCard() throws  Exception {
		if (!isConnected) {
			// show the list of available terminals
			// Connects with the first available smart card reader
			//************************************************************************//
			cleanCache();
			//****************************************************************************************//
			
			
			TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
			List<CardTerminal> terminals = factory.terminals().list();
			if (terminals.size() > 0) {
				// get the first terminal
				terminal = terminals.get(0);
				// establish a connection with the card
				if (!terminal.isCardPresent() && !terminal.waitForCardPresent(10)) {
			            throw new Exception("no card available");
			        }
				
				try {
					card = terminal.connect("*");
					atr = card.getATR();
					channel = card.getBasicChannel();
					isConnected = true;
				}
				catch (Exception e) {
					exception = "not a smart card";
				}
			} else {
				throw new NoReadersFoundException();
			}
		}
	}

	/**
	 * Disconnects the card from the connected slot.
	 * 
	 * @throws CardException
	 *             if the card operation failed
	 */
	public void disconnect() throws CardException {
		if (isConnected) {
			card.disconnect(true);
			isConnected = false;
		}
	}

	/**
	 * Indicates whether the smart card is currently connected to the system.
	 * 
	 * @return whether it is already connected
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * Returns the ATR (Answer To Reset) of the card.
	 * 
	 * @return the ATR
	 * @throws CardException
	 *             if the card operation failed
	 */
	public ATR getATR() throws CardNotFoundException {
		if (isConnected()) {
			return atr;
		} else {
			throw new CardNotFoundException(
					CardNotFoundException.CardNotFoundType.NOT_CONNECTED);
		}
	}

	/**
	 * Locks the connected smart card reader to avoid concurrency problems.
	 * 
	 * @throws CardNotFoundException
	 *             indicates that the card wasn't present in the system or was
	 *             reset, it could also be that no connection with the smart
	 *             card has been made yet
	 * @throws CardException
	 *             if the card operation failed
	 */
	public void beginTransaction() throws CardNotFoundException, CardException {
		synchronized (this) {
			// Handle the case when no connection has yet been made
			if (!isConnected()) {
				throw new CardNotFoundException(
						CardNotFoundException.CardNotFoundType.NOT_CONNECTED);
			} else {
				card.beginExclusive();
			}
		}
	}

	/**
	 * Unlocks the connected smart card reader to allow other instances to
	 * access the data on the smart card.
	 * 
	 * @throws CardNotFoundException
	 *             indicates that the card wasn't present in the system or was
	 *             reset, it could also be that no connection with the smart
	 *             card has been made yet
	 * @throws CardException
	 *             if the card operation failed
	 */
	public void endTransaction() throws CardNotFoundException, CardException {
		synchronized (this) {
			// Handle the case when no connection has yet been made
			if (!isConnected()) {
				throw new CardNotFoundException(
						CardNotFoundException.CardNotFoundType.NOT_CONNECTED);
			} else {
				card.endExclusive();
			}
		}
	}

	/**
	 * Transmits the given command APDU to the smart card.
	 * 
	 * @param cAPDU
	 *            is the command APDU to send
	 * @return the response as issued by the smart card in a response APDU
	 *         command
	 * @throws CardNotFoundException
	 *             indicates that the card wasn't present in the system or was
	 *             reset, it could also be that no connection with the smart
	 *             card has been made yet
	 * @throws CardException
	 *             if the card operation failed
	 */
	public ResponseAPDU transmitAPDU(final CommandAPDU cAPDU)
			throws CardException, CardNotFoundException {
		if (isConnected()) {
			// Transmit APDU over channel and return response
			return channel.transmit(cAPDU);
		} else {
			throw new CardNotFoundException(
					CardNotFoundException.CardNotFoundType.NOT_CONNECTED);
		}
	}

	/**
	 * Selects a file on the smart card so that it can be read from or written
	 * to.
	 * 
	 * @param fileID
	 *            is the ID of the file to select
	 * @return the response APDU containing the two resulting status bits
	 * @throws CardNotFoundException
	 *             indicates that the card wasn't present in the system or was
	 *             reset, it could also be that no connection with the smart
	 *             card has been made yet
	 * @throws CardException
	 *             if the card operation failed
	 */
	public ResponseAPDU selectFile(final byte[] fileID)
			throws CardNotFoundException, CardException {
		// Handle the case when no connection has yet been made
		if (!isConnected()) {
			throw new CardNotFoundException(
					CardNotFoundException.CardNotFoundType.NOT_CONNECTED);
		}

		return transmitAPDU(new CommandAPDU(0x00, 0xA4, 0x08, 0x0C, fileID,
				0x00));
	}

	/**
	 * Sends a command to the smart card to read a block of the currently
	 * selected file.
	 * 
	 * @param p1
	 *            contains the first parameter to indicate what to read
	 * @param p2
	 *            contains the second parameter to indicate what to read
	 * @param noBytesToRead
	 *            contains the number of bytes to read
	 * @return the block of data read
	 * @throws CardNotFoundException
	 *             indicates that the card wasn't present in the system or was
	 *             reset, it could also be that no connection with the smart
	 *             card has been made yet
	 * @throws CardException
	 *             if the card operation failed
	 */
	public ResponseAPDU readBinaryData(int p1, int p2, int noBytesToRead)
			throws CardNotFoundException, CardException {
		// Handle the case when no connection has yet been made
		if (!isConnected()) {
			throw new CardNotFoundException(
					CardNotFoundException.CardNotFoundType.NOT_CONNECTED);
		}

		return transmitAPDU(new CommandAPDU(0x00, 0xB0, p1, p2, noBytesToRead));
	}

	/**
	 * Reads a file from the smart card and returns the contents of the read
	 * file.
	 * 
	 * @param fileID
	 *            is the identifier for the file to read
	 * @param maxOutputLength
	 *            is the maximum length of the output returned by the reader
	 * @return the contents of the file
	 * @throws CardNotFoundException
	 *             indicates that the card wasn't present in the system or was
	 *             reset, it could also be that no connection with the smart
	 *             card has been made yet
	 * @throws CardException
	 *             if the card operation failed
	 */
	public byte[] readFile(byte[] fileID, final int maxOutputLength)
			throws CardNotFoundException, CardException {
		// Handle the case when no connection has yet been made
		if (!isConnected()) {
			throw new CardNotFoundException(
					CardNotFoundException.CardNotFoundType.NOT_CONNECTED);
		}

		// Lock card
		this.beginTransaction();

		// Init
		int blocklength = 0xF8;
		int length = 0;
		boolean enough = false;
		byte[] tmpReadData = new byte[maxOutputLength];

		// Select the file to read
		byte[] fullfileID = new byte[2 + fileID.length];
		fullfileID[0] = SmartCardReadable.fgMF[0];
		fullfileID[1] = SmartCardReadable.fgMF[1];
		//copie un tableau source à partir d'une position de début 
		//spécifique vers le tableau de destination à partir de la position mentionnée.
		//fileID : tableau à copier de
		//0 : position de départ dans le tableau source d'où copier
		//fullfileID : tableau à copier dans
		//2 : début position dans le tableau de destination, où copier dans
		//fileID.length : nb de composants à copier 
		System.arraycopy(fileID, 0, fullfileID, 2, fileID.length);
		this.selectFile(fullfileID);

		// Keep on reading the file until everything has been read
		while (!enough) {
			// Read block
			int p1 = length / 256;
			int p2 = length % 256;
			int noBytesToRead = blocklength;
			ResponseAPDU rAPDU = readBinaryData(p1, p2, noBytesToRead);
			//Renvoie la valeur de l'octet d'�tat SW1 sous la forme d'une valeur comprise entre 0 et 255.
			if ((rAPDU.getSW1() == 0x90) && (rAPDU.getSW2() == 0x00)) {
				// Data remains to be read
				System.arraycopy(rAPDU.getData(), 0, tmpReadData, length, rAPDU
						.getData().length);
				length += rAPDU.getData().length;
			} else {
				enough = true;

				if (rAPDU.getSW1() == 0x6C) {
					// Wrong length read (too much), so we are at the end of
					// the file and thus only need to read the value of SW2
					// number of bytes
					blocklength = rAPDU.getSW2();

					p1 = length / 256;
					p2 = length % 256;
					noBytesToRead = blocklength;
					rAPDU = readBinaryData(p1, p2, noBytesToRead);

					// This should have worked
					assert ((rAPDU.getSW1() == 0x90) && (rAPDU.getSW2() == 0x00));

					// Copy last bit of data in certificate
					System.arraycopy(rAPDU.getData(), 0, tmpReadData, length,
							blocklength);
					length += blocklength;
				}
			}
		}

		// Unlock card
		this.endTransaction();

		// Return the data
		byte[] result = new byte[length];
		System.arraycopy(tmpReadData, 0, result, 0, length);

		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			System.err.println(e);
			Thread.currentThread().interrupt();
		}
		
		return result;
	}



}