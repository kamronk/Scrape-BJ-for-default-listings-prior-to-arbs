
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author kenne413
 */
public class ScrapeBtcJam {

    //Leon, 4240
    //Griff, 10840
    //maxID, 39082
    public static void main(String[] args) {

        int maxUserId = 39082;

        TreeMap<String, String> accumulativeListOfOverDueListings = new TreeMap<String, String>();

        ScrapeBtcJam temp = new ScrapeBtcJam();

        int curUserId = 1;

        while (curUserId != maxUserId) {

            System.out.println("Currently at user ID " + curUserId);

            TreeMap<String, String> listOfOverDueListings = temp.getOverDueListingsForBorrower(curUserId);

            ArrayList<String> list = new ArrayList<String>(listOfOverDueListings.keySet());

            for (String key : list) {

                boolean priorToArbs = temp.wasThisLoanFromPriorToArbs(key);
                if (priorToArbs) {
                    accumulativeListOfOverDueListings.put(key, listOfOverDueListings.get(key));
                }

            }
            curUserId++;
        }

        ArrayList<String> list = new ArrayList<String>(accumulativeListOfOverDueListings.keySet());

        for (String key : list) {
            System.out.println("Key:" + key);
            System.out.println("Value:" + accumulativeListOfOverDueListings.get(key));
        }
        
        System.out.println("Count of listings: " + accumulativeListOfOverDueListings.size());

    }

    public boolean wasThisLoanFromPriorToArbs(String listingUrlPortion) {

        try {
            URL oracle = new URL("https://btcjam.com" + listingUrlPortion);
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
            String inputLine;
            boolean inTable = false;
            String investmentTableString = "";
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("listing_investments-table")) {
                    inTable = true;
                }
                if (inTable) {
                    investmentTableString += inputLine;
                }
                if (inputLine.contains("</table>")) {
                    inTable = false;
                }
            }
            String[] tableRows = investmentTableString.split("<tr>");
            for (String row : tableRows) {
                try {
                    String[] rowElements = row.split("<td>");
                    String latestInvestmentDateString = rowElements[3].split("\\s+")[0];
                    try {
                        Date latestInvestmentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(latestInvestmentDateString);
                        Date arbDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2013-03-01");
                        if (arbDate.after(latestInvestmentDate)) {
                            return true;
                        } else {
                            return false;
                        }
                    } catch (ParseException ex) {
                        Logger.getLogger(ScrapeBtcJam.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {

                }
            }
            in.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(MiningOperation_ReinvestingInNewMachines.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MiningOperation_ReinvestingInNewMachines.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public TreeMap<String, String> getOverDueListingsForBorrower(int borrowerId) {

        TreeMap<String, String> listOfOverDueListings = new TreeMap<String, String>();

        String tableString = "";

        boolean withinTheLoanListingsTable = false;

        try {
            URL oracle = new URL("https://btcjam.com/users/" + borrowerId);
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("my_loans-table")) {
                    withinTheLoanListingsTable = true;
                }
                if (withinTheLoanListingsTable) {
                    tableString += inputLine;
                }
                if (inputLine.contains("</table>")) {
                    withinTheLoanListingsTable = false;
                }

            }
            in.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(MiningOperation_ReinvestingInNewMachines.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MiningOperation_ReinvestingInNewMachines.class.getName()).log(Level.SEVERE, null, ex);
        }

        String[] tableRows = tableString.split("/tr");
        for (String row : tableRows) {
            try {
                String[] rowElements = row.split("<td>");
                if (rowElements[7].contains("Overdue")) {
                    String[] brk = rowElements[1].split(">");
                    String listingName = brk[1].split("<")[0];
                    String listingUrl = brk[0].split("\"")[1];
                    listOfOverDueListings.put(listingUrl, listingName);
                }
            } catch (ArrayIndexOutOfBoundsException e) {

            }
        }

        return listOfOverDueListings;

    }

}
