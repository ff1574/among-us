import java.io.Serializable;

/**
 * simple Chat Message object that returns a string (via its toString() method)
 * of the username (player.getPlayername) followed by the message
 * (taMsg.getText), to be passed between server/clients
 */

public class Chat implements Serializable {
    public Chat(String username, String message) {
        this.username = username;
        this.message = message;

    }

    static final long serialVersionUID = 1L;
    private String username;
    private String message;

    @Override
    public String toString() {
        return String.format("%5s: %5s \n", username, message);
    }

}
