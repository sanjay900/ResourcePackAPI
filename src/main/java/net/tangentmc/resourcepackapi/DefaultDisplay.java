package net.tangentmc.resourcepackapi;

import org.json.JSONObject;

public class DefaultDisplay {

    public static JSONObject BOW = new JSONObject("{" +
            "\"display\": {\n" +
            "        \"thirdperson_righthand\": {\n" +
            "            \"rotation\": [ -80, 260, -40 ],\n" +
            "            \"translation\": [ -1, -2, 2.5 ],\n" +
            "            \"scale\": [ 0.9, 0.9, 0.9 ]\n" +
            "        },\n" +
            "        \"thirdperson_lefthand\": {\n" +
            "            \"rotation\": [ -80, -280, 40 ],\n" +
            "            \"translation\": [ -1, -2, 2.5 ],\n" +
            "            \"scale\": [ 0.9, 0.9, 0.9 ]\n" +
            "        },\n" +
            "        \"firstperson_righthand\": {\n" +
            "            \"rotation\": [ 0, -90, 25 ],\n" +
            "            \"translation\": [ 1.13, 3.2, 1.13],\n" +
            "            \"scale\": [ 0.68, 0.68, 0.68 ]\n" +
            "        },\n" +
            "        \"firstperson_lefthand\": {\n" +
            "            \"rotation\": [ 0, 90, -25 ],\n" +
            "            \"translation\": [ 1.13, 3.2, 1.13],\n" +
            "            \"scale\": [ 0.68, 0.68, 0.68 ]\n" +
            "        }\n" +
            "    }" +
            "}");
    public static JSONObject SHIELD = new JSONObject("{" +
            "\"display\": {\n" +
            "        \"thirdperson_righthand\": {\n" +
            "            \"rotation\": [ 0, 90, 0 ],\n" +
            "            \"translation\": [ 10.51, 6, -4 ],\n" +
            "            \"scale\": [ 1, 1, 1 ]\n" +
            "        },\n" +
            "        \"thirdperson_lefthand\": {\n" +
            "            \"rotation\": [ 0, 90, 0 ],\n" +
            "            \"translation\": [ 10.51, 6, 12 ],\n" +
            "            \"scale\": [ 1, 1, 1 ]\n" +
            "        },\n" +
            "        \"firstperson_righthand\": {\n" +
            "            \"rotation\": [ 0, 180, 5 ],\n" +
            "            \"translation\": [ -10, 2, -10 ],\n" +
            "            \"scale\": [ 1.25, 1.25, 1.25 ]\n" +
            "        },\n" +
            "        \"firstperson_lefthand\": {\n" +
            "            \"rotation\": [ 0, 180, 5 ],\n" +
            "            \"translation\": [ 10, 0, -10 ],\n" +
            "            \"scale\": [ 1.25, 1.25, 1.25 ]\n" +
            "        },\n" +
            "        \"gui\": {\n" +
            "            \"rotation\": [ 15, -25, -5 ],\n" +
            "            \"translation\": [ 2, 3, 0 ],\n" +
            "            \"scale\": [ 0.65, 0.65, 0.65 ]\n" +
            "        },\n" +
            "        \"fixed\": {\n" +
            "            \"rotation\": [ 0, 180, 0 ],\n" +
            "            \"translation\": [ -2, 4, -5],\n" +
            "            \"scale\":[ 0.5, 0.5, 0.5]\n" +
            "        },\n" +
            "        \"ground\": {\n" +
            "            \"rotation\": [ 0, 0, 0 ],\n" +
            "            \"translation\": [ 4, 4, 2],\n" +
            "            \"scale\":[ 0.25, 0.25, 0.25]\n" +
            "        }\n" +
            "    }" +
            "}");
    //Display data for positioning blocks.
    public static JSONObject BLOCK = new JSONObject(
            "{"+
                    "        \"head\": { "+
                    "            \"rotation\": [ -30, 0, 0 ], " +
                    "            \"translation\": [ 0, -30.75, -7.25 ], " +
                    "            \"scale\": [ 3.0125, 3.0125, 3.0125 ]" +
                    "        },"+
                    "        \"gui\": {"+
                    "            \"rotation\": [ 30, 225, 0 ],"+
                    "            \"translation\": [ 0, 0, 0],"+
                    "            \"scale\":[ 0.625, 0.625, 0.625 ]"+
                    "        },"+
                    "        \"ground\": {"+
                    "            \"rotation\": [ 0, 0, 0 ],"+
                    "            \"translation\": [ 0, 3, 0],"+
                    "            \"scale\":[ 0.25, 0.25, 0.25 ]"+
                    "        },"+
                    "        \"fixed\": {"+
                    "            \"rotation\": [ 0, 0, 0 ],"+
                    "            \"translation\": [ 0, 0, 0],"+
                    "            \"scale\":[ 0.5, 0.5, 0.5 ]"+
                    "        },"+
                    "        \"thirdperson_righthand\": {"+
                    "            \"rotation\": [ 75, 45, 0 ],"+
                    "            \"translation\": [ 0, 2.5, 0],"+
                    "            \"scale\": [ 0.375, 0.375, 0.375 ]"+
                    "        },"+
                    "        \"firstperson_righthand\": {"+
                    "            \"rotation\": [ 0, 45, 0 ],"+
                    "            \"translation\": [ 0, 0, 0 ],"+
                    "            \"scale\": [ 0.40, 0.40, 0.40 ]"+
                    "        },"+
                    "        \"firstperson_lefthand\": {"+
                    "            \"rotation\": [ 0, 225, 0 ],"+
                    "            \"translation\": [ 0, 0, 0 ],"+
                    "            \"scale\": [ 0.40, 0.40, 0.40 ]"+
                    "        }"+
                    "}");
}
