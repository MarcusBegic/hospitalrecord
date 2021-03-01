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

            if(clientPrivelege.containsKey(serialNum)){
                switch(clientPrivelege.get(serialNum)){
                    case 3: //patient
                        while(true){
                        String input = in.readLine();
                            if(input.equals("1")){
                            String patient = serialToName.get(serialNum);
                            String path = partialPath + "/" + getWing.get(serialNum) + "/" + patient; 
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                out.println(line);
                                out.flush();
                                }
                            }
                            else{
                                out.println("access denied");
                                out.flush();
                                out.println(null);
                                out.flush();
                                break;
                            } 
                        }
                        break;
                    case 2: //nurse
                        while(true){
                        String input = in.readLine();
                        if(input.equals("2")){
                            String patient = patientMap.get(serialNum);
                            String path = partialPath + "/" + getWing.get(patient) + "/" + serialToName.get(patient);
                            File patientFile = new File(path);
                            Scanner scanner = new Scanner(patientFile);
                            while(scanner.hasNext()){
                                String line = scanner.nextLine();
                                out.println(line);
                                out.flush();
                            }
                            out.println("---------------------------------------------------------");
                            out.flush();
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
                    case 1: // doctor
                        break;
                    case 0: //NSA
                        break;
                    default:
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
