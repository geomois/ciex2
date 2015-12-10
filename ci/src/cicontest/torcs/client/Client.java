/*     */ package cicontest.torcs.client;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ @Deprecated
/*     */ public class Client {
/*   7 */   private static int UDP_TIMEOUT = 100;
/*     */   
/*     */ 
/*     */   private static int port;
/*     */   
/*     */ 
/*     */   private static String host;
/*     */   
/*     */ 
/*     */   private static String clientId;
/*     */   
/*     */ 
/*     */   private static boolean verbose;
/*     */   
/*     */ 
/*     */   private static int maxEpisodes;
/*     */   
/*     */ 
/*     */   private static int maxSteps;
/*     */   
/*     */   private static Controller.Stage stage;
/*     */   
/*     */   private static String trackName;
/*     */   
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  34 */     parseParameters(args);
/*  35 */     SocketHandler mySocket = new SocketHandler(host, port, verbose);
/*     */     
/*     */ 
/*  38 */     Controller driver = load(args[0]);
/*  39 */     driver.setStage(stage);
/*  40 */     driver.setTrackName(trackName);
/*     */     
/*     */ 
/*  43 */     float[] angles = driver.initAngles();
/*  44 */     String initStr = clientId + "(init";
/*  45 */     for (int i = 0; i < angles.length; i++) {
/*  46 */       initStr = initStr + " " + angles[i];
/*     */     }
/*  48 */     initStr = initStr + ")";
/*     */     
/*  50 */     long curEpisode = 0L;
/*  51 */     boolean shutdownOccurred = false;
/*     */     do
/*     */     {
/*     */       String inMsg;
/*     */       do
/*     */       {
/*  57 */         mySocket.send(initStr);
/*  58 */         inMsg = mySocket.receive(UDP_TIMEOUT);
/*  59 */       } while ((inMsg == null) || (inMsg.indexOf("***identified***") < 0));
/*     */       
/*     */ 
/*  62 */       long currStep = 0L;
/*     */       
/*     */       for (;;)
/*     */       {
/*  66 */         inMsg = mySocket.receive(UDP_TIMEOUT);
/*     */         
/*  68 */         if (inMsg != null)
/*     */         {
/*     */ 
/*  71 */           if (inMsg.indexOf("***shutdown***") >= 0) {
/*  72 */             shutdownOccurred = true;
/*  73 */             System.out.println("Server shutdown!");
/*  74 */             break;
/*     */           }
/*     */           
/*     */ 
/*  78 */           if (inMsg.indexOf("***restart***") >= 0) {
/*  79 */             driver.reset();
/*  80 */             if (!verbose) break;
/*  81 */             System.out.println("Server restarting!");
/*  82 */             break;
/*     */           }
/*     */           
/*     */ 
/*  86 */           Action action = new Action();
/*  87 */           if ((currStep < maxSteps) || (maxSteps == 0)) {
/*  88 */             action = driver.determineAction(new MessageBasedSensorModel(inMsg));
/*     */           } else {
/*  90 */             action.abandonRace = true;
/*     */           }
/*  92 */           currStep += 1L;
/*  93 */           mySocket.send(action.toString());
/*     */         } else {
/*  95 */           System.out.println("Server did not respond within the timeout");
/*     */         }
/*     */         
/*     */       }
/*  99 */     } while ((++curEpisode < maxEpisodes) && (!shutdownOccurred));
/*     */     
/*     */ 
/* 102 */     driver.shutdown();
/* 103 */     mySocket.close();
/* 104 */     System.out.println("Client shutdown.");
/* 105 */     System.out.println("Bye, bye!");
/*     */   }
/*     */   
/*     */   private static void parseParameters(String[] args)
/*     */   {
/* 110 */     port = 3001;
/* 111 */     host = "localhost";
/* 112 */     clientId = "championship2010";
/* 113 */     verbose = false;
/* 114 */     maxEpisodes = 1;
/* 115 */     maxSteps = 0;
/* 116 */     stage = Controller.Stage.PRACTICE;
/* 117 */     trackName = "unknown";
/*     */     
/* 119 */     for (int i = 1; i < args.length; i++) {
/* 120 */       java.util.StringTokenizer st = new java.util.StringTokenizer(args[i], ":");
/* 121 */       String entity = st.nextToken();
/* 122 */       String value = st.nextToken();
/* 123 */       if (entity.equals("port")) {
/* 124 */         port = Integer.parseInt(value);
/*     */       }
/* 126 */       if (entity.equals("host")) {
/* 127 */         host = value;
/*     */       }
/* 129 */       if (entity.equals("id")) {
/* 130 */         clientId = value;
/*     */       }
/* 132 */       if (entity.equals("verbose")) {
/* 133 */         if (value.equals("on")) {
/* 134 */           verbose = true;
/* 135 */         } else if (value.equals(Boolean.valueOf(false))) {
/* 136 */           verbose = false;
/*     */         } else {
/* 138 */           System.out.println(entity + ":" + value + " is not a valid option");
/*     */           
/* 140 */           System.exit(0);
/*     */         }
/*     */       }
/* 143 */       if (entity.equals("id")) {
/* 144 */         clientId = value;
/*     */       }
/* 146 */       if (entity.equals("stage")) {
/* 147 */         stage = Controller.Stage.fromInt(Integer.parseInt(value));
/*     */       }
/* 149 */       if (entity.equals("trackName")) {
/* 150 */         trackName = value;
/*     */       }
/* 152 */       if (entity.equals("maxEpisodes")) {
/* 153 */         maxEpisodes = Integer.parseInt(value);
/* 154 */         if (maxEpisodes <= 0) {
/* 155 */           System.out.println(entity + ":" + value + " is not a valid option");
/*     */           
/* 157 */           System.exit(0);
/*     */         }
/*     */       }
/* 160 */       if (entity.equals("maxSteps")) {
/* 161 */         maxSteps = Integer.parseInt(value);
/* 162 */         if (maxSteps < 0) {
/* 163 */           System.out.println(entity + ":" + value + " is not a valid option");
/*     */           
/* 165 */           System.exit(0);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   private static Controller load(String name) {
/* 172 */     Controller controller = null;
/*     */     try {
/* 174 */       controller = (Controller)Class.forName(name).newInstance();
/*     */     } catch (ClassNotFoundException e) {
/* 176 */       System.out.println(name + " is not a class name");
/* 177 */       System.exit(0);
/*     */     } catch (InstantiationException e) {
/* 179 */       e.printStackTrace();
/*     */     } catch (IllegalAccessException e) {
/* 181 */       e.printStackTrace();
/*     */     }
/* 183 */     return controller;
/*     */   }
/*     */ }


/* Location:              C:\Users\George\Desktop\CIContest-driver.jar!\cicontest\torcs\client\Client.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */