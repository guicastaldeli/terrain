package main.com.app.root.screen_controller.main;
import main.com.app.root.DocParser;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenElement;

public class SaveNameDialog extends Screen {
    private static final String DIALOG_PATH = DIR + "title/save_name_dialog.xml";
    private final MainScreenAction titleScreenAction;
    private String enteredName = "";
    private boolean active = false;
    
    public SaveNameDialog(MainScreenAction titleScreenAction) {
        super(DIALOG_PATH, "save_name_dialog");
        this.titleScreenAction = titleScreenAction;
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
            titleScreenAction.start(enteredName.trim());
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
            if(el.id.equals("name_display")) {
                el.text = enteredName.isEmpty() ? "Enter save name..." : enteredName;
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
}
