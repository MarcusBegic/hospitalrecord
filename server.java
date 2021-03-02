import java.io.*;

import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.util.*;
import java.math.BigInteger;

public class server implements Runnable {
    private ServerSocket serverSocket = null;
    private static int numConnectedClients = 0;
    private HashMap<String, Integer> clientPrivelege;
    private HashMap<String, String> patientMap;
    private HashMap<String, String> serialToName;
    private HashMap<String, String> getWing;
    private HashMap<String, Integer> recordCount;

    private final String marcus = "111428791329258712977551130365341285530045495356";
    private final String joel = "111428791329258712977551130365341285530045495358"; 
    private final String william = "111428791329258712977551130365341285530045495357";
    private final String joy = "111428791329258712977551130365341285530045495359";
    private final String fredrik = "111428791329258712977551130365341285530045495361";
    private final String philip ="111428791329258712977551130365341285530045495360";
    private final String nsa = "111428791329258712977551130365341285530045495362";


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
        patientMap = new HashMap<String, String>(); 
        patientMap.put(william,marcus); //william nurse marcus patient
        patientMap.put(joy, joel);//joy nurse joel patient
        patientMap.put(philip, marcus);// doctor philip patient marcus 
        patientMap.put(fredrik, joel);// doctor philip patient joel 
        serialToName = new HashMap<String,String>();
        serialToName.put("111428791329258712977551130365341285530045495356","marcus");
        serialToName.put("111428791329258712977551130365341285530045495358","joel");
        getWing = new HashMap<String,String>();
        getWing.put(marcus, "southward");
        getWing.put(joel, "northward");
        recordCount = new HashMap<String, Integer>();
        recordCount.put(marcus, 1);
        recordCount.put(joel, 1);
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
            FileWriter logwriter = new FileWriter(logpath);


            if(clientPrivelege.containsKey(serialNum)){
                switch(clientPrivelege.get(serialNum)){
                    case 3: //patient
                        while(true){
                        String input = in.readLine();
                            if(input.equals("1")){
                            patient = serialToName.get(serialNum);
                            String path;
                            if(recordCount.get(serialNum)>1){
                                out.println("Which record would you like to read?");
                                out.flush();
                                String answer = in.readLine();
                                path = partialPath + "/" + getWing.get(serialNum) + "/" + patient + answer; 
                            }
                            else{
                            path = partialPath + "/" + getWing.get(serialNum) + "/" + patient; 
                            }
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                out.println(line);
                                out.flush();
                                }
                            logwriter.write(serialToName.get(serialNum) + " " + "read his own record");
                            logwriter.close();
                            }
                            else{
                                out.println("Access denied");
                                out.flush();
                                logwriter.write(serialToName.get(serialNum) + "tried to access unauthorized content");
                                logwriter.close();
                                break;
                            } 
                        }
                        break;
                    case 2: //nurse
                        while(true){
                        String input = in.readLine();
                        if(input.equals("2")){
                            patient = patientMap.get(serialNum);
                            String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient);
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                out.println(line);
                                out.flush();
                            }
                            logwriter.write("Nurse read " + serialToName.get(patient) + " files");
                            logwriter.close();
                        }
                        else if(input.equals("1")){
                            out.println("Faulty choice");
                            out.flush();
                            break;
                        }
                        else {
                            out.println("Access denied");
                            out.flush();
                            break;
                            }
                        }
                        break;
                    case 1: 
                        while(true){
                        String input = in.readLine();
                        if(input.equals("3")){
                            out.println("Options: " + "1. Write to record"  + " 2.Create new record");
                            out.flush();
                            String answer = in.readLine(); 
                            if(answer.equals("2")){
                                patient = patientMap.get(serialNum);
                                String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient) + recordCount.get(patient).toString();
                                recordCount.put(patient, recordCount.get(patient)+1);
                                File newRecord = new File(path);
                                newRecord.createNewFile();
                                logwriter.write("doctor created a new record for " + serialToName.get(patient)); 
                                logwriter.close();
                            }
                            if(answer.equals("1")){
                                out.println(">");
                                out.flush();
                                input = in.readLine();
                                patient = patientMap.get(serialNum);
                                String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient); 
                                FileWriter writer = new FileWriter(path);
                                writer.write(input);
                                writer.close();
                                logwriter.write("doctor wrote to " + serialToName.get(patient) + " file"); 
                                logwriter.close();
                            }
                        } else if(input.equals("2")){
                            patient = patientMap.get(serialNum);
                            String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient);
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                out.println(line);
                                out.flush();
                                }
                                logwriter.write("doctor read" + serialToName.get(patient) + " file"); 
                                logwriter.close();
                            }
                        else{
                            break;
                        }
                        }
                        break;

                    case 0: { //NSA
                        String indata = in.readLine();
                        out.println("Chose a patient: " + " 1. Marcus" + " 2. Joel");
                        out.flush();
                        String numpatient = in.readLine();
                        patient = null;
                        if(numpatient.equals("1")){
                            patient = marcus;
                        }
                        if(numpatient.equals("2")){
                            patient = joel;
                        }
                        if(indata.equals("3")){
                            out.println("Options: " + "1. Write to record"  + " 2.Create new record");
                            out.flush();
                            String answer = in.readLine(); 
                            if(answer.equals("2")){
                                String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient) + recordCount.get(patient).toString();
                                recordCount.put(patient, recordCount.get(patient)+1);
                                File newRecord = new File(path);
                                newRecord.createNewFile();
                                logwriter.write("nsa created a new record for " + serialToName.get(patient)); 
                                logwriter.close();
                            }
                            if(answer.equals("1")){
                                out.println(">");
                                out.flush();
                                indata = in.readLine();
                                String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient); 
                                FileWriter writer = new FileWriter(path);
                                writer.write(indata);
                                writer.close();
                                logwriter.write("doctor wrote to " + serialToName.get(patient) + " file"); 
                                logwriter.close();
                            }

                        } else if(indata.equals("2")){
                            String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient);
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                out.println(line);
                                out.flush();
                            }
                                logwriter.write("nsa read" + serialToName.get(patient) + " file"); 
                                logwriter.close();
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
