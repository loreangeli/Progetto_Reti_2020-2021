
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * CLASSE LATO CLIENT che gestisce RMI CALLBACK mantenendo aggiornata la struttura "listuseronline"
 */

public class NotifyEventImpl extends RemoteObject implements NotifyEventInterface {

    private boolean DEBUG = false;
    private static final long serialVersionUID = 4998877407786628519L;
    private ConcurrentLinkedQueue<String> listuseronline;


    public NotifyEventImpl(ConcurrentLinkedQueue<String> listuseronline2) throws RemoteException {
        super();
        this.listuseronline = listuseronline2;
    }

    /*
     * Metodo invocato dal server per notificare un evento ad un client remoto.
     * se viene notificato un aggiornamento viene restituita la lista degli utenti online
     */
    @Override
    public void notifyEvent (OnlineUsers users) throws RemoteException {
        //rimuove ogni elemento della lista
        listuseronline.clear();

        //aggiorno lista degli utenti online
        ConcurrentHashMap<String, Integer> tmp = new ConcurrentHashMap<String, Integer> (users.get_list()); //hashmap di supporto
        //copio tutta la hashmap nell'array
        for (String key : tmp.keySet()) {
            listuseronline.add(key); //aggiunge elemento alla lista
        }

        if (DEBUG) {
            System.out.print("[CallBack] Update event received: ricevuta lista utenti online ");
            if (listuseronline != null) System.out.println(listuseronline);
            else System.out.println("[CallBack] errore, lista utenti online null");
        }
    }

}