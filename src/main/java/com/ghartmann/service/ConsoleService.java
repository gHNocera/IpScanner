package com.ghartmann.service;

import java.util.Scanner;

public class ConsoleService {

    private final Scanner scanner = new Scanner(System.in);

    public synchronized void printLine(String message){
        System.out.println(message);
    }
    
    public synchronized void print(String message){
        System.out.print(message);
    }

    public synchronized void printError(String message){
        System.err.println(message);
    }

    public String readLine(){
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            } else {
                // If there's no line available, wait a bit and return empty string
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return "";
            }
        } catch (java.util.NoSuchElementException e) {
            // Handle the case when scanner is closed or in an invalid state
            // Don't print warning every time to avoid console spam
            return "";
        } catch (IllegalStateException e) {
            // Handle the case when scanner is closed
            return "";
        }
    }

    public int readInt(){
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    public void close(){
        scanner.close();
    }

}
