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
package sopra.belgium.eid.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sopra.belgium.eid.exceptions.TagNotFoundException;
import sopra.belgium.eid.util.FormattedTLV;

/**
 * The IDData class represents the ID data of a certain beID card. This data
 * includes the personal information of the card holder such as the name, first
 * name, municipality, birth information, ... . This data also includes
 * information about the beID card itself such as the begin and end of the
 * validity, the chip number of the card and the card number, ... .

 */
public class IDData implements SmartCardReadable {

	
	
	/** Contains the different kinds of sexes that exist */
	public static char fgMALE = 'M';
	public static char fgFEMALEDUTCH = 'V';
	public static char fgFEMALEOTHER = 'F';
	public static char fgREMALEEN = 'W';

	/** Contains the ID specific file attributes to read from on the smart card */
	public final static char[] fgID = { fgDataTag, fgDataTagID };

	/** Contains the maximum size (in number of bytes) that the IDData can take */
	public final static int MAX_LEN = 1024;

	/** Contains the signature ID specific file attributes to read from */
	public final static char[] fgSIGID = { fgDataTag, fgDataTagIDSIG };

	/** Contains the card number of the ID */
	private final String cardNumber;

	/** Contains the chip number of the ID */
	private final String chipNumber;

	/** Contains the date from when the ID is valid */
	private final Date validFrom;

	/** Contains the date to when the ID is valid */
	private final Date validTo;

	/** Contains the municipality where the holder of the ID lives */
	private final String municipality;

	/** Contains the national number */
	private final String nationalNumber;

	/** Contains the (last) name of the holder of the ID */
	private final String name;

	/** Contains the 1st firstname of the holder of the ID */
	private final String firstname1;

	/** Contains the 3rd firstname of the holder of the ID */
	private final String firstname3;

	/** Contains the nationality of the holder of the ID */
	private final String nationality;

	/** Contains the birth place of the holder of the ID */
	private final String birthPlace;

	/** Contains the birth date of the holder of the ID */
	private final Date birthDate;

	/** Contains the sex of the holder of the ID */
	private final char sex;


	/** Contains the hash of the photo of the holder of the ID */
	private final byte[] hashPhoto;
	

	/**
	 * Parses the given stream of characters into a valid IDData object
	 * 
	 * @param characterStream
	 *            is the stream of characters to parse
	 * @return a fully initialized IDData object
	 * @throws TagNotFoundException
	 *             indicates that some of the data hasn't been read in a proper
	 *             format. If this occurs often, the ID card may be invalid
	 * @throws ParseException
	 *             indicates that some of the data hasn't been read in a proper
	 *             format. If this occurs often, the ID card may be invalid
	 */
	public static IDData parse(final byte[] characterStream)
			throws  ParseException {
		// Contains the TLV reader
		
		final FormattedTLV fTLV = new FormattedTLV(characterStream);
		
		
		// Contains the birth date, this can take several formats,
		Date birthDate;
		try {
			birthDate = fTLV.dateData((byte) 0x0C, "dd MMM yyyy");
		} catch (final ParseException e) {
			try {
				birthDate = fTLV.dateData((byte) 0x0C, "dd MMM  yyyy");
			} catch (final ParseException e1) {
				birthDate = fTLV.dateData((byte) 0x0C, "dd.MMM.yyyy");
			}
		}
		
		return new IDData(
				fTLV.stringData((byte) 0x01), 
				fTLV.hexadecimalData((byte) 0x02),
				fTLV.dateData((byte) 0x03, "dd.MM.yyyy"), 
				fTLV.dateData((byte) 0x04, "dd.MM.yyyy"), 
				fTLV.stringData((byte) 0x05), 
				fTLV.stringData((byte) 0x06),
				fTLV.stringData((byte) 0x07), 
				fTLV.stringData((byte) 0x08),
				fTLV.stringData((byte) 0x09), 
				fTLV.stringData((byte) 0x0A),
				fTLV.stringData((byte) 0x0B), 
				birthDate,
				fTLV.stringData((byte) 0x0D).charAt(0), 
				fTLV.asciiData((byte) 0x11));
	}

	/**
	 * Initializes the IDData object with the given data.
	 * 
	 * @param cardNumber
	 *            is the card number of the ID
	 * @param chipNumber
	 *            is the chip number of the ID
	 * @param validFrom
	 *            is the date from when the ID is valid (must be before the
	 *            validTo input)
	 * @param validTo
	 *            is the date to when the ID is valid (must be after the
	 *            validFrom input)
	 * @param municipality
	 *            is the municipality where the holder of the ID lives
	 * @param nationalNumber
	 *            is the national number
	 * @param name
	 *            is the (last) name of the holder of the ID
	 * @param firstname1
	 *            is the 1st firstname of the holder of the ID
	 * @param firstname3
	 *            is the 3rd firstname of the holder of the ID
	 * @param nationality
	 *            is the nationality of the holder of the ID
	 * @param birthPlace
	 *            is the birth place of the holder of the ID
	 * @param birthDate
	 *            is the birth date of the holder of the ID (must be a date in
	 *            the past)
	 * @param sex
	 *            is the sex of the holder of the ID (M/F)
	 * @param bs
	 *            is the hash of the photo of the holder of the ID
	 */
	public IDData(final String cardNumber, final String chipNumber,
			final Date validFrom, final Date validTo,
			final String municipality, final String nationalNumber,
			final String name, final String firstname1,
			final String firstname3, final String nationality,
			final String birthPlace, final Date birthDate, final char sex,
			 final byte[] hphoto) {
		if (validFrom.after(validTo)) {
			throw new IllegalArgumentException(
					"The validity of an ID card must be "
							+ "from a date that is earlier than the date to which it "
							+ "is valid.");
		}

		if (birthDate.after(new Date())) {
			throw new IllegalArgumentException(
					"The birth date can't be a date in the future.");
		}

		if ((sex != fgMALE) && (sex != fgFEMALEDUTCH) && (sex != fgFEMALEOTHER)
				&& (sex != fgREMALEEN)) {
			throw new IllegalArgumentException(
					"The given sex must be equal to 'M' or 'F' or 'V'");
		}

		// Initialize the data
		this.cardNumber = cardNumber;
		this.chipNumber = chipNumber;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.municipality = municipality;
		this.nationalNumber = nationalNumber;
		this.name = name;
		this.firstname1 = firstname1;
		this.firstname3 = firstname3;
		this.nationality = nationality;
		this.birthPlace = birthPlace;
		this.birthDate = birthDate;
		this.sex = sex;
		this.hashPhoto = hphoto.clone();
	}

	
	
	public String[] tabString() {
		String[] tabs = new String[19];
		SimpleDateFormat formatter =new SimpleDateFormat("dd/MM/yyyy");
		String c = formatter.format(birthDate); 
		tabs[0]= cardNumber;
		tabs[1]= chipNumber;
		tabs[2]=""+ validFrom;
		tabs[3]=""+validTo;
		tabs[4]=municipality;
		tabs[5]=nationalNumber;
		tabs[6]=name;
		tabs[7]=firstname1;
		tabs[8]=firstname3;
		tabs[9]=nationality;
		tabs[10]=birthPlace;
		tabs[11]=c;
		tabs[12]=""+sex;
		tabs[13]=new String(hashPhoto);
		return tabs;
	}

	/**
	 * Returns the card number of the ID.
	 * 
	 * @return the card number
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * Returns the chip number of the ID.
	 * 
	 * @return the chip number
	 */
	public String getChipNumber() {
		return chipNumber;
	}

	/**
	 * Returns the date from when the ID is valid.
	 * 
	 * @return the begin of the validity period
	 */
	public Date getValidFrom() {
		return validFrom;
	}

	/**
	 * Returns the date to when the ID is valid.
	 * 
	 * @return the end of the validity period
	 */
	public Date getValidTo() {
		return validTo;
	}

	/**
	 * Returns the municipality where the holder of the ID lives.
	 * 
	 * @return the municipality
	 */
	public String getMunicipality() {
		return municipality;
	}

	/**
	 * Returns the national number of the holder of the ID.
	 * 
	 * @return the national number
	 */
	public String getNationalNumber() {
		return nationalNumber;
	}

	/**
	 * Returns the last name of the holder of the ID.
	 * 
	 * @return the last name of the holder
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the 1st first name of the holder of the ID.
	 * 
	 * @return the 1st first name of the holder
	 */
	public String get1stFirstname() {
		return firstname1;
	}

	/**
	 * Returns the 3rd first name of the holder of the ID.
	 * 
	 * @return the 3rd first name of the holder
	 */
	public String get3rdFirstname() {
		return firstname3;
	}

	/**
	 * Returns the nationality of the holder of the ID.
	 * 
	 * @return the nationality of the holder
	 */
	public String getNationality() {
		return nationality;
	}

	/**
	 * Returns the birth place of the holder of the ID.
	 * 
	 * @return the birth place of the holder
	 */
	public String getBirthPlace() {
		return birthPlace;
	}

	/**
	 * Returns the birth date of the holder of the ID.
	 * 
	 * @return the birth date of the holder
	 */
	public Date getBirthDate() {
		return birthDate;
	}

	/**
	 * Returns the sex of the holder of the ID.
	 * 
	 * @return the sex ('M' or 'F')
	 */
	public char getSex() {
		return sex;
	}


	/**
	 * Returns the hash of the photo of the holder of the ID.
	 * 
	 * @return the hash of the photo
	 */
	public byte[] getHashPhoto() {
		return hashPhoto.clone();
	}
}