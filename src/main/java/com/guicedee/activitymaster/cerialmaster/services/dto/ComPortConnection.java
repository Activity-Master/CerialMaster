package com.guicedee.activitymaster.cerialmaster.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.guicedee.activitymaster.cerialmaster.services.exceptions.SerialPortException;
import com.guicedee.activitymaster.core.services.dto.IResourceItem;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.logger.LogFactory;
import gnu.io.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.guicedee.activitymaster.cerialmaster.services.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static gnu.io.SerialPort.*;
import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.*;

@Accessors(chain = true)
@Getter
@Setter
@EqualsAndHashCode(of = "comPort",callSuper = false)
public class ComPortConnection<J extends ComPortConnection<J>>
        extends NRSerialPort
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger log = LogFactory.getLog(ComPortConnection.class);
    public static final EnumSet<ComPortStatus> onlineServerStatus = EnumSet.of(Simulation, Idle, Logging, Running);
    public static final EnumSet<ComPortType> graderTypes = EnumSet.of(ComPortType.Sim20,ComPortType.Sim20_2,ComPortType.Lora,ComPortType.Device);
    
    public static final String COM_NAME = "COM";

    private final Set<Character> endOfMessageCharacters = new HashSet<>();
    private final Set<Character> allowedCharacters = new HashSet<>();

    private int comPort;
    private int baudRate = 9600;
    private int bufferSize = 2048;
    private int dataBits = DATABITS_8;
    private int stopBits = STOPBITS_1;
    private int parity = PARITY_NONE;

    @JsonIgnore
    private final ComPortConnection<J> me;
    
    private ComPortStatus status = Offline;
    private ComPortType type = ComPortType.Device;
    
    private IResourceItem<?> resourceItem;

    private PortReader reader;

    public ComPortConnection(int comPort,ComPortType type) {
        super(COM_NAME + comPort,0);
        this.comPort = comPort;
        this.type = type;
        endOfMessageCharacters.add('\n');
        me = this;
    }

    private DataInputStream ins;
    private DataOutputStream outs;

    public void open() {
        if(endOfMessageCharacters.isEmpty())
        {
            throw new RuntimeException("Tried to open a com connection without an end of line character set?");
        }
        try
        {
            connect();
        }catch (NRSerialPortException nre)
        {
            switch(nre.getMessage())
            {
                case "No Port":
                    setStatus(Missing);
                    return;
                case "Port in Use":
                    setStatus(InUse);
                            return;
                default:
                    log.log(Level.SEVERE,"Unknown Exception",nre);
                    setStatus(GeneralException);
                    return;
            }
        }
        getSerialPortInstance().setDTR(true);
        getSerialPortInstance().setRTS(true);
        // getSerialPortInstance().disableReceiveFraming();
        getSerialPortInstance().disableRs485();
        getSerialPortInstance().disableReceiveFraming();
        getSerialPortInstance().disableReceiveThreshold();
        notifyOnDataAvailable(true);
        getSerialPortInstance().notifyOnBreakInterrupt(true);
        getSerialPortInstance().notifyOnFramingError(true);
        getSerialPortInstance().notifyOnOutputEmpty(true);
        getSerialPortInstance().notifyOnCarrierDetect(true);
        getSerialPortInstance().notifyOnParityError(true);
        getSerialPortInstance().notifyOnDSR(true);
        getSerialPortInstance().notifyOnCTS(true);
        getSerialPortInstance().notifyOnOverrunError(true);
        ins = new DataInputStream(getInputStream());
        outs = new DataOutputStream(getOutputStream());
        try {
            getSerialPortInstance().setSerialPortParams(baudRate,
                    dataBits,
                    stopBits,
                    parity);
        } catch (UnsupportedCommOperationException e) {
            log.log(Level.SEVERE,"Cannot set parameters for the serial port",e);
            reader.processMessageTerminal("Cannot set com port properties",e);
        }
        try {
            addEventListener(reader = new PortReader());
        } catch (TooManyListenersException e) {
            log.log(Level.SEVERE,"Reader has too many listeners",e);
            reader.processMessageTerminal("Reader has too many listeners",e);
        }
        setStatus(Silent);
    }

    public void close()
    {
        try {
            reader = null;
            try {
                ins.close();
            } catch (Exception e) {

            }
            try {
                outs.close();
            } catch (Exception e) {

            }
        }catch (Throwable T)
        {

        }finally {
            try {
                disconnect();
            }catch (Throwable T1)
            {

            }
        }
        setStatus(ComPortStatus.Offline);
    }

    public void writeMessage(ServerMessage<?> sm) {
        try {
            log.log(Level.FINE,"[" + comPort+ "] TX : " + sm.generateMessage());
            outs.writeBytes(sm.generateMessage());
        } catch (Exception e) {
            log.log(Level.SEVERE,"Unable to write message",e);
            reader.processMessageTerminal(e.getMessage(), e);
            disconnect();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public J setStatus(ComPortStatus status) {
        ComPortStatus currentStatus = this.status;
        this.status = status;
        if(currentStatus != this.status)
        {
            Set<IComPortStatusChanged> cleanMessages = GuiceContext.instance()
                                                                   .getLoader(IComPortStatusChanged.class, ServiceLoader.load(IComPortStatusChanged.class));
            for (IComPortStatusChanged<?> messageReceiver : cleanMessages)
            {
                messageReceiver.onComPortStatusChanged(me, currentStatus, this.status);
            }
        }
        return (J) this;
    }

    private static final Set<Integer> baudRates = Set.of(110,
            300,
            600,
            1200,
            4800,
            9600,
            14400,
            19200,
            38400,
            57600,
            115200,
            128000,
            256000
    );
    public static Set<Integer> baudRates() {
        return baudRates;
    }
    private static final Set<Integer> dataBitsSet = Set.of(DATABITS_5,DATABITS_6,DATABITS_7,DATABITS_8);

    public static Set<Integer> dataBits() {
        return dataBitsSet;
    }

    private static final Set<Integer> stopBitsSet = Set.of(STOPBITS_1,STOPBITS_2,STOPBITS_1_5);
    public static Set<Integer> stopBits() {
        return stopBitsSet;
    }

    private static final Map<String,Integer> parityBitsSet = Map.of("PARITY_NONE",PARITY_NONE,
            "PARITY_ODD", PARITY_ODD,
            "PARITY_EVEN",PARITY_EVEN,
            "PARITY_MARK",PARITY_MARK,
            "PARITY_SPACE",PARITY_SPACE);
    public static Map<String,Integer> parities() {
        return parityBitsSet;
    }

    public class PortReader
            implements SerialPortEventListener {
        private StringBuilder readBuffer = new StringBuilder();

        @Override
        public void serialEvent(SerialPortEvent event) {
            if(event.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY)
            {
                //empty data string
            }
            else if(event.getEventType() == SerialPortEvent.HARDWARE_ERROR)
            {
                close();
                setStatus(GeneralException);
            }
            else if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
                try {
                    if(ins.available()>0) {
                        int i;
                        while((i = ins.read()) != -1)
                        {
                            char c = (char) i;
                            if (Character.isAlphabetic(c) || Character.isDigit(c) || allowedCharacters.contains(c)) {
                                readBuffer.append(c);
                            }
                            if (endOfMessageCharacters.contains(c)) {
                                try {
                                    processMessage(readBuffer.toString());
                                } catch (Throwable T) {
                                    processMessageException(readBuffer.toString(), T);
                                } finally {
                                    readBuffer = new StringBuilder();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    log.log(Level.SEVERE,"Cannot read com port, IO Error",e);
                }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void processMessage(String message) {
            log.finer("[" + comPort+ "] RX Raw : " + message);
            Set<ICleanReceivedMessage> cleanMessages = GuiceContext.instance()
                    .getLoader(ICleanReceivedMessage.class, ServiceLoader.load(ICleanReceivedMessage.class));
            for (ICleanReceivedMessage<?> messageReceiver : cleanMessages) {
                message = messageReceiver.cleanMessage(message, me);
            }

            log.config("[" + comPort+ "] RX : " + message);
            Set<IReceiveMessage> receiveMessages = GuiceContext.instance()
                    .getLoader(IReceiveMessage.class, ServiceLoader.load(IReceiveMessage.class));
            for (IReceiveMessage<?> messageReceiver : receiveMessages) {
                messageReceiver.receiveMessage(message, me);
            }
            me.setStatus(Running);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void processMessageException(String message, Throwable T) {
            Set<IErrorReceiveMessage> messageReceivers = GuiceContext.instance()
                    .getLoader(IErrorReceiveMessage.class, ServiceLoader.load(IErrorReceiveMessage.class));
            for (IErrorReceiveMessage<?> messageReceiver : messageReceivers) {
                messageReceiver.receiveErrorMessage(message, T, me);
            }
            log.log(Level.FINE,"Message Exception",T);
            log.log(Level.SEVERE,"Error in receiving string from COM-port: " + T + " - " + message);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void processMessageTerminal(String message, Throwable T) {
            Set<ITerminalReceiveMessage> messageReceivers = GuiceContext.instance()
                    .getLoader(ITerminalReceiveMessage.class, ServiceLoader.load(ITerminalReceiveMessage.class));
            for (ITerminalReceiveMessage<?> messageReceiver : messageReceivers) {
                messageReceiver.receiveTerminalMessage(message, T, me);
            }
            me.close();
            me.setStatus(ComPortStatus.Offline);
        }
    }
}
