package net.tradelib.misc;

public class InstrumentText {
   private boolean section = false; // true if this is a section (like "Metals")
   private String name;
   private String symbol;
   private String expiration;
   private String status;
   
   private InstrumentText() {
   }
   
   public static InstrumentText makeSection(String name) {
      InstrumentText ir = new InstrumentText();
      ir.setName(name);
      ir.section = true;
      return ir;
   }
   
   public static InstrumentText make(String name, String symbol, String expiration, String status) {
      InstrumentText ir = new InstrumentText();
      ir.setName(name);
      ir.setSymbol(symbol);
      ir.setStatus(status);
      ir.setExpiration(expiration);
      return ir;
   }
   
   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getSymbol() {
      return symbol;
   }
   public void setSymbol(String symbol) {
      this.symbol = symbol;
   }
   public String getStatus() {
      return status;
   }
   public void setStatus(String status) {
      this.status = status;
   }
   public String getExpiration() {
      return expiration;
   }

   public void setExpiration(String expiration) {
      this.expiration = expiration;
   }
   
   public boolean isSection() {
      return section;
   }
}
