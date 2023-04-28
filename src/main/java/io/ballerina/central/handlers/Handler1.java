package io.ballerina.central.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.central.handlers.exceptions.BalaNotFoundException;
import io.ballerina.central.handlers.exceptions.BuildException;
import io.ballerina.central.handlers.generator.ConnectorGenerator;
import io.ballerina.central.handlers.generator.TriggerGenerator;
import io.ballerina.central.handlers.models.connector.Connector;
import io.ballerina.central.handlers.models.trigger.Trigger;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// will be invoked by the queue
// compile the bala programmatically and generate jsons for apidocs, connectors, triggers
public class Handler1 {

//    private static final Path BALLERINA_HOME = Path.of("/home/ballerina-2201.4.1-swan-lake/distributions/ballerina-2201.4.1");
     private static final Path BALLERINA_HOME = Path.of("/usr/lib/ballerina/distributions/ballerina-2201.4.1");

    private static final Path USER_HOME = Paths.get(System.getenv("USER_HOME") == null ?
            System.getProperty("user.home") : System.getenv("USER_HOME"));

    public JsonObject handle(String balaUrl) throws IOException, BuildException, BalaNotFoundException {
        // download bala
        // unzip bala
        // compile bala
        // generate jsons
        // return error json if needed
        // return jsons

        long currentTime = System.nanoTime();
        Path tempPath = Files.createTempDirectory("bala-" + currentTime).toAbsolutePath();
        Path balaPath = tempPath.resolve(currentTime + ".bala");
        Path docsPath = tempPath.resolve("target");

        Files.createFile(balaPath);

        // Download the bala file and save
        downloadBala(balaUrl, balaPath);

        // Load bala as a project
        BalaProject balaProject = buildBala(balaPath);
        System.out.println("Built success");
        // Generate and save connectors
        List<Connector> connectors = ConnectorGenerator.generateConnectorModel(balaProject);
        System.out.println("conneector size: " + connectors.size());
        boolean containsDuplicates = ConnectorGenerator.containsDuplicateNames(connectors);
        for (Connector connector : connectors) {
//               connectorRepository.addConnector(connector, containsDuplicates);
//               LOG.info("generated connector " + connector.getName() + " for: " + balaFile.getPath());
        }

        // Generate and save triggers
        List<Trigger> triggers = TriggerGenerator.generateTriggerModel(balaProject);
        triggers.forEach(trigger -> {
//               triggerRepository.addTrigger(trigger);
//               LOG.info("generated trigger " + trigger.name + " for: " + balaFile.getPath());
        });

        System.out.println("Triggers built");
        // Generate docs
        generateDocs(docsPath, balaProject);
//           ObjectMapper objectMapper = new ObjectMapper();
//           ArrayNode arrayNode = objectMapper.createArrayNode();
        System.out.println("docs generated");
        Gson gson = new Gson();
        JsonArray arrayNode = new JsonArray();
        for (String moduleName : balaProject.currentPackage().manifest().exportedModules()) {
            Path docPath =
                    docsPath.resolve(balaProject.currentPackage().packageOrg().value())
                            .resolve(moduleName)
                            .resolve(balaProject.currentPackage().packageVersion().value().toString())
                            .resolve("api-docs.json");
            if (!Files.exists(docPath)) {
                continue;
            }
            // Create JSON payload
            String apiDocsJsonAsString = Files.readString(docPath);

//               JsonNode apiDocsDataJson = objectMapper.readTree(apiDocsJsonAsString);
//               ObjectNode buildOutputJson = objectMapper.createObjectNode();
            JsonElement apiDocsDataJson = gson.fromJson(apiDocsJsonAsString, JsonElement.class);
            JsonObject buildOutputJson = new JsonObject();
            buildOutputJson.addProperty("moduleName", moduleName);
            buildOutputJson.add("apiDocJson", apiDocsDataJson);
            arrayNode.add(buildOutputJson);
        }
//           ObjectNode docJsonArray = objectMapper.createObjectNode();
        JsonObject docJsonArray = new JsonObject();
        docJsonArray.addProperty("version", 1);
        docJsonArray.add("apiDocJsons", arrayNode);
        docJsonArray.add("connectors", gson.toJsonTree(connectors));
        docJsonArray.add("triggers", gson.toJsonTree(triggers));
        return docJsonArray;
    }

    /**
     * Download the bala file from URL.
     *
     * @param balaURL  URL for the bala file.
     * @param balaPath The output path of the bala file.
     * @throws BalaNotFoundException When bala URL is invalid or cannot be accessed.
     */
    private static void downloadBala(String balaURL, Path balaPath) throws BuildException, BalaNotFoundException {

        try {
            URL balaURI = new URL(balaURL);
            ReadableByteChannel readableByteChannel = Channels.newChannel(balaURI.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(balaPath.toFile());
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (FileNotFoundException e) {
            throw new BuildException("unable to write to temporary bala file: " + balaPath, e);
        } catch (MalformedURLException e) {
            throw new BalaNotFoundException("unable to locate bala file: " + balaURL, e);
        } catch (IOException e) {
            throw new BuildException("error reading from '" + balaURL + "' or writing to '" + balaPath + "'", e);
        }
    }

    /**
     * Building a bala file.
     *
     * @param balaPath The bala file.
     * @return The built project.
     * @throws BuildException When error occurred while building the bala.
     */
    public static BalaProject buildBala(Path balaPath) throws BuildException {

        try {
            // Remove this condition when https://github.com/ballerina-platform/ballerina-lang/issues/29169 is resolved
            if (Files.notExists(USER_HOME)) {
                Files.createDirectories(USER_HOME);
            }
            Environment environment = EnvironmentBuilder.getBuilder()
                    .setBallerinaHome(BALLERINA_HOME)
                    .setUserHome(USER_HOME).build();
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            System.out.println("in build bala");
            return BalaProject.loadProject(defaultBuilder, balaPath);
        } catch (Exception e) {
            throw new BuildException("error occurred when building: " + e.getMessage(), e);
        }
    }

    /**
     * Generate API docs for a bala project.
     *
     * @param docsPath    The path where docs should be generated to.
     * @param balaProject The bala project.
     * @throws BuildException When error occurred while generating docs.
     */
    private void generateDocs(Path docsPath, BalaProject balaProject) throws BuildException {

        try {
//            LOG.debug("generating docs to: " + docsPath.toString());
            Path docsParentPath = docsPath.getParent();
            if (null != docsParentPath) {
                Files.createDirectories(docsParentPath);
            }
            BallerinaDocGenerator.generateAPIDocs(balaProject, docsPath.toString(), true);
        } catch (Exception e) {
            throw new BuildException("error occurred when generating docs: " + e.getMessage(), e);
        }
    }
}
