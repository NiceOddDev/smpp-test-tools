package com.a1systems.smpp.simulator;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final Logger scriptLogger = LoggerFactory.getLogger("ScriptLogger");

    protected ScriptEngine scriptEngine = null;

    protected Invocable invocableEngine = null;

    protected Simulator simulator;

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public Invocable getInvocableEngine() {
        return invocableEngine;
    }

    public Simulator getSimulator() {
        return simulator;
    }

    public void setSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    public void run(String[] args) throws SmppChannelException, IOException {
        CliConfig cfg = null;

        ScheduledExecutorService asyncPool = Executors.newScheduledThreadPool(5);

        try {
            cfg = parseArgumets(args);

            simulator = new Simulator();

            simulator.setScheduledExecutor(asyncPool);

            if (cfg.getFileName() != null) {
                ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

                scriptEngine = scriptEngineManager.getEngineByName("JavaScript");

                invocableEngine = (Invocable)scriptEngine;

                Map<String, String> cfgMap = cfg.getMap();

                scriptEngine.put("argumentMap", cfgMap);

                if (!cfgMap.isEmpty()) {
                    for (String key:cfgMap.keySet()) {
                        scriptEngine.put(key, cfg.getMap().get(key));
                    }
                }

                scriptEngine.put("Logger", scriptLogger);

                Object eval = scriptEngine.eval(new FileReader(cfg.getFileName()));
            }

            SmppServerConfiguration configuration = new SmppServerConfiguration();
            configuration.setPort(cfg.getPort());
            configuration.setMaxConnectionSize(10);
            configuration.setNonBlockingSocketsEnabled(true);
            configuration.setDefaultRequestExpiryTimeout(30000);
            configuration.setDefaultWindowMonitorInterval(15000);
            configuration.setDefaultWindowSize(500);
            configuration.setDefaultWindowWaitTimeout(configuration.getDefaultRequestExpiryTimeout());
            configuration.setDefaultSessionCountersEnabled(true);
            configuration.setJmxEnabled(true);

            // create a server, start it up
            DefaultSmppServer smppServer = new DefaultSmppServer(configuration, new DefaultSmppServerHandler(this, asyncPool));

            logger.info("Starting SMPP server...");
            smppServer.start();
        } catch (CmdLineException ex) {
            logger.error("{}", ex.getMessage());

            CmdLineParser p = new CmdLineParser(new CliConfig());

            p.printUsage(System.out);

            System.exit(1);
        } catch (ScriptException ex) {
            logger.error("Script engine error on loading file: {}", ex.getMessage());

            System.exit(1);
        }
    }

    public CliConfig parseArgumets(String args[]) throws CmdLineException {
        CliConfig cfg = new CliConfig();

        CmdLineParser parser = new CmdLineParser(cfg);

        parser.parseArgument(args);

        validateArguments(cfg, parser);

        return cfg;
    }

    public void validateArguments(CliConfig cliConfig, CmdLineParser parser) throws CmdLineException{
        if (
            cliConfig.getFileName() != null
            && !cliConfig.getFileName().isEmpty()
        ) {
            File f = new File(cliConfig.getFileName());

            if (!f.exists()) {
                throw new CmdLineException(parser, String.format("File %s does not exist", cliConfig.getFileName()));
            }

            if (f.isDirectory()) {
                throw new CmdLineException(parser, String.format("File %s does not regular file", cliConfig.getFileName()));
            }

            if (!f.canRead()) {
                throw new CmdLineException(parser, String.format("File %s does not have read permissions", cliConfig.getFileName()));
            }
        }
    }
}