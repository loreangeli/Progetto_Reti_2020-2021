/*
 * Classe per gestire gli utenti online
 * L'arraylist contiene i nickname degli user online
 */

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;


public class OnlineUsers implements Serializable {

    private static final long serialVersionUID = 1L;
    //array che contiene gli utenti online <nickname, 0>
    private ConcurrentHashMap<String,Integer> onlineusers;


    /*
     * costruttore
     */
    public OnlineUsers() {
        onlineusers = new ConcurrentHashMap<String,Integer> ();
    }

    public OnlineUsers(ConcurrentHashMap<String,Integer> map) {
        onlineusers = new ConcurrentHashMap<String,Integer> (map);
    }

    /*
     * aggiunge un utente online
     */
    public void add_user(String nickname) {
        onlineusers.put(nickname, 0);
    }

    /*
     * rimuove utente online
     */
    public void remove_user(String nickname) {
        onlineusers.remove(nickname);
    }

    /*
     * numero degli utenti online
     */
    public int get_length() {
        return onlineusers.size();
    }

    /*
     * restituisce la lista degli utenti online
     */
    public ConcurrentHashMap<String, Integer> get_list() {
        return onlineusers;
    }


    /*
     * metodo che controlla se l'utente passato come argomento è già loggato(è online)
     * return true se è già loggato
     * false altrimenti
     */
    public boolean contains(String nickname) {
        if (onlineusers.get(nickname) == null )
            return false;

        return true;
    }
}
