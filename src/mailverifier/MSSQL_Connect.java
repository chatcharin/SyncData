/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailverifier;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Aek
 */
public class MSSQL_Connect {

    int tableNum = 7;
    //int[] lastCount = new int[tableNum];
    Timestamp[] lastTime = new Timestamp[tableNum];
    
    //table array
    ArrayList<String[]>[] tables = new ArrayList[tableNum];
    
    //MSSQL
    Connection con;
    //PostgreSQL
    Connection pcon;
    
    //Message
    public String msg = "DataSynchro started";
    public boolean error = false;
    
    public MSSQL_Connect(){
        
        newTables();
        
        //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
        //con = DriverManager.getConnection("jdbc:sqlserver://203.154.232.7:1433;databaseName=Formula1","sa","P@ssword");
        getMSSQL();
        getPostgreSQL();
        //getColumn();
    }
    
    public void newTables(){
        //new tables
        tables[0] = new ArrayList<String[]>();
        tables[1] = new ArrayList<String[]>();
        tables[2] = new ArrayList<String[]>(); 
        tables[3] = new ArrayList<String[]>();
        tables[4] = new ArrayList<String[]>();
        tables[5] = new ArrayList<String[]>();
        tables[6] = new ArrayList<String[]>();
    }
    
    public void getMSSQL(){
        //MSSQL
        msg = "Connecting to \nMicrosoft SQL server"; 
        try {
             Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
             con =DriverManager.getConnection("jdbc:sqlserver://203.154.232.7:1433;databaseName=Formula1","sa","P@ssword");
         } catch (Exception e) {
            System.out.println(e);
            msg = "Connection lost !";
            error = true;
         }
    }
    
    public void getPostgreSQL(){
        //PostgreSQL
         msg = "Connecting to \nPostgreSQL server"; 
         try {
             Class.forName("org.postgresql.Driver").newInstance();
             pcon = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/formula","postgres","admin");
         } catch (Exception e) {
            System.out.println(e);
            msg = "Connection lost !";
            error = true;
         }
    }
    
    public void checkUpdate(){
        try{
            /*int[] currNum = new int[tableNum];
            for(int i=0;i<tables.length;i++){
                currNum[i] = countRecord(i);
            }
            //currNum[0] = countRecord(0);lastUpdated(0,10);postgresQuery(0);Thread.sleep(10000);
            
            for(int i=0;i<tableNum;i++){
                if(lastCount[i]<currNum[i]){
                    lastUpdated(i,currNum[i]-lastCount[i]);
                    postgresQuery(i);
                    lastCount[i] = currNum[i];

                    Thread.sleep(10000);
                }
            }*/
            for(int i=0;i<tableNum;i++){
                checkModified(i);
            }
            newTables();
            System.gc();
            
        }catch(Exception e){ 
            System.out.println(e.getMessage()); 
            msg = "Fatal error !";
            error = true;
        }
    }
    
    /*public void lastUpdated(int num,int top){
        try{
            //queries
            String[] queries = new String[]{" FCSKID,FCCODE,FCSNAME,FCPDGRP FROM dbo.PROD ORDER BY FTLASTUPD DESC;",
            " FCPROD,FNQTY,FNPRICE FROM dbo.REFPROD ORDER BY FTLASTUPD DESC;",
            " FCSKID,FCNAME FROM dbo.PDGRP ORDER BY FTLASTUPD DESC;",
            " * FROM dbo.GLREF ORDER BY FTLASTUPD DESC;",
            " * FROM dbo.EMPL ORDER BY FTLASTUPD DESC;",
            " * FROM dbo.COOR ORDER BY FTLASTUPD DESC;",
            " * FROM dbo.UM ORDER BY FTLASTUPD DESC;"};    

              try {
                Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery("SELECT TOP "+top+queries[num]);
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int column = rsMetaData.getColumnCount();
                while(rs.next()){
                    String[] values = new String[column];
                    for(int i=0;i<column;i++){
                        values[i] = rs.getString(i+1).trim();
                    }
                    tables[num].add(values);
                    System.out.println(rs.getString(1).trim()+" , "+rs.getString(2).trim()+" , "+rs.getString(3).trim()+" , "+rs.getString(4).trim());
                }

                rs.close();
                stmt.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }catch(Exception ex){
            getMSSQL();
            lastUpdated(num,top);
        }
    }*/
    
    /*public void initCountRecord(){
        for(int i=0;i<tableNum;i++){
            lastCount[i] = countRecord(i);
        }
    }*/
    
    public void initTime(){
        for(int i=0;i<tableNum;i++){
            lastTime[i] = getLastTime(i);
        }
    }
    
    public Timestamp getLastTime(int num){
        Timestamp time = null; 
        String[] tableNames = new String[]{"PROD","REFPROD","PDGRP","GLREF","EMPL","COOR","UM"};
        try {
            Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT TOP 1 FTLASTUPD FROM dbo."+tableNames[num]+" ORDER BY FTLASTUPD DESC;");
            if(rs.next()){
                time = rs.getTimestamp(1);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println(e);
            msg = "Connection lost !";
            error = true;
        }
        return time;
    }
    
    public void checkModified(int num){
        msg = "Synchronizing . . .";
        //queries
        String[] queries = new String[]{" FCSKID,FCCODE,FCSNAME,FCPDGRP FROM dbo.PROD WHERE FTLASTUPD>CONVERT(datetime,'"+lastTime[num].toString()+"') ORDER BY FTLASTUPD DESC;",
        " FCPROD,FNQTY,FNPRICE FROM dbo.REFPROD WHERE FTLASTUPD>CONVERT(datetime,'"+lastTime[num]+"') ORDER BY FTLASTUPD DESC;",
        " FCSKID,FCNAME FROM dbo.PDGRP WHERE FTLASTUPD>CONVERT(datetime,'"+lastTime[num]+"') ORDER BY FTLASTUPD DESC;",
        " * FROM dbo.GLREF WHERE FTLASTUPD>CONVERT(datetime,'"+lastTime[num]+"') ORDER BY FTLASTUPD DESC;",
        " * FROM dbo.EMPL WHERE FTLASTUPD>CONVERT(datetime,'"+lastTime[num]+"') ORDER BY FTLASTUPD DESC;",
        " * FROM dbo.COOR WHERE FTLASTUPD>CONVERT(datetime,'"+lastTime[num]+"') ORDER BY FTLASTUPD DESC;",
        " * FROM dbo.UM WHERE FTLASTUPD>CONVERT(datetime,'"+lastTime[num]+"') ORDER BY FTLASTUPD DESC;"};    

          try {
            Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT "+queries[num]);
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int column = rsMetaData.getColumnCount();
            while(rs.next()){

                String[] values = new String[column];
                for(int i=0;i<column;i++){
                    values[i] = rs.getString(i+1).trim();
                }
                tables[num].add(values);
            }
            rs.close();
            stmt.close();

            //send data
            if(tables[num].size()>0){
                //new lastTime
                lastTime[num] = getLastTime(num);
                postgresQuery(num);
            }

        } catch (Exception e) {
            System.out.println(e);
            msg = "Connection lost !";
            error = true;
        }
    }
    
    /*public int countRecord(int num){
        try{
            int count = 0;
            String[] tableNames = new String[]{"PROD","REFPROD","PDGRP","GLREF","EMPL","COOR","UM"};
            try {
                Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery("select count(FCSKID) from dbo."+tableNames[num]+";");
                if(rs.next()){
                    count = Integer.valueOf(rs.getString(1));
                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            return count;
        }catch(Exception ex){
            getMSSQL();
            countRecord(num);
        }
        return 0;
    }*/
    
    public void postgresQuery(int num){
        msg = "Updating . . .";
        for(int i=0;i<tables[num].size();i++){
            
            //queries
            String[] queries = new String[]{"INSERT INTO \"PROD\"(\"FCSKID\",\"FCCODE\",\"FCSNAME\",\"FCPDGRP\") VALUES('",
            "INSERT INTO \"REFPROD\"(\"FCPROD\",\"FNQTY\",\"FNPRICE\") VALUES('",
            "INSERT INTO \"PDGRP\"(\"FCSKID\",\"FCNAME\") VALUES('",
            "INSERT INTO \"GLREF\"(\"FCUDATE\",\"FCDATASER\",\"FCSKID\",\"FCLUPDAPP\",\"FCRFTYPE\",\"FCREFTYPE\",\"FCCORP\",\"FCBRANCH\",\"FCDEPT\",\"FCSECT\",\"FCJOB\",\"FCSTAT\",\"FCSTEP\",\"FCACSTEP\",\"FCGLHEAD\",\"FCGLHEADCA\",\"FDDATE\",\"FDRECEDATE\",\"FDDUEDATE\",\"FDLAYDATE\",\"FDDEBTDATE\",\"FCDEBTCODE\",\"FCBOOK\",\"FCCODE\",\"FCREFNO\",\"FNDISCAMTI\",\"FNDISCAMT1\",\"FNDISCAMT2\",\"FNDISCAMTA\",\"FNDISCPCN1\",\"FNDISCPCN2\",\"FNDISCPCN3\",\"FNAMT\",\"FNPAYAMT\",\"FNSPAYAMT\",\"FNBEFOAMT\",\"FCVATISOUT\",\"FCVATTYPE\",\"FNVATRATE\",\"FNVATAMT\",\"FNAMT2\",\"FNAMTNOVAT\",\"FCCOOR\",\"FCEMPL\",\"FNCREDTERM\",\"FCISCASH\",\"FCHASRET\",\"FCVATDUE\",\"FNDEBTZERO\",\"FCLAYH\",\"FCLAYSEQ\",\"FCFRWHOUSE\",\"FCTOWHOUSE\",\"FCCREATEBY\",\"FCCORRECTB\",\"FCCANCELBY\",\"FMMEMDATA\",\"FCSEQ\",\"FCPROMOTE\",\"FCVATCOOR\",\"FCRECVMAN\",\"FCSTEPX1\",\"FCSTEPX2\",\"FCCREATETY\",\"FCEAFTERR\",\"FCATSTEP\",\"FNVATPORT\",\"FNVATPORTA\",\"FCMACHINE\",\"FCPERIODS\",\"FCSELTAG\",\"FTDATETIME\",\"FIMILLISEC\",\"FCUTIME\",\"FCCASHIER\",\"FCTXQCBRAN\",\"FCTXQCWHOU\",\"FCTXQCBOOK\",\"FCTXQCGLRE\",\"FCTXBRANCH\",\"FCTXWHOUSE\",\"FCTXBOOK\",\"FCAPPROVEB\",\"FDFULLFILL\",\"FCPROJ\",\"FCDISCSTR\",\"FCCURRENCY\",\"FNXRATE\",\"FNAMTKE\",\"FNVATAMTKE\",\"FNPAYAMTKE\",\"FNDISCAMTK\",\"FNBPAYINV\",\"FCVATSEQ\",\"FCTIMESTAM\",\"FCTRANWHY\",\"FCDELICOOR\",\"FCSUBBR\",\"FCMORDERH\",\"FCPLANT\",\"FTLASTUPD\",\"FCXFERSTEP\",\"FTXFER\",\"FNSTOCKUPD\",\"FCDOCOWNER\",\"FCREFDO\",\"FCINVIA\",\"FCOUTVIA\",\"FCVLDCTRL\",\"FCPCONTRAC\",\"FCQCMO\",\"FCQCMOBK\",\"FCHASCHAIN\",\"FMMEMDATA2\",\"FMMEMDATA3\",\"FMMEMDATA4\",\"FCSHOWPLED\",\"FNPPAYAMT\",\"FNPPAYAMTK\",\"FDVATDATE\",\"FNLOCKED\",\"FNRETENAMT\",\"FMMEMDATA5\",\"FCTXID\",\"FCSUBTXID\",\"FCTRADETRM\",\"FCDELIEMPL\",\"FCLAYMETHD\",\"FCCONSIGN\",\"FCSTEPD\",\"FTRECEIVE\",\"FCLID\",\"FTLASTEDIT\",\"FCPAYTERM\",\"FCU1STATUS\",\"FCU2STATUS\",\"FCDTYPE1\",\"FCDTYPE2\",\"FNDEPAMT\",\"FNDEPAMTKE\",\"FNBEFDEP\",\"FNBEFDEPKE\",\"FNU1CNT\",\"FNU2CNT\",\"FMERRMSG\",\"FCCREATEAP\",\"FNEXPAMT\",\"FCPERSON\",\"FCLINKAPP1\",\"FCLINKSTP1\",\"FCLINKAPP2\",\"FCLINKSTP2\",\"FCCOUNTER\",\"FCPDBRAND\",\"FNCRXRATE\",\"FDAPPROVE\",\"FMMEMDATA6\",\"FMMEMDATA7\",\"FMMEMDATA8\",\"FMMEMDATA9\",\"FMMEMDATAA\",\"FNAFTDEP\",\"FNAFTDEPKE\",\"FCCHKDT\",\"FCRECALBY\",\"FTLASRECAL\",\"FCMNMGLH\",\"FCMNMGLHCA\") VALUES('",
            "INSERT INTO \"EMPL\"(\"FCDATASER\",\"FCSKID\",\"FCUDATE\",\"FCUTIME\",\"FCLUPDAPP\",\"FCBAKYRHIS\",\"FCCORP\",\"FCTYPE\",\"FCBRANCH\",\"FCSECT\",\"FCEMZONE\",\"FCCODE\",\"FCNAME\",\"FCNAME2\",\"FCSNAME\",\"FCSNAME2\",\"FCADDR1\",\"FCADDR12\",\"FCADDR2\",\"FCADDR22\",\"FCZIP\",\"FCTEL\",\"FCFAX\",\"FNCOMMISSI\",\"FCREMARK\",\"FCREMARK2\",\"FDEXPIREDA\",\"FCRCODE\",\"FCCREATETY\",\"FCEAFTERR\",\"FCSELTAG\",\"FTDATETIME\",\"FIMILLISEC\",\"FCFCHR\",\"FCFCHRS\",\"FCPICTURE\",\"FCEMPL\",\"FCMOBILE\",\"FCISFMUSER\",\"FTLASTUPD\",\"FDBEGDATE\",\"FCDRIVENO\",\"FCDTYPE\",\"FDPERMIT\",\"FDEXPIRE\",\"FDBIRTH\",\"FCIDCARD\",\"FCPERMITBY\",\"FCDRVADDR1\",\"FCDRVADDR2\",\"FMHISTORYD\",\"FTLASTEDIT\",\"FCJOB\",\"FCEMPLTYPE\",\"FCCREATEAP\",\"FCEMAIL\",\"FMSIGNATUR\",\"FCMANFLAG\",\"FCADDAPVBY\",\"FCEDTAPVBY\",\"FCDELAPVBY\",\"FCISUSED\") VALUES('",
            "INSERT INTO \"COOR\"(\"FCSELTAG\",\"FCCODE\",\"FCNAME\",\"FCDATASER\",\"FCSKID\",\"FCUDATE\",\"FCUTIME\",\"FCLUPDAPP\",\"FCBAKYRHIS\",\"FCISCUST\",\"FCISSUPP\",\"FCCORP\",\"FCCRGRP\",\"FCCRZONE\",\"FCEMPL\",\"FCSNAME\",\"FCSNAME2\",\"FCNAME2\",\"FNCREDTERM\",\"FNCREDLIM\",\"FCZIP\",\"FNPAYFIFO\",\"FCGRADE\",\"FCPRICENO\",\"FCVATTYPE\",\"FNDISCPCN\",\"FCACCHART\",\"FCPERSONTY\",\"FMMEMDATA\",\"FCVATCOOR\",\"FCCOLCOOR\",\"FCPOLICYPR\",\"FCPOLICYDI\",\"FCCREATETY\",\"FCEAFTERR\",\"FCCONTACTN\",\"FTDATETIME\",\"FIMILLISEC\",\"FCFCHR\",\"FCFCHRS\",\"FNBLACK\",\"FCCONTPOS\",\"FCDELICOOR\",\"FMMEMDATA2\",\"FMMEMDATA3\",\"FMMEMDATA4\",\"FCBACCMTH\",\"FCCURRENCY\",\"FMMAPPICT\",\"FTLASTUPD\",\"FDFSTCONT\",\"FCSTATUS\",\"FDINACTIVE\",\"FCPAYTERM\",\"FCDISCSTR\",\"FCDEBTAAC\",\"FCBDEBTAAC\",\"FCPAYGRP\",\"FCBANK\",\"FCBBRANCH\",\"FCBANKNO\",\"FCWHOUSE\",\"FCEMAIL\",\"FCLAYMETHD\",\"FCCHQMETHD\",\"FMMAPNAME\",\"FCTRADETRM\",\"FCDELIEMPL\",\"FCLAYEMPL\",\"FMMEMDATA5\",\"FCTEL\",\"FCDCHANNEL\",\"FMPICSHMK\",\"FNLAT\",\"FNLONG\",\"FNLTDPO\",\"FCLID\",\"FTLASTEDIT\",\"FCCOORPGRP\",\"FCU1STATUS\",\"FCU2STATUS\",\"FCDTYPE1\",\"FCDTYPE2\",\"FNU1CNT\",\"FNU2CNT\",\"FMERRMSG\",\"FCCREATEAP\",\"FCIS1SHIP\",\"FCROUTEDEL\",\"FCLINKAPP1\",\"FCLINKSTP1\",\"FCLINKAPP2\",\"FCLINKSTP2\",\"FCCOLSEND\",\"FCBKBRCODE\",\"FCPYMETHOD\",\"FCCHGBEAR\",\"FCADVICEBY\",\"FCMANFLAG\",\"FCADDAPVBY\",\"FCEDTAPVBY\",\"FCDELAPVBY\",\"FCISUSED\") VALUES('",
            "INSERT INTO \"UM\"(\"FCDATASER\",\"FCSKID\",\"FCUDATE\",\"FCUTIME\",\"FCLUPDAPP\",\"FCBAKYRHIS\",\"FCSTAT\",\"FCCORP\",\"FCCODE\",\"FCNAME\",\"FCNAME2\",\"FCTYPE\",\"FCCREATETY\",\"FCEAFTERR\",\"FCSELTAG\",\"FTDATETIME\",\"FIMILLISEC\",\"FCFCHR\",\"FTLASTUPD\",\"FCLID\",\"FTLASTEDIT\",\"FCU1STATUS\",\"FCU2STATUS\",\"FCDTYPE1\",\"FCDTYPE2\",\"FNU1CNT\",\"FNU2CNT\",\"FMERRMSG\",\"FCCREATEAP\",\"FCCOLSEND\",\"FCMANFLAG\",\"FCADDAPVBY\",\"FCEDTAPVBY\",\"FCDELAPVBY\",\"FCISUSED\") VALUES('"};
            
            if(num>2){
                queries[num] += tables[num].get(i)[0]+"'";
                for(int j=1;j<tables[num].get(i).length;j++){
                    queries[num] += ",'"+tables[num].get(i)[j]+"'";
                }
                queries[num] += ");";
            }else if(num==0){
                queries[num] += tables[0].get(i)[0]+"','"+tables[0].get(i)[1]+"','"+tables[0].get(i)[2]+"','"+tables[0].get(i)[3]+"');";
            }else if(num==1){
                queries[num] += tables[1].get(i)[0]+"','"+tables[1].get(i)[1]+"','"+tables[1].get(i)[2]+"');";
            }else if(num==2){
                queries[num] += tables[2].get(i)[0]+"','"+tables[2].get(i)[1]+"');";
            }
            System.out.println(queries[num]);
            
            try {
                Statement stmt = pcon.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
                stmt.executeUpdate(queries[num]);
                
                stmt.close();
            } catch (Exception e) {
                System.out.println(e);
                msg = "Connection lost !";
                error = true;
            }
        }
    }
    
    /*void getColumn(){
        //queries
        String[] queries = new String[]{"SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'GLREF';",
        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'EMPL';",
        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'COOR';",
        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'UM';"};
            
        for(int i=0;i<4;i++){
            try {
                Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(queries[i]);

                String line = "(";
                rs.next();
                line += "\""+rs.getString(1)+"\"";
                while(rs.next()){
                    line += ",\""+rs.getString(1)+"\"";
                }
                System.out.println(line+")");
                System.out.println("-------------------------------------------");
                
                rs.close();
                stmt.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }*/
}