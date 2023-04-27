import java.io.Serializable;

public class Vote implements Serializable {
    String voteValue;
    static final long serialVersionUID = 1L;
    public Vote(String voteValue) {
        this.voteValue = voteValue;
    }
    public String getVoteValue() {
        return voteValue;
    }
    public void setVoteValue(String voteValue) {
        this.voteValue = voteValue;
    }
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}