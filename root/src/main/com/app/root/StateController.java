package main.com.app.root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateController implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isPaused;
    private boolean isInMenu;
    private String currentLevel;
    private List<String> activeEvents;
    private Map<String, Object> tempState;

    private boolean saveInProgress;
    private boolean loadInProgress;
    private String currentSaveId;
    private long lastAutoSaveTime;
    private int autoSaveInterval = 600;

    public StateController() {
        isPaused = false;
        isInMenu = true;
        currentLevel = "title_screen";
        activeEvents = new ArrayList<>();
        tempState = new HashMap<>();

        saveInProgress = false;
        loadInProgress = false;
        currentSaveId = null;
        lastAutoSaveTime = System.currentTimeMillis();
    }

    /* Paused */
    public void setPaused(boolean paused) {
        isPaused = paused;
    }
    public boolean isPaused() {
        return isPaused;
    }

    /* Is In Menu */
    public void setInMenu(boolean inMenu) {
        isInMenu = inMenu;
    }
    public boolean isInMenu() {
        return isInMenu;
    }

    /* Current Save Id */
    public void setCurrentSaveId(String saveId) { 
        currentSaveId = saveId; 
    }
    public String getCurrentSaveId() { 
        return currentSaveId; 
    }

    /* Current Level */
    public void setCurrentLevel(String level) {
        currentLevel = level;
    }
    public String getCurrentLevel() {
        return currentLevel;
    }

    /* Events */
    public void addActiveEvent(String event) {
        if(!activeEvents.contains(event)) {
            activeEvents.add(event);
        }
    }
    public void removeActiveEvent(String event) {
        activeEvents.remove(event);
    }
    public boolean hasActiveEvent(String event) {
        return activeEvents.contains(event);
    }

    /* Temp State */
    public void setTempState(String key, Object val) {
        tempState.put(key, val);
    }
    public Object getTempState(String key) {
        return tempState.get(key);
    }
    public void clearTempState() {
        tempState.clear();
    }

    /* Save */
    public void setSaveInProgress(boolean saving) {
        saveInProgress = saving;
    }
    public boolean isSaveInProgress() {
        return saveInProgress;
    }

    public void setLoadInProgress(boolean loading) {
        loadInProgress = loading;
    }
    public boolean isLoadInProgress() {
        return loadInProgress;
    }

    public boolean shouldAutoSave() {
        long currentTime = System.currentTimeMillis();
        long elapsed = (currentTime - lastAutoSaveTime) / 1000;
        return elapsed >= autoSaveInterval && !isPaused && !isInMenu;
    }

    public void resetAutoSaveTimer() {
        lastAutoSaveTime = System.currentTimeMillis();
    }

    public void setAutoSaveInterval(int secs) {
        autoSaveInterval = secs;
    }
    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }
}
