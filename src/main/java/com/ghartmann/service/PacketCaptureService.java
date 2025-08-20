package com.ghartmann.service;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketCaptureService {
    private final AppContext context;
    private final ExecutorService executorService;
    private PcapHandle handle;
    private final AtomicBoolean capturing = new AtomicBoolean(false);
    
    public PacketCaptureService(AppContext context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public void startCapture() {
        if (capturing.get()) {
            context.getConsoleService().printLine("\u001B[33mCaptura de pacotes já está em execução.\u001B[0m");
            return;
        }
        
        String trackedIp = context.getTrackedIp();
        if (trackedIp == null || trackedIp.isEmpty()) {
            context.getConsoleService().printLine("\u001B[31mNenhum IP para rastrear. Use o comando 'iptracker <IP>' primeiro.\u001B[0m");
            return;
        }
        
        executorService.submit(() -> {
            try {
                // Initialize network interface
                PcapNetworkInterface nif = getNetworkInterface();
                if (nif == null) {
                    context.getConsoleService().printError("Nenhuma interface de rede disponível para captura.");
                    return;
                }
                
                // Open the device and set filter with optimized settings
                // Reduced buffer size for faster processing and lower timeout (1ms instead of 10ms)
                handle = nif.openLive(4096, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1);
                String filter = "host " + trackedIp;
                handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
                
                // Set non-blocking mode for faster packet processing
                handle.setBlockingMode(PcapHandle.BlockingMode.NONBLOCKING);
                
                context.getConsoleService().printLine("\u001B[32mIniciando captura de pacotes para IP: " + trackedIp + "\u001B[0m");
                context.getConsoleService().printLine("\u001B[34mPressione 'exit' para parar a captura e sair do programa\u001B[0m");
                
                capturing.set(true);
                
                // Start optimized packet capture loop with batch processing
                while (capturing.get() && context.isRunning()) {
                    try {
                        // Process packets in batches for better performance
                        handle.loop(10, (PacketListener) packet -> {
                            if (packet != null && capturing.get()) {
                                processPacket(packet);
                            }
                        });
                        
                        // Small sleep to prevent CPU overuse
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // Thread was interrupted, exit the loop
                        break;
                    } catch (NotOpenException e) {
                        break;
                    }
                }
            } catch (PcapNativeException | NotOpenException e) {
                context.getConsoleService().printError("Erro na captura de pacotes: " + e.getMessage());
            } finally {
                stopCapture();
            }
        });
    }
    
    private void processPacket(Packet packet) {
        try {
            // Extract IP header information
            org.pcap4j.packet.IpPacket ipPacket = packet.get(org.pcap4j.packet.IpPacket.class);
            if (ipPacket != null) {
                String srcIp = ipPacket.getHeader().getSrcAddr().getHostAddress();
                String dstIp = ipPacket.getHeader().getDstAddr().getHostAddress();
                
                // Only show source and destination IP addresses
                context.getConsoleService().printLine("\u001B[36m[" + java.time.LocalTime.now() + "] " + 
                                                  "\u001B[33mOrigem: " + srcIp + " \u001B[32m→ \u001B[33mDestino: " + dstIp + "\u001B[0m");
            }
        } catch (Exception e) {
            // If we can't extract IP information, just show minimal info
            context.getConsoleService().printLine("\u001B[36m[" + java.time.LocalTime.now() + "] Pacote capturado\u001B[0m");
        }
    }
    
    private PcapNetworkInterface getNetworkInterface() {
        try {
            // Try to get the default interface
            InetAddress addr = InetAddress.getByName(context.getTrackedIp());
            for (PcapNetworkInterface dev : Pcaps.findAllDevs()) {
                for (PcapAddress address : dev.getAddresses()) {
                    if (address.getAddress() instanceof Inet4Address && 
                        address.getNetmask() != null) {
                        return dev;
                    }
                }
            }
            
            // Return the first available interface instead of asking for user selection
            // This avoids the interactive prompt that causes issues
            PcapNetworkInterface[] devices = Pcaps.findAllDevs().toArray(new PcapNetworkInterface[0]);
            if (devices.length > 0) {
                context.getConsoleService().printLine("\u001B[33mUsando interface de rede: " + devices[0].getName() + "\u001B[0m");
                return devices[0];
            } else {
                context.getConsoleService().printError("Nenhuma interface de rede disponível.");
                return null;
            }
        } catch (IOException | PcapNativeException e) {
            context.getConsoleService().printError("Erro ao selecionar interface de rede: " + e.getMessage());
            return null;
        }
    }
    
    public void stopCapture() {
        capturing.set(false);
        if (handle != null && handle.isOpen()) {
            try {
                handle.breakLoop();
                handle.close();
                context.getConsoleService().printLine("\u001B[33mCaptura de pacotes interrompida.\u001B[0m");
            } catch (NotOpenException e) {
                // Ignore if already closed
            }
        }
    }
    
    public void shutdown() {
        stopCapture();
        executorService.shutdownNow();
    }
    
    public boolean isCapturing() {
        return capturing.get();
    }
}