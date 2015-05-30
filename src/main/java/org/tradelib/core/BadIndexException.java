package org.tradelib.core;

public class BadIndexException extends RuntimeException {

   private static final long serialVersionUID = -576858998600981103L;

   public BadIndexException() { super(); }
   public BadIndexException(String message) { super(message); }
   public BadIndexException(String message, Throwable cause) { super(message, cause); }
   public BadIndexException(Throwable cause) { super(cause); }
}
