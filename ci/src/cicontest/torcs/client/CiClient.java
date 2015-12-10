/*     */ package cicontest.torcs.client;
/*     */ 
/*     */ import cicontest.algorithm.abstracts.DriversUtils;
/*     */ import cicontest.torcs.controller.Driver;
/*     */ import cicontest.torcs.controller.extras.AutomatedClutch;
/*     */ import cicontest.torcs.controller.extras.AutomatedGearbox;
/*     */ import cicontest.torcs.race.Race;
/*     */ import cicontest.torcs.race.RaceResult;
/*     */ import java.awt.AWTException;
/*     */ import java.awt.Robot;
/*     */ import java.io.PrintStream;
/*     */ import race.TorcsConfiguration;
/*     */ 
/*     */ public class CiClient extends Thread
/*     */ {
/*  16 */   private static int UDP_TIMEOUT = 100;
/*  17 */   private static int port = 3001;
/*  18 */   private String host = "localhost";
/*     */   
/*     */   private Driver driver;
/*     */   private int drivernumber;
/*     */   private SocketHandler mySocket;
/*     */   private Race race;
/*     */   private RaceResult result;
/*  25 */   private boolean isRemoteClient = false;
/*  26 */   private boolean isOverruled = false;
/*     */   
/*     */   public static void main(String[] args) {
/*  29 */     if (args[0].equals("-dummy")) {
/*  30 */       startDummy();
/*  31 */       return;
/*     */     }
/*  33 */     if (args[0].split("-").length > 1) {
/*  34 */       int start = Integer.parseInt(args[0].split("-")[0]);
/*  35 */       int end = Integer.parseInt(args[0].split("-")[1]);
/*     */       
/*  37 */       for (int i = start; i <= end; i++) {
/*  38 */         args[0] = ("" + i);
/*  39 */         startRemoteClient(args);
/*     */       }
/*     */     } else {
/*  42 */       startRemoteClient(args);
/*     */     }
/*     */   }
/*     */   
/*     */   public static void startDummy() {
/*  47 */     CiClient driverclient = new CiClient();
/*  48 */     driverclient.isRemoteClient = true;
/*  49 */     driverclient.drivernumber = 1;
/*  50 */     driverclient.host = "localhost";
/*     */     
/*  52 */     Driver driver = new DummyDriver();
/*     */     
/*  54 */     driver.setStage(Controller.Stage.PRACTICE);
/*     */     
/*  56 */     driverclient.setup(null, driver, 1);
/*  57 */     driverclient.connect();
/*  58 */     driverclient.start();
/*     */   }
/*     */   
/*     */   public static void startRemoteClient(String[] args) {
/*  62 */     CiClient driverclient = new CiClient();
/*  63 */     driverclient.isRemoteClient = true;
/*  64 */     driverclient.drivernumber = Integer.parseInt(args[0]);
/*  65 */     driverclient.host = args[1];
/*     */     
/*  67 */     Driver driver = null;
/*     */     try
/*     */     {
/*  70 */       driver = (Driver)driverclient.getContextClassLoader().loadClass(args[2]).newInstance();
/*     */       
/*  72 */       System.out.println("Loaded " + args[2]);
/*     */     } catch (Throwable t) {
/*  74 */       System.out.println("Error in " + args[2]);
/*  75 */       t.printStackTrace();
/*     */     }
/*     */     
/*  78 */     MemoryContainer.getInstance(driver.getClass());
/*     */     
/*  80 */     driver.setStage(Controller.Stage.PRACTICE);
/*     */     
/*  82 */     driverclient.setup(null, driver, Integer.parseInt(args[0]));
/*  83 */     driverclient.connect();
/*  84 */     driverclient.start();
/*     */   }
/*     */   
/*     */   public void setup(Race race, Driver driver, int drivernumber) {
/*  88 */     MemoryContainer.getInstance(driver.getClass());
/*  89 */     this.driver = driver;
/*  90 */     this.drivernumber = drivernumber;
/*  91 */     this.race = race;
/*  92 */     this.result = new RaceResult();
/*     */     
/*  94 */     driver.init();
/*     */   }
/*     */   
/*     */   private void connect() {
/*  98 */     this.mySocket = new SocketHandler(this.host, port + this.drivernumber - 1, false);
/*     */     
/*     */ 
/* 101 */     float[] angles = this.driver.initAngles();
/* 102 */     String initStr = "championship2010 " + this.drivernumber + "(init";
/* 103 */     for (int i = 0; i < angles.length; i++) {
/* 104 */       initStr = initStr + " " + angles[i];
/*     */     }
/* 106 */     initStr = initStr + ")";
/*     */     
/*     */     String inMsg;
/*     */     
/*     */     do
/*     */     {
/* 112 */       System.out.println(getName() + ": Sending init String via port: " + port + "...");
/* 113 */       this.mySocket.send(initStr);
/* 114 */       inMsg = this.mySocket.receive(UDP_TIMEOUT);
/* 115 */     } while ((inMsg == null) || (inMsg.indexOf("***identified***") < 0));
/* 116 */     System.out.println(getName() + ": Init String acknowledged by Torcs.");
/*     */     try
/*     */     {
/* 119 */       Thread.sleep(500L);
/*     */     } catch (InterruptedException e) {
/* 121 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */   
/*     */   public void run() {
/* 126 */     connect();
/* 127 */     String lastMessage = "";
/* 128 */     boolean errorfound = false;
/* 129 */     int timeout = 5;
/* 130 */     if ((TorcsConfiguration.getInstance().getOptionalProperty("timeout") != null) && 
/* 131 */       (!TorcsConfiguration.getInstance().getOptionalProperty("timeout").equals("")))
/*     */     {
/* 133 */       timeout = Integer.parseInt(TorcsConfiguration.getInstance().getProperty("timeout"));
/*     */     }
/*     */     
/*     */ 
/* 137 */     this.result.setDriver(this.driver);
/*     */     
/*     */ 
/* 140 */     int counter = 0;
/* 141 */     int counter2 = 0;
/*     */     
/*     */     for (;;)
/*     */     {
/* 145 */       String inMsg = this.mySocket.receive(UDP_TIMEOUT);
/*     */       
/* 147 */       if (inMsg != null) {
/* 148 */         counter = 0;
/* 149 */         counter2 = 0;
/* 150 */         this.race.crashfixdriver = ((this.drivernumber + 1) % this.race.size());
/*     */         
/*     */ 
/* 153 */         if (inMsg.indexOf("***shutdown***") >= 0) {
/* 154 */           System.out.println("Recieved Shutdown message");
/* 155 */           break;
/*     */         }
/*     */         
/*     */ 
/* 159 */         if (inMsg.indexOf("***restart***") >= 0) {
/*     */           break;
/*     */         }
/*     */         
/*     */ 
/* 164 */         Action action = null;
/*     */         
/* 166 */         MessageBasedSensorModel sensors = new MessageBasedSensorModel(inMsg);
/*     */         
/*     */ 
/* 169 */         if (!sensors.isValid()) {
/* 170 */           this.mySocket.send(lastMessage);
/*     */         } else {
/* 172 */           if (sensors.getMessageType() == 1) {
/* 173 */             gatherStatistics(sensors);
/* 174 */             this.driver.shutdown();
/*     */             
/*     */ 
/* 177 */             for (int ee = 0; ee < 3; ee++) {
/* 178 */               this.mySocket.send("***recieved stats***");
/* 179 */               synchronized (this) {
/*     */                 try {
/* 181 */                   wait(1000L);
/*     */                 }
/*     */                 catch (Exception e) {}
/*     */               }
/*     */             }
/* 186 */             break;
/*     */           }
/*     */           try {
/* 189 */             if (!this.isOverruled) {
/* 190 */               action = this.driver.determineAction(sensors);
/*     */             } else {
/* 192 */               action = new Action();
/* 193 */               new DriversUtils().calm(action, sensors);
/* 194 */               new AutomatedClutch().process(action, sensors);
/* 195 */               new AutomatedGearbox().process(action, sensors);
/*     */             }
/*     */           } catch (Throwable t) {
/* 198 */             if (!errorfound) {
/* 199 */               t.printStackTrace();
/*     */             }
/* 201 */             action = new Action();
/* 202 */             action.brake = 0.0D;
/* 203 */             action.steering = 0.0D;
/* 204 */             action.accelerate = 1.0D;
/* 205 */             t.printStackTrace();
/* 206 */             errorfound = true;
/*     */           }
/*     */           
/* 209 */           if ((this.race != null) && (this.race.getTerminationType() == cicontest.torcs.race.Race.Termination.TICKS) && 
/* 210 */             (this.race.getTerminationValue() <= sensors.getTicks())) {
/* 211 */             action.abandonRace = true;
/*     */           }
/*     */           try {
/* 214 */             if ((this.race != null) && (sensors.getTicks() > 1000) && (sensors.getDistanceRaced() < 100.0D) && (this.race.getStage() != Controller.Stage.PRACTICE)) {
/* 215 */               action = new Action();
/* 216 */               new DriversUtils().calm(action, sensors);
/* 217 */               new AutomatedClutch().process(action, sensors);
/* 218 */               new AutomatedGearbox().process(action, sensors);
/*     */             }
/* 220 */             if ((this.race != null) && (sensors.getCurrentLapTime() > 600.0D) && (this.race.getStage() != Controller.Stage.PRACTICE)) {
/* 221 */               this.isOverruled = true;
/*     */             }
/*     */           }
/*     */           catch (Throwable t) {}
/*     */           
/* 226 */           lastMessage = action.toString();
/* 227 */           this.mySocket.send(action.toString());
/*     */         }
/*     */       }
/* 230 */       else if (this.race.crashfixdriver == this.drivernumber) {
/* 231 */         counter++;
/* 232 */         if ((counter > timeout) && (!lastMessage.equals(""))) {
/* 233 */           counter2++;
/* 234 */           counter = 0;
/* 235 */           this.mySocket.send(lastMessage);
/* 236 */           if ((System.getProperty("os.name").toLowerCase().startsWith("win")) && (counter2 >= 1)) {
/*     */             try
/*     */             {
/* 239 */               System.out.println("TORCS hangs, now sending sendkeys");
/* 240 */               Robot robot = new Robot();
/* 241 */               robot.keyPress(80);
/*     */             } catch (AWTException e) {
/* 243 */               e.printStackTrace();
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     
/*     */ 
/* 251 */     if (!this.isRemoteClient) {
/* 252 */       this.race.setResults(this.driver, this.result);
/*     */     }
/*     */     
/* 255 */     this.driver.exit();
/*     */     
/* 257 */     this.driver.shutdown();
/* 258 */     this.mySocket.close();
/*     */   }
/*     */   
/*     */   private void gatherStatistics(MessageBasedSensorModel model) {
/* 262 */     this.result.setBestLapTime(model.getBestLapTime());
/* 263 */     this.result.setDistance(model.getDistanceRaced());
/* 264 */     this.result.setFinished(model.getIsFinished());
/* 265 */     this.result.setLaps(model.getLaps());
/* 266 */     this.result.setTime(model.getTime());
/* 267 */     this.result.setPosition(model.getRacePosition());
/* 268 */     this.result.setLastlap(model.getLastLapTime());
/*     */   }
/*     */ }


/* Location:              C:\Users\George\Desktop\CIContest-driver.jar!\cicontest\torcs\client\CiClient.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */