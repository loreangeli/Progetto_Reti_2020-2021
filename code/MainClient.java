import java.io.File;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * MAIN CLIENT
 */

public class MainClient {

    public static void main(String[] args) {

        /* VARIABILI DEBUG */
        boolean DEBUGS = true; //DEBUG scambio msg con il server
        boolean DEBUGCB = true; //DEBUG callback

        /* INIZIALIZZO SNIFFER PER CHAT di PROGETTO */
        String ip_multicast = "226.226.226.226"; //indirizzo ip del gruppo multicast a cui collegarsi
        int port = 7000; //porta dove creare ma MulticastSocket
        ConcurrentHashMap<String, Messaggi> msglist = new ConcurrentHashMap<String, Messaggi>(); //struttura dati per salvare i messaggi che arrivano alle chat di progetto
        //avvio thread che intercetta i pacchetti nel gruppo Multicast "ip_multicast"
        ThreadSniffing threadsniffing = new ThreadSniffing(msglist, ip_multicast, port);
        Thread t1 = new Thread(threadsniffing);
        t1.start();

        /* INIZIALIZZO STRUTTURE DATI */
        ConcurrentLinkedQueue<Utente> listuseregister = new ConcurrentLinkedQueue<Utente>(); //lista degli utenti registrati al servizio
        init_listregisteruser(listuseregister); //inizializzo lista utenti registrati
        ConcurrentLinkedQueue<String> listuseronline = new ConcurrentLinkedQueue<String>(); //lista degli utenti online
        //oggetto per gestire connessione UDP con il server
        ClientUDP connection_with_server_UDP = null;
        //oggetto per gestire connessione TCP con il server
        ClientTCP connection_with_server = null;
        //Credenziali Utente (si aggiornano dopo il login)
        String nickname = null;
        String passw = null;
        //se impostata a true il client ha aperto una connessione tcp con il server
        boolean connection_server = false;
        boolean runclient = true;

        /* INIZIALIZZO RMICALLBACK for notify state (online/offline) -> si occupa di tenere aggiornati gli stati (online/offline) degli utenti registrati
         * mantenendo aggiornata la struttura dati "listuseronline" */
        int port_rmi_notify = 5000;
        Registry registry_notify = null;
        INotify server_notify = null;
        NotifyEventInterface stub = null;

        /* INIZIALIZZO LETTURA DA TASTIERA*/
        @SuppressWarnings("resource")
        Scanner in = new Scanner(System.in);

        System.out.println("INFO: ciao, se sei nuovo digita il comando help");


        /* CICLO DEL CLIENT */
        while (runclient) {
            System.out.println("** pronto a leggere da tastiera **");
            String read_console = in.nextLine(); //leggo da tastiera
            String ar [] = read_console.split(" ");
            String comando = ar[0];

            /* HELP - Lista dei comandi di WORTH */
            if (comando.contentEquals("help")) {
                if (ar.length==1) { //comando corretto
                    System.out.println(" ** LISTA COMANDI ** ");
                    System.out.println("register nickname password");
                    System.out.println("login nickname password");
                    System.out.println("listusers");
                    System.out.println("listonlineusers");
                    System.out.println("create_project nameproject");
                    System.out.println("add_member nameproject member");
                    System.out.println("listprojects");
                    System.out.println("showmembers nameproject");
                    System.out.println("add_card nameproject cardname descriptioncard");
                    System.out.println("showcards nameproject");
                    System.out.println("showcard nameproject cardname");
                    System.out.println("movecard nameproject cardname listapartenza listadestinazione");
                    System.out.println("getcardhistory nameproject cardname");
                    System.out.println("readchat nameproject");
                    System.out.println("sendchatmsg nameproject"); //il messaggio va tra virgolette
                    System.out.println("cancel_project nameproject");
                    System.out.println("logout");
                }
                else {
                    System.out.println("errore, prova a scrivere help");
                }
            }

            /* REGISTRAZIONE */
            else if (comando.contentEquals("register")) {
                if (ar.length==3) {
                    String username = ar[1];
                    String password = ar[2];
                    //rmi
                    int port_rmi = 3000;
                    IRegister serverObject = null;
                    Remote RemoteObject = null;

                    try {
                        Registry r = LocateRegistry.getRegistry(port_rmi);
                        RemoteObject = r.lookup("Server-Register");
                        serverObject = (IRegister) RemoteObject;
                        int risposta = serverObject.register(username, password);
                        if (risposta==0)
                            System.out.println("ok");
                        else if (risposta==-1)
                            System.out.println("errore, username "+username+" già occupato");
                        else System.out.println("errore di altro genere");
                    }
                    catch (AccessException e1) {
                        e1.printStackTrace();
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    } catch (NotBoundException e1) {
                        e1.printStackTrace();
                    }
                }
                else {
                    if (ar.length==1)
                        System.out.println("errore, inserisci nickname e password");
                    else System.out.println("errore, inserisci password");
                }
            }

            /* LOGIN */
            else if (comando.contentEquals("login")) { //if-login
                if (nickname==null && passw==null) { //controllo che il login non sia già stato effettuato
                    if (ar.length==3) {
                        /* crea comunicazione TCP con il server */
                        int port_tcp_server=4000;
                        /* crea comunicazione udp con il server */
                        //NOTA: con questo if evito di aprire nuove connessioni per lo stesso client
                        if (connection_server == false) {
                            connection_with_server = new ClientTCP(port_tcp_server, listuseronline, listuseregister);
                            connection_with_server_UDP = new ClientUDP("226.226.226.226", msglist);
                            connection_server = true;
                        }
                        String msgbyserver = connection_with_server.MsgtoLogin(read_console+"\n");
                        System.out.println(msgbyserver); //msg da stampare in console
                        if (msgbyserver.contains("logged in")) { //login corretto
                            //aggiorno credenziali
                            nickname = new String(ar[1]);
                            passw = new String(ar[2]);
                            threadsniffing.setNickname(nickname);
                            /*comunicazione con il server per l'aggiornamento degli utenti online */
                            ArrayList<String> listuseronlinetmp = new ArrayList<String>(connection_with_server.get_liststate()); //lista utenti online restituita dal server (lista di appoggio)
                            System.out.println("ahaa");
                            //aggiorno lista originale "listuseronline" con il contenuto di "listuseronlinetmp"
                            listuseronline.clear();

                            //copio tutta l'array listuseronlinetmp in listuseronline
                            for (String key : listuseronlinetmp) {
                                listuseronline.add(key); //aggiunge elemento alla lista
                            }

                            /*comunicazione con il server per l'aggiornamento degli utenti registrati */
                            listuseregister = new ConcurrentLinkedQueue<Utente>(connection_with_server.get_listregister()); //lista utenti registrati
                            if (DEBUGS) print_useregister(listuseregister);

                            /* inizializzo e registro RMICALLBACK for notify state (online/offline) */
                            try {
                                registry_notify = LocateRegistry.getRegistry(port_rmi_notify);
                                String name = "server-callback";
                                server_notify = (INotify) registry_notify.lookup(name);
                                NotifyEventInterface callbackObj = new NotifyEventImpl(listuseronline);
                                stub = (NotifyEventInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
                            } catch (RemoteException e2) {
                                e2.printStackTrace();
                            } catch (NotBoundException e) {
                                e.printStackTrace();
                            }
                            // registro RMI callback sul client
                            try {
                                server_notify.registerForCallback(stub, nickname);
                            } catch (RemoteException e) {

                                e.printStackTrace();
                            }
                            if (DEBUGCB) System.out.println("[CallBack] registro il client "+nickname+" al sistema di callback");
                        }
                    }
                    else {
                        if (ar.length==1)
                            System.out.println("errore, inserisci nickname e password");
                        else System.out.println("errore, inserisci password");
                    }
                }
                else {
                    System.out.println("c'è un utente già collegato, deve essere prima scollegato");
                }
            } //chiudo if-login

            /* LOGOUT */
            else if (comando.contentEquals("logout")) { //if-logout
                if (nickname==null || passw==null) {
                    System.out.println("errore, login non eseguito, non posso fare logout");
                }
                else if (read_console.compareTo("logout")==0) { //logout corretto
                    //invio al server la stringa "logout nickname password"
                    //per controllare che l'utente esiste e sia online
                    String msgbyserver = connection_with_server.MsgtoLogout("logout "+nickname+" "+passw+"\n");
                    System.out.println(msgbyserver);
                    if (msgbyserver.compareTo(nickname+" scollegato")==0) {
                        /* RMI callback */
                        try {
                            server_notify.unregisterForCallback(stub, nickname);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        if (DEBUGCB) System.out.println("[CallBack] disiscrivo il client "+nickname+" dal sistema di callback");
                        nickname=null;
                        passw=null;
                        threadsniffing.setNickname(null);
                    }
                }
                else System.out.println("errore, prova a scrivere: logout");
            } //chiudo if-logout

            /* LISTUSERS */
            else if (comando.contentEquals("listusers")) {
                if (ar.length==1) { //comando corretto
                    if (nickname==null || passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else { //comando corretto
                        //aggiorno lista utenti registrati
                        listuseregister = new ConcurrentLinkedQueue<Utente>(connection_with_server.MsgListUsersUpdate());
                        /* stampo lista utenti registrati e il loro stato (online/offline)*/
                        for (Utente user : listuseregister) {
                            String utente = user.getUsername();
                            if (listuseronline.contains(user.getUsername())) //utente online
                                utente = utente + "->online";
                            else utente = utente + "->offline";
                            System.out.print(utente+"  ");
                        }
                        System.out.println();
                    }
                }
                else System.out.println("errore, prova a scrivere: listusers");

            } //chiudo if-listusers

            /* LISTONLINEUSERS */
            else if (comando.contentEquals("listonlineusers")) {
                if (ar.length==1) {
                    if (nickname==null && passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else { //comando corretto
                        /* stampo lista degli utenti online */
                        print_useronline(listuseronline);
                    }
                }
                else System.out.println("errore, prova a scrivere listonlineusers");
            } //chiudo if-listonlineusers

            /* CREA PROGETTO */
            else if (read_console.contains("create_project")) {
                if (ar.length == 2) {
                    if (comando.compareTo("create_project")==0) {
                        //controlla di aver fatto login
                        if (nickname==null && passw==null) {
                            System.out.println("errore, login non eseguito, operazione non concessa");
                        }
                        else { //comando corretto
                            /* invio dati al server */
                            String receive = connection_with_server.MsgToCreateProject(read_console+"\n");
                            System.out.println(receive);
                        }
                    }
                    else {
                        System.out.println("errore, prova a scrivere create_project nameproject");
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere create_project nameproject");
                }
            }

            /* ADDMEMBER */
            else if (read_console.contains("add_member")) {
                if (comando.compareTo("add_member")==0) {
                    if (ar.length==3) {
                        if (nickname==null && passw==null) {
                            System.out.println("errore, login non eseguito, operazione non concessa");
                        }
                        else { //comando corretto
                            String receive = connection_with_server.MsgToAddMember(read_console+" "+nickname+'\n');
                            System.out.println(receive);
                        }
                    }
                    else {
                        if (nickname==null && passw==null) {
                            System.out.println("errore, login non eseguito, operazione non concessa");
                        }
                        else {
                            System.out.println("errore, prova con add_member nameproject member");
                        }
                    }
                }

            } //chiudo if-add_member

            /* LISTPROJECTS (lista dei progetti di cui è membro) */
            else if (comando.contentEquals("listprojects")) {
                if (ar.length==1) {
                    if (nickname==null && passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else { //comando corretto
                        connection_with_server.MsgListProject(read_console+" "+nickname+'\n');
                    }

                }
                else {
                    System.out.println("errore, prova a scrivere listproject");
                }

            } //chiudo if-list project

            /* SHOWMEMBER (operazione per recuperare la lista dei membri di quel progetto) */
            else if (comando.contentEquals("showmembers")) {
                if (ar.length==2) {
                    if (nickname==null && passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else { //comando corretto
                        ArrayList<String> tmp =connection_with_server.MsgShowMember(read_console+" "+nickname+'\n');
                        System.out.println(tmp);
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere showmembers nameproject");
                }
            } //chiudo if-showmember

            /* ADDCARD */
            else if (comando.contentEquals("add_card")) {
                if (ar.length==4) {
                    if (nickname==null && passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else { //comando corretto
                        String read = connection_with_server.MsgAddCard(read_console+" "+nickname+'\n');
                        System.out.println(read);
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere add_card nameproject cardname descriptioncard");
                }
            } //chiudo if-addcard

            /* SHOWCARDS*/
            else if (comando.contentEquals("showcards")) {
                if (ar.length==2) {
                    if (nickname==null & passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else { //comando corretto
                        connection_with_server.MsgShowCards(read_console+" "+nickname);
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere showcards nameproject");
                }
            } //chiudo if-showcards

            /* SHOWCARD */
            else if (comando.contentEquals("showcard")) {
                if (ar.length==3) {
                    if (nickname==null & passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else {
                        Card carta = connection_with_server.MsgShowCard(read_console+" "+nickname);
                        if (carta!=null) {
                            System.out.println("name:"+carta.getName()+", description:"+carta.getDescription()+", stato:"+carta.getState());
                        }
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere showcard nameproject cardname");
                }
            } //chiudo if-showcard

            /* MOVECARD*/
            else if (comando.contentEquals("movecard")) {
                if (ar.length==5) {
                    if (nickname==null & passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else {
                        String risp = connection_with_server.MsgMoveCard(read_console+" "+nickname);
                        System.out.println(risp);
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere movecard nameproject cardname listapartenza listadestinazione");
                }
            }

            /* GETCARDHISTORY*/
            else if (comando.contentEquals("getcardhistory")) {
                if (ar.length==3) {
                    if (nickname==null & passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else {
                        ArrayList<Integer> tmp = connection_with_server.MsgCardHistory(read_console+" "+nickname);
                        if (tmp!= null) System.out.println(tmp);
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere getcardhistory nameproject cardname");
                }
            }

            /* SENDCHATMSG */
            else if (comando.contentEquals("sendchatmsg")) {
                if (ar.length==2) {
                    if (nickname==null & passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else {
                        //controllo di far parte del progetto
                        int check = connection_with_server.MsgCheckUserinProject(read_console+" "+nickname); //MSG: sendchatmsg nameproject nickname

                        if (check==1) {
                            System.out.println("digita messaggio: ");
                            String messaggio = in.nextLine(); //msg leggo da tastiera
                            connection_with_server_UDP.MsgSendChat(ar[1],nickname, messaggio); //arg1: projectname, arg2: msg
                        }
                        else {
                            System.out.println("l'utente non fa parte del progetto");
                        }

                    }
                }
                else {
                    System.out.println("errore, prova a scrivere sendchatmsg nameproject");
                }

            }

            /* READCHAT */
            else if (comando.contentEquals("readchat")) {
                if (ar.length==2) {
                    if (nickname==null && passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }

                    else {
                        //controllo d far parte del progetto
                        int check = connection_with_server.MsgCheckUserinProject(read_console+" "+nickname); //MSG: sendchatmsg nameproject nickname

                        if (check==1) {
                            connection_with_server_UDP.MsgReadChat(ar[1]); //arg1: nameproject
                        }
                        else {
                            System.out.println("l'utente non fa parte del progetto");
                        }

                    }
                }
                else {
                    System.out.println("errore, prova a scrivere readchat nameproject");
                }
            }

            /* CANCELPROJECT */
            else if (comando.contentEquals("cancel_project")) {
                if (ar.length == 2) {
                    if (nickname==null && passw==null) {
                        System.out.println("errore, login non eseguito, operazione non concessa");
                    }
                    else {
                        connection_with_server.MsgDeleteProject(read_console+" "+nickname);
                    }
                }
                else {
                    System.out.println("errore, prova a scrivere cancel_project nameproject");
                }
            }

            /* COMANDO NON RICONOSCIUTO */
            else System.out.println("comando non riconosciuto ps. forse hai inserito uno spazio di troppo");

        } //close ciclo

    }


    /* METODI UTILITY MAIN */

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
     * metodo che stampa la lista degli utenti online restituita dal server
     */
    public static void print_useronline(ConcurrentLinkedQueue<String> listuseronline) {
        System.out.print("lista utenti online: ");
        System.out.print(listuseronline);
        System.out.println();
    }

    /*
     * metodo che stampa la lista degli utenti registrati restituita dal server
     */
    public static void print_useregister(ConcurrentLinkedQueue<Utente> list) {
        System.out.println("stampa lista utenti registrati: ");

        for (Utente user : list) {
            user.print();
        }
        System.out.println();
    }


}