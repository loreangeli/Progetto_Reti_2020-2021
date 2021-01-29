import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * INTERFACCIA LATO CLIENT utilizzata per implementare il sistema di notifiche per ricevere
 * aggiornamenti sullo stato degli utenti registrati: viene notificato il client quando un utente cambia stato da online a offline o viceversa
 */

public interface NotifyEventInterface extends Remote {

    /*
     *  Metodo invocato dal server per notificare un evento ad un client remoto.
     *  Questo metodo si occupa di notificare eventi sullo stato degli utenti
     */
    public void notifyEvent(OnlineUsers onlineusers) throws RemoteException;

}
