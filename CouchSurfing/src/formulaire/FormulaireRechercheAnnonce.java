package formulaire;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import modele.Data;
import modele.Logement;
import modele.Offre;
import modele.Utilisateur;
import utilitaire.CustomDate;

public class FormulaireRechercheAnnonce {

	private String ville;
	private String dateDebut;
	private String dateFin;

	public FormulaireRechercheAnnonce(String ville,String dateDebut,String dateFin) {
		this.ville = ville;
		this.setDateDebut(dateDebut);
		this.setDateFin(dateFin);
	}

	public String getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(String dateDebut) {
		this.dateDebut = CustomDate.checkFormatDate(dateDebut);
	}

	public String getDateFin() {
		return dateFin;
	}

	public void setDateFin(String dateFin) {
		this.dateFin = CustomDate.checkFormatDate(dateFin);
	}

	/**
	 * @return liste des offres pour une ville donn�e
	 * @throws Exception
	 */
	public List<Offre> getListeOffre() throws Exception {
		List<Offre> result = new ArrayList<Offre>();
		boolean dateSpecifiee = this.dateDebut!=null && this.dateFin!=null;

		result.addAll(getOffreSansPostulation(dateSpecifiee));
		result.addAll(getOffreAvecPostulation(dateSpecifiee));
		if (result.isEmpty()){
			throw new Exception("Aucun logement a "+this.ville);
		}
		return result;
	}

	private List<Offre> getOffreSansPostulation(boolean dateSpecifiee) throws Exception {
		List<Offre> result = new ArrayList<Offre>();
		String strReq;
		strReq = "SELECT Logement.IdLogement,Utilisateur.IdUtilisateur,Logement.DateDebut,Logement.DateFin "
				+ "FROM Utilisateur,Logement "
				+ "WHERE Logement.IdLogement=Utilisateur.IdLogement AND Logement.ville = ? "
				+ "AND (Logement.IdLogement NOT IN "
				+ "(SELECT Postule.IdLogement FROM Postule)) ";
		if(dateSpecifiee){
			strReq += "AND Logement.DateDebut <= ? AND Logement.DateFin >= ? ";
		}
		PreparedStatement s = Data.BDD_Connection.prepareStatement(strReq);
		s.setString(1, this.ville);
		if(dateSpecifiee){
			s.setDate(2, Date.valueOf(this.dateFin));
			s.setDate(3, Date.valueOf(this.dateDebut));
		}

		ResultSet rs=s.executeQuery();
		while (rs.next()){
			Logement l=Logement.getLogementById(rs.getInt(1));
			Utilisateur u=Utilisateur.getUtilisateurById(rs.getInt(2));
			result.add(new Offre(l, u, rs.getDate(3), rs.getDate(4)));
		}
		return result;
	}

	private List<Offre> getOffreAvecPostulation(boolean dateSpecifiee) throws Exception {
		List<Offre> result = new ArrayList<Offre>();
		List<Offre> resultNonAccepte = new ArrayList<Offre>();

		result.addAll(getRestes(getOffreValideesCompactees(dateSpecifiee)));
		
		return result;
	}

	private List<Offre> getOffreValidees(boolean dateSpecifiee) throws Exception{
		List<Offre> resultAccepte = new ArrayList<Offre>();

		String strReqAccepte = "SELECT Postule.IdLogement,Postule.IdUtilisateur,Postule.dateDebut,Postule.dateFin "
				+ "FROM Postule,Logement "
				+ "WHERE (Postule.Status = 1) "
				+ "AND Logement.Ville = ? "
				+ "AND Postule.IdLogement = Logement.IdLogement "
				+ (dateSpecifiee ? "AND Logement.DateDebut <= ? AND Logement.DateFin >= ? " : "")
				+ "ORDER BY Postule.IdLogement DESC, Postule.dateDebut ASC ";
		PreparedStatement sAccepte = Data.BDD_Connection.prepareStatement(strReqAccepte);
		sAccepte.setString(1,this.ville);
		if(dateSpecifiee){
			sAccepte.setDate(2, Date.valueOf(this.dateFin));
			sAccepte.setDate(3, Date.valueOf(this.dateDebut));
		}
		ResultSet rsAccepte = sAccepte.executeQuery();
		while (rsAccepte.next()){
			Logement l=Logement.getLogementById(rsAccepte.getInt(1));
			Utilisateur u=Utilisateur.getUtilisateurById(rsAccepte.getInt(2));
			resultAccepte.add(new Offre(l, u, rsAccepte.getDate(3), rsAccepte.getDate(4)));
		}
		return resultAccepte;
	}

	private List<Offre> getOffreValideesCompactees(boolean dateSpecifiee) throws Exception {		
		List<Offre> resultAccepteReste = new ArrayList<Offre>();
		Offre derniereOffre = null;
		boolean toAdd = true;

		for(Offre offre : getOffreValidees(dateSpecifiee)){
			if(!resultAccepteReste.isEmpty()){
				if(offre.getLogement().getIdLogement() == derniereOffre.getLogement().getIdLogement()){
					//comparer les dates
					Date finPlusUnJ = derniereOffre.getDateFin();
					int[] tabDate = CustomDate.splitDate(finPlusUnJ.toString());
					finPlusUnJ = Date.valueOf(CustomDate.creerStringDate(tabDate[0],tabDate[1],tabDate[2]+1));
					//compacter les offres
					//et mettre a jour la liste
					if(offre.getDateDebut().toString().equals(finPlusUnJ.toString())) {
						derniereOffre.setDateFin(offre.getDateFin());
						toAdd=false;
					}
				}
			}
			derniereOffre = offre;
			if(toAdd){
				resultAccepteReste.add(offre);
				toAdd=true;
			}
		}
		return resultAccepteReste;
	}

	private List<Offre> getRestes(List<Offre> offreValideesCompactees) throws Exception {
		List<Offre> result = new ArrayList<Offre>();
		Logement nouveauLogement = null;

		for(Offre offre : offreValideesCompactees){
			if(result.isEmpty()){
				nouveauLogement = Logement.getLogementById(offre.getLogement().getIdLogement());
			} else {
				//si il manquai le dernier bout du logement pr�c�dant
				if(offre.getLogement().getIdLogement() != nouveauLogement.getIdLogement()){
					if(nouveauLogement.getDateDebut().compareTo(nouveauLogement.getDateFin()) <= 0 ){
						//ajout de la plage libre
						result.add(new Offre(nouveauLogement,offre.getHebergeur(),nouveauLogement.getDateDebut(),nouveauLogement.getDateFin()));
					}
					nouveauLogement=offre.getLogement();
				}
				
				if(nouveauLogement.getDateDebut().compareTo(offre.getDateDebut()) < 0 ){
					//logement [---------]
					//demande  [---x*****]
					//date moins un jour
					Date debutMoinsUnJ = offre.getDateDebut();
					int[] tabDate = CustomDate.splitDate(debutMoinsUnJ.toString());
					debutMoinsUnJ = Date.valueOf(CustomDate.creerStringDate(tabDate[0],tabDate[1],tabDate[2]-1));
					//ajout de la plage libre
					result.add(new Offre(nouveauLogement,offre.getHebergeur(),nouveauLogement.getDateDebut(),debutMoinsUnJ));
				}
				if(nouveauLogement.getDateFin().compareTo(offre.getDateFin()) > 0 ){						// date moins un jour
					//logement [---------]
					//demande  [*****x---]
					//date plus un jour
					Date finPlusUnJ = offre.getDateDebut();
					int[] tabDate = CustomDate.splitDate(finPlusUnJ.toString());
					finPlusUnJ = Date.valueOf(CustomDate.creerStringDate(tabDate[0],tabDate[1],tabDate[2]+1));
					//maj du logment => il restera que la partie de droite (voir ci dessus)
					nouveauLogement.setDateDebutFin(finPlusUnJ,nouveauLogement.getDateFin());
				}
			}
		}
		return result;
	}
}
