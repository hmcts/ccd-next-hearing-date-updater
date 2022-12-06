package uk.gov.hmcts.reform.next.hearing.date.updater;

import java.util.Scanner;

class ContainersShutdownListener implements Runnable {

    protected static final String EXIT_COMMAND = "stop";

    private boolean running = false;
    private boolean exit = false;

    @Override
    public void run() {

        running = true;
        Scanner scanner = new Scanner(System.in);

        while (!exit) {
            // set the exit flag once EXIT_COMMAND is entered
            if (EXIT_COMMAND.equalsIgnoreCase(scanner.nextLine())) {
                exit = true;
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean shouldExit() {
        return exit;
    }
}

