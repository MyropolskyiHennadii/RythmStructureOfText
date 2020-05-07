package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAnFooterListsForWebOutput;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static textsVocal.structure.TextPortionForRythm.SYMB_PARAGRAPH;

/**
 * class builds portions
 */
@Component
public class BuildingPortion {
    private static final Logger log = LoggerFactory.getLogger(BuildingPortion.class);//logger
    //=== fields =======================================
    private static int numPortion = 1;//start number of the portion

    //=== setters and getters
    public int getNumPortion() {
        return numPortion;
    }

    /**
     * create portion and fill list of instances
     * @param text string with porttion text
     * @param numPortion number of portion
     * @param thisIsVerse true or false
     */
    public void createTextPortionInstance(String text, int numPortion, boolean thisIsVerse){
        TextPortionForRythm instance =
                thisIsVerse?
                        new VersePortionForRythm(text):
                        new ProsePortionForRythm(text);

        instance.setNumberOfPortion(numPortion);
        AnalyserPortionOfText.getListOfInstance().add(thisIsVerse? (VersePortionForRythm)instance: (ProsePortionForRythm)instance);
    }


    /**
     * build portion in verse by separator
     */
    public void buildVersePortions(String testText, CommonConstants constants) throws ExecutionException, InterruptedException, IOException {

        boolean readingFromFile = constants.isReadingFromFile();
        ;
        String languageOfText = constants.getLanguageOfText();
        int numPortion = getNumPortion();
        String directoryInput = constants.getFileInputDirectory();
        String fileInputName = constants.getFileInputName();
        String directoryOuput = constants.getFileOutputDirectory();
        String fileOutputName = constants.getFileOutputName();
        String charsetName = constants.getCharsetName();
        String portionSeparator = constants.getPortionSeparator();

        //if user didn't give portion separator = there is only one portion
        if(portionSeparator.trim().isEmpty()){
            portionSeparator = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
        }

        if (!readingFromFile) {
            //PortionOfTextAnalyser.portionAnalysys(numPortion, testText, constants);
            createTextPortionInstance(testText, numPortion, true);
        } else {

            Path textPath = Paths.get(directoryInput, fileInputName);
            if (!Files.exists(textPath)) {
                log.error("There is no file " + textPath);
                throw new FileNotFoundException("There is no file " + textPath);
            }
            if (!Files.isReadable(textPath)) {
                log.error("Impossible to read file " + textPath);
                throw new FileNotFoundException("Impossible to read file " + textPath);
            }

            // find portion in verse by separator and in prose by paraghaph
            File input = new File(textPath.toString());
// read the content from file
            int k = 0;
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(input), charsetName))) {
                StringBuilder sPortion = new StringBuilder();
                String line = bufferedReader.readLine().trim();

                while (line != null) {

                    if (line.trim().isEmpty()) {
                        line = bufferedReader.readLine();
                        continue;
                    }

                    if (sPortion.toString().isEmpty() && line.contains(portionSeparator)) {
                        line = bufferedReader.readLine();
                        continue;
                    }

                    if ((k == 0) && (int) line.charAt(0) == 65279 && line.length() <= 1) {
                        line = bufferedReader.readLine();
                        k++;
                        continue;
                    }
                    k++;

                    if (!line.contains(portionSeparator)) {
                        sPortion.append(line);
                        sPortion.append((char) 12);
                    } else {
/*                        PortionOfTextAnalyser
                                .portionAnalysys(numPortion, sPortion.toString().trim(), constants);*/
                        createTextPortionInstance(sPortion.toString().trim(), numPortion, true);
                        sPortion.delete(0, sPortion.length());
                        numPortion++;
                        k = 0;
                    }
                    line = bufferedReader.readLine();
                }

                //last portion
                if (!sPortion.toString()
                        .isEmpty()) {
               /*     PortionOfTextAnalyser
                            .portionAnalysys(numPortion, sPortion.toString().trim(), constants);*/
                    createTextPortionInstance(sPortion.toString().trim(), numPortion, true);
                }
            } catch (FileNotFoundException e) {
                log.error("Undefinite FileNotFoundExctption in main", e);
                throw e;
            } catch (IOException e) {
                log.error("Undefinite IOException in main", e);
                throw e;
            }
        }
    }

    /**
     * build portion in prose by paraghaph
     */
    public void buildProsePortions(String testText, CommonConstants constants) throws IOException {

        boolean readingFromFile = constants.isReadingFromFile();
        int numPortion = getNumPortion();
        String directoryInput = constants.getFileInputDirectory();
        String fileInputName = constants.getFileInputName();
        String charsetName = constants.getCharsetName();

        if (!readingFromFile) {
            StringBuilder portionText = new StringBuilder();
            for (int i = 0; i < testText.length(); i++) {
                boolean mustAdd = true;
                for (CharSequence charSequence : SYMB_PARAGRAPH) {
                    if (("" + testText.charAt(i)).equals("" + charSequence)) {
                        if (portionText.length() > 0) {
                            /* PortionOfTextAnalyser.portionAnalysys(numPortion, portionText, constants);*/
                            createTextPortionInstance(portionText.toString(), numPortion, false);
                            portionText = new StringBuilder();
                            mustAdd = false;
                            numPortion++;
                        }
                    }
                }
                if (mustAdd) {
                    portionText.append(testText.charAt(i));
                }
            }
            //last portion
            if (portionText.length() > 0) {
                createTextPortionInstance(portionText.toString(), numPortion, false);
                numPortion++;
            }

        } else {

            Path textPath = Paths.get(directoryInput, fileInputName);
            if (!Files.exists(textPath)) {
                log.error("There is no file " + textPath);
                throw new FileNotFoundException("There is no file " + textPath);
            }
            if (!Files.isReadable(textPath)) {
                log.error("Impossible to read file " + textPath);
                throw new FileNotFoundException("Impossible to read file " + textPath);
            }

            // find portion in prose by paraghaph
            File input = new File(textPath.toString());

// read the content from file
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(input), charsetName))) {
                String line = bufferedReader.readLine();

                while (line != null) {
                    if (line.trim().isEmpty()) {
                        line = bufferedReader.readLine();
                        continue;
                    }
                    createTextPortionInstance(line.trim(), numPortion, false);
                    numPortion++;
                    line = bufferedReader.readLine();
                }
            } catch (FileNotFoundException e) {
                log.error("Undefinite FileNotFoundExctption in main", e);
                throw e;
            } catch (IOException e) {
                log.error("Undefinite IOException in main", e);
                throw e;
            }

        }
        //outputStressProfileOfWholeText(outputAccumulation, constants);

    }

    public void startPortionBuilding(String testText, CommonConstants constants) throws InterruptedException, ExecutionException, IOException {

        if (constants.isThisIsWebApp()){
            //clean static fields
            HeaderAnFooterListsForWebOutput.getPortionFooters().clear();
            HeaderAnFooterListsForWebOutput.getPortionHeaders().clear();
            AnalyserPortionOfText.getListOfInstance().clear();
            CommonConstants.getUnKnownWords().clear();
            CommonConstants.getTempWordDictionary().clear();
        }

        // build portions in poems by separator and in prose by paraghaph!!!
        if (constants.isThisIsVerse()) {
            buildVersePortions(testText, constants);
        } else {
            buildProsePortions(testText, constants);
        }
    }

}
