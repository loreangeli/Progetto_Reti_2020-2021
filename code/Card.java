import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Card implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private int state; //stato della carta -> state compresa tra [0,3]  0:TODO, 1:INPROGRESS, 2:TOBEREVISED, 3:DONE
    private ConcurrentLinkedQueue<Integer> historylist = new ConcurrentLinkedQueue<Integer>(); //(card history) lista degli stati


    //RICORDA: Ci vuole il costruttore default senza argomenti, altrimenti errore serializzazione
    public Card() {

    }

    public Card(String name, String description) {
        this.name=name;
        this.description=description;
        state=0;
        historylist.add(state);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getState() {
        return state;
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * aggiorna lo stato corrente senza modificare la historystate
     */
    public void onlysetState(int stato) {
        if (state<0 || state>3)
            System.out.println("errore state");
        else {
            state=stato;
        }
    }

    /*
     * aggiorna lo stato corrente a "stato" e lo aggiunge in coda alla historylist
     */
    public void setState(int stato) {
        if (state<0 || state>3)
            System.out.println("errore state");
        else {
            state=stato;
            historylist.add(stato);
        }
    }

    /*
     * aggiunge nuovo stato alla historylist (storia della carta)
     */
    public void addStateList(int stato) {
        historylist.add(stato);
    }

    public ConcurrentLinkedQueue<Integer> getStateList() {
        return historylist;
    }

    /*
     * sovrascrive la historylist corrente con la historylist passata per argomento
     */
    public void setStateList(ConcurrentLinkedQueue<Integer> concurrentLinkedQueue) {
        historylist = new ConcurrentLinkedQueue<Integer>(concurrentLinkedQueue);
    }

    /*
     * ritorna la dimensione della historylist
     */
    public int size_history() {
        return historylist.size();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj==null) return false;
        if( !(obj instanceof Card)) return false;

        Card o = (Card) obj;
        if (this.name==o.name && this.description==o.description && this.state==o.state)
            return true;

        return false;
    }

    public void print_card() {
        System.out.println("name:"+name+", description:"+description+", stato:"+state+", historylist: "+historylist);
    }
}