package net.spoolmc;

import net.minestom.server.entity.Player;
import net.spoolmc.logger.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

public class ScriptingEngine {
    private final Logger logger = new Logger("ScriptingEngine");

    ScriptingEngine() {
        logger.info("Compiling Script...");



        try (Context context = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).build()) {
            try {

                context.getBindings("js").putMember("Player", Player.class);

                logger.info("Finished Setting up engine");

                Value jsFunction = context.eval("js", """
                        () => {
                            return 51+5;
                        }""");

                logger.info("Parsed script...");

                long startTime = System.nanoTime();

                System.out.println(jsFunction.execute());

                long endTime = System.nanoTime();
                long executionTime = (endTime - startTime) / 1_000_000;

                logger.info("Execution took " + executionTime + " milliseconds");

            } catch (PolyglotException e) {
                System.err.println("Error occurred: " + e.getMessage());
            }


        }
    }
}
