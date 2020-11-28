package tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Classe abstraite représentant un modèle sérialisable pour être transmis
 * en tant que contenu de message entre agents.
 * Implémente les méthodes génériques de sérialisation et de désérialisation.
 */
public abstract class Model {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Sérialise l'objet courant.
     * @return Chaîne de caractère représentant l'objet courant.
     */
    public String serialize() {
        try {
            return getMapper().writeValueAsString(this);
        } catch(JsonProcessingException ex) {
            System.err.println("Unable to serialize" + this.getClass().getCanonicalName() + " object");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Désérialise une représentation d'un objet de type dérivant de Model.
     * @param serial Objet enfant de Model sérialisé.
     * @param c Instance de classe du type concret de l'objet sérialisé.
     * @return Instance du type concret de l'objet sérialisé.
     */
    public static <T extends Model> T deserialize(String serial, Class<T> c) {
        try {
            return getMapper().readValue(serial, c);
        } catch (IOException e) {
            System.err.println("Unable to deserialize" + c.getCanonicalName() + " object");
            e.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> deserializeToList(String serial, Class<T> c){
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, c);
        try {
            List<T> ts = mapper.readValue(serial, listType);
            return ts;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ObjectMapper getMapper() {
        return mapper;
    }
}
