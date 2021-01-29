import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Messaggi {

    private String nameproject;
    private ConcurrentLinkedQueue<String> msglist; //lista dei messaggi di un progetto


    public Messaggi() {
    }

    public Messaggi(String name) {
        nameproject = name;
        msglist = new ConcurrentLinkedQueue<String>();
    }

    public String getNameproject() {
        return nameproject;
    }

    public void setNameproject(String name) {
        nameproject = name;
    }

    public ConcurrentLinkedQueue<String> getMsglist() {
        return msglist;
    }

    public void setMsglist(ConcurrentLinkedQueue<String> list) {
        if (list!=null)
            msglist = new ConcurrentLinkedQueue<String>(list);
    }

    /*
     * aggiungi mesaggio in coda alla lista dei messaggi (msglist)
     */
    public void add_msg(String messaggio) {
        msglist.add(messaggio);
    }

    /*
     * rimuove il primo messaggio ricevuto dalla lista dei messaggi (msglist)
     * (rimuove dalla testa della lista)
     */
    public String remove_msg() {
        String msg_remove = null;
        try {
            msg_remove = msglist.remove();
        }
        catch (NoSuchElementException e) {
            return null;
        }
        return msg_remove;
    }

    /*
     * rimuovi tutti i messaggi dalla lista dei messaggi (msglist)
     */
    public void remove_allmsg() {
        while (true) {
            try {
                msglist.remove();
            }
            catch (NoSuchElementException e) {
                return;
            }
        }
    }

    public void print() {
        System.out.println("messaggi pendenti: "+nameproject+" "+msglist);
    }

}
