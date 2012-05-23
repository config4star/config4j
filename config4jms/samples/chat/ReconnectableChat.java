//-----------------------------------------------------------------------
// Copyright 2011 Ciaran McHale.
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions.
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.  
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
// BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
// ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//----------------------------------------------------------------------

package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.HashMap;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.config4jms.Config4JMS;


public class ReconnectableChat implements MessageListener, ExceptionListener
{
	private String				cfgFile;      // set by parseCommandLineArgs()
	private String				cfgScope;     // set by parseCommandLineArgs()
	private HashMap				cfgPresets;   // set by parseCommandLineArgs()
	private int					retrySeconds; // set by parseCommandLineArgs()
	private Config4JMS			jms;
	private MessageProducer		producer;
    private boolean				inSetup = false;


    public static void main(String[] args)
	{
        ReconnectableChat app = new ReconnectableChat();
		app.parseCommandLineArgs(args);
		app.createJMSObjects();
        app.produceMessages();
		app.exit(0);
	}


	private synchronized void createJMSObjects()
    {
		String[]		names;
		boolean			amStartingUp = false;
		boolean			firstReconnectionAttempt;
		int				i;

		inSetup = true;

		//--------
		// The first time we are called, we parse the config file
		// and verify that the named objects used by this application
		// are defined to be of the correct types.
		//--------
		if (jms == null) {
			amStartingUp = true;
			try {
				jms = Config4JMS.create(cfgFile, cfgScope, cfgPresets,
										new String[] {
											"MessageConsumer", "chatConsumer",
											"MessageProducer", "chatProducer",
											"TextMessage",     "chatMessage"});
			} catch(JMSException ex) {
				//System.err.println(ex.toString());
				ex.printStackTrace();
				exit(1);
			}
		}

		//--------
		// Keep looping until we successfully (re)establish our JMS
		// connection(s) and (re)create the JMS objects.
		//--------
		firstReconnectionAttempt = true;
		while (true) {
			try {
				jms.closeConnections();
			} catch(JMSException ex) {
			}

			try {
				jms.createJMSObjects();
				producer = jms.getMessageProducer("chatProducer");
				jms.getMessageConsumer("chatConsumer").setMessageListener(this);
				jms.setExceptionListener(this);
				jms.startConnections();
				break;
			} catch (JMSException ex) {
				if (!jms.isNoConnection(ex)) {
					System.err.println(ex.toString());
					//ex.printStackTrace();
					exit(1);
				}
				if (firstReconnectionAttempt) {
					System.err.println(ex.toString() + "\n");
					firstReconnectionAttempt = false;
				}
                System.out.println("Pausing " + retrySeconds + " seconds "
							+  "before retrying to establish connection...");
				try {
					Thread.sleep(retrySeconds * 1000);
				} catch (java.lang.InterruptedException ex2) {
				}
			}
		}

		//--------
		// Tell the user that everything is ready.
		//--------
		if (!amStartingUp) {
			System.out.println("Connection reestablished.");
		}
		System.out.println("\nEnter text messages. Press Enter to publish "
						   + "each message.\n");
		inSetup = false;
    }


	private synchronized MessageProducer getProducer()
	{
		return producer;
	}


	private synchronized TextMessage createChatMessage() throws JMSException
	{
		return (TextMessage)jms.createMessage("chatMessage");
	}


	private void produceMessages()
    {
		TextMessage			msg;
		BufferedReader		stdin;
		String				line;
		String				username;

		//--------
		// Loop: read standard input and send it as a message.
		// Return when stdin finishes.
		//--------
        try {
			username = (String)cfgPresets.get("username");
			if (username == null) {
				username = "anonymous";
			}
            stdin = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                line = stdin.readLine();
                if (line == null) {
					return;
				} else if (line.length() > 0) {
					try {
						msg = createChatMessage();
						msg.setText(username + ": " + line);
						getProducer().send(msg);
					} catch (JMSException ex) {
						System.err.println(ex.toString());
					}
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.toString());
			exit(1);
        }
    }


    public void onMessage(Message msg)
    {
		TextMessage		textMessage;

        try {
			textMessage = (TextMessage)msg;
			System.out.println(textMessage.getText());
			textMessage.acknowledge();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


	public void onException (JMSException ex)
	{
		System.err.println ("\n" + ex.getMessage());
		if (jms.isNoConnection(ex) && !inSetup) {
			createJMSObjects();
		}
	}


    private void exit(int exitCode)
    {
        try {
			if (jms != null) {
				jms.closeConnections();
			}
        } catch(JMSException ex) {
            System.err.println(ex.toString());
        }
        System.exit(exitCode);
    }


    public void parseCommandLineArgs(String[] args)
	{
		String					arg;
		int						i;

        cfgFile = null;
        cfgScope = "";
		cfgPresets = new HashMap();
		retrySeconds = 5;

        for (i = 0; i < args.length; i++) {
            arg = args[i];
            if (arg.equals("-cfg")) {
                if (i == args.length - 1) {
                    exit(1);
                }
                cfgFile = args[i+1];
				i++;
            } else if (arg.equals("-retryInterval")) {
                if (i == args.length - 1) {
                    exit(1);
                }
				try {
					retrySeconds = Integer.parseInt(args[i+1]);
				} catch(NumberFormatException ex) {
					printUsage();
					exit(1);
				}
				i++;
            } else if (arg.equals("-scope")) {
                if (i == args.length - 1) {
                    exit(1);
                }
                cfgScope = args[i+1];
				i++;
            } else if (arg.equals("-u")) {
                if (i == args.length - 1) {
                    exit(1);
                }
                cfgPresets.put("username", args[i+1]);
				i++;
            } else if (arg.equals("-p")) {
                if (i == args.length - 1) {
                    exit(1);
                }
                cfgPresets.put("password", args[i+1]);
				i++;
            } else if (arg.equals("-set")) {
                if (i >= args.length - 2) {
                    exit(1);
                }
                cfgPresets.put(args[i+1], args[i+2]);
				i += 2;
            } else {
                printUsage();
                exit(1);
            }
        }
        if (cfgFile == null) {
            System.err.println ("error: You must specify -cfg <file>");
            printUsage();
            exit(1);
        }
    }


    private static void printUsage()
	{
		String		className = ReconnectableChat.class.getName();

        StringBuffer use = new StringBuffer();
        use.append("usage: java " + className + " (options)\n");
        use.append("options:\n");
        use.append("  -cfg <file.cfg>\n");
        use.append("  -scope <scope> (default is the global scope)\n");
        use.append("  -set <name> <value> (preset name=value in ")
			.append("configuration)\n");
        use.append("  -u <username>    (short for -set username <username>)\n");
        use.append("  -p <password>    (short for -set password <password>)\n");
        use.append("  -retryInterval <seconds>\n");
        use.append("  -h                This usage message.\n");
        System.err.println (use);
    }

}
