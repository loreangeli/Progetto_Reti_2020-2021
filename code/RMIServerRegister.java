import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * CLASSE LATO SERVER
 * Questa classe contiene i metodi lato SERVER per la registrazione di un utente tramite RMI
 */

public class RMIServerRegister extends RemoteServer implements IRegister {

    private boolean DEBUG = true;
    private static final long serialVersionUID = 7109748990641379374L;
    //oggetto che contiene la lista degli utenti registrati
    private ConcurrentLinkedQueue<Utente> listuseregister;


    /*
     * costruttore
     */
    public RMIServerRegister(ConcurrentLinkedQueue<Utente> listuseregister2) {
        this.listuseregister = listuseregister2;
    }

    /*
     * avvio thread per rmi
     */
    public void run_rmiserver(ConcurrentLinkedQueue<Utente> listuseregister) {
        int port=3000;
        RMIServerRegister rmiserver = new RMIServerRegister(listuseregister);

        try {
            // metodo per esportare l'oggetto remoto e mi crea lo stub  (cioè il rappresentante dell'oggetto remoto)
            IRegister stub = (IRegister) UnicastRemoteObject.exportObject(rmiserver, 0);
            //registro l'oggetto nel registry
            LocateRegistry.createRegistry(port);
            Registry r = LocateRegistry.getRegistry(port);
            //registro nel registry lo stub generato precedentemente con il nome "Server_Register"
            r.rebind("Server-Register", stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (DEBUG)
            System.out.println("server rmi-ready");
    }

    @Override
    /*
     * metodo per registrare un utente al servizio Worth
     * la registrazione crea un file nella cartella Utenti
     */
    public synchronized int register(String nickUtente, String password) throws RemoteException {
        if (nickUtente == null || password==null)
            return 1;

        /* creo cartella del nuovo utente */
        String pathFolderUtente = "Utenti/"+nickUtente;
        File folder2 = new File(pathFolderUtente);
        // Verifichiamo che non sia già esistente come cartella
        if (!folder2.isDirectory()) // In caso non sia già presente la creiamo
            folder2.mkdir();
        else { //quel nickUtente è già occupato/esiste già
            return -1;
        }

        /* Utente to JSON (creo file)*/
        Utente user = new Utente(nickUtente, password);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("Utenti/"+nickUtente+"/"+"login"+".json");
            file.createNewFile();
            objectMapper.writeValue(file, user);
            //aggiorno lista utenti registrati
            listuseregister.add(user);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return -2;
    }

}