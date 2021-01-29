/*
 * INTERFACCIA LATO SERVER
 * Interfaccia Remota RMI per la registrazione di un utente
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRegister extends Remote {

    /*
     * return:
     * 0 la registrazione è stata fatta con successo
     * 1 nickUtente o password null
     * -1 in caso di nickUtente già occupato
     * -2 altro
     */
    int register(String nickUtente, String password) throws RemoteException;

}
