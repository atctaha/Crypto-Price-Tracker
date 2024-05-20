import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class CryptoPriceTracker extends JFrame {

    private final JLabel statusLabel;
    private final JTextArea resultArea;
    private final JPanel topCryptosPanel;

    public CryptoPriceTracker() {
        setTitle("Crypto Price Tracker");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.BLUE);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.GREEN);
        JButton refreshButton = new JButton("Refresh");
        buttonPanel.add(refreshButton);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setBackground(Color.BLUE);
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Status label
        statusLabel = new JLabel("Press Refresh to get latest crypto prices", SwingConstants.CENTER);
        statusLabel.setBackground(Color.ORANGE);
        statusLabel.setOpaque(true);

        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);

        // Wrap result area in a scroll pane
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Panel to display top 10 cryptos
        topCryptosPanel = new JPanel();
        topCryptosPanel.setLayout(new BoxLayout(topCryptosPanel, BoxLayout.Y_AXIS));
        JLabel topCryptosLabel = new JLabel("Top 10 Cryptos");
        topCryptosPanel.add(topCryptosLabel);

        // Fetch top cryptos on startup
        fetchTopCryptos();

        // Wrap top cryptos panel in a scroll pane
        JScrollPane topCryptosScrollPane = new JScrollPane(topCryptosPanel);

        // Arrange main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(searchPanel, BorderLayout.SOUTH);
        mainPanel.add(statusLabel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(topCryptosScrollPane, BorderLayout.EAST);

        // Button click action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchCryptoPrices(resultArea);
                fetchTopCryptos();
            }
        });

        // Search button click action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                if (!searchText.isEmpty()) {
                    searchCrypto(searchText, resultArea);
                }
            }
        });

        // Add main panel to the frame
        add(mainPanel);
    }

    // Function to fetch crypto prices
    private void fetchCryptoPrices(JTextArea resultArea) {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=10&convert=USD";
        final String apiKey = "";   // Write your own api key.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-CMC_PRO_API_KEY", apiKey);

            int responseCode = connection.getResponseCode();
            InputStream inputStream;
            if (responseCode == 200) {
                inputStream = connection.getInputStream();
                processResponse(inputStream, resultArea);
                statusLabel.setText("Crypto prices updated successfully");
            } else {
                inputStream = connection.getErrorStream();
                statusLabel.setText("Error response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            statusLabel.setText("Connection error occurred");
            e.printStackTrace();
        }
    }

    // Function to search for crypto
    private void searchCrypto(String searchText, JTextArea resultArea) {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=" + searchText.toUpperCase();
        String apiKey = "8ad73866-ca1d-4f29-b57d-392b88f1c818";

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-CMC_PRO_API_KEY", apiKey);

            int responseCode = connection.getResponseCode();
            InputStream inputStream;
            if (responseCode == 200) {
                inputStream = connection.getInputStream();
                processResponse(inputStream, resultArea);
                statusLabel.setText("Crypto information for " + searchText.toUpperCase());
            } else if (responseCode == 404) {
                resultArea.setText("");
                statusLabel.setText("This crypto cannot be found: " + searchText.toUpperCase());
            } else {
                inputStream = connection.getErrorStream();
                statusLabel.setText("Error response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            statusLabel.setText("Connection error occurred");
            e.printStackTrace();
        }
    }

    // Function to fetch top 10 cryptos
    private void fetchTopCryptos() {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=10&convert=USD";
        String apiKey = "8ad73866-ca1d-4f29-b57d-392b88f1c818";

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-CMC_PRO_API_KEY", apiKey);

            int responseCode = connection.getResponseCode();
            InputStream inputStream;
            if (responseCode == 200) {
                inputStream = connection.getInputStream();
                processTopCryptosResponse(inputStream);
            } else {
                inputStream = connection.getErrorStream();
                System.out.println("Error response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            System.out.println("Connection error occurred");
            e.printStackTrace();
        }
    }

    // Function to process top 10 cryptos response
    private void processTopCryptosResponse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Process JSON data
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");

        // Clear existing data
        topCryptosPanel.removeAll();

        // Add label for top cryptos
        JLabel topCryptosLabel = new JLabel("Top 10 Cryptos");
        topCryptosPanel.add(topCryptosLabel);

        // Add sorted top cryptos to the panel
        for (JsonElement element : dataArray) {
            JsonObject coinInfo = element.getAsJsonObject();
            String name = coinInfo.get("name").getAsString();
            String symbol = coinInfo.get("symbol").getAsString();
            double price = coinInfo.getAsJsonObject("quote").getAsJsonObject("USD").get("price").getAsDouble();

            JLabel cryptoLabel = new JLabel(name + " (" + symbol + "): $" + price);
            topCryptosPanel.add(cryptoLabel);
        }

        // Refresh the panel
        topCryptosPanel.revalidate();
        topCryptosPanel.repaint();
    }

    // Function to process response
    private void processResponse(InputStream inputStream, JTextArea resultArea) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Process JSON data
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
        JsonObject dataObject = jsonObject.getAsJsonObject("data");

        // Get crypto information
        JsonObject coinInfo = dataObject.entrySet().iterator().next().getValue().getAsJsonObject();
        String name = coinInfo.get("name").getAsString();
        String symbol = coinInfo.get("symbol").getAsString();
        double price = coinInfo.getAsJsonObject("quote").getAsJsonObject("USD").get("price").getAsDouble();

        // Update result area
        resultArea.setText("Name: " + name + ", Symbol: " + symbol + ", Price: " + price + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CryptoPriceTracker tracker = new CryptoPriceTracker();
                tracker.setVisible(true);
            }
        });
    }
}