package com.guicedee.activitymaster.cerialmaster.services.dto;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import com.google.common.collect.EvictingQueue;
import com.guicedee.activitymaster.cerialmaster.services.*;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.plugins.quickforms.annotations.*;
import com.jwebmp.plugins.quickforms.annotations.states.*;
import gnu.io.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Log4j2Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.*;
import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortType.*;
import static gnu.io.SerialPort.*;

@Accessors(chain = true)
@Getter
@Setter
@EqualsAndHashCode(of = "comPort", callSuper = false)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"inspection"})
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ComPortConnection<J extends ComPortConnection<J>>
		extends NRSerialPort<J>
		implements Serializable, Comparable<J>
{
	@Serial
	private static final long serialVersionUID = 1L;
	
	private UUID id;
	
	private String logName = "";
	private transient Logger log;
	
	public static final EnumSet<ComPortStatus> onlineServerStatus = EnumSet.of(Simulation, Idle, Opening, Logging, OperationInProgress, Running, Silent, FileTransfer);
	
	public static final String COM_NAME = "COM";
	
	private Integer maxLength;
	
	@WebFormStartRow
	@WebField(classes = "col-12 col-md-2")
	@LabelField(value = "Start Chars", classes = "col-12 col-md-2")
	@TextField
	private Set<Character> startOfMessageCharacters = new HashSet<>();
	@WebField(classes = "col-12 col-md-2")
	@LabelField(value = "End Chars", classes = "col-12 col-md-2")
	@TextField
	private Set<Character> endOfMessageCharacters = new HashSet<>();
	
	@WebField(classes = "col-12 col-md-2")
	@LabelField(value = "Allowed Chars", classes = "col-12 col-md-2")
	@TextField
	private Set<Character> allowedCharacters = new HashSet<>();
	
	@WebFormEndRow
	@WebFormStartRow
	@LabelField(value = "Baud Rate", classes = "col-12 col-md-3")
	@WebField(classes = "col-12 col-md-3")
	@NumberField
	private int baudRate = 9600;
	@WebField(classes = "col-12 col-md-3")
	@LabelField(value = "Buffer Size", classes = "col-12 col-md-3")
	@NumberField
	private int bufferSize = 4096;
	
	@WebFormEndRow
	@WebFormStartRow
	@WebField(classes = "col-12 col-md-3")
	@LabelField(value = "Data Bits", classes = "col-12 col-md-3")
	@NumberField
	private int dataBits = DATABITS_8;
	@WebField(classes = "col-12 col-md-3")
	@LabelField(value = "Stop Bits", classes = "col-12 col-md-3")
	@NumberField
	private int stopBits = STOPBITS_1;
	@WebField(classes = "col-12 col-md-3")
	@LabelField(value = "Parity", classes = "col-12 col-md-3")
	@NumberField
	private int parity = PARITY_NONE;
	
	@WebFormEndRow
	@WebFormStartRow
	@WebIgnore
	@WebField(classes = "col-12 col-md-3")
	@LabelField(value = "Status", classes = "col-12 col-md-3")
	@WebReadOnly
	@SelectField
	@JsonProperty("comPortStatus")
	@JsonAlias({"status", "comPortStatus"})
	private ComPortStatus comPortStatus = Offline;
	
	@WebIgnore
	@WebField(classes = "col-12 col-md-3")
	@LabelField(value = "Type", classes = "col-12 col-md-3")
	@SelectField
	private ComPortType type;
	
	@WebReadOnly
	@WebIgnore
	private int comPort;
	
	@JsonIgnore
	private IResourceItem<?, ?> resourceItem;
	
	@JsonIgnore
	private transient PortReader reader;
	
	private transient boolean reading;
	private transient boolean simulated;
	
	private LocalDateTime lastMessageReceivedTime;
	
	private Pattern matchingPattern;
	
	public J setBaudRate(int rate)
	{
		setBaud(rate);
		this.baudRate = rate;
		//noinspection unchecked
		return (J) this;
	}
	
	@JsonIgnore
	private final ComPortConnection<J> me;
	
	public J setResourceItem(IResourceItem<?, ?> item)
	{
		this.resourceItem = item;
		setId(item.getId());
		//noinspection unchecked
		return (J) this;
	}
	
	public IResourceItem<?, ?> getResourceItem()
	{
		if (resourceItem == null && id != null)
		{
			resourceItem = GuiceContext.get(IResourceItemService.class)
			                           .findByUUID(id);
		}
		return resourceItem;
	}
	
	public ComPortConnection()
	{
		this(-1, ComPortType.Device);
	}
	
	public ComPortConnection(int comPort, ComPortType type)
	{
		super(COM_NAME + comPort, 0);
		this.comPort = comPort;
		this.type = type;
		endOfMessageCharacters.add('\n');
		endOfMessageCharacters.add('\r');
		if (scanners.contains(type))
		{
			startOfMessageCharacters.add((char) 1);
			startOfMessageCharacters.add((char) 2);
			endOfMessageCharacters.add((char) 3);
			endOfMessageCharacters.add((char) 4);
		}
		logName = "comports.logs" + comPort;
		log = Logger.getLogger(logName);
		me = this;
	}
	
	public ComPortConnection<J> setComPort(int comPort)
	{
		logName = "comports.logs" + comPort;
		log = Logger.getLogger(logName);
		this.comPort = comPort;
		return this;
	}
	
	@JsonIgnore
	private transient InputStream ins;
	@JsonIgnore
	private transient OutputStream outs;
	
	private transient SimulatedInputStream simIn;
	
	public void open()
	{
		open(false);
	}
	
	public void open(boolean silentStatusUpdate)
	{
		if (endOfMessageCharacters.isEmpty())
		{
			throw new RuntimeException("Tried to open a com connection without an end of line character set?");
		}
		try
		{
			if (!silentStatusUpdate)
			{
				setComPortStatus(Opening);
			}
			else
			{
				setComPortStatus(Opening, true);
			}
			
			connect();
			//	Thread.sleep(100);
			
			//JobService.getInstance()
			//          .addPollingJob("ComPortLastReceivedStatusPolling", new ComPortIdleMonitor(), 5, TimeUnit.MINUTES);
			//	JobService.getInstance()
			//          .addPollingJob("ComPortRestartReadingPolling", new ComPortRestarter(this),60, 15, TimeUnit.SECONDS);
			
			getLog().log(Level.INFO, "Created Com Port Status Monitor - " + comPort);
			
			if (getSerialPortInstance() == null)
			{
				throw new NRSerialPortException("No Port Internal error");
			}
			
			getSerialPortInstance().disableReceiveFraming();
			getSerialPortInstance().disableRs485();
			getSerialPortInstance().disableReceiveFraming();
			getSerialPortInstance().disableReceiveThreshold();
			
			notifyOnDataAvailable(true);
			getSerialPortInstance().notifyOnDataAvailable(true);
			getSerialPortInstance().setInputBufferSize(512000);
			getSerialPortInstance().setOutputBufferSize(512000);
			getSerialPortInstance().setFlowControlMode(FLOWCONTROL_NONE);
			
			ins = new DataInputStream(getInputStream());
			outs = new DataOutputStream(getOutputStream());
			try
			{
				getSerialPortInstance().setSerialPortParams(baudRate,
						dataBits,
						stopBits,
						parity);
			}
			catch (UnsupportedCommOperationException e)
			{
				getLog().log(Level.SEVERE, "Cannot set parameters for the serial port", e);
				processMessageTerminal("Cannot set com port properties", e);
			}
			simulated = false;
			try
			{
				if (reader == null)
				{
					addEventListener(reader = new PortReader());
				}
				else
				{
					addEventListener(reader);
				}
			}
			catch (TooManyListenersException e)
			{
				getLog().log(Level.WARNING, "Reader has too many listeners");
				processMessageTerminal("Reader has too many listeners", e);
			}
			if (!silentStatusUpdate)
			{
				setComPortStatus(Silent);
			}
			else
			{
				setComPortStatus(Silent, true);
			}
		}
		catch (NRSerialPortException nre)
		{
			getLog().log(Level.SEVERE, "Unable to port it - ", nre);
			switch (nre.getMessage())
			{
				case "No Port":
					setComPortStatus(Missing);
					return;
				case "No Port Internal error":
					setComPortStatus(GeneralException);
					return;
				case "Port in Use":
					setComPortStatus(InUse);
					return;
				default:
					getLog().log(Level.SEVERE, "Unknown Exception", nre);
					setComPortStatus(GeneralException);
			}
		}
		catch (Throwable nre)
		{
			getLog().log(Level.SEVERE, "Unknown Exception", nre);
			setComPortStatus(GeneralException);
		}
	}
	
	public Set<IReceiveMessage<?>> getReceivers()
	{
		if (!receivers.containsKey(this))
		{
			try
			{
				Set<IReceiveMessage> receiveMessages = GuiceContext.instance()
				                                                   .getLoader(IReceiveMessage.class, ServiceLoader.load(IReceiveMessage.class));
				receivers.putIfAbsent(this, (Set) receiveMessages);
			}
			catch (Throwable T)
			{
				log.log(Level.SEVERE, "Cannot register IReceiveMessage(s)", T);
			}
		}
		return receivers.getOrDefault(this, new HashSet<>());
	}
	
	public void close()
	{
		close(false);
	}
	
	public void close(boolean silentStatusUpdate)
	{
		try
		{
			reader = null;
			try
			{
				ins.close();
			}
			catch (Exception e)
			{
			
			}
			try
			{
				outs.close();
			}
			catch (Exception e)
			{
			
			}
		}
		catch (Throwable T)
		{
		
		}
		finally
		{
			try
			{
				receivers.remove(this);
				disconnect();
			}
			catch (Throwable T1)
			{
			
			}
		}
		if (!silentStatusUpdate)
		{
			setComPortStatus(com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.Offline);
		}
		else
		{
			setComPortStatus(com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.Offline, true);
		}
	}
	
	public void writeMessage(ServerMessage<?> sm)
	{
		try
		{
			writeOrReadMessage(sm.generateMessage(), false);
		}
		catch (Exception e)
		{
			getLog().log(Level.SEVERE, "Unable to write message - " + sm.generateMessage(), e);
			processMessageTerminal(e.getMessage(), e);
			try
			{
				wait(1000);
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public J setComPortStatus(ComPortStatus comPortStatus)
	{
		ComPortStatus currentStatus = this.comPortStatus;
		this.comPortStatus = comPortStatus;
		if (currentStatus != this.comPortStatus)
		{
			Set<IComPortStatusChanged> cleanMessages = GuiceContext.instance()
			                                                       .getLoader(IComPortStatusChanged.class, ServiceLoader.load(IComPortStatusChanged.class));
			for (IComPortStatusChanged<?> comPortStatusChangeReceiver : cleanMessages)
			{
				comPortStatusChangeReceiver.onComPortStatusChanged(me, currentStatus, this.comPortStatus);
			}
		}
		return (J) this;
	}
	
	@SuppressWarnings({"unchecked"})
	public J setComPortStatus(ComPortStatus status, boolean noOperations)
	{
		this.comPortStatus = status;
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
	
	public static Set<Integer> baudRates()
	{
		return baudRates;
	}
	
	private static final Set<Integer> dataBitsSet = Set.of(DATABITS_5, DATABITS_6, DATABITS_7, DATABITS_8);
	
	public static Set<Integer> dataBits()
	{
		return dataBitsSet;
	}
	
	private static final Set<Integer> stopBitsSet = Set.of(STOPBITS_1, STOPBITS_2, STOPBITS_1_5);
	
	public static Set<Integer> stopBits()
	{
		return stopBitsSet;
	}
	
	private static final Map<String, Integer> parityBitsSet = Map.of("PARITY_NONE", PARITY_NONE,
			"PARITY_ODD", PARITY_ODD,
			"PARITY_EVEN", PARITY_EVEN,
			"PARITY_MARK", PARITY_MARK,
			"PARITY_SPACE", PARITY_SPACE);
	
	public static Map<String, Integer> parities()
	{
		return parityBitsSet;
	}
	
	@Override
	public int compareTo(J o)
	{
		return Integer.compare(getComPort(), o.getComPort());
	}
	
	public static class SimulatedInputStream extends InputStream
	{
		private final List<Character> message = new ArrayList<>();
		
		public SimulatedInputStream writeText(String message)
		{
			for (char c : message.toCharArray())
			{
				getMessage().add(c);
			}
			return this;
		}
		
		public List<Character> getMessage()
		{
			return message;
		}
		
		@Override
		public int read() throws IOException
		{
			ListIterator<Character> characterListIterator = message.listIterator();
			if (characterListIterator.hasNext())
			{
				char c = characterListIterator.next();
				return c;
			}
			return 0;
		}
		
		@Override
		public int available() throws IOException
		{
			return getMessage().size();
		}
		
		
		@Override
		public synchronized void reset() throws IOException
		{
			getMessage().clear();
			super.reset();
		}
	}
	
	public static class SimulatedOutputStream extends OutputStream
	{
		@Override
		public void write(int b) throws IOException
		{
			//throw away message logged elsewhere
		}
	}
	
	public class PortReader
			implements SerialPortEventListener
	{
		private StringBuffer readBuffer = new StringBuffer();
		
		@Override
		public synchronized void serialEvent(SerialPortEvent event)
		{
			if (event.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY)
			{
				//empty data string
			}
			else if (event.getEventType() == SerialPortEvent.CTS)
			{
				System.out.println("Clear to send?!?!? - ok lets check");
			}
			else if (event.getEventType() == SerialPortEvent.HARDWARE_ERROR)
			{
				//	close();
				//	setComPortStatus(GeneralException);
			}
			else if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE && event.getNewValue())
			{
				try
				{
					reading = true;
					readByte();
				}
				catch (Throwable e)
				{
					getLog().log(Level.SEVERE, "Cannot read com port, IO Error", e);
				}
				finally
				{
					reading = false;
				}
			}
		}
		
		private void readByte() throws IOException
		{
			if (ins.available() > 0)
			{
				int i;
				nextchar:
				while ((i = ins.read()) != -1)
				{
					char c = (char) i;
					if (startOfMessageCharacters.contains(c))
					{
						readBuffer = new StringBuffer();
					}
					if (Character.isAlphabetic(c) ||
					    Character.isDigit(c) ||
					    '.' == c ||
					    ',' == c ||
					    allowedCharacters.contains(c)
					)
					{
						readBuffer.append(c);
					}
					
					boolean found = false;
					if (matchingPattern != null)
					{
						Matcher matcher = matchingPattern.matcher(readBuffer.toString());
						while (matcher.find())
						{
							found = true;
							String message = matcher.group(0);
							try
							{
								message = matcher.group(0);
								if (!Strings.isNullOrEmpty(message))
								{
									writeOrReadMessage(message, true);
								}
							}
							finally
							{
								readBuffer = new StringBuffer();
								continue nextchar;
							}
						}
					}
					else if ((endOfMessageCharacters.contains(c) || (maxLength != null && readBuffer.length() >= maxLength)))
					{
						//	System.out.println("end of message char - " + c + " size - " +  readBuffer.length() + " / " + maxLength);
						try
						{
							String message = readBuffer.toString();
							message = StringUtils.strip(message);
							message = StringUtils.trim(message);
							if (!Strings.isNullOrEmpty(message))
							{
								writeOrReadMessage(message, true);
							}
							
						}
						catch (Throwable T)
						{
							processMessageException(readBuffer.toString(), T);
						}
						finally
						{
							readBuffer = new StringBuffer();
						}
					}
				}
			}
		}
	}
	
	@Getter
	private static final Map<ComPortConnection<?>, Set<IReceiveMessage<?>>> receivers = new HashMap<>();
	
	public static String getDateString()
	{
		return DateTimeFormatter.ofPattern("yyyy-MM-dd")
		                        .format(com.entityassist.RootEntity.getNow());
	}
	
	@JsonIgnore
	private static final EvictingQueue<String> lastMessages = com.google.common.collect.EvictingQueue.create(100);
	
	/**
	 * Thread control between read and write
	 *
	 * @param message
	 * @param in
	 */
	public synchronized void writeOrReadMessage(String message, boolean in)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		if (in)
		{
			getLog().log(Level.FINE, "[" + sdf.format(new Date()) + "]-[" + comPort + "] RX : " + message);
			var comporttxrxlogger = Log4j2Utils.createLog4j2RollingLog("comport_" + getComPort(),
					getDateString() + "rxtx_comport_" + getComPort(), "%d{ABSOLUTE} %m%n", "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
			comporttxrxlogger.info("RX " + message);
			if (lastMessages.stream()
			                .anyMatch(a -> a.equals(message.trim())))
			{
				//	System.out.println("Connection null - com port - " + comPort);
			}
			else
			{
				lastMessages.add(message);
				processMessage(message);
			}
		}
		else
		{
			try
			{
				if (!Strings.isNullOrEmpty(message.trim()))
				{
					if (outs == null)
					{
					
					}
					else
					{
						outs.write((message + "\r\n").getBytes(StandardCharsets.UTF_8));
						getLog().log(Level.FINE, "[" + sdf.format(new Date()) + "]-[" + comPort + "] TX : " + message);
						var comporttxrxlogger = Log4j2Utils.createLog4j2RollingLog("comport_" + getComPort(),
								getDateString() + "rxtx_comport_" + getComPort(), "%d{ABSOLUTE} %m%n", "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
						comporttxrxlogger.info("TX " + message);
						
						var comportlogger = Log4j2Utils.createLog4j2RollingLog("comport_" + getComPort(),
								getDateString() + "_comport_" + getComPort(), "%m%n", "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
						comportlogger.info(message);
						
						outs.flush();
					}
				}
			}
			catch (Exception e)
			{
				var comportlogger = Log4j2Utils.createLog4j2RollingLog("comport_send_error_" + getComPort(),
						getDateString() + "_comport_send_error_" + getComPort(), "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
				comportlogger.info(message);
				getLog().log(Level.SEVERE, "[" + sdf.format(new Date()) + "]-[" + comPort + "] TX : " + message);
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void processMessage(String message)
	{
		lastMessageReceivedTime = LocalDateTime.now();
		
		if (getComPortStatus() != Idle || getComPortStatus() != Running)
		{
			setComPortStatus(Running);
		}
		
		var rawlogger = Log4j2Utils.createLog4j2RollingLog("rawreceive_comport_" + getComPort(),
				getDateString() + "_rawreceive_comport_" + getComPort(), "%m%n", "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
		rawlogger.info(message);
		
		Set<ICleanReceivedMessage> cleanMessages = GuiceContext.instance()
		                                                       .getLoader(ICleanReceivedMessage.class, ServiceLoader.load(ICleanReceivedMessage.class));
		
		for (ICleanReceivedMessage<?> messageReceiver : cleanMessages)
		{
			message = messageReceiver.cleanMessage(message, me);
		}
		if (Strings.isNullOrEmpty(message.trim()))
		{
			return;
		}
		
		var cleanlogger = Log4j2Utils.createLog4j2RollingLog("receive_comport_" + getComPort(),
				getDateString() + "_receive_comport_" + getComPort(), "%m%n", "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
		cleanlogger.info(message);
		
		var comporttxrxlogger = Log4j2Utils.createLog4j2RollingLog("comport_" + getComPort(),
				getDateString() + "rxtx_comport_" + getComPort(), "%d{ABSOLUTE} %m%n", "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
		comporttxrxlogger.info("RX " + message);
		
		var comportlogger = Log4j2Utils.createLog4j2RollingLog("comport_" + getComPort(),
				getDateString() + "_comport_" + getComPort(), "%m%n", "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
		comportlogger.info(message);
		
		if (!Strings.isNullOrEmpty(message))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
			getLog().fine("[" + sdf.format(new Date()) + "]-[" + comPort + "] RX : " + message);
			for (IReceiveMessage<?> messageReceiver : getReceivers())
			{
				messageReceiver.receiveMessage(message, me);
			}
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void processMessageException(String message, Throwable T)
	{
		Set<IErrorReceiveMessage> messageReceivers = GuiceContext.instance()
		                                                         .getLoader(IErrorReceiveMessage.class, ServiceLoader.load(IErrorReceiveMessage.class));
		for (IErrorReceiveMessage<?> messageReceiver : messageReceivers)
		{
			messageReceiver.receiveErrorMessage(message, T, me);
		}
		var comporterrorlogger = Log4j2Utils.createLog4j2RollingLog("comport_error_" + getComPort(),
				getDateString() + "_comport_error_" + getComPort(), "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
		comporterrorlogger.fatal(message, T);
		
		getLog().log(Level.FINE, "Message Exception", T);
		getLog().log(Level.SEVERE, "Error in receiving string from COM-port: " + T + " - " + message, T);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void processMessageTerminal(String message, Throwable T)
	{
		var comporterrorlogger = Log4j2Utils.createLog4j2RollingLog("comport_error_" + getComPort(),
				getDateString() + "_comport_error_" + getComPort(), "%d{yy-MM-dd}", org.apache.logging.log4j.Level.INFO);
		comporterrorlogger.fatal(message, T);
		
		Set<ITerminalReceiveMessage> messageReceivers = GuiceContext.instance()
		                                                            .getLoader(ITerminalReceiveMessage.class, ServiceLoader.load(ITerminalReceiveMessage.class));
		for (ITerminalReceiveMessage<?> messageReceiver : messageReceivers)
		{
			messageReceiver.receiveTerminalMessage(message, T, me);
		}
	}
	
	/**
	 * Returns this logger for this comport messaging
	 *
	 * @return
	 */
	@JsonIgnore
	public Logger getLog()
	{
		if (log == null)
		{
			log = Logger.getLogger(getLogName());
		}
		return log;
	}
	
	public String getLogName()
	{
		return logName;
	}
	
	public boolean isSimulated()
	{
		return simulated;
	}
	
	/**
	 * Returns the last message received time or null
	 *
	 * @return
	 */
	public LocalDateTime getLastMessageReceivedTime()
	{
		return lastMessageReceivedTime;
	}
	
	/**
	 * Sets the last time received for status monitoring check
	 *
	 * @param lastMessageReceivedTime
	 * @return
	 */
	public ComPortConnection<J> setLastMessageReceivedTime(LocalDateTime lastMessageReceivedTime)
	{
		this.lastMessageReceivedTime = lastMessageReceivedTime;
		return this;
	}
	
	public Pattern getMatchingPattern()
	{
		return matchingPattern;
	}
	
	public ComPortConnection<J> setMatchingPattern(Pattern matchingPattern)
	{
		this.matchingPattern = matchingPattern;
		return this;
	}
}
