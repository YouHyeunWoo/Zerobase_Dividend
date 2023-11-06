package com.example.stock.scraper;

import com.example.stock.model.Company;
import com.example.stock.model.Dividend;
import com.example.stock.model.ScrapedResult;
import com.example.stock.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d" +
            "&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400;

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0);

            Element tbody = tableEle.children().get(1); //tbody 태그에 있는 데이터는 인덱스1번이다, thead = get(0), tfoot = get(2)

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] str = txt.split(" ");
                int month = Month.strToNumber(str[0]); //객체를 생성하지 않고 바로 메소드를 사용할 수 있는 이유
                //strToNumber메소드가 static으로 구현을 했기 때문이다.
                int day = Integer.parseInt(str[1].replace(",", ""));
                int year = Integer.valueOf(str[2]);
                String dividend = str[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + str[0]);
                }
                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

//                System.out.println(year + "/" + month + "/" + day + " -> " + dividend);
            }
            scrapResult.setDividends(dividends);

        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        Connection connection = Jsoup.connect(url);

        try {
            Document document = connection.get();
            Element titleElement = document.getElementsByTag("h1").get(0);//get(0) 0번째인덱스 >> 타이틀 제목("h1")태그
            String title = titleElement.text().split("-")[1].trim();
            //ex abc - def 를 abc와 def로 나누고 abc 와 def를 가진 배열로 반환
            //trim() = 앞뒤의 공백을 제거해주는 메소드

            return new Company(ticker, title);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
