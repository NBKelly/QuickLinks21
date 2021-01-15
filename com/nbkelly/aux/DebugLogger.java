package com.nbkelly.aux;

public interface DebugLogger {
    //expected commands:
    //DEBUG(level, message)
    //DEBUGF(level, message, args)
    public void DEBUG(String message); //default: 1
    public void DEBUG(int level, Object message);
    public void DEBUGF(int level, String message, Object... args);
    
    //print(value)
    //printf(value, args)
    //println(value)
    public void print(Object value);
    public void printf(String value, Object... args);
    public void println(Object value);
}
