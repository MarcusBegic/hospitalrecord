import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.util.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class server implements Runnable {
    private ServerSocket serverSocket = null;
    private static int numConnectedClients = 0;
    private HashMap<String, Integer> clientPrivelege;
    private HashMap<String, String> serialToName;
    private HashMap<String, String> nameToSerial;
    private HashMap<String, String> getWing;
    private HashMap<String, Integer> recordCount;
    private HashMap<String, List<String>> patientMap;
    private final String marcus = "111428791329258712977551130365341285530045495356";
    private final String joel = "111428791329258712977551130365341285530045495358"; 
    private final String william = "111428791329258712977551130365341285530045495357";
    private final String joy = "111428791329258712977551130365341285530045495359";
    private final String fredrik = "111428791329258712977551130365341285530045495361";
    private final String philip ="111428791329258712977551130365341285530045495360";
    private final String nsa = "111428791329258712977551130365341285530045495362";
    private HashMap<String,Set<String>> patientRecords;

    public server(ServerSocket ss) throws IOException {
        serverSocket = ss;
        newListener();
        clientPrivelege = new HashMap<String, Integer>(); 
        clientPrivelege.put(marcus, 3); //marcus patient
        clientPrivelege.put(joel, 3); //joel patient
        clientPrivelege.put(william, 2); //william nurse
        clientPrivelege.put(joy, 2); //joy nurse
        clientPrivelege.put(fredrik, 1); //fredrik doc
        clientPrivelege.put(philip,1); //philip doc
        clientPrivelege.put(nsa, 0); //nsa gov
        serialToName = new HashMap<String,String>();
        serialToName.put("111428791329258712977551130365341285530045495356","marcus");
        serialToName.put("111428791329258712977551130365341285530045495358","joel");
        serialToName.put("111428791329258712977551130365341285530045495357", "william");
        serialToName.put("111428791329258712977551130365341285530045495359", "joy");
        serialToName.put("111428791329258712977551130365341285530045495361", "fredrik");
        serialToName.put("111428791329258712977551130365341285530045495360", "philip");
        nameToSerial = new HashMap<String,String>();
        nameToSerial.put("marcus","111428791329258712977551130365341285530045495356");
        nameToSerial.put("joel","111428791329258712977551130365341285530045495358");
        nameToSerial.put("william","111428791329258712977551130365341285530045495357");
        nameToSerial.put("joy","111428791329258712977551130365341285530045495359");
        nameToSerial.put("fredrik","111428791329258712977551130365341285530045495361");
        nameToSerial.put("philip","111428791329258712977551130365341285530045495360");
        getWing = new HashMap<String,String>();
        getWing.put(marcus, "southward");
        getWing.put(joel, "northward");
        recordCount = new HashMap<String, Integer>();
        recordCount.put(marcus, 1);
        recordCount.put(joel, 1);
        patientRecords = new HashMap<String,Set<String>>();
        patientRecords.put(marcus, new HashSet<String>());
        patientRecords.put(joel, new HashSet<String>());
        patientMap = new HashMap<String, List<String>>(); 
        ArrayList<String> philipPatients = new ArrayList();
        philipPatients.add(marcus);
        ArrayList<String> williamPatients = new ArrayList();
        williamPatients.add(marcus);
        ArrayList<String> joyPatients = new ArrayList();
        joyPatients.add(joel);
        ArrayList<String> fredrikPatients = new ArrayList();
        fredrikPatients.add(joel);
        patientMap.put(philip, philipPatients);
        patientMap.put(fredrik, fredrikPatients);
        patientMap.put(joy, joyPatients);
        patientMap.put(william, williamPatients);
    }

    private String accessTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date).toString() + ": ";
    }
     
    private String queryPatient(String worker,PrintWriter out, BufferedReader in) {
        out.println("Which patient?");
        out.flush();
        List<String> patientList = patientMap.get(worker);
        String res = "";
        for(String pat : patientList){
            res = res + serialToName.get(pat) + " ";
        }
        out.println(res);
        out.flush();
        String chosenPatient = "";
        try{
        chosenPatient = nameToSerial.get(in.readLine());
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
        return chosenPatient;
    }
    
    private String queryRecord(String patient,PrintWriter out, BufferedReader in) {
        out.println("Which record?");
        out.flush();
        Set<String> recordSet = patientRecords.get(patient);
        Iterator<String> recordSetIt = recordSet.iterator();
        String res = "";
        while(recordSetIt.hasNext()){
            res = res + recordSetIt.next() + " ";
        }
        out.println(res);
        out.flush();
        String record = "";
        try{
        record = in.readLine();
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
        return record;
    }
    
    private void scanRecords(String path, String ward, String patient){
    File folder = new File(path + "/"+ward);
    File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
              patientRecords.get(patient).add(listOfFiles[i].getName());
            }
        }
    }

    public void run() {
        try {
            SSLSocket socket=(SSLSocket)serverSocket.accept();
            newListener();
            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String serialNum = cert.getSerialNumber().toString();
    	    numConnectedClients++;
            System.out.println("client connected");
            PrintWriter out = null;
            BufferedReader in = null;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String partialPath = "/home/marcus/hospitalrecord/records/";
            String patient;
            String logpath = "/home/marcus/hospitalrecord/log";
            scanRecords(partialPath,"southward",marcus);
            scanRecords(partialPath,"northward",joel);

            if(clientPrivelege.containsKey(serialNum)){
                switch(clientPrivelege.get(serialNum)){
                    case 3: //patient
                        while(true){
                            FileWriter logwriter = new FileWriter(logpath,true);
                            out.println("init");
                            out.flush();
                            String input = in.readLine();
                            if(input.equals("1")){
                            patient = serialToName.get(serialNum);
                            String path;
                            String record = queryRecord(serialNum,out,in);
                            path = partialPath + "/" + getWing.get(serialNum) + "/" + record; 
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            String output = "";
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                output = output + line + ". ";
                            }
                            out.println(output);
                            out.flush();
                            logwriter.write("\n" + accessTime() + serialToName.get(serialNum) + " " + "read his own record");
                            logwriter.close();
                            }
                            else if(input.equals("4")){
                                break;
                            }
                            else{
                                out.println("Access denied");
                                out.flush();
                                logwriter.write("\n" + accessTime() + serialToName.get(serialNum) + "tried to access unauthorized content");
                                logwriter.close();
                            } 
                        }
                        break;
                    case 2: //nurse
                        while(true){
                        FileWriter logwriter = new FileWriter(logpath,true);
                        out.println("init");
                        out.flush();
                        String input = in.readLine();
                        patient = queryPatient(serialNum, out,in);
                        if(input.equals("2")){
                            String fileName = queryRecord(patient, out, in);
                            String path = partialPath + "/" + getWing.get(patient) + "/" + fileName;
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            String output = "";
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                output = output + line + ". ";
                            }
                            out.println(output);
                            out.flush();
                            logwriter.write( "\n" +accessTime() + serialToName.get(serialNum) + " read " + serialToName.get(patient) + " files");
                            logwriter.close();
                        }
                        else if(input.equals("1")){
                                out.println("Approved access");
                                out.flush();
                                out.println("Please enter what you want to put in the record");
                                out.flush();
                                input = in.readLine(); 
                                patient = queryPatient(serialNum, out,in);
                                String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient); 
                                FileWriter writer = new FileWriter(path, true);
                                writer.write("; " +input);
                                writer.close();
                                logwriter.write("\n" + accessTime()  + serialToName.get(serialNum) + " wrote to "+ serialToName.get(patient) + " file"); 
                                logwriter.close();
                                out.println("Wrote successfully to record");
                                out.flush();
                        } else if(input.equals("4")){
                            break;
                        }
                        else {
                            out.println("Access denied");
                            out.flush();
                            logwriter.write( "\n" + accessTime() + serialToName.get(serialNum) + " tried to write to " + serialToName.get(patient) + " files, unauthorized");
                            logwriter.close();
                            }
                        }
                        break;
                    case 1: //doctors
                        while(true){
                        FileWriter logwriter = new FileWriter(logpath,true);
                        out.println("init");
                        out.flush();
                        String input = in.readLine();
                        patient = queryPatient(serialNum,out,in);
                        if(input.equals("3")){
                            out.println("Options: " );
                            out.flush();
                            out.println("1. Write to record"  + " 2.Create new record");
                            out.flush();
                            String answer = in.readLine(); 
                            if(answer.equals("2")){
                                out.println("Approved access");
                                out.flush();
                                out.println("What nurse do you want to be able to see this record?");
                                out.flush();
                                String nurse = in.readLine();
                                nurse = nameToSerial.get(nurse);
                                patientMap.get(nurse).add(patient);
                                recordCount.put(patient, recordCount.get(patient)+1);
                                String newFileName = serialToName.get(patient) + recordCount.get(patient).toString();
                                String path = partialPath + "/" + getWing.get(patient) + "/" + newFileName + recordCount.get(patient);
                                File newRecord = new File(path);
                                newRecord.createNewFile();
                                logwriter.write("\n" + accessTime() +serialToName.get(serialNum) +" created a new record for " + serialToName.get(patient)); 
                                logwriter.close();
                                patientRecords.get(patient).add(newFileName+recordCount.get(patient));
                                out.println("Created a new record");
                                out.flush();
                            }
                            if(answer.equals("1")){
                                String record = queryRecord(patient,out,in);
                                out.println("Approved access");
                                out.flush();
                                out.println("Please enter what you want to put in the record");
                                out.flush();
                                input = in.readLine();
                                String path = partialPath + "/" + getWing.get(patient) + "/" + record;
                                FileWriter writer = new FileWriter(path, true);
                                writer.write("; "+ input);
                                writer.close();
                                logwriter.write("\n" + accessTime()  + serialToName.get(serialNum) + " wrote to "+ serialToName.get(patient) + " file"); 
                                logwriter.close();
                                out.println("Wrote successfully to record");
                                out.flush();
                            }
                        } else if(input.equals("4")){
                           break; 
                        }else if(input.equals("2")){
                            String record = queryRecord(patient,out,in);
                            String path = partialPath + "/" + getWing.get(patient) + "/" + record;
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            String output = "";
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                output = output + line + ". ";
                            }
                            out.println(output);
                            out.flush();
                            logwriter.write("\n" + accessTime()  + "doctor read" + serialToName.get(patient) + " file"); 
                            logwriter.close();
                            }
                        else{
                            out.println("please select a correct option");
                            out.flush();
                            }
                        }
                        break;
                    case 0: { //NSA
                        while(true){
                        FileWriter logwriter = new FileWriter(logpath,true);
                        out.println("nsainit");
                        out.flush();
                        String indata = in.readLine();
                        if (indata.equals("2")){
                            break;
                        } 
                        out.println("Chose a patient:" );
                        out.flush();
                        out.println( " 1. Marcus" + " 2. Joel");
                        out.flush();
                        String numpatient = in.readLine();
                        patient = null;
                        if(numpatient.equals("1")){
                            patient = marcus;
                        }
                        
                        if(numpatient.equals("2")){
                            patient = joel;
                        }
                        if(indata.equals("1")){
                            out.println("Options: " );
                            out.flush();
                            out.println("1. Read record"  + " 2. Delete record");
                            out.flush();
                            String answer = in.readLine(); 
                            if(answer.equals("2")){
                                String recordName = queryRecord(patient, out, in);
                                String path = partialPath + "/" + getWing.get(patient) + "/" + recordName;
                                File record = new File(path);
                                record.delete();
                                logwriter.write("\n"  + accessTime() + "nsa deleted" + serialToName.get(patient) + "'s record"); 
                                logwriter.close();
                                patientRecords.get(patient).remove(recordName);
                                out.println("Successfully deleted record");
                                out.flush();
                                }
                            if(answer.equals("1")){
                                String recordName = queryRecord(patient, out, in);
                                String path = partialPath + "/" + getWing.get(patient) + "/" + recordName;
                                File patientFile = new File(path);
                                Scanner scanner = new Scanner(patientFile);
                                String output = "";
                                while(scanner.hasNext()){
                                    String line = scanner.nextLine();
                                    output = output + line + ". ";
                                }
                                out.println(output);
                                out.flush();
                                logwriter.write("nsa read" + serialToName.get(patient) + " file"); 
                                logwriter.close();
                            }
                        } 
                        }
                        break;
                        
                    }
                    default:
                            System.out.println("lol");
                        break;
                }
            }else{
                out.println("Unrecognized client, disconnecting");
                out.flush();
            }
			in.close();
			out.close();
			socket.close();
    	    numConnectedClients--;
            System.out.println("client disconnected");
            System.out.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (IOException e) {
            System.out.println("Client died: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    private void newListener() { (new Thread(this)).start(); } // calls run()

    public static void main(String args[]) {
        System.out.println("\nServer Started\n");
        int port = -1;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        String type = "TLS";
        try {
            ServerSocketFactory ssf = getServerSocketFactory(type);
            ServerSocket ss = ssf.createServerSocket(port);
            ((SSLServerSocket)ss).setNeedClientAuth(true); // enables client authentication
            new server(ss);
        } catch (IOException e) {
            System.out.println("Unable to start Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ServerSocketFactory getServerSocketFactory(String type) {
        if (type.equals("TLS")) {
            SSLServerSocketFactory ssf = null;
            try { // set up key manager to perform server authentication
                SSLContext ctx = SSLContext.getInstance("TLS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
                char[] password = "server".toCharArray();
                ks.load(new FileInputStream("/home/marcus/hospitalrecord/server/serverkeystore"), password);  // keystore password (storepass)
                ts.load(new FileInputStream("/home/marcus/hospitalrecord/server/servertruststore"), password); // truststore password (storepass)
                kmf.init(ks, password); // certificate password (keypass)
                tmf.init(ts);  // possible to use keystore as truststore here
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }
}
