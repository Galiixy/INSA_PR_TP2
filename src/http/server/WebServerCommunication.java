package http.server;

import com.google.gson.Gson;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * WebServerCommunication : classe de gestion des communication HTTP possible avec le serveur
 * @author gaelle et mathis
 */
public class WebServerCommunication extends Thread {

    private final Socket socket;

    private RequestHTTP requestHTTP;
    
    /**
     * Constructeur de WebServerCommunication
     * @param socket socket de communication client - serveur
     */
    WebServerCommunication(Socket socket) {
        this.socket = socket;
    }

    @Override
    /**
     * Execution de l'instance de communication
     * Cr�e la requ�te HTTP � partir des information envoy�s de la socket 
     * puis appelle les m�thodes suivants les verbes
     */
    public void run() {
        String request = readRequest();

        String httpVerb = "";
        String ressourcePath = "";

        if (!request.equals("")) {
            requestHTTP = new RequestHTTP(request);
            httpVerb = requestHTTP.getHttpVerb();
        }

        switch (httpVerb) {
            case "GET":
                requestGet();
                break;
            case "HEAD":
                requestHead();
                break;
            case "POST" :
                requestPost();
                break;
            case "PUT" :
                requestPut();
                break;
            case "DELETE" :
                requestDelete();
                break;
            case "" :
                break;
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * requestGet : Action du verbe Get : renvoie la ressource demand�e � l'utilisteur
     * par la socket de communication. 
     * gestion du ressourcePath (chemin de la ressource) s'il existe pour la transmettre.
     * Renvoie 200 si la ressource est bien transmise
     * Renvoie 404 si la ressource n'est pas trouv�e
     */
    private void requestGet() {
        try {
        	String ressourcePath = requestHTTP.getRessourcePath();
        	
            File file = new File("./src/http/ressources" + ressourcePath);

            if (!file.exists()) throw new IOException();

            String mimesType = Files.probeContentType(file.toPath());

            HashMap<String, String> headerParams = new HashMap<>();
            headerParams.put("Content-Type", mimesType);
            headerParams.put("Server", "Bot");

            sendRequestHeader("200", "OK", headerParams);

            Files.copy(file.toPath(), socket.getOutputStream());

        } catch (IOException ex) {
            HashMap<String, String> headerParams = new HashMap<>();
            headerParams.put("Content-Type", "text/html");
            headerParams.put("Server", "Bot");
            sendRequestHeader("404", "Not Found", headerParams);
            sendRequestBody("<h1>Resource Not Found</h1>");
        }
    }

    /**
     * requestHead : Action du verbe Head : demande les en-t�tes qui seraient 
     * retourn�s si la ressource sp�cifi�e �tait demand�e avec une m�thode HTTP GET
     * r�cup�re le ressourcePath de la requ�te 
     * Renvoie 200 si la ressource est bien transmise
     * Renvoie 404 si la ressource n'est pas trouv�e
     */
    private void requestHead() {
        try {
        	String ressourcePath = requestHTTP.getRessourcePath();
        	
            File file = new File("./src/http/ressources" + ressourcePath);

            if (!file.exists()) throw new IOException();

            String mimesType = Files.probeContentType(file.toPath());

            HashMap<String, String> headerParams = new HashMap<>();
            headerParams.put("Content-Type", mimesType);
            headerParams.put("Server", "Bot");

            sendRequestHeader("200", "OK", headerParams);

            sendRequestBody("");
        } catch (IOException ex) {
            HashMap<String, String> headerParams = new HashMap<>();
            headerParams.put("Content-Type", "text/html");
            headerParams.put("Server", "Bot");
            sendRequestHeader("404", "Not Found", headerParams);
            sendRequestBody("");
        }
    }

    /**
     * requestPost : Action du verbe POST : envoie une ressource au serveur
     * R�cup�re le body de la requ�te pour l'envoyer en JSON au client
     */
    private void requestPost() {
        // Get Body
        String bodyString = requestHTTP.getBody();
        // Here the body can be used with some actions

        // In our case, the body come from an HTML Form and have key1=value1&key2=value2 form
        // We want to serialize it to return has a JSON
        HashMap<String, String> body = new HashMap<>();
        for (String keyValue : bodyString.split("&")) {
            String key = URLDecoder.decode(keyValue.split("=")[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(keyValue.split("=")[1], StandardCharsets.UTF_8);
            body.put(key, value);
        }
        String bodyJson = new Gson().toJson(body);

        // Send JSON to Client
        HashMap<String, String> headerParams = new HashMap<>();
        headerParams.put("Content-Type", "application/json");
        headerParams.put("Server", "Bot");
        sendRequestHeader("200", "OK", headerParams);
        sendRequestBody(bodyJson);
    }
    
    /**
     * requestPut : Action du verbe PUT : cr�e une nouvelle ressource ou remplace une 
     * repr�sentation de la ressource cibl�e par le contenu de la requ�te.
     * r�cup�re le ressourcePath, le body et le content type de la requ�te
     * Renvoie 201 si la ressource est bien cr�� et la renvoie � l'utilisateur
     * Renvoie 415 si le m�dia n'est pas support�
     * Renvoie 500 si erreur du serveur
     */
    private void requestPut() {
    	
    	String ressourcePath = requestHTTP.getRessourcePath();
    	
        String body = requestHTTP.getBody();

        HashMap<String, String> headerParams = new HashMap<>();
        headerParams.put("Server", "Bot");

        String contentType = requestHTTP.getHeaderProperty("Content-Type");
        String extension = "";
        try {
            extension = getExtension(contentType);
            if (extension.isEmpty()) throw new Exception();
        } catch (Exception e) {
            headerParams.put("Content-Type", "text/html");
            sendRequestHeader("415", "Unsupported Media Type", headerParams);
            sendRequestBody("<h1>Unsupported Media Type</h1>");
            return;
        }

        try {
            Files.writeString(Paths.get("./src/http/ressources/" + ressourcePath + "." + extension), body, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            headerParams.put("Content-Type", "text/html");
            sendRequestHeader("500", "Internal Server Error", headerParams);
            sendRequestBody("<h1>Internal Server Error</h1>");
            return;
        }

        // Send the file content who have been created
        headerParams.put("Content-Type", "text/html");
        sendRequestHeader("201", "Created", headerParams);
        sendRequestBody(body);

    }
    
    /**
     * requestDelete : Action du verbe Delete : supprime la ressource indiqu�e.
     * r�cup�re le ressourcePath de la requ�te. 
     * Renvoie 204 si la ressource est bien supprim�, renvoie pas de contenu en retour
     * Renvoie 403 si la ressource ne peut pas �tre supprim�e : dossier non vide ou pas les doits
     * Renvoie 404 si la ressource n'est pas trouv�e
     * Renvoie 500 si erreur du serveur
     */
    private void requestDelete() {
    	
    	String ressourcePath = requestHTTP.getRessourcePath();
    	
        HashMap<String, String> headerParams = new HashMap<>();
        headerParams.put("Server", "Bot");
        try {
            Files.delete(Paths.get("./src/http/ressources/" + ressourcePath));
            headerParams.put("Content-Type", "text/html");
            sendRequestHeader("204", "No Content", headerParams);
            sendRequestBody("");
        } catch (NoSuchFileException x) {
            sendRequestHeader("404", "Not Found", headerParams);
            sendRequestBody("<h1>Resource Not Found</h1>");
        } catch (DirectoryNotEmptyException x) {
            headerParams.put("Content-Type", "text/html");
            sendRequestHeader("403", "Forbidden", headerParams);
            sendRequestBody("<h1>Forbidden : Directory not empty</h1>");
        } catch (IOException x) {
            headerParams.put("Content-Type", "text/html");
            sendRequestHeader("403", "Forbidden", headerParams);
            sendRequestBody("<h1>Forbidden : Server can't remove this resource</h1>");
        }
    }
    
    /**
     * readRequest : r�cup�re les donn�es venant de la socket
     * @return String requ�te 
     */
    private String readRequest() {
        StringBuilder request = new StringBuilder();
        while(true) {
            try {
                if(socket.getInputStream().available() == 0) break;
                request.append((char) socket.getInputStream().read());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return request.toString();
    }

    /**
     * sendRequestHeader : transmet l'ent�te � la socket 
     * @param statusCode : 200,201,204,403,404,500
     * @param statusMessage : message relatif au code 
     * @param properties : param�tres de l'ent�te (Content-type,...)
     */
    private void sendRequestHeader(String statusCode, String statusMessage, HashMap<String, String> properties) {
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.0 ").append(statusCode).append(" ").append(statusMessage).append(System.lineSeparator());
        for(Map.Entry<String, String> property : properties.entrySet() ) {
            header
                    .append(property.getKey())
                    .append(": ")
                    .append(property.getValue())
                    .append(System.lineSeparator());
        }
        header.append(System.lineSeparator());
        try {
            socket.getOutputStream().write(header.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * sendRequestBody : transmet le body � la socket
     * @param body : corps des donn�es � renvoyer
     */
    private void sendRequestBody(String body) {
        try {
            socket.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * getExtension : r�cup�re l'extension du fichier
     * @param mimeType : standard permettant d'indiquer la nature et le format d'un document
     * @return l'extension associ� au sous-type (text/javascript --> js) retourne l'extension sans le point
     * @throws MimeTypeException
     */
    private String getExtension(String mimeType) throws MimeTypeException {
        MimeType mime = MimeTypes.getDefaultMimeTypes().forName(mimeType);
        String extention = (mime != null) ? mime.getExtension() : "";
        if (!extention.isEmpty()) extention = extention.substring(1);
        return extention;
    }

}
