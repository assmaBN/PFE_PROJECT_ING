package sopra.belgium.eid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Connexion {
	 private final boolean cardConnected;
	 
	 public Connexion(boolean cardConnected) {
			super();
			this.cardConnected = cardConnected;
		}

	public boolean getCardConnected() {
		return cardConnected;
	}

	
	 
}
