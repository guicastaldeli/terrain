package main.com.app.root;
import java.io.*;

import main.com.app.root.screen.ScreenController;
import main.com.app.root.screen.ScreenController.SCREENS;

public class Console {
    private static Console instance;
    private Main main;
    private Window window;    
    private Scene scene;
    private ScreenController screenController;

    private boolean running = false;
    
    private boolean standalone = false;
    private boolean initializationComplete = false;
    private Thread logWatcherThread;
    private static final String LOG_FILE = "output.log";
    private static final String LOCK_FILE = "console.lock";
    private long sessionStartTime = 0;
    private boolean sessionDetected = false;

    public static Console getInstance() {
        if(instance == null) {
            instance = new Console();
        }
        return instance;
    }

    /**
     * Init
     */
    public void init(Main main, Window window, ScreenController screenController) {
        System.out.println("Console Ready...");
        this.main = main;
        this.window = window;
        this.screenController = screenController;
        this.standalone = false;

        this.running = false;
        this.initializationComplete = false;
        this.sessionDetected = false;
        
        clearLogFile();
        sessionStartTime = System.currentTimeMillis();
        
        initializationComplete = true;
        switchToMainScreen();
    }

    /**
     * Run
     */
    public void run() {
        this.standalone = true;
        this.initializationComplete = true;
        this.sessionDetected = false;
        
        clearLockFile();
        
        System.out.println("=============================================================");
        System.out.println("                                                             "); 
        System.out.println("                 Console --- Terrain  Beta                   "); 
        System.out.println("                                                             "); 
        System.out.println("=============================================================");
        System.out.println(" ");
        System.out.println("Waiting for start...");
        System.out.println();
        
        clearLogFile();
        sessionStartTime = System.currentTimeMillis();
        startLogWatcher();
        
        createLockFile();
        
        keepAlive();
    }
    
    /**
     * Setup Log
     */
    private void clearLogFile() {
        try {
            PrintWriter writer = new PrintWriter(LOG_FILE);
            writer.close();
            File file = new File(LOG_FILE);
            file.setLastModified(0);
        } catch (IOException e) {
            return;
        }
    }
    
    /**
     * Create lock file
     */
    private void createLockFile() {
        try {
            PrintWriter writer = new PrintWriter(LOCK_FILE);
            writer.println("CONSOLE_ACTIVE");
            writer.close();
        } catch (IOException e) {
            return;
        }
    }
    
    /**
     * Clear lock file
     */
    private void clearLockFile() {
        File lock = new File(LOCK_FILE);
        if(lock.exists()) {
            lock.delete();
        }
    }
    
    /**
     * Write Log
     */
    private void writeToLogOnly(String message) {
        try(PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(message);
            writer.flush();
        } catch (IOException e) {
            return;
        }
    }
    
    /**
     * Start Watcher - FIXED version
     */
    private void startLogWatcher() {
        logWatcherThread = new Thread(() -> {
            long lastSize = 0;
            boolean isNewSession = false;
            
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    File file = new File(LOG_FILE);
                    
                    long fileAge = System.currentTimeMillis() - file.lastModified();
                    if(fileAge < 2000 && !sessionDetected && file.length() > 0) {
                        isNewSession = true;
                        sessionDetected = true;
                    }
                    
                    if(file.exists()) {
                        long currentSize = file.length();
                        if(isNewSession) {
                            System.out.println();
                            System.out.println("------ Session Started ------");
                            System.out.println();
                            isNewSession = false;
                        }
                        
                        if(currentSize > lastSize) {
                            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                                raf.seek(lastSize);
                                String line;
                                while((line = raf.readLine()) != null) {
                                    System.out.println(line);
                                }
                                lastSize = raf.getFilePointer();
                            }
                        } else if(currentSize < lastSize) {
                            lastSize = 0;
                            sessionDetected = false;
                            System.out.println();
                            System.out.println("------ Session Restarted ------");
                            System.out.println();
                        }
                    }
                    
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                if(!Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        });
        logWatcherThread.setDaemon(true);
        logWatcherThread.start();
    }
    
    /**
     * Keep Alive
     */
    private void keepAlive() {
        try {
            while(true) {
                Thread.sleep(1000);
                File lock = new File(LOCK_FILE);
                if(!lock.exists()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            return;
        }
    }
    
    private void stopLogWatcher() {
        if(logWatcherThread != null) {
            logWatcherThread.interrupt();
        }
        clearLockFile();
    }

    /**
     * Switch to Main Screen
     */
    public void switchToMainScreen() {
        info("Initialized!");
        info("Initializing main screen...");
        if(!standalone && screenController != null) {
            screenController.switchTo(SCREENS.MAIN);
            screenController.enableCursor();
        }
        running = false;
        info("Main screen initialized!");
    }

    /**
     * Start
     */
    public void start() {
        if(!running) {
            info("Starting...");

            if(!standalone && screenController != null) {
                //////
            }
            running = true;

            info("STARTED - Scene is now active!");
            info("Player controls enabled...");
            info("Cursor disabled...");
        } else {
            info("already running!");
        }
    }

    /**
     * Pause
     */
    public void pause() {
        if(running) {
            info("Paused");            
            if(!standalone) {
                System.out.println("paused");
            }
        }
    }

    /**
     * Resume
     */
    public void resume() {
        if(scene.init && !running) {
            info("Resuming...");            
            if(!standalone) {
                System.out.println("resumed");
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isSceneInitialized() {
        return scene != null && scene.init;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        info("Scene reference set");
    }
    
    /**
     * Log
     */
    public void log(String message) {
        long beforeWrite = System.currentTimeMillis();
        
        try(PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(message);
            writer.flush();
        } catch(IOException e) {
            return;
        }
        
        try {
            File file = new File(LOG_FILE);
            file.setLastModified(beforeWrite - 5000);
        } catch (Exception e) {
            return;
        }
        
        if(initializationComplete && !standalone) {
            System.out.println(message);
        }
    }
    
    /**
     * Info
     */
    private void info(String message) {
        long beforeWrite = System.currentTimeMillis();
        
        try(PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(message);
            writer.flush();
        } catch (IOException e) {
            return;
        }
        
        try {
            File file = new File(LOG_FILE);
            file.setLastModified(beforeWrite - 5000);
        } catch (Exception e) {
            return;
        }
    }
    
    /**
     * Important
     */
    private void important(String message) {
        long beforeWrite = System.currentTimeMillis();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(message);
            writer.flush();
        } catch (IOException err) {
            return;
        }
        
        try {
            File file = new File(LOG_FILE);
            file.setLastModified(beforeWrite - 5000);
        } catch (Exception e) {
            return;
        }
        
        if(!standalone) {
            System.out.println(message);
        }
    }

    /**
     * Handle Action
     */
    public void handleAction(String action) {
        info("Received action: " + action);
        switch (action) {
            case "start":
                start();
                break;
            case "continue":
                resume();
                break;
            case "pause":
                pause();
                break;
            case "settings":
                info("Settings");
                break;
            case "exit":
                info("Exiting...");
                break;
            default:
                info("Unknown action: " + action);
        }
    }
    
    public void cleanup() {
        if(!standalone) {
            important("Shutting down...");
        }
        stopLogWatcher();
    }

    public static void main(String[] args) {
        Console console = Console.getInstance();
        console.run();
    }
}