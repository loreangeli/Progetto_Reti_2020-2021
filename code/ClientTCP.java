import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * CLASSE LATO CLIENT che gestisce la connessione tcp con il server
 */

public class ClientTCP {
    private boolean DEBUGS = true;//DEBUG scambio msg con il server
    private int myPort;
    private Socket socket; //socket per comunicare con il server
    private ConcurrentLinkedQueue<String> onlineusers; //lista utenti online
    private ConcurrentLinkedQueue<Utente> useregister; //lista degli utenti registrati
    private BufferedReader inFromServer = null;
    private DataOutputStream outToServer = null;


    public ClientTCP (int port, ConcurrentLinkedQueue<String> listuseronline, ConcurrentLinkedQueue<Utente> listuseregister) {
        this.onlineusers = listuseronline;
        this.useregister = listuseregister;
        myPort = port;

        try {
            socket = new Socket("localhost", myPort);
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToServer = new DataOutputStream (socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * metodo che gestisce il login con il server tcp
     * invia il messaggio di login al server e attende risposta
     * msg è il messaggio da inviare
     * restituisce il msg ricevuto dal server
     */
    public String MsgtoLogin(String msg) {

        try {

            /* invio dati al server */
            if (DEBUGS) System.out.println("send msg to server: "+msg);
            outToServer.writeBytes(msg);

            /* ricevo dati dal server */
            //leggi il messaggio inviato dal server
            String read_line=inFromServer.readLine();
            if (DEBUGS) System.out.println("receive msg by server: "+read_line);

            return read_line; //restituisce il msg inviato dal server

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * metodo per effettuare il logout con il server tcp
     * invia il messaggio di logout e attende risposta dal server
     */
    public String MsgtoLogout(String msg) {

        try {
            /* invio dati al server */
            if (DEBUGS) System.out.println("send msg to server: "+msg);
            outToServer.writeBytes(msg);

            /* ricevo dati dal server */
            //leggi il messaggio inviato dal server
            String read_line=inFromServer.readLine();
            if (DEBUGS) System.out.println("receive msg by server: "+read_line);

            return read_line; //restituisce il msg inviato dal server

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * metodo che restituisce la lista degli utenti registrati
     */
    public ConcurrentLinkedQueue<Utente> get_listuseregister() {
        return useregister;
    }

    /*
     * metodo che restituisce la lista degli utenti online
     * NOTA: viene fatto uno scambio di msg tra il client e il server (TCP)
     */
    public ConcurrentLinkedQueue<String> get_liststate() {
        if (DEBUGS) System.out.println("[INIZIO] ricevo lista utenti online dal server");
        onlineusers = new ConcurrentLinkedQueue<String> (); //reset list

        try {
            /* ricevo dati dal server */
            //leggi il messaggio inviato dal server
            int nuser=inFromServer.read(); //numero di utenti registrati
            if (DEBUGS) System.out.println("receive msg by server: "+nuser);

            for (int i=0;i<nuser;i++) { //ricevo utenti
                String user = inFromServer.readLine();
                if (DEBUGS) System.out.println("receive msg by server: "+user);
                onlineusers.add(user); //copio utente nella lista resettata
            }

            //stampo array di utenti online
            if (DEBUGS) System.out.println("stampa lista utenti online: "+onlineusers);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (DEBUGS) System.out.println("[FINE] ricevo lista utenti online dal server");

        return onlineusers; //restituisce il msg inviato dal server
    }

    /*
     *metodo che restituisce la lista degli utenti registrati
     *NOTA: viene fatto uno scambio di msg tra il client e il server
     */
    public ConcurrentLinkedQueue<Utente> get_listregister() {
        if (DEBUGS) System.out.println("[INIZIO] ricevo lista utenti registrati dal server");
        useregister = new ConcurrentLinkedQueue<Utente>(); //reset list

        try {
            /* ricevo dati dal server */
            //leggi il messaggio inviato dal server
            int nuser=inFromServer.read(); //numero di utenti registrati
            if (DEBUGS) System.out.println("receive msg by server: "+nuser);

            for (int i=0;i<nuser;i++) { //ricevo utenti
                String user = inFromServer.readLine();
                if (DEBUGS) System.out.println("receive msg by server: "+user);
                //outToServer.write(225); //check
                //if (DEBUGS) System.out.println("send msg to server: 225");
                String password = inFromServer.readLine();
                if (DEBUGS) System.out.println("receive msg by server: "+password);
                Utente utente = new Utente(user, password);
                useregister.add(utente); //copio utente nella lista resettata
                if (DEBUGS) System.out.println("receive msg by server: "+user+" "+password);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (DEBUGS) System.out.println("[FINE] ricevo lista utenti registrati dal server");

        return useregister; //restituisce il msg inviato dal server
    }


    /*
     * metodo per comunicare con il server e create un nuovo progetto
     */
    public String MsgToCreateProject(String msg) {
        String read = null;

        /* invio dati al server */
        if (DEBUGS) System.out.println("[CREATE PROJECT] send msg to server: "+msg);
        try {
            outToServer.writeBytes(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        //leggi il messaggio inviato dal server

        try {
            read = inFromServer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (DEBUGS) System.out.println("[CREATE PROJECT] receive msg by server: "+read);

        return read; //restituisce il msg inviato dal server


    }

    public String MsgToAddMember(String msg) {

        String read = null;

        /* invio dati al server */
        try {
            outToServer.writeBytes(msg);
            if (DEBUGS) System.out.println("[ADD MEMBER] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        //leggi il messaggio inviato dal server

        try {
            read = inFromServer.readLine();
            if (DEBUGS) System.out.println("[ADD MEMBER] receive msg by server: "+read);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return read; //restituisce il msg inviato dal server

    }


    public void MsgListProject(String msg) {
        String read = null;
        int size = 0;

        /* invio dati al server */
        try {
            outToServer.writeBytes(msg);
            if (DEBUGS) System.out.println("[LIST_PROJECT] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        try {
            size = inFromServer.read(); //numero di progetti creati
            if (size==0) {
                System.out.println("nessun progetto creato");
            }
            if (DEBUGS) System.out.println("[LIST_PROJECT] receive msg by server: "+read);
            for (int i=0;i<size;i++) {
                read = inFromServer.readLine();
                if (DEBUGS) System.out.println("[LIST_PROJECT] receive msg by server: "+read);
                System.out.println(read);
                outToServer.writeBytes(read+'\n');
                if (DEBUGS) System.out.println("[LIST_PROJECT] send msg to server: "+msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public ArrayList<String> MsgShowMember(String msg) {
        ArrayList<String> tmp = new ArrayList<String>();
        String read = null;
        int size = 0;

        /* invio dati al server */
        if (DEBUGS) System.out.println("[SHOW MEMBERS] send msg to server: "+msg);
        try {
            outToServer.writeBytes(msg);
            if (DEBUGS) System.out.println("[SHOW MEMBERS] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        try {
            size = inFromServer.read();
            if (DEBUGS) System.out.println("[SHOW MEMBERS] numero dei membri ricevuti dal server: "+size);
            outToServer.write(255);
            if (DEBUGS) System.out.println("[SHOW MEMBERS] send msg to server: "+255);

            if (size!= 0) {
                for (int i=0;i<size;i++) { //lista membri
                    read = inFromServer.readLine();
                    if (DEBUGS) System.out.println("[SHOW MEMBERS] receive msg from server: "+read);
                    tmp.add(read);
                    outToServer.writeBytes(read+'\n');
                    if (DEBUGS) System.out.println("[SHOW MEMBERS] send msg to server: "+read);
                }

                return tmp;
            }
            else { //lista membri vuota
                read = inFromServer.readLine();
                if (DEBUGS) System.out.println("[SHOW MEMBERS] receive msg from server: "+read);
                tmp.add(read);
                return tmp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String MsgAddCard(String msg) {
        String read = null;

        /* invio dati al server */
        if (DEBUGS) System.out.println("[ADDCARD] send msg to server: "+msg);
        try {
            outToServer.writeBytes(msg);
            if (DEBUGS) System.out.println("[ADDCARD] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo i dati dal server*/
        try {
            read = inFromServer.readLine();
            if (DEBUGS) System.out.println("[ADDCARD] receive msg from server: "+read);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return read;
    }

    public ArrayList<Card> MsgShowCards(String msg) {
        String read = null;
        int read_int = 0;
        ArrayList<Card> listacarte = new ArrayList<Card>();

        /* invio dati al server */
        try {
            outToServer.writeBytes(msg+'\n');
            if (DEBUGS) System.out.println("[SHOWCARDS] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo i dati dal server*/
        try {
            read_int = inFromServer.read();
            if (DEBUGS) System.out.println("[SHOWCARDS] receive msg from server: "+read_int);
            if (read_int==1) {
                read = "errore, impossibile eseguire l'operazione, non fai parte del progetto";
                System.out.println(read);
            }
            else if (read_int == 2) {
                read = "errore, progetto inesistente";
                System.out.println(read);
            }
            else if (read_int==0) {
                int size;
                outToServer.write(255);
                size = inFromServer.read(); //leggo dim array
                if (size == 0) {
                    read = "nessuna carta presente";
                    System.out.println(read);
                }
                else { //ok
                    outToServer.write(255);
                    for (int i=0;i<size;i++) {
                        //leggo nome
                        String namecard = inFromServer.readLine();
                        if (DEBUGS) System.out.println("receive from server: "+namecard);
                        outToServer.write(255);
                        if (DEBUGS) System.out.println("send from server: 255");

                        //leggo descrizione
                        String description = inFromServer.readLine();
                        if (DEBUGS) System.out.println("receive from server: "+description);
                        outToServer.write(255);
                        if (DEBUGS) System.out.println("send from server: 255");

                        //leggo stato
                        int state = inFromServer.read();
                        if (DEBUGS) System.out.println("receive from server: "+state);
                        outToServer.write(255);
                        if (DEBUGS) System.out.println("send from server: 255");

                        //leggo historylist
                        ConcurrentLinkedQueue<Integer> copyhistorylist = new ConcurrentLinkedQueue<Integer>();
                        int sizehistory = inFromServer.read(); //ricevo size historylist
                        if (DEBUGS) System.out.println("receive from server: (dim history) "+size);
                        outToServer.write(255);
                        if (DEBUGS) System.out.println("send from server: 255");
                        for (int h=0;h<sizehistory;h++) {
                            int stato = inFromServer.read();
                            if (DEBUGS) System.out.println("receive from server for: "+stato);
                            copyhistorylist.add(stato);
                            outToServer.write(255);
                            if (DEBUGS) System.out.println("send from server: "+255);
                        }

                        //crea carta da aggiungere
                        Card carta = new Card(namecard, description);
                        carta.setState(state);
                        carta.setStateList(copyhistorylist);
                        //aggiungila all'array
                        listacarte.add(carta);
                        /* STAMPA CARTA */
                        carta.print_card();
                    }
                }
            }
            else {
                read = "errore di altro genere";
                System.out.println(read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listacarte;
    }

    /*
     * ritorna una carta specifica all'intero di un progetto specifico
     * return null se la carta non esiste
     */
    public Card MsgShowCard(String msg) {
        int read_int = 0;

        /* invio dati al server */
        try {
            outToServer.writeBytes(msg+'\n');
            if (DEBUGS) System.out.println("[SHOWCARD] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        try {
            read_int = inFromServer.read();
            if (DEBUGS) System.out.println(read_int);

            if (read_int==3) { //errore, carta non presente in questo progetto
                System.out.println("errore, carta non presente in questo progetto");
                return null;
            }
            else if (read_int==1) { //errore, impossibile eseguire l'operazione, non fai parte del progetto
                System.out.println("errore, impossibile eseguire l'operazione, non fai parte del progetto");
                return null;
            }
            else if (read_int==2) { //errore, progetto inesistente
                System.out.println("errore, progetto inesistente");
                return null;
            }
            else if (read_int==0) { //comando corretto
                outToServer.write(255);
                if (DEBUGS) System.out.println("255");

                //leggo nome
                String namecard = inFromServer.readLine();
                if (DEBUGS) System.out.println("receive msg by server: "+namecard);
                outToServer.write(255);
                if (DEBUGS) System.out.println("send msg to server: 255");

                //leggo descrizione
                String description = inFromServer.readLine();
                if (DEBUGS) System.out.println("receive msg by server: "+description);
                outToServer.write(255);
                if (DEBUGS) System.out.println("send msg to server: 255");

                //leggo stato
                int state = inFromServer.read();
                outToServer.write(255);
                if (DEBUGS) System.out.println("send msg to server: 255");

                //crea carta da restituire
                Card carta = new Card(namecard, description);
                carta.setState(state);

                return carta;
            }
            else {
                System.out.println("errore, non dovrei essere arrivato qua");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public String MsgMoveCard(String msg) {
        int read_int = 0;
        String risp = null;

        /* invio dati al server */
        try {
            outToServer.writeBytes(msg+'\n');
            if (DEBUGS) System.out.println("[MOVECARD] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }


        /* ricevo dati dal server */
        try {
            read_int = inFromServer.read();
            if (DEBUGS) System.out.println("[MOVECARD] receive msg by server: "+read_int);

            if (read_int==0) risp = "ok";

            if (read_int==5) risp = "errore di altro genere";

            if (read_int==3) risp = "errore, carta non presente in questo progetto";

            if (read_int==1) risp = "errore, impossibile eseguire l'operazione, non fai parte del progetto";

            if (read_int==2) risp = "errore, progetto inesistente";

            return risp;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> MsgCardHistory(String msg) {
        int read_int = -1;
        int val = 0;
        ArrayList<Integer> historylist = new ArrayList<Integer>();

        /* invio dati al server */
        try {
            outToServer.writeBytes(msg+'\n');
            if (DEBUGS) System.out.println("[MOVECARD] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        try {
            read_int = inFromServer.read();
            if (read_int == 0) { //ok
                outToServer.write(255);
                read_int = inFromServer.read(); //leggo dimensione lista
                outToServer.write(255);
                if (read_int != 0) {
                    for (int i=0;i<read_int;i++) {
                        val = inFromServer.read();
                        historylist.add(val);
                        outToServer.write(255);
                    }

                    return historylist;
                }
                else { //lista vuota
                    System.out.println("lista: null");
                    return null;
                }
            }

            if (read_int==3) System.out.println("errore, carta non presente in questo progetto");

            if (read_int==1) System.out.println("errore, impossibile eseguire l'operazione, non fai parte del progetto");

            if (read_int==2) System.out.println("errore, progetto inesistente");

            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void MsgDeleteProject(String msg) {
        int read_int = 0;

        /* invio dati al server */
        try {
            outToServer.writeBytes(msg+'\n'); //sendchatmsg nameproject nickname
            if (DEBUGS) System.out.println("[SENDCHATMSG] send msg to server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        try {
            read_int = inFromServer.read();
            if (DEBUGS) System.out.println("[SENDCHATMSG] receive msg from server: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (read_int==0) System.out.println("ok");
        else if (read_int==1) System.out.println("errore, impossibile eseguire l'operazione, non fai parte del progetto");
        else if (read_int==2) System.out.println("errore, progetto inesistente");
        else if (read_int==3) System.out.println("errore, non tutte le carte sono DONE, impossibile cancellare progetto");
        else System.out.println("errore, non dovrei essere qua");
    }

    /*
     * il Client contatta il Server per controllare se l'utente fa parte del progetto
     * return 1: l'utente fa parte del progetto
     * return 0: altrimenti
     */
    public int MsgCheckUserinProject (String msg) {

        //controllo di far parte del progetto chiedendolo al server (invio sendchatmsg projectname nickname)
        /* invio dati al server */
        if (DEBUGS) System.out.println("send msg to server: "+msg);
        try {
            outToServer.writeBytes(msg+'\n');
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        //leggi il messaggio inviato dal server
        int read_int=-1;
        try {
            read_int = inFromServer.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (DEBUGS) System.out.println("receive msg by server: "+read_int);

        return read_int;

    }

    /*
     * Metodo per comunicare con il Server per aggiornare la lista degli utenti registrati
     * messaggio da inviare al server: listusers
     */
    public ConcurrentLinkedQueue<Utente> MsgListUsersUpdate() {
        ConcurrentLinkedQueue<Utente> tmp = new ConcurrentLinkedQueue<Utente>();

        /* invio dati al server */
        if (DEBUGS) System.out.println("send msg to server: listusers");
        try {
            outToServer.writeBytes("listusers"+'\n');
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* ricevo dati dal server */
        //leggi il messaggio inviato dal server
        String read_nickname;
        String read_passw;
        try {
            int nutenti = inFromServer.read(); //leggi il numero degli utenti da ricevere
            outToServer.write(255); //invio ok al client
            for (int i=0;i<nutenti;i++) {
                String read_msg = inFromServer.readLine();
                if (DEBUGS) System.out.println("receive msg by server: "+read_msg);
                String ar[] = read_msg.split(" ");
                read_nickname  = ar[0];
                read_passw = ar[1];
                Utente utente = new Utente(read_nickname, read_passw);
                tmp.add(utente);
                outToServer.write(255); //invio ok al server
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tmp;
    }

}
