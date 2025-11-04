package com.kavindu.farmshare.dto;

import java.io.Serializable;
import java.util.List;

public class SingleFarmDto implements Serializable {
    private boolean success;
    private String codeName;
    private String farmName;
    private String stockPrice;
    private String stockPriceCents;
    private String valuePrice;
    private String valuePercentage;
    private boolean drop;
    private String farmType;
    private String avgIncome;
    private String riskScore;
    private String ownerName;
    private String ownerDate;
    private String seasonMonths;
    private String landSize;
    private String avgYield;
    private String farmStatus;
    private String lat;
    private String lng;
    private List<String> imageList;
    private List<ChartEntruDto> weekChartData;
    private List<ChartEntruDto> monthChartData;
    private List<ChartEntruDto> seasonChartData;
    private boolean invested;
    private String investedStock;
    private String investedPercentage;
    private String expectIncome;
    private boolean investDrop;
    private String profileImg;
    private boolean stockReleased;
    private String mobile;
    private int stockCount;
    private int minStockCount;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String farmerId;
    private int totalStocks;
    private String latitude;
    private String longitude;

    public SingleFarmDto() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFarmerId() {
        return farmerId;
    }
    
    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }
    
    public int getTotalStocks() {
        return totalStocks;
    }
    
    public void setTotalStocks(int totalStocks) {
        this.totalStocks = totalStocks;
    }
    
    public String getLatitude() {
        return latitude;
    }
    
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    
    public String getLongitude() {
        return longitude;
    }
    
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    
    public int getStockCount() {
        return stockCount;
    }

    public void setStockCount(int stockCount) {
        this.stockCount = stockCount;
    }
    
    public int getMinStockCount() {
        return minStockCount;
    }
    
    public void setMinStockCount(int minStockCount) {
        this.minStockCount = minStockCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getStockPrice() {
        return stockPrice;
    }

    public void setStockPrice(String stockPrice) {
        this.stockPrice = stockPrice;
    }

    public String getStockPriceCents() {
        return stockPriceCents;
    }

    public void setStockPriceCents(String stockPriceCents) {
        this.stockPriceCents = stockPriceCents;
    }

    public String getValuePrice() {
        return valuePrice;
    }

    public void setValuePrice(String valuePrice) {
        this.valuePrice = valuePrice;
    }

    public String getValuePercentage() {
        return valuePercentage;
    }

    public void setValuePercentage(String valuePercentage) {
        this.valuePercentage = valuePercentage;
    }

    public boolean isDrop() {
        return drop;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    public String getFarmType() {
        return farmType;
    }

    public void setFarmType(String farmType) {
        this.farmType = farmType;
    }

    public String getAvgIncome() {
        return avgIncome;
    }

    public void setAvgIncome(String avgIncome) {
        this.avgIncome = avgIncome;
    }

    public String getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(String riskScore) {
        this.riskScore = riskScore;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerDate() {
        return ownerDate;
    }

    public void setOwnerDate(String ownerDate) {
        this.ownerDate = ownerDate;
    }

    public String getSeasonMonths() {
        return seasonMonths;
    }

    public void setSeasonMonths(String seasonMonths) {
        this.seasonMonths = seasonMonths;
    }

    public String getLandSize() {
        return landSize;
    }

    public void setLandSize(String landSize) {
        this.landSize = landSize;
    }

    public String getAvgYield() {
        return avgYield;
    }

    public void setAvgYield(String avgYield) {
        this.avgYield = avgYield;
    }

    public String getFarmStatus() {
        return farmStatus;
    }

    public void setFarmStatus(String farmStatus) {
        this.farmStatus = farmStatus;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }
    
    public List<String> getImageUris() {
        return imageList;
    }

    public List<ChartEntruDto> getWeekChartData() {
        return weekChartData;
    }

    public void setWeekChartData(List<ChartEntruDto> weekChartData) {
        this.weekChartData = weekChartData;
    }

    public List<ChartEntruDto> getMonthChartData() {
        return monthChartData;
    }

    public void setMonthChartData(List<ChartEntruDto> monthChartData) {
        this.monthChartData = monthChartData;
    }

    public List<ChartEntruDto> getSeasonChartData() {
        return seasonChartData;
    }

    public void setSeasonChartData(List<ChartEntruDto> seasonChartData) {
        this.seasonChartData = seasonChartData;
    }

    public boolean isInvested() {
        return invested;
    }

    public void setInvested(boolean invested) {
        this.invested = invested;
    }

    public String getInvestedStock() {
        return investedStock;
    }

    public void setInvestedStock(String investedStock) {
        this.investedStock = investedStock;
    }

    public String getInvestedPercentage() {
        return investedPercentage;
    }

    public void setInvestedPercentage(String investedPercentage) {
        this.investedPercentage = investedPercentage;
    }

    public String getExpectIncome() {
        return expectIncome;
    }

    public void setExpectIncome(String expectIncome) {
        this.expectIncome = expectIncome;
    }

    public boolean isInvestDrop() {
        return investDrop;
    }

    public void setInvestDrop(boolean investDrop) {
        this.investDrop = investDrop;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public boolean isStockReleased() {
        return stockReleased;
    }

    public void setStockReleased(boolean stockReleased) {
        this.stockReleased = stockReleased;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    // Stock count functionality is already defined above
}
