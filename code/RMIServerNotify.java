import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/*
 * CLASSE LATO SERVER che implementa i metodi per RMI CALLBACKS
 */

public class RMIServerNotify extends RemoteObject implements INotify {

    private static final long serialVersionUID = 4239638003429779414L;
    //oggetto che contiene la lista degli utenti collegati al sistema di notifica (callbacks)
    private List <NotifyEventInterface> clients;
    //hashmap delle coppie <nickname,stub> utile per disiscrivere un utente dal sistema di notifiche in caso di disconnessione improvvisa.
    private ConcurrentHashMap<String, NotifyEventInterface> map;
    boolean DEBUG = true;


    public RMIServerNotify() throws RemoteException {
        //init strutture dati
        clients = new ArrayList<NotifyEventInterface>();
        map = new ConcurrentHashMap<String, NotifyEventInterface>();
    }

    /*
     * registro l'utente al sistema di callbacks
     */
    @Override
    public synchronized void registerForCallback(NotifyEventInterface ClientInterface, String nickname) throws RemoteException {
        if (!clients.contains(ClientInterface)) {
            clients.add(ClientInterface);
            map.put(nickname, ClientInterface);
        }
        if (DEBUG) System.out.println("[CallBack] client "+nickname+" registrato al sistema di notifiche callback");
    }

    /*
     * disiscrivo l'utente al sistema di callbacks
     */
    @Override
    public synchronized void unregisterForCallback(NotifyEventInterface Client, String nickname) throws RemoteException {

        if (clients.remove(Client))
        {
            //tenta di rimuovere il Client dalla hashmap
            try {
                map.remove(nickname);
            }
            catch (NullPointerException e) {
                System.out.println(e);
            }

            if (DEBUG) System.out.println("[CallBack] client "+nickname+" unregistered to CallBack");
        }
        else {
            if (DEBUG) System.out.println("[CallBack] unable to unregister client "+nickname);
        }
    }

    /*
     * notifica la variazione di stato da parte di un utente registrato (un utente è passato da online a offline o viceversa)
     * quando viene chiamato questo metodo invia una notifica a tutti i client registrati a questo sistema di callbacks
     */
    public void update(OnlineUsers onlineusers) throws RemoteException {
        Iterator<NotifyEventInterface> i = clients.iterator( );

        while (i.hasNext()) {
            NotifyEventInterface client =
                    (NotifyEventInterface) i.next();
            client.notifyEvent(onlineusers);
        }
    }

    /*
     * disiscrive l'utente dalla lista degli utenti registrati al sistema di callbacks tramite "nickname"
     * questo metodo viene usato per togliere il cliente dal sistema di notifiche quando il cliente chiude improvvisamente il client.
     * NOTA BENE: essendo il client ad occuparsi della fase di disiscrizione al servizio di callback in caso di chiusura improvvisa di questo,
     * è il server che dovrà gestire la disiscrizione. il server deve quindi rimuovere dalla lista "clients" l'oggetto associato all'utente disconnesso.
     * utilizza la struttura "private ConcurrentHashMap<String, NotifyEventInterface> map;" che lega il nome del client registrato alla callback con lo stub
     * associato ed è utile per disiscrivere un utente dal sistema di notifiche in caso di disconnessione improvvisa. si va a cercare lo stub nella hashmap "map"
     * e si rimuove il cliente dalla lista "clients".
     */
    public synchronized void fix_unregisterForCallback (String nickname) {
        NotifyEventInterface Client = map.get(nickname);

        if (clients.remove(Client))
        {
            if (DEBUG) System.out.println("[CallBack] client "+nickname+" unregistered to CallBack");
        }
        else {
            if (DEBUG) System.out.println("[CallBack] unable to unregister client "+nickname);
        }
    }

}
