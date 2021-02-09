/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.ml.scorecard.common;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

/**
 *
 * @author azrul
 */
@Configuration
public class DecisioningConfig {
    
    @Value("${scorecard.pmmlfile}")
    private String pmmlfile;
    
    @Bean
    public Evaluator evaluator(){
        try {
            URL resource = getClass().getClassLoader().getResource(pmmlfile);
            
            Evaluator evaluator = new LoadingModelEvaluatorBuilder()
                    .load(new File(resource.toURI()))
                    .build();
            return evaluator;
        } catch (URISyntaxException ex) {
            Logger.getLogger(DecisioningConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DecisioningConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DecisioningConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(DecisioningConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
