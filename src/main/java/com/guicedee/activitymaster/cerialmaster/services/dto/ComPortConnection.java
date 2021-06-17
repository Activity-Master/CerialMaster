package com.guicedee.activitymaster.cerialmaster.services.dto;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.cerialmaster.services.*;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.plugins.quickforms.annotations.*;
import com.jwebmp.plugins.quickforms.annotations.states.*;
import gnu.io.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		extends NRSerialPort
		implements Serializable,Comparable<J>
{
	@Serial
	private static final long serialVersionUID = 1L;
	
	private UUID id;
	
	private String logName = "";
	private transient Logger log;
	
	public static final EnumSet<ComPortStatus> onlineServerStatus = EnumSet.of(Simulation, Idle, Logging,OperationInProgress, Running, Silent, FileTransfer);
	public static final String COM_NAME = "COM";
	
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
	private ComPortStatus status = Offline;
	
	@WebIgnore
	@WebField(classes = "col-12 col-md-3")
	@LabelField(value = "Type", classes = "col-12 col-md-3")
	@SelectField
	private ComPortType type;
	
	@WebReadOnly
	@WebIgnore
	private int comPort;
	
	@JsonIgnore
	private IResourceItem<?,?> resourceItem;
	
	@JsonIgnore
	private transient PortReader reader;
	
	private transient boolean reading;
	
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
			//startOfMessageCharacters.add((char) 1);
			//startOfMessageCharacters.add((char) 2);
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
	
	public void open()
	{
		if (endOfMessageCharacters.isEmpty())
		{
			throw new RuntimeException("Tried to open a com connection without an end of line character set?");
		}
		try
		{
			connect();
			setStatus(Silent);
		}
		catch (NRSerialPortException nre)
		{
			switch (nre.getMessage())
			{
				case "No Port":
					setStatus(Missing);
					return;
				case "Port in Use":
					setStatus(InUse);
					return;
				default:
					getLog().log(Level.SEVERE, "Unknown Exception", nre);
					setStatus(GeneralException);
					return;
			}
		}
		//	getSerialPortInstance().setDTR(true);
		//	getSerialPortInstance().setRTS(true);
		getSerialPortInstance().disableReceiveFraming();
			getSerialPortInstance().disableRs485();
			getSerialPortInstance().disableReceiveFraming();
			getSerialPortInstance().disableReceiveThreshold();
			
		notifyOnDataAvailable(true);
		getSerialPortInstance().notifyOnDataAvailable(true);
	/*	getSerialPortInstance().notifyOnBreakInterrupt(true);
		getSerialPortInstance().notifyOnFramingError(true);
		getSerialPortInstance().notifyOnOutputEmpty(true);
		getSerialPortInstance().notifyOnCarrierDetect(true);
		getSerialPortInstance().notifyOnParityError(true);
		getSerialPortInstance().notifyOnDSR(true);
		getSerialPortInstance().notifyOnCTS(true);*/
		//	getSerialPortInstance().notifyOnOverrunError(true);
	/*	try
		{
			getSerialPortInstance().enableReceiveFraming('\n');
			
			getSerialPortInstance().enableReceiveTimeout(3500);
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
		getSerialPortInstance().enableReceiveThreshold(50);
		getSerialPortInstance().setInputBufferSize(512000);
		getSerialPortInstance().setOutputBufferSize(512000);
		getSerialPortInstance().setFlowControlMode(FLOWCONTROL_NONE);
		//getSerialPortInstance().setFlowControlMode(FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT);
		
		ins = new DataInputStream(getInputStream());
		outs = new DataOutputStream(getOutputStream());
		
	/*	ins = new BufferedInputStream(new DataInputStream(getInputStream()));
		outs = new BufferedOutputStream(new DataOutputStream(getOutputStream()));*/
		
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
		try
		{
			addEventListener(reader = new PortReader());
		}
		catch (TooManyListenersException e)
		{
			getLog().log(Level.SEVERE, "Reader has too many listeners", e);
			processMessageTerminal("Reader has too many listeners", e);
		}
	}
	
	public void close()
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
				disconnect();
			}
			catch (Throwable T1)
			{
			
			}
		}
		setStatus(ComPortStatus.Offline);
	}
	
	/**
	 * Thread control between read and write
	 * @param message
	 * @param in
	 */
	private void writeOrReadMessage(String message, boolean in)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		if (in)
		{
			processMessage(message);
		}
		else
		{
			try
			{
				outs.write(message.getBytes(StandardCharsets.UTF_8));
				getLog().log(Level.INFO, "[" + sdf.format(new Date())+ "]-[" + comPort + "] TX : " + message);
				outs.flush();
			}
			catch (Exception e)
			{
				getLog().log(Level.SEVERE, "[" + sdf.format(new Date())+ "]-["+ comPort + "] TX : " + message);
				e.printStackTrace();
			}
		}
	}
	
	public void writeMessage(ServerMessage<?> sm)
	{
		try
		{
		//	getLog().log(Level.INFO, "[" + comPort + "] TX : " + sm.generateMessage());
			writeOrReadMessage(sm.generateMessage(), false);
		//	outs.write(sm.generateMessage().getBytes(StandardCharsets.UTF_8));
		//	outs.flush();
		}
		catch (Exception e)
		{
			getLog().log(Level.SEVERE, "Unable to write message - " + sm.generateMessage(), e);
			processMessageTerminal(e.getMessage(), e);
			close();
			setStatus(GeneralException);
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public J setStatus(ComPortStatus status)
	{
		ComPortStatus currentStatus = this.status;
		this.status = status;
		if (currentStatus != this.status)
		{
			Set<IComPortStatusChanged> cleanMessages = GuiceContext.instance()
			                                                       .getLoader(IComPortStatusChanged.class, ServiceLoader.load(IComPortStatusChanged.class));
			for (IComPortStatusChanged<?> comPortStatusChangeReceiver : cleanMessages)
			{
				comPortStatusChangeReceiver.onComPortStatusChanged(me, currentStatus, this.status);
			}
		}
		return (J) this;
	}
	
	@SuppressWarnings({"unchecked"})
	public J setStatus(ComPortStatus status, boolean noOperations)
	{
		this.status = status;
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
	
	public class PortReader
			implements SerialPortEventListener
	{
		private StringBuffer readBuffer = new StringBuffer();
		@Override
		public void serialEvent(SerialPortEvent event)
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
				close();
				setStatus(GeneralException);
			}
			else if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE && event.getNewValue())
			{
				try
				{
					reading = true;
					if (ins.available() > 0)
					{
						int i;
					//	System.out.println("-----");
						while ((i = ins.read()) != -1)
						{
							char c = (char) i;
						//	System.out.print(c);
							if (Character.isAlphabetic(c) ||
							    Character.isDigit(c) ||
							    allowedCharacters.contains(c) ||
							    startOfMessageCharacters.contains(c))
							{
								readBuffer.append(c);
								if (!startOfMessageCharacters.isEmpty())
								{
									boolean startCorrect = false;
									for (Character startOfMessageCharacter : startOfMessageCharacters)
									{
										if (readBuffer.toString()
										              .startsWith(startOfMessageCharacter + ""))
										{
											startCorrect = true;
											break;
										}
									}
									if (!startCorrect)
									{
										readBuffer = new StringBuffer();
									}
								}
							}
							if (endOfMessageCharacters.contains(c))
							{
								try
								{
									writeOrReadMessage(readBuffer.toString(), true);
								//	processMessage(readBuffer.toString());
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
					//	System.out.println("------------");
					//	System.out.print(readBuffer.toString());
					}
				}
				catch (IOException e)
				{
					getLog().log(Level.SEVERE, "Cannot read com port, IO Error", e);
				}
				finally
				{
					reading = false;
				}
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void processMessage(String message)
	{
		Set<ICleanReceivedMessage> cleanMessages = GuiceContext.instance()
		                                                       .getLoader(ICleanReceivedMessage.class, ServiceLoader.load(ICleanReceivedMessage.class));
		for (ICleanReceivedMessage<?> messageReceiver : cleanMessages)
		{
			message = messageReceiver.cleanMessage(message, me);
		}
		if(!Strings.isNullOrEmpty(message))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
			getLog().config("[" + sdf.format(new Date()) + "]-[" + comPort + "] RX : " + message);
			Set<IReceiveMessage> receiveMessages = GuiceContext.instance()
			                                                   .getLoader(IReceiveMessage.class, ServiceLoader.load(IReceiveMessage.class));
			for (IReceiveMessage<?> messageReceiver : receiveMessages)
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
		getLog().log(Level.FINE, "Message Exception", T);
		getLog().log(Level.SEVERE, "Error in receiving string from COM-port: " + T + " - " + message);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void processMessageTerminal(String message, Throwable T)
	{
		Set<ITerminalReceiveMessage> messageReceivers = GuiceContext.instance()
		                                                            .getLoader(ITerminalReceiveMessage.class, ServiceLoader.load(ITerminalReceiveMessage.class));
		for (ITerminalReceiveMessage<?> messageReceiver : messageReceivers)
		{
			messageReceiver.receiveTerminalMessage(message, T, me);
		}
		getMe().close();
		getMe().setStatus(ComPortStatus.Offline);
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
}
