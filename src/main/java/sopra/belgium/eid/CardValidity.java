package sopra.belgium.eid;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardValidity {
	private final String exception;

	public CardValidity(String exception) {
		super();
		this.exception = exception;
	}

	public String getException() {
		return exception;
	}
	
}
