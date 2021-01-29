import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Questa classe crea un Task che gestisce una connessione tcp con un Client
 */

public class TaskConnectionTCP implements Runnable {
    private Socket connectionSocket;
    private OnlineUsers onlineusers; //lista utenti online
    private ConcurrentLinkedQueue<Utente> listuseregister; //lista utenti registrati
    private RMIServerNotify server_notify; //oggetto per notificare al client un aggiornamento della lista utenti online
    private ConcurrentHashMap<String, Progetto> progetti; //mappa che contiene tutti i progetti
    private int id_server = -1; //identificativo del server

    public TaskConnectionTCP(Socket socket, OnlineUsers onlineusers, ConcurrentLinkedQueue<Utente> listuseregister2, ConcurrentHashMap<String, Progetto> progetti, RMIServerNotify server_notify, int i) {
        connectionSocket = socket;
        this.onlineusers = onlineusers;
        this.listuseregister = listuseregister2;
        this.server_notify = server_notify;
        this.progetti = progetti;
        id_server = i;
    }

    @Override
    public void run() {

        /* INIT VARIABILI */
        //variabili di DEBUG
        boolean DEBUG = true;
        boolean DEBUGCB = false; //DEBUG RMI CALLBACK
        //altre variabili
        boolean run = true; //variabile usata per terminare il thread
        String to_send = null; //contiene il messaggio da inviare al client
        String nickname = null;
        String password = null;


        if (DEBUG) System.out.println("Client ["+id_server+"] "+connectionSocket.getInetAddress()+":"+connectionSocket.getLocalPort()+" is connected");

        BufferedReader inFromClient = null;
        DataOutputStream outToClient = null;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream (connectionSocket.getOutputStream());
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try {

            while (run) {
                /* leggi msg inviato dal client */
                if (DEBUG) System.out.println("** Server ["+id_server+"] in attesa di elaborare richieste **");
                String read_line= null;
                read_line=inFromClient.readLine();

                System.out.println("["+id_server+"] "+"#richiesta inviata dal client da elaborare: "+read_line);

                if (read_line!=null) {
                    String ar[] = read_line.split(" ");
                    String comando = ar[0]; //comando da eseguire
                    String projectname = null;


                    /* LOGIN [login nickname password] */
                    if (comando.compareTo("login")==0) {
                        //gestione errori
                        if (ar.length!=3) {
                            if (ar.length==2) to_send = "errore, inserisci password";
                            else if (ar.length==1) to_send = "errore, inserisci nickname e password";
                            else to_send = "errore di altro genere";
                        }
                        else {
                            //se sono qui vuol dire che il client ha inviato un messaggio di login corretto
                            //controllo che l'account esista
                            nickname = new String(ar[1]);
                            password = new String(ar[2]);
                            if (check_login(nickname, password)==0) { //credenziali corrette
                                //controllo che l'utente non sia già loggato
                                if (onlineusers.contains(nickname)) {
                                    to_send="errore, utente già loggato da un altro dispositivo";
                                }
                                else {
                                    to_send=nickname+" logged in";
                                    onlineusers.add_user(nickname); //user online
                                    server_notify.update(onlineusers); //notifico i client di un nuovo utente online (rmi callback)
                                    if (DEBUGCB) System.out.println("["+id_server+"] "+"[CallBack] invio notifica al client (login)");
                                }
                            }
                            else if (check_login(nickname, password)==2)
                                to_send="errore, l'utente non esiste";
                            else if (check_login(nickname, password)==1)
                                to_send="errore, password errata";
                            else to_send="errore di altro genere";
                        }

                        /* invio msg di risposta al client */
                        outToClient.writeBytes(to_send+"\n");
                        System.out.println("["+id_server+"] "+"risposta del server: "+to_send);

                        /* INVIO lista utenti online al client*/
                        if (to_send.compareTo(nickname+" logged in")==0) {
                            if (DEBUG) System.out.println("["+id_server+"] "+"[INIZIO] invio lista utenti online al client");
                            int nuser = onlineusers.get_length();
                            outToClient.write(nuser);
                            if (DEBUG) System.out.println("["+id_server+"] "+"send msg to client: "+nuser);

                            ConcurrentHashMap<String,Integer> list =  onlineusers.get_list(); //copia della lista degli utenti online
                            //inivio la lista degli utenti online al Client
                            for (String key : list.keySet()) {
                                outToClient.writeBytes(key+"\n");
                                if (DEBUG) System.out.println("["+id_server+"] "+"send msg to client: "+key);
                            }
                            if (DEBUG) System.out.println("["+id_server+"] "+"[FINE] invio lista utenti online al client");
                        }

                        /* INVIO lista utenti registrati al client*/
                        if (to_send.compareTo(nickname+" logged in")==0) {
                            if (DEBUG) System.out.println("["+id_server+"] "+"[INIZIO] invio lista utenti registrati al client");
                            int nuser = listuseregister.size();
                            outToClient.write(nuser);
                            if (DEBUG) System.out.println("["+id_server+"] "+"send msg to client: "+nuser);

                            //invio la lista degli utenti registrati al Client
                            for( Utente utente : listuseregister ){
                                outToClient.writeBytes(utente.getUsername()+"\n");
                                if (DEBUG) System.out.println("["+id_server+"] "+"send msg to client: "+utente.getUsername());
                                //int check = inFromClient.read();
                                //if (DEBUG) System.out.println("["+id_server+"] "+"receive msg by client: "+check);
                                //if (check==225) {
                                    outToClient.writeBytes(utente.getPassword()+"\n");
                                    if (DEBUG) System.out.println("["+id_server+"] "+"send msg to client: "+utente.getPassword());
                                //}
                                else System.out.println("["+id_server+"] "+"errore check");
                            }
                            if (DEBUG) System.out.println("["+id_server+"] "+"[FINE] invio lista utenti registrati al client");
                        }

                    }

                    /* LOGOUT [logout nickname password] */
                    else if (comando.compareTo("logout")==0) {
                        if (check_login(nickname, password)==0) { //credenziali corrette
                            to_send=nickname+" scollegato";
                            //tolgo l'utente dalla lista degli utenti online
                            onlineusers.remove_user(nickname);
                            server_notify.update(onlineusers); //notifico i client che un utente si è disconnesso (rmi callback)
                            if (DEBUGCB) System.out.println("["+id_server+"] "+"[CallBack] invio notifica al client (logout)");
                            if (DEBUG) System.out.println("["+id_server+"] "+"scollegato");
                        }
                        else if (check_login(nickname, password)==2)
                            to_send="errore, l'utente non esiste";
                        else if (check_login(nickname, password)==1)
                            to_send="errore, password errata";
                        else to_send="errore di altro genere";

                        /* invio il msg di risposta al client */
                        outToClient.writeBytes(to_send+"\n");
                        System.out.println("["+id_server+"] "+"risposta del server: "+to_send);

                    }

                    /* CREATEPROJECT [create_project projectname] */
                    else if (comando.compareTo("create_project")==0) {
                        projectname = ar[1];

                        /* creo cartella del nuovo progetto */
                        to_send = "errore di altro genere";
                        String pathFolderProgetto = "Progetti/"+projectname;
                        File folder2 = new File(pathFolderProgetto);
                        // Verifichiamo che non sia già esistente come cartella
                        if (!folder2.exists()) { // cartella non esistente
                            folder2.mkdir();
                            /* gestione creazione progetto nella struttura dati */
                            Progetto project = new Progetto(projectname, nickname);
                            progetti.put(projectname, project);
                            to_send="progetto "+projectname+" creato con successo";
                            outToClient.writeBytes(to_send+'\n'); //progetto creato con successo
                            if (DEBUG) System.out.println("send msg to client: "+to_send);
                        }
                        else { //cartella già esistente
                            to_send="progetto "+projectname+" già esistente, impossibile crearlo, cambia nome";
                            outToClient.writeBytes(to_send+'\n');
                            if (DEBUG) System.out.println("send msg to client: "+to_send);
                        }

                    }

                    /* ADDMEMBER [add_member projectname membertoadd memberdiprogetto] */
                    else if (comando.compareTo("add_member")==0) {
                        boolean check = true;
                        to_send = "errore, impossibile aggiungere questo nuovo utente al progetto se non esiste";
                        if (ar[1]!=null && ar[2]!=null && ar[3]!=null) {
                            //controllo che il progetto esista
                            Progetto project = progetti.get(ar[1]);
                            if (project!=null) {
                                //controllo che il membro che aggiungo sia registrato
                                for (Utente user : listuseregister) {
                                    if (check && user.getUsername().compareTo(ar[2])==0) {//comando corretto
                                        to_send = project.add_member(ar[2], ar[3]);
                                        check=false;
                                    }
                                }
                            }
                            else to_send = "errore, progetto non esistente";
                            /* invio dati al client */
                            outToClient.writeBytes(to_send+'\n');
                            if (DEBUG) System.out.println("send msg to client: "+to_send);
                        }
                        else {
                            to_send = "parametro null";
                            outToClient.writeBytes(to_send+'\n');
                            if (DEBUG) System.out.println("send msg to client: "+to_send);
                        }

                    }

                    /* LISTPROJECTS [listprojects memberdiprogetto] */
                    else if (comando.compareTo("listprojects")==0) {
                        ArrayList<String> tmp = new ArrayList<String>(); //lista che contiene i progetti di cui fa parte il membro

                        //controllare che esista l'utente che vuole la lista
                        for (String key : progetti.keySet()) {
                            Progetto prog = progetti.get(key);
                            if (prog.search_member(ar[1])) //membro appartiene al progetto
                                tmp.add(prog.get_nameproject());
                        }

                        /* invia il numero di progetti da inviare al client */
                        outToClient.write(tmp.size());
                        if (DEBUG) System.out.println("msg to client: "+tmp.size());

                        for (String prog : tmp) {
                            outToClient.writeBytes(prog+'\n');
                            inFromClient.readLine();
                        }


                    }

                    /* SHOWMEMBERS [showmembers projectname memberdiprogetto] */
                    else if (comando.compareTo("showmembers")==0) {
                        ConcurrentLinkedQueue<String> tmp = new ConcurrentLinkedQueue<String>(); //lista che contiene i membri del progetto
                        String read = null;
                        int var = 0;

                        if (progetti.containsKey(ar[1])) { //controllo che il progetto esista
                            Progetto prog = progetti.get(ar[1]); //recupero oggetto progetto
                            if (prog.search_member(ar[2])) { //controllo che  l'utente che vuole la lista faccia parte del progetto
                                tmp = prog.get_listamembri();
                                /* invia il numero di progetti da inviare al client */
                                outToClient.write(tmp.size());
                                if (DEBUG) System.out.println("numero dei membri: "+tmp.size());
                                var = inFromClient.read();
                                if (DEBUG) System.out.println("receive msg by client: "+var);

                                for (String member : tmp) {
                                    outToClient.writeBytes(member+'\n');
                                    if (DEBUG) System.out.println("send msg to client: "+member);
                                    read = inFromClient.readLine();
                                    if (DEBUG) System.out.println("receive msg by client: "+read);
                                }


                            }
                            else { //l'utente che ha richiesto la lista non fa parte del progetto
                                /* invia il numero di progetti da inviare al client */
                                outToClient.write(tmp.size());
                                if (DEBUG) System.out.println("send msg to client: "+tmp.size());
                                var = inFromClient.read();
                                if (DEBUG) System.out.println("receive msg by client: "+var);
                                to_send = "errore, l'utente che ha richiesto la lista non fa parte del progetto";
                                outToClient.writeBytes(to_send+'\n');
                                if (DEBUG) System.out.println("send msg to client: "+to_send);
                            }
                        }
                        else {
                            outToClient.write(tmp.size());
                            if (DEBUG) System.out.println("send msg to client: "+tmp.size());
                            var = inFromClient.read();
                            if (DEBUG) System.out.println("receive msg by client: "+var);
                            to_send = "errore, progetto inesistente";
                            outToClient.writeBytes(to_send+'\n');
                            if (DEBUG) System.out.println("send msg to client: "+to_send);
                        }

                    }

                    /* ADDCARD [add_card projectname cardname descriptioncard memberdiprogetto] */
                    else if (comando.compareTo("add_card")==0) {

                        if (progetti.containsKey(ar[1])) { //controllo che il progetto esista
                            Progetto prog = progetti.get(ar[1]); //recupero oggetto progetto
                            if (prog.add_card(ar[2], ar[3])==true) { //ok
                                progetti.remove(prog.get_nameproject());
                                progetti.put(ar[1], prog);
                                to_send = "carta inserita con successo";
                            }
                            else to_send = "errore, carta già presente, impossibile inserire";
                        }
                        else to_send = "errore, progetto inesistente";

                        outToClient.writeBytes(to_send+'\n');
                        if (DEBUG) System.out.println("send msg to client: "+to_send);
                    }

                    /* SHOWCARDS [showcards projectname memberdiprogetto] */
                    else if (comando.compareTo("showcards")==0) {
                        int check = 4;
                        if (progetti.containsKey(ar[1])) { //controllo che il progetto esista
                            Progetto prog = progetti.get(ar[1]);
                            ArrayList<Card> listatmp = new ArrayList<Card>(); //lista di appoggio
                            if (prog.search_member(ar[2])) { //controlla che il membro faccia parte della lista
                                //aggiungo carte alla lista di appoggio
                                for (Card carta : prog.showcards())
                                    listatmp.add(carta);

                                check = 0;
                                outToClient.write(check); //ok
                                inFromClient.read(); //ricevo 255
                                outToClient.write(listatmp.size()); //scrivo dimensione array
                                if (listatmp.size()!=0) {
                                    inFromClient.read();
                                    for (Card carta : listatmp) {
                                        //scrivo nome
                                        outToClient.writeBytes(carta.getName()+'\n');
                                        inFromClient.read();

                                        //scrivo descrizione
                                        outToClient.writeBytes(carta.getDescription()+'\n');
                                        inFromClient.read();

                                        //scrivo stato
                                        outToClient.write(carta.getState());
                                        inFromClient.read();

                                        //aggiorno historylist
                                        ArrayList<Integer> historylistcopy = new ArrayList<Integer>(carta.getStateList()); //copia della historylist della carta
                                        outToClient.write(historylistcopy.size()); //invio dim historylist
                                        inFromClient.read(); //255
                                        for (int j=0;j<historylistcopy.size();j++) {
                                            outToClient.write(historylistcopy.get(j));
                                            inFromClient.read();
                                        }


                                    }
                                }

                            }
                            else {
                                check = 1; //errore, impossibile eseguire l'operazione, non fai parte del progetto
                                outToClient.write(check);
                            }
                        }
                        else {
                            check = 2; //errore, progetto inesistente
                            outToClient.write(check);
                        }

                    }

                    /* SHOWCARD [showcard projectname cardname memberdiprogetto] */
                    else if (comando.compareTo("showcard")==0) {
                        int check = 0;
                        int leggi = 0;

                        if (progetti.containsKey(ar[1])) { //controllo che il progetto esista
                            Progetto prog = progetti.get(ar[1]);
                            if (prog.search_member(ar[3])) { //controlla che il membro faccia parte della lista
                                //controlla che la carta esista
                                Card carta = prog.search_card(ar[2]);

                                if (carta!=null) { //carta esistente
                                    check = 0;
                                    outToClient.write(check);
                                    if (DEBUG) System.out.println("send msg to client: "+check);
                                    leggi = inFromClient.read();
                                    if (DEBUG) System.out.println("receive msg from client: "+leggi);

                                    //scrivo nome
                                    outToClient.writeBytes(carta.getName()+'\n');
                                    if (DEBUG) System.out.println("send msg to client: "+carta.getName());
                                    leggi = inFromClient.read();
                                    if (DEBUG) System.out.println("receive msg from client: "+leggi);

                                    //scrivo descrizione
                                    outToClient.writeBytes(carta.getDescription()+'\n');
                                    if (DEBUG) System.out.println("send msg to client: "+carta.getDescription());
                                    leggi = inFromClient.read();
                                    if (DEBUG) System.out.println("receive msg from client: "+leggi);

                                    //scrivo stato
                                    outToClient.write(carta.getState());
                                    if (DEBUG) System.out.println("send msg to client: "+carta.getState());
                                    inFromClient.read();
                                    if (DEBUG) System.out.println("receive msg from client: "+leggi);
                                }
                                else { //carta inesistente
                                    check = 3; //errore, carta non presente in questo progetto
                                    outToClient.write(check);
                                    if (DEBUG) System.out.println("send msg to client: "+check);
                                }
                            }
                            else {
                                check = 1; //errore, impossibile eseguire l'operazione, non fai parte del progetto
                                outToClient.write(check);
                                if (DEBUG) System.out.println("send msg to client: "+check);
                            }
                        }
                        else {
                            check = 2; //errore, progetto inesistente
                            if (DEBUG) outToClient.write(check);
                        }

                    }

                    /* MOVECARD*/
                    else if (comando.compareTo("movecard")==0) {
                        int check = 0;

                        //init stato 1
                        int stato1=-1;
                        if (ar[3].contentEquals("todo")) stato1=0;
                        else if (ar[3].contentEquals("inprogress")) stato1=1;
                        else if (ar[3].contentEquals("toberevised")) stato1=2;
                        else if (ar[3].contentEquals("done")) stato1=3;

                        //init stato2
                        int stato2=-1;
                        if (ar[4].contentEquals("todo")) stato2=0;
                        else if (ar[4].contentEquals("inprogress")) stato2=1;
                        else if (ar[4].contentEquals("toberevised")) stato2=2;
                        else if (ar[4].contentEquals("done")) stato2=3;

                        System.out.println(stato1+" "+ stato2);
                        if (progetti.containsKey(ar[1])) { //controllo che il progetto esista
                            Progetto prog = progetti.get(ar[1]);
                            if (prog.search_member(ar[5])) { //controlla che il membro faccia parte della lista
                                //controlla che la carta esista
                                Card carta = prog.search_card(ar[2]);

                                if (carta!=null) { //carta esistente
                                    if (prog.move_card(ar[2], stato1, stato2)) {
                                        check=0;
                                        outToClient.write(check);
                                        System.out.println("send msg to client: "+check);
                                        //aggiorno struttura dati
                                        progetti.remove(ar[1]);
                                        progetti.put(ar[1], prog);
                                    }
                                    else {
                                        check = 5; //errore di altro genere
                                        outToClient.write(check);
                                        System.out.println("send msg to client: "+check);
                                    }

                                }
                                else {
                                    check = 3; //errore, carta non presente in questo progetto
                                    outToClient.write(check);
                                    System.out.println("send msg to client: "+check);
                                }
                            }
                            else {
                                check = 1; //errore, impossibile eseguire l'operazione, non fai parte del progetto
                                outToClient.write(check);
                                System.out.println("send msg to client: "+check);
                            }
                        }
                        else {
                            check = 2; //errore, progetto inesistente
                            outToClient.write(check);
                            System.out.println("send msg to client: "+check);
                        }

                    }

                    /* GET CARD HISTORY*/
                    else if (comando.compareTo("getcardhistory")==0) {
                        int check = 4;
                        Card carta = null;

                        if (progetti.containsKey(ar[1])) { //controllo che il progetto esista
                            Progetto prog = progetti.get(ar[1]);
                            if (prog.search_member(ar[3])) { //controlla che il membro faccia parte della lista
                                if ( (carta=prog.search_card(ar[2])) != null) { //controllo che la carta esista
                                    check = 0; //ok
                                    outToClient.write(check);
                                    inFromClient.read();
                                    //invio dimensione history list
                                    ArrayList<Integer> historylist = new ArrayList<Integer> (carta.getStateList());
                                    outToClient.write(historylist.size());
                                    inFromClient.read();
                                    if (historylist.size() != 0) {
                                        for (Integer val : historylist) {
                                            outToClient.write(val); //invio valore history list
                                            inFromClient.read();
                                        }

                                    }

                                }
                                else {
                                    check = 3; //errore, carta inesistente
                                    outToClient.write(check);
                                    System.out.println("send msg to client: "+check);
                                }

                            }
                            else {
                                check = 1; //errore, impossibile eseguire l'operazione, non fai parte del progetto
                                outToClient.write(check);
                                System.out.println("send msg to client: "+check);
                            }
                        }
                        else {
                            check = 2; //errore, progetto inesistente
                            outToClient.write(check);
                            System.out.println("send msg to client: "+check);
                        }
                    }


                    /* CANCEL PROJECT cancel_project progetto nickname */
                    else if (comando.compareTo("cancel_project")==0) {
                        int check = 5;
                        projectname = ar[1];


                        //controllo che il progetto esista
                        if (progetti.containsKey(projectname)) {
                            Progetto prog = progetti.get(projectname); //progetto da eliminare
                            if (prog.search_member(ar[2])) { //controlla che il membro faccia parte della lista del progetto
                                if (prog.canCancelProject()) {//controllo che tutte le card siano nella lista DONE
                                    /* cancello progetto */
                                    //elimino progetto
                                    progetti.remove(projectname);
                                    //elimino file del progetto e le eventuali sottocartelle
                                    File progetto = new File("Progetti/"+projectname);
                                    File membri = new File("Progetti/"+projectname+"/listamembri.txt");
                                    if (membri.delete()) System.out.println("file eliminato: "+"Progetti/"+projectname+"/listamembri.txt");
                                    else System.out.println("file non eliminato: "+"Progetti/"+projectname+"/listamembri.txt");

                                    File carte = new File("Progetti/"+projectname+"/Card");
                                    String listacarte []= carte.list();
                                    for (int i=0;i<listacarte.length;i++) {
                                        File carta = new File("Progetti/"+projectname+"/Card/"+listacarte[i]);
                                        if (carta.delete()) System.out.println("file eliminato: "+"Progetti/"+projectname+"/Card/"+listacarte[i]);
                                        else System.out.println("file non eliminato: "+"Progetti/"+projectname+"/Card/"+listacarte[i]);
                                    }
                                    if (carte.delete()) System.out.println("file eliminato: "+"Progetti/"+projectname+"/Card");
                                    else System.out.println("file non eliminato: "+"Progetti/"+projectname+"/Card");
                                    if (progetto.delete()) System.out.println("file eliminato: "+"Progetti/"+projectname);
                                    else System.out.println("file non eliminato: "+"Progetti/"+projectname);

                                    /* invio msg al client */
                                    check = 0;
                                    outToClient.write(check);
                                    System.out.println("send msg to client: "+check);
                                }
                                else { //non tutte le carte sono DONE (impossibile cancellare il progetto)
                                    check = 3; //non tutte le carte sono DONE
                                    outToClient.write(check);
                                    System.out.println("send msg to client: "+check);
                                }
                            }
                            else {
                                check = 1; //membro non fa parte del progetto
                                outToClient.write(check);
                                System.out.println("send msg to client: "+check);
                            }
                        }
                        else {
                            check = 2; //progetto inesistente
                            outToClient.write(check);
                            System.out.println("send msg to client: "+check);
                        }
                    }

                    else if (comando.compareTo("listusers")==0) {
                        /* clona listuseregister */
                        ConcurrentLinkedQueue<Utente> clone = new ConcurrentLinkedQueue<Utente>(listuseregister); //copia della struttura dati listuseregister
                        outToClient.write(clone.size()); //invio al client il numero degli utenti che sta per ricevere
                        inFromClient.read(); //ricevo ok da client
                        for (Utente user : clone) {
                            outToClient.writeBytes(user.getUsername()+" "+user.getPassword()+'\n'); //invio stringa contenente nickname e password dell'utente
                            inFromClient.read(); // ricevo ok dal client
                        }

                    }

                    /* Metodo di Controllo
                     * controlla se l'utente fa parte del progetto oppure no
                     * gli viene passato un msg1: sendchatmsg progetto1 nickname
                     * oppure un msg2: readchat progetto1 nickname
                     * return 1: l'utente fa parte del progetto
                     * return 0: l'utente non fa parte del progetto
                     * 					 */
                    else if (comando.compareTo("sendchatmsg")==0 || comando.compareTo("readchat")==0) {
                        //controlla se l'utente fa parte del progetto
                        projectname=ar[1];
                        Progetto tmp = progetti.get(projectname);
                        int risposta=0;
                        if (tmp!=null && tmp.search_member(ar[2]))
                            risposta=1;

                        //invio risposta al Client
                        outToClient.write(risposta);
                    }
                }
            }//close while ->ciclo while(true)

        } catch (SocketException e) { //La gestione di questa eccezione è importantissima perchè gestisce una possibile chiusura improvvisa del terminale
            System.out.println("["+id_server+"] "+"ECCEZIONE: probabilmente il client ha chiuso improvvisamente il terminale");
            //se nickname == null l'utente non ha effettuato il login e di conseguenza non è nemmeno nella
            //lista degli utenti online
            if (nickname != null) {
                onlineusers.remove_user(nickname); //user passa offline
                server_notify.fix_unregisterForCallback(nickname); //comando per didiscrizione forzata dalla rmi callback
                try {
                    //notifico i client che un utente si è disconnesso (rmi callback)
                    server_notify.update(onlineusers);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
            //kill del client (chiusura forzata)
            run = false;
            System.out.println("["+id_server+"] "+"Chiusura improvvisa gestita con successo");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server ["+id_server+"] CHIUSO");
    } //close run



    /* METODI VARI */

    /*
     * controlla che esista un account con quelle credenziali passate per argomenti
     * cerca nella directory di "Utenti"
     * return 0 se esiste l'account
     * 1 se il nome utente esiste ma la password è errata
     * 2 se l'account non esiste
     * 3 errore di altro genere
     */
    private int check_login (String nickname, String password) {

        String pathFolderName = "Utenti/"+nickname;
        File folder = new File(pathFolderName);
        // Verifichiamo l'esistenza della cartella con le credenziali
        if (!folder.isDirectory()) {
            return 2;
        }
        //Verifichiamo che le credenziali siano corrette
        String pathFolderlogin = "Utenti/"+nickname+"/login.json";
        File filelogin = new File (pathFolderlogin);
        //deserializzo oggetto JSON (JSON to JAVA)
        ObjectMapper objectMapper = new ObjectMapper();
        Utente user;
        try {
            user = objectMapper.readValue(filelogin, Utente.class);

            if ( (user.getUsername().compareTo(nickname)==0) && (user.getPassword().compareTo(password)==0) )
                return 0;
            if ((user.getUsername().compareTo(nickname)==0) && (user.getPassword().compareTo(password)!=0))
                return 1;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 3;
    }


}