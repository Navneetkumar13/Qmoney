
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
//import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static final String TOKEN = "2e7f2645b8da5095fd723e31999d219198bfc3a8";

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    //return Collections.emptyList();
    File file = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<String>();
    for (PortfolioTrade trade : trades) {
      symbols.add(trade.getSymbol());
    }
    return symbols;
     
  }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/navneet1465-ME_QMONEY_V2/qmoney/src/main/resources/"+"trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@43";
     String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "24:1";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

    public static List<TotalReturnsDto> mainReadQuotesHelper(String[] args,
      List<PortfolioTrade> trades) throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> tests = new ArrayList<TotalReturnsDto>();
    for (PortfolioTrade t : trades) {
      String uri = "https://api.tiingo.com/tiingo/daily/" + t.getSymbol() + "/prices?startDate="
          + t.getPurchaseDate().toString() + "&endDate=" + args[1]
          + "&token=2e7f2645b8da5095fd723e31999d219198bfc3a8";
      TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
      if (results != null) {
        tests.add(new TotalReturnsDto(t.getSymbol(), results[results.length - 1].getClose()));
      }
    }
    return tests;
  }

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays
        .asList(objectMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
    List<TotalReturnsDto> sortedByValue = mainReadQuotesHelper(args, trades);
    Collections.sort(sortedByValue, TotalReturnsDto.closingComparator);
    List<String> stocks = new ArrayList<String>();
    for (TotalReturnsDto trd : sortedByValue) {
      stocks.add(trd.getSymbol());
    }
    return stocks;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
     File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    List<PortfolioTrade> symbols = new ArrayList<PortfolioTrade>();
    for (PortfolioTrade trade : trades) {
      symbols.add(trade);
    }
    return symbols;

  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    // String x = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?endDate="
    //     + endDate + "&token=" + token;
    // System.out.println(x);
      return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="+ trade
          .getPurchaseDate().toString() +"&endDate=" + endDate
         + "&token=" + token;
    //  return null;
  }

  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  public static String getToken() {
  return TOKEN;    
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {

    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] stockStartToEndDate = restTemplate.getForObject(url, TiingoCandle[].class);
    List<Candle> candles = new ArrayList<>();
    for (TiingoCandle s : stockStartToEndDate) {
      candles.add(s);
    }
    return candles;
  }
  
  public static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade,LocalDate endLocalDate) {
    String ticker = trade.getSymbol();
    LocalDate startLocalDate = trade.getPurchaseDate();

    if (startLocalDate.compareTo(endLocalDate) >= 0) {
      throw new RuntimeException();
    }
    String url = String.format(
        "https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s",
        ticker, startLocalDate.toString(), endLocalDate.toString(), TOKEN);

    RestTemplate restTemplate = new RestTemplate();

    TiingoCandle[] stockStartToEndDate = restTemplate.getForObject(url, TiingoCandle[].class);

    if (stockStartToEndDate != null) {
      TiingoCandle stockStartDate = stockStartToEndDate[0];
      TiingoCandle stockEndDate = stockStartToEndDate[stockStartToEndDate.length - 1];

      Double buyPrice = stockStartDate.getOpen();
      Double sellPrice = stockEndDate.getClose();

      AnnualizedReturn annualizedReturn =
          calculateAnnualizedReturns(endLocalDate, trade, buyPrice, sellPrice);
      return annualizedReturn;
    } else {
      return new AnnualizedReturn(ticker, Double.NaN, Double.NaN);
    }
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    //return Collections.emptyList();
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    LocalDate endLocalDate = LocalDate.parse(args[1]);
    
    File trades = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] tradeJson = objectMapper.readValue(trades, PortfolioTrade[].class);
    
    for (int i = 0; i < tradeJson.length; i++) {
      annualizedReturns.add(getAnnualizedReturn(tradeJson[i], endLocalDate));
    }
    
    Comparator<AnnualizedReturn> SortByAnnReturn =
        Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns, SortByAnnReturn);

    return annualizedReturns;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    Double totReturn = (sellPrice - buyPrice) / buyPrice;
    
    String symbol = trade.getSymbol();
    LocalDate purchaseDate = trade.getPurchaseDate();

    Double numYears = (double) ChronoUnit.DAYS.between(purchaseDate, endDate) / 365;

    Double annulizedReturns = Math.pow((1 + totReturn), (1 / numYears)) - 1;

      return new AnnualizedReturn(symbol, annulizedReturns, totReturn);
  }






























  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  // public static RestTemplate restTemplate = new RestTemplate();
  // public static PortfolioManager portfolioManager =
  //     PortfolioManagerFactory.getPortfolioManager(restTemplate);

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
      //  String contents = readFileAsString(file);
      //  ObjectMapper objectMapper = getObjectMapper();
       RestTemplate restTemplate = new RestTemplate();
       PortfolioManager portfolioManager =
           PortfolioManagerFactory.getPortfolioManager(restTemplate);
       return portfolioManager.calculateAnnualizedReturn(readTradesFromJson(file), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

   //printJsonObject(mainReadFile(args));


    //printJsonObject(mainReadQuotes(args));



   // printJsonObject(mainCalculateSingleReturn(args));




    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

