package sopra.belgium.eid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Datas {

    private final String municipality;
    private final String nationalnumber;
    private final String name;
    private final String firstname1;
    private final String firstname3;
    private final String nationality;
    private final String birthplace;
    private final String birthdate;
    private final String sex;

    private final String hashpicture;
    private final String street;
    private final String zipcode;
    private final String municipalityadr;
    private final String convertedImage;
    private final String path;
    
    public Datas (
    		String municipality,String nationalnumber, String name,String firstname1,
    		String firstname3, String nationality, String birthplace, String birthdate,
    		String sex, String hashpicture,String street,
    		String zipcode,String municipalityadr,String convertedImage, String path)
    {
    	
    	this.municipality = municipality;
    	this.nationalnumber = nationalnumber;
    	this.name = name;
    	this.firstname1 = firstname1;
    	this.firstname3 = firstname3;
    	this.nationality = nationality;
    	this.birthplace = birthplace;
    	this.birthdate = birthdate;
    	this.sex = sex ;

    	this.hashpicture = hashpicture;
    	this.street = street;
    	this.zipcode = zipcode;
    	this.municipalityadr = municipalityadr;
    	this.convertedImage = convertedImage;
    	this.path = path;
    }
    
    
	public String getStreet() {
		return street;
	}

	public String getConvertedImage() {
		return convertedImage;
	}

	public String getZipcode() {
		return zipcode;
	}

	public String getMunicipalityadr() {
		return municipalityadr;
	}

	public String getMunicipality() {
		return municipality;
	}

	public String getNationalnumber() {
		return nationalnumber;
	}

	public String getName() {
		return name;
	}

	public String getFirstname1() {
		return firstname1;
	}

	public String getFirstname3() {
		return firstname3;
	}

	public String getNationality() {
		return nationality;
	}

	public String getBirthplace() {
		return birthplace;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public String getSex() {
		return sex;
	}

	public String getHashpicture() {
		return hashpicture;
	}


	public String getPath() {
		return path;
	}
    		
}
