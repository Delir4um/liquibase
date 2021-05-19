package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.integration.IntegrationConfiguration;
import liquibase.util.StringUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

class CommandRunner implements Callable<CommandResults> {

    private CommandLine.Model.CommandSpec spec;

    @Override
    public CommandResults call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add(spec.commandLine().getCommandName());

        CommandLine parentCommand = spec.commandLine().getParent();
        while (!parentCommand.getCommandName().equals("liquibase")) {
            command.add(0, parentCommand.getCommandName());
            parentCommand = parentCommand.getParent();
        }
        final CommandLine rootCommand = parentCommand;


        final String[] commandName = LiquibaseCommandLine.getCommandNames(spec.commandLine());
        for (int i=0; i<commandName.length; i++) {
            commandName[i] = StringUtil.toCamelCase(commandName[i]);
        }

        final CommandScope commandScope = new CommandScope(commandName);
        final File outputFile = IntegrationConfiguration.OUTPUT_FILE.getCurrentValue();
        OutputStream outputStream = null;

        try {
            if (outputFile != null) {
                outputStream = new FileOutputStream(outputFile);
                commandScope.setOutput(outputStream);
            }

            return commandScope.execute();
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }


    private String toCommandArgumentDefinition(CommandLine.Model.OptionSpec option) {
        final String argName = option.names()[0];
        return StringUtil.toCamelCase(argName.replaceFirst("^--", ""));
    }

    public void setSpec(CommandLine.Model.CommandSpec spec) {
        this.spec = spec;
    }
}
