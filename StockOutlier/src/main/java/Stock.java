import org.apache.commons.csv.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Stock {

    // WebDriver setup for Selenium
    private static WebDriver driver;

    public static void main(String[] args) {
        // Set the path to ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "C:/Users/ritwe/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        driver = new ChromeDriver();

        try {
            // Directory containing input CSV files
            File directory = new File(
                    "C:/Users/ritwe/Downloads/(TC1)(TC2) stock_price_data_files/(TC1)(TC2) stock_price_data_files/LSE");
            File[] csvFiles = directory.listFiles((dir, name) -> name.endsWith(".csv"));

            // Check if there are files to process
            if (csvFiles == null || csvFiles.length == 0) {
                System.out.println("No CSV files found in the specified directory!");
                return;
            }

            // Process up to 2 files
            int filesToProcess = Math.min(2, csvFiles.length);
            for (int i = 0; i < filesToProcess; i++) {
                File file = csvFiles[i];
                System.out.println("Processing file: " + file.getName());
                processFile(file);
            }

        } catch (Exception e) {
            // Log any unexpected errors
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure the WebDriver is properly closed
            driver.quit();
        }
    }

    /**
     * Processes a single CSV file for detecting outliers.
     * 
     * @param file the CSV file to process
     */
    private static void processFile(File file) {
        try {
            // Parse the CSV file into a list of maps
            List<Map<String, String>> data = parseCsv(file);
            System.out.println("Data Size: " + data.size());

            // Skip files with insufficient data
            if (data.size() < 30) {
                System.out.println("File has less than 30 rows, skipping: " + file.getName());
                return;
            }

            // Select a random sample of 30 consecutive data points
            List<Map<String, String>> sampledData = sampleData(data);

            // Print sampled data
            for (Map<String, String> entry : sampledData) {
                System.out.println(entry);
            }

            // Detect outliers in the sampled data
            List<Map<String, String>> outliers = detectOutliers(sampledData);

            // Write outliers to a new CSV file
            writeOutliersToCsv(file.getName(), outliers);

        } catch (Exception e) {
            // Log any errors specific to the file processing
            System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Parses a CSV file and converts it into a list of maps.
     * 
     * @param file the CSV file to parse
     * @return a list of rows represented as maps
     * @throws IOException if an error occurs during file reading
     */
    private static List<Map<String, String>> parseCsv(File file) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            for (CSVRecord record : csvParser) {
                // Ensure the row has the expected number of columns
                if (record.size() < 3) {
                    System.err.println("Invalid row in file " + file.getName() + ": " + record.toString());
                    continue;
                }

                Map<String, String> row = new HashMap<>();
                row.put("Stock-ID", record.get(0).trim());
                row.put("Timestamp", record.get(1).trim());
                row.put("stock_price", record.get(2).trim());
                data.add(row);
            }
        }
        return data;
    }

    /**
     * Samples 30 consecutive rows from the data.
     * 
     * @param data the input data
     * @return a sublist of 30 rows
     */
    private static List<Map<String, String>> sampleData(List<Map<String, String>> data) {
        Random random = new Random();
        int startIndex = random.nextInt(data.size() - 29); // Ensure room for 30 rows
        return data.subList(startIndex, startIndex + 30);
    }

    /**
     * Detects outliers in the data based on mean and standard deviation.
     * 
     * @param data the input data
     * @return a list of rows identified as outliers
     */
    private static List<Map<String, String>> detectOutliers(List<Map<String, String>> data) {
        List<Map<String, String>> outliers = new ArrayList<>();
        double sum = 0, sumSq = 0;
        int n = data.size();

        // Calculate mean and standard deviation
        for (Map<String, String> row : data) {
            double price = Double.parseDouble(row.get("stock_price"));
            sum += price;
            sumSq += price * price;
        }
        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double stdDev = Math.sqrt(variance);

        // Identify outliers
        for (Map<String, String> row : data) {
            double price = Double.parseDouble(row.get("stock_price"));
            double deviation = price - mean;
            if (Math.abs(deviation) > 2 * stdDev) {
                Map<String, String> outlier = new HashMap<>(row);
                outlier.put("mean", String.valueOf(mean));
                outlier.put("deviation", String.valueOf(deviation));
                outlier.put("percent_deviation", String.valueOf((deviation / (2 * stdDev)) * 100));
                outliers.add(outlier);
            }
        }

        return outliers;
    }

    /**
     * Writes outliers to a new CSV file.
     * 
     * @param originalFileName the name of the original file
     * @param outliers         the list of outliers
     */
    private static void writeOutliersToCsv(String originalFileName, List<Map<String, String>> outliers) {
        if (outliers.isEmpty()) {
            System.out.println("No outliers found in " + originalFileName);
            return;
        }

        String outputFileName = originalFileName.replace(".csv", "_outliers.csv");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFileName));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            // Write header
            csvPrinter.printRecord("Stock-ID", "Timestamp", "Stock Price", "Mean", "Deviation", "Percent Deviation");

            // Write outliers
            for (Map<String, String> outlier : outliers) {
                csvPrinter.printRecord(
                        outlier.get("Stock-ID"),
                        outlier.get("Timestamp"),
                        outlier.get("stock_price"),
                        outlier.get("mean"),
                        outlier.get("deviation"),
                        outlier.get("percent_deviation"));
            }

            System.out.println("Outliers written to: " + outputFileName);

        } catch (IOException e) {
            System.err.println("Error writing outliers to file: " + e.getMessage());
        }
    }
}
