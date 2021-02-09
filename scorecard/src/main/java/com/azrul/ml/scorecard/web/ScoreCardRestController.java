/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.ml.scorecard.web;

import com.azrul.ml.scorecard.service.ScoreCardService;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author azrul
 */
@RestController
@RequestMapping(path = "scorecard/api/")
public class ScoreCardRestController {
    
    @Autowired
    ScoreCardService scoreCardService;

    @RequestMapping(value = "/decisions",method = RequestMethod.GET)
    @ResponseBody
    public String decide(@RequestBody Map<String, Object>[] features) throws Exception {
        return scoreCardService.makeDecision(Arrays.asList(features)).orElseThrow();
    }
    
    @RequestMapping(value = "/decision",method = RequestMethod.GET)
    @ResponseBody
    public String decide(@RequestBody Map<String, Object> feature) throws Exception {
        return scoreCardService.makeDecision(feature).orElseThrow();
    }
    
    

}
