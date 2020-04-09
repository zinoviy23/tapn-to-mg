package com.github.zinoviy23;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "tapn-to-mg",
    description = "Converts Timed Arc Petri Net to Metric Graph and vice versa.",
    version = "tapn-to-mg 0.0"
)
public class TapnToMgApp implements Callable<Void> {
  @CommandLine.Parameters(index = "0", description = "Your name")
  private String name;

  @SuppressWarnings("FieldMayBeFinal")
  @CommandLine.Option(names = {"-u", "--upper-case"}, description = "Print with uppercase?")
  private boolean upperCase = false;

  public static void main(String[] args) {
    var exitCode = new CommandLine(new TapnToMgApp()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Void call() {
    String res = "Hello, " + name;

    System.out.println(upperCase ? res.toUpperCase() : res);

    return null;
  }
}
