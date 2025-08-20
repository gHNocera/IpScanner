package com.ghartmann.service;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkScanner {
    private final AppContext context;
    private final ExecutorService executorService;
    private volatile boolean scanning = false;
    
    public NetworkScanner(AppContext context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(10);
    }
    
    public void startScan() {
        if (scanning) {
            return;
        }
        
        scanning = true;
        executorService.submit(() -> {
            try {
                scanLocalNetwork();
            } catch (Exception e) {
                context.getConsoleService().printError("Erro ao escanear a rede: " + e.getMessage());
            } finally {
                scanning = false;
            }
        });
        
        context.getConsoleService().printLine("\u001B[32mIniciando escaneamento da rede...\u001B[0m");
    }
    
    private void scanLocalNetwork() throws SocketException, UnknownHostException {
        // Get all network interfaces
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        
        // Add local machine to the list
        InetAddress localHost = InetAddress.getLocalHost();
        addHostToList(localHost);
        
        // Scan all network interfaces
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            
            // Skip loopback, virtual, and non-operational interfaces
            if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                continue;
            }
            
            // Get all IP addresses for this interface
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                
                // Skip IPv6 addresses for simplicity
                if (!(inetAddress instanceof Inet4Address)) {
                    continue;
                }
                
                String interfaceIp = inetAddress.getHostAddress();
                
                // Add this interface's IP to the list
                try {
                    byte[] mac = networkInterface.getHardwareAddress();
                    if (mac != null) {
                        String macAddress = formatMacAddress(mac);
                        context.getIpToMac().put(interfaceIp, macAddress);
                        context.getMacToVendor().put(macAddress, getVendorByMac(macAddress));
                    }
                } catch (Exception e) {
                    context.getConsoleService().printError("Erro ao obter MAC para " + interfaceIp + ": " + e.getMessage());
                }
                
                // Extract network prefix for this interface
                String[] ipParts = interfaceIp.split("\\.");
                String networkPrefix = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";
                
                // Scan all IPs in this subnet
                for (int i = 1; i <= 254; i++) {
                    final String ip = networkPrefix + i;
                    executorService.submit(() -> {
                        try {
                            InetAddress address = InetAddress.getByName(ip);
                            if (address.isReachable(1000)) { // 1 second timeout
                                try {
                                    // Try to get MAC address using ARP
                                    String macAddress = getMacFromArp(ip);
                                    if (macAddress != null && !macAddress.isEmpty()) {
                                        context.getIpToMac().put(ip, macAddress);
                                        context.getMacToVendor().put(macAddress, getVendorByMac(macAddress));
                                    }
                                } catch (Exception e) {
                                    // Silently ignore errors for individual IPs
                                }
                            }
                        } catch (IOException e) {
                            // Silently ignore unreachable hosts
                        }
                    });
                }
            }
        }
    }
    
    private String getMacFromArp(String ip) {
        try {
            // Run ARP command to get MAC address
            Process process = Runtime.getRuntime().exec("arp -a " + ip);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(ip)) {
                    // Extract MAC address from ARP output
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        for (String part : parts) {
                            // Look for MAC address format (xx-xx-xx-xx-xx-xx)
                            if (part.matches("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})")) {
                                return part.replace('-', ':').toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Ignore errors
        }
        return null;
    }
    
    private String formatMacAddress(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X", mac[i]));
            if (i < mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
    
    private String getVendorByMac(String macAddress) {
        // This would ideally use a MAC vendor database
        // For simplicity, we'll just return some common vendors based on OUI
        String oui = macAddress.substring(0, 8).toUpperCase();
        switch (oui) {
            case "00:00:0C":
                return "Cisco Systems";
            case "00:1A:11":
                return "Google";
            case "00:25:00":
                return "Apple, Inc.";
            case "B8:27:EB":
                return "Raspberry Pi Foundation";
            case "00:50:56":
                return "VMware, Inc.";
            case "00:1B:44":
                return "SanDisk Corporation";
            default:
                return "Desconhecido";
        }
    }
    
    private void addHostToList(InetAddress address) {
        try {
            String ip = address.getHostAddress();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
            if (networkInterface != null) {
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    String macAddress = formatMacAddress(mac);
                    context.getIpToMac().put(ip, macAddress);
                    context.getMacToVendor().put(macAddress, getVendorByMac(macAddress));
                }
            }
        } catch (Exception e) {
            context.getConsoleService().printError("Erro ao obter MAC para host: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        scanning = false;
        executorService.shutdownNow();
    }
}