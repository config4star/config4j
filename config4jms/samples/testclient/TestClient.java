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

package testclient;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4jms.Config4JMS;
import org.config4jms.Config4JMSException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;


public class TestClient
{
	private Config4JMS			jms;
	private Configuration		cfg;
	private HashMap				listeners;
	private MessageFormatter	msgFormatter;
	private String[]			consumerNames;
	private String[]			producerNames;
	private String[]			qBrowserNames;
	private static boolean		wantConsumers;
	private static boolean		wantProducers;
	private static boolean		wantQueueBrowsers;


	private void doWork(
		String				cfgFile,
		String				scope,
		HashMap				cfgPresets)
    {
		MessageProducer		producer;
		MessageConsumer		consumer;
		Session				session;
		String				config4JMSScope;
		String				appScope;
		int					i;
		Object				obj;
		MessageListener		listener;
		Thread				thread;

		config4JMSScope = scope + ".config4jms";
		appScope        = scope + ".config4app";
        try {
			//--------
			// Parse configuration.
			//--------
			jms = Config4JMS.create(cfgFile, config4JMSScope, cfgPresets);
			cfg = jms.getConfiguration();

			msgFormatter = new MessageFormatter(cfg, cfg.mergeNames(appScope,
												"MessageFormatter"));

			//--------
			// Create JMS and application objects
			//--------
			jms.createJMSObjects();

			if (wantConsumers) {
				//--------
				// Create and register a message listener for each
				// MessageConsumer.
				//--------
				consumerNames = jms.listMessageConsumerNames();
				for (i = 0; i < consumerNames.length; i++) {
					listener = new MessageListener(jms, cfg, appScope,
												consumerNames[i], msgFormatter);
					consumer = jms.getMessageConsumer(consumerNames[i]);
					consumer.setMessageListener(listener);
				}
			}

			if (wantProducers) {
				//--------
				// TODO: create threads for each MessageProducer
				//--------
				producerNames = jms.listMessageProducerNames();
				for (i = 0; i < producerNames.length; i++) {
					thread = new ProducerThread(jms, cfg, appScope,
												producerNames[i], msgFormatter);
					thread.start();
				}
			}

			if (wantQueueBrowsers) {
				//--------
				// TODO: create threads for each QueueBrowser
				//--------
				qBrowserNames = jms.listQueueBrowserNames();
				for (i = 0; i < qBrowserNames.length; i++) {
					thread = new QueueBrowserThread(jms, cfg, appScope,
												qBrowserNames[i], msgFormatter);
					thread.start();
				}
			}

			jms.startConnections();

			System.out.println("Created message listeners. Sleeping...");
			Thread.sleep(1000 * 60 * 60);
			jms.stopConnections();
			System.exit(0);
        } catch (ConfigurationException ex) {
            System.err.println(ex.getMessage());
			System.exit(1);
		} catch (JMSException ex) {
            ex.printStackTrace();
			System.exit(1);
		} catch (Exception ex) {
            ex.printStackTrace();
			System.exit(1);
        }
    }


    /**
	 * Main program entry point.
	 * */
    public static void main(String argv[])
	{
        String					cfgFile = null;
        String					scope = null;
		HashMap					cfgPresets = new HashMap();
		String					arg;
		int						i;

		wantConsumers = false;
		wantProducers = false;
		wantQueueBrowsers = false;
        for (i = 0; i < argv.length; i++) {
            arg = argv[i];
            if (arg.equals("-cfg")) {
                if (i == argv.length - 1) {
                    System.exit(1);
                }
                cfgFile = argv[i+1];
				i++;
            } else if (arg.equals("-scope")) {
                if (i == argv.length - 1) {
                    System.exit(1);
                }
                scope = argv[i+1];
				i++;
            } else if (arg.equals("-set")) {
                if (i >= argv.length - 2) {
                    System.exit(1);
                }
                cfgPresets.put(argv[i+1], argv[i+2]);
				i += 2;
            } else if (arg.equals("-consumers")) {
				wantConsumers = true;
            } else if (arg.equals("-producers")) {
				wantProducers = true;
            } else if (arg.equals("-queueBrowsers")) {
				wantQueueBrowsers = true;
            } else if (arg.equals("-all")) {
				wantConsumers = true;
				wantProducers = true;
				wantQueueBrowsers = true;
            } else {
				System.out.println("\nError processing argument/option '"
								   + arg + "'");
                printUsage();
                System.exit(1);
            }
        }
        if (cfgFile == null) {
            System.err.println ("error: You must specify -cfg <file>");
            printUsage();
            System.exit(1);
        }
        if (scope == null) {
            System.err.println ("error: You must specify -scope <scope>");
            printUsage();
            System.exit(1);
        }
		if (wantConsumers == false && wantProducers == false
			&& wantQueueBrowsers == false)
		{
            System.err.println ("error: You must specify at least one of: "
							+ "-consumers, -producers, -queueBrowsers -all");
            printUsage();
            System.exit(1);
		}

        TestClient client = new TestClient();
        client.doWork(cfgFile, scope, cfgPresets);
    }

    /**
	 * Prints the usage.
	 */
    private static void printUsage()
	{
		String		className = (new TestClient()).getClass().getName();

        StringBuffer use = new StringBuffer();
        use.append("usage: java " + className + " (options)\n");
        use.append("options:\n");
        use.append("  -cfg <file.cfg>\n");
        use.append("  -scope <scope> (default is the global scope)\n");
        use.append("  -set <name> <value>\n");
        use.append("  -producers\n");
        use.append("  -consumers\n");
        use.append("  -queueBrowsers\n");
        use.append("  -all\n");
        use.append("  -h           This usage message.\n");
        System.err.println (use);
    }

}
