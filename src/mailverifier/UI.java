
package mailverifier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

    
public class UI extends JPanel implements Runnable{
    
    public MSSQL_Connect mscon;
    private Thread thread;
    private boolean pause = false;
    private Font font;
    
    private int deg = 0;
    
    private JLabel button;
    private Color bColor = Color.WHITE;
    private int sec = 6000;
    
    public UI(MSSQL_Connect m){
        setLayout(null);
	setPreferredSize(new Dimension(500,200));
        
        mscon = m;
        font = new Font("Segoe UI Light",0,24);
        
        button = new JLabel();
        button.setBounds(380, 45, 80, 80);
        button.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){
                bColor = new Color(224,186,224);
            }
            public void mouseExited(MouseEvent e){
                bColor = Color.WHITE;
            }
        });
        add(button);
        
        thread = new Thread(this);
        thread.start();
    }
    
    public void paint(Graphics g){
        //graphic synchronized
        Toolkit.getDefaultToolkit().sync();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(154,50,156));
        g2d.fillRect(0, 0, 500, 200);
        
        printString(g2d,font,mscon.msg,170,90,30,Color.WHITE);
        
        if(button.isEnabled()){
            g2d.setColor(bColor);
            g2d.fill(button.getBounds());
            printString(g2d,font.deriveFont(13f),"Program will\nterminated in\n"+(sec/1000)+" seconds",button.getX()+4,button.getY()+14,13,new Color(154,50,156));
            printString(g2d,font.deriveFont(30f),":(",button.getX()+4,button.getY()+75,0,new Color(154,50,156));
        }
        
        g2d.setColor(Color.WHITE);
        g2d.rotate(Math.toRadians(deg),110,85);
        g2d.fillOval(80, 70, 6, 6);
        g2d.rotate(Math.toRadians(deg+1),110,85);
        g2d.fillOval(80, 70, 6, 6);
        g2d.rotate(Math.toRadians(deg+2),110,85);
        g2d.fillOval(80, 70, 5, 5);
        g2d.rotate(Math.toRadians(deg+3),110,85);
        g2d.fillOval(80, 70, 5, 5);
        g2d.rotate(Math.toRadians(deg+4),110,85);
        g2d.fillOval(80, 70, 4, 4);
    }
    
    public void printString(Graphics2D g2d,Font font,String msg,int x,int y,int z,Color color){
        g2d.setColor(color);
        g2d.setFont(font);
        
        String[] lines = msg.split("\n");
        for(int i=0;i<lines.length;i++){
            g2d.drawString(lines[i], x, y+(i*z));
        }
    }
    
    public void run(){
        while(true){
            try {
                if(deg>360){
                    deg = 0;
                }
                deg+=3;

                //show terminted msg
                if(mscon.error){
                    button.setEnabled(true);
                    sec-=40;
                }else{
                    button.setEnabled(false);
                }
                //end
                if(sec<=0){
                    JOptionPane.showMessageDialog(null, "Error: Program terminated", "DataSynchro", JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                }

                if(!pause){
                    repaint();
                }
                thread.sleep(40);
            } catch (InterruptedException ex) {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void pause(){
        System.gc();
        pause = true;
    }
	
    public void resume(){
        System.gc();
        pause = false;
    }
}
