package org.sakaiproject.guide.logic;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.guide.model.TaoExercise;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by Sascha Schneider on 24.08.2016.
 */
public class TaoResultsImpl {

    private final static Logger log = Logger.getLogger(TaoResultsImpl.class);
    private String taoRestServer = "";
    private String taoIRestServer = "";
    private String taoRestUser = "";
    private String taoRestPass = "";

    public TaoResultsImpl() {
        this.setTaoRestServer(ServerConfigurationService.getString("taoRestServer"));
        this.setTaoIRestServer(ServerConfigurationService.getString("taoIRestServer"));
        this.setTaoRestUser(ServerConfigurationService.getString("taoRestUser"));
        this.setTaoRestPass(ServerConfigurationService.getString("taoRestPassword"));
    }

    public String getTaoRestServer() {
        return taoRestServer;
    }

    public void setTaoRestServer(String taoRestServer) {
        this.taoRestServer = taoRestServer;
    }

    public String getTaoIRestServer() {
        return taoIRestServer;
    }

    public void setTaoIRestServer(String taoIRestServer) {
        this.taoIRestServer = taoIRestServer;
    }

    public String getTaoRestUser() {
        return taoRestUser;
    }

    public void setTaoRestUser(String taoRestUser) {
        this.taoRestUser = taoRestUser;
    }

    public String getTaoRestPass() {
        return taoRestPass;
    }

    public void setTaoRestPass(String taoRestPass) {
        this.taoRestPass = taoRestPass;
    }

    /*
     * Calculates the total earned score for a given DeliveryExecution
     *
     * @param deliveryExecutionId
     *          The ID of the Delivery Execution in TAO
     * @return The total score of the Test
     */
    public double getTaoTotalScore(String deliveryExecutionId, boolean intermediate) {
        double totalScore = 0;

        try {
            //Do a REST Call to get the Scores of all Questions
            String userCredentials = taoRestUser + ":" + taoRestPass;
            String auth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
            URL url = null;
            if (!intermediate) {
                url = new URL(taoRestServer + "/taoResultServer/RestResults");
            } else {
                url = new URL(taoIRestServer + "/taoResultServer/RestResults");
            }
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.addRequestProperty("Authorization", auth);
            urlConnection.addRequestProperty("Accept", "application/json");
            urlConnection.addRequestProperty("URI", deliveryExecutionId);
            if (urlConnection.getResponseCode() == 200) {

                try {
                    //Parse the JSON response and calculate the total Score
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonResults = (JSONObject) ((JSONObject) jsonParser.parse(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))).get("data");
                    for (Object key : jsonResults.keySet()) {
                        JSONArray properties = (JSONArray) jsonResults.get(key);
                        for (int i = 0; i < properties.size(); i++) {
                            JSONObject o = (JSONObject) properties.get(i);
                            if (o.get("identifier").equals("SCORE")) {
                                totalScore += Double.parseDouble(o.get("value").toString());
                            }
                        }
                    }
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalScore;
    }

    /*
     * Gets the scores for each individual question of the Test
     *
     * @param deliveryExecutionId
     *          The ID of the Delivery Execution in TAO
     * @param intermediate - boolean if user is on an intermediate level
     * @return A list containing the taoExercise objects of the test
     */
    public Map<String, TaoExercise> getTaoIndividualScores(String deliveryExecutionId, boolean intermediate) {
        Map<String, Double> scores = new HashMap();
        Map<String, String> name = new HashMap();
        Map<String, String[]> response = new HashMap();
        ArrayList<String> responseTempList = new ArrayList<>();
        Map<String, String[]> correctResponse = new HashMap();
        Map<String, HashMap<String, String>> choiceMapping = new HashMap();
        Map<String, TaoExercise> result = new HashMap();
        ArrayList<String> correctResponseTemp = new ArrayList<>();
        log.info("TAOTAOTAO INTERMEDIATE " + intermediate);
        try {
            //Do a REST Call to get the Scores of all Questions
            String userCredentials = taoRestUser + ":" + taoRestPass;
            String auth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
            URL url = null;
            if (intermediate) {
                url = new URL(taoIRestServer + "/taoResultServer/RestResults");
            } else {
                url = new URL(taoRestServer + "/taoResultServer/RestResults");
            }
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.addRequestProperty("Authorization", auth);
            urlConnection.addRequestProperty("Accept", "application/json");
            urlConnection.addRequestProperty("URI", deliveryExecutionId);
            String keyString = "";
            if (urlConnection.getResponseCode() == 200) {
                log.info("TAOTAOTAO CONNECTION 200");
                try {
                    //Parse the JSON response and calculate the total Score
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonResults = (JSONObject) ((JSONObject) jsonParser.parse(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))).get("data");
                    for (Object key : jsonResults.keySet()) {
                        keyString = key.toString();
                        JSONArray properties = (JSONArray) jsonResults.get(key);
                        for (int i = 0; i < properties.size(); i++) {
                            JSONObject o = (JSONObject) properties.get(i);
                            if (o.get("identifier").equals("SCORE")) {
                                scores.put(key.toString(), Double.parseDouble(o.get("value").toString()));
                                log.info("TAOTAOTAO SCORE" + Double.parseDouble(o.get("value").toString()));
                            }
                            //get all the responses
                            else if (o.get("identifier").toString().contains("RESPONSE")) {
                                name.put(key.toString(), o.get("title").toString());
                                //responseTemp.put(key.toString(), o.get("value").toString());
                                String responseString = o.get("value").toString();
                                responseString = responseString.replace("[", "");
                                responseString = responseString.replace("]", "");
                                if (responseString.contains(";")) {
                                    String[] temp = responseString.split(";");
                                    for (int x = 0; x < temp.length; x++) {
                                        responseTempList.add(temp[x]);
                                    }
                                } else {
                                    responseTempList.add(responseString);
                                }
                                // and the correct responses
                                JSONArray correctResponses = (JSONArray) o.get("correctResponse");

                                //String[] correctResponsesArray = new String[correctResponses.size()];
                                for (int x = 0; x < correctResponses.size(); x++) {
                                    correctResponseTemp.add(correctResponses.get(x).toString());
                                    //correctResponsesArray[x] = correctResponses.get(x).toString();
                                    log.info("TAOTAOTAO" + correctResponses.get(x).toString());
                                }
                                try {
                                    choiceMapping.put(key.toString(), (HashMap<String, String>) o.get("choiceMappings"));
                                } catch (ClassCastException e) {
                                    choiceMapping.put(key.toString(), new HashMap<String, String>());
                                }
                            }
                        }
                        if (responseTempList.size() > correctResponseTemp.size()) {
                            log.info("TAOTAOTAO Trim result sizes");
                            responseTempList = trimSize(responseTempList, correctResponseTemp);
                        }
                        response.put(keyString, responseTempList.toArray(new String[0]));
                        correctResponse.put(keyString, correctResponseTemp.toArray(new String[0]));
                        responseTempList.clear();
                        correctResponseTemp.clear();
                    }
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        //code for choice mappings, to replpace placeholders with their correct values
        Iterator it = choiceMapping.entrySet().iterator();
        log.info("TAOTAOTAO Map choices");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //get the placeholders in responses that have to be replaced
            String[] correctResponses = correctResponse.get(pair.getKey());
            String[] responseString = response.get(pair.getKey());
            HashMap<String, String> choiceMap = (HashMap<String, String>) pair.getValue();
            Iterator itTemp = choiceMap.entrySet().iterator();
            //replace them with the correct values
            while (itTemp.hasNext()) {
                Map.Entry pairTemp = (Map.Entry) itTemp.next();
                String tempResponse = pairTemp.getValue().toString();
                tempResponse = tempResponse.replace("<p>", "");
                tempResponse = tempResponse.replace("</p>", "");
                tempResponse = tempResponse.trim();
                for (int x = 0; x < correctResponses.length; x++) {
                    if (correctResponses[x].contains(pairTemp.getKey().toString())) {
                        correctResponses[x] = correctResponses[x].replace(pairTemp.getKey().toString(), tempResponse);
                    }
                }
                for (int x = 0; x < responseString.length; x++) {
                    if (responseString[x].contains(pairTemp.getKey().toString())) {
                        responseString[x] = responseString[x].replace(pairTemp.getKey().toString(), tempResponse);
                    }
                }
            }
            response.put((String) pair.getKey(), responseString);
            correctResponse.put((String) pair.getKey(), correctResponses);
        }

        // build the results to exercises and return them to be saved with the comalat grade
        it = name.entrySet().iterator();
        log.info("TAOTAOTAO Build tao exercise");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            TaoExercise te = new TaoExercise((String) pair.getValue());

            te.setScore(scores.get(pair.getKey()));
            if (!response.isEmpty()) {
                for (int i = 0; i < response.get(pair.getKey()).length; i++) {
                    te.addUserChoice(response.get(pair.getKey())[i]);
                }
            }
            if (!correctResponse.isEmpty()) {
                for (int i = 0; i < correctResponse.get(pair.getKey()).length; i++) {
                    te.addCorrectChoice(correctResponse.get(pair.getKey())[i]);
                }
            }
            result.put(te.getExerciseName(), te);
            log.info("TAOTAOTAO" + te.getExerciseName() + " " + te.getUserChoice().size());
        }
        return result;
    }

    /**
     * trim the size of the result list so that its size is equal to the source list
     *
     * @param result - list of results
     * @param source - list of correct results
     * @return the trimmed size of results
     */
    private ArrayList<String> trimSize(ArrayList<String> result, ArrayList<String> source) {
        boolean modified = false;
        if (result.size() > source.size()) {
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).isEmpty()) {
                    result.remove(result.get(i));
                    modified = true;
                    break;
                }
            }
        } else {
            return result;
        }
        if (modified) {
            result = trimSize(result, source);
        }
        return result;
    }
}
