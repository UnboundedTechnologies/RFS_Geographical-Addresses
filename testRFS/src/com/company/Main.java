package com.company;

import com.company.APIToolBox;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    static Dotenv dotenv = Dotenv.load();
    protected static final String TOKEN = dotenv.get("TOKEN");
    protected static final String keyAPI = dotenv.get("API_KEY");
    protected static final String tokenURL = "https://idp2.renault.com/nidp/oauth/nam/token";
    protected static final String postalAddressesURL = "https://apis-qa.renault.com/addresses/v1/postal-addresses";
    protected static final JSONObject fichierConf = new JSONObject();
    protected static int nbAdresses = 0;
    protected static int nbAdressesUniques = 0;
    protected static int page = 1;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String BLACK_BOLD_BRIGHT = "\033[1;30m"; // BLACK
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";

    public static JSONObject getFichierConf() throws JSONException {
        fichierConf.put("client_id", dotenv.get("CLIENT_ID"));
        fichierConf.put("client_secret", dotenv.get("CLIENT_SECRET"));
        fichierConf.put("api_key", dotenv.get("API_KEY"));
        fichierConf.put("username", dotenv.get("USERNAME"));
        fichierConf.put("password", dotenv.get("PASSWORD"));

        return fichierConf;
    }

    public static String getKeyAPI() {
        return keyAPI;
    }

    public static String getTokenURL() {
        return tokenURL;
    }

    public static String getPostalAddressesURL() { return postalAddressesURL; }

    public static JSONObject getTokenViaAPI() throws JSONException {
        JSONObject tokenResponseAPI = APIToolBox.getToken(getTokenURL());
        return tokenResponseAPI;
    }

    public static void getAccessPointsAPI() throws JSONException {
        //String tokenAPI = (APIToolBox.getToken(getTokenURL(), getFichierConf())).toString();
        while (!(page == 50)) {
            String postaleURL = "https://apis-qa.renault.com/enterprise-repositories-alliance-sites/v2/access-points-addresses?addressType=POSTAL&_page=" + page;

            JSONObject reponseAPI = APIToolBox.getJSONObject(postaleURL, TOKEN, getKeyAPI());

            JSONArray listeAccessPointIdentifier = reponseAPI.getJSONArray("access-points-addresses");

            nbAdresses = listeAccessPointIdentifier.length() - 1;
            System.out.println("NOMBRE D'ADRESSES: " + nbAdresses);

            nbAdressesUniques = listeAccessPointIdentifier.length() - 1;

            String[] accessPointIdentifier = new String[listeAccessPointIdentifier.length()];
            JSONObject responseFindGeographical = new JSONObject();

            JSONArray listeAccessPointsByIdentifier = new JSONArray();

            String bodyUpdateAPI;

            ArrayList<String> previousAddressSummary = new ArrayList<String>(listeAccessPointIdentifier.length()+1);
            ArrayList<String> previousRowId = new ArrayList<String>(listeAccessPointIdentifier.length()+1);

            //ajouter le premier element de la liste pour le comparer avec les autres
            previousAddressSummary.add(listeAccessPointIdentifier.getJSONObject(0).getString("addressSummary"));
            previousRowId.add(listeAccessPointIdentifier.getJSONObject(0).getString("rowId"));

            if (listeAccessPointIdentifier != null) {
                for (int i = 0; i < listeAccessPointIdentifier.length(); i++) {
                    //afficher l'indice de la liste
                    System.out.println(ANSI_GREEN_BACKGROUND + BLACK_BOLD_BRIGHT +"INDICE DE LA LISTE: " + i + ANSI_RESET);

                    //afficher le premier element de previousAddressSummary et previousRowId
                    System.out.println(ANSI_PURPLE_BACKGROUND + "previousAddressSummary: " + previousAddressSummary.get(0) + ANSI_RESET);
                    System.out.println(ANSI_PURPLE_BACKGROUND + "previousRowId: " + previousRowId.get(0) + ANSI_RESET);

                    JSONObject obj = listeAccessPointIdentifier.getJSONObject(i);

                    accessPointIdentifier[i] = obj.optString("accessPointIdentifier");

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //récupérer l'ancienne adresseSummary & rowId
                    previousAddressSummary.add(obj.optString("addressSummary"));
                    previousRowId.add(obj.optString("rowId"));

                    //afficher l'adresseSummary & rowId
                    System.out.println("\nADRESSE SUMMARY: " + previousAddressSummary.get(i+1));
                    System.out.println("ROW ID: " + previousRowId.get(i+1));

                    //vérifier si l'adresseSumary au rang i est égale à l'adresseSummary de l'adresse à l'indice i+1 dans la liste des adresses
                    if (previousAddressSummary.get(i).equals(previousAddressSummary.get(i + 1)) && !(previousRowId.get(i).equals(previousRowId.get(i + 1)))) {

                        //on décrémente nbAdressesUniques pour ne pas dépasser la limite de la liste des adresses
                        nbAdressesUniques--;

                        //on affiche le nombre d'adresses restantes
                        System.out.println(ANSI_PURPLE_BACKGROUND + BLACK_BOLD_BRIGHT + "NOMBRE D'ADRESSES UNIQUES: " + nbAdressesUniques + ANSI_RESET);
                        System.out.println(ANSI_PURPLE_BACKGROUND + BLACK_BOLD_BRIGHT + "ADRESSE EN DOUBLE TROUVEE"+ ANSI_RESET);

                        if ((i == 49) && (i == nbAdresses)) {
                            System.out.println(ANSI_GREEN_BACKGROUND + BLACK_BOLD_BRIGHT + "DENIERE ADRESSE POSTALE DE LA PAGE ATTEINTE! " + nbAdresses + ANSI_RESET);
                            page += 1;
                            nbAdressesUniques = 49;
                            System.out.println(ANSI_GREEN_BACKGROUND + BLACK_BOLD_BRIGHT + "PASSAGE A LA PAGE PAGE SUIVANTE: " + page + ANSI_RESET);

                            if (page == 50) {
                                System.out.println(ANSI_CYAN_BACKGROUND + BLACK_BOLD_BRIGHT + "PAGE AVANT LE BREAK: " + page + ANSI_RESET);
                                break;
                            }
                        }

                        //on saute l'adresse en double
                        continue;
                    }
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // PARTIE GET accessPointIdentifier
                    String findGeographical = "https://apis-qa.renault.com/enterprise-repositories-alliance-sites/v2/access-points-addresses?accessPointIdentifier=" + accessPointIdentifier[i];

                    responseFindGeographical = APIToolBox.getJSONObject(findGeographical, TOKEN, getKeyAPI());

                    listeAccessPointsByIdentifier = responseFindGeographical.getJSONArray("access-points-addresses");
                    System.out.println("IDENTIFIER ["+ ANSI_CYAN_BACKGROUND + BLACK_BOLD_BRIGHT + i + ANSI_RESET +"]: " + accessPointIdentifier[i]);
                    System.out.println("NOMBRE ["+ ANSI_CYAN_BACKGROUND + BLACK_BOLD_BRIGHT + i + ANSI_RESET +"]: " + listeAccessPointsByIdentifier.length());
                    System.out.println("SORTIE ["+ ANSI_CYAN_BACKGROUND + BLACK_BOLD_BRIGHT + i + ANSI_RESET +"]: " + listeAccessPointsByIdentifier);

                    ArrayList<String> listeIdentifierGeographic = new ArrayList<String>();
                    ArrayList<String> listeFinalIdentifiers = new ArrayList<String>();

                    ArrayList<String> identifier = new ArrayList<String>();
                    ArrayList<String> ligne2 = new ArrayList<String>();
                    ArrayList<String> additionalInformation = new ArrayList<String>();
                    ArrayList<String> territorialDivision = new ArrayList<String>();

                    if (listeAccessPointsByIdentifier != null) {
                        for (int j = 0; j < listeAccessPointsByIdentifier.length(); j++) {
                            JSONObject obj2 = listeAccessPointsByIdentifier.getJSONObject(j);

                            if (obj2.optString("addressType").equals("POSTAL")) {
                                System.out.println("VALEUR DE J 1: " + j);

                                listeFinalIdentifiers = new ArrayList<String>(listeAccessPointsByIdentifier.length());
                                listeFinalIdentifiers.add(obj2.optString("addressIdentifier"));
                                System.out.println("ADRESS IDENTIFIER " + listeFinalIdentifiers.get(0));

                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                                String findPostalByIdentifier = "https://apis-qa.renault.com/addresses/v1/postal-addresses?identifier=" + listeFinalIdentifiers.get(0);

                                JSONObject reponsePostaleAPI = APIToolBox.getJSONObject(findPostalByIdentifier, TOKEN, getKeyAPI());

                                JSONArray listePostalAddresses = reponsePostaleAPI.getJSONArray("postal-addresses");

                                identifier = new ArrayList<String>(listePostalAddresses.length());
                                ligne2 = new ArrayList<String>(listePostalAddresses.length());
                                territorialDivision = new ArrayList<String>(listePostalAddresses.length());

                                additionalInformation = new ArrayList<String>(listePostalAddresses.length());

                                if (listePostalAddresses != null) {
                                    for (int l = 0; l < listePostalAddresses.length(); l++) {
                                        //System.out.println("VALEUR DE L: " + l);
                                        JSONObject obj4 = listePostalAddresses.getJSONObject(l);

                                        //Identifier
                                        identifier.add(obj4.optString("identifier"));
                                        System.out.println(PURPLE_BOLD + "identifier[" + l + "]: " + identifier.get(0) + ANSI_RESET);

                                        //Ligne 2
                                        if(obj4.optString("line2").isEmpty()){
                                            ligne2.add("-");
                                            System.out.println(PURPLE_BOLD + "ligne2 VIDE: " + ligne2.get(0) + ANSI_RESET);
                                        }
                                        else {
                                            ligne2.add(obj4.optString("line2"));
                                            System.out.println(PURPLE_BOLD + "ligne2 PLEINE: " + ligne2.get(0) + ANSI_RESET);
                                        }

                                        //Ligne 5 aka Territorial Division
                                        if(obj4.optString("line5").isEmpty()){
                                            territorialDivision.add("");
                                            System.out.println(PURPLE_BOLD + "territorialDivision VIDE: " + territorialDivision.get(0) + ANSI_RESET);
                                        }
                                        else {
                                            territorialDivision.add(obj4.optString("line5"));
                                            System.out.println(PURPLE_BOLD + "territorialDivision PLEINE: " + territorialDivision.get(0) + ANSI_RESET);
                                        }

                                        //additionalInformation
                                        if (obj4.optString("line3").isEmpty() && !(obj4.optString("line4").isEmpty())) {
                                            additionalInformation.add((obj4.optString("line4")).trim());
                                        }
                                        if (obj4.optString("line4").isEmpty() && !(obj4.optString("line3").isEmpty())) {
                                            additionalInformation.add((obj4.optString("line3")).trim());
                                        }
                                        if (obj4.optString("line3").isEmpty() && obj4.optString("line4").isEmpty()) {
                                            additionalInformation.add("");
                                        }
                                        else{
                                            String newAdditionalInformation = new String(new char[35 - (obj4.optString("line3").length())]).replace('\0',' ') + obj4.optString("line4");
                                            additionalInformation.add((obj4.optString("line3") + newAdditionalInformation));
                                        }

                                        System.out.println(PURPLE_BOLD + "LIGNE 3 " + obj4.optString("line3") + " LIGNE 4 " + obj4.optString("line4") + ANSI_RESET);
                                        System.out.println(PURPLE_BOLD + "additionalInformation[" + l + "]: " + additionalInformation.get(0) + "\n" + ANSI_RESET);

                                        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                    }
                                }
                            }


                            if (obj2.optString("addressType").equals("GEOGRAPHICAL")) {
                                //System.out.println("VALEUR DE J 2: " + j);

                                listeIdentifierGeographic = new ArrayList<String>(listeAccessPointsByIdentifier.length());

                                listeIdentifierGeographic.add(obj2.optString("addressIdentifier"));
                                System.out.println("Identifier Geographique AVANT[" + j + "]: " + listeIdentifierGeographic.get(0));

                                //Rechercher les adresses géographiques via l'addressIdentifier obtenu précédemment
                                String geographicalByRowId = "https://apis-qa.renault.com/addresses/v1/geographical-addresses?identifier=" + listeIdentifierGeographic.get(0);

                                JSONObject responseGeographicalRowId = APIToolBox.getJSONObject(geographicalByRowId, TOKEN, getKeyAPI());

                                System.out.println("REPONSE API: " + responseGeographicalRowId);

                                JSONArray listeGeographicalByRowId = new JSONArray();

                                listeGeographicalByRowId = responseGeographicalRowId.getJSONArray("geographical-addresses");

                                System.out.println("listeGeographicalIdentifier: " + listeGeographicalByRowId);

                                String[] listeRowIDGeographical = new String[listeGeographicalByRowId.length()];

                                //INFO POUR UPDATE
                                String[] gAdditionnalInformation = new String[listeGeographicalByRowId.length()];
                                String[] gDeliveryPoint = new String[listeGeographicalByRowId.length()];
                                String[] gIdentifier = new String[listeGeographicalByRowId.length()];
                                String[] gExtension = new String[listeGeographicalByRowId.length()];
                                String[] gPostalCode = new String[listeGeographicalByRowId.length()];
                                String[] gCoordinates = new String[listeGeographicalByRowId.length()];
                                String[] gLocality = new String[listeGeographicalByRowId.length()];
                                String[] gWayNumber = new String[listeGeographicalByRowId.length()];
                                String[] gCityId = new String[listeGeographicalByRowId.length()];
                                String[] gCountryId = new String[listeGeographicalByRowId.length()];
                                //ROW ID HERE!!!
                                String[] gStreetName = new String[listeGeographicalByRowId.length()];
                                String[] gCityName = new String[listeGeographicalByRowId.length()];
                                String[] gCountryCode = new String[listeGeographicalByRowId.length()];
                                String[] gWayLabel = new String[listeGeographicalByRowId.length()];
                                String[] gCountryName = new String[listeGeographicalByRowId.length()];
                                String[] gTerritorialDivision = new String[listeGeographicalByRowId.length()];

                                if (listeGeographicalByRowId != null) {
                                    for (int k = 0; k < listeGeographicalByRowId.length(); k++) {
                                        System.out.println("VALEUR DE K: " + k);

                                        JSONObject obj3 = listeGeographicalByRowId.getJSONObject(k);

                                        listeRowIDGeographical[k] = obj3.optString("rowId");
                                        System.out.println("listeRowIDGeographical[" + k + "]: " + listeRowIDGeographical[k]);

                                        //INIT INFO UPDATE
                                        gAdditionnalInformation[k] = obj3.optString("additionalInformation");
                                        gDeliveryPoint[k] = obj3.optString("deliveryPoint");
                                        gIdentifier[k] = obj3.optString("identifier");
                                        System.out.println("gIdentifier[" + k + "]: " + gIdentifier[k]);

                                        gExtension[k] = obj3.optString("extension");
                                        gPostalCode[k] = obj3.optString("postalCode");
                                        gCoordinates[k] = obj3.optString("coordinates");
                                        gLocality[k] = obj3.optString("locality");
                                        gWayNumber[k] = obj3.optString("wayNumber");
                                        gCityId[k] = obj3.optString("cityId");
                                        gCountryId[k] = obj3.optString("countryId");
                                        gStreetName[k] = obj3.optString("streetName");
                                        gCityName[k] = obj3.optString("cityName");
                                        gCountryCode[k] = obj3.optString("countryCode");
                                        gWayLabel[k] = obj3.optString("wayLabel");
                                        gCountryName[k] = obj3.optString("countryName");
                                        gTerritorialDivision[k] = obj3.optString("territorialDivision");

                                        if ((listeIdentifierGeographic.get(0)).equals(gIdentifier[k])) {
                                                if ((identifier.get(0)).equals(listeFinalIdentifiers.get(0))) {

                                                if ((gAdditionnalInformation[k].isEmpty()) && !(gStreetName[k].isEmpty()) && !(gTerritorialDivision[k].isEmpty())) {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + additionalInformation.get(0) + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + gStreetName[k] + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + gTerritorialDivision[k] + "\"}";
                                                    System.out.println("IF ADDITIONNAL VIDE");
                                                } else if (!(gAdditionnalInformation[k].isEmpty()) && (gStreetName[k].isEmpty()) && !(gTerritorialDivision[k].isEmpty())) {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + gAdditionnalInformation[k] + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + ligne2.get(0) + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + gTerritorialDivision[k] + "\"}";
                                                    System.out.println("IF STREET NAME VIDE");
                                                } else if (!(gAdditionnalInformation[k].isEmpty()) && !(gStreetName[k].isEmpty()) && (gTerritorialDivision[k].isEmpty())) {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + gAdditionnalInformation[k] + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + gStreetName[k] + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + territorialDivision.get(0) + "\"}";
                                                    System.out.println("IF TERRITORIAL DIVISION VIDE");
                                                } else if((gAdditionnalInformation[k].isEmpty()) && (gStreetName[k].isEmpty()) && !(gTerritorialDivision[k].isEmpty())) {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + additionalInformation.get(0) + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + ligne2.get(0) + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + gTerritorialDivision[k] + "\"}";
                                                    System.out.println("IF TOUT VIDE SAUF TERRITORIAL DIVISION");
                                                }
                                                else if(!(gAdditionnalInformation[k].isEmpty()) && (gStreetName[k].isEmpty()) && (gTerritorialDivision[k].isEmpty())) {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + gAdditionnalInformation[k] + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + ligne2.get(0) + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + territorialDivision.get(0) + "\"}";
                                                    System.out.println("IF TOUT VIDE SAUF ADDITIONNAL INFORMATION");
                                                }
                                                else if((gAdditionnalInformation[k].isEmpty()) && !(gStreetName[k].isEmpty()) && (gTerritorialDivision[k].isEmpty())) {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + additionalInformation.get(0) + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + gStreetName[k] + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + territorialDivision.get(0) + "\"}";
                                                    System.out.println("IF TOUT VIDE SAUF STREET NAME");
                                                }
                                                else if (!(gAdditionnalInformation[k].isEmpty()) && !(gStreetName[k].isEmpty()) && !(gTerritorialDivision[k].isEmpty())) {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + gAdditionnalInformation[k] + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + gStreetName[k] + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + gTerritorialDivision[k] + "\"}";
                                                    System.out.println("IF LES 3 EXISTENT");
                                                } else {
                                                    bodyUpdateAPI = "{\"additionalInformation\":\"" + additionalInformation.get(0) + "\",\"deliveryPoint\":\"" + gDeliveryPoint[k] + "\",\"identifier\":\"" + gIdentifier[k] + "\",\"extension\":\"" + gExtension[k] + "\",\"postalCode\":\"" + gPostalCode[k] + "\",\"coordinates\":\"" + gCoordinates[k] + "\",\"locality\":\"" + gLocality[k] + "\",\"wayNumber\":\"" + gWayNumber[k] + "\",\"cityId\":\"" + gCityId[k] + "\",\"countryId\":\"" + gCountryId[k] + "\",\"rowId\":\"" + listeRowIDGeographical[k] + "\",\"streetName\":\"" + ligne2.get(0) + "\",\"cityName\":\"" + gCityName[k] + "\",\"countryCode\":\"" + gCountryCode[k] + "\",\"wayLabel\":\"" + gWayLabel[k] + "\",\"countryName\":\"" + gCountryName[k] + "\",\"territorialDivision\":\"" + territorialDivision.get(0) + "\"}";
                                                    System.out.println("ELSE LES 3 VIDES");
                                                }

                                                System.out.println("BODY: " + bodyUpdateAPI);

                                                String finalUrlUpdate = "https://apis-qa.renault.com/addresses/v1/geographical-addresses/" + listeRowIDGeographical[k];
                                                JSONObject finalUpdate = APIToolBox.putJSONObject(finalUrlUpdate, TOKEN, getKeyAPI(), bodyUpdateAPI);
                                                System.out.println("API Update: "+finalUpdate);

                                                JSONObject testUPDATE = APIToolBox.update(listeRowIDGeographical[k], TOKEN, getKeyAPI(), bodyUpdateAPI);
                                                System.out.println("API Update: " + testUPDATE);

                                                System.out.println("UPDATE REUSSIE");

                                                if ((i == 49) && (i == nbAdresses)) {
                                                    System.out.println(ANSI_GREEN_BACKGROUND + BLACK_BOLD_BRIGHT + "DENIERE ADRESSE POSTALE DE LA PAGE ATTEINTE! " + nbAdressesUniques + ANSI_RESET);
                                                    page += 1;
                                                    nbAdressesUniques = 49;
                                                    System.out.println(ANSI_GREEN_BACKGROUND + BLACK_BOLD_BRIGHT + "PASSAGE A LA PAGE PAGE SUIVANTE: " + page + ANSI_RESET);

                                                    if (page == 50) {
                                                        System.out.println(ANSI_CYAN_BACKGROUND + BLACK_BOLD_BRIGHT + "PAGE AVANT LE BREAK: " + page + ANSI_RESET);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws JSONException {
        getFichierConf();
        System.out.println("FICHIER CONF: " + getFichierConf());

        getAccessPointsAPI();
    }
}
