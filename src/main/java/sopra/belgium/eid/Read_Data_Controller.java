package sopra.belgium.eid;


import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sopra.belgium.eid.exceptions.EIDException;
import sopra.belgium.eid.metier.BeID;
import sopra.belgium.eid.objects.IDData;

@RestController
public class Read_Data_Controller {
	//http://dlnxhradev02.ptx.fr.sopra:30522
	@CrossOrigin(origins = "*")
	@RequestMapping("/")
	public Datas getData() throws Exception {
		
				
		
			BeID eID = new BeID(true); // We allow information to be fetched
										// from test cards
			
			String[] data = eID.getIDData().tabString();
			String[] dataAddr = eID.getIDAddress().tabString();
			String convertedImage , path ;
			if (eID.readPhotoData())
			{
				eID.getIDPhoto().writeToFile("photoeID");
				path = eID.getIDPhoto().pathImage();
				System.out.println("Path Image : "+eID.getIDPhoto().pathImage());
				
				
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
	@RequestMapping("/check")
	public Connexion checkConnexion() throws Exception{
		
			Connexion connexion ;
			BeID eID = new BeID(true); 
			connexion = new Connexion(eID.verifyCardConnected()) ;
			return connexion;
}
	@CrossOrigin(origins = "*")
	@RequestMapping("/checkval")
	public CardValidity checkValidity() throws Exception{
		CardValidity crdval;
		BeID eID = new BeID(true);
		crdval = new CardValidity(eID.verifycard());
		return crdval;
	}
	
}
