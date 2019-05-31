package sopra.belgium.eid;




import java.io.IOException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sopra.belgium.eid.metier.BeID;


@RestController
public class Read_Data_Controller {
	@CrossOrigin(origins = "*")
	@GetMapping(value="/")
	public Datas getData() throws IOException, Exception {
		
			BeID eID = new BeID(true); // We allow information to be fetched
										// from test cards
			String[] data = eID.getIDData().tabString();
			String[] dataAddr = eID.getIDAddress().tabString();
			String convertedImage ;
			String path ;
			if (eID.readPhotoData())
			{
				eID.getIDPhoto().writeToFile("photoeID");
				path = eID.getIDPhoto().pathImage();
				convertedImage = eID.convertToBase64();
			}
			else 
			{
				convertedImage="";
				path="";
			}
			
			Datas dataa = new Datas(data[4],
					data[5],data[6],data[7],
					data[8],data[9],data[10],
					data[11],data[12],data[13],
					dataAddr[0],dataAddr[1],dataAddr[2],convertedImage, path);
			eID.disconnect();
			return dataa;
			
}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/check")
	public Connexion checkConnexion() throws Exception{
		
			Connexion connexion ;
			BeID eID = new BeID(true); 
			connexion = new Connexion(eID.verifyCardConnected()) ;
			return connexion;
}
	@CrossOrigin(origins = "*")
	@GetMapping(value="/checkval")
	public CardValidity checkValidity() throws Exception{
		CardValidity crdval;
		BeID eID = new BeID(true);
		crdval = new CardValidity(eID.verifycard());
		return crdval;
	}
	
}
