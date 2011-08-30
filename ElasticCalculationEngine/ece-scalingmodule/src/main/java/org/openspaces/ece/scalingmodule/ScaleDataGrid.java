package org.openspaces.ece.scalingmodule;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
import org.openspaces.admin.space.ElasticSpaceDeployment;
import org.openspaces.core.util.MemoryUnit;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ScaleDataGrid {
    InputStream in;
    @Parameter(names = {"-l", "--locator"})
    String locator = "127.0.0.1";
    @Parameter(names = {"-g", "--group"})
    String group = "Gigaspaces-XAPPremium-8.0.3-rc";
    @Parameter(names = {"-n", "--name"})
    String processingUnitName = "ece-datagrid";
    @Parameter(names = {"-m"})
    Integer memoryCapacity = 512;
    @Parameter(names = {"-a"})
    Integer allocationChunk = 256;

    Admin admin;
    GridServiceManager gsm;
    ProcessingUnit pu = null;

    public ScaleDataGrid(InputStream in) {
        this.in = in;
    }

    public static void main(String... args) {
        ScaleDataGrid scaleDataGrid = new ScaleDataGrid(System.in);
        new JCommander(scaleDataGrid, args);
        scaleDataGrid.init();
        scaleDataGrid.run();
        scaleDataGrid.close();
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
            pu = gsm.deploy(new ElasticSpaceDeployment(processingUnitName)
                    .singleMachineDeployment()
                    .memoryCapacityPerContainer(allocationChunk, MemoryUnit.MEGABYTES)
                    .maxMemoryCapacity(memoryCapacity * 4, MemoryUnit.MEGABYTES)
                            //		         .maxNumberOfCpuCores(maxNumberOfCpuCores)
                    .addContextProperty("space-config.proxy-settings.connection-retries", "5")
                            //initial scale
                    .scale(new ManualCapacityScaleConfigurer().
                            //	         			numberOfCpuCores(cpuCores)
                                    memoryCapacity(memoryCapacity, MemoryUnit.MEGABYTES).
                            create())
            );
            try {
                monitorPUScaleProgress(pu, memoryCapacity);
            } catch (Exception ignored) {
            }
        }
    }

    void monitorPUScaleProgress(ProcessingUnit pu, int targetCapacity) throws Exception {
        double bias = targetCapacity / 10; // 10 %
        double progressPercentage;

        while (true) {
            int totalGSCs = pu.getAdmin().getGridServiceContainers().getSize();

            double currentMemUsageMB = getPUTotalMemoryUtilization(pu);
            double diff = Math.abs(targetCapacity - currentMemUsageMB);
            if (currentMemUsageMB >= targetCapacity) {
                // scale down scenario
                progressPercentage = (100 * (1 - (diff / currentMemUsageMB)));
            } else {
                // scale up scenario
                progressPercentage = (100 - ((diff / targetCapacity) * 100));
            }
            System.out.printf("Total memory used: %3.2fMB - Progress: %3.2f%% - Total Containers: %d%n",
                    currentMemUsageMB, progressPercentage, totalGSCs);
            Thread.sleep(2000);
            if (currentMemUsageMB > (targetCapacity - bias)
                    && (currentMemUsageMB < (targetCapacity + bias)))
                break;

            if (progressPercentage > 95)
                break;
        }
    }

    double getPUTotalMemoryUtilization(ProcessingUnit pu) {
        HashMap<String, Double> puJVMsSet = new HashMap<String, Double>();

        pu.waitFor(pu.getNumberOfInstances() * 2, 2, TimeUnit.SECONDS);
        double totalMemoryInMB;
        ProcessingUnitInstance instances[] = pu.getInstances();
        for (ProcessingUnitInstance processingUnitInstance : instances) {
            puJVMsSet.put(processingUnitInstance.getMachine().getHostAddress() +
                    processingUnitInstance.getVirtualMachine().getDetails().getPid(),
                    processingUnitInstance.getVirtualMachine().getDetails().
                            getMemoryHeapMaxInMB());
        }

        totalMemoryInMB = puJVMsSet.size() * 256;

        return totalMemoryInMB;
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
                    scale(pu, memoryCapacity, MemoryUnit.MEGABYTES);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean handleChangeRequest(String input) {
        boolean scale = true;
        if (input.equals("+")) {
            if (memoryCapacity < memoryCapacity * 4) {
                memoryCapacity += allocationChunk;
            }
        } else {
            if (input.equals("-")) {
                if (memoryCapacity > allocationChunk * 2) {
                    memoryCapacity -= allocationChunk;
                }
            } else {
                scale = false;
            }
        }
        return scale;
    }

    void scale(ProcessingUnit pu, int targetMemoryCapacity, MemoryUnit memUnit) throws Exception {
        long startTime;
        // checking deployed PU.
        System.out.printf("scaling datagrid capacity from %4g MB to %4d MB%n",
                getPUTotalMemoryUtilization(pu), memoryCapacity);
        startTime = System.currentTimeMillis();
        pu.scale(new ManualCapacityScaleConfigurer()
                .memoryCapacity(targetMemoryCapacity, memUnit)
                .create());

        monitorPUScaleProgress(pu, targetMemoryCapacity);
        long endTime = System.currentTimeMillis();
        System.out.printf("Data-Grid Memory capacity change done!%nTime to scale system: %f seconds%n", (endTime - startTime) / 1000.0);

    }
}
