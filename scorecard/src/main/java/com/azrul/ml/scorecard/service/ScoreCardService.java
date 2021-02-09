/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.ml.scorecard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.dmg.pmml.baseline.FieldValue;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

/**
 *
 * @author azrul
 */
@Service
public class ScoreCardService {
    
    @Autowired
    private Evaluator evaluator;
    
    public Optional<String> makeDecision(List<Map<String, Object>> features){
       
        try {
            List<Map<String, ?>> responses = new ArrayList<>();
            for (Map<String, Object> cdata : features) {
                Map<String, Object> response = makeDecision(evaluator, cdata);
                responses.add(response);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responses);
            return Optional.of(responseJson);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ScoreCardService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
        
    }
    
    public Optional<String> makeDecision(Map<String, Object> feature){
        try {
            Map<String, Object> response = makeDecision(evaluator, feature);
            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            return Optional.of(responseJson);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ScoreCardService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    private Map<String, Object> makeDecision(Evaluator evaluator, Map<String, Object> cdata) {
        //String sdata = "10000,10000,10000,36 months,12.99,336.9,90000,n,23.01,690,694,12,1661,15,36,0,0,11447.72,11447.72,10000,1447.72,0,0,0,6394.22,654,650,Individual,42037,NA,NA,NA,NA,NA,NA,NA,NA,NA,11100,5,3822,6406,11,128,25,2,5,25,1,2,3,11,11,7,20,2,12,3,96.9,0,62454,42037,7200,51354,N,Cash,N,TRUE,242349";
        List<InputField> infield = evaluator.getInputFields();
        Object id =cdata.get("id");
        //List<String> cdata = Arrays.asList(sdata.split(","));
        Map<FieldName, Object> arguments = new HashMap<>();
        for (int i = 0; i < infield.size(); i++) {
            FieldValue fieldValue = new FieldValue();
            fieldValue.setField(infield.get(i).getFieldName());
            Object cdataValue=cdata.get(infield.get(i).getFieldName().getValue());
            boolean isNumeric = infield.get(i).getOpType().equals(OpType.CONTINUOUS);
            //System.out.println("Field name:" + infield.get(i).getFieldName().getValue() + "---value:" + cdataValue);
            if ("NA".equals(cdataValue)) {
                arguments.put(infield.get(i).getFieldName(), null);
            } else {
                if (isNumeric) {
                    //fieldValue.setValue(new Double(Double.parseDouble(cdata.get(i))));
                    arguments.put(infield.get(i).getFieldName(), cdataValue);
                } else {
                    //fieldValue.setValue(cdata.get(i));
                    arguments.put(infield.get(i).getFieldName(), cdataValue);
                }
            }
            
        }
        Map<FieldName, ?> responseRaw = evaluator.evaluate(arguments);
        Map<String, Object> response = new HashMap<>();
        for(Map.Entry<FieldName,?> entry : responseRaw.entrySet()){
            response.put(entry.getKey().getValue(), EvaluatorUtil.decode(entry.getValue()));
        }
        response.put("id",id);
        return response;
    }
    
}
