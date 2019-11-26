package pt.isel.ngspipes.engine_app;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ArgumentsParser {

    private static final String APP_NAME = "NGSPipes Engine";

    public static final String PIPES_PATH = "pipes";
    public static final String IR_PATH = "ir";
    public static final String OUT_PATH = "out";
    public static final String WORKING_DIRECTORY = "workDir";
    public static final String PARALLEL = "parallel";
    public static final String CPUS = "cpus";
    public static final String MEM = "mem";
    public static final String DISK = "disk";
    public static final String PARAMETERS = "parameters";

    public static final int DEFAULT_CPUS = 0;
    public static final int DEFAULT_MEM = 0;
    public static final int DEFAULT_DISK = 0;



    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();


    public ArgumentsParser(){
        options.addOption(PIPES_PATH, true, "Pipeline path (mandatory)");
        options.addOption(IR_PATH, true, "Pipeline intermediate representation");
        options.addOption(OUT_PATH, true, "Output absolute pathname (mandatory)");
        options.addOption(WORKING_DIRECTORY, true, "Working directory absolute pathname (mandatory)");
        options.addOption(CPUS, true, "Assigned cores");
        options.addOption(DISK, true, "Assigned disk space");
        options.addOption(MEM, true, "Assigned max memory in megabytes");
        options.addOption(PARALLEL, true, "Indicates either execution must be parallel or sequential (mandatory).");
        options.addOption(PARAMETERS, true, "Pipeline parameters. (e.x. param_name=1,param1_name=true,param2_name=add)");
    }


    public ConsoleArguments parse(String[] args) throws ParseException {
        CommandLine cmdLine = parser.parse( options, args );

        // check mandatory arguments
        if (!validateMandatoryArguments(cmdLine) || !validateArguments(cmdLine)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( APP_NAME, options );
            return null;
        }

        return createArguments(cmdLine);
    }

    private boolean validateMandatoryArguments(CommandLine cmdLine){
        boolean hasPipes = cmdLine.hasOption(PIPES_PATH);
        boolean hasIr = cmdLine.hasOption(IR_PATH);
        if(     (hasPipes ^ !hasIr) ||
                !cmdLine.hasOption(PARALLEL) ||
                !cmdLine.hasOption(OUT_PATH)){

            System.err.println("Missing mandatory arguments.");
            return false;
        }

        return  true;
    }

    private boolean validateArguments(CommandLine cmdLine){
        boolean validatePipes = validatePipesPath(cmdLine.getOptionValue(PIPES_PATH, ""));
        boolean validateIRPath = validateIRPath(cmdLine.getOptionValue(IR_PATH, ""));
        boolean validateOutPath = validateOutPath(cmdLine.getOptionValue(OUT_PATH, ""));
        boolean validateWorkingDirectory = validateOutPath(cmdLine.getOptionValue(WORKING_DIRECTORY, ""));
        boolean validateParallel = validateParallel(cmdLine.getOptionValue(PARALLEL, ""));
        boolean validateParameters = validateParameters(cmdLine.getOptionValue(PARAMETERS, ""));
        boolean validateCpus = validateCpus(cmdLine.getOptionValue(CPUS, ""));
        boolean validateDisk = validateDisk(cmdLine.getOptionValue(DISK, ""));
        boolean validateMem = validateMem(cmdLine.getOptionValue(MEM, ""));

        return  validatePipes && validateIRPath && validateOutPath &&
                validateParallel && validateParameters && validateCpus &&
                validateDisk && validateMem && validateWorkingDirectory;
    }

    private boolean validatePipesPath(String path) {
        boolean valid = true;

        boolean existFile = !new File(path).exists();
        if(!path.isEmpty() && existFile){
            System.err.println("Nonexistent pipeline path!");
            valid = false;
        }

        return valid;
    }

    private boolean validateIRPath(String path) {
        boolean valid = true;

        if(!path.isEmpty() &&!new File(path).exists()){
            System.err.println("Nonexistent ir path!");
            valid = false;
        }

        return valid;
    }

    private boolean validateOutPath(String path) {
        boolean valid = true;

        if(path == null || path.isEmpty()) {
            System.err.println("Invalid output path");
            valid = false;
        }

        boolean existFile = !new File(path).exists();
        if(existFile){
            System.err.println("Nonexistent output path");
            valid = false;
        }

        return valid;
    }

    private boolean validateParallel(String parallel) {
        return parallel.equalsIgnoreCase("true")
                || parallel.equalsIgnoreCase("false");
    }

    private boolean validateParameters(String parameters) {
        if (parameters != null && !parameters.isEmpty())
            return parameters.contains("=");

        return true;
    }

    private boolean validateCpus(String cpus) {
        boolean valid = true;

        if(cpus != null && !cpus.isEmpty()) {
            try{
                int number = Integer.parseInt(cpus);

                if(number<=0){
                    System.err.println("Cpus value must be a positive!");
                    valid = false;
                }

            } catch (NumberFormatException ex) {
                System.err.println("Invalid cpus value! It must be an int.");
                valid = false;
            }
        }

        return valid;
    }


    private boolean validateDisk(String disk) {
        boolean valid = true;

        if(disk != null && !disk.isEmpty()) {
            try{
                int number = Integer.parseInt(disk);

                if(number<=0){
                    System.err.println("Mem value must be a positive!");
                    valid = false;
                }

            } catch (NumberFormatException ex) {
                System.err.println("Invalid mem value! It must be an int.");
                valid = false;
            }
        }

        return valid;
    }

    private boolean validateMem(String mem) {
        boolean valid = true;

        if(mem != null && !mem.isEmpty()) {
            try{
                int number = Integer.parseInt(mem);

                if(number<=0){
                    System.err.println("Mem value must be a positive!");
                    valid = false;
                }

            } catch (NumberFormatException ex) {
                System.err.println("Invalid mem value! It must be an int.");
                valid = false;
            }
        }

        return valid;
    }

    private ConsoleArguments createArguments(CommandLine cmdLine) throws ParseException {
        if (cmdLine.hasOption(IR_PATH))
            return new ConsoleArguments(
                    cmdLine.getOptionValue(OUT_PATH),
                    cmdLine.getOptionValue(WORKING_DIRECTORY),
                    getInt(cmdLine.getOptionValue(CPUS, DEFAULT_CPUS+"")),
                    getInt(cmdLine.getOptionValue(MEM, DEFAULT_MEM+"")),
                    getInt(cmdLine.getOptionValue(DISK, DEFAULT_DISK+"")),
                    getParallel(cmdLine.getOptionValue(PARALLEL, "false")),
                    getParameters(cmdLine.getOptionValue(PARAMETERS, "")),
                    cmdLine.getOptionValue(IR_PATH));
        return new ConsoleArguments(
                cmdLine.getOptionValue(PIPES_PATH),
                cmdLine.getOptionValue(OUT_PATH),
                cmdLine.getOptionValue(WORKING_DIRECTORY),
                getInt(cmdLine.getOptionValue(CPUS, DEFAULT_CPUS+"")),
                getInt(cmdLine.getOptionValue(MEM, DEFAULT_MEM+"")),
                getInt(cmdLine.getOptionValue(DISK, DEFAULT_DISK+"")),
                getParallel(cmdLine.getOptionValue(PARALLEL, "true")),
                getParameters(cmdLine.getOptionValue(PARAMETERS, "")));
    }

    private Map<String, Object> getParameters(String value) throws ParseException {
        Map<String, Object> parameters = new HashMap<>();

        if (value == null || value.isEmpty())
            return parameters;

        String[] parametersStr = value.split(",");

        for (String parameter : parametersStr){
            String[] parameterValue = parameter.split("=");

            if (parameterValue.length < 2)
                throw new ParseException("Error parsing parameters");

            parameters.put(parameterValue[0], parameterValue[1]);
        }

        return parameters;
    }

    private int getInt(String value) {
        return Integer.parseInt(value);
    }

    private boolean getParallel(String value) {
        return value.equalsIgnoreCase("true");
    }
}