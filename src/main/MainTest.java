package main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import tools.*;

import java.util.*;
import java.util.stream.Collectors;

public class MainTest {
    public static void main(String[] args) throws JsonProcessingException {
        List<Integer> l = Arrays.asList(1,3,6);
        l = l.stream()
             .sorted()
             .collect(Collectors.toList());
        System.out.println(l);
    }
}
