package main.com.app.root._save;

public class SaveInfo {
    public String saveId;
    public String saveName;
    public String creationDate;
    public String lastPlayed;
    public String playTime;
    public String version;
    public String lastModified;

    @Override
    public String toString() {
        return saveName + " - " + playTime + " - " + lastPlayed;
    }
}
