 package tk.RedstoneEditor.SurvivalGames;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 public class MapManager
 {
   static String directory = "plugins/SurvivalGames";
   static String fileName = "maps.txt";
   static String br = System.getProperty("line.separator");
   static ArrayList<String> list = new ArrayList();
   static File mapList = new File(directory + "/" + fileName);
   static File serverProperties = new File("server.properties");
   static File newProperties = new File("newserver.properties");
   static File oldProperties = new File("oldserver.properties");
private static int ii;
 
   public static ArrayList<String> getMapList() { return list; }
 
   public static boolean addMap(String mapName) {
     File map = new File(mapName);
     if (map.exists()) {
       list.add(mapName);
       saveList();
       return true;
     }
     return false;
   }
 
   public static void removeMap(String mapName) {
     for (int i = 0; i < list.size(); i++) {
       if (((String)list.get(i)).equals(mapName)) {
         list.set(i, "");
       }
     }
     saveList();
     loadList();
   }
   public static boolean parseMap(String mapName) {
     for (int i = 0; i < list.size(); i++) {
       if (((String)list.get(i)).equals(mapName)) {
         return true;
       }
     }
     return false;
   }
   public static int getLines(File file) {
     int lines = 0;
     try
     {
       FileReader fr = new FileReader(file);
       LineNumberReader ln = new LineNumberReader(fr);
       while (ln.readLine() != null) {
         lines++;
       }
       ln.close();
     } catch (FileNotFoundException e) {
       e.printStackTrace();
     } catch (IOException e) {
       e.printStackTrace();
     }
     return lines;
   }
   public static void saveList() {
     try {
       BufferedWriter write = new BufferedWriter(new FileWriter(mapList));
       for (int i = 0; i < list.size(); i++) {
         if (!((String)list.get(i)).equals(""))
         {
           if (i > 0) {
             write.append(br);
           }
           write.append((CharSequence)list.get(i));
         }
       }
       write.close();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   public static void loadList() {
     mapList = new File(directory + "/" + fileName);
     list.clear();
     try {
       BufferedReader read = new BufferedReader(new FileReader(mapList));
       int length = getLines(mapList);
       for (int i = 0; i < length; i++) {
         list.add(read.readLine());
       }
       read.close();
     } catch (FileNotFoundException e) {
       try {
         mapList.createNewFile();
       } catch (IOException e1) {
         e1.printStackTrace();
       }
       loadList();
     } catch (NumberFormatException e) {
       e.printStackTrace();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   public static void clearList() { String blankText = "";
     try
     {
       BufferedWriter write = new BufferedWriter(new FileWriter(mapList));
       write.append(blankText);
       write.close();
     } catch (IOException e) {
       e.printStackTrace();
     }
     loadList(); }
 
   public static void changeMap(String map) {
     try {
       newProperties.createNewFile();
       ArrayList lines = new ArrayList();
       int line = -1;
       BufferedWriter write = new BufferedWriter(new FileWriter(newProperties));
       LineNumberReader read = new LineNumberReader(new FileReader(serverProperties));
       int i = 0; for (int ii = getLines(serverProperties); i < ii; i++) {
         String current = read.readLine();
         lines.add(current);
         if (current.contains("level-name")) {
           line = read.getLineNumber() - 1;
           lines.set(line, "level-name=" + map);
         }
       }
       read.close();
       for (i = lines.size(); i < ii; i++) {
         if (i > 0) {
           write.append(br);
         }
         write.append((CharSequence)lines.get(i));
       }
       write.close();
       serverProperties.delete();
       newProperties.renameTo(serverProperties);
       newProperties.delete();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 }

/* Location:           C:\Users\RedstoneEditor\Desktop\LSGM.jar
 * Qualified Name:     com.hockeygoalie5.lsgm.MapList
 * JD-Core Version:    0.6.2
 */