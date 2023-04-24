import java.io.Serializable;

public class Chat implements Serializable {
    public Chat(String username, String message, int voteValue) {
        this.username = username;
        this.message = message;
        this.voteValue = voteValue;
    }
    static final long serialVersionUID = 1L;
    private String username;
    private String message;
    private int voteValue;
    @Override
    public String toString() {
     return String.format("%5s: %5s \n", username, message);
    }
   
    
}
