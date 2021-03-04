
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.*;
import java.util.*;

public class client {

    private static void initOptions(){
        System.out.println("-------------------------------------------------------");
        System.out.println("Options:");
        System.out.println("1. Read your record?");
        System.out.println("2. Read your patients record?");
        System.out.println("3. Create/write to your patients record?");
        System.out.println("4. Quit");
        System.out.print(">");
        }

    private static void nsaInitOptions(){
        System.out.println("-------------------------------------------------------");
        System.out.println("Secret Options:");
        System.out.println("1. Delete/read to any patients record?");
        System.out.println("2. Quit");
        System.out.print(">");
        }

    public static void main(String[] args) throws Exception {
        String host = null;
        int port = -1;
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
        if (args.length < 2) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        try { /* get input parameters */
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }

        try { /* set up a key manager for client authentication */
            SSLSocketFactory factory = null;
            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore ts = KeyStore.getInstance("JKS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                SSLContext ctx = SSLContext.getInstance("TLS");
                boolean verified=false;
                String username = null;
                char[] password=null;

                while(!verified){
                    try{
                        Console console = System.console();
                        username = console.readLine("Username: ");
                        password = console.readPassword("Password: ");
                        StringBuilder sb = new StringBuilder();
                        sb.append("/home/marcus/hospitalrecord/");
                        sb.append(username);
                        sb.append("/");
                        sb.append(username);
                        StringBuilder sb2 = new StringBuilder(sb);
                        sb.append("keystore");
                        sb2.append("truststore");
                        String keystorePath = sb.toString();
                        String truststorePath = sb2.toString();
                        ks.load(new FileInputStream(keystorePath), password);  // keystore password (storepass)
                        ts.load(new FileInputStream(truststorePath), password); // truststore password (storepass);
                        verified=true;
                    }catch (java.io.IOException e){
                        // System.out.println(e.toString());
                        System.out.println("Incorrect credentials, try again");
                    }
                }

				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                factory = ctx.getSocketFactory();

            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            System.out.println("\nsocket before handshake:\n" + socket + "\n");
            socket.startHandshake();
            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String subject = cert.getSubjectDN().getName();
            System.out.println("certificate name (subject DN field) on certificate received from server:\n" + subject + "\n");
            System.out.println("socket after handshake:\n" + socket + "\n");
            System.out.println("secure connection established\n\n");
            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            String servermsg;
            for (;;) {
                servermsg = in.readLine();
                if(servermsg.equals("init")){
                    initOptions();
                }else if(servermsg.equals("nsainit")){
                    nsaInitOptions();
                }
                else{
                System.out.println(servermsg + "\n");
                }
                System.out.print(">");
                msg = read.readLine();
                if (msg.equalsIgnoreCase("quit")) {
                    break;
                }
                System.out.print("sending '" + msg + "' to server...");
                out.println(msg);
                out.flush();
                System.out.println("done");
                servermsg = in.readLine();
                System.out.println(servermsg);
            
            }
            in.close();
			out.close();
			read.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
