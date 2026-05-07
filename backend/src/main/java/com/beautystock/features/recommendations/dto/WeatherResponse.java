package com.beautystock.features.recommendations.dto;

public class WeatherResponse {
    private String city;
    private Double temperature;
    private Integer humidity;
    private String condition;
    private String advice;

    public WeatherResponse() {}

    public WeatherResponse(String city, Double temperature, Integer humidity, String condition, String advice) {
        this.city = city;
        this.temperature = temperature;
        this.humidity = humidity;
        this.condition = condition;
        this.advice = advice;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
}
