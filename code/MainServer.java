import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * MAIN SERVER
 */

public class MainServer {

    public static void main(String[] args) throws RemoteException {


        /* VARIABILI DEBUG */
        boolean DEBUG = true;


        /* init LISTA PROGETTI */
        /*  crea cartella Progetti se non esiste */
        String pathFolderName = "Progetti";
        File folder = new File(pathFolderName);
        // Verifichiamo che non sia già esistente come cartella
        if (!folder.isDirectory()) // In caso non sia già presente la creiamo
            folder.mkdir();
        //struttura dati principale che contiene i progetti, la chiave è il nome del progetto (univoca per ogni progetto)
        ConcurrentHashMap<String, Progetto> progetti = new ConcurrentHashMap<String, Progetto>();
        init_listproject(progetti);
        System.out.println("** stampa lista progetti (se esistenti)**");
        int i=1;
        for (String nameproject : progetti.keySet()) {
            Progetto objectprog = progetti.get(nameproject);
            System.out.print("["+i+"] ");
            objectprog.print_progetto();
            i++;
        }


        /* init CARTELLE VARIE */
        /* crea cartella Utenti se non esiste */
        pathFolderName = "Utenti";
        folder = new File(pathFolderName);
        // Verifichiamo che non sia già esistente come cartella
        if (!folder.isDirectory()) // In caso non sia già presente la creiamo
            folder.mkdir();


        /* init RMICALLBACK*/
        int port_rmi_notify = 5000;
        RMIServerNotify server_notify = new RMIServerNotify();
        INotify stub=(INotify) UnicastRemoteObject.exportObject (server_notify,39000);
        String name = "server-callback";
        LocateRegistry.createRegistry(port_rmi_notify);
        Registry registry = null;
        try {
            registry=LocateRegistry.getRegistry(5000);
            registry.bind (name, stub);
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }


        /* init RMI SERVER REGISTER */
        ConcurrentLinkedQueue<Utente> listuseregister = new ConcurrentLinkedQueue<Utente>(); //oggetto che contiene la lista degli utenti registrati e i messaggi
        //inizializza listuseregister NOTA: leggi commenti metodo per capire a cosa serve
        init_listregisteruser(listuseregister);
        if (DEBUG) {
            print_listuseregister(listuseregister);

        }
        RMIServerRegister register = new RMIServerRegister(listuseregister);
        //crea processo demone (sempre attivo)
        register.run_rmiserver(listuseregister);


        /* login e altri comandi (logout ecc..) */
        int myPort=4000;
        OnlineUsers onlineusers = new OnlineUsers(); //oggetto che contiene la lista degli utenti online
        ServerTCP serverconnection_tcp = new ServerTCP(myPort, onlineusers, listuseregister, progetti, server_notify, stub);
        //avvio server per gestire le nuove connessioni tcp con i client
        Thread server_tcp = new Thread(serverconnection_tcp);
        server_tcp.start();

        /* sendchatmsg e altri comandi inerenti alla chat */
        //creo server multicast
        String ip_multicast = "226.226.226.226";
        ServerMulticast serverconnection_udp = new ServerMulticast(ip_multicast);
        Thread server_udp = new Thread(serverconnection_udp);
        server_udp.start();

        System.out.println("*server avviato *");
    }


    /* METODI UTILITY MAIN*/

    /*
     * stampa lista utenti registrati
     */
    public static void print_listuseregister(ConcurrentLinkedQueue<Utente> listuseregister) {
        System.out.println("** stampa lista utenti registrati (se esistenti) **");

        if (listuseregister.size()!=0) {
            for (Utente user : listuseregister) {
                user.print();
            }
            System.out.println();
        }
    }


    /*
     * metodo che inizializza la lista degli utenti registrati (ricostruisce le informazioni)
     * NOTA: se riavvio il server la lista non rimane salvata, devo quindi inserire di nuovo
     * gli utenti già registrati tramite questo metodo
     */
    public static void init_listregisteruser(ConcurrentLinkedQueue<Utente> listuseregister) {
        //init listuseregister
        File file = new File("Utenti/");
        if (file.isDirectory()) {
            for (File fileEntry : file.listFiles()) { //scorro i file nella directory "file"
                ObjectMapper objectMapper = new ObjectMapper();
                //JSON to JAVA
                Utente utente;
                String nick = new String(fileEntry.toString());
                nick = nick.substring(7); //ottengo nickname
                File filejson = new File("Utenti/"+nick+"/login.json");
                try {
                    utente = objectMapper.readValue(filejson, Utente.class);
                    //aggiungo l'oggetto utente alla lista
                    Utente tmp = new Utente(utente.getUsername(),utente.getPassword());
                    listuseregister.add(tmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /*
     * metodo che inizializza la lista dei progetti (ricostruisce le informazioni)
     * NOTA: se riavvio il server la lista non rimane salvata, devo quindi inserire di nuovo
     * i progetti già creati tramite questo metodo
     */
    public static void init_listproject(ConcurrentHashMap<String, Progetto> progetti) {
        File[] files = new File("Progetti/").listFiles();

        for (File f : files) { //scorro i file/cartelle dentro la cartella "Progetti"
            if (f.isDirectory()) {
                //scorro file di testo riga per riga
                FileReader project_read = null;
                try {
                    project_read = new FileReader("Progetti/"+f.getName()+"/listamembri.txt");
                    BufferedReader b = new BufferedReader(project_read);
                    String member_creator = b.readLine();
                    String tmp = member_creator;
                    //creo progetto
                    Progetto project = new Progetto(f.getName(), tmp);

                    /* aggiungo lista membri al progetto */
                    if (tmp!=null) {
                        tmp = b.readLine();
                        while (tmp!=null) {
                            if (tmp.compareTo("\n")!=0)
                                project.add_member(tmp, member_creator);
                            tmp = b.readLine();
                        }
                    }
                    progetti.put(f.getName(), project);
                    /* aggiungo carte al progetto */
                    File[] cards = new File("Progetti/"+f.getName()+"/Card").listFiles();
                    for (File carta : cards) { //scorro le carte
                        ObjectMapper objectMapper = new ObjectMapper();
                        Card card = objectMapper.readValue(carta, Card.class);
                        project.add_card(card.getName(), card.getDescription(), card.getState(), card.getStateList());

                    }

                    b.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }



}