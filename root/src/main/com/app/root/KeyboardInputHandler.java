package main.com.app.root;
import static org.lwjgl.glfw.GLFW.*;

public class KeyboardInputHandler {
    private String currentText = "";
    private boolean capsLock = false;
    private boolean shiftPressed = false;
    private int maxLen = 50;
    
    public KeyboardInputHandler(int maxLen) {
        this.maxLen = maxLen;
    }

    /**
     * Handle Key
     */
    public boolean handleKey(int key, int action) {
        if (key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT) {
            shiftPressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
            return false;
        }
        if(key == GLFW_KEY_CAPS_LOCK && action == GLFW_PRESS) {
            capsLock = !capsLock;
            return false;
        }
        if(action != GLFW_PRESS && action != GLFW_REPEAT) {
            return false;
        }
        if(key == GLFW_KEY_ENTER || key == GLFW_KEY_ESCAPE) {
            return false;
        }
        return handleTextInput(key);
    }

    /**
     * Handle Backspace
     */
    private boolean handleBackspace() {
        if(!currentText.isEmpty()) {
            currentText = currentText.substring(0, currentText.length() - 1);
            return true;
        }
        return false;
    }

    /**
     * Handle Text Input
     */
    private boolean handleTextInput(int key) {
        if(currentText.length() >= maxLen) {
            return false;
        }

        char c = getCharForKey(key);
        if(c != '\0') {
            currentText += c;
            return true;
        }

        return false;
    }

    /**
     * Get Char for Key
     */
    private char getCharForKey(int key) {
        if(key >= GLFW_KEY_A && key <= GLFW_KEY_Z) {
            char baseChar = (char) ('a' + (key - GLFW_KEY_A));
            boolean makeUppercase = shiftPressed ^ capsLock; // XOR
            return makeUppercase ? Character.toUpperCase(baseChar) : baseChar;
        }
        if(key >= GLFW_KEY_0 && key <= GLFW_KEY_9) {
            if (shiftPressed) {
                char[] symbols = {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')'};
                return symbols[key - GLFW_KEY_0];
            }
            return (char) ('0' + (key - GLFW_KEY_0));
        }
        if(key == GLFW_KEY_SPACE) {
            return ' ';
        }
        switch(key) {
            case GLFW_KEY_MINUS:
                return shiftPressed ? '_' : '-';
            case GLFW_KEY_EQUAL:
                return shiftPressed ? '+' : '=';
            case GLFW_KEY_LEFT_BRACKET:
                return shiftPressed ? '{' : '[';
            case GLFW_KEY_RIGHT_BRACKET:
                return shiftPressed ? '}' : ']';
            case GLFW_KEY_SEMICOLON:
                return shiftPressed ? ':' : ';';
            case GLFW_KEY_APOSTROPHE:
                return shiftPressed ? '"' : '\'';
            case GLFW_KEY_COMMA:
                return shiftPressed ? '<' : ',';
            case GLFW_KEY_PERIOD:
                return shiftPressed ? '>' : '.';
            case GLFW_KEY_SLASH:
                return shiftPressed ? '?' : '/';
            case GLFW_KEY_BACKSLASH:
                return shiftPressed ? '|' : '\\';
            case GLFW_KEY_GRAVE_ACCENT:
                return shiftPressed ? '~' : '`';
        }
        
        return '\0';
    }

    /**
     * Text
     */
    public void setText(String text) {
        if(text.length() <= maxLen) {
            this.currentText = text;
        } else {
            this.currentText = text.substring(0, maxLen);
        }
    }
    public String getText() {
        return currentText;
    }

    /**
     * Max Length
     */
    public void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
        if(currentText.length() > maxLen) {
            currentText = currentText.substring(0, maxLen);
        }
    }
    public int getMaxLen() {
        return maxLen;
    }

    public boolean isShiftPressed() {
        return shiftPressed;
    }

    public boolean isCapsLock() {
        return capsLock;
    }
    
    public int getLength() {
        return currentText.length();
    }
    
    public boolean isEmpty() {
        return currentText.isEmpty();
    }

    public void clear() {
        currentText = "";
    }
}
