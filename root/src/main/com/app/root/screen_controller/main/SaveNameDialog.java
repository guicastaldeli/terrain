package main.com.app.root.screen_controller.main;
import main.com.app.root.DocParser;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenElement;

public class SaveNameDialog extends Screen {
    public static final String DIALOG_PATH = DIR + "main/save_name_dialog.xml";
    private final MainScreenAction mainScreenAction;
    public boolean active = false;
    private String enteredName = "";
    
    public SaveNameDialog(MainScreenAction mainScreenAction) {
        super(DIALOG_PATH, "save_name_dialog");
        this.mainScreenAction = mainScreenAction;
    }

    @Override
    public void handleAction(String action) {
        switch(action) {
            case "confirm":
                confirmName();
                break;
            case "cancel":
                cancel();
                break;
            case "clear":
                enteredName = "";
                updateNameDisplay();
                break;
        }
    }

    /**
     * Confirm Save Name
     */
    private void confirmName() {
        if(!enteredName.trim().isEmpty()) {
            mainScreenAction.start(enteredName.trim());
        }
        setActive(false);
        clearEl();
    }

    /**
     * Cancel
     */
    private void cancel() {
        setActive(false);
        clearEl();
        enteredName = "";
    }

    /**
     * Update Name Display
     */
    private void updateNameDisplay() {
        for(ScreenElement el : screenData.elements) {
            if(el.id.equals("nameDisplay")) {
                el.text = enteredName;
                break;
            }
        }
    }

    /**
     * Show
     */
    public void show() {
        setActive(true);
        active = true;
        enteredName = "";

        try {
            this.screenData = DocParser.parseScreen(
                DIALOG_PATH,
                window.getWidth(),
                window.getHeight()
            );
            updateNameDisplay();
        } catch (Exception e) {
            System.err.println("Failed to parse save name dialog: " + e.getMessage());
        }
    }

    public boolean isActive() {
        return active;
    }

    public void clearEl() {
        screenData.elements.clear();
    }

    @Override
    public void render() {
        if(active) {
            super.render();
        }
    }

    @Override
    public void onWindowResize(int width, int height) {
        if(getTextRenderer() != null) {
            getTextRenderer().updateScreenSize(width, height);
        }
        
        try {
            this.screenData = DocParser.parseScreen(
                DIALOG_PATH,
                width,
                height
            );
        } catch (Exception err) {
            System.err.println("Failed to re-parse save menu on resize: " + err.getMessage());
        }
    }
}
