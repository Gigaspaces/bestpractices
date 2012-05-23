package org.openspaces.ece.scalingmodule;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ScaleWorker {
    InputStream in;
    @Parameter(names = {"-l", "--locator"})
    String locator = "127.0.0.1";
    @Parameter(names = {"-g", "--group"})
    String group = "Gigaspaces-XAPPremium-8.0.3-ga";
    @Parameter(names = {"-n", "--name"})
    String processingUnitName = "ece-worker";
    @Parameter(names = {"-m"})
    Integer initialWorkers = 2;

    Admin admin;
    GridServiceManager gsm;
    ProcessingUnit pu = null;

    public ScaleWorker(InputStream in) {
        this.in = in;
    }

    public static void main(String... args) {
        ScaleWorker scaleWorker = new ScaleWorker(System.in);
        new JCommander(scaleWorker, args);
        scaleWorker.init();
        scaleWorker.run();
        scaleWorker.close();
    }

    private void close() {
        admin.close();
    }

    private void init() {
        admin = new AdminFactory().addGroup(group).addLocator(locator).createAdmin();
        System.out.println(admin);
        GridServiceAgents agents = admin.getGridServiceAgents();
        System.out.println(agents);
        agents.waitForAtLeastOne(10, TimeUnit.SECONDS);
        ElasticServiceManagers esms = admin.getElasticServiceManagers();
        System.out.println(esms);
        esms.waitForAtLeastOne(10, TimeUnit.SECONDS);

        gsm = admin.getGridServiceManagers().waitForAtLeastOne(10, TimeUnit.SECONDS);
        ProcessingUnits pus = admin.getProcessingUnits();
        System.out.println(pus);
        pu = pus.waitFor(processingUnitName, 5, TimeUnit.SECONDS);
        System.out.printf("Processing unit (null is acceptable): %s%n", pu);
        if (pu == null) {

            pu = gsm.deploy(new ElasticStatelessProcessingUnitDeployment(processingUnitName)
                    //initial scale
                    .scale(
                            new ManualCapacityScaleConfigurer()
                                    // .numberOfCpuCores(1)
                                    .create())
            );
            try {
                monitorPUScaleProgress(pu, 1);
            } catch (Exception ignored) {
            }
            try {
                scale(pu, initialWorkers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            initialWorkers = getPUWorkerCount(pu);
        }
    }

    void monitorPUScaleProgress(ProcessingUnit pu, int targetCapacity) throws Exception {
        int totalWorkers = getPUWorkerCount(pu);

        while (totalWorkers != targetCapacity) {
            //int totalGSCs = pu.getAdmin().getGridServiceContainers().getSize();
            System.out.printf("Target Workers: %d - Total Workers: %d%n", targetCapacity, totalWorkers);
            Thread.sleep(2000);
            totalWorkers = getPUWorkerCount(pu);
        }
    }

    private int getPUWorkerCount(ProcessingUnit pu) {
        return pu.getNumberOfInstances();
    }

    private void run() {
        Scanner scanner = new Scanner(in);
        String input;
        do {
            System.out.printf("+,-,quit input> ");
            System.out.flush();
            input = scanner.next();
            processInput(input);
        } while (!input.equals("quit"));
    }

    private void processInput(String input) {
        if (!input.equals("quit")) {
            boolean scale = handleChangeRequest(input);
            try {
                if (scale) {
                    scale(pu, initialWorkers);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean handleChangeRequest(String input) {
        boolean scale = true;
        if (input.equals("+")) {
            initialWorkers++;
        } else {
            if (input.equals("-")) {
                if (initialWorkers > 1) {
                    initialWorkers--;
                } else {
                    scale = false;
                }
            } else {
                scale = false;
            }
        }
        return scale;
    }

    void scale(ProcessingUnit pu, int targetCapacity) throws Exception {
        long startTime;
        // checking deployed PU.
        System.out.printf("scaling worker nodes from from %4d to %4d%n",
                getPUWorkerCount(pu), targetCapacity);
        startTime = System.currentTimeMillis();
        if (getPUWorkerCount(pu) > targetCapacity) {
            pu.decrementInstance();
        } else {
            pu.incrementInstance();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Worker capacity change done!%nTime to scale system: %f seconds%n", (endTime - startTime) / 1000.0);

    }
}
