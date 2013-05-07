/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccsthread;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import mailverifier.MSSQL_Connect;
import mailverifier.ThSendDB;
import mailverifier.UI;

/**
 *
 * @author Aek
 */
public class CCSThread extends JFrame{
    
    static TrayIcon trayIcon;
    static SystemTray tray;
    
    ThSendDB tsd;
    UI ui;
    
    public CCSThread(){
        setTitle("DataSynchro");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(300, 100, 500, 200);
        setResizable(false);
        
        // SYSTEM TRAY
        if (SystemTray.isSupported()) {
                tray = SystemTray.getSystemTray();
                
                // tray menu
                PopupMenu popup = new PopupMenu();
                MenuItem item1 = new MenuItem("Open");
                ActionListener openListener = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                tray.remove(trayIcon);
                                setVisible(true);
                                setExtendedState(JFrame.NORMAL);
                                ui.resume();
                        }
                };
                item1.addActionListener(openListener);

                MenuItem item2 = new MenuItem("Exit");
                item2.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                System.exit(0);
                        }
                });
                popup.add(item1);
                popup.add(item2);

                // icon
                Image icon = null;
                try {
                        icon = ImageIO.read(getClass().getResourceAsStream("/java.png"));
                } catch (IOException e1) {
                        e1.printStackTrace();
                }
                //new trayIcon
                trayIcon = new TrayIcon(icon, "DataSynchro Tray", popup);
                trayIcon.setImageAutoSize(true);
                
                addWindowStateListener(new WindowStateListener() {
                        public void windowStateChanged(WindowEvent e) {
                                if (e.getNewState() == NORMAL) {
                                        setVisible(true);
                                        ui.resume();
                                }
                        }
                });

                // CLOSE BUTTON
                addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                                try {
                                        System.exit(0);
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                }
                        }
                        public void windowIconified(WindowEvent e){
                                try {
                                        tray.add(trayIcon);
                                        setVisible(false);
                                        ui.pause();
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                }
                        }
                });
        }
        tsd = new ThSendDB("CCS");
        ui = new UI(tsd.mSSQL_Connect);
        add(ui);
        setVisible(true);
        
        tsd.start();
    }
    
    public static void main(String[] args) {
        CCSThread c = new CCSThread();
    }
}
