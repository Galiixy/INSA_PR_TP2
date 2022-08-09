package http.server;

import java.util.HashMap;

/**
 * Classe RequestHTTP : g�re la lecture d'une requ�te
 * @author gaelle & mathis
 */
public class RequestHTTP {

    private final HashMap<String, String> header = new HashMap<>();
    private final StringBuilder body = new StringBuilder();
    private final String httpVerb;
    private final String ressourcePath;

    /**
     * Constructeur de RequestHTTP
     * @param requestAsString : requ�te transmise par le navigateur
     * d�ocnstruit la requ�te avec l'ent�te (verbe HTTP, chemin de la ressource et infos header) et body.
     */
    public RequestHTTP(String requestAsString) {
        // Get each lines of the request
        String[] lines = requestAsString.split(System.lineSeparator());

        // Treat the first line
        httpVerb = lines[0].split(" ")[0];
        ressourcePath = lines[0].split(" ")[1];

        int numLine = 1;

        // Treat the header
        while (!lines[numLine].equals("\r")) {
            String key = lines[numLine].split(": ")[0];
            String value = lines[numLine].split(": ")[1];
            value = value.split("\r")[0];
            header.put(key, value);
            ++numLine;
        }

        // Treat the body
        ++numLine;
        while (numLine < lines.length) {
            body.append(lines[numLine]);
            if (numLine != lines.length-1)
                body.append(System.lineSeparator());
            ++numLine;
        }
    }

    /**
     * getHeaderProperty : retourne une information de l'ent�te Header
     * @param propertyName : nom de l'information � retourner
     * @return la propri�t� du header
     */
    public String getHeaderProperty(String propertyName) {
        return header.get(propertyName);
    }
    
    /**
     * getBody 
     * @return String : le body de la requ�te
     */
    public String getBody() {
        return body.toString();
    }
    
    /**
     * getHttpVerb
     * @return String : le verbe HTTP
     */
    public String getHttpVerb() {
        return httpVerb;
    }
    
    /**
     * getRessourcePath
     * @return String : le chemin de la ressource
     */
    public String getRessourcePath() {
        return ressourcePath;
    }

}
