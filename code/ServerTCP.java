import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
/*  CLASSE LATO SERVER che attiva un server che si occupa di gestire connessioni tcp con i client
*/

public class ServerTCP implements Runnable {

    private boolean DEBUG = true;
    private int myPort;
    //lista aggiornata degli utenti online
    private OnlineUsers onlineusers;
    //lista degli utenti registrati
    private ConcurrentLinkedQueue<Utente> listuseregister;
    //oggetto per inviare notifica al client (rmi_callback)
    private RMIServerNotify server_notify;
    private ConcurrentHashMap<String, Progetto> progetti; //struttura che contiene i progetti

    private final int max_thread_active=20;

    public ServerTCP(int myPort, OnlineUsers onlineusers, ConcurrentLinkedQueue<Utente> listuseregister2, ConcurrentHashMap<String, Progetto> progetti,  RMIServerNotify server_notify, INotify stub) {
        this.myPort=myPort;
        this.onlineusers = onlineusers;
        this.listuseregister = listuseregister2;
        this.server_notify = server_notify;
        this.progetti = progetti;
    }

    /*
     * metodo sempre attivo che si occupa di accettare nuove connessioni TCP
     */
    public void run_server_tcp() {
        int i=0; //contatore per numerare i thread server
        ServerSocket server_socket;
        try {
            server_socket = new ServerSocket(myPort);
            ExecutorService pool = Executors.newFixedThreadPool(max_thread_active);
            if (DEBUG) System.out.println ("ATTIVO run_server_tcp in attesa di richieste di connessioni sulla porta " + myPort);

            while (true) {
                pool.execute(new TaskConnectionTCP(server_socket.accept(), onlineusers, listuseregister, progetti, server_notify, i));
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        run_server_tcp();
    }

}