import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.ObjectMapper;


public class Progetto {
    private String nameproject;
    private ConcurrentLinkedQueue <String> listamembri; //membri che partecipano al progetto
    private ConcurrentLinkedQueue <Card> listatodo; //lista TODO
    private ConcurrentLinkedQueue <Card> listainprogress; //lista INPROGRESS
    private ConcurrentLinkedQueue <Card> listatoberevised; //lista TOBEREVISED
    private ConcurrentLinkedQueue <Card> listadone; //lista DONE



    /*
     * nameproject: nome del progetto, username: nome del creatore del progetto
     */
    public Progetto(String nameproject, String username) {
        if (nameproject==null) {
            System.out.println("nameproject null");
            return;
        }
        if (username == null ) {
            System.out.println("username null");
            return;
        }

        this.nameproject=nameproject;
        listamembri=new ConcurrentLinkedQueue<String>();
        listatodo=new ConcurrentLinkedQueue<Card>();
        listainprogress=new ConcurrentLinkedQueue<Card>();
        listatoberevised=new ConcurrentLinkedQueue<Card>();
        listadone=new ConcurrentLinkedQueue<Card>();

        /* creo cartella del progetto */
        String pathFolderName = "Progetti/"+nameproject;
        File folder = new File(pathFolderName);
        // Verifichiamo che non sia già esistente come cartella
        if (!folder.isDirectory()) // In caso non sia già presente la creiamo
            folder.mkdir();


        /* crea cartella Card */
        pathFolderName = "Progetti/"+nameproject+"/Card";
        folder = new File(pathFolderName);
        // Verifichiamo che non sia già esistente come cartella
        if (!folder.isDirectory()) // In caso non sia già presente la creiamo
            folder.mkdir();

        //aggiungo il membro creatore alla lista dei membri
        add_member(username);
    }


    public String get_nameproject() { return nameproject; }


    /*
     * aggiungi membro al progetto
     * member: membro da aggiungere
     * member_invito: membro che ha invitato l'altro (deve farne parte del progetto)
     */
    public String add_member(String member) {
        String to_send = null;

        if (member==null) { //completare (controlla che il membro abbia creato un account)
            to_send = "membro null o inesistente";
            System.out.println("membro null o inesistente");
            return to_send;
        }
        //controllo che il membro da aggiungere non sia già presente
        if (listamembri.contains(member)) {
            to_send = "membro già presente";
            System.out.println(to_send);
            return to_send;
        }


        listamembri.add(member);

        /* creo lista.txt contentente i membri del progetto */
        String pathListName = "Progetti/"+nameproject+"/listamembri.txt";
        //aggiungo il membro creatore alla lista dei membri
        FileWriter w;
        try {
            w = new FileWriter(pathListName);
            BufferedWriter b=new BufferedWriter (w);
            b.write(member+'\n');
            b.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        to_send = "membro aggiunto correttamente";
        return to_send;
    }


    /*
     * aggiungi membro al progetto
     * member: membro da aggiungere
     * member_invito: membro che ha invitato l'altro (deve fare parte del progetto)
     */
    public String add_member(String member, String member_invito) {
        String to_send = null;

        if (member==null) { //completare (controlla che il membro abbia creato un account)
            to_send = "membro null o inesistente";
            System.out.println("membro null o inesistente");
            return to_send;
        }
        if (listamembri.contains(member)) {
            to_send = "membro già presente";
            System.out.println(to_send);
            return to_send;
        }
        if (!listamembri.contains(member_invito)) {
            to_send = "impossibile aggiungere membro, non fai parte del progetto";
            System.out.println(to_send);
            return to_send;
        }

        listamembri.add(member);

        String pathListName = "Progetti/"+nameproject+"/listamembri.txt";

        try {
            PrintWriter outputStream = new PrintWriter(new FileOutputStream(pathListName, true));
            outputStream.append(member+'\n');
            outputStream.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        to_send = "membro aggiunto correttamente";
        return to_send;
    }

    /*
     * rimuovi il membro dal progetto
     */
    public boolean remove_member(String member) {
        if (member==null) {
            System.out.println("membro null");
            return false;
        }
        if ( listamembri.remove(member) ) {
            System.out.println(member+" rimosso con successo");

            /* rimuovo membro dalla listamembri */
            //leggo il contenuto della lista membri e lo copio su una stringa senza copiare
            //la riga che contiene il membro da rimuovere e dopo cancello la lista membri e
            //ne creo un'altra copiandoci il contenuto della stringa
            try {
                FileReader f = new FileReader("Progetti/"+nameproject+"/listamembri.txt");
                BufferedReader b = new BufferedReader(f);
                String copy = null; //stringa contentente tutta la listamembri.txt
                String tmp;
                try {
                    tmp = b.readLine();

                    if (tmp!=null) {
                        while (tmp!=null) {
                            if (tmp.compareTo(member)!=0)
                                copy = copy + tmp;
                            tmp = b.readLine();
                        }
                    }
                    b.close();

                    //elimino la listamembri.txt e dopo la sostituisco con quella nuova
                    File file = new File("Progetti/"+nameproject+"/listamembri.txt");
                    file.delete();

                    //copio stringa nella lista
                    FileWriter w;
                    try {
                        w = new FileWriter("Progetti/"+nameproject+"/listamembri.txt");
                        BufferedWriter buffer=new BufferedWriter (w);
                        buffer.write(copy);
                        buffer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            return true;
        }
        else {
            System.out.println(member+" non fa parte del progetto");
            return false;
        }
    }

    /*
     * aggiungi carta al progetto
     */
    public boolean add_card(String cardname, String description) {

        if (cardname==null) {
            System.out.println("cardname null");
            return false;
        }

        if (listcontainsCard(cardname)) {
            System.out.println("cardname già presente");
            return false;
        }

        Card card = new Card(cardname, description);
        listatodo.add(card);

        /* creo file contentente le info della card e la serializzo*/
        ObjectMapper objectMapper = new ObjectMapper();
        Card Carta = new Card(cardname, description);
        File file = null;

        try {
            file = new File("Progetti/"+nameproject+"/Card/"+cardname+".json");
            file.createNewFile();
            objectMapper.writeValue(file, Carta);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /*
     * sposta carta dallo stato1 allo stato2
     */
    public boolean move_card(String cardname, int stato1, int stato2) {
        boolean DEBUG = false;

        if (DEBUG) {
            System.out.println("cardname: "+cardname+" stato1:"+stato1+" stato2:"+stato2);
            print_listacarte();
        }

        //controlla lo stato iniziale della carta
        Card carta = search_card(cardname);
        if (stato1 != carta.getState()) {
            System.out.println("stato1 errato, non combacia con lo stato effettivo della carta");
        }

        //controlla l'esistenza della carta
        if (!listcontainsCard(cardname)) {
            System.out.println("cardname non esiste 1");
            return false;
        }
        //controllo che cardname sia nella lista di tipo stato1
        if (!listcontainsCard(cardname, stato1)) {
            System.out.println("cardname non esiste 2");
            return false;
        }
        //controllo che gli stati siano consistenti
        if ( (stato1==0 && stato2==2) || (stato1==0 && stato2==3) || (stato1==1 && stato2==0) ) {
            System.out.println("spostamento non consentito");
            return false;
        }

        Card tmp = null;

        //listatodo
        if (stato1==0) {
            //prendi carta da cardname
            for( Card card : listatodo ) {
                if (card.getName().contentEquals(cardname)) {
                    //crea oggetto
                    tmp = new Card(card.getName(),card.getDescription());
                    tmp.setStateList(card.getStateList());
                    tmp.setState(stato2);
                    //elimina oggetto dalla lista1
                    listatodo.remove(card);
                    //aggiungi oggetto alla lista 2
                    listainprogress.add(tmp);
                    //aggiorna file.json
                    ObjectMapper objectMapper = new ObjectMapper();
                    File file=new File("Progetti/"+nameproject+"/Card/"+card.getName()+".json");
                    try {
                        file.createNewFile();
                        objectMapper.writeValue(file, tmp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (DEBUG) {
                        System.out.println("STAMPA DOPO MODIFICHE: ");
                        print_listacarte();
                    }
                    return true;
                }
            }
        }

        //listainprogress
        if (stato1==1) {
            //prendi carta da cardname
            for( Card card : listainprogress ) {
                if (card.getName().contentEquals(cardname)) {
                    //crea oggetto
                    tmp = new Card(card.getName(),card.getDescription());
                    tmp.setStateList(card.getStateList());
                    tmp.setState(stato2);
                    //elimina oggetto dalla lista1
                    listainprogress.remove(card);
                    //aggiungi oggetto alla lista 2
                    if (stato2==2) {
                        listatoberevised.add(tmp);

                        //aggiorna file.json
                        ObjectMapper objectMapper = new ObjectMapper();
                        File file=new File("Progetti/"+nameproject+"/Card/"+card.getName()+".json");
                        try {
                            file.createNewFile();
                            objectMapper.writeValue(file, tmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (DEBUG) {
                            System.out.println("STAMPA DOPO MODIFICHE: ");
                            print_listacarte();
                        }
                        return true;
                    }
                    else {
                        listadone.add(tmp);
                        //aggiorna file.json
                        ObjectMapper objectMapper = new ObjectMapper();
                        File file=new File("Progetti/"+nameproject+"/Card/"+card.getName()+".json");
                        try {
                            file.createNewFile();
                            objectMapper.writeValue(file, tmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (DEBUG) {
                            System.out.println("STAMPA DOPO MODIFICHE: ");
                            print_listacarte();
                        }
                        return true;
                    }
                }
            }
        }

        //listatoberevised
        if (stato1==2) {
            //prendi carta da cardname
            for( Card card : listatoberevised ) {
                if (card.getName().contentEquals(cardname)) {
                    //crea oggetto
                    tmp = new Card(card.getName(),card.getDescription());
                    tmp.setStateList(card.getStateList());
                    tmp.setState(stato2);
                    //elimina oggetto dalla lista1
                    listatoberevised.remove(card);
                    //aggiungi oggetto alla lista 2
                    if (stato2==1) {
                        listainprogress.add(tmp);
                        //aggiorna file.json
                        ObjectMapper objectMapper = new ObjectMapper();
                        File file=new File("Progetti/"+nameproject+"/Card/"+card.getName()+".json");
                        try {
                            file.createNewFile();
                            objectMapper.writeValue(file, tmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (DEBUG) {
                            System.out.println("STAMPA DOPO MODIFICHE: ");
                            print_listacarte();
                        }
                        return true;
                    }
                    else {
                        listadone.add(tmp);
                        //aggiorna file.json
                        ObjectMapper objectMapper = new ObjectMapper();
                        File file=new File("Progetti/"+nameproject+"/Card/"+card.getName()+".json");
                        try {
                            file.createNewFile();
                            objectMapper.writeValue(file, tmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (DEBUG) {
                            System.out.println("STAMPA DOPO MODIFICHE: ");
                            print_listacarte();
                        }
                        return true;
                    }
                }
            }
        }


        return false;
    }

    /*
     * rimuove carta "cardname" (da una delle liste in base all'elemento scelto da eliminare)
     * return true se la rimuove else false
     */
    public boolean remove_card(String cardname) {
        if (cardname==null) {
            System.out.println("cardname null");
            return false;
        }
        else if (!listcontainsCard(cardname)) {
            System.out.println("carta non presente");
            return false;
        }

        int lista = getlistcontainsCard(cardname); //lista indica l'indice della lista che contiene la carta cardname

        /* cerco oggetto da rimuovere e lo tolgo */
        Card tmp = null;

        //scorre listatodo
        if (lista==0) {
            for( Card card : listatodo ) {
                if (card.getName().compareTo(cardname)==0) {
                    tmp=new Card(card.getName(),card.getDescription());
                    tmp.setState(card.getState());
                    listatodo.remove(tmp);
                    return true;
                }
            }
        }
        //scorre listainprogress
        if (lista==1) {
            for( Card card : listainprogress ) {
                if (card.getName().compareTo(cardname)==0) {
                    tmp=new Card(card.getName(),card.getDescription());
                    tmp.setState(card.getState());
                    listainprogress.remove(tmp);
                    return true;
                }
            }
        }
        //scorre listatoberevised
        if (lista==2) {
            for( Card card : listatoberevised ) {
                if (card.getName().compareTo(cardname)==0) {
                    tmp=new Card(card.getName(),card.getDescription());
                    tmp.setState(card.getState());
                    listatoberevised.remove(tmp);
                    return true;
                }
            }
        }
        //scorre listadone
        if (lista==3) {
            for( Card card : listadone ) {
                if (card.getName().compareTo(cardname)==0) {
                    tmp=new Card(card.getName(),card.getDescription());
                    tmp.setState(card.getState());
                    listadone.remove(tmp);
                    return true;
                }
            }
        }

        return false;
    }


    /*
     * controllo che tutte le Card siano nella lista DONE
     */
    public boolean canCancelProject() {

        //scorre lista TODO
        if (listatodo.size()!=0) {
            return false;
        }
        //scorre lista INPROGRESS
        if (listainprogress.size()!=0) {
            return false;
        }
        //scorre lista TOBEREVISED
        if (listatoberevised.size()!=0) {
            return false;
        }

        //scorre lista DONE
        return true;
    }


    /* restituisci le carte presenti nel progetto */
    public ArrayList<Card> showcards() {
        ArrayList<Card> listacarte = new ArrayList<Card>();

        //scorre lista TODO
        for (Card card : listatodo)
            listacarte.add(card);
        //scorre lista INPROGRESS
        for (Card card : listainprogress)
            listacarte.add(card);
        //scorre lista TOBEREVISED
        for (Card card : listatoberevised)
            listacarte.add(card);
        //scorre lista DONE
        for (Card card : listadone) {
            listacarte.add(card);
        }

        return listacarte;
    }


    //UTILITY METHOD//

    /*
     * "stato" compreso tra [0,3]: 0->listatodo, 1->listainprogress, 2->toberevised, 3->done
     * ritorna true se la lista specificata da "stato" contiene la carta "cardname"
     * altrimenti false
     */
    private boolean listcontainsCard(String nomecarta, int stato) {
        //scorre listatodo
        if (stato==0) {
            for( Card card : listatodo ){
                if (card.getName().compareTo(nomecarta)==0)
                    return true;
            }
            return false;
        }
        //scorre listainprogress
        if (stato==1) {
            for( Card card : listainprogress ){
                if (card.getName().compareTo(nomecarta)==0)
                    return true;
            }
            return false;
        }
        //scorre listatoberevised
        if (stato==2) {
            for( Card card : listatoberevised ){
                if (card.getName().compareTo(nomecarta)==0)
                    return true;
            }
            return false;
        }
        //scorre listadone
        if (stato==3) {
            for( Card card : listadone ){
                if (card.getName().compareTo(nomecarta)==0)
                    return true;
            }
        }

        return false;
    }

    /*
     * ritorna true se nelle liste stato (listatodo, listainprogress, listatoberevised o listadone) è presente una card con lo stesso nome di quello passato per argomento
     * altrimenti false
     */
    private boolean listcontainsCard(String nomecarta) {
        //scorre listatodo
        for( Card card : listatodo ) {
            if (card.getName().compareTo(nomecarta)==0)
                return true;
        }
        //scorre listainprogress
        for( Card card : listainprogress ) {
            if (card.getName().compareTo(nomecarta)==0)
                return true;
        }
        //scorre listatoberevised
        for( Card card : listatoberevised ) {
            if (card.getName().compareTo(nomecarta)==0)
                return true;
        }
        //scorre listadone
        for( Card card : listadone ) {
            if (card.getName().compareTo(nomecarta)==0)
                return true;
        }

        return false;
    }

    /*
     * ritorna 0->nomecarta si trova nella listatodo, 1->nomecarta si trova nella listainprogress, 2->nomecarta si trova nella listatoberevised,
     * 3->nomecarta si trova nella listadone
     * ritorna -1 se nomecarta non è presente in nessuna lista
     */
    private int getlistcontainsCard(String nomecarta) {
        //scorre listatodo
        for( Card card : listatodo ) {
            if (card.getName().compareTo(nomecarta)==0)
                return 0;
        }
        //scorre listainprogress
        for( Card card : listainprogress ) {
            if (card.getName().compareTo(nomecarta)==0)
                return 1;
        }
        //scorre listatoberevised
        for( Card card : listatoberevised ) {
            if (card.getName().compareTo(nomecarta)==0)
                return 2;
        }
        //scorre listadone
        for( Card card : listadone ) {
            if (card.getName().compareTo(nomecarta)==0)
                return 3;
        }

        return -1;
    }

    /*
     * controlla se il membro passato per argomento se fa parte del progetto
     * return true: member fa parte del progetto
     * return false: member non fa parte del progetto
     */
    public boolean search_member(String member) {
        return listamembri.contains(member);
    }


    /*
     * ricerca carta nelle quattro liste e la restituisce se esiste
     * altrimenti ritorna null
     */
    public Card search_card(String nomecarta) {
        //scorre listatodo
        for( Card card : listatodo ) {
            if (card.getName().compareTo(nomecarta)==0)
                return card;
        }
        //scorre listainprogress
        for( Card card : listainprogress ) {
            if (card.getName().compareTo(nomecarta)==0)
                return card;
        }
        //scorre listatoberevised
        for( Card card : listatoberevised ) {
            if (card.getName().compareTo(nomecarta)==0)
                return card;
        }
        //scorre listadone
        for( Card card : listadone ) {
            if (card.getName().compareTo(nomecarta)==0)
                return card;
        }

        return null;
    }


    public ConcurrentLinkedQueue<String> get_listamembri() {
        return listamembri;
    }

    /*
     * metodo utilizzato dalla init_listproject (usata nel MainServer) per inizializzare il server
     */
    public boolean add_card(String cardname, String description, int state, ConcurrentLinkedQueue<Integer> concurrentLinkedQueue) {

        if (cardname==null) {
            System.out.println("cardname null");
            return false;
        }

        if (listcontainsCard(cardname)) {
            System.out.println("cardname già presente");
            return false;
        }

        Card card = new Card(cardname, description);
        card.onlysetState(state);
        card.setStateList(concurrentLinkedQueue);
        if (state==0)
            listatodo.add(card);
        if (state==1)
            listainprogress.add(card);
        if (state==2)
            listatoberevised.add(card);
        if (state==3)
            listadone.add(card);

        return true;
    }


    /* METODI DI STAMPA */
    public void print_progetto() {
        System.out.println("nome progetto: "+nameproject);
        System.out.println("lista membri: "+listamembri);
        print_listacarte();
    }

    public void print_listacarte() {
        System.out.println("** listacarte ** ");

        //scorre lista TODO
        if (listatodo.size()==0)
            System.out.println("lista TODO: vuota");
        else {
            System.out.println("lista TODO");
            for (Card card : listatodo)
                card.print_card();
        }
        //scorre lista INPROGRESS
        if (listainprogress.size()==0)
            System.out.println("lista INPROGRESS: vuota");
        else {
            System.out.println("lista INPROGRESS");
            for (Card card : listainprogress)
                card.print_card();
        }
        //scorre lista TOBEREVISED
        if (listatoberevised.size()==0)
            System.out.println("lista TOBEREVISED: vuota");
        else {
            System.out.println("lista TOBEREVISED");
            for (Card card : listatoberevised)
                card.print_card();
        }
        //scorre lista DONE
        if (listadone.size()==0)
            System.out.println("lista DONE: vuota");
        else {
            System.out.println("lista DONE");
            for (Card card : listadone)
                card.print_card();
        }
    }

    public void print_listamembri() {
        System.out.println("lista membri: "+listamembri);
    }

}