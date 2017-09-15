package ca.junctionbox.cljbuck.source;

import java.util.concurrent.ConcurrentHashMap;

public class FormsTable {
   final ConcurrentHashMap<String, String> table;

   private FormsTable() {
       table = new ConcurrentHashMap();
   }

   public static FormsTable create() {
       return new FormsTable();
   }
}
